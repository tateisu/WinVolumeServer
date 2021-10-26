package jp.juggler.volumeclient.utils

import android.util.Log

@Suppress("unused")
class LogTag(private val prefix: String) {
    companion object {
        const val tag = "VolumeClient"
    }

    fun e(msg: String) = Log.e(tag, "$prefix: $msg")
    fun w(msg: String) = Log.w(tag, "$prefix: $msg")
    fun i(msg: String) = Log.i(tag, "$prefix: $msg")
    fun d(msg: String) = Log.d(tag, "$prefix: $msg")
    fun v(msg: String) = Log.v(tag, "$prefix: $msg")

    fun e(ex: Throwable, msg: String = "error.") = Log.e(tag, "$prefix: $msg", ex)
    fun w(ex: Throwable, msg: String = "error.") = Log.w(tag, "$prefix: $msg", ex)
}
