package jp.juggler.volumeclient.utils

import android.util.Base64
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.min

fun Float.clip(low: Float, high: Float) =
    max(low, min(high, this))

fun String.digestSha256(): ByteArray =
    MessageDigest.getInstance("SHA-256")
        .also { it.update(this.toByteArray(Charsets.UTF_8)) }
        .digest()

fun ByteArray.encodeBase64Url(): String =
    Base64.encodeToString(
        this,
        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    )
