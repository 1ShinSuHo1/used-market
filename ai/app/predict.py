import torch
import torch.nn as nn
import requests
from io import BytesIO


#이미지 모델 전처리 도구
from torchvision import transforms, models
# 이미지를 파이썬에서 열고 다룰 수 있게 해주는 라이브러리
from PIL import Image
import os


# 1. 기본 설정
# 현재 이파일의 절대 경로를 구하고 학습해서 저장해둔 모델 파일의 경로를 만든다 그리고 절대경로로 바꾼다
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "../models/mobilenet_quality_confusion.pth")
MODEL_PATH = os.path.abspath(MODEL_PATH)

DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")
CLASS_NAMES = ["A", "B", "C"]  # 등급 라벨


# 2. 모델 불러오기
print(" 모델 로딩 중...")

# MobileNetV2 모델을 가져온다.ImageNet데이터로 미리 학습된 가중치를 사용
model = models.mobilenet_v2(weights=models.MobileNet_V2_Weights.IMAGENET1K_V1)
# 원래 MobileNetV2의 마지막분류층은 1000개이다 우리는 A,B,C만 필요하므로 마지막 층을 3개로 바꾼다
model.classifier[1] = nn.Linear(model.last_channel, len(CLASS_NAMES))
model.load_state_dict(torch.load(MODEL_PATH, map_location=DEVICE))
model = model.to(DEVICE)

# 모델을 학습이 아닌 예측모드로 동작하게함
model.eval()
print(" 모델 로드 완료!\n")


# 3. 이미지 전처리 설정 (학습 시와 동일하게)
# 이미지를 모델이 좋아하는 모양으로 바꿔준다
transform = transforms.Compose([
    transforms.Resize((224, 224)), # 크기를 통일해주고
    transforms.ToTensor(), # 텐서로 변환
    transforms.Normalize(mean=[0.485, 0.456, 0.406], #평균 과 그아래는 표준편차
                         std=[0.229, 0.224, 0.225])
])


# 4. 예측 함수
# 하나의 이미지 경로를 받아서 등급과 확신도를 돌려준다
def predict_image(image_path):
    # 1. 웹 URL인지 확인
    if image_path.startswith("http://") or image_path.startswith("https://"):
        try:
            response = requests.get(image_path, timeout=10)
            response.raise_for_status()
            img = Image.open(BytesIO(response.content)).convert("RGB")
        except Exception as e:
            raise FileNotFoundError(f" URL에서 이미지를 불러올 수 없습니다: {e}")
    else:
        # 2. 로컬 파일인 경우
        if not os.path.exists(image_path):
            raise FileNotFoundError(f" 파일을 찾을 수 없습니다: {image_path}")
        img = Image.open(image_path).convert("RGB")
    
    
    img_tensor = transform(img).unsqueeze(0).to(DEVICE)

    # 예측할때는 기울기 계산이 필요없으므로 no_grad()로 감싸주기
    with torch.no_grad():
        #모델에 이미지를 넣으면 각클래스에 대한 점수가 나온다
        outputs = model(img_tensor)
        
        #점수를 확률로 바꿔준다
        probs = torch.softmax(outputs, dim=1)[0]

        # 가장확률이 큰 클래스의 위치를 골라준다
        pred_idx = torch.argmax(probs).item()

        # 확신도를 꺼내주고 0~1의 값을 0~100으로 바꿔준다
        confidence = probs[pred_idx].item() * 100

    # 콘솔 로그 
    print(f" 입력 이미지: {os.path.basename(image_path)}")
    print(f" 예측 결과: {CLASS_NAMES[pred_idx]} 등급 ({confidence:.2f}%)")

    # Fast API가 바로 JSON으로 내보낼 수 있게 문자와 숫자를 변환해준다
    return CLASS_NAMES[pred_idx], round(confidence, 2)


# 5. 실행부
if __name__ == "__main__":
    test_image = input("예측할 이미지 경로를 입력하세요: ").strip()
    predict_image(test_image)
