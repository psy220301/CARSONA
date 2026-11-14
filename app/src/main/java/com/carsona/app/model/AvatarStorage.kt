package com.carsona.app.model

import android.content.Context
import android.util.Log
import org.json.JSONObject

/**
 * 토토 저장: 앱을 껐다 켜도 이름·단계·성장·성향·꾸미기·상자가 남도록 기기에 저장한다.
 * (SharedPreferences 에 JSON 한 줄. 차 밖으로 보내지 않는다)
 */
class AvatarStorage(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("carsona", Context.MODE_PRIVATE)

    /** 저장된 토토. 없거나 이름을 아직 안 지었으면 null */
    fun load(): AvatarState? = runCatching {
        val raw = prefs.getString(KEY, null) ?: return null
        val o = JSONObject(raw)
        val t = o.getJSONObject("tendency")
        val r = o.optJSONObject("lastReveal") ?: JSONObject()
        AvatarState(
            name = o.optString("name"),
            stage = o.optInt("stage").coerceIn(0, AvatarState.MAX_STAGE),
            growth = o.optInt("growth").coerceIn(0, AvatarState.STAGE_GOAL),
            tendency = Tendency(t.optInt("city", 25), t.optInt("adventure", 25), t.optInt("emotional", 25), t.optInt("stable", 25)),
            background = o.optString("background", "default"),
            equipped = Equipped(head = o.optJSONObject("equipped")?.optString("head")?.takeIf { it.isNotEmpty() }),
            unopenedBox = o.optInt("unopenedBox"),
            totalKm = o.optDouble("totalKm", 0.0).toFloat(),
            lastReveal = LastReveal(r.optInt("gained"), r.optBoolean("stageUp"), r.optString("lineKey")),
        ).takeIf { it.name.isNotBlank() }
    }.onFailure { Log.w(TAG, "저장된 토토 읽기 실패: ${it.message}") }.getOrNull()

    fun save(a: AvatarState) {
        val o = JSONObject()
            .put("name", a.name)
            .put("stage", a.stage)
            .put("growth", a.growth)
            .put("tendency", JSONObject()
                .put("city", a.tendency.city).put("adventure", a.tendency.adventure)
                .put("emotional", a.tendency.emotional).put("stable", a.tendency.stable))
            .put("background", a.background)
            .put("equipped", JSONObject().put("head", a.equipped.head ?: ""))
            .put("unopenedBox", a.unopenedBox)
            .put("totalKm", a.totalKm.toDouble())
            .put("lastReveal", JSONObject()
                .put("gained", a.lastReveal.gained).put("stageUp", a.lastReveal.stageUp)
                .put("lineKey", a.lastReveal.lineKey))
        prefs.edit().putString(KEY, o.toString()).apply()
    }

    companion object {
        private const val KEY = "avatar"
        private const val TAG = "CARSONA_SAVE"
    }
}
