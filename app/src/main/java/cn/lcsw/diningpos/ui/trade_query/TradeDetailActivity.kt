package cn.lcsw.diningpos.ui.trade_query

import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.databinding.ActivityTradeDetailBinding
import cn.lcsw.diningpos.entity.PayQueryResult
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.utils.AppManager
import cn.lcsw.diningpos.utils.TimeConverter
import cn.lcsw.diningpos.utils.UnitUtils
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_trade_detail.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class TradeDetailActivity : BaseActivity<ActivityTradeDetailBinding>() {

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
    }

    override val layoutId: Int = R.layout.activity_trade_detail

    private val prefs: PrefsHelper by instance()

    private lateinit var scanner: ToolScanner
    private lateinit var parcelableExtra: PayQueryResult

    override fun initView() {
        AppManager.getAppManager().addActivity(this)
        scanner = ToolScanner()
        initScanListener()
        tv_merchant_name.text = "订单详情"

        val s2 = SpannableString("【确定】键退款，【取消】键退出")
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.red)),
            0,
            4,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_66)),
            4,
            8,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.red)),
            8,
            12,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_66)),
            12,
            s2.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        notice.text = s2

        parcelableExtra = intent.getParcelableExtra(KEY_PAYQUERYRESULT)
        tv_total_fee.text = "￥${UnitUtils.fen2Yuan(parcelableExtra.total_fee?.toLong() ?: 0)}"
        tv_mch_name.text = parcelableExtra.merchant_name
        tv_mch_no.text = parcelableExtra.merchant_no
        tv_terminal_no.text = parcelableExtra.terminal_id
        tv_trade_time.text = TimeConverter.conver(parcelableExtra.pay_time)
        tv_order_no.text = parcelableExtra.out_trade_no

    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onScanSuccess(barcode: String?) {
                super.onScanSuccess(barcode)
                RefundActivity.launch(this@TradeDetailActivity, parcelableExtra.total_fee ?: "0", parcelableExtra.out_trade_no ?: "")
            }

            override fun onF2Press() {
                super.onF2Press()
                this@TradeDetailActivity.finish()
            }

        })
    }

    companion object {

        private const val KEY_PAYQUERYRESULT = "key_payqueryresult"

        fun launch(activity: AppCompatActivity, payQueryResult: PayQueryResult) =
            activity.apply {
                val intent = Intent(this, TradeDetailActivity::class.java)
                intent.putExtra(KEY_PAYQUERYRESULT, payQueryResult)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppManager.getAppManager().finishActivity(this)
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