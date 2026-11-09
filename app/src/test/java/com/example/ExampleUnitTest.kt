package com.example

import com.example.model.HoloViewMode
import com.example.ui.HudViewModel
import com.example.ui.render3d.HoloGeometryCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `verify 3d holographic geometry and viewmodel state`() {
        // Verify exact icosahedron geometry (12 vertices, 20 faces, 30 edges)
        assertEquals(12, HoloGeometryCache.icosahedronVertices.size)
        assertEquals(20, HoloGeometryCache.icosahedronFaces.size)
        assertEquals(30, HoloGeometryCache.icosahedronEdges.size)

        // Verify point-cloud globe and floating polyhedrons are populated
        assertTrue(HoloGeometryCache.globePoints.isNotEmpty())
        assertEquals(7, HoloGeometryCache.scenePolyhedrons.size)

        // Verify HudViewModel interactions
        val viewModel = HudViewModel()
        assertEquals("CONNECTING...", viewModel.uiState.value.centerStatusText)

        viewModel.selectContact("USER_03")
        assertEquals("USER_03", viewModel.uiState.value.selectedContactId)

        viewModel.toggleCall()
        assertTrue(viewModel.uiState.value.isCallActive)
        assertEquals("CALLING USER_03...", viewModel.uiState.value.centerStatusText)

        viewModel.toggleSecure()
        assertTrue(viewModel.uiState.value.isSecureActive)
        assertEquals("SECURE LINK: USER_03", viewModel.uiState.value.centerStatusText)

        viewModel.disconnectOrReconnect()
        assertTrue(viewModel.uiState.value.isDisconnected)
        assertFalse(viewModel.uiState.value.isCallActive)
        assertEquals("DISCONNECTED", viewModel.uiState.value.centerStatusText)

        viewModel.setViewMode(HoloViewMode.THREEJS_WEBGL)
        assertEquals(HoloViewMode.THREEJS_WEBGL, viewModel.uiState.value.viewMode)
    }
}
