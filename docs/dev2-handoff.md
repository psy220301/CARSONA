# 개발 2에게: 성장 계산 연결 방법

화면(개발 1)은 **공통 약속 1(아바타 상태)** 만 읽어서 그려요. 성장 계산은 아래 인터페이스 하나만 구현하면 바로 연결돼요.

## 파일 위치

| 파일 | 내용 | 담당 |
|---|---|---|
| `app/src/main/java/com/carsona/app/model/AvatarState.kt` | 공통 약속 1 (`stage`, `growth`, `tendency`, `unopenedBox`, `totalKm`, `lastReveal` …) | 공용 |
| `app/src/main/java/com/carsona/app/vehicle/VehicleEvent.kt` | 공통 약속 2 이벤트, `VehicleDataSource` 인터페이스 | 공용 |
| `app/src/main/java/com/carsona/app/growth/GrowthEngine.kt` | 성장 계산 인터페이스 | **개발 2** |
| `app/src/main/java/com/carsona/app/growth/TempGrowthEngine.kt` | 개발 1이 화면 확인용으로 만든 임시 계산 (가이드 5장 규칙) | 교체 대상 |

## 구현할 것

```kotlin
interface GrowthEngine {
    fun onEvent(state: AvatarState, event: VehicleEvent): AvatarState
}
```

- 이벤트: `TripStart(destination)`, `TurnSignal`, `Distance(km)`, `SmoothBrake`, `Park`
- 주행 중 이벤트에서는 화면용 값(`growth` 등)을 바꾸지 말고 이번 주행 값만 쌓아 주세요. 주행 중 화면에는 숫자를 보여주지 않아요.
- `Park`를 받으면 `growth`, `stage`, `tendency`, `unopenedBox`, `totalKm`, `lastReveal`을 갱신해서 돌려주세요. 화면은 그 직후 성장 공개 화면을 띄워요.
- `lastReveal.lineKey`는 `stage_up` / `reveal_safe` / `reveal_normal` 중 하나예요. 화면이 `dialogues.json`에서 문장을 골라요.

## 교체 방법 (한 줄)

`MainActivity.kt`에서 아래 줄의 `TempGrowthEngine()`을 본인 클래스로 바꾸면 끝이에요.

```kotlin
private val store = CarsonaStore(TempGrowthEngine())
```

## 시나리오 재생

`VehicleDataSource`를 구현하면 돼요 (`events: Flow<VehicleEvent>`, `start()`, `stop()`). 실제 차량용 구현은 `vehicle/PleosVehicleSource.kt`를 참고하세요. 다 되면 개발 1이 화면 구석의 "시나리오 재생" 버튼에 연결할게요.

## 에뮬레이터에서 바로 확인

에뮬레이터와 앱을 켠 뒤 Git Bash에서 `bash scripts/demo_trip.sh` → 기어 D, 28km, 방향지시등 5회, 기어 P 순서로 신호가 들어가요. 자세한 건 `docs/signal-test.md`.
