package jp.juggler.volumeclient

import android.os.SystemClock
import jp.juggler.volumeclient.Utils.digestSha256
import jp.juggler.volumeclient.Utils.encodeBase64Url
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
    private var port = AtomicInteger(0)
    private var password = AtomicReference<String>(null)
    private val volumeDb = AtomicReference<Float>(null)
    private val willGet = AtomicBoolean(false)
    private val willSet = AtomicBoolean(false)

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun update(newVolume: Float? = null) {
        val addr = this.addr.get()
        val port = this.port.get()
        if (addr == null || port <= 0) {
            error("missing addr or port.")
        }
        val request = if (newVolume != null) {
            val url = "http://${addr}:${port}/volume?v=${newVolume}"
            Request.Builder()
                .url(url)
                .also { embedPassword(it) }
                .method("POST", "".toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .build()
        } else {
            val url = "http://${addr}:${port}/volume"
            Request.Builder()
                .url(url)
                .also { embedPassword(it) }
                .build()
        }

        val response = client.newCall(request).enqueueAndAwait()
        val bodyString = response.body?.string()
        val errorText =
            "$bodyString\n${response.code} ${response.message}\n${request.method} ${request.url}"
        if (!response.isSuccessful || bodyString == null) {
            error(errorText)
        }
        val root = try {
            JSONObject(bodyString)
        } catch (ex: Throwable) {
            error("json parse error.\n$errorText")
        }
        val deviceName = root.optString("device", "")
        val volumeDb = root.optDouble("volume", Double.NaN).toFloat()
        if (!volumeDb.isFinite()) error("can't get volume value. $bodyString")
        withContext(Dispatchers.Main) {
            onError("")
            handleGetResult(deviceName, volumeDb)
        }
    }

    private fun embedPassword(rb: Request.Builder) {
        val password = this.password.get()
        if (password != null && password.isNotEmpty()) {
            val timeLong = System.currentTimeMillis()
            val digest = "${timeLong}:${password}".digestSha256().encodeBase64Url()
            rb.addHeader("X-Password-Time", timeLong.toString())
            rb.addHeader("X-Password-Digest", digest)
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

    suspend fun postGet(addr: String?, port: Int, password: String?) {
        this.addr.set(addr)
        this.port.set(port)
        this.password.set(password)
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
                        break
                    }
                    if (willSet.compareAndSet(true, false)) {
                        update(volumeDb.get())
                    } else {
                        update()
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
