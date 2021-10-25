package jp.juggler.volumeclient

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.juggler.volumeclient.Utils.clip
import kotlinx.coroutines.channels.sendBlocking
import kotlin.math.abs

class MainActivityViewModel(contextSrc: Context) : ViewModel() {
    companion object {
        const val prefName = "pref"
        const val keyShowConnectionSettings = "showConnectionSettings"
        const val keyServerAddr = "serverAddr"
        const val keyServerPort = "serverPort"
        const val keyPresets = "presets"

        private const val minDb = 30
        const val seekBarMax = minDb * 2

        fun seekBarPositionToVolumeDb(pos: Int) =
            (pos - seekBarMax).toFloat().div(2f).clip((-minDb).toFloat(),0f)

        fun volumeDbToSeekBarPos(db:Float) =
            ((db + minDb.toFloat()) * 2f + 0.5f).toInt().clip(0, seekBarMax)
    }

    @SuppressLint("StaticFieldLeak")
    val context: Context = contextSrc.applicationContext

    private val pref: SharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    val showConnectionSettings = MutableLiveData<Boolean>()
    val serverAddr = MutableLiveData<String>()
    val serverPort = MutableLiveData<String>()
    val volumeBarPos = MutableLiveData<Int>()
    val volumeDb = MutableLiveData<Float>()
    val error = MutableLiveData<String>()
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
        updater.channel.sendBlocking(-1L)
    }

    fun loadOrRestore() {
        showConnectionSettings.value = pref.getBoolean(keyShowConnectionSettings, true)
        serverAddr.value = pref.getString(keyServerAddr, "X.X.X.X")
        serverPort.value = pref.getString(keyServerPort, "2021")
        presets.value = pref.getString(keyPresets, null)
            ?.split("/")
            ?.mapNotNull { it.toFloatOrNull() }
            ?.sorted()
            ?: emptyList()
    }

    fun save() {
        pref.edit()
            .putBoolean(keyShowConnectionSettings,showConnectionSettings.value?: true)
            .putString(keyServerAddr,serverAddr.value)
            .putString(keyServerPort,serverPort.value)
            .putString(keyPresets,presets.value?.joinToString("/"))
            .apply()
    }

    fun updateServer(addr: String, port: String) {
        serverAddr.value = addr
        serverPort.value = port
        postGetCurrentVolume()
    }

    fun postGetCurrentVolume() {
        updater.addr.set(serverAddr.value)
        updater.port.set(serverPort.value?.toIntOrNull() ?: 0)
        updater.willGet.set(true)
        updater.channel.sendBlocking(SystemClock.elapsedRealtime())
    }

    private fun showVolume(newValue: Float) {
        volumeDb.value = newValue
        volumeBarPos.value = volumeDbToSeekBarPos(newValue)
    }

    fun setVolume(volumeDb: Float) {
        showVolume(volumeDb)
        updater.volumeDb.set(volumeDb)
        updater.willSet.set(true)
        updater.channel.sendBlocking(SystemClock.elapsedRealtime())
    }

    fun setVolumeDelta(delta:Int) {
        val newPos = ((volumeBarPos.value?:0)+delta).clip(0, seekBarMax)
        setVolume(seekBarPositionToVolumeDb(newPos))
    }

    fun addPreset(newValue: Float ) {
        val dstList = ArrayList( presets.value ?: emptyList())
        if( dstList.any{ abs(it-newValue)<0.1f}) return
        dstList.add(newValue)
        dstList.sort()
        presets.value = dstList
        save()
    }

    fun removePreset(value: Float) {
        val dstList = ArrayList( presets.value ?: emptyList())
            .filter {  abs(it-value)>0.1f }
        presets.value = dstList
        save()
    }
}