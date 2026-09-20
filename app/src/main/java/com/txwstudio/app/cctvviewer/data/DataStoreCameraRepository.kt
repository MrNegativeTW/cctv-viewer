package com.txwstudio.app.cctvviewer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

// 每個程序只建立一個 DataStore，避免重新建立 Activity 時開啟相同檔案。
val Context.cameraDataStore by preferencesDataStore(name = "camera_settings")

class DataStoreCameraRepository(
    private val store: DataStore<Preferences>,
    defaults: List<Camera>,
) : CameraRepository {
    private val initialCameras = defaults.toList().also { require(it.size == CameraSettings.COUNT) }

    override val cameras = store.data.map { preferences ->
        if (preferences[CONFIGURED] != true) initialCameras
        else List(CameraSettings.COUNT) { index ->
            Camera(
                preferences[nameKey(index)] ?: "",
                preferences[urlKey(index)] ?: "",
            )
        }
    }.distinctUntilChanged()

    override suspend fun saveCameras(cameras: List<Camera>) {
        val validated = CameraSettings.validate(cameras)
        store.edit { preferences ->
            validated.forEachIndexed { index, camera ->
                preferences[nameKey(index)] = camera.name
                preferences[urlKey(index)] = camera.url
            }
            preferences[CONFIGURED] = true
        }
    }

    private fun nameKey(index: Int) = stringPreferencesKey("camera_${index}_name")
    private fun urlKey(index: Int) = stringPreferencesKey("camera_${index}_url")

    companion object {
        private val CONFIGURED = booleanPreferencesKey("configured")
    }
}
