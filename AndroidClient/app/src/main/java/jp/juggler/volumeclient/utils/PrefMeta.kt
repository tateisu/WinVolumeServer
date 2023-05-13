package jp.juggler.volumeclient.utils

import android.content.SharedPreferences

private sealed interface PrefAccess<T> {
    fun read(pref: SharedPreferences, key: String, defVal: T): T
    fun put(e: SharedPreferences.Editor, key: String, value: T)
}

private object PrefAccessBoolean : PrefAccess<Boolean> {
    override fun read(pref: SharedPreferences, key: String, defVal: Boolean) =
        pref.getBoolean(key, defVal)

    override fun put(e: SharedPreferences.Editor, key: String, value: Boolean) {
        e.putBoolean(key, value)
    }
}

private object PrefAccessInt : PrefAccess<Int> {
    override fun read(pref: SharedPreferences, key: String, defVal: Int) =
        pref.getInt(key, defVal)

    override fun put(e: SharedPreferences.Editor, key: String, value: Int) {
        e.putInt(key, value)
    }
}

private object PrefAccessString : PrefAccess<String> {
    override fun read(pref: SharedPreferences, key: String, defVal: String) =
        pref.getString(key, defVal) ?: defVal

    override fun put(e: SharedPreferences.Editor, key: String, value: String) {
        e.putString(key, value)
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> prefReaderByType(sampleValue: T) =
    when (sampleValue) {
        is Boolean -> PrefAccessBoolean
        is Int -> PrefAccessInt
        is String -> PrefAccessString
        else -> error("prefReaderByType: not implemented for type ${sampleValue::class.java.simpleName}")
    } as PrefAccess<T>

class PrefMeta<T : Any>(
    private val key: String,
    private val defVal: T,
    // UIやLiveDataの現在の値を返す
    private val get: () -> T?,
    // 設定ファイルから読んだ値をUIやLiveDataに反映する
    private val set: (T) -> Unit,
) {
    fun saveTo(e: SharedPreferences.Editor) {
        prefReaderByType(defVal).put(e, key, get() ?: defVal)
    }

    fun loadFrom(pref: SharedPreferences) {
        set(prefReaderByType(defVal).read(pref, key, defVal))
    }
}
