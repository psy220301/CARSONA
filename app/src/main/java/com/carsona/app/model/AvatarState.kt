package com.carsona.app.model

/**
 * 공통 약속 1: 아바타 상태. 개발 2가 계산하고, 개발 1(화면)은 이 값만 읽어서 그린다.
 * 필드 이름은 팀 가이드의 JSON과 똑같이 맞춘다.
 */
data class AvatarState(
    val name: String = "",
    /** 0 아기, 1 청소년, 2 성인 */
    val stage: Int = 0,
    /** 현재 단계 안에서의 성장치 (시연 기준 50이면 다음 단계) */
    val growth: Int = 0,
    val tendency: Tendency = Tendency(),
    val background: String = "default",
    val equipped: Equipped = Equipped(),
    val unopenedBox: Int = 0,
    val totalKm: Float = 0f,
    val lastReveal: LastReveal = LastReveal(),
) {
    companion object {
        const val MAX_STAGE = 2
        /** 시연용 단계 기준 (팀 가이드 5장) */
        const val STAGE_GOAL = 50
    }
}

/** 성향 4개, 합은 항상 100 */
data class Tendency(
    val city: Int = 25,
    val adventure: Int = 25,
    val emotional: Int = 25,
    val stable: Int = 25,
)

data class Equipped(val head: String? = null)

/** 마지막 주행 결과. 성장 공개 화면이 이걸 그대로 보여준다. */
data class LastReveal(
    val gained: Int = 0,
    val stageUp: Boolean = false,
    /** dialogues.json의 키: stage_up / reveal_safe / reveal_normal */
    val lineKey: String = "",
)

val Int.stageLabel: String
    get() = when (this) { 0 -> "아기"; 1 -> "청소년"; else -> "성인" }
