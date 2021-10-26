package jp.juggler.volumeclient

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.juggler.volumeclient.ui.theme.TestJetpackComposeTheme

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun TestBoxWithConstraints() {
    TestJetpackComposeTheme {
        BoxWithConstraints {
            val rectangleHeight = 100.dp
            if (maxHeight < rectangleHeight * 2) {
                Box(Modifier.size(50.dp, rectangleHeight).background(Color.Blue))
            } else {
                Column {
                    Box(Modifier.size(50.dp, rectangleHeight).background(Color.Blue))
                    Box(Modifier.size(50.dp, rectangleHeight).background(Color.Gray))
                }
            }
        }
    }
}

