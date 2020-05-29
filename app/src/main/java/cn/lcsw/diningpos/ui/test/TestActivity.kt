package cn.lcsw.diningpos.ui.test

import android.annotation.SuppressLint
import android.annotation.TargetApi
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
import cn.lcsw.diningpos.databinding.ActivityTest1Binding
import cn.lcsw.diningpos.function.scan.NormalScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.utils.SystemProperty
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import com.alipay.iot.sdk.APIManager
import com.alipay.iot.sdk.voice.VoiceAPI
import com.alipay.zoloz.smile2pay.InstallCallback
import com.alipay.zoloz.smile2pay.Zoloz
import com.alipay.zoloz.smile2pay.ZolozConfig
import com.alipay.zoloz.smile2pay.ZolozConstants
import com.alipay.zoloz.smile2pay.feature.FeatureResult
import com.alipay.zoloz.smile2pay.verify.BehaviourLog
import com.alipay.zoloz.smile2pay.verify.FaceVerifyResult
import com.alipay.zoloz.smile2pay.verify.Smile2PayResponse
import com.alipay.zoloz.smile2pay.verify.VerifyCallback
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.PhoneUtils
import com.google.gson.Gson
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_test1.*
import org.json.JSONObject
import org.kodein.di.Kodein
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit


class TestActivity : BaseActivity<ActivityTest1Binding>() {

    private val TAG = "TestActivity"

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
    }

    override val layoutId: Int = R.layout.activity_test1

    private lateinit var context: Context
    private lateinit var scanner: ToolScanner
    private lateinit var voiceAPI: VoiceAPI

    private var mPresentation: DifferentDisplay? = null
    var mZoloz: Zoloz? = null
    private var sn = ""
    private var mac = ""

    @SuppressLint("WrongConstant")
    override fun initView() {
        context = this
        scanner = ToolScanner()
        initScanListener()
        logo.setOnClickListener {
            systemSettings()
        }
        voiceAPI = APIManager.getInstance().voiceAPI
        voice.setOnClickListener {
            voiceAPI.play("ONLYPRICE", "1.2")
        }
        light.setOnClickListener {
            requestLed("yellow", 1)
            Observable.timer(3000, TimeUnit.MILLISECONDS)
                .doOnNext { requestLed("blue", 1) }
                .autoDisposable(scopeProvider)
                .subscribe()
        }
        screen.setOnClickListener {
            startActivity(Intent(this@TestActivity, TestScreenActivity::class.java))
        }

        mZoloz = Zoloz.getInstance(this)
        try {
            sn = SystemProperty.getProperties("ro.serialno", "")
            Timber.tag("cccccc").d("sn:$sn")
            mac = DeviceUtils.getMacAddress()
            Timber.tag("cccccc").d("mac:$mac")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (mZoloz != null) {
            mZoloz?.install(mockMerchantInfo())
            mZoloz?.register(null, object : InstallCallback() {
                override fun onResponse(smileToPayResponse: Smile2PayResponse) {
                    Timber.tag("cccccc").d("loadFeature()...response")
                    Timber.tag("cccccc").d(smileToPayResponse.toString())
                    if (smileToPayResponse != null) {
                        val code: Int = smileToPayResponse.getCode()
                        val msg =
                            if (Smile2PayResponse.CODE_SUCCESS === code) "install成功" else "install失败"

                        Timber.tag("cccccc").d(msg)
                        if (smileToPayResponse.getExtInfo() != null) {
                            val featureResult: FeatureResult =
                                smileToPayResponse.getExtInfo().get(ZolozConstants.KEY_FEATURE_RESULT) as FeatureResult
                            Timber.tag("cccccc").d("featureResult:$featureResult")
                            Timber.tag("cccccc").d(Gson().toJson(featureResult))
                        }
                    }
                }
            })

            mZoloz?.setConnectCallback { connected, componentName ->
                Timber.tag("cccccc").d("registerConnectCall, connected:$connected")
                if (!connected) {
                    mZoloz?.register(null, object : InstallCallback() {
                        override fun onResponse(smileToPayResponse: Smile2PayResponse) {
                            Timber.tag("cccccc").d(smileToPayResponse.toString())
                        }
                    })
                }
            }
        }

        face_pay.setOnClickListener {
            facePay()
        }
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : NormalScanSuccessListener() {
            override fun onScanSuccess(barcode: String) {

            }


            override fun onTyping(barcode: String, nowKeyName: String) {
                when (nowKeyName) {
                    "0" -> voiceAPI.play("e24", "0")
                    "1" -> voiceAPI.play("e24", "1")
                    "2" -> voiceAPI.play("e24", "2")
                    "3" -> voiceAPI.play("e24", "3")
                    "4" -> voiceAPI.play("e24", "4")
                    "5" -> voiceAPI.play("e24", "5")
                    "6" -> voiceAPI.play("e24", "6")
                    "7" -> voiceAPI.play("e24", "7")
                    "8" -> voiceAPI.play("e24", "8")
                    "9" -> voiceAPI.play("e24", "9")
                    "." -> voiceAPI.play("e18")
                }
            }
        })
    }

    @SuppressLint("WrongConstant")
    private fun requestLed(color: String, status: Int) {
        try {
            val antManager = getSystemService("antservice")
            val antManagerClass: Class<*> = antManager.javaClass
            val requestLed = antManagerClass.getDeclaredMethod(
                "requestLedStatus",
                String::class.java,
                Int::class.javaPrimitiveType
            )
            requestLed.isAccessible = true
            requestLed.invoke(antManager, color, status)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        showSubScreen()
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

    private fun showSubScreen() {
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
                    mPresentation = DifferentDisplay(applicationContext, display)
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

    private fun facePay() {
        if (mZoloz != null) {
            mZoloz?.verify(mockConfigInfo(), object : VerifyCallback() {
                override fun onResponse(smileToPayResponse: Smile2PayResponse) {
                    Timber.tag("cccccc").d("verify()...response")
                    if (smileToPayResponse != null) {
                        val code = smileToPayResponse.code
                        val uid = smileToPayResponse.alipayUid
                        val msg =
                            if (Smile2PayResponse.CODE_SUCCESS == code) "识别成功" else "识别失败"
                        val behaviourLog =
                            smileToPayResponse.extInfo[ZolozConstants.KEY_BEHAVIOR_LOG] as BehaviourLog?
                        val faceVerifyResult =
                            smileToPayResponse.extInfo[ZolozConstants.KEY_FACE_VERIFY_RESULT] as FaceVerifyResult?
                        // toast显示刷脸结果，isv应按照真实场景处理和显示刷脸结果
                        Timber.tag("cccccc").d(
                            String.format(
                                Locale.getDefault(), "code:%s, result: %s, %s",
                                code, msg, Gson().toJson(faceVerifyResult)
                            )
                        )
                        Timber.tag("cccccc").d("map:" + smileToPayResponse.extInfo.size)
                    }
                }
            })
        }
    }

    private fun mockMerchantInfo(): Map<String, String> {
        val merchantInfo: MutableMap<String, String> = HashMap()
        // 必填项，商户机具终端编号设备SN
        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_DEVICE_NUM] = sn
        // 必填项，ISV PID
        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_ISV_PID] = "2016081501751688"
        // 必填项，ISV 名称
        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_ISV_NAME] = "湖北利楚科技"
        // 必填项，商户id(如团餐场景：学校社会信用代码或者学校国标码)
        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_MERCHANT_ID] = "914201065818086720"
        // 必填项，商户名称(如团餐场景：学校名称)
        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_MERCHANT_NAME] = "御龙时代中心"
        // 必填项，库id
        merchantInfo[ZolozConstants.KEY_GROUP_ID] = "K12_914201065818086720"
        return merchantInfo
    }

    private fun mockConfigInfo(): Map<String, Any> {
        val configInfo = JSONObject()
        // 必填项，模式
        configInfo.put(ZolozConfig.KEY_MODE_FACE_MODE, ZolozConfig.FaceMode.FACEPAY)

        val zolozConfig: MutableMap<String, Any> = HashMap(2)
        zolozConfig[ZolozConfig.KEY_ZOLOZ_CONFIG] = configInfo.toString()
        // 设置UI config
        val uiInfo = JSONObject()
        uiInfo.put(ZolozConfig.KEY_UI_PAY_AMOUNT, 0.01f)
//        uiInfo.put(ZolozConfig.KEY_UI_ENABLE_TIME_OUT, false)
        zolozConfig.put(ZolozConfig.KEY_UI_CONFIG, uiInfo.toString())
        return zolozConfig
    }
}