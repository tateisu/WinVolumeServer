package jp.juggler.volumeclient

import android.view.Window
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.material.TopAppBar
import androidx.compose.material.TriStateCheckbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import jp.juggler.volumeclient.MainActivityViewModelImpl.Companion.seekBarPositionToVolumeDb
import jp.juggler.volumeclient.ui.theme.TestJetpackComposeTheme
import jp.juggler.volumeclient.utils.LogTag
import jp.juggler.volumeclient.utils.SpacerH
import jp.juggler.volumeclient.utils.resources
import jp.juggler.volumeclient.utils.setIfChanged
import jp.juggler.volumeclient.utils.setSystemUiColor
import jp.wasabeef.gap.Gap

@Suppress("unused")
private val log = LogTag("MainActivityContent")

// IDE上でプレビューを表示する
@Preview
@Composable
fun PreviewMainActivityContent() =
    MainActivityContent(null, MainActivityViewModelStub)

// メイン画面のUI
@Composable
fun MainActivityContent(
    window: Window?,
    viewModel: MainActivityViewModel
) {
    val darkTheme by viewModel.darkTheme.observeAsState()
    TestJetpackComposeTheme(darkTheme = darkTheme) {

        window?.setSystemUiColor(MaterialTheme.colors.surface)

        val showTitleBarState = viewModel.showTitleBar.observeAsState()

        Scaffold(
            topBar = {
                if (showTitleBarState.value != false) {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.app_name)) },
                        navigationIcon = {
                            IconButton(onClick = {}) {
                                Icon(
                                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                                    contentDescription = stringResource(R.string.app_name),
                                    tint = Color.Unspecified
                                )
                            }
                        },
                    )
                }
            },
            modifier = Modifier
                .background(MaterialTheme.colors.background),
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                ) {
                    // actual composable state
                    val showConnectionSettings by viewModel.showConnectionSettings.observeAsState()

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showConnectionSettings ?: true,
                            onCheckedChange = { viewModel.showConnectionSettings.value = it },
                            modifier = Modifier.height(40.dp),
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
                        )
                        Gap(4.dp)
                        Text(
                            text = stringResource(id = R.string.show_settings),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colors.onBackground,
                        )
                    }
                    AnimatedVisibility(visible = showConnectionSettings != false) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {

                            val serverAddr by viewModel.serverAddr.observeAsState()
                            val serverPort by viewModel.serverPort.observeAsState()
                            val password by viewModel.password.observeAsState()

                            TextField(
                                value = serverAddr ?: "",
                                label = { Text(stringResource(R.string.server_addr)) },
                                modifier = Modifier.fillMaxWidth(),
                                onValueChange = {
                                    viewModel.serverAddr.setIfChanged(it)
                                    viewModel.postGetCurrentVolume()
                                },
                                colors = textFieldColors(
                                    textColor = MaterialTheme.colors.onBackground,
                                )
                            )

                            Gap(4.dp)

                            TextField(
                                value = serverPort ?: "",
                                label = { Text(stringResource(R.string.server_port)) },
                                modifier = Modifier.fillMaxWidth(),
                                onValueChange = {
                                    viewModel.serverPort.setIfChanged(it)
                                    viewModel.postGetCurrentVolume()
                                },
                                colors = textFieldColors(
                                    textColor = MaterialTheme.colors.onBackground,
                                )
                            )

                            Gap(4.dp)

                            var showPassword by remember { mutableStateOf(false) }
                            TextField(
                                value = password ?: "",
                                visualTransformation = if (showPassword)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showPassword = !showPassword }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Visibility,
                                            contentDescription = stringResource(id = R.string.password_showing_toggle),
                                        )
                                    }
                                },
                                label = { Text(stringResource(R.string.password)) },
                                modifier = Modifier.fillMaxWidth(),
                                onValueChange = {
                                    viewModel.password.setIfChanged(it)
                                    viewModel.postGetCurrentVolume()
                                },
                                colors = textFieldColors(
                                    textColor = MaterialTheme.colors.onBackground,
                                )
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TriStateCheckbox(
                                    state = when (darkTheme) {
                                        null -> ToggleableState.Indeterminate
                                        false -> ToggleableState.Off
                                        else -> ToggleableState.On
                                    },
                                    onClick = {
                                        viewModel.darkTheme.value = when (darkTheme) {
                                            null -> false
                                            false -> true
                                            true -> null
                                        }
                                    },
                                    modifier = Modifier.height(40.dp),
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
                                )
                                Gap(4.dp)
                                Text(
                                    text = stringResource(id = R.string.dark_theme),
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colors.onBackground,
                                )
                            }

                            Gap(4.dp)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = showTitleBarState.value ?: true,
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary),
                                    onCheckedChange = { viewModel.setShowTitleBar(it) }
                                )
                                Gap(4.dp)
                                Text(
                                    text = stringResource(id = R.string.title_bar),
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colors.onBackground,
                                )
                            }
                        }
                    }

                    Gap(8.dp)

                    @Composable
                    fun incButtonStyle() =
                        Modifier.background(color = MaterialTheme.colors.secondary)
                            // IconButtonのサイズ変更はthenを挟む必要がある
                            .then(Modifier.size(40.dp))

                    val textResAndArgs by viewModel.error.observeAsState()
                    Text(
                        text = textResAndArgs?.toString(resources()) ?: "",
                        modifier = Modifier.fillMaxWidth(),
                        color = if (textResAndArgs?.resId == R.string.connected)
                            MaterialTheme.colors.onBackground
                        else
                            MaterialTheme.colors.error,
                    )

                    Gap(8.dp)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val deviceName by viewModel.deviceName.observeAsState()
                        Text(
                            text = deviceName ?: "",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colors.onBackground,
                            textAlign = TextAlign.End
                        )

                        Gap(4.dp)

                        IconButton(
                            onClick = { viewModel.postGetCurrentVolume() },
                            modifier = incButtonStyle(),
                        ) {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.refresh),
                                tint = MaterialTheme.colors.onSecondary
                            )
                        }
                    }

                    val volumeBarPos by viewModel.volumeBarPos.observeAsState()
                    val volumeDb by viewModel.volumeDb.observeAsState()

                    Slider(
                        value = volumeBarPos ?: 0.5f,
                        onValueChange = {
                            viewModel.volumeBarPos.value = it
                        },
                        onValueChangeFinished = {
                            val newDb =
                                seekBarPositionToVolumeDb(viewModel.volumeBarPos.value ?: 0f)
                            val oldDb = viewModel.volumeDb.value
                            if (newDb != oldDb) {
                                viewModel.setVolume(newDb)
                            }
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = volumeDb?.let { "${it}dB" } ?: "",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colors.onBackground,
                            fontSize = 20.sp,
                        )

                        Gap(8.dp)

                        IconButton(
                            onClick = { viewModel.setVolume((volumeDb ?: 0f) - 0.5f) },
                            modifier = incButtonStyle(),
                        ) {
                            Icon(
                                Icons.Outlined.ChevronLeft,
                                contentDescription = stringResource(R.string.decrement),
                                tint = MaterialTheme.colors.onSecondary
                            )
                        }

                        Gap(10.dp)

                        IconButton(
                            onClick = { viewModel.setVolume((volumeDb ?: 0f) + 0.5f) },
                            modifier = incButtonStyle(),
                        ) {
                            Icon(
                                Icons.Outlined.ChevronRight,
                                contentDescription = stringResource(R.string.increment),
                                tint = MaterialTheme.colors.onSecondary
                            )
                        }
                    }

                    Gap(8.dp)

                    Text(
                        text = stringResource(id = R.string.presets_title),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colors.onBackground,
                    )

                    FlowRow(crossAxisSpacing = 4.dp) {
                        val presets by viewModel.presets.observeAsState()

                        @Composable
                        fun createButton(
                            text: String,
                            onClick: () -> Unit,
                            onLongClick: (() -> Unit)? = null
                        ) {
                            val volumeButtonHeight = 40.dp
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colors.secondary)
                                    .height(volumeButtonHeight)
                                    .combinedClickable(
                                        onClick = onClick,
                                        onLongClick = onLongClick,
                                    ),
                            ) {
                                Text(
                                    text = text,
                                    color = MaterialTheme.colors.onSecondary,
                                    fontSize = with(LocalDensity.current) {
                                        (volumeButtonHeight * 0.67f).toSp()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(horizontal = (volumeButtonHeight * 0.3f)),
                                )
                            }
                        }

                        createButton(
                            text = stringResource(id = R.string.plus_punk),
                            onClick = { viewModel.addPreset(viewModel.volumeDb.value ?: 0f) }
                        )
                        presets?.forEach { it ->
                            SpacerH(4.dp)
                            createButton(
                                text = it.toString(),
                                onClick = { viewModel.setVolume(it, callApi = true) },
                                onLongClick = { viewModel.removePreset(it) },
                            )
                        }
                    }

                    Gap(8.dp)

                    Text(
                        text = stringResource(id = R.string.media_control),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colors.onBackground,
                    )

                    FlowRow(crossAxisSpacing = 4.dp) {
                        MediaControl.valuesCache.forEachIndexed() { i, m ->
                            if (i != 0) SpacerH(4.dp)
                            val text =  stringResource(m.nameId)
                            when (val icon =m.icon) {
                                null -> {
                                    val mediaButtonHeight = 40.dp
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colors.secondary)
                                            .height(mediaButtonHeight)
                                            .combinedClickable(
                                                onClick =  { viewModel.mediaControl(m) },
                                            ),
                                    ) {
                                        Text(
                                            text =  text,
                                            color = MaterialTheme.colors.onSecondary,
                                            fontSize = with(LocalDensity.current) {
                                                (mediaButtonHeight * 0.67f).toSp()
                                            },
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .padding(horizontal = (mediaButtonHeight * 0.3f)),
                                        )
                                    }
                                }
                                else -> {
                                    IconButton(
                                        onClick = { viewModel.mediaControl(m) },
                                        modifier = incButtonStyle(),
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = text,
                                            tint = MaterialTheme.colors.onSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
