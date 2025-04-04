## 📖 소개 - Learning Spring AI
본 공부 내용은 **Spring AI** 프레임워크를 활용하여 OpenAI(ChatGPT), Google Gemini API와 연동해 텍스트 및 이미지 생성 기능, 음식 사진을 보내면 음식 이름, 정보, 설명 반환 기능을 구현한 것을 기록했습니다.

또한 **Stable Diffusion(WebUI)** 를 사용해 이미지를 생성하고, 그 결과를 저장하는 API 구현 방법도 포함되어 있습니다.

- **텍스트 기반 응답**: OpenAI 및 Google Gemini API
- **이미지 생성 및 저장**: Stable Diffusion 및 Gemini API의 Base64 이미지 응답을 PNG 파일로 저장
- **이미지 기반 응답**: Google Gemini API


## 🧩 Spring AI란?
> [공식문서 참고](https://spring.io/projects/spring-ai)

- **Spring AI**는 AI 엔지니어링을 위한 Spring 생태계 확장 프레임워크입니다.
- 모듈화, 이식성, POJO 기반 구성을 AI 응용 개발에 적용.
- 다양한 AI 모델 (OpenAI, Google Gemini, Microsoft Azure, Amazon, Ollama 등) 지원.
- 주요 기능:
  - Chat Completion (대화 응답)
  - Embedding (임베딩)
  - Text-to-Image (텍스트 → 이미지)
  - Audio Transcription (음성 → 텍스트)
  - Text-to-Speech (텍스트 → 음성)
  - Moderation (콘텐츠 필터링)


## ⚙️ 설치 및 준비 사항
### 📑각각 Model
- ChatGPT : gpt-3.5-turbo (temperature=0.7 , max-tokens=100)
- Gemini : gemini-2.0-flash-exp-image-generation (temperature=1 , max-tokens=8192)
  
**+ API Key 발급 URL**

OpenAI (ChatGPT) API Key 발급: https://platform.openai.com/api-keys

Google Gemini API Key 발급: https://ai.google.dev/gemini-api/docs?hl=ko

### application.properties에 기록
```bash
spring.mvc.async.request-timeout=600000
spring.ai.openai.api-key=${GPT_API}
spring.ai.openai.chat.options.model=gpt-3.5-turbo
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.max-tokens=100

spring.ai.google.api-key=${GEMINI_API}
spring.ai.google.chat.options.model=gemini-2.0-flash-exp-image-generation
spring.ai.google.chat.options.temperature=1
spring.ai.google.chat.options.max-tokens=8192
```
---

### ✅ 필수 환경
- Windows OS
- Python 3.10.x (권장: 3.10.6)
- Git
- NVIDIA GPU 드라이버 (GPU 사용 시)

### ✅ Stable Diffusion WebUI 설치 및 실행 방법
```bash
git clone https://github.com/AUTOMATIC1111/stable-diffusion-webui.git
cd stable-diffusion-webui
```
- 모델 다운로드 (HuggingFace 등) 후 `models/Stable-diffusion` 폴더에 추가
- 기본 실행 명령어:
```bash
py -3.10 launch.py --api --skip-torch-cuda-test --no-half --precision full
```
- 실행 후 `http://127.0.0.1:7860` 접속 가능

**⚠️ 실행 오류 시**:
- `webui-user.bat` 파일을 다음과 같이 수정:
```batch
@echo off

set PYTHON=C:\Users\{본인유저명}\AppData\Local\Programs\Python\Python310\python.exe
set GIT=
set VENV_DIR=
set COMMANDLINE_ARGS=--api --skip-torch-cuda-test --no-half --use-cpu all

call webui.bat
```
- 이후 CMD 창에서 다음 명령어를 입력해 가상환경 생성 및 실행:
```bash
C:\Users\{본인유저명}\AppData\Local\Programs\Python\Python310\python.exe -m venv venv
venv\Scripts\activate
webui-user.bat
```

## 💡 실행 중 발생 가능한 오류 및 TIP
| 상황 | 해결 방법 |
|------|------------|
| GPU가 없거나 인식되지 않을 때 | `--use-cpu all` 옵션 추가 후 실행 |
| 실행 중 느린 속도 발생 | width/height 값 256으로 낮추고 steps 10~15로 조절 |
| 긴 요청 처리 중 타임아웃 | Spring `async-timeout` 또는 `server.tomcat.async-timeout` 값 조정 |
| 대기 시간이 너무 길 때 | 비동기 큐 처리 및 상태 확인용 폴링 방식 추천 |
| 모델이 없다는 오류 발생 | HuggingFace 등에서 `.ckpt` 또는 `.safetensors` 파일 다운로드 후 `models/Stable-diffusion` 폴더에 배치 |


## 📂 디렉토리 구조
```
learning/
├─ Config/
├─ Controller/
├─ Service/
├─ ServiceImpl/
├─ Util/
├─ DTO/
```


## 🚀 API 엔드포인트 정리
| 메서드 | 경로 | 기능 |
|--------|------|------|
| POST | /api/chat | OpenAI(ChatGPT) + Gemini Text 동시 응답 반환 |
| POST | /api/gemini-image | Gemini 이미지 생성 및 JSON 반환 |
| POST | /api/sd-image | Stable Diffusion 이미지 생성 (Base64 JSON 포함) 반환 |
| POST | /api/save-sd-image | SD JSON 응답 전달 → 이미지 저장 |
| POST | /api/save-gemini-image | Gemini JSON 응답 전달 → 이미지 저장 |
| POST | /api/food-info |	음식 이미지 업로드 → 음식명, 설명, 영양 정보 JSON 반환 (Gemini Vision 활용) |


## 🛠 서비스 및 Util 설명
### ✅ ChatServiceImpl 주요 기능
- OpenAI API 호출 (OpenAiChatModel)
- Google Gemini API 비동기 WebClient 호출
- Stable Diffusion API 호출 및 JSON 반환
- 응답 내 Base64 이미지 추출 후 파일 저장
- 음식 이미지 분석 기능 추가:
  - Gemini Vision API를 통해 이미지에서 음식 식별
  - 인식된 음식의 이름, 간단한 설명, 칼로리·단백질·지방 등의 영양 성분 추정값 제공

### ✅ ImageSaveUtil
- `saveGeminiImageFromResponse()`: Gemini JSON → Base64 추출 및 이미지 저장
- `saveSdImageFromResponse()`: SD JSON → Base64 추출 및 이미지 저장
- `createOutputPath()`: 자동 저장 경로 및 파일명 생성 함수


## 🔎 사용 흐름 요약
```
Gemini 이미지를 저장할 때
1. /gemini-image → TEXT + IMAGE JSON 반환
2. /save-gemini-image → JSON 전달 후 이미지 저장

SD 이미지를 저장할 때
1. /sd-image → Stable Diffusion API 호출 및 JSON 반환
2. /save-sd-image → SD JSON 전달 후 이미지 저장

음식 이미지 정보 분석 흐름:
1. /api/food-info → 이미지 업로드 및 분석 요청
2. Gemini Vision API를 통해 음식 이름 + 설명 + 영양 정보 JSON 형식 반환
```


## ⚠️ 주의사항 및 경험 정리
- **ChatGPT API 사용**: 유료 API 키 필요
- **Gemini API 토큰 제한**: 32,768 tokens 초과 시 오류 발생
- **WebClient 메모리 제한**: 최소 10MB 이상으로 설정 필요
- Base64 응답 내 개행/공백 제거 필수 (`replaceAll("\\s+", "")`)
- SD 응답 전달 시 전체 JSON 포맷으로 전달해야 오류 방지


## ✅ 참고 자료
- [Spring AI 공식문서](https://spring.io/projects/spring-ai)
- [Google Gemini API 이미지 생성 가이드](https://ai.google.dev/gemini-api/docs/image-generation?hl=ko)
- [Stable Diffusion WebUI GitHub](https://github.com/AUTOMATIC1111/stable-diffusion-webui)

## 📸 실제 Postman 테스트 결과
| 테스트 API | 설명 | 요청 스크린샷 | 응답 결과 이미지 |
|------------|------|----------------|-----------------|
| /api/chat | Gemini & ChatGPT 동시 응답 확인 | <img src="https://github.com/user-attachments/assets/d0ab1b98-b0b9-4e3d-9a34-a6e6c4a93b8b" width="400"/> | - |
| /api/sd-image | Stable Diffusion 이미지 생성 응답 | <img src="https://github.com/user-attachments/assets/93cfe73d-66fd-41d6-b4e3-f317702aa81f" width="400"/> | - |
| /api/save-sd-image | SD JSON 응답 전달 및 저장 요청 | <img src="https://github.com/user-attachments/assets/e518677b-9684-40e0-8130-633857269818" width="400"/> | <img src="https://github.com/user-attachments/assets/a13936a5-6e75-46eb-9695-85cca18e64c1" width="400"/> |
| /api/gemini-image | Gemini 이미지 생성 응답 확인 | <img src="https://github.com/user-attachments/assets/f1dfa3f6-a48b-4085-8b02-fe65771370e8" width="400"/> | - |
| /api/save-gemini-image | Gemini JSON 응답 전달 및 저장 요청 | <img src="https://github.com/user-attachments/assets/ad902c3b-4ea5-4061-8a44-d0e8fd595867" width="400"/> | <img src="https://github.com/user-attachments/assets/b70e57c6-96c2-45a6-9132-c9a5422725ee" width="400"/> |
| /api/food-info | 음식 이미지 분석 (음식명, 설명, 영양 정보 반환) | <img src="https://github.com/user-attachments/assets/food-request.png" width="400"/> | <img src="https://github.com/user-attachments/assets/food-response.png" width="400"/> |



---
> 작성일: 2025-04-04


