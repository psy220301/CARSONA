package com.carsona.app.growth

import com.carsona.app.model.AvatarState
import com.carsona.app.model.LastReveal
import com.carsona.app.model.Tendency
import com.carsona.app.vehicle.VehicleEvent
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 개발 2 성장 로직. TempGrowthEngine 을 대체한다. (팀 가이드 5장 규칙)
 */
class CarsonaGrowthEngine : GrowthEngine {

    private var inTrip = false
    private var destination = "office"
    private var tripKm = 0f
    private var tripTurns = 0
    private var tripBrakes = 0

    override fun onEvent(state: AvatarState, event: VehicleEvent): AvatarState {
        when (event) {
            is VehicleEvent.TripStart -> {
                inTrip = true
                destination = event.destination
                tripKm = 0f
                tripTurns = 0
                tripBrakes = 0
            }
            is VehicleEvent.Distance -> if (inTrip) tripKm += event.km
            VehicleEvent.TurnSignal -> if (inTrip) tripTurns++
            VehicleEvent.SmoothBrake -> if (inTrip) tripBrakes++
            VehicleEvent.Park -> {
                if (!inTrip) return state
                inTrip = false
                return finishTrip(state)
            }
        }
        return state
    }

    private fun finishTrip(state: AvatarState): AvatarState {
        val raw = tripKm * 0.5f + tripTurns * 3f + tripBrakes * 2f
        val gained = min(raw.roundToInt(), MAX_PER_TRIP)

        var stage = state.stage
        var growth = state.growth + gained
        var stageUp = false
        while (stage < AvatarState.MAX_STAGE && growth >= AvatarState.STAGE_GOAL) {
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

    private fun nextTendency(t: Tendency, destination: String): Tendency {
        val target = when (destination) {
            "office", "school" -> 0
            "camping" -> 1
            "cafe" -> 2
            "home" -> 3
            else -> 0
        }
        val raw = listOf(t.city, t.adventure, t.emotional, t.stable)
            .mapIndexed { i, v -> (1 - ALPHA) * v + if (i == target) ALPHA * 100f else 0f }

        val result = raw.map { it.toInt() }.toMutableList()
        var remain = 100 - result.sum()
        for (i in raw.indices.sortedByDescending { raw[it] - result[it] }) {
            if (remain <= 0) break
            result[i] += 1
            remain -= 1
        }
        return Tendency(result[0], result[1], result[2], result[3])
    }

    companion object {
        const val MAX_PER_TRIP = 40
        const val ALPHA = 0.15f
    }
}