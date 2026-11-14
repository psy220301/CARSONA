#!/usr/bin/env bash
# 시연용 주행 1회: 기어 D → 28km 주행 + 방향지시등 5회 → 기어 P
# 결과: +14(거리) +15(방향지시등) = 오늘 +29 성장. 두 번 돌리면 58점으로 청소년이 된다.
#
# 사용법 (Git Bash): 에뮬레이터와 CARSONA 앱을 켠 뒤
#   bash scripts/demo_trip.sh          # 기본 속도
#   STEP=0.5 bash scripts/demo_trip.sh # 빠르게
set -e
ADB="${ADB:-$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe}"
API="http://127.0.0.1:8088/vehicle"
STEP="${STEP:-1.5}"

put() {
  local res; res=$(curl -s -X PUT "$API/$1")
  case "$res" in *'"error"'*) echo "  실패: $1 → $res" >&2 ;; esac
}

"$ADB" forward tcp:8088 tcp:8088 >/dev/null

# Virtual Console은 브라우저 세션이 1초마다 살아 있다고 알려야 신호를 받는다.
# 브라우저를 열지 않아도 되도록 스크립트가 대신 보낸다.
HB="http://127.0.0.1:8088/browser/session?client=carsona-demo"
( while true; do curl -s -o /dev/null -X POST "$HB"; sleep 1; done ) &
HB_PID=$!
trap 'kill $HB_PID 2>/dev/null; curl -s -o /dev/null -X POST "$HB&closing=1"' EXIT
sleep 2

# 시작 주행거리: 지금 계기 값에서 이어서 올린다.
BASE=$(curl -s "$API/state" | grep -oE '"traveled_distance_km":[0-9.]+' | head -1 | cut -d: -f2)
BASE=${BASE%.*}; BASE=${BASE:-0}

echo "기어 P (준비)"; put "gear?gear=Park"; sleep "$STEP"
echo "기어 D (주행 시작)"; put "gear?gear=Drive"; sleep "$STEP"

km=0
for i in 1 2 3 4 5; do
  km=$((km + 5)); put "odometer?traveled_distance_km=$((BASE + km))"; sleep "$STEP"
  echo "방향지시등 $i"
  if (( i % 2 )); then put "turn-signal?mode=BlinkLeft"; else put "turn-signal?mode=BlinkRight"; fi
  sleep "$STEP"; put "turn-signal?mode=Off"; sleep "$STEP"
done
put "odometer?traveled_distance_km=$((BASE + 28))"
# 주행거리는 4~5초마다 앱에 전달되므로, 정차 전에 마지막 값이 도착할 때까지 기다린다.
sleep 6
echo "28km 주행 완료"

echo "기어 P (정차 공개)"; put "gear?gear=Park"
