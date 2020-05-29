package cn.lcsw.diningpos.ui.flash

import android.annotation.SuppressLint
import android.content.Intent
import android.view.KeyEvent
import android.view.View
import cn.lcsw.diningpos.R
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import cn.lcsw.diningpos.databinding.ActivityFlashBinding
import cn.lcsw.diningpos.function.scan.NormalScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.ui.main.MainActivity
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import com.blankj.utilcode.util.ToastUtils
import com.uber.autodispose.autoDisposable
import kotlinx.android.synthetic.main.activity_flash.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class FlashActivity : BaseActivity<ActivityFlashBinding>(){

    private val TAG = "FlashActivity"

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
        import(flashKodeinModule)
    }

    override val layoutId: Int = R.layout.activity_flash

    private val viewModel: FlashViewModel by instance()

    val loadingViewModel: CommonLoadingViewModel by instance()
    private lateinit var scanner: ToolScanner

    override fun initView() {
        scanner = ToolScanner()
        initScanListener()

        logo.setOnClickListener { systemSettings() }

        viewModel.loadingLayout.toReactiveStream()
            .doOnNext { loadingViewModel.applyState(it) }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.errorMessage.toReactiveStream()
            .doOnNext {
                notice.visibility = View.VISIBLE
                val s2 = SpannableString("请按【确认】键重新初始化")
                s2.setSpan(
                    ForegroundColorSpan(resources.getColor(R.color.color_66)),
                    0,
                    2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                s2.setSpan(
                    ForegroundColorSpan(resources.getColor(R.color.red)),
                    2,
                    6,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                s2.setSpan(
                    ForegroundColorSpan(resources.getColor(R.color.color_66)),
                    6,
                    s2.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                notice.text = s2
                ToastUtils.showShort(it)
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.initInfo.toReactiveStream()
            .doOnNext {
                notice.visibility = View.INVISIBLE
                toMain()
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.initNewInfo.toReactiveStream()
            .doOnNext {
                notice.visibility = View.INVISIBLE
                toMain()
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        requestPhoneState()
    }

    @SuppressLint("MissingPermission")
    override fun showPhoneState() {
        super.showPhoneState()
        var telephonyManager: TelephonyManager =
            getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        var simSerialNumber : String? = telephonyManager.simSerialNumber
        if (simSerialNumber != null && simSerialNumber.length == 20){
            viewModel.initNew(simSerialNumber)
        } else {
            viewModel.initNew(null)
        }
    }

    override fun onPhoneDenied() {
        super.onPhoneDenied()
        ToastUtils.showShort("应用需要允许读取手机状态的权限，请在设置中打开")
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : NormalScanSuccessListener() {
            override fun onScanSuccess(barcode: String) {
                requestPhoneState()
            }
        })
    }

    private fun toMain() {
        MainActivity.launch(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
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

    fun systemSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS)
        startActivityForResult(intent, 0)
    }
}