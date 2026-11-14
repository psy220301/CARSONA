# CARSONA (카르소나)

**평소처럼 차를 타기만 하면, 우리 가족이 자주 가는 곳과 안전 운전 습관을 닮아 자라는 차량 아바타 '토토'**

현대자동차그룹 차량용 앱 개발 공모전 출품작 · 팀 **카소랩 (CarsoLab)** · Pleos Connect (Android Automotive OS)

## 시연 영상

(영상 링크를 넣어 주세요)

## APK

- 위치: [`apk/CarsoLab_CARSONA_release.apk`](apk/CarsoLab_CARSONA_release.apk)
- 직접 만들기: `./gradlew :app:packageContestApk` (서명 키 `keystore.properties`가 있어야 release 서명이 돼요)

## 테스트 환경

| 항목 | 값 |
|---|---|
| 에뮬레이터 | Pleos Connect Emulator **v2.1.0** (Android 14, API 34, x86_64) |
| 화면 규격 | **2560 × 1440**, 160 dpi, 가로 |
| 개발 도구 | Android Studio, Kotlin, Jetpack Compose |
| SDK | minSdk 28, targetSdk 37, Pleos Vehicle SDK `ai.pleos.playground:Vehicle:2.3.2` |
| 권한 | `pleos.car.permission.CAR_DRIVE`, `pleos.car.permission.CAR_LIGHTS` |

## 실행 방법

1. Pleos Connect Emulator v2.1.0을 실행해요.
2. APK를 설치하고 실행해요: `adb install -r apk/CarsoLab_CARSONA_release.apk`
3. 첫 화면에서 아바타 이름을 지어요.
4. 차량 신호는 **Pleos Connect Virtual Console**로 바꿔요.
   - `adb forward tcp:8088 tcp:8088` 후 브라우저에서 `http://127.0.0.1:8088/` → Emulation Controls
   - 기어 **D** → 주행 중 화면 ("조용히 기록하고 있어요")
   - 주행거리(Odometer) 올리기, 방향지시등(Turn Signal) 켜기 → 성장치가 쌓여요 (화면에는 숫자를 보여주지 않아요)
   - 기어 **P** → "오늘 성장 +N!" 공개, 상자, 대사. 기준을 넘으면 아기 → 청소년
5. 한 번에 시연하려면 (Git Bash): `bash scripts/demo_trip.sh` — 주행 1회에 +29, 두 번이면 청소년이 돼요.

자세한 신호 정보: [docs/signal-test.md](docs/signal-test.md)

## 동작 방식

```
Pleos Vehicle SDK (기어·방향지시등·주행거리)
   → PleosVehicleSource : 공통 약속 2 이벤트 (tripStart, turnSignal, distance, smoothBrake, park)
   → GrowthEngine       : 공통 약속 1 아바타 상태 계산 (성장·성향·상자·대사 키)
   → CarsonaStore / 화면 : 첫 만남 · 홈 · 주행 중 · 성장 공개 (기어로 자동 전환)
```

- 주행 중에는 소리·애니메이션·숫자 없이 한 줄만 보여줘요. 성장은 정차(P) 후에만 공개해요.
- 서버 없이 차 안(앱 메모리)에만 저장해요.

## 폴더 구조

```
app/src/main/java/com/carsona/app/
 ├─ vehicle/   차량 신호 → 이벤트 (VehicleDataSource, PleosVehicleSource)
 ├─ model/     아바타 상태 (공통 약속 1)
 ├─ growth/    성장 계산 (GrowthEngine)
 └─ ui/        화면, 대사
app/src/main/res/drawable/   avatar_stage0~2.png, bg_default.png, bg_reveal.png, part_*.png
app/src/main/assets/         dialogues.json, scenarios/*.json
scripts/                     시연용 주행 스크립트
docs/                        신호 테스트, 팀 전달 문서
```
