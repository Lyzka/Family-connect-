package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.HudViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context and verify holographic hud state`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HoloComm 3D", appName)

        val viewModel = HudViewModel()
        assertEquals("CONNECTING...", viewModel.uiState.value.centerStatusText)

        viewModel.selectContact("USER_02")
        assertEquals("USER_02", viewModel.uiState.value.selectedContactId)

        viewModel.toggleCall()
        assertTrue(viewModel.uiState.value.isCallActive)
        assertEquals("CALLING USER_02...", viewModel.uiState.value.centerStatusText)
    }
}
