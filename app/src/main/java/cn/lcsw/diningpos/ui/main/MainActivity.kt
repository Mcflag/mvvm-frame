package cn.lcsw.diningpos.ui.main

import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import cn.lcsw.diningpos.databinding.ActivityMainBinding
import cn.lcsw.diningpos.function.scan.NormalScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import com.uber.autodispose.autoDisposable
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.entity.AuthInfoResult
import cn.lcsw.diningpos.entity.BarCodePayResult
import cn.lcsw.diningpos.entity.PayQueryResult
import cn.lcsw.diningpos.ui.pay.BarcodePayActivity
import android.view.View
import cn.lcsw.diningpos.function.speech.SimpleSpeechSynthesis
import cn.lcsw.diningpos.ui.setting.SettingsGridActivity
import cn.lcsw.diningpos.ui.trade_query.SumDetailActivity
import cn.lcsw.diningpos.utils.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
        import(mainKodeinModule)
    }

    override val layoutId: Int = R.layout.activity_main

    val viewModel: MainViewModel by instance()

    val loadingViewModel: CommonLoadingViewModel by instance()

    val prefs: PrefsHelper by instance()

    // 显示result使用
    var payTotalFee: String = "0"
    var isSuccess: Boolean = false
    var errorMsg: String? = ""
    var mDispose: Disposable? = null

    private lateinit var scanner: ToolScanner

    var totalFee: String = "0"

    val mTTS: SimpleSpeechSynthesis by instance()

    override fun initView() {
        isSuccess = intent.getBooleanExtra(IS_SUCCESS, false)
        errorMsg = intent.getStringExtra(FAIL_RESULT)
        viewModel.loadingLayout
            .toReactiveStream()
            .doOnNext { loadingViewModel.applyState(it) }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.errorMessage.toReactiveStream()
            .doOnNext {
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        val s2 = SpannableString("输入收款金额后，点击小键盘【确认】键")
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_66)),
            0,
            13,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.red)),
            13,
            17,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_66)),
            17,
            s2.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv_notice.text = s2
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : NormalScanSuccessListener() {
            override fun onScanSuccess(barcode: String) {
                if(result_layout.visibility == View.VISIBLE){
                    result_layout.visibility = View.INVISIBLE
                }else {
                    if (barcode.isNotEmpty() && barcode != ".") {
                        totalFee = UnitUtils.getFenByYuan(barcode).toString()
                        if (totalFee == "0") {
                            Toast.makeText(this@MainActivity, "输入金额为0，请重新输入", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            prefs.total = totalFee
                            if (ClickUtil.isFastClick()) {
                                BarcodePayActivity.launch(this@MainActivity)
                            }
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "输入金额为空，请重新输入", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onTyping(barcode: String, nowKeyName: String ) {
                notDisplayResult()
                tv_money.text = "￥${barcode}"
                if (barcode.isNotEmpty() && barcode != ".") {
                    totalFee = UnitUtils.getFenByYuan(barcode).toString()
                }
                if (barcode.isNotEmpty()) {
                    tv_money.visibility = View.VISIBLE
                    input_line.visibility = View.INVISIBLE
                } else {
                    tv_money.visibility = View.INVISIBLE
                    input_line.visibility = View.VISIBLE
                }
                when(nowKeyName){
                    "0"-> mTTS.speak0()
                    "1"-> mTTS.speak1()
                    "2"-> mTTS.speak2()
                    "3"-> mTTS.speak3()
                    "4"-> mTTS.speak4()
                    "5"-> mTTS.speak5()
                    "6"-> mTTS.speak6()
                    "7"-> mTTS.speak7()
                    "8"-> mTTS.speak8()
                    "9"-> mTTS.speak9()
                    "."-> mTTS.speakPoint()
                }
            }

            override fun onMenuPress() {
                super.onMenuPress()
                SettingsGridActivity.launch(this@MainActivity)
            }

            override fun onF1Press() {
                super.onF1Press()
                SumDetailActivity.launch(this@MainActivity, true)
            }
        })
    }

    companion object {
        const val IS_SUCCESS: String = "IS_SUCCESS"
        const val FAIL_RESULT: String = "FAIL_RESULT"
        var SHOULD_RESULT: Boolean = false
        fun launch(activity: AppCompatActivity) =
            activity.apply {
                var intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
                finish()
            }

        fun launchWithResult(activity: AppCompatActivity, isSuccess: Boolean, msg: String? = null) =
            activity.apply {
                var intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra(IS_SUCCESS, isSuccess)
                if (!msg.isNullOrEmpty()) {
                    intent.putExtra(FAIL_RESULT, msg)
                }
                SHOULD_RESULT = true
                startActivity(intent)
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
                finish()
            }
    }

    override fun onResume() {
        super.onResume()
        displayResult(errorMsg ?: "")
        reset()
        scanner = ToolScanner()
        scanner.setMoneyInput(true)
        scanner.setIsAiboshi(true)
        initScanListener()
        scanner.clear()

        Observable.timer(200, TimeUnit.MILLISECONDS)
            .doOnNext {
                if (SHOULD_RESULT) {
                    if (isSuccess) {
                        mTTS.speakSuccess()
                        SubScreenUtils.showText(this, "支付成功")
                    } else {
                        mTTS.speakFail()
                        SubScreenUtils.showText(this, "支付失败")
                    }
                } else {
                    mTTS.speakWelcome()
                    SubScreenUtils.showText(this, "欢迎光临")
                }
            }
            .autoDisposable(scopeProvider)
            .subscribe()

    }

    private fun displayResult(msg: String) {
        if (!SHOULD_RESULT) {
            return
        }
        payTotalFee = prefs.total
        if (payTotalFee.isNullOrEmpty()) {
            payTotalFee = "0"
        }
        tv_result_money.text = "￥${UnitUtils.fen2Yuan(payTotalFee.toLong())}"
        if (isSuccess) {
            status_icon.setImageResource(R.drawable.status1)
        } else {
            status_icon.setImageResource(R.drawable.status3)
        }
        tv_result.text = if (isSuccess) "收款成功" else "支付失败"
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
                    result_layout.visibility = View.INVISIBLE
                    SubScreenUtils.showText(this@MainActivity, "欢迎光临")
                }
            })
        result_layout.visibility = View.VISIBLE
    }

    private fun notDisplayResult() {
        result_layout.visibility = View.INVISIBLE
        SHOULD_RESULT = false
    }

    override fun onPause() {
        super.onPause()
        SHOULD_RESULT = false
        mDispose?.dispose()
//        mTTS.stop()
    }

    private fun reset() {
        tv_money.text = ""
        tv_money.visibility = View.INVISIBLE
        input_line.visibility = View.VISIBLE
        prefs.total = ""
        prefs.authCache = AuthInfoResult()
        prefs.payResult = BarCodePayResult()
        prefs.queryResult = PayQueryResult()
        prefs.outTradeNo = ""
        prefs.channelTradeNo = ""
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
