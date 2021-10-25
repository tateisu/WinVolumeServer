package jp.juggler.volumeclient

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.postDelayed
import androidx.core.widget.addTextChangedListener
import jp.juggler.volumeclient.MainActivityViewModel.Companion.seekBarPositionToVolumeDb
import jp.juggler.volumeclient.Utils.getAttrColor
import jp.juggler.volumeclient.Utils.provideViewModel
import jp.juggler.volumeclient.Utils.vg
import jp.juggler.volumeclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val views by lazy {
        ActivityMainBinding.inflate(layoutInflater, null, false)
    }
    private val viewModel by lazy {
        provideViewModel(this) { MainActivityViewModel(this) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(views.root)
        views.sbVolume.max = MainActivityViewModel.seekBarMax

        viewModel.loadOrRestore()
        viewModel.bindViewModelEvents()
        views.bindUiEvents()
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

    private fun MainActivityViewModel.bindViewModelEvents() {
        val activity = this@MainActivity
        showConnectionSettings.observe(activity) {
            if (it != null) {
                views.swConnectionSettings.isChecked = it
                views.tlConnectionSettings.vg(it)
            }
        }
        serverAddr.observe(activity) {
            views.etServerAddr.setTextIfChanged(it)
        }
        serverPort.observe(activity) {
            views.etServerPort.setTextIfChanged(it)
        }
        password.observe(activity) {
            views.etPassword.setTextIfChanged(it)
        }

        volumeBarPos.observe(activity) {
            if (it != null && it != views.sbVolume.progress) {
                views.sbVolume.progress = it
            }
        }
        volumeDb.observe(activity) {
            val text = it?.let { "${it}dB" } ?: ""
            if (text != views.tvVolume.text.toString()) {
                views.tvVolume.text = text
            }
        }
        presets.observe(activity) {
            updatePresets(it)
        }
        error.observe(activity) {
            views.etError.run {
                if (it == null) {
                    setText("")
                    setTextColor(getAttrColor(R.attr.colorEditTextNormal))
                } else {
                    setText(it.toString(applicationContext))
                    setTextColor(
                        getAttrColor(
                            if (it.resId == R.string.connected) {
                                R.attr.colorEditTextNormal
                            } else {
                                R.attr.colorEditTextError
                            }
                        )
                    )
                }
            }
        }
        deviceName.observe(activity) {
            views.tvDeviceName.text = it ?: ""
        }
    }

    private fun ActivityMainBinding.bindUiEvents() {
        swConnectionSettings.setOnCheckedChangeListener { _, isChecked ->
            viewModel.showConnectionSettings.value = isChecked
        }
        etServerAddr.addTextChangedListener {
            setServerConfig()
        }
        etServerPort.addTextChangedListener {
            setServerConfig()
        }
        etPassword.addTextChangedListener {
            setServerConfig()
        }
        btnIncrement.setOnClickListener {
            viewModel.setVolumeDelta(+1)
        }
        btnDecrement.setOnClickListener {
            viewModel.setVolumeDelta(-1)
        }
        btnPresetPlus.setOnClickListener {
            viewModel.volumeDb.value?.let { db -> viewModel.addPreset(db) }
        }
        btnRefresh.setOnClickListener {
            viewModel.postGetCurrentVolume()
        }
        sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val volumeDb = seekBarPositionToVolumeDb(seekBar.progress)
                if (volumeDb != viewModel.volumeDb.value) {
                    viewModel.setVolume(volumeDb)
                }
            }
        })
    }

    private fun EditText.setTextIfChanged(text: String?) {
        text?.takeIf { it != this.text.toString() }
            ?.let { this.setText(text) }
    }

    private fun setServerConfig() {
        // bindUiEventsの呼び出しが全部終わってから実行したい
        views.root.postDelayed(333L) {
            viewModel.setServerConfig(
                views.etServerAddr.text.toString(),
                views.etServerPort.text.toString(),
                views.etPassword.text.toString(),
            )
        }
    }

    private fun updatePresets(srcList: List<Float>?) {
        val flPresets = views.flPresets
        (0 until flPresets.childCount)
            .filter {
                val child = flPresets.getChildAt(it)
                child != null && child.id != R.id.btnPresetPlus
            }
            .reversed()
            .forEach { flPresets.removeViewAt(it) }

        srcList?.sorted()?.forEach { value ->
            val view = layoutInflater.inflate(R.layout.preset_button, flPresets, false)
            flPresets.addView(view)
            if (view is Button) {
                view.run {
                    text = value.toString()
                    setOnClickListener {
                        viewModel.setVolume(value)
                    }
                    setOnLongClickListener {
                        viewModel.removePreset(value)
                        true
                    }
                }
            }
        }
    }
}
