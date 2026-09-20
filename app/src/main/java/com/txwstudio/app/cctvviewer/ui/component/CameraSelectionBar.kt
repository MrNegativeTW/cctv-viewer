package com.txwstudio.app.cctvviewer.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.txwstudio.app.cctvviewer.R
import com.txwstudio.app.cctvviewer.data.Camera
import com.txwstudio.app.cctvviewer.ui.preview.CameraPreviewData
import com.txwstudio.app.cctvviewer.ui.theme.CCTVViewerTheme

@Composable
fun CameraSelectionBar(
    cameras: List<Camera>,
    activeIndex: Int,
    focusedIndex: Int,
    transition: MutableTransitionState<Boolean>,
    onCameraFocused: (Int) -> Unit,
    onCameraConfirmed: (Int) -> Unit,
) {
    val focusRequesters = remember(cameras.size) { List(cameras.size) { FocusRequester() } }
    val scrollRequesters = remember(cameras.size) { List(cameras.size) { BringIntoViewRequester() } }
    val scrollState = rememberScrollState()
    val ready = transition.isIdle && transition.currentState

    AnimatedVisibility(
        visibleState = transition,
        enter = expandVertically(
            animationSpec = tween(ANIMATION_MS, easing = FastOutSlowInEasing),
            expandFrom = Alignment.Bottom,
        ) + slideInVertically(
            animationSpec = tween(ANIMATION_MS, easing = FastOutSlowInEasing),
            initialOffsetY = { it },
        ) + fadeIn(animationSpec = tween(ANIMATION_MS)),
        exit = shrinkVertically(
            animationSpec = tween(ANIMATION_MS, easing = FastOutSlowInEasing),
            shrinkTowards = Alignment.Bottom,
        ) + slideOutVertically(
            animationSpec = tween(ANIMATION_MS, easing = FastOutSlowInEasing),
            targetOffsetY = { it },
        ) + fadeOut(animationSpec = tween(ANIMATION_MS)),
        label = "cameraBarVisibility",
    ) {
        Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState)
                    .padding(horizontal = 48.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                cameras.forEachIndexed { index, camera ->
                    CameraButton(
                        onClick = { if (ready) onCameraConfirmed(index) },
                        selected = index == activeIndex,
                        modifier = Modifier.width(128.dp).height(64.dp)
                            .bringIntoViewRequester(scrollRequesters[index])
                            .focusRequester(focusRequesters[index])
                            .focusProperties { canFocus = ready }
                            .onFocusChanged { if (it.isFocused) onCameraFocused(index) },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.camera_number, index + 1),
                                style = MaterialTheme.typography.labelSmall,
                            )
                            Text(
                                text = camera.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
        // 只在清單已展開且節點存在時還原焦點，避免動畫與捲動互相干擾。
        LaunchedEffect(ready, focusedIndex) {
            if (ready) {
                focusRequesters[focusedIndex].requestFocus()
                scrollRequesters[focusedIndex].bringIntoView()
            }
        }
    }
}

private const val ANIMATION_MS = 260

@Preview(name = "電視・攝影機選單", widthDp = 960, heightDp = 116, locale = "zh-rTW")
@Composable
private fun CameraSelectionBarPreview() {
    CCTVViewerTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            CameraSelectionBar(
                cameras = CameraPreviewData.cameras,
                activeIndex = 0,
                focusedIndex = 1,
                transition = remember { MutableTransitionState(true) },
                onCameraFocused = {},
                onCameraConfirmed = {},
            )
        }
    }
}
