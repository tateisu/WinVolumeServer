package jp.juggler.volumeclient.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

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
) = ViewModelProvider(owner, viewModelFactory(T::class.java, creator))[T::class.java]
