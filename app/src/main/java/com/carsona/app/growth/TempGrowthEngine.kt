package com.carsona.app.growth

import com.carsona.app.model.AvatarState
import com.carsona.app.model.LastReveal
import com.carsona.app.model.Tendency
import com.carsona.app.vehicle.VehicleEvent
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * [임시] 화면 연결 확인용 성장 계산 (개발 1 작성). 개발 2의 GrowthEngine으로 교체 예정.
 * 규칙은 팀 가이드 5장(시연용)을 그대로 따른다.
 */
class TempGrowthEngine : GrowthEngine {
    private var tripRaw = 0f
    private var tripKm = 0f
    private var tripTurns = 0
    private var destination = "office"

    override fun onEvent(state: AvatarState, event: VehicleEvent): AvatarState {
        when (event) {
            is VehicleEvent.TripStart -> {
                tripRaw = 0f; tripKm = 0f; tripTurns = 0
                destination = event.destination
            }
            is VehicleEvent.Distance -> { tripKm += event.km; tripRaw += event.km * 0.5f }
            VehicleEvent.TurnSignal -> { tripTurns++; tripRaw += 3f }
            VehicleEvent.SmoothBrake -> tripRaw += 2f
            VehicleEvent.Park -> return finishTrip(state)
        }
        return state
    }

    private fun finishTrip(state: AvatarState): AvatarState {
        val gained = min(tripRaw.roundToInt(), 40)
        var stage = state.stage
        var growth = state.growth + gained
        var stageUp = false
        if (stage < AvatarState.MAX_STAGE && growth >= AvatarState.STAGE_GOAL) {
            stage += 1
            growth -= AvatarState.STAGE_GOAL
            stageUp = true
        }
        if (stage == AvatarState.MAX_STAGE) growth = min(growth, AvatarState.STAGE_GOAL)
        val lineKey = when {
            stageUp -> "stage_up"
            tripTurns >= 1 -> "reveal_safe"
            else -> "reveal_normal"
        }
        return state.copy(
            stage = stage,
            growth = growth,
            tendency = nextTendency(state.tendency, destination),
            unopenedBox = state.unopenedBox + 1,
            totalKm = state.totalKm + tripKm,
            lastReveal = LastReveal(gained, stageUp, lineKey),
        )
    }

    /** 새 비율 = 0.85 × 기존 + 0.15 × 이번 주행 점수(목적지 성향 100, 나머지 0). 합 100 유지. */
    private fun nextTendency(t: Tendency, destination: String): Tendency {
        val target = when (destination) {
            "office", "school" -> 0
            "camping" -> 1
            "cafe" -> 2
            else -> 3
        }
        val raw = listOf(t.city, t.adventure, t.emotional, t.stable)
            .mapIndexed { i, v -> 0.85f * v + if (i == target) 15f else 0f }
        val rounded = raw.map { it.roundToInt() }.toMutableList()
        rounded[target] += 100 - rounded.sum()
        return Tendency(rounded[0], rounded[1], rounded[2], rounded[3])
    }
}
