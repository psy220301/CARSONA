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
import com.carsona.app.ui.Screen
import com.carsona.app.ui.theme.CARSONATheme
import com.carsona.app.vehicle.PleosVehicleSource
import com.carsona.app.vehicle.VehicleDataSource
import com.carsona.app.voice.TotoVoice
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // 성장 계산: 개발 2의 CarsonaGrowthEngine
    private val store = CarsonaStore(CarsonaGrowthEngine())

    // 실제 차량 신호와 시나리오 재생은 같은 이벤트를 내보내므로 화면은 출처를 신경 쓰지 않는다.
    private lateinit var vehicle: VehicleDataSource
    private lateinit var scenario: VehicleDataSource
    private lateinit var voice: TotoVoice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialogues = Dialogues.load(this)

        vehicle = PleosVehicleSource(this)
        // 주행 사이 간격: 성장 공개 화면과 토토 목소리가 끝까지 보이고 들리도록 4.5초
        scenario = ScenarioVehicleSource(this, tripGapMs = 4500)
        lifecycleScope.launch { vehicle.events.collect { store.onEvent(it) } }
        lifecycleScope.launch { scenario.events.collect { store.onEvent(it) } }
        vehicle.start()

        // 토토 목소리: 정차 후 공개 화면에서만 읽고, 주행 화면이 되면 즉시 끈다.
        voice = TotoVoice(this)
        lifecycleScope.launch {
            store.state.collect { if (it.screen == Screen.DRIVING) voice.stop() }
        }

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
                    onSpeak = voice::speak,
                    onEquip = store::equipHead,
                )
            }
        }
    }

    override fun onDestroy() {
        voice.shutdown()
        scenario.stop()
        vehicle.stop()
        super.onDestroy()
    }
}
