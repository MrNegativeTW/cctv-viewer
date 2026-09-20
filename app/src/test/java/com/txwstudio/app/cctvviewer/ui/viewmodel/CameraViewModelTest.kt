package com.txwstudio.app.cctvviewer.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.txwstudio.app.cctvviewer.data.Camera
import com.txwstudio.app.cctvviewer.data.CameraRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val cameras = List(8) { Camera("Camera ${it + 1}", "https://example.invalid/${it + 1}") }
    private val repository = CameraRepository { cameras }
    private val models = mutableListOf<CameraViewModel>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        models.forEach { it.onForegroundChanged(false) }
        Dispatchers.resetMain()
    }

    private fun model(handle: SavedStateHandle = SavedStateHandle()) =
        CameraViewModel(repository, handle).also { models += it }

    @Test
    fun startsWithFirstCameraAndVisibleMenu() {
        val state = model().uiState.value
        assertEquals(cameras, state.cameras)
        assertEquals(cameras.first(), state.activeCamera)
        assertEquals(0, state.focusedIndex)
        assertTrue(state.cameraBarVisible)
    }

    @Test
    fun movingFocusDoesNotLoadUntilConfirmed() {
        val vm = model()
        vm.onAction(CameraAction.FocusNextCamera)
        vm.onAction(CameraAction.FocusNextCamera)
        assertEquals(2, vm.uiState.value.focusedIndex)
        assertEquals(0, vm.uiState.value.activeIndex)
        assertEquals(0, vm.uiState.value.reloadVersion)
        vm.onAction(CameraAction.ConfirmCamera(2))
        assertEquals(2, vm.uiState.value.activeIndex)
    }

    @Test
    fun focusStopsAtBothEnds() {
        val vm = model()
        vm.onAction(CameraAction.FocusPreviousCamera)
        assertEquals(0, vm.uiState.value.focusedIndex)
        repeat(12) { vm.onAction(CameraAction.FocusNextCamera) }
        assertEquals(7, vm.uiState.value.focusedIndex)
    }

    @Test
    fun hiddenMenuRetainsFocusAndIgnoresSelection() {
        val vm = model()
        vm.onAction(CameraAction.CameraFocused(5))
        vm.onAction(CameraAction.HideCameraBar)
        vm.onAction(CameraAction.FocusNextCamera)
        vm.onAction(CameraAction.CameraFocused(1))
        vm.onAction(CameraAction.ConfirmCamera(1))
        assertEquals(5, vm.uiState.value.focusedIndex)
        assertEquals(0, vm.uiState.value.activeIndex)
        vm.onAction(CameraAction.ShowCameraBar)
        assertEquals(5, vm.uiState.value.focusedIndex)
        assertTrue(vm.uiState.value.cameraBarVisible)
    }

    @Test
    fun invalidIndicesDoNotChangeSelection() {
        val vm = model()
        val initial = vm.uiState.value
        vm.onAction(CameraAction.ConfirmCamera(-1))
        vm.onAction(CameraAction.ConfirmCamera(8))
        vm.onAction(CameraAction.CameraFocused(100))
        assertEquals(initial, vm.uiState.value)
    }

    @Test
    fun reloadOnlyChangesPageRequest() {
        val vm = model()
        vm.onAction(CameraAction.ConfirmCamera(3))
        vm.onAction(CameraAction.HideCameraBar)
        vm.onAction(CameraAction.ReloadCamera)
        assertEquals(3, vm.uiState.value.activeIndex)
        assertEquals(3, vm.uiState.value.focusedIndex)
        assertEquals(1, vm.uiState.value.reloadVersion)
        assertFalse(vm.uiState.value.cameraBarVisible)
    }

    @Test
    fun confirmingActiveCameraDoesNotReloadIt() {
        val vm = model()
        val initial = vm.uiState.value
        vm.onAction(CameraAction.ConfirmCamera(0))
        assertEquals(initial, vm.uiState.value)
    }

    @Test
    fun restoresActiveCameraFocusAndMenuVisibility() {
        val handle = SavedStateHandle()
        val original = model(handle)
        original.onAction(CameraAction.ConfirmCamera(2))
        original.onAction(CameraAction.CameraFocused(6))
        original.onAction(CameraAction.HideCameraBar)
        val restored = model(SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) }))
        assertEquals(original.uiState.value, restored.uiState.value)
    }

    @Test
    fun clampsRestoredIndicesToAvailableCameras() {
        val state = model(SavedStateHandle(mapOf(
            "active_camera_index" to -1,
            "focused_camera_index" to 99,
        ))).uiState.value
        assertEquals(0, state.activeIndex)
        assertEquals(7, state.focusedIndex)
    }

    @Test
    fun menuHidesAtTenSecondsAndDoesNotReloadCamera() = runTest {
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(9_999)
        runCurrent()
        assertTrue(vm.uiState.value.cameraBarVisible)
        advanceTimeBy(1)
        runCurrent()
        assertFalse(vm.uiState.value.cameraBarVisible)
        assertEquals(0, vm.uiState.value.activeIndex)
        assertEquals(0, vm.uiState.value.reloadVersion)
    }

    @Test
    fun interactionCancelsOldDeadlineAndStartsFullTimeout() = runTest {
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(7_000)
        vm.onAction(CameraAction.UserInteraction)
        runCurrent()
        advanceTimeBy(9_999)
        runCurrent()
        assertTrue(vm.uiState.value.cameraBarVisible)
        advanceTimeBy(1)
        runCurrent()
        assertFalse(vm.uiState.value.cameraBarVisible)
    }

    @Test
    fun confirmationRestartsTimerEvenWhenCameraIsUnchanged() = runTest {
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(8_000)
        vm.onAction(CameraAction.ConfirmCamera(0))
        runCurrent()
        advanceTimeBy(3_000)
        runCurrent()
        assertTrue(vm.uiState.value.cameraBarVisible)
        advanceTimeBy(7_000)
        runCurrent()
        assertFalse(vm.uiState.value.cameraBarVisible)
    }

    @Test
    fun backgroundCancelsTimerAndResumeStartsFreshTimeout() = runTest {
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(8_000)
        vm.onForegroundChanged(false)
        advanceTimeBy(30_000)
        runCurrent()
        assertTrue(vm.uiState.value.cameraBarVisible)
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(9_999)
        runCurrent()
        assertTrue(vm.uiState.value.cameraBarVisible)
        advanceTimeBy(1)
        runCurrent()
        assertFalse(vm.uiState.value.cameraBarVisible)
    }

    @Test
    fun automaticFocusRestorationDoesNotExtendTimeout() = runTest {
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(9_000)
        vm.onAction(CameraAction.CameraFocused(0))
        advanceTimeBy(1_000)
        runCurrent()
        assertFalse(vm.uiState.value.cameraBarVisible)
    }

    @Test
    fun showingMenuAfterAutoHideStartsAnotherTimeout() = runTest {
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(10_000)
        runCurrent()
        vm.onAction(CameraAction.ShowCameraBar)
        runCurrent()
        advanceTimeBy(9_999)
        runCurrent()
        assertTrue(vm.uiState.value.cameraBarVisible)
        advanceTimeBy(1)
        runCurrent()
        assertFalse(vm.uiState.value.cameraBarVisible)
    }

    @Test
    fun manualHideCancelsOldTimerBeforeMenuIsShownAgain() = runTest {
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(9_000)
        vm.onAction(CameraAction.HideCameraBar)
        vm.onAction(CameraAction.ShowCameraBar)
        runCurrent()
        advanceTimeBy(2_000)
        runCurrent()
        assertTrue(vm.uiState.value.cameraBarVisible)
    }
}
