package com.carsona.app.voice

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.content.Context
import android.media.AudioAttributes
import android.util.Log
import java.util.Locale

/**
 * 토토 목소리 (Pleos TextToSpeech SDK, 선택 기능).
 * - ON_DEVICE 모드: 차량 안 음성 엔진만 써서 인터넷·앱 등록(Client ID/Secret)이 필요 없다.
 * - 정차 후 성장 공개 화면에서만 대사를 읽는다. 주행 화면이 되면 [stop]으로 바로 끈다.
 * - 초기화에 실패하면 조용히 아무것도 하지 않는다 (화면 대사는 그대로 보인다).
 */
class TotoVoice(context: Context) {
    private val tts: TextToSpeech? = runCatching {
        TextToSpeech(context.applicationContext, Mode.ON_DEVICE)
    }.onFailure { Log.w(TAG, "TTS 생성 실패: ${it.message}") }.getOrNull()
    private var ready = false

    private val events = object : EventListener {
        override fun onStart() = Unit
        override fun onReady() = Unit
        override fun onDone() = Unit
        override fun onStop() = Unit
        override fun onUpdatedRms(rms: Double) = Unit
        override fun onError(type: ErrorType) { Log.w(TAG, "TTS 오류: $type") }
    }

    init {
        runCatching { tts?.initialize(object : OnInitializeListener {
            override fun onSuccess() {
                val tts = tts ?: return
                tts.addEventListener(events)
                tts.setLocale(Locale.KOREA)
                tts.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                ready = true
                Log.i(TAG, "TTS 준비 완료 (ON_DEVICE)")
            }

            override fun onFailure(type: ErrorType) { Log.w(TAG, "TTS 초기화 실패: $type") }
        }) }.onFailure { Log.w(TAG, "TTS 초기화 실패: ${it.message}") }
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        runCatching { tts?.speak(text, true) }.onFailure { Log.w(TAG, "speak 실패: ${it.message}") }
        Log.i(TAG, "speak: $text")
    }

    fun stop() {
        if (ready) runCatching { tts?.stop() }
    }

    fun shutdown() {
        stop()
        runCatching {
            tts?.removeEventListener(events)
            tts?.release()
        }
    }

    companion object { private const val TAG = "CARSONA_VOICE" }
}
