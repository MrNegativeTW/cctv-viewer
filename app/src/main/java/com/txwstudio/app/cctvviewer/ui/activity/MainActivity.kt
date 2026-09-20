package com.txwstudio.app.cctvviewer.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.txwstudio.app.cctvviewer.data.BuildConfigCameraDefaults
import com.txwstudio.app.cctvviewer.data.DataStoreCameraRepository
import com.txwstudio.app.cctvviewer.data.cameraDataStore
import com.txwstudio.app.cctvviewer.ui.screen.CameraRoute
import com.txwstudio.app.cctvviewer.ui.theme.CCTVViewerTheme
import com.txwstudio.app.cctvviewer.ui.viewmodel.CameraViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val viewModel = ViewModelProvider(
            this,
            CameraViewModel.factory(DataStoreCameraRepository(applicationContext.cameraDataStore, BuildConfigCameraDefaults.load())),
        )[CameraViewModel::class.java]
        setContent {
            CCTVViewerTheme { CameraRoute(viewModel) }
        }
    }
}
