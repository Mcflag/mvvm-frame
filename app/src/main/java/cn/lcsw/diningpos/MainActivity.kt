package cn.lcsw.diningpos

import android.content.ComponentName
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.utils.SystemProperty
import com.alipay.zoloz.scan2pay.ScanCallback
import com.alipay.zoloz.smile2pay.*
import com.alipay.zoloz.smile2pay.feature.FeatureResult
import com.alipay.zoloz.smile2pay.verify.BehaviourLog
import com.alipay.zoloz.smile2pay.verify.FaceVerifyResult
import com.alipay.zoloz.smile2pay.verify.Smile2PayResponse
import com.alipay.zoloz.smile2pay.verify.VerifyCallback
import com.google.gson.Gson
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {
    var mZoloz: Zoloz? = null
    var mScanCallback: ScanCallback? = null

    private var sn = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mm)
        val report = findViewById<Button>(R.id.report)
        mZoloz = Zoloz.getInstance(this)
        try {
            var version1 = getPackageManager().getPackageInfo("com.alipay.zoloz.smile", 0)
            Timber.tag("cccccc").d(version1.versionName)

            var buildVersion = SystemProperty.getProperties("ro.alipayiot.build.version", "")
            Timber.tag("cccccc").d(buildVersion)
            sn = SystemProperty.getProperties("ro.serialno", "")
            Timber.tag("cccccc").d(sn)
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

        report.setOnClickListener {
            facePay()
        }
    }

    private fun registerScan() {
        if (mScanCallback == null) {
            mScanCallback = object : ScanCallback() {
                override fun onResult(code: String?) {
                    Timber.tag("cccccc").d(code)
                }

                override fun onEvent(eventCode: String?, eventMsg: String?) {
                    Timber.tag("cccccc").d(
                        String.format(
                            Locale.getDefault(), "code:%s, message: %s", eventCode, eventMsg
                        )
                    )
                }
            }
        }
        if (mZoloz != null) {
            mZoloz?.register(null, mScanCallback)
        }
    }

    private fun unRegisterScan() {
        if (mScanCallback != null) {
            mZoloz?.unregister(mScanCallback)
        }
    }

    override fun onResume() {
        super.onResume()
//        registerScan()
    }

    override fun onPause() {
        super.onPause()
//        unRegisterScan()
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

    private fun mockMerchantInfo(): Map<String, String> {
        val merchantInfo: MutableMap<String, String> = HashMap()
        // 必填项，商户机具终端编号设备SN
        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_DEVICE_NUM] = sn
        // 必填项，ISV PID
        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_ISV_PID] = "2016081501751688"
        // 必填项，ISV 名称
        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_ISV_NAME] = ""
//        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_ISV_NAME] = "湖北利楚科技"
        // 必填项，商户id(如团餐场景：学校社会信用代码或者学校国标码)
        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_MERCHANT_ID] = "914201065818086720"
        // 必填项，商户名称(如团餐场景：学校名称)
        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_MERCHANT_NAME] = ""
//        merchantInfo[ZolozConstants.KEY_MERCHANT_INFO_MERCHANT_NAME] = "利楚中学"
        // 必填项，库id
        merchantInfo[ZolozConstants.KEY_GROUP_ID] = "K12_914201065818086720"
        return merchantInfo
    }

}