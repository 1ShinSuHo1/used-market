import os
from PIL import Image, ImageOps
from tqdm import tqdm
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import random

# 딥러닝
import torch
import torch.nn as nn
import torch.optim as optim
from torchvision import models, transforms
from torch.utils.data import Dataset, DataLoader
from sklearn.metrics import confusion_matrix, classification_report
from sklearn.model_selection import train_test_split

# 1. 설정값 정의해주기
DATA_DIR = "../data/train"   # 원본 데이터 루트(기기명/A,B,C 구조)
MODEL_DIR = "../models"

BATCH_SIZE = 32              # 미니 배치 크기
EPOCHS = 20                  # 최대 에포크 수 정의해주기
BASE_IMG_SIZE = 224          
VAL_RATIO = 0.2
SEED = 42

DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# 분류할 클래스 (A,B,C)
CLASSES = ["A", "B", "C"]

random.seed(SEED)
np.random.seed(SEED)
torch.manual_seed(SEED)

# 2. 전처리(비율유지+패딩 리사이즈)
def pad_resize_keep_ratio(img: Image.Image, size: int = 224, fill_color=(0, 0, 0)):
    img = ImageOps.exif_transpose(img) # EXIF 정보에 따라 회전보정 
    w, h = img.size
    scale = float(size) / max(w, h) # 긴변을 기준으로 스케일 계산
    new_w, new_h = int(round(w * scale)), int(round(h * scale))
    img = img.resize((new_w, new_h), Image.LANCZOS) # 고품질 리사이즈
    canvas = Image.new("RGB", (size, size), fill_color) # 검정배경 캔버스생성
    offset = ((size - new_w) // 2, (size - new_h) // 2) # 중앙 정렬
    canvas.paste(img, offset)
    return canvas

# 이미지 크기를 모바일넷이 요구하는 224X224로 통일시켜주기
def preprocess_images():
    print("[1단계] 이미지 크기 통일 중...")
    for root, _, files in os.walk(DATA_DIR):
        for fname in tqdm(files, desc=root): # 진행률을 눈으로 보기위해 사용
            if not fname.lower().endswith((".jpg", ".jpeg", ".png")):
                continue
            fpath = os.path.join(root, fname)
            try:
                with Image.open(fpath) as im:
                    im = im.convert("RGB")
                    out = pad_resize_keep_ratio(im, BASE_IMG_SIZE)
                    out.save(fpath, quality=95)
            except Exception as e:
                print(f"이미지 오류: {fpath} ({e})")
    print("모든 이미지가 224x224로 통일됨\n")

# 3. 리스트 기반 분할(기존 파일이동을 없애버림)
# 전체 폴더를 돌며 모든이미지 경로와 라벨을 수집
def collect_samples(root_dir):
    samples = []
    labels = []
    for device_folder in os.listdir(root_dir):
        device_path = os.path.join(root_dir, device_folder)
        if not os.path.isdir(device_path):
            continue
        for grade in CLASSES:
            grade_path = os.path.join(device_path, grade)
            if not os.path.isdir(grade_path):
                continue
            label = CLASSES.index(grade)
            for fname in os.listdir(grade_path):
                if fname.lower().endswith((".jpg", ".jpeg", ".png")):
                    samples.append(os.path.join(grade_path, fname))
                    labels.append(label)
    return samples, labels

# sklearn을 사용하여 검증셋분리 실제파일 이동 X
def split_indices(samples, labels, val_ratio=0.2, seed=42):
    idx = np.arange(len(samples))
    train_idx, val_idx = train_test_split(
        idx, test_size=val_ratio, random_state=seed, stratify=labels
    )
    return train_idx, val_idx

# 4. Dataset 정의
# 파일 경로 리스트와 라벨 리스트를 기반으로 이미지를 불러옴
class FileListABCDataset(Dataset):
    def __init__(self, files, labels, transform=None):
        self.files = files
        self.labels = labels
        self.transform = transform

    def __len__(self):
        return len(self.files)

    def __getitem__(self, idx):
        path = self.files[idx]
        label = self.labels[idx]
        image = Image.open(path).convert("RGB")
        if self.transform:
            image = self.transform(image)
        return image, label

# 5. 학습 파이프라인
# 데이터 증강 강화,모바일넷 부분 파인튜닝, adamw + onecycle
def train_model_v3():
    print("[2단계] 데이터 로드 및 분할")

    # 데이터 전체 목록 수집
    all_files, all_labels = collect_samples(DATA_DIR)

    # 학습/검증 인덱스 분할
    train_idx, val_idx = split_indices(all_files, all_labels, VAL_RATIO, SEED)

    train_files = [all_files[i] for i in train_idx] 
    train_labels = [all_labels[i] for i in train_idx] 
    val_files = [all_files[i] for i in val_idx]
    val_labels = [all_labels[i] for i in val_idx]

    print(f"학습 데이터: {len(train_files)} / 검증 데이터: {len(val_files)}")

    # 전처리(증강 강화)
    train_tf = transforms.Compose([
        transforms.Resize((BASE_IMG_SIZE, BASE_IMG_SIZE)), 
        transforms.RandomHorizontalFlip(),  # 좌우 반전
        transforms.RandomVerticalFlip(),    # 상하 반전
        transforms.RandomRotation(15),      # 15도 반전
        transforms.ColorJitter(brightness=0.4, contrast=0.4, saturation=0.4, hue=0.2), # 색변화
        transforms.RandomAffine(degrees=10, translate=(0.1, 0.1), scale=(0.9, 1.1)),   # 이동/확대
        transforms.RandomPerspective(distortion_scale=0.4, p=0.5),  # 원근 왜곡
        transforms.RandomAdjustSharpness(sharpness_factor=2, p=0.5),    # 선명도 조정
        transforms.ToTensor(),
        transforms.RandomErasing(p=0.3, scale=(0.02, 0.15)), #일부 영역지우기
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225]), #정규화
    ])

    val_tf = transforms.Compose([
        transforms.Resize((BASE_IMG_SIZE, BASE_IMG_SIZE)),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225]),
    ])

    # 데이터셋과 데이터 로더 정의
    train_ds = FileListABCDataset(train_files, train_labels, transform=train_tf)
    val_ds = FileListABCDataset(val_files, val_labels, transform=val_tf)

    train_loader = DataLoader(train_ds, batch_size=BATCH_SIZE, shuffle=True, num_workers=0)
    val_loader = DataLoader(val_ds, batch_size=BATCH_SIZE, shuffle=False, num_workers=0)

    print("[3단계] 모델 준비(v3)")

    model = models.mobilenet_v2(weights=models.MobileNet_V2_Weights.IMAGENET1K_V1)
    # 전체 동결
    for p in model.parameters():
        p.requires_grad = False
    # 마지막 3개 블록 + classifier 미세조정
    for blk in list(model.features.children())[-3:]:
        for p in blk.parameters():
            p.requires_grad = True

    
    model.classifier = nn.Sequential(
        nn.Dropout(p=0.4),
        nn.Linear(model.last_channel, len(CLASSES))
    )
    for p in model.classifier.parameters():
        p.requires_grad = True

    model = model.to(DEVICE)


    # 손실함수 및 옵티마이저 및 스케줄러 설정
    criterion = nn.CrossEntropyLoss()
    
    optimizer = optim.AdamW(filter(lambda p: p.requires_grad, model.parameters()),
                            lr=1e-3, weight_decay=1e-4)
    
    # 스케줄러 학습률을 한 사이클로 조정
    scheduler = optim.lr_scheduler.OneCycleLR(
        optimizer,
        max_lr=1e-3,
        steps_per_epoch=len(train_loader),
        epochs=EPOCHS
    )

    os.makedirs(MODEL_DIR, exist_ok=True)
    model_path = os.path.join(MODEL_DIR, "mobilenet_quality_v3.pth")

    best_val_loss = float('inf')
    patience, trigger = 3, 0

    train_losses, val_losses, val_accuracies = [], [], []

    print("[4단계] 학습 시작")
    for epoch in range(EPOCHS):
        print(f"\nEpoch {epoch + 1}/{EPOCHS}")
        model.train()
        running_loss = 0.0

        # 학습 루프
        for imgs, labels in tqdm(train_loader, desc="Training"):
            imgs, labels = imgs.to(DEVICE), labels.to(DEVICE)
            optimizer.zero_grad() #Gradient 초기화
            outputs = model(imgs) #순전파
            loss = criterion(outputs, labels)
            loss.backward()       # 역전파
            optimizer.step()      # 가중치 갱신
            scheduler.step()      # 학습률 조정
            running_loss += loss.item()

        avg_train_loss = running_loss / max(1, len(train_loader))

        # 검증
        model.eval()
        correct, total, val_loss = 0, 0, 0.0
        all_preds, all_gt = [], []
        with torch.no_grad():
            for imgs, labels in val_loader:
                imgs, labels = imgs.to(DEVICE), labels.to(DEVICE)
                outputs = model(imgs)
                loss = criterion(outputs, labels)
                val_loss += loss.item()

                _, preds = torch.max(outputs, 1)
                correct += (preds == labels).sum().item()
                total += labels.size(0)
                all_preds.extend(preds.cpu().numpy())
                all_gt.extend(labels.cpu().numpy())

        avg_val_loss = val_loss / max(1, len(val_loader))
        val_acc = correct / max(1, total) * 100.0

        train_losses.append(avg_train_loss)
        val_losses.append(avg_val_loss)
        val_accuracies.append(val_acc)

        print(f"train_loss: {avg_train_loss:.4f} | val_loss: {avg_val_loss:.4f} | val_acc: {val_acc:.2f}%")

        if avg_val_loss < best_val_loss:
            best_val_loss = avg_val_loss
            trigger = 0
            torch.save(model.state_dict(), model_path)
        else:
            trigger += 1
            if trigger >= patience:
                print("Early stopping triggered")
                break

    print(f"\n학습 완료. 모델 저장됨 → {model_path}")

    # 시각화
    epochs_range = range(1, len(train_losses) + 1)
    plt.figure(figsize=(10, 4))

    plt.subplot(1, 2, 1)
    plt.plot(epochs_range, train_losses, label="Train Loss")
    plt.plot(epochs_range, val_losses, label="Val Loss")
    plt.title("Loss Curve")
    plt.xlabel("Epoch")
    plt.ylabel("Loss")
    plt.legend()
    plt.grid(True)

    plt.subplot(1, 2, 2)
    plt.plot(epochs_range, val_accuracies, label="Val Accuracy")
    plt.title("Validation Accuracy")
    plt.xlabel("Epoch")
    plt.ylabel("Accuracy (%)")
    plt.legend()
    plt.grid(True)

    plt.tight_layout()
    plt.show()

    # 결과 분석
    print("\n[결과 분석]")
    cm = confusion_matrix(all_gt, all_preds)
    print("\nConfusion Matrix:\n", cm)
    print("\nClassification Report:\n", classification_report(all_gt, all_preds, target_names=CLASSES))

    plt.figure(figsize=(6,5))
    sns.heatmap(cm, annot=True, fmt="d", cmap="Blues", xticklabels=CLASSES, yticklabels=CLASSES)
    plt.xlabel("예측 클래스")
    plt.ylabel("실제 클래스")
    plt.title("혼동 행렬 (Confusion Matrix)")
    plt.tight_layout()
    plt.show()

# 실행
if __name__ == "__main__":
    preprocess_images()
    train_model_v3()
