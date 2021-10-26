package jp.juggler.volumeclient

import android.view.Window
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import jp.juggler.volumeclient.utils.*

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

        Scaffold(
            topBar = {
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
            },
            modifier = Modifier
                .background(MaterialTheme.colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(all = 12.dp)
            ) {

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
                    SpacerH(4.dp)
                    Text(
                        text = stringResource(id = R.string.dark_theme),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colors.onBackground,
                    )
                }

                // actual composable state
                val showConnectionSettings by viewModel.showConnectionSettings.observeAsState()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showConnectionSettings ?: true,
                        onCheckedChange = { viewModel.showConnectionSettings.value = it },
                        modifier = Modifier.height(40.dp),
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
                    )
                    SpacerH(4.dp)
                    Text(
                        text = stringResource(id = R.string.show_connection_settings),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colors.onBackground,
                    )
                }
                // AnimatedVisibility を使いたいがまだexperimental
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

                        SpacerV(4.dp)

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

                        SpacerV(4.dp)

                        var showPassword by remember { mutableStateOf(false) }
                        TextField(
                            value = password ?: "",
                            visualTransformation = if (showPassword)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        log.i("passwordVisibility toggle")
                                        showPassword = !showPassword
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Visibility,
                                        contentDescription = stringResource(id = R.string.show_hide_password),
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
                    }
                }

                SpacerV(8.dp)

                val textResAndArgs by viewModel.error.observeAsState()
                Text(
                    text = textResAndArgs?.toString(resources()) ?: "",
                    modifier = Modifier.fillMaxWidth(),
                    color = if (textResAndArgs?.resId == R.string.connected)
                        MaterialTheme.colors.onBackground
                    else
                        MaterialTheme.colors.error,
                )

                SpacerV(8.dp)

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

                    SpacerH(4.dp)

                    IconButton(
                        onClick = { viewModel.postGetCurrentVolume() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(color = MaterialTheme.colors.secondary),
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
                        val newDb = seekBarPositionToVolumeDb(viewModel.volumeBarPos.value ?: 0f)
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

                    SpacerH(8.dp)

                    IconButton(
                        onClick = { viewModel.setVolume((volumeDb ?: 0f) - 0.5f) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(color = MaterialTheme.colors.secondary),
                    ) {
                        Icon(
                            Icons.Outlined.ChevronLeft,
                            contentDescription = stringResource(R.string.decrement),
                            tint = MaterialTheme.colors.onSecondary
                        )
                    }

                    SpacerH(4.dp)

                    IconButton(
                        onClick = { viewModel.setVolume((volumeDb ?: 0f) + 0.5f) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(color = MaterialTheme.colors.secondary),
                    ) {
                        Icon(
                            Icons.Outlined.ChevronRight,
                            contentDescription = stringResource(R.string.increment),
                            tint = MaterialTheme.colors.onSecondary
                        )
                    }
                }

                SpacerV(8.dp)

                Text(
                    text = stringResource(id = R.string.presets_title),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.onBackground,
                )

                FlowRow(
                    crossAxisSpacing = 4.dp
                ) {
                    val presets by viewModel.presets.observeAsState()

                    @Composable
                    fun createButton(
                        text: String,
                        onClick: () -> Unit,
                        onLongClick: (() -> Unit)? = null
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colors.secondary)
                                .height(40.dp)
                                .widthIn(40.dp, 100.dp)
                                .combinedClickable(
                                    onClick = onClick,
                                    onLongClick = onLongClick,
                                ),
                        ) {
                            Text(
                                text = text,
                                color = MaterialTheme.colors.onSecondary,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 12.dp),
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
            }
        }
    }
}

