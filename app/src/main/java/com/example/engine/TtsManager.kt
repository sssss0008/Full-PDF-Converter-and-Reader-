package com.example.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(1.0f)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlaying.value = true
                }
                override fun onDone(utteranceId: String?) {
                    _isPlaying.value = false
                }
                override fun onError(utteranceId: String?) {
                    _isPlaying.value = false
                }
            })
        }
    }

    fun speak(text: String) {
        if (!isInitialized || text.isBlank()) return
        tts?.setSpeechRate(_speechRate.value)
        tts?.setPitch(_speechPitch.value)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PDFOmni_TTS_${System.currentTimeMillis()}")
        _isPlaying.value = true
    }

    fun stop() {
        tts?.stop()
        _isPlaying.value = false
    }

    fun setRate(rate: Float) {
        _speechRate.value = rate.coerceIn(0.5f, 2.5f)
        tts?.setSpeechRate(_speechRate.value)
    }

    fun setPitch(pitch: Float) {
        _speechPitch.value = pitch.coerceIn(0.5f, 2.0f)
        tts?.setPitch(_speechPitch.value)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
