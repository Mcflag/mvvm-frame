package cn.lcsw.diningpos.ui.pay

import android.content.Intent
import android.os.RemoteException
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import cn.lcsw.diningpos.databinding.ActivityPayBinding
import cn.lcsw.diningpos.entity.AuthInfoResult
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.ui.result.ResultActivity
import cn.lcsw.diningpos.utils.ClickUtil
import cn.lcsw.diningpos.utils.KeySign
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_pay.*
import kotlinx.android.synthetic.main.layout_top.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PayActivity : BaseActivity<ActivityPayBinding>() {

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
        import(payKodeinModule)
    }

    override val layoutId: Int = R.layout.activity_pay

    private val viewModel: PayViewModel by instance()

    val loadingViewModel: CommonLoadingViewModel by instance()

    val prefs: PrefsHelper by instance()

    private lateinit var scanner: ToolScanner

    var terminalTrace: String? = null
    var terminalTime: String? = null
    var totalFee: String = "0"
    var isPaySuccess: Boolean = false

    override fun initView() {
        totalFee = prefs.total
        money.text = ""
        scanner = ToolScanner()
        top_back.visibility = View.INVISIBLE
        top_title.text = "正在刷脸支付"
        top_title.visibility = View.INVISIBLE
        top_divider.visibility = View.INVISIBLE
        initScanListener()

        viewModel.loadingLayout.toReactiveStream()
            .doOnNext { loadingViewModel.applyState(it) }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.errorMessage.toReactiveStream()
            .doOnNext {
                Toast.makeText(this@PayActivity, it, Toast.LENGTH_SHORT).show()
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.authInfo.toReactiveStream()
            .doOnNext {
                if (totalFee.isNotEmpty() && totalFee != "0") {
                    facePay(it, totalFee)
                } else {
                    Toast.makeText(this@PayActivity, "输入金额有误：\" $totalFee \"，请重新输入", Toast.LENGTH_SHORT).show()
                }
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.facePay.toReactiveStream()
            .doOnNext {
                if ("01" == it.return_code && "01" == it.result_code) {
                    isPaySuccess = true
                } else {
                }
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.barcodePay.toReactiveStream()
            .doOnNext {
                if ("01" == it.return_code && "01" == it.result_code) {
                    isPaySuccess = true
                } else {
                }
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.queryResult.toReactiveStream()
            .doOnNext {
                isPaySuccess = true
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.noResult.toReactiveStream()
            .doOnNext {
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        Observable.timer(300, TimeUnit.MILLISECONDS)
            .doOnNext { startFacePay() }
            .autoDisposable(scopeProvider)
            .subscribe()
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onDelPress() {
                super.onDelPress()
            }
        })
    }

    private fun startFacePay() {
        if (ClickUtil.isFastClick()) {

        } else {
//            Toast.makeText(this@QRPayActivity, "请勿频繁操作", Toast.LENGTH_SHORT).show()
        }

    }

    private fun facePay(auth: AuthInfoResult, totalFee: String) {
    }

    companion object {
        fun launch(activity: AppCompatActivity) =
            activity.apply {
                startActivity(Intent(this, PayActivity::class.java))
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
    }

    override fun onResume() {
        super.onResume()
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