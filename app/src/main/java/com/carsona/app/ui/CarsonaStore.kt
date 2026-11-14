package com.carsona.app.ui

import com.carsona.app.growth.GrowthEngine
import com.carsona.app.model.AvatarState
import com.carsona.app.vehicle.VehicleEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 지금 보여줄 화면. 버튼이 아니라 기어 신호(이벤트)로 바뀐다. */
enum class Screen { FIRST_MEET, HOME, DRIVING, REVEAL }

data class UiState(
    val screen: Screen = Screen.FIRST_MEET,
    val avatar: AvatarState = AvatarState(),
    /** 성장 공개 화면의 막대 애니메이션 시작값 (공개 직전 growth) */
    val growthBefore: Int = 0,
)

/** 개발 1: 화면 상태. 성장 계산은 [GrowthEngine](개발 2)에 맡기고 결과만 읽는다. */
class CarsonaStore(private val engine: GrowthEngine) {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * 성장 계산(engine)은 주행 중 값을 내부에 쌓으므로 이벤트 하나에 정확히 한 번만 불러야 한다.
     * (StateFlow.update 는 경합 시 블록을 다시 실행할 수 있어서 쓰지 않는다)
     */
    @Synchronized
    fun onEvent(event: VehicleEvent) {
        val ui = _state.value
        val before = ui.avatar
        val after = engine.onEvent(before, event)
        _state.value = when (event) {
            is VehicleEvent.TripStart -> ui.copy(avatar = after, screen = Screen.DRIVING)
            VehicleEvent.Park -> ui.copy(
                avatar = after,
                screen = if (ui.screen == Screen.DRIVING) Screen.REVEAL else ui.screen,
                growthBefore = if (after.lastReveal.stageUp) 0 else before.growth,
            )
            else -> ui.copy(avatar = after)
        }
    }

    /** 첫 만남: 이름을 지으면 홈으로 */
    @Synchronized
    fun setName(name: String) = _state.update {
        it.copy(avatar = it.avatar.copy(name = name.trim().ifEmpty { "토토" }), screen = Screen.HOME)
    }

    /** 공개 화면 닫기. 이름을 짓기 전에 주행했다면 첫 만남(이름 짓기)으로 돌아간다. */
    @Synchronized
    fun closeReveal() = _state.update {
        it.copy(screen = if (it.avatar.name.isBlank()) Screen.FIRST_MEET else Screen.HOME)
    }
}
