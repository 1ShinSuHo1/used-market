import os
import random
import shutil
from PIL import Image, ImageOps
from tqdm import tqdm
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np

import torch
import torch.nn as nn
import torch.optim as optim
from torchvision import models, transforms
from torch.utils.data import Dataset, DataLoader
from sklearn.metrics import confusion_matrix, classification_report

# ============================================================
# 1️⃣ 설정값
# ============================================================
DATA_DIR = "../data"
TRAIN_DIR = os.path.join(DATA_DIR, "train")
VAL_DIR = os.path.join(DATA_DIR, "val")
MODEL_DIR = "../models"
BATCH_SIZE = 32
EPOCHS = 10
LR = 1e-4
IMG_SIZE = 224
VAL_RATIO = 0.2
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")

CLASSES = ["A", "B", "C"]

# ============================================================
# 2️⃣ (전처리) 비율유지+패딩 리사이즈
# ============================================================
def pad_resize_keep_ratio(img: Image.Image, size: int = 224, fill_color=(0, 0, 0)):
    img = ImageOps.exif_transpose(img)
    w, h = img.size
    scale = float(size) / max(w, h)
    new_w, new_h = int(round(w * scale)), int(round(h * scale))
    img = img.resize((new_w, new_h), Image.LANCZOS)
    canvas = Image.new("RGB", (size, size), fill_color)
    offset = ((size - new_w) // 2, (size - new_h) // 2)
    canvas.paste(img, offset)
    return canvas

def preprocess_images():
    print("🖼️ [1단계] 이미지 크기 통일 중...")
    for root, _, files in os.walk(DATA_DIR):
        for fname in tqdm(files, desc=root):
            if not fname.lower().endswith((".jpg", ".jpeg", ".png")):
                continue
            fpath = os.path.join(root, fname)
            try:
                with Image.open(fpath) as im:
                    im = im.convert("RGB")
                    out = pad_resize_keep_ratio(im, IMG_SIZE)
                    out.save(fpath, quality=95)
            except Exception as e:
                print(f"❌ 이미지 오류: {fpath} ({e})")
    print("✅ 모든 이미지가 224x224(비율 유지)로 통일됨!\n")

# ============================================================
# 3️⃣ (데이터 분리) 8:2 비율로 이동
# ============================================================
def split_dataset():
    print("📦 [2단계] 데이터셋 train/val 분리 중...")

    if os.path.exists(VAL_DIR):
        shutil.rmtree(VAL_DIR)
    os.makedirs(VAL_DIR, exist_ok=True)

    random.seed(42)
    for device in os.listdir(TRAIN_DIR):
        device_path = os.path.join(TRAIN_DIR, device)
        if not os.path.isdir(device_path):
            continue

        for grade in CLASSES:
            grade_path = os.path.join(device_path, grade)
            if not os.path.isdir(grade_path):
                continue

            val_grade_path = os.path.join(VAL_DIR, device, grade)
            os.makedirs(val_grade_path, exist_ok=True)

            images = [f for f in os.listdir(grade_path) if f.lower().endswith((".jpg", ".jpeg", ".png"))]
            random.shuffle(images)
            n_val = int(len(images) * VAL_RATIO)
            val_images = images[:n_val]

            for img in val_images:
                shutil.move(os.path.join(grade_path, img), os.path.join(val_grade_path, img))

            print(f"{device}/{grade}: 총 {len(images)}장 중 {n_val}장을 val로 이동")
    print("✅ train/val 분리 완료!\n")

# ============================================================
# 4️⃣ Dataset 정의
# ============================================================
class UnifiedABCDataset(Dataset):
    def __init__(self, root_dir, transform=None):
        self.samples = []
        self.transform = transform
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
                        self.samples.append((os.path.join(grade_path, fname), label))

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, idx):
        path, label = self.samples[idx]
        image = Image.open(path).convert("RGB")
        if self.transform:
            image = self.transform(image)
        return image, label

# ============================================================
# 5️⃣ 학습 파이프라인 + 혼동 행렬
# ============================================================
def train_model():
    print("🧠 [3단계] 모델 학습 시작...")

    train_tf = transforms.Compose([
        transforms.Resize((IMG_SIZE, IMG_SIZE)),
        transforms.RandomHorizontalFlip(),
        transforms.RandomRotation(10),
        transforms.ColorJitter(0.2, 0.2, 0.2),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ])

    val_tf = transforms.Compose([
        transforms.Resize((IMG_SIZE, IMG_SIZE)),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ])

    train_data = UnifiedABCDataset(TRAIN_DIR, transform=train_tf)
    val_data = UnifiedABCDataset(VAL_DIR, transform=val_tf)
    train_loader = DataLoader(train_data, batch_size=BATCH_SIZE, shuffle=True)
    val_loader = DataLoader(val_data, batch_size=BATCH_SIZE, shuffle=False)

    print(f"📊 학습 데이터: {len(train_data)}장 / 검증 데이터: {len(val_data)}장")

    model = models.mobilenet_v2(weights=models.MobileNet_V2_Weights.IMAGENET1K_V1)
    model.classifier[1] = nn.Linear(model.last_channel, len(CLASSES))
    model = model.to(DEVICE)

    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=LR)

    for epoch in range(EPOCHS):
        print(f"\n🧩 Epoch {epoch + 1}/{EPOCHS}")
        model.train()
        running_loss = 0.0

        for imgs, labels in tqdm(train_loader, desc="Training"):
            imgs, labels = imgs.to(DEVICE), labels.to(DEVICE)
            optimizer.zero_grad()
            outputs = model(imgs)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()
            running_loss += loss.item()

        avg_train_loss = running_loss / len(train_loader)

        # ---- 검증 ----
        model.eval()
        correct, total, val_loss = 0, 0, 0.0
        all_preds, all_labels = [], []
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
                all_labels.extend(labels.cpu().numpy())

        avg_val_loss = val_loss / len(val_loader)
        val_acc = correct / total * 100
        print(f"train_loss: {avg_train_loss:.4f} | val_loss: {avg_val_loss:.4f} | val_acc: {val_acc:.2f}%")

    # ---- 저장 ----
    os.makedirs(MODEL_DIR, exist_ok=True)
    model_path = os.path.join(MODEL_DIR, "mobilenet_quality_confusion.pth")
    torch.save(model.state_dict(), model_path)
    print(f"\n✅ 학습 완료! 모델이 저장되었습니다 → {model_path}")

    # ============================================================
    # 6️⃣ 혼동 행렬 + 리포트
    # ============================================================
    print("\n📊 [결과 분석] 혼동 행렬 및 클래스별 리포트")
    cm = confusion_matrix(all_labels, all_preds)
    print("\nConfusion Matrix:\n", cm)
    print("\nClassification Report:\n", classification_report(all_labels, all_preds, target_names=CLASSES))

    plt.figure(figsize=(6,5))
    sns.heatmap(cm, annot=True, fmt="d", cmap="Blues", xticklabels=CLASSES, yticklabels=CLASSES)
    plt.xlabel("예측 클래스")
    plt.ylabel("실제 클래스")
    plt.title("혼동 행렬 (Confusion Matrix)")
    plt.tight_layout()
    plt.show()

# ============================================================
# 실행 순서
# ============================================================
if __name__ == "__main__":
    preprocess_images()
    split_dataset()
    train_model()
