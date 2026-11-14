package com.carsona.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.carsona.app.growth.CarsonaGrowthEngine
import com.carsona.app.growth.ScenarioVehicleSource
import com.carsona.app.ui.CarsonaApp
import com.carsona.app.ui.CarsonaStore
import com.carsona.app.ui.Dialogues
import com.carsona.app.ui.theme.CARSONATheme
import com.carsona.app.vehicle.PleosVehicleSource
import com.carsona.app.vehicle.VehicleDataSource
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // 성장 계산: 개발 2의 CarsonaGrowthEngine
    private val store = CarsonaStore(CarsonaGrowthEngine())

    // 실제 차량 신호와 시나리오 재생은 같은 이벤트를 내보내므로 화면은 출처를 신경 쓰지 않는다.
    private lateinit var vehicle: VehicleDataSource
    private lateinit var scenario: VehicleDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialogues = Dialogues.load(this)

        vehicle = PleosVehicleSource(this)
        scenario = ScenarioVehicleSource(this)
        lifecycleScope.launch { vehicle.events.collect { store.onEvent(it) } }
        lifecycleScope.launch { scenario.events.collect { store.onEvent(it) } }
        vehicle.start()

        // 기획이 assets/scenarios/commute.json 을 올렸을 때만 테스트용 버튼을 보여준다.
        val hasScenario = assets.list("scenarios")?.contains("commute.json") == true

        setContent {
            CARSONATheme {
                val ui by store.state.collectAsState()
                CarsonaApp(
                    ui, dialogues,
                    onName = store::setName,
                    onCloseReveal = store::closeReveal,
                    onPlayScenario = if (hasScenario) ({ scenario.start() }) else null,
                )
            }
        }
    }

    override fun onDestroy() {
        scenario.stop()
        vehicle.stop()
        super.onDestroy()
    }
}
