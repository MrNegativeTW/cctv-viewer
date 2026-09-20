package com.txwstudio.app.cctvviewer.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.txwstudio.app.cctvviewer.ui.theme.CCTVViewerTheme
import com.txwstudio.app.cctvviewer.R

@Composable
fun CameraButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    Button(
        onClick = onClick,
        modifier = modifier.semantics { this.selected = selected },
        shape = ButtonDefaults.shape(shape = shape),
        colors = ButtonDefaults.colors(
            containerColor = if (selected) colors.primaryContainer else colors.surfaceVariant,
            contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
            focusedContainerColor = colors.primary,
            focusedContentColor = colors.onPrimary,
        ),
        border = ButtonDefaults.border(
            border = if (selected) Border(BorderStroke(2.dp, colors.primary), shape = shape) else Border.None,
            focusedBorder = Border(BorderStroke(2.dp, colors.onPrimaryContainer), shape = shape),
        ),
        scale = ButtonDefaults.scale(focusedScale = 1.04f),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        content = content,
    )
}

@Preview(name = "攝影機按鈕・一般與已選取", locale = "zh-rTW")
@Composable
private fun CameraButtonPreview() {
    CCTVViewerTheme {
        Row(
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CameraButton(onClick = {}, modifier = Modifier.width(128.dp).height(64.dp)) {
                Text(stringResource(R.string.camera_number, 1))
            }
            CameraButton(onClick = {}, selected = true, modifier = Modifier.width(128.dp).height(64.dp)) {
                Text(stringResource(R.string.camera_number, 2))
            }
        }
    }
}
