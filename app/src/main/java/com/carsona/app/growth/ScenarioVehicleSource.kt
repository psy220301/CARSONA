package com.carsona.app.growth

import android.content.Context
import android.util.Log
import com.carsona.app.vehicle.VehicleDataSource
import com.carsona.app.vehicle.VehicleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * 개발 2: 시나리오 재생. assets/scenarios/commute.json 을 차량 이벤트로 바꿔 흘려보낸다.
 */
class ScenarioVehicleSource(
    private val context: Context,
    private val fileName: String = "scenarios/commute.json",
    private val eventDelayMs: Long = 400,
    private val tripGapMs: Long = 3000,
) : VehicleDataSource {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    private val _events = MutableSharedFlow<VehicleEvent>(extraBufferCapacity = 64)
    override val events: Flow<VehicleEvent> = _events.asSharedFlow()

    override fun start() {
        stop()
        val list = try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            parse(json)
        } catch (e: Exception) {
            Log.e(TAG, "시나리오 읽기 실패: $fileName", e)
            return
        }

        job = scope.launch {
            delay(300)
            for (ev in list) {
                _events.emit(ev)
                delay(if (ev == VehicleEvent.Park) tripGapMs else eventDelayMs)
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }

    companion object {
        private const val TAG = "Scenario"

        fun parse(json: String): List<VehicleEvent> {
            val out = mutableListOf<VehicleEvent>()
            val trips = JSONObject(json).getJSONArray("trips")

            for (i in 0 until trips.length()) {
                val trip = trips.getJSONObject(i)
                out += VehicleEvent.TripStart(trip.optString("destination", "office"))

                val events = trip.getJSONArray("events")
                var parked = false
                for (j in 0 until events.length()) {
                    val e = events.getJSONObject(j)
                    when (e.optString("type")) {
                        "road" -> out += VehicleEvent.Distance(e.optDouble("km", 0.0).toFloat())
                        "turnSignal" -> out += VehicleEvent.TurnSignal
                        "brake" -> {
                            if (e.optBoolean("smooth", false) && e.optDouble("speedKmh", 0.0) >= 40.0) {
                                out += VehicleEvent.SmoothBrake
                            }
                        }
                        "park" -> { out += VehicleEvent.Park; parked = true }
                        else -> Unit
                    }
                }
                if (!parked) out += VehicleEvent.Park
            }
            return out
        }
    }
}