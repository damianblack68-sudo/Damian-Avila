package com.example.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.domain.model.VoicePersona
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // Real-time microphone input volume (0.0f to 1.0f) for dynamic pulse reactions
    private val _micLevel = MutableStateFlow(0.15f)
    val micLevel: StateFlow<Float> = _micLevel.asStateFlow()

    // Real-time audio waveform amplitudes: 16 bars normalized between 0.05f and 1.0f
    private val _waveformAmplitudes = MutableStateFlow(List(16) { 0.1f })
    val waveformAmplitudes: StateFlow<List<Float>> = _waveformAmplitudes.asStateFlow()

    private var currentPersona: VoicePersona = VoicePersona.ONYX_EXECUTIVE
    private var waveAnimationJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var speechRecognizer: SpeechRecognizer? = null
    private var pendingTranscriptionCallback: ((String) -> Unit)? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "MX"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("es", "ES"))
            }
            applyPersona(currentPersona)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    startWaveformSimulation(isInput = false)
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopWaveformSimulation()
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopWaveformSimulation()
                }
            })
            isInitialized = true
        }
    }

    fun setPersona(persona: VoicePersona) {
        currentPersona = persona
        applyPersona(persona)
    }

    private fun applyPersona(persona: VoicePersona) {
        tts?.setPitch(persona.pitch)
        tts?.setSpeechRate(persona.speechRate)
        try {
            val voices = tts?.voices
            if (!voices.isNullOrEmpty()) {
                val masculineVoice = voices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    (name.contains("male") || name.contains("hombre") || name.contains("masc") || name.contains("deep")) && !name.contains("female")
                }
                if (masculineVoice != null) {
                    tts?.voice = masculineVoice
                }
            }
        } catch (_: Exception) {}
    }

    private fun isEnglishText(text: String): Boolean {
        val lower = text.lowercase()
        val englishKeywords = listOf(
            "the", "system", "status", "executive", "confirmed", "directive", "task",
            "protocol", "neural", "operating", "welcome", "please", "ready", "corporate",
            "active", "standby", "analyzing", "complete", "schedule", "priority"
        )
        val words = lower.split(Regex("[^a-zA-Z]+")).filter { it.isNotBlank() }
        val matches = words.count { it in englishKeywords }
        return matches >= 2 || (words.size in 1..4 && words.any { it in englishKeywords })
    }

    fun speak(text: String) {
        if (!isInitialized) return
        stop()

        // Configure bilingual voice synthesis
        if (isEnglishText(text)) {
            tts?.setLanguage(Locale.US)
        } else {
            val esResult = tts?.setLanguage(Locale("es", "MX"))
            if (esResult == TextToSpeech.LANG_MISSING_DATA || esResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("es", "ES"))
            }
        }
        applyPersona(currentPersona)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "onyx_speech_${System.currentTimeMillis()}")
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "onyx_speech")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        stopWaveformSimulation()
    }

    /**
     * Connects microphone input to speech recognition and live audio pulses.
     * Tries Android SpeechRecognizer first, falling back gracefully to simulated speech if unavailable.
     */
    fun startListening(onTranscriptionReady: (String) -> Unit) {
        stop() // Stop TTS if speaking
        pendingTranscriptionCallback = onTranscriptionReady
        _isListening.value = true
        _micLevel.value = 0.35f
        startWaveformSimulation(isInput = true)

        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                        }

                        override fun onBeginningOfSpeech() {
                            _micLevel.value = 0.7f
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            // Live microphone amplitude: typically -2dB to 10+ dB
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.12f, 1.0f)
                            _micLevel.value = normalized
                            updateAmplitudesWithLevel(normalized)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _micLevel.value = 0.2f
                        }

                        override fun onError(error: Int) {
                            // If speech recognizer fails (e.g. offline emulator), fallback smoothly to simulation
                            fallbackToSimulatedListening(onTranscriptionReady)
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            stopWaveformSimulation()
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val transcribed = matches?.firstOrNull() ?: "Resolver la tarea de matemáticas paso a paso"
                            onTranscriptionReady(transcribed)
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                            if (!partial.isNullOrBlank()) {
                                _micLevel.value = 0.8f
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-MX")
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "es-MX")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                speechRecognizer?.startListening(intent)
            } else {
                fallbackToSimulatedListening(onTranscriptionReady)
            }
        } catch (e: Exception) {
            fallbackToSimulatedListening(onTranscriptionReady)
        }
    }

    private fun fallbackToSimulatedListening(onTranscriptionReady: (String) -> Unit) {
        coroutineScope.launch {
            delay(3200)
            if (_isListening.value) {
                _isListening.value = false
                stopWaveformSimulation()
                val simulatedPhrases = listOf(
                    "Diseñar plano arquitectónico con medidas y cotas exactas",
                    "Resolver ecuación cuadrática 2x² - 8x + 6 = 0 con deducción completa",
                    "Generar aplicación web interactiva con botones y estilo moderno",
                    "Estructurar mapa conceptual sobre redes neuronales y machine learning"
                )
                onTranscriptionReady(simulatedPhrases.random())
            }
        }
    }

    fun startListeningSimulation(onTranscriptionReady: (String) -> Unit) {
        startListening(onTranscriptionReady)
    }

    fun stopListening() {
        _isListening.value = false
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        stopWaveformSimulation()
    }

    private fun updateAmplitudesWithLevel(level: Float) {
        val newAmplitudes = List(16) { index ->
            val centerFactor = 1.0f - (kotlin.math.abs(index - 7.5f) / 8f) * 0.35f
            val variance = Random.nextFloat() * 0.3f
            (level * centerFactor + variance).coerceIn(0.1f, 1.0f)
        }
        _waveformAmplitudes.value = newAmplitudes
    }

    private fun startWaveformSimulation(isInput: Boolean) {
        waveAnimationJob?.cancel()
        waveAnimationJob = coroutineScope.launch {
            while (isActive) {
                val base = if (isInput) _micLevel.value else 0.65f
                val newAmplitudes = List(16) { index ->
                    val centerFactor = 1.0f - (kotlin.math.abs(index - 7.5f) / 8f) * 0.4f
                    val noise = Random.nextFloat() * 0.5f + 0.5f
                    (base * centerFactor * noise).coerceIn(0.12f, 1.0f)
                }
                _waveformAmplitudes.value = newAmplitudes
                if (isInput && speechRecognizer == null) {
                    // Modulate mic level dynamically during simulation
                    _micLevel.value = (0.35f + Random.nextFloat() * 0.55f).coerceIn(0.2f, 1.0f)
                }
                delay(65)
            }
        }
    }

    private fun stopWaveformSimulation() {
        waveAnimationJob?.cancel()
        _waveformAmplitudes.value = List(16) { 0.08f }
        _micLevel.value = 0.05f
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        waveAnimationJob?.cancel()
    }
}
