package cn.lcsw.diningpos.ui.trade_query

import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.databinding.ActivitySettingsGridBinding
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_settings_grid.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class TradeQueryGridActivity : BaseActivity<ActivitySettingsGridBinding>() {

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
    }

    override val layoutId: Int = R.layout.activity_trade_query

    private val prefs: PrefsHelper by instance()

    private lateinit var scanner: ToolScanner

    override fun initView() {
        scanner = ToolScanner()
        initScanListener()
        tv_merchant_name.text = "${prefs.initNewCache.merchant_name}"


        val s2 = SpannableString("按【取消】键退出此页面")
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_66)),
            0,
            1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.red)),
            1,
            5,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_66)),
            5,
            s2.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        notice.text = s2
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onScanSuccess(barcode: String?) {
                super.onScanSuccess(barcode)
            }

            override fun onF2Press() {
                super.onF2Press()
                this@TradeQueryGridActivity.finish()
            }

            override fun onTyping(barcode: String, nowKeyName: String) {
                when (nowKeyName) {
                    "1" -> {
                        OrderQueryActivity.launch(this@TradeQueryGridActivity, "单笔订单查询")
                    }
                    "2" -> {
                        BeginTimeActivity.launch(this@TradeQueryGridActivity)
                    }
                    "3" -> {
                        SumDetailActivity.launch(this@TradeQueryGridActivity, true)
                    }
                }
            }
        })
    }

    companion object {
        fun launch(activity: AppCompatActivity) =
            activity.apply {
                startActivity(Intent(this, TradeQueryGridActivity::class.java))
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        scanner.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        scanner.analysisKeyEvent(event)
        if (event.keyCode == KeyEvent.KEYCODE_MENU) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

}