# 에뮬레이터 차량 신호 테스트 결과 (2026-11-14)

환경: Pleos Connect Emulator v2.1.0 (API 34), Pleos Vehicle SDK `ai.pleos.playground:Vehicle:2.3.2`

## 결론

기어, 방향지시등, 주행거리 모두 우리 앱에서 실시간으로 받을 수 있어요.

| 신호 | SDK 호출 | 권한 | 받은 값 |
|---|---|---|---|
| 기어 | `vehicle.getGear().registerGearSelection` | `pleos.car.permission.CAR_DRIVE` | `GEAR_PARK` / `GEAR_DRIVE` ✅ |
| 방향지시등 | `vehicle.getTurnSignal().registerTurnSignalState` | `pleos.car.permission.CAR_LIGHTS` | `OFF` / `LEFT` / `RIGHT` ✅ |
| 주행거리(km) | `vehicle.getOdometer().registerDistanceDriven` | `pleos.car.permission.CAR_DRIVE` | `12.0` ✅ |

- 표준 Android `CarPropertyManager`로는 방향지시등과 주행거리를 읽을 수 없어요. 시스템 앱 전용 권한이 필요해요. 그래서 **반드시 Pleos Vehicle SDK를 써요.**
- 주행거리는 차가 서 있어도 약 4~5초마다 같은 값이 다시 들어와요. 값이 바뀔 때만 처리해야 해요.

## 신호 바꾸는 법 (Pleos Connect Virtual Console)

1. 에뮬레이터를 켠 뒤 터미널에서 실행해요: `adb forward tcp:8088 tcp:8088`
2. 브라우저에서 `http://127.0.0.1:8088/`을 열고 **Emulation Controls**에서 버튼을 눌러요.
   - Motion → Gear Selection (P/R/N/D)
   - Convenience → Turn Signal State (Off / Blink left / Blink right)
   - Motion → Odometer (km)

### 명령어로 바꾸기 (시연·시나리오 재생용)

```
curl -X PUT "http://127.0.0.1:8088/vehicle/gear?gear=Drive"
curl -X PUT "http://127.0.0.1:8088/vehicle/turn-signal?mode=BlinkLeft"
curl -X PUT "http://127.0.0.1:8088/vehicle/turn-signal?mode=Off"
curl -X PUT "http://127.0.0.1:8088/vehicle/odometer?traveled_distance_km=12"
curl -X PUT "http://127.0.0.1:8088/vehicle/gear?gear=Park"
```

- 기어 값: `Park`, `Reverse`, `Neutral`, `Drive`
- 방향지시등 값: `Off`, `BlinkLeft`, `BlinkRight`, `FlashLeft`, `FlashRight`

## 공통 약속 2(차량 이벤트)와 연결

| 이벤트 | 만드는 조건 |
|---|---|
| `tripStart` | 기어가 P가 아닌 값에서 `GEAR_DRIVE`로 바뀔 때 |
| `turnSignal` | 방향지시등이 `OFF`에서 `LEFT` 또는 `RIGHT`로 바뀔 때 |
| `distance` | 주행거리 값이 늘어날 때 (늘어난 만큼 km) |
| `smoothBrake` | (미확인) Brake Pedal 값으로 판단할 예정 |
| `park` | 기어가 `GEAR_PARK`로 바뀔 때 |
