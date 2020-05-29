package cn.lcsw.diningpos.base

import android.app.Application
import android.content.Context
import cn.lcsw.diningpos.BuildConfig
import cn.lcsw.diningpos.di.*
import cn.lcsw.diningpos.utils.ProcessPhoenix
import cn.lcsw.mvvm.logger.initLogger
import com.alipay.iot.sdk.APIManager
import com.blankj.utilcode.util.Utils
import com.squareup.leakcanary.LeakCanary
import com.tencent.bugly.crashreport.CrashReport
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.androidModule
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import timber.log.Timber


open class BaseApplication : Application(), KodeinAware {

    override val kodein: Kodein = Kodein.lazy {
        bind<Context>() with singleton { this@BaseApplication }
        import(androidModule(this@BaseApplication))
        import(androidXModule(this@BaseApplication))

        import(serviceModule)
        import(dbModule)
        import(httpClientModule)
        import(prefsModule)
        import(speechModule)
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        initLogger(BuildConfig.DEBUG)
//        initLeakCanary()
        Utils.init(this)
        CrashReport.initCrashReport(applicationContext, "4731c79681", BuildConfig.DEBUG)

        try {
            APIManager.getInstance().initialize(this, "2016081501751688") {
                Timber.tag("cccccc").d("APIManager initialize SUCCESS")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)
    }

    companion object {
        lateinit var INSTANCE: BaseApplication
    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    fun restart() {
        ProcessPhoenix.triggerRebirth(INSTANCE)
    }
}