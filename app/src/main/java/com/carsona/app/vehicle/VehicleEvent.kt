package com.carsona.app.vehicle

import kotlinx.coroutines.flow.Flow

/**
 * 공통 약속 2: 차량 이벤트. 실제 차량 신호와 시나리오 파일 모두 이 이벤트로 바뀌어
 * 개발 2의 성장 계산(GrowthEngine)에 들어간다.
 */
sealed interface VehicleEvent {
    /** 주행 시작: 기어 P → D. 실제 신호에서는 목적지를 일단 "office"로 둔다. */
    data class TripStart(val destination: String = "office") : VehicleEvent

    /** 주행 중 방향지시등이 꺼짐 → 켜짐으로 바뀔 때 1회 */
    data object TurnSignal : VehicleEvent

    /** 달린 거리 (km) */
    data class Distance(val km: Float) : VehicleEvent

    /** 부드러운 감속 (1차에서는 시나리오에서만 사용) */
    data object SmoothBrake : VehicleEvent

    /** 주행 끝: 기어가 P로 바뀔 때 → 성장 공개 */
    data object Park : VehicleEvent
}

/** 차량 이벤트 공급원. 실제 차량(PleosVehicleSource)과 시나리오 재생(개발 2)이 같은 모양을 쓴다. */
interface VehicleDataSource {
    val events: Flow<VehicleEvent>
    fun start()
    fun stop()
}
