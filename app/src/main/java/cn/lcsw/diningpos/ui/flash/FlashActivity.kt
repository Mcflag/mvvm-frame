package cn.lcsw.diningpos.ui.flash

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import cn.lcsw.diningpos.R
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.SurfaceHolder
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import cn.lcsw.diningpos.databinding.ActivityFlashBinding
import cn.lcsw.diningpos.function.barcode.camera.CameraManager
import cn.lcsw.diningpos.function.barcode.decode.FlashMainHandler
import cn.lcsw.diningpos.function.scan.NormalScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.function.speech.SimpleSpeechSynthesis
import cn.lcsw.diningpos.ui.main.MainActivity
import cn.lcsw.diningpos.utils.WifiUtil
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import com.blankj.utilcode.util.PhoneUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_flash.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class FlashActivity : BaseActivity<ActivityFlashBinding>(), SurfaceHolder.Callback {

    private val TAG = "FlashActivity"

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
        import(flashKodeinModule)
    }

    override val layoutId: Int = R.layout.activity_flash

    private val viewModel: FlashViewModel by instance()

    val loadingViewModel: CommonLoadingViewModel by instance()
    private lateinit var scanner: ToolScanner

    // 扫码摄像头
    private var mainHandler: FlashMainHandler? = null
    private var mCameraManager: CameraManager? = null
    private var canScan = false
    val handler: Handler?
        get() = mainHandler

    val mTTS: SimpleSpeechSynthesis by instance()

    override fun initView() {
        WifiUtil.openWifi()
        scanner = ToolScanner()
        initScanListener()
        mTTS.speakEmpty()

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

        mCameraManager = CameraManager(application)

        initCamera(null)
    }

    override fun onPause() {
        releaseCamera()
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

    private fun encodeAsBitmap(url: String): Bitmap? {
        var bitmap: Bitmap? = null
        val result: BitMatrix
        val multiFormatWriter = MultiFormatWriter()
        try {
            result = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200)
            val barcodeEncoder = BarcodeEncoder()
            bitmap = barcodeEncoder.createBitmap(result)
        } catch (e: WriterException) {
            e.printStackTrace()
        } catch (iae: IllegalArgumentException) {
            return null
        }
        return bitmap
    }

    fun systemSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS)
        startActivityForResult(intent, 0)
    }

    //region 初始化和回收相关资源
    private fun initCamera(surfaceHolder: SurfaceHolder?) {
        canScan = true
        mainHandler = null
        try {
            mCameraManager!!.openDriver(surfaceHolder)
            if (mainHandler == null) {
                mainHandler = FlashMainHandler(this, mCameraManager)
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
    fun checkResult(result: String) {
        if (canScan) {
            canScan = false
            mTTS.speakQrcode()
            WifiUtil.joinToWifi(result)
            ToastUtils.showShort("加入WiFi成功")
            Observable.timer(500, TimeUnit.MILLISECONDS)
                .doOnNext {
                    canScan = true
                }
                .autoDisposable(scopeProvider)
                .subscribe()
        }
    }
    //endregion
}