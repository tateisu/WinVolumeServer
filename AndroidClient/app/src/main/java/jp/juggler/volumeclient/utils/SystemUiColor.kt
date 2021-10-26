package jp.juggler.volumeclient.utils

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

fun Window.setSystemUiColor(color: Color, forceDark: Boolean = false) {
    // 古い端末ではナビゲーションバーのアイコン色を設定できないため
    // メディアビューア画面ではステータスバーやナビゲーションバーの色を設定しない…
    if (forceDark && Build.VERSION.SDK_INT < 26) return

    if (Build.VERSION.SDK_INT < 30) {
        @Suppress("DEPRECATION")
        clearFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
    }

    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

    var c = when {
        forceDark -> Color.Black
        else -> color
    }
    setStatusBarColorCompat(c)

    c = when {
        forceDark -> Color.Black
        else -> color
    }
    setNavigationBarColorCompat(c)
}

private fun Window.setStatusBarColorCompat(color: Color) {
    @ColorInt val c = color.toArgb()
    val isLightBg = color.luminance() >= 0.5f

    statusBarColor = android.graphics.Color.BLACK or c

    if (Build.VERSION.SDK_INT >= 30) {
        decorView.windowInsetsController?.run {
            val bit = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            setSystemBarsAppearance(if (isLightBg) bit else 0, bit)
        }
    } else if (Build.VERSION.SDK_INT >= 23) {
        @Suppress("DEPRECATION")
        val bit = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        @Suppress("DEPRECATION")
        decorView.systemUiVisibility =
            if (isLightBg) {
                //Dark Text to show up on your light status bar
                decorView.systemUiVisibility or bit
            } else {
                //Light Text to show up on your dark status bar
                decorView.systemUiVisibility and bit.inv()
            }
    }
}

private fun Window.setNavigationBarColorCompat(color: Color) {
    @ColorInt val c = color.toArgb()
    val isLightBg = color.luminance() >= 0.5f

    if (c == 0) {
        // no way to restore to system default, need restart app.
        return
    }

    navigationBarColor = c or android.graphics.Color.BLACK

    if (Build.VERSION.SDK_INT >= 30) {
        decorView.windowInsetsController?.run {
            val bit = WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            setSystemBarsAppearance(if (isLightBg) bit else 0, bit)
        }
    } else if (Build.VERSION.SDK_INT >= 26) {
        @Suppress("DEPRECATION")
        val bit = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        @Suppress("DEPRECATION")
        decorView.systemUiVisibility = when {
            isLightBg -> {
                //Dark Text to show up on your light status bar
                decorView.systemUiVisibility or bit
            }
            else -> {
                //Light Text to show up on your dark status bar
                decorView.systemUiVisibility and bit.inv()
            }
        }
    }
}
