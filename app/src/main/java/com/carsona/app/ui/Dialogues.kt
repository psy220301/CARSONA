package com.carsona.app.ui

import android.content.Context
import org.json.JSONObject

/**
 * 공통 약속 4: 대사. 기획이 app/src/main/assets/dialogues.json 을 올리면 그 파일을 쓰고,
 * 없으면 팀 가이드의 예시 문장을 쓴다. 같은 키에 문장이 여러 개면 하나를 고른다.
 */
class Dialogues(private val lines: Map<String, List<String>>) {
    fun pick(key: String): String = lines[key]?.randomOrNull().orEmpty()

    companion object {
        private val DEFAULT = mapOf(
            "birth" to listOf("안녕! 나 방금 태어났어. 이름 지어 줄래?"),
            "driving" to listOf("멈추면 오늘 얼마나 컸는지 보여 줄게!"),
            "reveal_safe" to listOf("깜빡이 잘 켜 줘서 오늘 빨리 자랐어. 고마워!"),
            "reveal_normal" to listOf("오늘도 같이 달려 줘서 고마워!"),
            "stage_up" to listOf("나 좀 컸지? 같이 달린 길 덕분이야."),
            // 다 자란 뒤(성인)에는 "자랐어" 대신 함께한 것을 말한다
            "reveal_adult" to listOf("오늘도 함께해서 든든했어!"),
            // 거리 없이 바로 세웠을 때(+0)
            "reveal_zero" to listOf("잠깐이었지만 같이 나와서 좋았어!"),
        )

        fun load(context: Context): Dialogues {
            val json = runCatching {
                context.assets.open("dialogues.json").bufferedReader().use { it.readText() }
            }.getOrNull() ?: return Dialogues(DEFAULT)
            val obj = JSONObject(json)
            val loaded = obj.keys().asSequence().associateWith { key ->
                val arr = obj.optJSONArray(key)
                if (arr == null) listOf(obj.optString(key)) else List(arr.length()) { arr.optString(it) }
            }
            return Dialogues(DEFAULT + loaded)
        }
    }
}
