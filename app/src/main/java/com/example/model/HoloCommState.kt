package com.example.model

enum class BustAvatarType {
    USER_01_MALE,
    USER_02_HOODED,
    USER_03_COMMANDER
}

data class HoloContact(
    val id: String,
    val label: String,
    val avatarType: BustAvatarType,
    val sectorCode: String,
    val frequencyGhz: Float,
    val targetLat: Float,
    val targetLon: Float
)

enum class ConnectionState(val centerLabel: String) {
    CONNECTING("CONNECTING..."),
    CALL_ACTIVE("VOICE LINK ACTIVE"),
    VIDEO_ACTIVE("HOLO STREAM LIVE"),
    SECURE_LOCKED("QUANTUM SECURED"),
    DISCONNECTED("DISCONNECTED")
}

enum class HoloViewMode {
    NATIVE_3D_HUD,
    AI_CHAT,
    THREEJS_WEBGL,
    MCP_SHADER_LAB
}

data class HoloCommUiState(
    val contacts: List<HoloContact> = listOf(
        HoloContact(
            id = "USER_01",
            label = "USER_01",
            avatarType = BustAvatarType.USER_01_MALE,
            sectorCode = "SEC-09 // ORBITAL",
            frequencyGhz = 142.8f,
            targetLat = 35f,
            targetLon = -20f
        ),
        HoloContact(
            id = "USER_02",
            label = "USER_02",
            avatarType = BustAvatarType.USER_02_HOODED,
            sectorCode = "SEC-14 // DEEP NET",
            frequencyGhz = 288.4f,
            targetLat = 50f,
            targetLon = 15f
        ),
        HoloContact(
            id = "USER_03",
            label = "USER_03",
            avatarType = BustAvatarType.USER_03_COMMANDER,
            sectorCode = "SEC-03 //CITADEL",
            frequencyGhz = 419.2f,
            targetLat = 12f,
            targetLon = 75f
        )
    ),
    val selectedContactId: String = "USER_01",
    val connectionState: ConnectionState = ConnectionState.CONNECTING,
    val isCallActive: Boolean = false,
    val isVideoActive: Boolean = false,
    val isSecureActive: Boolean = false,
    val isDisconnected: Boolean = false,
    val cameraPermissionGranted: Boolean = false,
    val showCameraPermissionNotice: Boolean = false,
    val globeYawOffset: Float = 0f,
    val globePitchOffset: Float = 0f,
    val pulseEnergy: Float = 1f,
    val viewMode: HoloViewMode = HoloViewMode.NATIVE_3D_HUD
) {
    val centerStatusText: String
        get() = when {
            isDisconnected -> "DISCONNECTED"
            showCameraPermissionNotice -> "CAMERA AUTH REQUIRED"
            isVideoActive -> "HOLO-VIDEO: $selectedContactId"
            isCallActive && isSecureActive -> "SECURE LINK: $selectedContactId"
            isCallActive -> "CALLING $selectedContactId..."
            isSecureActive -> "CONNECTING [AES-Q]..."
            else -> "CONNECTING..."
        }
}
