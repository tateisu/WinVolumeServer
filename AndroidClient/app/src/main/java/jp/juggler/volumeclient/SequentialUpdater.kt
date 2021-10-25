package jp.juggler.volumeclient

import android.os.SystemClock
import jp.juggler.volumeclient.Utils.enqueueAndAwait
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
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
    companion object {
        private val log = LogTag("SequentialUpdater")
    }

    private val client = OkHttpClient()
    private val channel = Channel<Long>(capacity = 3)
    private val addr = AtomicReference<String>(null)
    private var port = AtomicInteger(0);
    private val volumeDb = AtomicReference<Float>(null)
    private val willGet = AtomicBoolean(false)
    private val willSet = AtomicBoolean(false)

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

    private suspend fun send(value: Long = SystemClock.elapsedRealtime()) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                channel.send(value)
            } catch (ex: Throwable) {
                log.w(ex)
            }
        }
    }

    suspend fun postGet(addr: String?, port: Int) {
        this.addr.set(addr)
        this.port.set(port)
        willGet.set(true)
        send()
    }

    suspend fun postVolume(volumeDb: Float) {
        this.volumeDb.set(volumeDb)
        willSet.set(true)
        send()
    }

    suspend fun sendExit() {
        send(-1L)
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
                    log.w(ex)
                    if (ex is CancellationException || ex is ClosedReceiveChannelException) break
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
