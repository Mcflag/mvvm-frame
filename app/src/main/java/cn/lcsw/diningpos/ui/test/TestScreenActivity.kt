package cn.lcsw.diningpos.ui.test

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Presentation
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Build
import android.provider.Settings
import android.view.Display
import android.view.KeyEvent
import android.view.WindowManager
import android.view.WindowManager.InvalidDisplayException
import android.widget.Toast
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.databinding.ActivityTest2Binding
import cn.lcsw.diningpos.function.scan.NormalScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import com.alipay.iot.sdk.APIManager
import com.alipay.iot.sdk.voice.VoiceAPI
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_test2.*
import org.kodein.di.Kodein
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class TestScreenActivity : BaseActivity<ActivityTest2Binding>() {

    private val TAG = "TestActivity"

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
    }

    override val layoutId: Int = R.layout.activity_test2

    private lateinit var context: Context
    private lateinit var scanner: ToolScanner

    private var mPresentation: Presentation? = null

    @SuppressLint("WrongConstant")
    override fun initView() {
        context = this
        scanner = ToolScanner()
        initScanListener()
        logo.setOnClickListener { finish() }
        screen1.setOnClickListener {
            showScreen1()
        }
        screen2.setOnClickListener {
            var amount = Random.nextInt(1, 100)
            showScreen2(amount)
        }
        screen3.setOnClickListener {
            showScreen3()
        }
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : NormalScanSuccessListener() {
            override fun onScanSuccess(barcode: String) {

            }
        })
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

    private fun showScreen1() {
        if (Settings.canDrawOverlays(applicationContext)) {
            val displayManager: DisplayManager =
                getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
            if (displays.isNotEmpty()) {
                val display: Display = displays[0]
                if (mPresentation != null) {
                    mPresentation?.dismiss()
                    mPresentation = null
                }

                if (mPresentation == null) {
                    mPresentation = SubScreenDisplay1(applicationContext, display)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
                        mPresentation?.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                    } else {
                        mPresentation?.window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                    }
                    try {
                        mPresentation?.show()
                    } catch (e: InvalidDisplayException) {
                        e.printStackTrace()
                        mPresentation = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mPresentation = null
                    }
                }
            }
        }
    }

    private fun showScreen2(amount: Int) {
        if (Settings.canDrawOverlays(applicationContext)) {
            if(mPresentation!=null && mPresentation is SubScreenDisplay2){
                (mPresentation as SubScreenDisplay2?)?.setAmount(amount)
            }else {
                val displayManager: DisplayManager =
                    getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                val displays =
                    displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
                if (displays.isNotEmpty()) {
                    val display: Display = displays[0]
                    if (mPresentation != null) {
                        mPresentation?.dismiss()
                        mPresentation = null
                    }

                    if (mPresentation == null) {
                        mPresentation = SubScreenDisplay2(applicationContext, display, amount)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
                            mPresentation?.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                        } else {
                            mPresentation?.window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                        }
                        try {
                            mPresentation?.show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            mPresentation = null
                        }
                    }
                }
            }
        }
    }

    private fun showScreen3() {
        if (Settings.canDrawOverlays(applicationContext)) {
            val displayManager: DisplayManager =
                getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
            if (displays.isNotEmpty()) {
                val display: Display = displays[0]
                if (mPresentation != null) {
                    mPresentation?.dismiss()
                    mPresentation = null
                }

                if (mPresentation == null) {
                    mPresentation = SubScreenDisplay3(applicationContext, display)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
                        mPresentation?.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                    } else {
                        mPresentation?.window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                    }
                    try {
                        mPresentation?.show()
                    } catch (e: InvalidDisplayException) {
                        e.printStackTrace()
                        mPresentation = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mPresentation = null
                    }
                }
            }
        }
    }
}