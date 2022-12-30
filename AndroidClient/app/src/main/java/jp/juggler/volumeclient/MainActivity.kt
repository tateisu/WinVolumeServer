package jp.juggler.volumeclient

import android.app.AlertDialog
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import jp.juggler.volumeclient.utils.provideViewModel

class MainActivity : ComponentActivity() {

    private val viewModel by lazy {
        provideViewModel(this) {
            MainActivityViewModelImpl(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadOrRestore()
        viewModel.mediaError.observe(this) {
            AlertDialog.Builder(this)
                .setMessage("${it.javaClass.simpleName} : ${it.message}")
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
        setContent { MainActivityContent(window, viewModel) }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        viewModel.save()
    }

    override fun onStart() {
        super.onStart()
        viewModel.postGetCurrentVolume()
    }

    override fun onStop() {
        super.onStop()
        viewModel.save()
    }
}
