package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.model.ConnectionState
import com.example.model.HoloCommUiState
import com.example.model.HoloViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HudViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HoloCommUiState())
    val uiState: StateFlow<HoloCommUiState> = _uiState.asStateFlow()

    fun selectContact(contactId: String) {
        _uiState.update { state ->
            val contact = state.contacts.find { it.id == contactId }
            state.copy(
                selectedContactId = contactId,
                isDisconnected = false,
                connectionState = if (state.isCallActive) ConnectionState.CALL_ACTIVE else ConnectionState.CONNECTING,
                globeYawOffset = (contact?.targetLon ?: 0f) * 0.015f,
                pulseEnergy = 1.6f
            )
        }
    }

    fun toggleCall() {
        _uiState.update { state ->
            val nextCall = !state.isCallActive
            state.copy(
                isCallActive = nextCall,
                isDisconnected = false,
                connectionState = if (nextCall) ConnectionState.CALL_ACTIVE else ConnectionState.CONNECTING,
                pulseEnergy = if (nextCall) 1.5f else 1.0f
            )
        }
    }

    fun toggleVideo() {
        _uiState.update { state ->
            val nextVideo = !state.isVideoActive
            state.copy(
                isVideoActive = nextVideo,
                isCallActive = if (nextVideo) true else state.isCallActive,
                isDisconnected = false,
                showCameraPermissionNotice = false,
                connectionState = if (nextVideo) ConnectionState.VIDEO_ACTIVE else ConnectionState.CONNECTING,
                pulseEnergy = if (nextVideo) 1.8f else 1.0f
            )
        }
    }

    fun onCameraPermissionResult(isGranted: Boolean, activateVideoOnGrant: Boolean = true) {
        _uiState.update { state ->
            if (isGranted) {
                val nextVideo = if (activateVideoOnGrant) !state.isVideoActive else state.isVideoActive
                state.copy(
                    cameraPermissionGranted = true,
                    showCameraPermissionNotice = false,
                    isVideoActive = nextVideo,
                    isCallActive = if (nextVideo) true else state.isCallActive,
                    isDisconnected = false,
                    connectionState = if (nextVideo) ConnectionState.VIDEO_ACTIVE else state.connectionState,
                    pulseEnergy = if (nextVideo) 1.8f else state.pulseEnergy
                )
            } else {
                state.copy(
                    cameraPermissionGranted = false,
                    isVideoActive = false,
                    showCameraPermissionNotice = true,
                    pulseEnergy = 0.8f
                )
            }
        }
    }

    fun toggleSecure() {
        _uiState.update { state ->
            val nextSecure = !state.isSecureActive
            state.copy(
                isSecureActive = nextSecure,
                isDisconnected = false,
                connectionState = if (nextSecure) ConnectionState.SECURE_LOCKED else ConnectionState.CONNECTING,
                pulseEnergy = if (nextSecure) 1.4f else 1.0f
            )
        }
    }

    fun disconnectOrReconnect() {
        _uiState.update { state ->
            if (!state.isDisconnected) {
                state.copy(
                    isDisconnected = true,
                    isCallActive = false,
                    isVideoActive = false,
                    isSecureActive = false,
                    connectionState = ConnectionState.DISCONNECTED,
                    pulseEnergy = 0.35f
                )
            } else {
                state.copy(
                    isDisconnected = false,
                    connectionState = ConnectionState.CONNECTING,
                    pulseEnergy = 1.0f
                )
            }
        }
    }

    fun onGlobeDrag(dragX: Float, dragY: Float) {
        _uiState.update { state ->
            state.copy(
                globeYawOffset = state.globeYawOffset + dragX * 0.006f,
                globePitchOffset = (state.globePitchOffset + dragY * 0.004f).coerceIn(-0.65f, 0.65f)
            )
        }
    }

    fun resetGlobeOrbit() {
        _uiState.update { state ->
            state.copy(
                globeYawOffset = 0f,
                globePitchOffset = 0f,
                pulseEnergy = 1.4f
            )
        }
    }

    fun setViewMode(mode: HoloViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }
}
