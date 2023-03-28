package jp.juggler.volumeclient

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import jp.juggler.volumeclient.utils.EmptyScope
import jp.juggler.volumeclient.utils.LogTag
import jp.juggler.volumeclient.utils.StringResAndArgs
import jp.juggler.volumeclient.utils.clip
import jp.juggler.volumeclient.utils.setIfChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val volumeMinDbDefault = 96
private const val volumeMinDbClip = 10

interface MainActivityViewModel {
    // tri-value: boolean or null
    val darkTheme: MutableLiveData<Boolean>
    val showConnectionSettings: MutableLiveData<Boolean>
    val serverAddr: MutableLiveData<String>
    val serverPort: MutableLiveData<String>
    val password: MutableLiveData<String>
    val volumeBarPos: MutableLiveData<Float>
    val volumeDb: MutableLiveData<Float>
    val error: MutableLiveData<StringResAndArgs>
    val presets: MutableLiveData<List<Float>>
    val deviceName: MutableLiveData<String>
    val showTitleBar: MutableLiveData<Boolean>
    val mediaError: LiveEvent<Throwable>
    val volumeMinDb: MutableLiveData<Int>

    val volumeMinDbFixedInt: Int
        get() = (volumeMinDb.value?.takeIf { it >= volumeMinDbClip } ?: volumeMinDbDefault)

    val volumeMinDbFixedFloat: Float
        get() = volumeMinDbFixedInt.toFloat()

    fun addPreset(v: Float)
    fun mediaControl(m: MediaControl)
    fun onVolumeSeekFinished()
    fun postGetCurrentVolume()
    fun removePreset(v: Float)
    fun setShowTitleBar(v: Boolean)
    fun setVolume(v: Float, callApi: Boolean = true)
    fun setVolumeMinDb(text: String)
}

// Jetpack Compose のIDEプレビュー用
object MainActivityViewModelStub : MainActivityViewModel {
    override val darkTheme = MutableLiveData<Boolean>(null)
    override val showConnectionSettings = MutableLiveData(true)
    override val serverAddr = MutableLiveData("X.X.X.X")
    override val serverPort = MutableLiveData("2021")
    override val password = MutableLiveData("123456")
    override val volumeBarPos = MutableLiveData(0.5f)
    override val volumeDb = MutableLiveData(-15f)
    override val error = MutableLiveData(
        StringResAndArgs(R.string.connection_error, arrayOf("aaa"))
    )
    override val presets = MutableLiveData(listOf(-30f, -15f, 0f))
    override val deviceName = MutableLiveData("device name")
    override val showTitleBar = MutableLiveData(true)
    override val volumeMinDb = MutableLiveData(volumeMinDbDefault)
    override val mediaError = LiveEvent<Throwable>()

    override fun postGetCurrentVolume() = Unit
    override fun setVolume(v: Float, callApi: Boolean) = Unit
    override fun addPreset(v: Float) = Unit
    override fun removePreset(v: Float) = Unit
    override fun setShowTitleBar(v: Boolean) = Unit
    override fun mediaControl(m: MediaControl) = Unit
    override fun onVolumeSeekFinished() = Unit
    override fun setVolumeMinDb(text: String) = Unit
}

// 実際の実装
class MainActivityViewModelImpl(
    context: Context
) : ViewModel(), MainActivityViewModel {
    companion object {
        val log = LogTag("MainActivityViewModel")
        const val prefName = "pref"
        const val keyDarkTheme = "darkTheme"
        const val keyShowConnectionSettings = "showConnectionSettings"
        const val keyServerAddr = "serverAddr"
        const val keyServerPort = "serverPort"
        const val keyPassword = "password"
        const val keyPresets = "presets"
        const val keyShowTitleBar = "showTitleBar"
        const val keyVolumeMinDb = "volumeMinDb"
    }

    private val pref: SharedPreferences = context.getSharedPreferences(
        prefName,
        Context.MODE_PRIVATE
    )

    override val darkTheme = MutableLiveData<Boolean>()
    override val showConnectionSettings = MutableLiveData<Boolean>()
    override val serverAddr = MutableLiveData<String>()
    override val serverPort = MutableLiveData<String>()
    override val password = MutableLiveData<String>()
    override val volumeBarPos = MutableLiveData<Float>()
    override val volumeDb = MutableLiveData<Float>()
    override val error = MutableLiveData<StringResAndArgs>()
    override val presets = MutableLiveData<List<Float>>()
    override val deviceName = MutableLiveData<String>()
    override val showTitleBar = MutableLiveData(true)
    override val volumeMinDb = MutableLiveData(volumeMinDbDefault)
    override val mediaError = LiveEvent<Throwable>()
    private var isCleared = false

    private val updater = SequentialUpdater(
        onMediaError = { mediaError.value = it },
        onError = {
            if (!this.isCleared) {
                error.value = it
            }
        },
        handleGetResult = { deviceName, volumeDb ->
            if (!this.isCleared) {
                this.deviceName.value = deviceName
                setVolume(volumeDb, callApi = false)
                save()
            }
        }
    )

    override fun onCleared() {
        isCleared = true
        super.onCleared()
        EmptyScope.launch { updater.send(-1L) }
    }

    fun loadOrRestore() {
        showConnectionSettings.value = pref.getBoolean(keyShowConnectionSettings, true)
        darkTheme.value = when (pref.getInt(keyDarkTheme, -1)) {
            -1 -> null
            0 -> false
            else -> true
        }
        serverAddr.value = pref.getString(keyServerAddr, "X.X.X.X")
        serverPort.value = pref.getString(keyServerPort, "2021")
        password.value = pref.getString(keyPassword, "")
        presets.value = pref.getString(keyPresets, null)
            ?.split("/")
            ?.mapNotNull { it.toFloatOrNull() }
            ?.sorted()
            ?: emptyList()
        showTitleBar.value = pref.getBoolean(keyShowTitleBar, true)
        volumeMinDb.value = pref.getInt(keyVolumeMinDb, volumeMinDbDefault)
    }

    fun save() {
        pref.edit()
            .putBoolean(keyShowConnectionSettings, showConnectionSettings.value ?: true)
            .putInt(
                keyDarkTheme, when (darkTheme.value) {
                    null -> -1
                    false -> 0
                    true -> 1
                }
            )
            .putString(keyServerAddr, serverAddr.value)
            .putString(keyServerPort, serverPort.value)
            .putString(keyPassword, password.value)
            .putString(keyPresets, presets.value?.joinToString("/"))
            .putBoolean(keyShowTitleBar, showTitleBar.value ?: true)
            .apply()
    }

    override fun postGetCurrentVolume() {
        viewModelScope.launch {
            updater.postGet(
                serverAddr.value,
                serverPort.value?.toIntOrNull() ?: 0,
                password.value
            )
        }
    }

    // 0.5dB単位に丸める
    // 範囲内にクリップする
    private fun Float.roundDb() =
        (this * 2f).toInt().toFloat().times(0.5f).clip(-volumeMinDbFixedFloat, 0f)

    private fun seekBarPositionToVolumeDb(pos: Float) =
        (pos - 1f).times(volumeMinDbFixedFloat).roundDb()

    private fun volumeDbToSeekBarPos(db: Float) =
        db.roundDb().div(volumeMinDbFixedFloat) + 1f

    override fun onVolumeSeekFinished() {
        val newDb = seekBarPositionToVolumeDb(volumeBarPos.value ?: 0f)
        val oldDb = volumeDb.value
        if (newDb != oldDb) setVolume(newDb)
    }

    override fun setVolumeMinDb(text: String) {
        val newMinDb = text.toIntOrNull()?.takeIf { it >= volumeMinDbClip } ?: return
        volumeMinDb.setIfChanged(newMinDb)
        volumeDb.value?.let { setVolume(it) }
    }

    override fun setVolume(v: Float, callApi: Boolean) {
        val newDb = v.roundDb()
        if (newDb != volumeDb.value) {
            volumeDb.value = newDb
        }
        val newPos = volumeDbToSeekBarPos(newDb)
        if (newPos != volumeBarPos.value) {
            log.i("volumeBarPos=$newPos")
            volumeBarPos.value = newPos
        }
        if (callApi) {
            viewModelScope.launch {
                updater.postVolume(newDb)
            }
        }
    }

    override fun addPreset(v: Float) {
        val dstList = ArrayList(presets.value ?: emptyList())
        if (dstList.any { abs(it - v) < 0.1f }) return
        dstList.add(v)
        dstList.sort()
        presets.value = dstList
        save()
    }

    override fun removePreset(v: Float) {
        val dstList = ArrayList(presets.value ?: emptyList())
            .filter { abs(it - v) > 0.1f }
        presets.value = dstList
        save()
    }

    override fun setShowTitleBar(v: Boolean) {
        showTitleBar.value = v
        save()
    }

    override fun mediaControl(m: MediaControl) {
        viewModelScope.launch {
            updater.postMedia(m)
        }
    }

}
