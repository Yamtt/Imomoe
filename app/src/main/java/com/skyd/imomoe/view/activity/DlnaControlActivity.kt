package com.skyd.imomoe.view.activity

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.skyd.imomoe.R
import com.skyd.imomoe.databinding.ActivityDlnaControlBinding
import com.skyd.imomoe.ext.addFitsSystemWindows
import com.skyd.imomoe.ext.gone
import com.skyd.imomoe.ext.visible
import com.skyd.imomoe.util.Util.setColorStatusBar
import com.skyd.imomoe.util.dlna.CastObject
import com.skyd.imomoe.util.dlna.Utils
import com.skyd.imomoe.util.dlna.Utils.isLocalMediaAddress
import com.skyd.imomoe.util.dlna.Utils.toLocalHttpServerAddress
import com.skyd.imomoe.util.dlna.dmc.DLNACastManager
import com.skyd.imomoe.util.dlna.dmc.control.ICastInterface
import com.skyd.imomoe.util.dlna.dmc.control.newGetInfoListener
import com.skyd.imomoe.util.dlna.dms.MediaServer
import com.skyd.imomoe.util.logE
import com.skyd.imomoe.util.logI
import com.skyd.imomoe.util.showToast
import com.skyd.imomoe.view.listener.dsl.setOnSeekBarChangeListener
import org.fourthline.cling.model.meta.Device

class DlnaControlActivity : BaseActivity<ActivityDlnaControlBinding>() {
    private lateinit var layoutDlnaControlActivityLoading: RelativeLayout
    private var mediaServer: MediaServer? = null
    private lateinit var deviceKey: String
    private lateinit var url: String
    private lateinit var title: String
    private var isPlaying = false

    companion object {
        const val TAG = "DlnaControlActivity"
        val deviceHashMap = HashMap<String, Device<*, *, *>>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setColorStatusBar(window, ContextCompat.getColor(this, R.color.gray_5))

        layoutDlnaControlActivityLoading =
            mBinding.layoutDlnaControlActivityLoading.layoutCircleProgressTextTip1

        url = intent.getStringExtra("url") ?: ""
        title = intent.getStringExtra("title") ?: ""
        deviceKey = intent.getStringExtra("deviceKey") ?: ""

        if (deviceKey.isEmpty()) {
            getString(R.string.dlna_init_data_error).showToast()
            finish()
        }

        //禁止发送数据时点击
        mBinding.run {
            root.addFitsSystemWindows(right = true, top = true, bottom = true)
            layoutDlnaControlActivityLoading.layoutCircleProgressTextTip1.isClickable = true
            layoutDlnaControlActivityLoading.layoutCircleProgressTextTip1.isFocusable = true
            layoutDlnaControlActivityLoading.tvCircleProgressTextTip1.text =
                getString(R.string.sending_data_please_wait)

            ivDlnaControlActivityPlay.setOnClickListener {
                if (isPlaying) {
                    pause()
                } else {
                    play()
                }
            }

            ivDlnaControlActivityStop.setOnClickListener { finish() }
        }

        DLNACastManager.instance.registerActionCallbacks(
            object : ICastInterface.CastEventListener {
                override fun onSuccess(result: String) {
                    deviceHashMap[deviceKey]?.let {
                        DLNACastManager.instance.getMediaInfo(
                            it, newGetInfoListener { t, _ -> logI(TAG, t?.currentURI.toString()) }
                        )
                    }
                    if (!isPlaying) play()
                }

                override fun onFailed(errMsg: String) {
                    getString(R.string.dlna_cast_failed, errMsg).showToast()
                }
            },
            object : ICastInterface.PlayEventListener {
                override fun onSuccess(result: Void?) {
                    getString(R.string.dlna_play).showToast()
                    isPlaying = true
                    mBinding.ivDlnaControlActivityPlay.setImageResource(R.drawable.ic_pause_circle_24)
                    mBinding.ivDlnaControlActivityPlay.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(this@DlnaControlActivity, android.R.color.white)
                    )

                    positionHandler.start(refreshPositionTime)
                    volumeHandler.start(refreshVolumeTime)

                    layoutDlnaControlActivityLoading.gone()
                }

                override fun onFailed(errMsg: String) {
                    getString(R.string.dlna_play_failed, errMsg).showToast()
                    layoutDlnaControlActivityLoading.gone()
                }
            },
            object : ICastInterface.PauseEventListener {
                override fun onSuccess(result: Void?) {
                    getString(R.string.dlna_pause).showToast()
                    isPlaying = false
                    mBinding.ivDlnaControlActivityPlay.setImageResource(R.drawable.ic_play_circle_24)
                    mBinding.ivDlnaControlActivityPlay.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(this@DlnaControlActivity, android.R.color.white)
                    )

                    layoutDlnaControlActivityLoading.gone()
                }

                override fun onFailed(errMsg: String) {
                    getString(R.string.dlna_pause_failed, errMsg).showToast()
                    layoutDlnaControlActivityLoading.gone()
                }
            },
            object : ICastInterface.StopEventListener {
                override fun onSuccess(result: Void?) {
                    getString(R.string.dlna_stop).showToast()
                    isPlaying = false
                    mBinding.ivDlnaControlActivityPlay.setImageResource(R.drawable.ic_play_circle_24)
                    mBinding.ivDlnaControlActivityPlay.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(this@DlnaControlActivity, android.R.color.white)
                    )

                    positionHandler.stop()
                    volumeHandler.stop()

                    layoutDlnaControlActivityLoading.gone()
                }

                override fun onFailed(errMsg: String) {
                    getString(R.string.dlna_stop_failed, errMsg).showToast()
                    layoutDlnaControlActivityLoading.gone()
                }
            },
            object : ICastInterface.SeekToEventListener {
                override fun onSuccess(result: Long) {
                    getString(R.string.dlna_seek_to, Utils.getStringTime(result)).showToast()
                    play()
                    layoutDlnaControlActivityLoading.gone()
                }

                override fun onFailed(errMsg: String) {
                    getString(R.string.dlna_seen_to_failed, errMsg).showToast()
                    layoutDlnaControlActivityLoading.gone()
                }
            }
        )

        mBinding.sbDlnaControlActivity.setOnSeekBarChangeListener {
            onStopTrackingTouch { seekBar ->
                if (durationMillSeconds > 0 && seekBar != null) {
                    val position =
                        (seekBar.progress * 1f / seekBar.max * durationMillSeconds).toInt()
                    layoutDlnaControlActivityLoading.visible()
                    DLNACastManager.instance.seekTo(position.toLong())
                }
            }
        }

        mBinding.sbDlnaControlActivityVolume.setOnSeekBarChangeListener {
            onStopTrackingTouch { seekBar ->
                seekBar ?: return@onStopTrackingTouch
                val volume = (seekBar.progress * 100f / seekBar.max).toInt()
                DLNACastManager.instance.setVolume(volume)
                // 同时取消静音
                DLNACastManager.instance.setMute(false)
                layoutDlnaControlActivityLoading.visible()
            }

            onProgressChanged { seekBar, progress, _ ->
                seekBar ?: return@onProgressChanged
                val volume = (progress * 100f / seekBar.max).toInt()
                mBinding.tvDlnaControlActivityVolume.text = getString(
                    R.string.dlna_volume, volume.toString()
                )
            }
        }

        // 若是本地视频，则转换为本地服务器地址
        if (url.isLocalMediaAddress()) {
            mediaServer = MediaServer(this).apply {
                start()
                DLNACastManager.instance.addMediaServer(this)
            }
            url = url.toLocalHttpServerAddress()
        }

        deviceHashMap[deviceKey]?.let {
            DLNACastManager.instance.cast(
                it,
                CastObject.CastVideo.newInstance(url, System.currentTimeMillis().toString(), title)
            )
        }
    }

    override fun getBinding(): ActivityDlnaControlBinding =
        ActivityDlnaControlBinding.inflate(layoutInflater)

    private fun play() {
        layoutDlnaControlActivityLoading.visible()
        DLNACastManager.instance.play()
    }

    private fun pause() {
        layoutDlnaControlActivityLoading.visible()
        DLNACastManager.instance.pause()
    }

    private fun stop() {
        layoutDlnaControlActivityLoading.visible()
        DLNACastManager.instance.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
        DLNACastManager.instance.unregisterActionCallbacks()
        deviceHashMap.remove(deviceKey)
    }

    private var durationMillSeconds: Long = 0
    private val refreshPositionTime: Long = 500
    private val refreshVolumeTime: Long = 500

    private val positionRunnable: Runnable = object : Runnable {
        override fun run() {
            val device = deviceHashMap[deviceKey] ?: return
            DLNACastManager.instance.getPositionInfo(device, newGetInfoListener { t, errMsg ->
                layoutDlnaControlActivityLoading.gone()
                if (t != null) {
                    mBinding.tvDlnaControlActivityTime.text =
                        String.format("%s / %s", t.relTime, t.trackDuration)
                    if (t.trackDurationSeconds != 0L) {
                        durationMillSeconds = t.trackDurationSeconds * 1000
                        mBinding.sbDlnaControlActivity.progress =
                            (t.trackElapsedSeconds * 100 / t.trackDurationSeconds).toInt()
                    } else {
                        mBinding.sbDlnaControlActivity.progress = 0
                    }
                } else {
                    logE(TAG, errMsg.toString())
                }
            })
        }
    }

    private val volumeRunnable: Runnable = object : Runnable {
        override fun run() {
            val device = deviceHashMap[deviceKey] ?: return
            // update volume
            DLNACastManager.instance.getVolumeInfo(device, newGetInfoListener { t, errMsg ->
                layoutDlnaControlActivityLoading.gone()
                if (t != null) {
                    if (t <= mBinding.sbDlnaControlActivityVolume.max) {
                        mBinding.sbDlnaControlActivityVolume.progress = t
                    }
                    mBinding.tvDlnaControlActivityVolume.text = getString(
                        R.string.dlna_volume, t.toString()
                    )
                } else {
                    logE(TAG, errMsg.toString())
                }
            })
        }
    }

    private val positionHandler = CircleMessageHandler(refreshPositionTime, positionRunnable)
    private val volumeHandler = CircleMessageHandler(refreshVolumeTime, volumeRunnable)

    private class CircleMessageHandler(private val duration: Long, private val runnable: Runnable) :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            runnable.run()
            sendEmptyMessageDelayed(MSG, duration)
        }

        fun start(delay: Long) {
            stop()
            sendEmptyMessageDelayed(MSG, delay)
        }

        fun stop() {
            removeMessages(MSG)
        }

        companion object {
            private const val MSG = 101
        }
    }
}
