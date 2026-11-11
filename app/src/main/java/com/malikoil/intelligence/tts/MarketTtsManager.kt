package com.malikoil.intelligence.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class TtsState {
    IDLE,
    INITIALIZING,
    READY,
    SPEAKING,
    COMPLETED,
    ERROR
}

class MarketTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val TAG = "MarketTtsManager"
    private var tts: TextToSpeech? = null

    private val _ttsState = MutableStateFlow(TtsState.IDLE)
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    private val _currentUtterance = MutableStateFlow("")
    val currentUtterance: StateFlow<String> = _currentUtterance.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _isUrduSupported = MutableStateFlow(false)
    val isUrduSupported: StateFlow<Boolean> = _isUrduSupported.asStateFlow()

    private var isInitialized = false

    init {
        _ttsState.value = TtsState.INITIALIZING
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TextToSpeech", e)
            _ttsState.value = TtsState.ERROR
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            _ttsState.value = TtsState.READY

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _ttsState.value = TtsState.SPEAKING
                }

                override fun onDone(utteranceId: String?) {
                    _ttsState.value = TtsState.COMPLETED
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _ttsState.value = TtsState.ERROR
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e(TAG, "TTS utterance error: $errorCode")
                    _ttsState.value = TtsState.ERROR
                }
            })

            // Check if Urdu is available
            val urduLocale = Locale("ur", "PK")
            val urduResult = tts?.isLanguageAvailable(urduLocale)
            if (urduResult == TextToSpeech.LANG_AVAILABLE || urduResult == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                _isUrduSupported.value = true
            }

            // Set default rate
            tts?.setSpeechRate(_speechRate.value)
            tts?.setPitch(1.0f)
        } else {
            Log.e(TAG, "TTS initialization failed with status $status")
            _ttsState.value = TtsState.ERROR
        }
    }

    fun speak(text: String, languageCode: String = "en") {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized yet")
            return
        }

        stop()

        _currentUtterance.value = text

        val targetLocale = when (languageCode) {
            "ur" -> {
                val urduLocale = Locale("ur", "PK")
                if (_isUrduSupported.value) urduLocale else Locale.getDefault()
            }
            "roman" -> Locale.US
            else -> Locale.US
        }

        try {
            val langResult = tts?.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default locale
                tts?.language = Locale.getDefault()
            }
        } catch (_: Exception) {
            tts?.language = Locale.US
        }

        tts?.setSpeechRate(_speechRate.value)
        tts?.setPitch(1.0f)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "malik_market_summary_${System.currentTimeMillis()}")
        }

        _ttsState.value = TtsState.SPEAKING
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "malik_summary_utterance")
    }

    fun setSpeed(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 2.0f)
        _speechRate.value = clamped
        tts?.setSpeechRate(clamped)
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
            _ttsState.value = TtsState.IDLE
        }
    }

    fun shutdown() {
        try {
            stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }

    /**
     * Converts a raw summary or rates list into natural, phonetically clear spoken sentences.
     */
    fun formatSpokenSummary(
        dateStr: String,
        ratesDescription: List<String>,
        sentiment: String,
        lang: String = "en"
    ): String {
        return when (lang) {
            "ur" -> {
                val sb = StringBuilder()
                sb.append("ملک آئل اینڈ ایکسپیلر ملز سمندری کی روزانہ منڈی رپورٹ برائے $dateStr۔ ")
                sb.append("منڈی کا مجموعی رجحان $sentiment ہے۔ ")
                ratesDescription.forEach { line ->
                    sb.append(line).append("۔ ")
                }
                sb.append("تمام نرخ تصدیق شدہ مشاہدات کے مطابق ہیں۔ شکریہ۔")
                sb.toString()
            }
            "roman" -> {
                val sb = StringBuilder()
                sb.append("Malik Oil and Expeller Mills Samundri ki daily market report, date $dateStr. ")
                sb.append("Overall market trend is $sentiment. ")
                ratesDescription.forEach { line ->
                    sb.append(line).append(". ")
                }
                sb.append("Rates are verified with 3 percent alert policy. Shukriya.")
                sb.toString()
            }
            else -> {
                val sb = StringBuilder()
                sb.append("Malik Oil & Expeller Mills Samundri daily market summary for $dateStr. ")
                sb.append("The overall market sentiment is $sentiment. ")
                ratesDescription.forEach { line ->
                    sb.append(line).append(". ")
                }
                sb.append("All quotes are verified against official mandi weighbridge standards. Thank you.")
                sb.toString()
            }
        }
    }
}
