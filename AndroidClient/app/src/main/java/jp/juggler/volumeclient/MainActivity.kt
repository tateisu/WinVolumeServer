package jp.juggler.volumeclient

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import jp.juggler.volumeclient.Utils.provideViewModel
import jp.juggler.volumeclient.ui.theme.TestJetpackComposeTheme

class MainActivity : ComponentActivity() {

    private val viewModel by lazy {
        provideViewModel(this) { MainActivityViewModelImpl(this) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadOrRestore()

        setContent {
            MainActivityContent(viewModel)
        }

//        setContentView(views.root)
//        views.sbVolume.max = MainActivityViewModel.seekBarMax
//        viewModel.bindViewModelEvents()
//        views.bindUiEvents()
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
