package jp.juggler.volumeclient

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import jp.juggler.volumeclient.MainActivityViewModel.Companion.seekBarPositionToVolumeDb
import jp.juggler.volumeclient.Utils.provideViewModel
import jp.juggler.volumeclient.Utils.vg
import jp.juggler.volumeclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val viewBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater, null, false)
    }
    private val viewModel by lazy {
        provideViewModel(this) { MainActivityViewModel(this) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.sbVolume.apply {
            this.max = MainActivityViewModel.seekBarMax
        }

        viewModel.loadOrRestore()

        viewModel.showConnectionSettings.observe(this) {
            if (it != null) {
                viewBinding.swConnectionSettings.isChecked = it
                viewBinding.tlConnectionSettings.vg(it)
            }
        }

        viewModel.serverAddr.observe(this) {
            if (it != null && it != viewBinding.etServerAddr.text.toString()) {
                viewBinding.etServerAddr.setText(it)
            }
        }
        viewModel.serverPort.observe(this) {
            if (it != null && it != viewBinding.etServerPort.text.toString()) {
                viewBinding.etServerPort.setText(it)
            }
        }
        viewModel.volumeBarPos.observe(this) {
            if (it != null && it != viewBinding.sbVolume.progress) {
                viewBinding.sbVolume.progress = it
            }
        }
        viewModel.volumeDb.observe(this) {
            val text = it?.let{"${it}dB"} ?: ""
            if (text != viewBinding.tvVolume.text.toString()) {
                viewBinding.tvVolume.text = text
            }
        }
        viewModel.presets.observe(this) {
            updatePresets(it)
        }
        viewModel.error.observe(this) {
            viewBinding.etError.setText(it ?: "")
        }
        viewModel.deviceName.observe(this) {
            viewBinding.tvDeviceName.text = it ?: ""
        }

        viewBinding.swConnectionSettings.setOnCheckedChangeListener { _, isChecked ->
            viewModel.showConnectionSettings.value = isChecked
        }

        viewBinding.etServerAddr.addTextChangedListener {
            updateServerDelayed()
        }

        viewBinding.etServerPort.addTextChangedListener {
            updateServerDelayed()
        }

        viewBinding.sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val volumeDb = seekBarPositionToVolumeDb(seekBar.progress)
                viewModel.setVolume(volumeDb)
            }
        })

        viewBinding.btnIncrement.setOnClickListener {
            viewModel.setVolumeDelta( +1)
        }
        viewBinding.btnDecrement.setOnClickListener {
            viewModel.setVolumeDelta( -1)
        }
        viewBinding.btnPresetPlus.setOnClickListener { _->
            viewModel.volumeDb.value?.let{viewModel.addPreset(it ) }
        }
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

    private fun updateServerDelayed() {
        viewBinding.root.postDelayed(
            {
                viewModel.updateServer(
                    viewBinding.etServerAddr.text.toString(),
                    viewBinding.etServerPort.text.toString()
                )
            },
            333L
        )
    }

    private fun updatePresets(srcList: List<Float>?) {
        val flPresets = viewBinding.flPresets
        var i = flPresets.childCount
        while (--i >= 0) {
            val child = flPresets.getChildAt(i)
            if (child == null || child.id == R.id.btnPresetPlus) continue
            flPresets.removeViewAt(i)
        }
        srcList?.sorted()?.forEach { value ->
            val view =layoutInflater.inflate(R.layout.preset_button, flPresets,false)
            flPresets.addView(view)
            if( view is Button){
                view.run {
                    text = value.toString()
                    setOnClickListener { viewModel.setVolume(value) }
                    setOnLongClickListener{
                        viewModel.removePreset(value)
                        true
                    }
                }
            }
        }
    }

}
