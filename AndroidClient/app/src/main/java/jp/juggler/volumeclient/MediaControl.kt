package jp.juggler.volumeclient

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.ui.graphics.vector.ImageVector

enum class MediaControl(
    val keySpec: String,
    @StringRes val nameId: Int,
    val icon: ImageVector? = null,
) {
    Stop("stop", R.string.media_stop, Icons.Outlined.Stop),
    PlayPause("playPause", R.string.media_play_pause, Icons.Outlined.PlayArrow),
    PreviousTrack("previousTrack", R.string.media_previous_track, Icons.Outlined.SkipPrevious),
    NextTrack("nextTrack", R.string.media_next_track, Icons.Outlined.SkipNext),
    KillAmazonMusic("killAmazonMusic",R.string.kill_amazon_music)

    ;

    companion object {
        val valuesCache by lazy { values() }
    }
}
