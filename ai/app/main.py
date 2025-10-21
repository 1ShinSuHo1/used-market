from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from app.predict import predict_image
import torch

# ==========================================
# ✅ FastAPI 앱 초기화
# ==========================================
app = FastAPI(
    title="AI Quality Predictor",
    description="Used-Market 프로젝트용 AI 품질 예측 API",
    version="1.0.0"
)

# ==========================================
# ✅ 요청 스키마
# ==========================================
class PredictRequest(BaseModel):
    image_url: str

# ==========================================
# ✅ 헬스체크 (Spring에서 서버 상태 확인용)
# ==========================================
@app.get("/health")
async def health():
    device = "mps" if torch.backends.mps.is_available() else "cuda" if torch.cuda.is_available() else "cpu"
    return {"status": "ok", "device": device}

# ==========================================
# ✅ AI 품질 예측 API
# ==========================================
@app.post("/predict")
async def predict(req: PredictRequest):
    try:
        # 이미지 경로 입력받아 예측 수행
        grade, confidence = predict_image(req.image_url)
        return {"grade": grade, "confidence": confidence}
    except FileNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"AI 예측 중 오류 발생: {str(e)}")
