package jp.juggler.volumeclient

import android.content.res.Resources
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp

@Composable
fun SpacerH(width: Dp) = Spacer(modifier = Modifier.width(width))

@Composable
fun SpacerV(height: Dp) = Spacer(modifier = Modifier.height(height))

@Composable
@ReadOnlyComposable
fun resources(): Resources = LocalContext.current.resources
