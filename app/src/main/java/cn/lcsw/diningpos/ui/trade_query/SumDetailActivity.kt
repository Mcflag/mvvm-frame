package cn.lcsw.diningpos.ui.trade_query

import android.annotation.SuppressLint
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import cn.lcsw.diningpos.databinding.ActivitySumDetailBinding
import cn.lcsw.diningpos.entity.PayQueryResult
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.ui.pay.PayViewModel
import cn.lcsw.diningpos.ui.pay.payKodeinModule
import cn.lcsw.diningpos.utils.*
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import com.blankj.utilcode.util.ToastUtils
import com.uber.autodispose.autoDisposable
import kotlinx.android.synthetic.main.activity_sum_detail.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

class SumDetailActivity : BaseActivity<ActivitySumDetailBinding>() {

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
        import(payKodeinModule)
    }

    override val layoutId: Int = R.layout.activity_sum_detail

    private val viewModel: PayViewModel by instance()

    val loadingViewModel: CommonLoadingViewModel by instance()

    private val prefs: PrefsHelper by instance()

    private lateinit var scanner: ToolScanner
    var isToday: Boolean = false
    var beginTime: String = ""
    var endTime: String = ""

    @SuppressLint("SetTextI18n")
    override fun initView() {
        AppManager.getAppManager().addActivity(this)
        scanner = ToolScanner()
        initScanListener()
        tv_merchant_name.text = "汇总查询"

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

        viewModel.loadingLayout.toReactiveStream()
            .doOnNext { loadingViewModel.applyState(it) }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.errorMessage.toReactiveStream()
            .doOnNext {
                ToastUtils.showShort(it)

            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.sumResult.toReactiveStream()
            .doOnNext {
                tv_total_number.text = it.trade_num
                tv_total_fee.text = "￥${UnitUtils.fen2Yuan(it.trade_sum?.toLong() ?: 0)}"
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.refundResult.toReactiveStream()
            .doOnNext {
                tv_refund_number.text = it.trade_num
                tv_refund_fee.text = "-￥${UnitUtils.fen2Yuan(abs(it.trade_sum?.toInt() ?: 0).toLong())}"
            }
            .autoDisposable(scopeProvider)
            .subscribe()


        isToday = intent.getBooleanExtra(IS_TODAY, true)
        var current = System.currentTimeMillis()
        var zero = current/(1000*3600*24)*(1000*3600*24)- TimeZone.getDefault().getRawOffset()
        var twelve = zero+24*60*60*1000-1

        if (isToday) {
            beginTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Timestamp(zero))
            endTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Timestamp(twelve))
        } else {
            beginTime = if(prefs.beginTime.isEmpty()) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Timestamp(zero)) else prefs.beginTime
            endTime = if(prefs.endTime.isEmpty()) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Timestamp(twelve)) else prefs.endTime
        }

        var bTime=beginTime.split(" ")
        var eTime=endTime.split(" ")
        tv_time.text = "${bTime[0].substring(5).replace('-', '/')} ${bTime[1]} - ${eTime[0].substring(5).replace('-', '/')} ${eTime[1]}"

        viewModel.querySum(beginTime, endTime)
        viewModel.queryRefund(beginTime, endTime)
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onScanSuccess(barcode: String?) {

            }

            override fun onF2Press() {
                super.onF2Press()
                AppManager.getAppManager().finishAllActivity()
                this@SumDetailActivity.finish()
            }

            override fun onUpPress() {
                super.onUpPress()
                val ran = (1..1000).random()
                SubScreenUtils.showBarcodeText(this@SumDetailActivity, "￥${UnitUtils.fen2Yuan(ran.toLong())}")
            }
        })
    }

    companion object {
        const val IS_TODAY = "IS_TODAY"

        fun launch(activity: AppCompatActivity, isToday: Boolean) =
            activity.apply {
                val intent = Intent(this, SumDetailActivity::class.java)
                intent.putExtra(IS_TODAY, isToday)
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