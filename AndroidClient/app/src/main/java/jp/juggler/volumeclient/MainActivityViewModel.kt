package jp.juggler.volumeclient

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.juggler.volumeclient.Utils.clip
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivityViewModel(contextSrc: Context) : ViewModel() {
    companion object {
        // val log = LogTag("MainActivityViewModel")
        const val prefName = "pref"
        const val keyShowConnectionSettings = "showConnectionSettings"
        const val keyServerAddr = "serverAddr"
        const val keyServerPort = "serverPort"
        const val keyPassword = "password"
        const val keyPresets = "presets"

        private const val minDb = 30
        const val seekBarMax = minDb * 2

        fun seekBarPositionToVolumeDb(pos: Int) =
            (pos - seekBarMax).toFloat().div(2f).clip((-minDb).toFloat(), 0f)

        fun volumeDbToSeekBarPos(db: Float) =
            ((db + minDb.toFloat()) * 2f + 0.5f).toInt().clip(0, seekBarMax)
    }

    @SuppressLint("StaticFieldLeak")
    val context: Context = contextSrc.applicationContext

    private val pref: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    val showConnectionSettings = MutableLiveData<Boolean>()
    val serverAddr = MutableLiveData<String>()
    val serverPort = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val volumeBarPos = MutableLiveData<Int>()
    val volumeDb = MutableLiveData<Float>()
    val error = MutableLiveData<StringResAndArgs>()
    val presets = MutableLiveData<List<Float>>()
    val deviceName = MutableLiveData<String>()

    private var isCleared = false

    private val updater = SequentialUpdater(
        onError = {
            if (!this.isCleared) {
                error.value = it
            }
        },
        handleGetResult = { deviceName, volumeDb ->
            if (!this.isCleared) {
                this.deviceName.value = deviceName
                showVolume(volumeDb)
                save()
            }
        }
    )

    override fun onCleared() {
        isCleared = true
        super.onCleared()
        EmptyScope.launch {
            updater.sendExit()
        }
    }

    fun loadOrRestore() {
        showConnectionSettings.value = pref.getBoolean(keyShowConnectionSettings, true)
        serverAddr.value = pref.getString(keyServerAddr, "X.X.X.X")
        serverPort.value = pref.getString(keyServerPort, "2021")
        password.value = pref.getString(keyPassword, "")
        presets.value = pref.getString(keyPresets, null)
            ?.split("/")
            ?.mapNotNull { it.toFloatOrNull() }
            ?.sorted()
            ?: emptyList()
    }

    fun save() {
        pref.edit()
            .putBoolean(keyShowConnectionSettings, showConnectionSettings.value ?: true)
            .putString(keyServerAddr, serverAddr.value)
            .putString(keyServerPort, serverPort.value)
            .putString(keyPassword, password.value)
            .putString(keyPresets, presets.value?.joinToString("/"))
            .apply()
    }

    fun setServerConfig(addr: String, port: String, password: String) {
        if (addr != this.serverAddr.value) this.serverAddr.value = addr
        if (port != this.serverPort.value) this.serverPort.value = port
        if (password != this.password.value) this.password.value = password
        postGetCurrentVolume()
    }

    fun postGetCurrentVolume() {
        viewModelScope.launch {
            updater.postGet(
                serverAddr.value,
                serverPort.value?.toIntOrNull() ?: 0,
                password.value
            )
        }
    }

    private fun showVolume(newValue: Float) {
        volumeDb.value = newValue
        volumeBarPos.value = volumeDbToSeekBarPos(newValue)
    }

    fun setVolume(volumeDb: Float) {
        showVolume(volumeDb)
        viewModelScope.launch {
            updater.postVolume(volumeDb)
        }
    }

    fun setVolumeDelta(delta: Int) {
        val newPos = ((volumeBarPos.value ?: 0) + delta).clip(0, seekBarMax)
        setVolume(seekBarPositionToVolumeDb(newPos))
    }

    fun addPreset(newValue: Float) {
        val dstList = ArrayList(presets.value ?: emptyList())
        if (dstList.any { abs(it - newValue) < 0.1f }) return
        dstList.add(newValue)
        dstList.sort()
        presets.value = dstList
        save()
    }

    fun removePreset(value: Float) {
        val dstList = ArrayList(presets.value ?: emptyList())
            .filter { abs(it - value) > 0.1f }
        presets.value = dstList
        save()
    }
}
