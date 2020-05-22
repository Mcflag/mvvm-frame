package cn.lcsw.diningpos.ui.pay

import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.databinding.ActivityFixedMainBinding
import cn.lcsw.diningpos.function.scan.NormalScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import com.uber.autodispose.autoDisposable
import kotlinx.android.synthetic.main.activity_fixed_main.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.entity.AuthInfoResult
import cn.lcsw.diningpos.entity.BarCodePayResult
import cn.lcsw.diningpos.entity.PayQueryResult
import android.view.View
import cn.lcsw.diningpos.function.speech.SimpleSpeechSynthesis
import cn.lcsw.diningpos.utils.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class FixedMainActivity : BaseActivity<ActivityFixedMainBinding>() {

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
    }

    override val layoutId: Int = R.layout.activity_fixed_main

    val prefs: PrefsHelper by instance()

    // 显示result使用
    var payTotalFee: String = "0"
    var errorMsg: String? = ""
    var mDispose: Disposable? = null

    private lateinit var scanner: ToolScanner

    var totalFee: String = "0"

    val mTTS:SimpleSpeechSynthesis by instance()

    override fun initView() {
        scanner = ToolScanner()
        scanner.setMoneyInput(true)
        scanner.setIsAiboshi(true)
        initScanListener()
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
                if (barcode.isNotEmpty() && barcode != ".") {
                    totalFee = UnitUtils.getFenByYuan(barcode).toString()
                    if (totalFee == "0") {
                        Toast.makeText(this@FixedMainActivity, "输入金额为0，请重新输入", Toast.LENGTH_SHORT).show()
                    } else {
                        prefs.total = totalFee
                        if (ClickUtil.isFastClick()) {
                            FixedBarcodePayActivity.launch(this@FixedMainActivity)
                        }
                    }
                } else {
                    Toast.makeText(this@FixedMainActivity, "输入金额为空，请重新输入", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onTyping(barcode: String, nowKeyName: String ) {
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

            override fun onF2Press() {
                super.onF2Press()
                if (ClickUtil.isFastClick()) {
                    finish()
                }
            }
        })
    }

    companion object {
        fun launch(activity: AppCompatActivity) =
            activity.apply {
                var intent = Intent(this, FixedMainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
    }

    override fun onResume() {
        super.onResume()
        reset()

        Observable.timer(200, TimeUnit.MILLISECONDS)
            .doOnNext {
                mTTS.speakWelcome()
                SubScreenUtils.showText(this, "欢迎光临")
            }
            .autoDisposable(scopeProvider)
            .subscribe()
    }

    override fun onPause() {
        super.onPause()
        mDispose?.dispose()
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
