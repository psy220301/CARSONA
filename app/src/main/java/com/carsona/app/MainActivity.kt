package com.carsona.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.constant.area.TurnSignalValue
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.DistanceDrivenListener
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import ai.pleos.playground.vehicle.listener.TurnSignalStateListener
import com.carsona.app.ui.theme.CARSONATheme

// 임시 신호 테스트 화면: 기어·방향지시등·주행거리를 Pleos Vehicle SDK로 읽어 표시한다.
class MainActivity : ComponentActivity() {
    private val gear = mutableStateOf("?")
    private val turn = mutableStateOf("?")
    private val distance = mutableStateOf("?")
    private val log = mutableStateListOf<String>()

    private lateinit var vehicle: Vehicle

    private val gearListener = object : GearSelectionListener {
        override fun onSelected(value: VehicleGear?) = record("gear", "${value?.name}") { gear.value = it }
        override fun onFailed(e: Exception) = record("gear", "실패 ${e.message}") { gear.value = it }
    }
    private val turnListener = object : TurnSignalStateListener {
        override fun onTurnSignalStateUpdated(value: TurnSignalValue?) = record("turn", "${value?.name}") { turn.value = it }
        override fun onFailed(e: Exception) = record("turn", "실패 ${e.message}") { turn.value = it }
    }
    private val distanceListener = object : DistanceDrivenListener {
        override fun onDistanceDrivenUpdated(value: Float?) = record("distance", "$value") { distance.value = it }
        override fun onFailed(e: Exception) = record("distance", "실패 ${e.message}") { distance.value = it }
    }

    private fun record(kind: String, value: String, apply: (String) -> Unit) {
        Log.i(TAG, "$kind=$value")
        runOnUiThread {
            apply(value)
            log.add(0, "$kind = $value")
            if (log.size > 15) log.removeAt(log.lastIndex)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CARSONATheme {
                Column(Modifier.fillMaxSize().padding(48.dp)) {
                    Text("CARSONA 신호 테스트", fontSize = 40.sp)
                    Text("기어: ${gear.value}", fontSize = 32.sp)
                    Text("방향지시등: ${turn.value}", fontSize = 32.sp)
                    Text("주행거리: ${distance.value}", fontSize = 32.sp)
                    log.forEach { Text(it, fontSize = 20.sp) }
                }
            }
        }

        vehicle = Vehicle(this)
        vehicle.initialize()
        vehicle.getGear().run {
            getCurrentGearSelectionState({ record("gear(get)", "${it?.name}") { v -> gear.value = v } },
                { record("gear(get)", "실패 ${it.message}") { v -> gear.value = v } })
            registerGearSelection(gearListener)
        }
        vehicle.getTurnSignal().run {
            getCurrentTurnSignalState({ record("turn(get)", "${it?.name}") { v -> turn.value = v } },
                { record("turn(get)", "실패 ${it.message}") { v -> turn.value = v } })
            registerTurnSignalState(turnListener)
        }
        vehicle.getOdometer().run {
            getCurrentDistanceDriven({ record("distance(get)", "$it") { v -> distance.value = v } },
                { record("distance(get)", "실패 ${it.message}") { v -> distance.value = v } })
            registerDistanceDriven(distanceListener)
        }
    }

    override fun onDestroy() {
        vehicle.getGear().unregisterGearSelection(gearListener)
        vehicle.getTurnSignal().unregisterTurnSignalState(turnListener)
        vehicle.getOdometer().unregisterDistanceDriven(distanceListener)
        vehicle.release()
        super.onDestroy()
    }

    companion object { const val TAG = "CARSONA_SIG" }
}
