package jp.juggler.volumeclient.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val LightColorPalette = lightColors(
    background = Color.White,
    onBackground = Color.Black,

    primary = colorPrimaryLight,
    primaryVariant = colorPrimaryVariantLight,
    onPrimary = Color.White,

    secondary = buttonBgLight,
    onSecondary = Color.Black,
)

private val DarkColorPalette = darkColors(
    background = Color.Black,
    onBackground = Color.White,

    primary = colorPrimaryDark,
    primaryVariant = colorPrimaryVariantDark,
    onPrimary = Color.White,

    secondary = buttonBgDark,
    onSecondary = Color.White,
)


@Composable
fun TestJetpackComposeTheme(
    darkTheme: Boolean? = null,
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme?: isSystemInDarkTheme()) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
