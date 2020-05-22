package cn.lcsw.diningpos.ui.setting

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.SoundPool
import android.os.Environment
import android.os.Handler
import android.os.SystemClock
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.lcsw.diningpos.R
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import cn.lcsw.diningpos.databinding.ActivitySettingsBinding
import cn.lcsw.mvvm.base.view.activity.BaseActivity
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import com.uber.autodispose.autoDisposable
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.SurfaceHolder
import cn.lcsw.diningpos.BuildConfig
import cn.lcsw.diningpos.base.CHANNEL_ID
import cn.lcsw.diningpos.base.UPDATE_URL
import cn.lcsw.diningpos.function.barcode.camera.CameraManager
import cn.lcsw.diningpos.function.barcode.decode.MainHandler
import cn.lcsw.diningpos.function.barcode.decode.SettingMainHandler
import cn.lcsw.diningpos.function.scan.DefaultScanSuccessListener
import cn.lcsw.diningpos.function.scan.ToolScanner
import cn.lcsw.diningpos.function.speech.SimpleSpeechSynthesis
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.utils.*
import com.blankj.utilcode.util.ToastUtils
import com.vector.update_app.UpdateAppBean
import com.vector.update_app.UpdateAppManager
import com.vector.update_app.UpdateCallback
import com.vector.update_app.service.DownloadService
import com.vector.update_app.utils.AppUpdateUtils
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_settings.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class SettingsActivity : BaseActivity<ActivitySettingsBinding>(), SurfaceHolder.Callback {

    private val TAG = "SettingsActivity"

    override val kodein: Kodein = Kodein.lazy {
        extend(parentKodein)
        import(settingsKodeinModule)
    }

    override val layoutId: Int = R.layout.activity_settings

    private val viewModel: SettingsViewModel by instance()

    val loadingViewModel: CommonLoadingViewModel by instance()
    private val prefs: PrefsHelper by instance()

    private lateinit var scanner: ToolScanner
    private var isForceUpdate: Boolean = false

    internal var mHits = LongArray(COUNTS)
    private var target: String = ""

    val mTTS: SimpleSpeechSynthesis by instance()

    // 扫码摄像头
    private var mainHandler: SettingMainHandler? = null
    private var mCameraManager: CameraManager? = null
    private var canScan = false
    val handler: Handler?
        get() = mainHandler

    override fun initView() {
        scanner = ToolScanner()
        initScanListener()
        terminal_mac.text = "机器号：${DeviceUtil.getMacid()}"
        terminal_version.text = "当前版本号：${BuildConfig.VERSION_NAME}"
        tv_notice.text = "按“确定”键检查更新"

        viewModel.loadingLayout.toReactiveStream()
            .doOnNext { loadingViewModel.applyState(it) }
            .autoDisposable(scopeProvider)
            .subscribe()

        viewModel.errorMessage.toReactiveStream()
            .doOnNext {
                Toast.makeText(this@SettingsActivity, it, Toast.LENGTH_SHORT).show()
            }
            .autoDisposable(scopeProvider)
            .subscribe()
    }

    private fun initScanListener() {
        scanner.setOnScanSuccessListener(object : DefaultScanSuccessListener() {
            override fun onScanSuccess(barcode: String?) {
                super.onScanSuccess(barcode)
                updateDiy()
            }

            override fun onF2Press() {
                super.onF2Press()
                this@SettingsActivity.finish()
            }

            override fun onF1Press() {
                System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.size - 1] = SystemClock.uptimeMillis()
                if (mHits[0] >= SystemClock.uptimeMillis() - DURATION) {
                    systemSettings()
                }
            }
        })
    }

    fun systemSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS)
        startActivityForResult(intent, 0)
    }

    fun payStyleSettings() {
        PayStyleActivity.launch(this)
    }

    companion object {
        internal val COUNTS = 5//点击次数
        internal val DURATION = (3 * 1000).toLong()//规定有效时间

        fun launch(activity: AppCompatActivity) =
            activity.apply {
                startActivity(Intent(this, SettingsActivity::class.java))
                overridePendingTransition(R.anim.fade_entry, R.anim.hold)
            }
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

    //region 初始化和回收相关资源
    private fun initCamera(surfaceHolder: SurfaceHolder?) {
        canScan = true
        mainHandler = null
        try {
            mCameraManager!!.openDriver(surfaceHolder)
            if (mainHandler == null) {
                mainHandler = SettingMainHandler(this, mCameraManager)
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

    private val isShowDownloadProgress = true

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

                public override fun hasNewApp(updateApp: UpdateAppBean?, updateAppManager: UpdateAppManager) {
                    //自定义对话框

                    var appName: String = AppUpdateUtils.getApkName(updateApp)
                    var appDir = File(getDir())
                    if (!appDir.exists()) {
                        appDir.mkdirs()
                    }

                    target = "${appDir}${File.separator}${updateApp?.getNewVersion()}${File.separator}$appName"
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
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
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

        tv_title.text = "发现新版本"
        tv_version.text = String.format("版本号：%s", updateApp.newVersion)
        if (isShowDownloadProgress) {
            updateAppManager.download(object : DownloadService.DownloadCallback {
                override fun onStart() {
                    HProgressDialogUtils.showHorizontalProgressDialog(this@SettingsActivity, "下载进度", false)
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
                    Toast.makeText(this@SettingsActivity, msg, Toast.LENGTH_SHORT).show()
                    HProgressDialogUtils.cancel()
                    progress.text = "更新失败，请重试。\n原因：$msg"
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            Activity.RESULT_CANCELED -> when (requestCode) {
                AppUpdateUtils.REQ_CODE_INSTALL_APP -> Toast.makeText(
                    this@SettingsActivity,
                    "取消了安装包的更新",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}