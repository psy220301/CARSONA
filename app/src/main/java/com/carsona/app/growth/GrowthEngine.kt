package com.carsona.app.growth

import com.carsona.app.model.AvatarState
import com.carsona.app.vehicle.VehicleEvent

/**
 * 개발 2가 구현하는 성장 계산. 이벤트를 하나 받을 때마다 새 아바타 상태를 돌려준다.
 * - 주행 중 이벤트는 이번 주행 성장치만 쌓고, 화면용 값(growth 등)은 바꾸지 않는다.
 * - Park를 받으면 growth·stage·tendency·unopenedBox·totalKm·lastReveal을 갱신한다.
 *
 * 개발 2는 TempGrowthEngine 대신 자기 클래스를 만들고, MainActivity의 한 줄만 바꾸면 된다.
 */
interface GrowthEngine {
    fun onEvent(state: AvatarState, event: VehicleEvent): AvatarState
}
