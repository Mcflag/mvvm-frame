package cn.lcsw.diningpos.ui.trade_query

import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import cn.lcsw.diningpos.databinding.ActivityBarcodePayBinding
import cn.lcsw.diningpos.function.barcode.camera.CameraManager
import cn.lcsw.diningpos.function.barcode.decode.IHandlerAct
import cn.lcsw.diningpos.function.barcode.decode.MainHandler
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.function.speech.SimpleSpeechSynthesis
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.ui.pay.PayViewModel
import cn.lcsw.diningpos.ui.pay.payKodeinModule
import cn.lcsw.diningpos.utils.AppManager
import cn.lcsw.diningpos.utils.ClickUtil
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import com.blankj.utilcode.util.ToastUtils
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_trade_query_single.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class OrderQueryActivity : BaseActivity<ActivityBarcodePayBinding>(), SurfaceHolder.Callback,
    IHandlerAct {

    override fun getHandler(): MainHandler? {
        return handler as MainHandler?
    }

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
        import(payKodeinModule)
    }

    override val layoutId: Int = R.layout.activity_trade_query_single

    private val viewModel: PayViewModel by instance()

    val loadingViewModel: CommonLoadingViewModel by instance()

    val prefs: PrefsHelper by instance()

    private lateinit var scanner: ToolScanner

    val mTTS: SimpleSpeechSynthesis by instance()

    override fun initView() {
        AppManager.getAppManager().addActivity(this)
        tv_title.setText(intent.getStringExtra(KEY_TITLE))

        scanner = ToolScanner()
        scanner.setNumberInput(true)
        scanner.setIsAiboshi(true)
        initScanListener()

        canScan = false

        viewModel.loadingLayout.toReactiveStream()
            .doOnNext { loadingViewModel.applyState(it) }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.errorMessage.toReactiveStream()
            .doOnNext {
                ToastUtils.showShort(it)
                et_order_no.setText("")
                canScan = true
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.queryResult.toReactiveStream()
            .doOnNext {
                Timber.d(it.toString())
                TradeDetailActivity.launch(this, it)
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.noResult.toReactiveStream()
            .doOnNext {
                ToastUtils.showShort("查询失败")
                et_order_no.setText("")
                canScan = true
            }
            .autoDisposable(scopeProvider)
            .subscribe()

    }

//    override fun showCamera() {
//
//    }

//    override fun onCameraDenied() {
//        super.onCameraDenied()
//        Toast.makeText(this, "无法打开相机扫码，请在设置中打开扫码权限", Toast.LENGTH_SHORT).show()
//    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onScanSuccess(barcode: String) {
                if (barcode.isNotEmpty()) {
                    startQuery(barcode)
                }
            }

            override fun onF2Press() {
                super.onF2Press()
                if (ClickUtil.isFastClick()) {
                    finish()
                }
            }

            override fun onTyping(barcode: String, nowKeyName: String) {
                et_order_no.setText(barcode)
            }

        })
    }

    private fun startQuery(outTradeNo: String) {
        viewModel.query(outTradeNo)
    }


    companion object {

        const val KEY_TITLE = "key_title"

        private val MY_PERMISSIONS_REQUEST_CAMERA = 26
        val EXTRA_STRING = "extra_string"
        private val TAG = OrderQueryActivity.javaClass.simpleName

        fun launch(activity: AppCompatActivity, title: String) =
            activity.apply {
                val intent = Intent(this, OrderQueryActivity::class.java)
                intent.putExtra(KEY_TITLE, title)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
    }

    override fun onResume() {
        super.onResume()
        scanner.clear()
        et_order_no.setText("")

        mCameraManager = CameraManager(application)

        initCamera(null)

    }

    override fun onPause() {
        releaseCamera()
        super.onPause()
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

            et_order_no.setText(result)

            mTTS.speakQrcode()
            startQuery(result)
        }
    }
    //endregion
}