@file:Suppress("MemberVisibilityCanBePrivate")

package jp.juggler.volumeclient.utils

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.security.MessageDigest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.min

// ViewModelのfactoryを毎回書くのが面倒
// あと使わない場合にはViewModelの引数を生成したくない
fun <VM : ViewModel> viewModelFactory(vmClass: Class<VM>, creator: () -> VM) =
    object : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(vmClass)) {
                error("unexpected modelClass. ${modelClass.simpleName}")
            }
            return creator() as T
        }
    }

// ViewModelProvider(…).get を毎回書くのが面倒
inline fun <reified T : ViewModel> provideViewModel(
    owner: ViewModelStoreOwner,
    noinline creator: () -> T
) =
    ViewModelProvider(owner, viewModelFactory(T::class.java, creator)).get(T::class.java)

suspend fun Call.enqueueAndAwait(): Response =
    suspendCancellableCoroutine { cont ->
        this.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                cont.resume(response)
            }
        })
    }

fun Float.clip(min: Float, max: Float): Float {
    return max(min, min(max, this))
}

fun String.digestSha256(): ByteArray =
    MessageDigest.getInstance("SHA-256")
        .also { it.update(this.toByteArray(Charsets.UTF_8)) }
        .digest()

fun ByteArray.encodeBase64Url(): String =
    Base64.encodeToString(
        this,
        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    )
