package jp.juggler.volumeclient

import jp.juggler.volumeclient.Utils.enqueueAndAwait
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class SequentialUpdater(
    val onError: (String) -> Unit,
    val handleGetResult: (String, Float) -> Unit
) {
    private val client = OkHttpClient()
    val channel = Channel<Long>(capacity = 3)
    val addr = AtomicReference<String>(null)
    var port = AtomicInteger(0);
    val volumeDb = AtomicReference<Float>(null)
    val willGet = AtomicBoolean(false)
    val willSet = AtomicBoolean(false)

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getCurrentVolume() {
        val addr = this.addr.get()
        val port = this.port.get()
        if (addr == null || port <= 0) {
            error("missing addr or port.")
        }
        val url = "http://${addr}:${port}/volume"
        val request = Request.Builder()
            .url(url)
            .build()
        val response = client.newCall(request).enqueueAndAwait()
        val bodyString = response.body?.string()
        if (!response.isSuccessful || bodyString == null) {
            error("$bodyString / ${response.code} ${response.message} $bodyString / ${request.method} $url")
        }
        val root = JSONObject(bodyString)
        val deviceName = root.optString("device", "")
        val volumeDb = root.optDouble("volume", Double.NaN).toFloat()
        if (!volumeDb.isFinite()) error("can't get volume value. $bodyString")
        withContext(Dispatchers.Main) {
            handleGetResult(deviceName, volumeDb)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun setCurrentVolume() {
        val addr = this.addr.get()
        val port = this.port.get()
        val volume = this.volumeDb.get()
        if (addr == null || port <= 0 || volume == null) {
            error("missing addr or port.")
        }
        val url = "http://${addr}:${port}/volume?v=${volume}"
        val request = Request.Builder()
            .url(url)
            .method("POST", "".toRequestBody("application/x-www-form-urlencoded".toMediaType()))
            .build()
        val response = client.newCall(request).enqueueAndAwait()
        val bodyString = response.body?.string()
        if (!response.isSuccessful || bodyString == null) {
            error("$bodyString / ${response.code} ${response.message} $bodyString / ${request.method} $url")
        }
        val root = JSONObject(bodyString)
        val deviceName = root.optString("device", "")
        val volumeDb = root.optDouble("volume", Double.NaN).toFloat()
        if (!volumeDb.isFinite()) error("can't get volume value. $bodyString")
        withContext(Dispatchers.Main) {
            handleGetResult(deviceName, volumeDb)
        }
    }

    init {
        EmptyScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val t = channel.receive()
                    if (t < 0L) {
                        channel.close()
                        break;
                    }
                    if (willGet.compareAndSet(true, false)) {
                        getCurrentVolume()
                    }
                    if (willSet.compareAndSet(true, false)) {
                        setCurrentVolume()
                    }
                } catch (ex: Throwable) {
                    if (ex is CancellationException) break
                    val text = if (ex is IllegalStateException) {
                        ex.message ?: "(no message)"
                    } else {
                        "${ex.javaClass.simpleName} ${ex.message}"
                    }
                    withContext(Dispatchers.Main) {
                        onError(text)
                    }
                }
            }
        }
    }
}
