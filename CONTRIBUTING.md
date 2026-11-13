# 작업 규칙

## 1. 브랜치

- `main`에는 직접 올리지 않아요. 항상 실행되는 상태로 유지해요.
- 각자 자기 브랜치에서 작업해요.

| 담당 | 브랜치 이름 예시 |
|---|---|
| A | `a/screen-navigation`, `a/vehicle-sdk` |
| B | `b/growth`, `b/scenario` |
| C | `c/avatar-images` |
| D | `d/lines` |
| E | `e/scenarios` |

## 2. 올리는 순서

1. 작업을 시작하기 전에 최신 `main`을 받아요: `git pull origin main`
2. 내 브랜치를 만들어요: `git checkout -b b/growth`
3. 작업하고 커밋해요.
4. GitHub에 올려요: `git push origin b/growth`
5. GitHub에서 **Pull Request**를 만들어요.
6. A가 확인하고 `main`에 합쳐요.

## 3. 커밋 메시지

`[담당] 무엇을 했는지` 형식으로 한 줄로 써요.

- `[A] 홈 화면과 꾸미기 화면 연결`
- `[B] 고속도로 주행 성장 계산 추가`
- `[C] 아바타 기본 이미지 추가`
- `[D] 첫 터널 대사 추가`
- `[E] 출퇴근 1주 시나리오 추가`

## 4. 파일 이름 규칙

### 이미지 (C)

소문자 영어와 밑줄(`_`)만 써요. 안드로이드는 대문자, 띄어쓰기, 한글 파일 이름을 쓸 수 없어요.

| 종류 | 형식 | 예시 |
|---|---|---|
| 아바타 기본 | `avatar_<상태>.png` | `avatar_baby.png` |
| 무늬 | `pattern_<이름>_<번호>.png` | `pattern_stripe_01.png` |
| 파츠 | `part_<슬롯>_<이름>.png` | `part_head_lantern.png` |

슬롯은 `head`, `body`, `acc`(액세서리) 세 가지예요.

### 대사 (D)

`app/src/main/assets/lines/lines.txt`에 한 줄에 하나씩 `키 = 대사` 형식으로 써요.

```
first_tunnel = 터널 지나서 눈이 반짝이게 됐어!
camping_arrive = 산 냄새 좋다! 다음에 또 오자
```

키는 소문자 영어와 밑줄만 써요. 키 목록은 A와 B가 정해서 알려줘요.

### 시나리오 (E)

형식은 B가 `scenarios/example.csv`에 예시를 만들면 그대로 복사해서 채워요.

## 5. 올리면 안 되는 것

- 비밀번호, API 키, `local.properties`
- `build/` 폴더 (자동으로 제외돼요)
