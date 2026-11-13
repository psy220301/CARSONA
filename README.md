# CARSONA

운전하면 자라는 우리 차 아바타 앱 (현대자동차·기아 차량용 앱 개발 공모전)

- 플랫폼: Pleos Connect (Android Automotive OS 기반)
- 개발 환경: Android Studio, Kotlin, Pleos Connect Emulator 2.1.0 (API 34)
- 최소 SDK: API 28

## 시연 영상

(영상 담당 E가 링크를 넣어주세요)

## 팀 역할

| 담당 | 맡은 일 | 작업 폴더 |
|---|---|---|
| A. 개발 리더 | 앱 뼈대, 화면 연결, 차량 SDK 연동 | `app/` |
| B. 개발 | 성장 계산, 시나리오 재생, TTS | `app/` |
| C. 디자인 | 아바타·무늬·파츠 이미지 | `app/src/main/res/drawable/` |
| D. 기획서 | 기획 문서, 아바타 대사 텍스트 | `docs/`, `app/src/main/assets/lines/` |
| E. 영상 | 시연 시나리오 데이터, 영상 대본 | `scenarios/`, `docs/` |

## 폴더 구조

```
app/src/main/java/.../carsona/
 ├─ vehicle/    A: 차량 SDK 연동, VehicleDataSource
 ├─ ui/         A: 화면, 화면 이동, 주행/정차 모드 전환
 ├─ growth/     B: 성장 계산
 ├─ scenario/   B: 시나리오 재생
 └─ voice/      B: TTS
app/src/main/res/drawable/   C: 아바타·무늬·파츠 이미지
app/src/main/assets/lines/   D: 아바타 대사 텍스트
scenarios/                   E: 시연 시나리오 데이터
docs/                        D, E: 기획서, 영상 대본
```

## 처음 시작하는 법

1. 저장소를 내려받아요: `git clone <저장소 주소>`
2. Android Studio에서 이 폴더를 열어요.
3. Pleos Connect Emulator를 실행하고 앱을 실행해요.
4. 작업 규칙은 [CONTRIBUTING.md](CONTRIBUTING.md)를 확인해요.
