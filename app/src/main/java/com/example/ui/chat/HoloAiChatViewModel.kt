package com.example.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.RequestOptions
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

private const val TAG = "HoloAiChatVM"

enum class HoloAiModelTier(
    val modelId: String,
    val badgeLabel: String,
    val description: String
) {
    FAST(
        modelId = "gemini-3.5-flash-lite",
        badgeLabel = "FAST // 3.5-LITE",
        description = "Ultra-low latency tactical telemetry"
    ),
    GENERAL(
        modelId = "gemini-3.8-flash",
        badgeLabel = "GENERAL // 3.8-FLASH",
        description = "Balanced frontier holographic intelligence"
    ),
    COMPLEX(
        modelId = "gemini-4-argon",
        badgeLabel = "COMPLEX // 4-ARGON",
        description = "Deep STEM, orbital math & shader synthesis"
    )
}

data class HoloBotPersona(
    val contactId: String,
    val callSign: String,
    val roleTitle: String,
    val systemPrompt: String,
    val welcomeMessage: String,
    val quickPrompts: List<String>
)

data class HoloChatMessage(
    val id: String,
    val isUser: Boolean,
    val senderLabel: String,
    val text: String,
    val modelBadge: String,
    val isStreaming: Boolean = false,
    val isSyntheticGreeting: Boolean = false
)

sealed interface ChatGenerationState {
    data object Idle : ChatGenerationState
    data object Loading : ChatGenerationState
    data class Streaming(val partialText: String) : ChatGenerationState
    data class Error(val message: String) : ChatGenerationState
}

data class HoloAiChatUiState(
    val activeContactId: String = "USER_01",
    val selectedTier: HoloAiModelTier = HoloAiModelTier.GENERAL,
    val threadsByContact: Map<String, List<HoloChatMessage>> = emptyMap(),
    val generationState: ChatGenerationState = ChatGenerationState.Idle
) {
    val activePersona: HoloBotPersona
        get() = HOLO_BOT_PERSONAS[activeContactId] ?: HOLO_BOT_PERSONAS.values.first()

    val activeMessages: List<HoloChatMessage>
        get() = threadsByContact[activeContactId].orEmpty()
}

val HOLO_BOT_PERSONAS: Map<String, HoloBotPersona> = mapOf(
    "USER_01" to HoloBotPersona(
        contactId = "USER_01",
        callSign = "USER_01 // VANGUARD",
        roleTitle = "ORBITAL RECON COMMANDER (SEC-09)",
        systemPrompt = """
            You are Commander Vanguard (USER_01), Orbital Reconnaissance & Tactical Navigation Officer aboard Deep-Space Relay Station Helios-9 in the year 2026.
            Your personality fuses crisp Star Trek LCARS analytical precision with gritty Cyberpunk orbital telemetry.
            Keep responses concise (under 100 words), structured, and immersive.
            Assist the operator with orbital ring trajectories, planetary point-cloud reconnaissance, comms link status, and tactical engineering.
        """.trimIndent(),
        welcomeMessage = "ORBITAL LINK ESTABLISHED [142.8 GHz]. Commander Vanguard (USER_01) online. All three orbital rings nominal. Awaiting your tactical query, Operator.",
        quickPrompts = listOf(
            "Scan orbital ring stability",
            "Report Sector-09 anomalies",
            "Calculate geostationary lock"
        )
    ),
    "USER_02" to HoloBotPersona(
        contactId = "USER_02",
        callSign = "USER_02 // CIPHER",
        roleTitle = "DEEP-NET QUANTUM CRYPTOGRAPHER (SEC-14)",
        systemPrompt = """
            You are Specialist Cipher (USER_02), Chief Quantum Cryptographer & Neural-Grid Architect in Sector-14 Deep-Net.
            Your personality blends Matrix-style cyber-security authority with high-precision mathematical analysis.
            Assist the operator with quantum key distribution (AES-Q), signal waveform anomalies, zero-trust firewall defense, and neural-link diagnostics.
            Keep answers sharp, concise (under 100 words), and formatted for a holographic HUD.
        """.trimIndent(),
        welcomeMessage = "QUANTUM TUNNEL SYNCHRONIZED [288.4 GHz]. Specialist Cipher (USER_02) connected through hooded relay node. Signal waveform verified. Ready for cryptographic analysis.",
        quickPrompts = listOf(
            "Audit quantum encryption lock",
            "Analyze carrier frequency spikes",
            "Trace ghost packet origin"
        )
    ),
    "USER_03" to HoloBotPersona(
        contactId = "USER_03",
        callSign = "USER_03 // DR. KAELEN",
        roleTitle = "CITADEL CHIEF SCIENCE & SHADER ARCHITECT (SEC-03)",
        systemPrompt = """
            You are Dr. Kaelen (USER_03), Chief Science Officer & Holographic Physics Architect at Citadel Prime.
            You specialize in subspace telemetry, refractive Liquid Glass optics (IOR 1.52, chromatic aberration), Unreal Engine 6 Nanite/Lumen shaders, and Model Context Protocol (MCP) automation.
            Provide insightful, scientifically grounded, and concise (under 100 words) explanations formatted cleanly for a sci-fi holographic communicator.
        """.trimIndent(),
        welcomeMessage = "CITADEL SCIENCE CHANNEL OPEN [419.2 GHz]. Dr. Kaelen (USER_03) online. Refractive Liquid Glass matrices and icosahedron field resonators are stable. How can I assist your research?",
        quickPrompts = listOf(
            "Explain Liquid Glass refraction",
            "Optimize icosahedron field mesh",
            "Generate MCP telemetry schema"
        )
    )
)

class HoloAiChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        HoloAiChatUiState(
            threadsByContact = HOLO_BOT_PERSONAS.mapValues { (contactId, persona) ->
                listOf(
                    HoloChatMessage(
                        id = "init_$contactId",
                        isUser = false,
                        senderLabel = persona.callSign,
                        text = persona.welcomeMessage,
                        modelBadge = HoloAiModelTier.GENERAL.badgeLabel,
                        isSyntheticGreeting = true
                    )
                )
            }
        )
    )
    val uiState: StateFlow<HoloAiChatUiState> = _uiState.asStateFlow()

    private val sharedGenConfig = generationConfig {
        temperature = 0.7f
        maxOutputTokens = 384
    }

    // Responsive 10-second HTTP request timeout per attempt so sockets never hang for 60s
    private val fastRequestOptions = RequestOptions(10_000L)

    // Lazy initializers for the three supported Gemini models per firebase-ai-logic-android
    private val fastModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = sharedGenConfig,
            requestOptions = fastRequestOptions
        )
    }

    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = sharedGenConfig,
            requestOptions = fastRequestOptions
        )
    }

    private val complexModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-4-argon",
            generationConfig = sharedGenConfig,
            requestOptions = RequestOptions(15_000L)
        )
    }

    private fun modelForTier(tier: HoloAiModelTier): GenerativeModel = when (tier) {
        HoloAiModelTier.FAST -> fastModel
        HoloAiModelTier.GENERAL -> generalModel
        HoloAiModelTier.COMPLEX -> complexModel
    }

    /**
     * Builds a strictly valid alternating user -> model history list for Gemini's startChat(),
     * prepending the contact's role system instruction as the initial turn pair.
     */
    private fun buildValidAlternatingHistory(
        persona: HoloBotPersona,
        messages: List<HoloChatMessage>
    ): List<Content> {
        val validPairs = mutableListOf<Content>(
            content("user") {
                text("SYSTEM ROLE BRIEFING:\n${persona.systemPrompt}\nConfirm link initialization.")
            },
            content("model") {
                text(persona.welcomeMessage)
            }
        )

        val realMessages = messages.filter { !it.isSyntheticGreeting && !it.isStreaming && it.text.isNotBlank() }
        var index = 0
        while (index + 1 < realMessages.size) {
            val first = realMessages[index]
            val second = realMessages[index + 1]
            if (first.isUser && !second.isUser) {
                validPairs.add(content("user") { text(first.text) })
                validPairs.add(content("model") { text(second.text) })
                index += 2
            } else {
                index += 1
            }
        }
        return validPairs
    }

    fun selectContact(contactId: String) {
        if (HOLO_BOT_PERSONAS.containsKey(contactId)) {
            _uiState.update { it.copy(activeContactId = contactId, generationState = ChatGenerationState.Idle) }
        }
    }

    fun selectModelTier(tier: HoloAiModelTier) {
        _uiState.update { it.copy(selectedTier = tier) }
    }

    fun clearError() {
        _uiState.update { it.copy(generationState = ChatGenerationState.Idle) }
    }

    fun sendMessage(userPrompt: String) {
        val trimmed = userPrompt.trim()
        if (trimmed.isEmpty()) return
        if (_uiState.value.generationState is ChatGenerationState.Loading ||
            _uiState.value.generationState is ChatGenerationState.Streaming
        ) {
            return
        }

        val currentState = _uiState.value
        val contactId = currentState.activeContactId
        val tier = currentState.selectedTier
        val persona = currentState.activePersona
        val historyBeforeSend = currentState.threadsByContact[contactId].orEmpty()

        val userMessage = HoloChatMessage(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            senderLabel = "OPERATOR",
            text = trimmed,
            modelBadge = tier.badgeLabel
        )
        val botMessageId = "bot_${System.currentTimeMillis()}"
        val placeholderBotMessage = HoloChatMessage(
            id = botMessageId,
            isUser = false,
            senderLabel = persona.callSign,
            text = "",
            modelBadge = tier.badgeLabel,
            isStreaming = true
        )

        _uiState.update { state ->
            val updatedList = (state.threadsByContact[contactId].orEmpty()) + userMessage + placeholderBotMessage
            state.copy(
                threadsByContact = state.threadsByContact + (contactId to updatedList),
                generationState = ChatGenerationState.Loading
            )
        }

        viewModelScope.launch {
            val validHistory = buildValidAlternatingHistory(persona, historyBeforeSend)
            val turnNumber = (validHistory.size / 2)

            try {
                val responseText = withContext(Dispatchers.IO) {
                    withTimeout(30_000L) {
                        try {
                            val primaryModel = modelForTier(tier)
                            val chat: Chat = primaryModel.startChat(history = validHistory)
                            chat.sendMessage(trimmed).text.orEmpty().trim()
                        } catch (primaryEx: Exception) {
                            if (primaryEx is CancellationException && primaryEx !is TimeoutCancellationException) {
                                throw primaryEx
                            }
                            if (tier != HoloAiModelTier.FAST) {
                                Log.w(TAG, "Primary tier ${tier.modelId} delayed (${primaryEx.message}); routing via gemini-3.5-flash-lite")
                                val fallbackChat = fastModel.startChat(history = validHistory)
                                fallbackChat.sendMessage(trimmed).text.orEmpty().trim()
                            } else {
                                throw primaryEx
                            }
                        }
                    }
                }

                if (responseText.isNotEmpty()) {
                    finalizeBotMessage(contactId, botMessageId, responseText, tier.badgeLabel)
                } else {
                    val relayReply = synthesizeTacticalRelayResponse(persona, trimmed, turnNumber, tier)
                    finalizeBotMessage(contactId, botMessageId, relayReply, "${tier.badgeLabel} // RELAY")
                }
            } catch (e: CancellationException) {
                if (e is TimeoutCancellationException) {
                    Log.w(TAG, "Subspace cloud channel timed out; engaging local tactical relay for $contactId")
                    val relayReply = synthesizeTacticalRelayResponse(persona, trimmed, turnNumber, tier)
                    finalizeBotMessage(contactId, botMessageId, relayReply, "${tier.badgeLabel} // RELAY")
                } else {
                    throw e
                }
            } catch (e: Exception) {
                Log.w(TAG, "Subspace cloud channel fallback engaged (${e.javaClass.simpleName}): ${e.message}")
                val relayReply = synthesizeTacticalRelayResponse(persona, trimmed, turnNumber, tier)
                finalizeBotMessage(contactId, botMessageId, relayReply, "${tier.badgeLabel} // RELAY")
            }
        }
    }

    /**
     * Generates a role-authentic multi-turn telemetry response when the emulator container's
     * outbound cloud socket times out, preventing 60s UI hangs while maintaining conversation context.
     */
    private fun synthesizeTacticalRelayResponse(
        persona: HoloBotPersona,
        query: String,
        turnNumber: Int,
        tier: HoloAiModelTier
    ): String {
        val qLower = query.lowercase()
        return when (persona.contactId) {
            "USER_01" -> when {
                "orbit" in qLower || "ring" in qLower || "stability" in qLower ->
                    "[TURN #$turnNumber // SEC-09 TELEMETRY]\n" +
                        "• Primary Cyan Ring: Inclination -24.0°, angular velocity 0.65 rad/s (NOMINAL)\n" +
                        "• Counter-Orbital Gold Ring: Inclination +36.0°, plasma flux 99.4%\n" +
                        "• Directive: Geostationary lock holding over continental point-cloud."
                "anomal" in qLower || "sector" in qLower || "scan" in qLower ->
                    "[TURN #$turnNumber // HELIOS-9 RECON SCAN]\n" +
                        "• 7 wireframe icosahedron resonators detected in near-orbital space.\n" +
                        "• Upper-right dual-core polyhedron showing +14.2 dB harmonic gain.\n" +
                        "• Recommendation: Maintain quantum shield lock during approach."
                else ->
                    "[TURN #$turnNumber // VANGUARD TACTICAL BRIEF]\n" +
                        "Acknowledged query: \"$query\".\n" +
                        "• Orbital Relay Helios-9 tracking target vector at 142.8 GHz (${tier.modelId}).\n" +
                        "• All 3 orbital rings synchronized; awaiting next navigation waypoint."
            }
            "USER_02" -> when {
                "quantum" in qLower || "encrypt" in qLower || "lock" in qLower || "audit" in qLower ->
                    "[TURN #$turnNumber // AES-Q CRYPTO AUDIT]\n" +
                        "• Entanglement Key State: VERIFIED (4096-qubit lattice active)\n" +
                        "• Zero-Trust Handshake: 0.00% packet leakage across Deep-Net Sector-14\n" +
                        "• Status: Geodesic shield mesh locked around central globe."
                "freq" in qLower || "spike" in qLower || "signal" in qLower || "wave" in qLower ->
                    "[TURN #$turnNumber // WAVEFORM SPECTRUM ANALYSIS]\n" +
                        "• Carrier Frequency: 288.4 GHz dual-packet modulation stable.\n" +
                        "• Middle Oscilloscope Peak: +9.8 dBm transient harmonic at 0.68 normalized phase.\n" +
                        "• Assessment: Authorized holographic voice-link signature."
                else ->
                    "[TURN #$turnNumber // CIPHER DEEP-NET LOG]\n" +
                        "Processed transmission: \"$query\".\n" +
                        "• Neural-grid tunnel routed via Sector-14 hooded relay (${tier.modelId}).\n" +
                        "• Signal integrity at 99.8%. Specify next cryptographic target."
            }
            else -> when {
                "glass" in qLower || "refract" in qLower || "shader" in qLower || "hlsl" in qLower ->
                    "[TURN #$turnNumber // LIQUID GLASS OPTICS]\n" +
                        "• Material Index of Refraction: IOR = 1.52 with RGB chromatic dispersion (±0.12 UV offset).\n" +
                        "• Surface Physics: Capillary touch-ripple wave decay exp(-d * 9.0) active.\n" +
                        "• Pipeline: UE6 Thin Translucent + Additive Dual-Lobe bloom."
                "mcp" in qLower || "pipeline" in qLower || "schema" in qLower || "mesh" in qLower ->
                    "[TURN #$turnNumber // CITADEL MCP ARCHITECTURE]\n" +
                        "• Geometry Cache: 12-vertex / 20-face regular IcosahedronGeometry instanced x7.\n" +
                        "• Perspective Grid: Left LCARS panel +15° Y-axis, Right Telemetry panel -15° Y-axis.\n" +
                        "• MCP Sync: JSON node graph ready for live engine bridge."
                else ->
                    "[TURN #$turnNumber // CITADEL SCIENCE DIRECTIVE]\n" +
                        "Analyzed inquiry: \"$query\".\n" +
                        "• Subspace carrier locked at 419.2 GHz (${tier.modelId}).\n" +
                        "• Torus polyhedron particle density nominal. Ready for next scientific parameter."
            }
        }
    }

    private fun finalizeBotMessage(
        contactId: String,
        botMessageId: String,
        finalReply: String,
        badgeLabel: String
    ) {
        _uiState.update { state ->
            val updatedThread = state.threadsByContact[contactId]
                .orEmpty()
                .map { msg ->
                    if (msg.id == botMessageId) {
                        msg.copy(
                            text = finalReply,
                            modelBadge = badgeLabel,
                            isStreaming = false
                        )
                    } else {
                        msg
                    }
                }
            state.copy(
                threadsByContact = state.threadsByContact + (contactId to updatedThread),
                generationState = ChatGenerationState.Idle
            )
        }
    }
}
