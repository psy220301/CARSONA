# 작업 규칙 (1차 제출까지)

한 줄 요약: **받고(Pull) → 작업하고 → 커밋하고 → 올린다(Push)**

## 1. 팀 규칙

1. 작업 시작 = Pull, 작업 끝 = Commit & Push
2. 브랜치 없이 `main`에 바로 올려요. 대신 **자기 담당 폴더만** 수정해요 (충돌 예방)
3. 커밋 메시지는 한글로 "무엇을 했는지" 써요 (예: `아기 토토 이미지 추가`)
4. 한 번에 몰아서 말고 완성될 때마다 나눠서 커밋해요
5. 비밀번호, 서명 키(`keystore.properties`, `*.jks`), Pleos Client ID·Secret은 절대 올리지 않아요
6. 개발자는 Push 전에 에뮬레이터에서 앱이 실행되는지 확인해요
7. 5명 모두 자기 계정으로 최소 1번 커밋해요 (제출 전 Insights → Contributors에서 확인)

## 2. 담당 폴더

| 역할 | 맡는 일 | 커밋하는 곳 |
|---|---|---|
| 개발 1 | 화면 4개, 차량 신호, 통합, GitHub, APK, README, 녹화 | `app/` 화면·차량 코드, `apk/`, `scripts/` |
| 개발 2 | 성장·성향 계산, 시나리오 재생, (선택) TTS | `app/src/main/java/com/carsona/app/growth/` 등 로직 코드 |
| 디자인 1 | 토토 3단계 그림, (선택) 파츠 1~2개 | `app/src/main/res/drawable/` |
| 디자인 2 | 배경 2장, 영상 썸네일 | `app/src/main/res/drawable/`, `docs/images/` |
| 기획 | 기획서, 대사, 시나리오, 영상 편집, 제출 | `docs/`, `app/src/main/assets/` |

## 3. 파일 이름

영어 소문자, 숫자, 밑줄(`_`)만 써요. 한글·대문자·띄어쓰기가 있으면 앱이 빌드되지 않아요.

| 파일 | 위치 | 내용 |
|---|---|---|
| `avatar_stage0.png` / `avatar_stage1.png` / `avatar_stage2.png` | `res/drawable/` | 아기 / 청소년 / 성인 토토. 1024×1024 투명 PNG, 3장 같은 위치 |
| `part_helmet.png`, `part_leaf.png` | `res/drawable/` | (선택) 머리 파츠. 몸은 지우고 파츠만 |
| `bg_default.png`, `bg_reveal.png` | `res/drawable/` | 홈·첫 만남 배경 / 성장 공개 배경 |
| `dialogues.json` | `assets/` | 대사 (공통 약속 4) |
| `scenarios/commute.json` | `assets/` | 시나리오 (공통 약속 3) |

정해진 이름으로 올리면 코드 수정 없이 앱에 바로 나와요. 이름이 틀려도 괜찮으니 일단 올리고 개발 1에게 알려 주세요.

## 4. 커밋하는 법

### 디자인·기획 (웹사이트, 설치 없음)

- 파일 올리기: 저장소에서 올릴 폴더로 이동 → 오른쪽 위 **Add file → Upload files** → 파일 끌어다 놓기 → 메시지 적고 **Commit changes**
- 텍스트 고치기: 파일 클릭 → 연필 아이콘 → 수정 → **Commit changes**

### 개발자 (Android Studio)

1. 작업 시작 전: **Git → Pull**
2. 작업 후: **Git → Commit** → 바뀐 파일 체크 → 메시지 → **Commit and Push**
3. 처음 한 번: Git 이름은 실명 대신 **GitHub 아이디**로, 이메일은 GitHub 가입 이메일로 설정해요. 저장소를 공개하면 커밋 이름이 보이는데, 공모전 규정상 실명이 노출되면 안 돼요.

```
git config --global user.name "깃허브아이디"
git config --global user.email "깃허브가입이메일"
```

## 5. 충돌이 났을 때

"conflict" 메시지가 나오면 더 진행하지 말고 단톡에 화면 캡처를 공유하고, 그 파일 담당자나 개발 1과 같이 봐요.
