package cn.lcsw.diningpos.ui.trade_query

import android.content.Intent
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import cn.lcsw.diningpos.databinding.ActivityRefundBinding
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.utils.AppManager
import cn.lcsw.diningpos.utils.ClickUtil
import cn.lcsw.diningpos.utils.UnitUtils
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_refund.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RefundActivity : BaseActivity<ActivityRefundBinding>() {

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
        import(queryKodeinModule)
    }

    override val layoutId: Int = R.layout.activity_refund

    private val viewModel: QueryViewModel by instance()

    val loadingViewModel: CommonLoadingViewModel by instance()

    val prefs: PrefsHelper by instance()

    private lateinit var scanner: ToolScanner

    var totalFee: String = "0"
    var outTradeNo: String = ""
    var password: String = ""
    var mDispose: Disposable? = null

    override fun initView() {
        AppManager.getAppManager().addActivity(this)
        totalFee = intent.getStringExtra(TOTAL_FEE)
        outTradeNo = intent.getStringExtra(OUT_TRADE_NO)
        password_view.visibility = View.VISIBLE
        password = prefs.initNewCache.merchant_no?.substring(prefs.initNewCache.merchant_no!!.length - 6) ?: ""
        scanner = ToolScanner()
        scanner.setNumberInput(true)
        scanner.setIsAiboshi(true)
        initScanListener()

        viewModel.loadingLayout.toReactiveStream()
            .doOnNext { loadingViewModel.applyState(it) }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.errorMessage.toReactiveStream()
            .doOnNext {
                displayResult(false, it)
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.refundResult.toReactiveStream()
            .doOnNext {
                displayResult(true)
            }
            .autoDisposable(scopeProvider)
            .subscribe()

    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onScanSuccess(barcode: String) {
                if(result_layout.visibility == View.VISIBLE){
                    AppManager.getAppManager().finishAllActivity()
                    finish()
                }
            }

            override fun onF2Press() {
                super.onF2Press()
                if (ClickUtil.isFastClick()) {
                    AppManager.getAppManager().finishAllActivity()
                    finish()
                }
            }

            override fun onTyping(barcode: String, nowKeyName: String) {
                password_view.addPassWord(barcode)
                if (barcode.length == 6) {
                    if (barcode == password) {
                        viewModel.refund(outTradeNo, totalFee)
                    } else {
                        Toast.makeText(this@RefundActivity, "密码不正确", Toast.LENGTH_SHORT).show()
                        scanner.clear()
                        password_view.addPassWord("")
                    }
                }
            }

        })
    }

    private fun displayResult(isSuccess: Boolean, msg: String = "") {
        tv_result_money.text = "￥${UnitUtils.fen2Yuan(totalFee.toLong())}"
        if (isSuccess) {
            status_icon.setImageResource(R.drawable.status1)
        } else {
            status_icon.setImageResource(R.drawable.status3)
        }
        tv_result.text = if (isSuccess) "退款成功" else "退款失败"
        tv_result.setTextColor(if (isSuccess) resources.getColor(R.color.color_33) else resources.getColor(R.color.red))
        tv_result_money.visibility = if (isSuccess) View.VISIBLE else View.INVISIBLE
        tv_result_notice.visibility = if (isSuccess) View.INVISIBLE else View.VISIBLE
        tv_result_notice.text = msg
        Observable.interval(50, 1000, TimeUnit.MILLISECONDS)
            .take(3)
            .observeOn(AndroidSchedulers.mainThread())
            .map { 3 - it }
            .autoDisposable(scopeProvider)
            .subscribe(object : Observer<Long> {

                override fun onSubscribe(d: Disposable) {
                    mDispose = d
                }

                override fun onNext(t: Long) {
                    close.text = "${t}秒后自动关闭"
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                    AppManager.getAppManager().finishAllActivity()
                    finish()
                }
            })
        result_layout.visibility = View.VISIBLE
    }

    companion object {
        const val TOTAL_FEE = "TOTAL_FEE"
        const val OUT_TRADE_NO = "OUT_TRADE_NO"

        fun launch(activity: AppCompatActivity, totalFee: String, outTradeNo:String) =
            activity.apply {
                val intent = Intent(this, RefundActivity::class.java)
                intent.putExtra(TOTAL_FEE, totalFee)
                intent.putExtra(OUT_TRADE_NO, outTradeNo)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mDispose?.dispose()
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