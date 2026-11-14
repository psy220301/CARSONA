# 성장 계산·시나리오 구조 (개발 1 ↔ 개발 2)

화면(개발 1)은 **공통 약속 1(아바타 상태)** 만 읽어서 그려요. 성장 계산과 시나리오 재생(개발 2)은 아래 인터페이스로 연결돼 있어요. (2026-11-14 연결 완료)

## 파일 위치

| 파일 | 내용 | 담당 |
|---|---|---|
| `app/src/main/java/com/carsona/app/model/AvatarState.kt` | 공통 약속 1 (`stage`, `growth`, `tendency`, `unopenedBox`, `totalKm`, `lastReveal` …) | 공용 |
| `app/src/main/java/com/carsona/app/vehicle/VehicleEvent.kt` | 공통 약속 2 이벤트, `VehicleDataSource` 인터페이스 | 공용 |
| `app/src/main/java/com/carsona/app/vehicle/PleosVehicleSource.kt` | 실제 차량 신호(Pleos Vehicle SDK) → 이벤트 | 개발 1 |
| `app/src/main/java/com/carsona/app/growth/GrowthEngine.kt` | 성장 계산 인터페이스 | 공용 |
| `app/src/main/java/com/carsona/app/growth/CarsonaGrowthEngine.kt` | 성장·성향·상자·대사 키 계산 | 개발 2 |
| `app/src/main/java/com/carsona/app/growth/ScenarioVehicleSource.kt` | `assets/scenarios/commute.json` 재생 → 이벤트 | 개발 2 |
| `app/src/main/java/com/carsona/app/MainActivity.kt` | 위 부품 연결, "시나리오 재생" 버튼 | 개발 1 |

## 인터페이스

```kotlin
interface GrowthEngine {
    fun onEvent(state: AvatarState, event: VehicleEvent): AvatarState
}
```

- 이벤트: `TripStart(destination)`, `TurnSignal`, `Distance(km)`, `SmoothBrake`, `Park`
- 주행 중 이벤트에서는 화면용 값(`growth` 등)을 바꾸지 않고 이번 주행 값만 쌓아요. 주행 중 화면에는 숫자를 보여주지 않아요.
- `Park`를 받으면 `growth`, `stage`, `tendency`, `unopenedBox`, `totalKm`, `lastReveal`을 갱신해서 돌려줘요. 화면은 그 직후 성장 공개 화면을 띄워요.
- `lastReveal.lineKey`는 `stage_up` / `reveal_safe` / `reveal_normal` 중 하나예요. 화면이 `assets/dialogues.json`에서 문장을 골라요. 다만 다 자란 뒤(성인)에는 `reveal_adult`, 이번 성장이 0이면 `reveal_zero`를 화면이 대신 골라요.

## 시나리오 재생

`VehicleDataSource`(`events: Flow<VehicleEvent>`, `start()`, `stop()`)를 구현해요. 실제 차량 신호와 같은 이벤트를 내보내므로 화면은 출처를 신경 쓰지 않아요. 홈 화면 오른쪽 위 "시나리오 재생" 버튼은 `assets/scenarios/commute.json`이 있을 때만 보여요.

## 에뮬레이터에서 확인

- 실제 신호: 에뮬레이터와 앱을 켠 뒤 Git Bash에서 `bash scripts/demo_trip.sh` → 기어 D, 28km, 방향지시등 5회, 기어 P (+29 성장). 자세한 건 `docs/signal-test.md`.
- 시나리오: 홈 화면 "시나리오 재생" → 출퇴근 4회 (회당 +14, 4회째 청소년).
