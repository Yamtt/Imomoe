package com.skyd.imomoe.view.activity

import android.os.Bundle
import android.view.View
import android.view.ViewStub
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.flurry.android.FlurryAgent
import com.skyd.imomoe.R
import com.skyd.imomoe.config.Const
import com.skyd.imomoe.ext.*
import com.skyd.imomoe.ext.theme.*
import com.skyd.imomoe.util.Util
import com.skyd.imomoe.util.eventbus.EventBusSubscriber
import com.skyd.imomoe.util.logE
import org.greenrobot.eventbus.EventBus

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    protected lateinit var mBinding: VB
    private lateinit var loadFailedTipView: View
    private lateinit var tvImageTextTip1: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beforeSetContentView()

        mBinding = getBinding()
        setContentView(mBinding.root)

        if (transparentSystemBar()) {
            window.transparentSystemBar(mBinding.root)
        }

        if (Util.lastReadUserNoticeVersion() >= Const.Common.USER_NOTICE_VERSION) {
            initializeFlurry(application)
        }
    }

    protected open fun transparentSystemBar(): Boolean = true

    protected abstract fun getBinding(): VB

    override fun onStart() {
        super.onStart()
        if (this is EventBusSubscriber) EventBus.getDefault().register(this)
        FlurryAgent.onStartSession(this)
    }

    override fun onStop() {
        super.onStop()
        if (this is EventBusSubscriber && EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        FlurryAgent.onEndSession(this)
    }

    protected open fun getLoadFailedTipView(): ViewStub? = null

    protected open fun showLoadFailedTip(
        text: String = getString(R.string.load_data_failed_click_to_retry),
        onClickListener: View.OnClickListener? = null
    ) {
        val loadFailedTipViewStub = getLoadFailedTipView() ?: return
        if (loadFailedTipViewStub.parent != null) {
            loadFailedTipView = loadFailedTipViewStub.inflate()
            tvImageTextTip1 = loadFailedTipView.findViewById(R.id.tv_image_text_tip_1)
            tvImageTextTip1.text = text
            if (onClickListener != null) loadFailedTipView.setOnClickListener(onClickListener)
        } else {
            if (this::loadFailedTipView.isInitialized) {
                loadFailedTipView.visible()
            } else {
                logE("showLoadFailedTip", "layout_image_text_tip_1 isn't initialized")
            }
        }
    }

    protected open fun hideLoadFailedTip() {
        val loadFailedTipViewStub = getLoadFailedTipView() ?: return
        if (loadFailedTipViewStub.parent == null) {
            if (this::loadFailedTipView.isInitialized) {
                loadFailedTipView.gone()
            } else {
                logE("showLoadFailedTip", "layout_image_text_tip_1 isn't initialized")
            }
        }
    }
}