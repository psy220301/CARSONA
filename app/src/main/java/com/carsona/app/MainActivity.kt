package com.carsona.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.carsona.app.growth.CarsonaGrowthEngine
import com.carsona.app.growth.TempGrowthEngine
import com.carsona.app.ui.CarsonaApp
import com.carsona.app.ui.CarsonaStore
import com.carsona.app.ui.Dialogues
import com.carsona.app.ui.theme.CARSONATheme
import com.carsona.app.vehicle.PleosVehicleSource
import com.carsona.app.vehicle.VehicleDataSource
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // 개발 2: 성장 계산을 완성하면 CarsonaGrowthEngine() 을 본인 클래스로 바꾸세요.
    private val store = CarsonaStore(CarsonaGrowthEngine())
    private lateinit var source: VehicleDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialogues = Dialogues.load(this)

        source = PleosVehicleSource(this)
        lifecycleScope.launch { source.events.collect { store.onEvent(it) } }
        source.start()

        setContent {
            CARSONATheme {
                val ui by store.state.collectAsState()
                CarsonaApp(ui, dialogues, onName = store::setName, onCloseReveal = store::closeReveal)
            }
        }
    }

    override fun onDestroy() {
        source.stop()
        super.onDestroy()
    }
}
