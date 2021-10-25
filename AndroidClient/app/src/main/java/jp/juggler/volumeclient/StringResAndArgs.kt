package jp.juggler.volumeclient

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes

@Suppress("MemberVisibilityCanBePrivate")
class StringResAndArgs(@StringRes val resId: Int, val args: Array<out Any?>) {
    companion object {
        fun create(@StringRes resId: Int, vararg args: Any?) = StringResAndArgs(resId, args)
    }

    constructor(@StringRes resId: Int) : this(resId, emptyArray())

    override fun toString() = "StringRes($resId, ${args.joinToString(", ")})"
    fun toString(context: Context) = context.getString(resId, *args)
    fun toString(resources: Resources) = resources.getString(resId, *args)
}
