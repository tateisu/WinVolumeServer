package jp.juggler.volumeclient.utils

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.setIfChanged(newValue: T) {
    if (this.value != newValue) this.value = newValue
}
