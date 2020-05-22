package cn.lcsw.diningpos.ui.result

import android.content.Intent
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.databinding.ActivityResultBinding
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.ui.main.MainActivity
import cn.lcsw.diningpos.utils.UnitUtils
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_result.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.lang.Exception
import java.util.concurrent.TimeUnit

class ResultActivity : BaseActivity<ActivityResultBinding>() {

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
    }

    val prefs: PrefsHelper by instance()
    var totalFee: String = "0"
    var isSuccess: Boolean = false
    val timer: MutableLiveData<Int> = MutableLiveData()
    private lateinit var scanner: ToolScanner

    var mDispose: Disposable? = null

    override val layoutId: Int = R.layout.activity_result

    override fun initView() {
        scanner = ToolScanner()
        initScanListener()
        totalFee = prefs.total
        tv_merchant_name.text = "${prefs.initNewCache.merchant_name}"
        tv_money.text = "￥${UnitUtils.fen2Yuan(totalFee.toLong())}"
        isSuccess = intent.getBooleanExtra(IS_SUCCESS, false)

//        if (isSuccess) {
//            status_icon.setImageResource(R.drawable.status1)
//            SubScreenUtils.oledUpdate320x480(this, "支付成功")
//        } else {
//            status_icon.setImageResource(R.drawable.status3)
//            SubScreenUtils.oledUpdate320x480(this, "支付失败")
//        }
        tv_result.text = if (isSuccess) "支付成功" else "支付失败"
        tv_result.setTextColor(if (isSuccess) resources.getColor(R.color.color_33) else resources.getColor(R.color.red))
        tv_money.visibility = if (isSuccess) View.VISIBLE else View.INVISIBLE
        tv_notice.visibility = if (isSuccess) View.INVISIBLE else View.VISIBLE

        mDispose = Observable.interval(50, 1000, TimeUnit.MILLISECONDS)
            .take(5)
            .map { 5 - it }
            .autoDisposable(scopeProvider)
            .subscribe({
                timer.postValue(it.toInt())
            }, {

            }, {
                toMain()
            })
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onBackPress() {
                super.onBackPress()
                toMain()
            }

            override fun onScanSuccess(barcode: String?) {
                super.onScanSuccess(barcode)
                toMain()
            }
        })
    }

    override fun onResume() {
        super.onResume()
//        Observable.timer(200, TimeUnit.MILLISECONDS)
//            .doOnNext {
//                if (isSuccess) {
////                    speech.speak("收款${UnitUtils.fen2Yuan(totalFee.toLong())}元")
//                    speech.speakSuccess()
//                } else {
//                    speech.speakFail()
//                }
//            }
//            .autoDisposable(scopeProvider)
//            .subscribe()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mDispose?.dispose()
        scanner.onDestroy()
    }

    fun toMain() {
        MainActivity.launch(this)
    }

    companion object {
        const val IS_SUCCESS: String = "IS_SUCCESS"
        fun launch(activity: AppCompatActivity, isSuccess: Boolean) =
            activity.apply {
                var intent = Intent(this, ResultActivity::class.java)
                intent.putExtra(IS_SUCCESS, isSuccess)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        scanner.analysisKeyEvent(event)
        if (event.keyCode == KeyEvent.KEYCODE_MENU) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}