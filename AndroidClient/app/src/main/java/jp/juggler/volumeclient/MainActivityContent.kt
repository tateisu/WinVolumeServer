package jp.juggler.volumeclient

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.google.accompanist.flowlayout.FlowRow
import jp.juggler.volumeclient.MainActivityViewModelImpl.Companion.seekBarPositionToVolumeDb
import jp.juggler.volumeclient.ui.theme.TestJetpackComposeTheme

private val log = LogTag("MainActivityContent")

@ExperimentalFoundationApi
@Preview
@Composable
fun PreviewMainActivityContent() =
    MainActivityContent(MainActivityViewModelStub)

fun <T> MutableLiveData<T>.setIfChanged(newValue: T) {
    if (newValue != this.value) {
        this.value = newValue
    }
}

@ExperimentalFoundationApi
@Composable
fun MainActivityContent(viewModel: MainActivityViewModel) {
    TestJetpackComposeTheme {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.app_name)) }
                )
            }
        ) {
            // actual composable state
            var scrollOffset by remember { mutableStateOf(0f) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 12.dp)
                    .scrollable(
                        orientation = Orientation.Vertical,
                        // Scrollable state: describes how to consume
                        // scrolling delta and update offset
                        state = rememberScrollableState { delta ->
                            delta.also { scrollOffset += delta }
                            scrollOffset += delta
                            delta
                        }
                    )
                    .background(MaterialTheme.colors.background),
                // android:fillViewport="true"
                // android:scrollbarStyle="outsideOverlay"
            ) {

                // actual composable state
                val showConnectionSettings by viewModel.showConnectionSettings.observeAsState()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = showConnectionSettings ?: true,
                        onCheckedChange = { viewModel.showConnectionSettings.value = it },
                        modifier = Modifier.height(48.dp)
                    )
                    SpacerH(4.dp)
                    Text(
                        text = stringResource(id = R.string.show_connection_settings),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colors.onBackground,
                    )
                }
                // AnimatedVisibility を使いたいがまだexperimental
                if (showConnectionSettings != false) {
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

                        TextField(
                            value = password ?: "",
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
                    text = textResAndArgs?.let { stringResource(it.resId, *(it.args)) } ?: "",
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

                    SpacerH(4.dp)

                    IconButton(
                        onClick = { viewModel.setVolume((volumeDb ?: 0f) - 0.5f) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(color = MaterialTheme.colors.secondary),
                    ) {
                        Icon(
                            Icons.Outlined.ArrowBack,
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
                            Icons.Outlined.ArrowForward,
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

                FlowRow {
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
