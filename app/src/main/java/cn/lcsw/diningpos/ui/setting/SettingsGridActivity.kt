package cn.lcsw.diningpos.ui.setting

import android.content.Intent
import android.os.Environment
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.base.CHANNEL_ID
import cn.lcsw.diningpos.base.UPDATE_URL
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import cn.lcsw.diningpos.databinding.ActivitySettingsGridBinding
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.function.speech.SimpleSpeechSynthesis
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.ui.pay.FixedMainActivity
import cn.lcsw.diningpos.ui.trade_query.OrderQueryActivity
import cn.lcsw.diningpos.ui.trade_query.TradeQueryGridActivity
import cn.lcsw.diningpos.utils.*
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import com.blankj.utilcode.util.ToastUtils
import com.uber.autodispose.autoDisposable
import com.vector.update_app.UpdateAppBean
import com.vector.update_app.UpdateAppManager
import com.vector.update_app.UpdateCallback
import com.vector.update_app.service.DownloadService
import com.vector.update_app.utils.AppUpdateUtils
import kotlinx.android.synthetic.main.activity_settings_grid.*
import org.json.JSONException
import org.json.JSONObject
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsGridActivity : BaseActivity<ActivitySettingsGridBinding>() {

    private val TAG = "SettingsGridActivity"

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
        import(settingsKodeinModule)
    }

    override val layoutId: Int = R.layout.activity_settings_grid

    private val viewModel: SettingsViewModel by instance()

    val loadingViewModel: CommonLoadingViewModel by instance()
    private val prefs: PrefsHelper by instance()

    private val audioHelper: AudioMngHelper by instance()

    val mTTS: SimpleSpeechSynthesis by instance()

    private lateinit var scanner: ToolScanner

    override fun initView() {
        scanner = ToolScanner()
        initScanListener()
        audioHelper.setVoiceStep100(10)
        tv_merchant_name.text = "${prefs.initNewCache.merchant_name}"

        viewModel.loadingLayout.toReactiveStream()
            .doOnNext { loadingViewModel.applyState(it) }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.errorMessage.toReactiveStream()
            .doOnNext {
                Toast.makeText(this@SettingsGridActivity, it, Toast.LENGTH_SHORT).show()
            }
            .autoDisposable(scopeProvider)
            .subscribe()

        val s2 = SpannableString("按【取消】键退出此页面")
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_66)),
            0,
            1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.red)),
            1,
            5,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s2.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_66)),
            5,
            s2.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        notice.text = s2
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onScanSuccess(barcode: String?) {
                super.onScanSuccess(barcode)
            }

            override fun onF2Press() {
                super.onF2Press()
                this@SettingsGridActivity.finish()
            }

            override fun onTyping(barcode: String, nowKeyName: String) {
                when (nowKeyName) {
                    "1" -> {
                        TradeQueryGridActivity.launch(this@SettingsGridActivity)
                    }
                    "2" -> {
                        OrderQueryActivity.launch(this@SettingsGridActivity, "退款")
                    }
                    "3" -> {
                        FixedMainActivity.launch(this@SettingsGridActivity)
                    }
                    "4" -> updateDiy()
                }
            }

            override fun onUpPress() {
                super.onUpPress()
                audioHelper.addVoice100()
                if(audioHelper.get100CurrentVolume() >= 100){
                    mTTS.speakQrcode()
                }else{
                    mTTS.speakQrcode()
                }
            }

            override fun onMultiPress() {
                super.onMultiPress()
                audioHelper.subVoice100()
                mTTS.speakQrcode()
            }
        })
    }

    companion object {
        fun launch(activity: AppCompatActivity) =
            activity.apply {
                startActivity(Intent(this, SettingsGridActivity::class.java))
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
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

    private val isShowDownloadProgress = true
    private var isForceUpdate: Boolean = false
    private var target: String = ""

    fun updateDiy() {
        val mUpdateUrl = UPDATE_URL

        val params = HashMap<String, String>()

        params["merchant_no"] = prefs.initNewCache.merchant_no ?: ""
        params["terminal_no"] = prefs.initNewCache.terminal_id ?: ""
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        val curDate = Date(System.currentTimeMillis())
        val str = formatter.format(curDate)
        params["terminal_time"] = str

        params["channelid"] = CHANNEL_ID
        params["versionType"] = "1"
        params["versionCode"] = (AppUpdateUtils.getVersionCode(this)).toString() + ""

        params["trace_no"] = KeySign.getRandomUUID()
        params["key_sign"] = SignBeanUtil.sginAccessToken(params, prefs.initNewCache.access_token)

        UpdateAppManager.Builder()
            .setActivity(this)
            .setHttpManager(OkGoUpdateHttpUtil())
            .setUpdateUrl(mUpdateUrl)
            .setPost(true)
            .setParams(params)
            //设置apk下载路径，默认是在下载到sd卡下/Download/1.0.0/test.apk
//            .setTargetPath(path)
            .dismissNotificationProgress()
            .build()
            .checkNewApp(object : UpdateCallback() {
                override fun parseJson(json: String): UpdateAppBean {
                    Timber.tag("cccccc").d(json)
                    val updateAppBean = UpdateAppBean()
                    try {
                        val jsonObject = JSONObject(json)
                        val updateState = jsonObject.optString("updateType")

                        //（必须）是否更新Yes,No
                        if ("2" == updateState) {
                            updateAppBean.setUpdate("Yes").isConstraint = false
                            isForceUpdate = false
                            ToastUtils.showShort("有新的安装包")
                        } else if ("1" == updateState) {
                            updateAppBean.setUpdate("Yes").isConstraint = true
                            isForceUpdate = true
                            ToastUtils.showShort("有新的安装包")
                        } else {
                            updateAppBean.setUpdate("No").isConstraint = false
                            ToastUtils.showShort("已是最新的安装包")
                        }
                        updateAppBean
                            //（必须）新版本号，
                            .setNewVersion(jsonObject.optString("versionName"))
                            //（必须）下载地址
                            .setApkFileUrl(jsonObject.optString("downloadlink")).updateLog =
                            jsonObject.optString("specification")

//                        tv_message.text = jsonObject.optString("return_msg")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        ToastUtils.showShort("已是最新的安装包")
                    }

                    return updateAppBean
                }

                public override fun hasNewApp(
                    updateApp: UpdateAppBean?,
                    updateAppManager: UpdateAppManager
                ) {
                    //自定义对话框

                    var appName: String = AppUpdateUtils.getApkName(updateApp)
                    var appDir = File(getDir())
                    if (!appDir.exists()) {
                        appDir.mkdirs()
                    }

                    target =
                        "${appDir}${File.separator}${updateApp?.getNewVersion()}${File.separator}$appName"
                    showDiyDialog(updateApp!!, updateAppManager)
                }

                public override fun onBefore() {

                }

                public override fun onAfter() {

                }

                public override fun noNewApp(error: String?) {

                }
            })
    }


    private fun getDir(): String {
        var path: String = ""
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()) {
            try {
                path = externalCacheDir.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (TextUtils.isEmpty(path)) {
                path =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .getAbsolutePath()
            }
        } else {
            path = getCacheDir().getAbsolutePath()
        }
        return path
    }

    private fun showDiyDialog(updateApp: UpdateAppBean, updateAppManager: UpdateAppManager) {
        val targetSize = updateApp.targetSize
        val updateLog = updateApp.updateLog

        var msg = ""

        if (!TextUtils.isEmpty(targetSize)) {
            msg = "新版本大小：$targetSize\n\n"
        }

        if (!TextUtils.isEmpty(updateLog)) {
            msg += updateLog
        }

        if (isShowDownloadProgress) {
            updateAppManager.download(object : DownloadService.DownloadCallback {
                override fun onStart() {
                    HProgressDialogUtils.showHorizontalProgressDialog(
                        this@SettingsGridActivity,
                        "下载进度",
                        false
                    )
                }

                override fun onProgress(progress: Float, totalSize: Long) {
                    HProgressDialogUtils.setProgress(Math.round(progress * 100))
                }

                override fun setMax(total: Long) {

                }

                override fun onFinish(file: File): Boolean {
                    HProgressDialogUtils.cancel()
                    val intent = Intent("com.sanstar.quiet.install")
                    intent.putExtra("Package_Path", target)
                    intent.putExtra("Package_Name", "cn.lcsw.diningpos")
                    intent.putExtra("Package_Class", "cn.lcsw.diningpos.ui.flash.FlashActivity")
                    sendBroadcast(intent)
                    return false
                }

                override fun onError(msg: String) {
                    Toast.makeText(this@SettingsGridActivity, msg, Toast.LENGTH_SHORT).show()
                    HProgressDialogUtils.cancel()
                }

                override fun onInstallAppAndAppOnForeground(file: File): Boolean {
                    return false
                }
            })
        } else {
            //不显示下载进度
            updateAppManager.download()
        }
    }

}