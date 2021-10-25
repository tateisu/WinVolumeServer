package jp.juggler.volumeclient

import android.content.Context
import android.graphics.Color
import android.util.Base64
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.security.MessageDigest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.min

@Suppress("MemberVisibilityCanBePrivate")
object Utils {
    private val log = LogTag("Utils")

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

    inline fun <reified T : View> T?.vg(isVisible: Boolean): T? =
        this?.also { visibility = if (isVisible) View.VISIBLE else View.GONE }?.takeIf { isVisible }

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

    fun Int.clip(min: Int, max: Int): Int {
        return max(min, min(max, this))
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

    fun Context.getResColor(@ColorRes resId: Int) =
        ContextCompat.getColor(this, resId)

    fun Context.attrToResId(@AttrRes attrId: Int): Int {
        val outValue = TypedValue()
        theme.resolveAttribute(attrId, outValue, false)
        if (outValue.type == TypedValue.TYPE_REFERENCE) {
            return outValue.data
        }
        error("attrToResId. can't resolve. attrId=$attrId")
    }

    fun Context.getAttrColor(@AttrRes attrId: Int): Int {
        return try {
            getResColor(attrToResId(attrId))
        } catch (ex: Throwable) {
            log.w(ex, "getAttrColor failed.")
            Color.MAGENTA;
        }
    }
}

object EmptyScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext + Dispatchers.Main
}
