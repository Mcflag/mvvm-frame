package cn.lcsw.diningpos.ui.pay

import android.content.Intent
import android.os.Build
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import cn.lcsw.diningpos.databinding.ActivityFixedBarcodePayBinding
import cn.lcsw.diningpos.function.barcode.camera.CameraManager
import cn.lcsw.diningpos.function.barcode.decode.IHandlerAct
import cn.lcsw.diningpos.function.barcode.decode.MainHandler
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.function.speech.SimpleSpeechSynthesis
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.utils.ClickUtil
import cn.lcsw.diningpos.utils.KeySign
import cn.lcsw.diningpos.utils.SubScreenUtils
import cn.lcsw.diningpos.utils.UnitUtils
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_fixed_barcode_pay.*
import okhttp3.Call
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FixedBarcodePayActivity : BaseActivity<ActivityFixedBarcodePayBinding>(), SurfaceHolder.Callback,
    IHandlerAct {

    override fun getHandler(): MainHandler? {
        return handler as MainHandler?
    }

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
        import(payKodeinModule)
    }

    override val layoutId: Int = R.layout.activity_fixed_barcode_pay

    private val viewModel: PayViewModel by instance()

    val loadingViewModel: CommonLoadingViewModel by instance()

    val prefs: PrefsHelper by instance()

    private lateinit var scanner: ToolScanner

    var terminalTrace: String? = null
    var terminalTime: String? = null
    var totalFee: String = "0"
    var isPaySuccess: Boolean = false

    var isPaying: Boolean = false

    val mTTS: SimpleSpeechSynthesis by instance()
    var mDispose: Disposable? = null

    var outTradeNo = ""
    var channelNo = ""

    override fun initView() {
        totalFee = prefs.total
        val s2 = SpannableString("金额 ￥${UnitUtils.fen2Yuan(totalFee.toLong())}")
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_33)),
            0,
            2,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.money_blue)),
            2,
            s2.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv_money.text = s2
        scanner = ToolScanner()
        scanner.setIsAiboshi(true)
        initScanListener()
        isPaying = false
        tv_paying.visibility = View.INVISIBLE

        canScan = false

        viewModel.loadingLayout.toReactiveStream()
            .doOnNext { loadingViewModel.applyState(it) }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.errorMessage.toReactiveStream()
            .doOnNext {
                displayResult(isPaySuccess, it)
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.barcodePay.toReactiveStream()
            .doOnNext {
                if ("01" == it.return_code && "01" == it.result_code) {
                    outTradeNo = it.out_trade_no ?: ""
                    channelNo = it.channel_trade_no ?: ""
                    isPaySuccess = true
                    if (it.pay_type == "010") {

                    }
                    displayResult(isPaySuccess)
                } else {
                    displayResult(isPaySuccess)
                }
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.queryResult.toReactiveStream()
            .doOnNext {
                outTradeNo = it.out_trade_no ?: ""
                channelNo = it.channel_trade_no ?: ""
                isPaySuccess = true
                if (it.pay_type == "010") {

                }
                displayResult(isPaySuccess)
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.noResult.toReactiveStream()
            .doOnNext {
                displayResult(isPaySuccess)
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.signInfo.toReactiveStream()
            .doOnNext {

            }
            .autoDisposable(scopeProvider)
            .subscribe()
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onScanSuccess(barcode: String) {
                if(result_layout.visibility == View.VISIBLE){
                    result_layout.visibility = View.INVISIBLE
                    isPaying = false
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

    fun startBarcodePay(barcode: String) {
        if (totalFee.isNotEmpty() && totalFee != "0") {
            if (!isPaying && barcode.length > 9) {
                isPaying = true
                mTTS.speakQrcode()
                barcodePay(barcode)
            }
        } else {
            Toast.makeText(
                this@FixedBarcodePayActivity,
                "支付金额有误：\" $totalFee \"，请重新输入",
                Toast.LENGTH_SHORT
            ).show()
        }
        canScan = true
    }

    private fun barcodePay(auth: String) {
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        terminalTime = formatter.format(Date(System.currentTimeMillis()))
        terminalTrace = KeySign.getRandomUUID()
        tv_paying.visibility = View.VISIBLE
        isPaySuccess = false
        viewModel.barcodePay(auth, totalFee, terminalTrace ?: "", terminalTime ?: "", "")
    }

    private fun displayResult(isSuccess: Boolean, msg: String = "") {
        if (isSuccess) {
            mTTS.speakSuccess()
            SubScreenUtils.showText(this, "支付成功")
        } else {
            mTTS.speakFail()
            SubScreenUtils.showText(this, "支付失败")
        }
        tv_result_money.text = "￥${UnitUtils.fen2Yuan(totalFee.toLong())}"
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
                    SubScreenUtils.showBarcodeText(this@FixedBarcodePayActivity, "￥${UnitUtils.fen2Yuan(totalFee.toLong())}")
                    isPaying = false
                }
            })
        result_layout.visibility = View.VISIBLE
        tv_paying.visibility = View.INVISIBLE
    }

    companion object {
        private val MY_PERMISSIONS_REQUEST_CAMERA = 26
        val EXTRA_STRING = "extra_string"
        private val TAG = "BarcodePayActivity"

        fun launch(activity: AppCompatActivity) =
            activity.apply {
                startActivity(Intent(this, FixedBarcodePayActivity::class.java))
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
    }

    override fun onResume() {
        super.onResume()

        Observable.timer(200, TimeUnit.MILLISECONDS)
            .doOnNext {
                mTTS.speakScan()
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        mCameraManager = CameraManager(application)

        initCamera(null)
        Observable.timer(100, TimeUnit.MILLISECONDS)
            .doOnNext {
                SubScreenUtils.showBarcodeText(this, "￥${UnitUtils.fen2Yuan(totalFee.toLong())}")
            }
            .autoDisposable(scopeProvider)
            .subscribe()
    }

    override fun onPause() {
        releaseCamera()
        super.onPause()
        mDispose?.dispose()
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

    // 扫码摄像头
    private var mainHandler: MainHandler? = null

    private var mCameraManager: CameraManager? = null

    private var canScan = false

    val handler: Handler?
        get() = mainHandler

    //region 初始化和回收相关资源
    private fun initCamera(surfaceHolder: SurfaceHolder?) {
        canScan = true
        mainHandler = null
        try {
            mCameraManager!!.openDriver(surfaceHolder)
            if (mainHandler == null) {
                mainHandler = MainHandler(this, mCameraManager)
            }
        } catch (ioe: IOException) {
            Log.e(TAG, "相机被占用", ioe)
        } catch (e: RuntimeException) {
            e.printStackTrace()
            Log.e(TAG, "Unexpected error initializing camera")
        }

    }

    private fun releaseCamera() {
        canScan = false
        if (null != mainHandler) {
            //关闭聚焦,停止预览,清空预览回调,quit子线程looper
            mainHandler!!.quitSynchronously()
            mainHandler = null
        }
        //关闭相机
        if (mCameraManager != null) {
            mCameraManager!!.closeDriver()
            mCameraManager = null
        }
    }
    //endregion

    //region SurfaceHolder Callback 回调方法
    override fun surfaceCreated(holder: SurfaceHolder?) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }
    //endregion

    //region 扫描结果
    override fun checkResult(result: String) {
        if (canScan) {
            canScan = false
            startBarcodePay(result)
        }
    }
    //endregion
}