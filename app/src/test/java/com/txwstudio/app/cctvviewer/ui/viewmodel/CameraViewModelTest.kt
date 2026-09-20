package com.txwstudio.app.cctvviewer.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.txwstudio.app.cctvviewer.data.Camera
import com.txwstudio.app.cctvviewer.data.FakeCameraRepository
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
    private val repository = FakeCameraRepository(cameras)
    private val stores = mutableListOf<ViewModelStore>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        stores.forEach { it.clear() }
        Dispatchers.resetMain()
    }

    private fun model(handle: SavedStateHandle = SavedStateHandle()): CameraViewModel {
        val store = ViewModelStore().also { stores += it }
        val factory = viewModelFactory { initializer { CameraViewModel(repository, handle) } }
        return ViewModelProvider(store, factory)[CameraViewModel::class.java].also {
            dispatcher.scheduler.runCurrent()
        }
    }

    private fun runViewModelTest(block: suspend TestScope.() -> Unit) = runTest {
        try {
            block()
        } finally {
            // Cancel repeating timers before runTest drains the virtual-time scheduler.
            stores.forEach { it.clear() }
        }
    }

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
        assertEquals(8, vm.uiState.value.focusedIndex)
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
        assertEquals(8, state.focusedIndex)
    }

    @Test
    fun menuHidesAtTenSecondsAndDoesNotReloadCamera() = runViewModelTest {
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
    fun interactionCancelsOldDeadlineAndStartsFullTimeout() = runViewModelTest {
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
    fun confirmationRestartsTimerEvenWhenCameraIsUnchanged() = runViewModelTest {
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
    fun backgroundCancelsTimerAndResumeStartsFreshTimeout() = runViewModelTest {
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
    fun automaticFocusRestorationDoesNotExtendTimeout() = runViewModelTest {
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
    fun showingMenuAfterAutoHideStartsAnotherTimeout() = runViewModelTest {
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
    fun manualHideCancelsOldTimerBeforeMenuIsShownAgain() = runViewModelTest {
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
    @Test
    fun remoteNameChangeUpdatesWithoutReload() = runViewModelTest {
        val vm = model()
        repository.saveCameras(cameras.mapIndexed { index, camera ->
            if (index == 0) camera.copy(name = "新名稱") else camera
        })
        runCurrent()
        assertEquals("新名稱", vm.uiState.value.activeCamera?.name)
        assertEquals(0, vm.uiState.value.reloadVersion)
    }

    @Test
    fun remoteActiveUrlChangeReloadsOnlyCurrentCamera() = runViewModelTest {
        val vm = model()
        repository.saveCameras(cameras.mapIndexed { index, camera ->
            if (index == 1) camera.copy(url = "https://example.invalid/other") else camera
        })
        runCurrent()
        assertEquals(0, vm.uiState.value.reloadVersion)
        repository.saveCameras(repository.cameras.value.mapIndexed { index, camera ->
            if (index == 0) camera.copy(url = "https://example.invalid/current") else camera
        })
        runCurrent()
        assertEquals("https://example.invalid/current", vm.uiState.value.activeCamera?.url)
        assertEquals(1, vm.uiState.value.reloadVersion)
    }

    @Test
    fun settingsPauseAutoHideAndClosingRestartsTimer() = runViewModelTest {
        val vm = model()
        vm.onForegroundChanged(true)
        vm.onAction(CameraAction.OpenSettings)
        runCurrent()
        advanceTimeBy(30_000)
        runCurrent()
        assertTrue(vm.uiState.value.settingsVisible)
        assertTrue(vm.uiState.value.cameraBarVisible)
        vm.onAction(CameraAction.CloseSettings)
        runCurrent()
        advanceTimeBy(9_999)
        runCurrent()
        assertTrue(vm.uiState.value.cameraBarVisible)
        advanceTimeBy(1)
        runCurrent()
        assertFalse(vm.uiState.value.cameraBarVisible)
    }

    @Test
    fun automaticReloadRepeatsEveryThirtySecondsWithoutChangingSelectionOrMenu() = runViewModelTest {
        val vm = model()
        vm.onAction(CameraAction.ConfirmCamera(3))
        vm.onAction(CameraAction.CameraFocused(5))
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(0, vm.uiState.value.reloadVersion)
        val beforeReload = vm.uiState.value
        advanceTimeBy(1)
        runCurrent()
        assertEquals(beforeReload.copy(reloadVersion = 1), vm.uiState.value)
        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(beforeReload.copy(reloadVersion = 2), vm.uiState.value)
    }

    @Test
    fun menuInteractionAndConfirmingCurrentCameraDoNotPostponeReload() = runViewModelTest {
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(25_000)
        vm.onAction(CameraAction.ShowCameraBar)
        vm.onAction(CameraAction.UserInteraction)
        vm.onAction(CameraAction.ConfirmCamera(0))
        runCurrent()
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(1, vm.uiState.value.reloadVersion)
        assertTrue(vm.uiState.value.cameraBarVisible)
    }

    @Test
    fun switchingCameraAndManualRetryStartFreshReloadIntervals() = runViewModelTest {
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(25_000)
        vm.onAction(CameraAction.ShowCameraBar)
        vm.onAction(CameraAction.ConfirmCamera(2))
        runCurrent()
        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(2, vm.uiState.value.activeIndex)
        assertEquals(0, vm.uiState.value.reloadVersion)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, vm.uiState.value.reloadVersion)

        advanceTimeBy(25_000)
        vm.onAction(CameraAction.ReloadCamera)
        runCurrent()
        assertEquals(2, vm.uiState.value.reloadVersion)
        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(2, vm.uiState.value.reloadVersion)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(3, vm.uiState.value.reloadVersion)
    }

    @Test
    fun onlyActiveUrlChangesRestartTheReloadInterval() = runViewModelTest {
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(25_000)
        repository.saveCameras(cameras.mapIndexed { index, camera ->
            when (index) {
                0 -> camera.copy(name = "Updated name")
                1 -> camera.copy(url = "https://example.invalid/other")
                else -> camera
            }
        })
        runCurrent()
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(1, vm.uiState.value.reloadVersion)

        advanceTimeBy(25_000)
        repository.saveCameras(repository.cameras.value.mapIndexed { index, camera ->
            if (index == 0) camera.copy(url = "https://example.invalid/current") else camera
        })
        runCurrent()
        assertEquals(2, vm.uiState.value.reloadVersion)
        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(2, vm.uiState.value.reloadVersion)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(3, vm.uiState.value.reloadVersion)
    }

    @Test
    fun backgroundAndSettingsPauseReloadAndResumeWithFullIntervals() = runViewModelTest {
        val vm = model()
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(0, vm.uiState.value.reloadVersion)
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(25_000)
        vm.onForegroundChanged(false)
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(0, vm.uiState.value.reloadVersion)

        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(0, vm.uiState.value.reloadVersion)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, vm.uiState.value.reloadVersion)

        advanceTimeBy(25_000)
        vm.onAction(CameraAction.OpenSettings)
        runCurrent()
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(1, vm.uiState.value.reloadVersion)
        vm.onAction(CameraAction.CloseSettings)
        runCurrent()
        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(1, vm.uiState.value.reloadVersion)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, vm.uiState.value.reloadVersion)
    }

    @Test
    fun reloadWaitsForCameraDataAndSkipsBlankUrls() = runViewModelTest {
        repository.cameras.value = emptyList()
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(0, vm.uiState.value.reloadVersion)
        repository.saveCameras(cameras)
        runCurrent()
        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(1, vm.uiState.value.reloadVersion)

        repository.saveCameras(cameras.map { it.copy(url = "") })
        runCurrent()
        assertEquals(2, vm.uiState.value.reloadVersion)
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(2, vm.uiState.value.reloadVersion)
    }

    @Test
    fun clearingViewModelCancelsAutomaticReload() = runViewModelTest {
        val vm = model()
        vm.onForegroundChanged(true)
        runCurrent()
        advanceTimeBy(25_000)
        stores.forEach { it.clear() }
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(0, vm.uiState.value.reloadVersion)
    }

}
