package com.carsona.app.vehicle

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.constant.area.TurnSignalValue
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.DistanceDrivenListener
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import ai.pleos.playground.vehicle.listener.TurnSignalStateListener
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Pleos Vehicle SDK 신호(기어·방향지시등·주행거리)를 [VehicleEvent]로 바꾼다.
 * 필요 권한: pleos.car.permission.CAR_DRIVE, pleos.car.permission.CAR_LIGHTS
 */
class PleosVehicleSource(context: Context) : VehicleDataSource {
    private val vehicle = Vehicle(context.applicationContext)
    private val _events = MutableSharedFlow<VehicleEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<VehicleEvent> = _events.asSharedFlow()

    private var driving = false
    private var lastTurn: TurnSignalValue = TurnSignalValue.OFF
    private var lastOdometerKm: Float? = null

    private val gearListener = object : GearSelectionListener {
        override fun onSelected(gear: VehicleGear?) = onGear(gear)
        override fun onFailed(e: Exception) = logFail("gear", e)
    }
    private val turnListener = object : TurnSignalStateListener {
        override fun onTurnSignalStateUpdated(state: TurnSignalValue?) = onTurn(state)
        override fun onFailed(e: Exception) = logFail("turn", e)
    }
    private val distanceListener = object : DistanceDrivenListener {
        override fun onDistanceDrivenUpdated(value: Float?) = onOdometer(value)
        override fun onFailed(e: Exception) = logFail("distance", e)
    }

    override fun start() {
        vehicle.initialize()
        vehicle.getOdometer().run {
            getCurrentDistanceDriven({ onOdometer(it) }, { logFail("distance(get)", it) })
            registerDistanceDriven(distanceListener)
        }
        vehicle.getGear().run {
            getCurrentGearSelectionState({ onGear(it) }, { logFail("gear(get)", it) })
            registerGearSelection(gearListener)
        }
        vehicle.getTurnSignal().run {
            getCurrentTurnSignalState({ onTurn(it) }, { logFail("turn(get)", it) })
            registerTurnSignalState(turnListener)
        }
    }

    override fun stop() {
        vehicle.getGear().unregisterGearSelection(gearListener)
        vehicle.getTurnSignal().unregisterTurnSignalState(turnListener)
        vehicle.getOdometer().unregisterDistanceDriven(distanceListener)
        vehicle.release()
    }

    @Synchronized
    private fun onGear(gear: VehicleGear?) {
        Log.i(TAG, "gear=$gear")
        when {
            gear == VehicleGear.GEAR_DRIVE && !driving -> {
                driving = true
                emit(VehicleEvent.TripStart())
            }
            gear == VehicleGear.GEAR_PARK && driving -> {
                driving = false
                emit(VehicleEvent.Park)
            }
        }
    }

    @Synchronized
    private fun onTurn(state: TurnSignalValue?) {
        if (state == null || state == lastTurn) return
        Log.i(TAG, "turn=$state")
        val wasOff = lastTurn == TurnSignalValue.OFF
        lastTurn = state
        if (driving && wasOff && state != TurnSignalValue.OFF) {
            emit(VehicleEvent.TurnSignal)
        }
    }

    /** 주행거리는 누적값이 4~5초마다 반복해서 들어오므로, 늘어난 만큼만 이벤트로 만든다. */
    @Synchronized
    private fun onOdometer(km: Float?) {
        if (km == null) return
        val prev = lastOdometerKm
        lastOdometerKm = km
        if (prev == null || km <= prev) return
        Log.i(TAG, "odometer=$km (+${km - prev})")
        if (driving) emit(VehicleEvent.Distance(km - prev))
    }

    private fun emit(event: VehicleEvent) {
        Log.i(TAG, "event=$event")
        _events.tryEmit(event)
    }

    private fun logFail(what: String, e: Exception) {
        Log.w(TAG, "$what 실패: ${e.message}")
    }

    companion object {
        const val TAG = "CARSONA_VEHICLE"
    }
}
