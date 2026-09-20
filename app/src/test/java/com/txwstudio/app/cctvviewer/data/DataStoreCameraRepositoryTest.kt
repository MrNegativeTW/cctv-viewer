package com.txwstudio.app.cctvviewer.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreCameraRepositoryTest {
    @get:Rule val folder = TemporaryFolder()
    private val defaults = List(8) { Camera("攝影機 ${it + 1}", "https://example.invalid/${it + 1}") }

    @Test
    fun usesDefaultsUntilSavedAndRestoresSavedSettingsAfterReopening() = runBlocking {
        val file = File(folder.root, "cameras.preferences_pb")
        val firstScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val edited = defaults.mapIndexed { index, camera ->
            if (index == 0) Camera("門口 \"測試\" & 中文", "http://192.0.2.1/view?a=1&b=2") else camera
        }
        try {
            val repository = DataStoreCameraRepository(
                PreferenceDataStoreFactory.create(scope = firstScope, produceFile = { file }), defaults)
            assertEquals(defaults, repository.cameras.first())
            repository.saveCameras(edited)
            assertEquals(edited, repository.cameras.first())
            try {
                repository.saveCameras(edited.mapIndexed { index, camera ->
                    if (index == 1) camera.copy(url = "javascript:alert(1)") else camera
                })
                throw AssertionError("Invalid URL was accepted")
            } catch (_: IllegalArgumentException) {
                assertEquals(edited, repository.cameras.first())
            }
        } finally {
            firstScope.coroutineContext[Job]!!.cancelAndJoin()
        }
        val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val repository = DataStoreCameraRepository(
                PreferenceDataStoreFactory.create(scope = secondScope, produceFile = { file }),
                List(8) { Camera("不同初始值", "") },
            )
            assertEquals(edited, repository.cameras.first())
            repository.saveCameras(List(8) { Camera("", "") })
            assertTrue(repository.cameras.first().all { it.url.isEmpty() })
            assertEquals("", repository.cameras.first().first().name)
        } finally {
            secondScope.coroutineContext[Job]!!.cancelAndJoin()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPartialConfiguration() {
        CameraSettings.validate(defaults.take(7))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidPort() {
        CameraSettings.validate(defaults.map { it.copy(url = "http://192.0.2.1:99999/") })
    }
}
