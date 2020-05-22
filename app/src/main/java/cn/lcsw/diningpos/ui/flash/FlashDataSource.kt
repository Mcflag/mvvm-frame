package cn.lcsw.diningpos.ui.flash

import android.os.Build
import arrow.core.Either
import arrow.core.getOrElse
import cn.lcsw.diningpos.base.BaseApplication
import cn.lcsw.diningpos.base.INST_NO
import cn.lcsw.diningpos.base.KEY
import cn.lcsw.diningpos.entity.*
import cn.lcsw.diningpos.http.service.ServiceManager
import cn.lcsw.diningpos.manager.InitManager
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.utils.DeviceUtil
import cn.lcsw.diningpos.utils.KeySign
import cn.lcsw.diningpos.utils.SignBeanUtil
import cn.lcsw.mvvm.base.repository.BaseRepositoryBoth
import cn.lcsw.mvvm.base.repository.ILocalDataSource
import cn.lcsw.mvvm.base.repository.IRemoteDataSource
import cn.lcsw.mvvm.util.RxSchedulers
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import java.text.SimpleDateFormat
import java.util.*

interface IFlashRemoteDataSource : IRemoteDataSource {
    fun initNew(sim: String?): Flowable<Either<Errors, InitNewResult>>
    fun init(): Flowable<Either<Errors, InitResult>>
}

interface IFlashLocalDataSource : ILocalDataSource {
    fun savePrefsInit(initInfo: InitResult): Completable

    fun savePrefsInitNew(initInfo: InitNewResult): Completable
}

class FlashDataSourceRepository(
    remoteDataSource: IFlashRemoteDataSource,
    localDataSource: IFlashLocalDataSource
) : BaseRepositoryBoth<IFlashRemoteDataSource, IFlashLocalDataSource>(remoteDataSource, localDataSource) {
    fun init(): Flowable<Either<Errors, InitResult>> =
        remoteDataSource.init()
            .doOnNext { either ->
                either.fold({

                }, {

                })
            }
            .flatMap {
                localDataSource.savePrefsInit(it.getOrElse { InitResult() })
                    .andThen(Flowable.just(it))
            }

    fun initNew(sim: String?): Flowable<Either<Errors, InitNewResult>> =
        remoteDataSource.initNew(sim)
            .doOnNext { either ->
                either.fold({

                }, {

                })
            }
            .flatMap {
                localDataSource.savePrefsInitNew(it.getOrElse { InitNewResult() })
                    .andThen(Flowable.just(it))
            }
}

class FlashRemoteDataSource(
    private val serviceManager: ServiceManager
) : IFlashRemoteDataSource {
    override fun initNew(sim: String?): Flowable<Either<Errors, InitNewResult>> {
        val req = InitNewRequest()

        req.version = "100"
        req.inst_no = INST_NO
        req.terminal_mac = DeviceUtil.getMacid()
        req.terminal_model = Build.MODEL
        sim?.let {
            req.serialnum = sim
        }

        req.trace_no = KeySign.getRandomUUID()
        req.key_sign = SignBeanUtil.signKey(req, KEY)

        return serviceManager.payService
            .initDeviceNew(req)
            .subscribeOn(RxSchedulers.io)
            .map {
                Either.right(it)
            }
    }

    override fun init(): Flowable<Either<Errors, InitResult>> {
        val req = InitRequest()

        req.terminal_mac = DeviceUtil.getMacid()
        req.terminal_brand = Build.BRAND
        req.terminal_model = Build.MODEL
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        val curDate = Date(System.currentTimeMillis())
        val str = formatter.format(curDate)
        req.terminal_time = str

        req.terminal_ver = android.os.Build.VERSION.RELEASE

        req.device_mac = DeviceUtils.getMacAddress()

        req.device_type = "5"
        req.app_verno = AppUtils.getAppVersionName()
        req.app_name = AppUtils.getAppName()
        req.app_package = AppUtils.getAppPackageName()
        req.trace_no = KeySign.getRandomUUID()

        return serviceManager.payService
            .initDevice(req)
            .subscribeOn(RxSchedulers.io)
            .map {
                Either.right(it)
            }
    }
}

class FlashLocalDataSource(
    private val prefs: PrefsHelper
) : IFlashLocalDataSource {
    override fun savePrefsInit(initInfo: InitResult): Completable =
        Completable.fromAction {
            prefs.initCache = initInfo
        }

    override fun savePrefsInitNew(initInfo: InitNewResult): Completable =
        Completable.fromAction {
            prefs.initNewCache = initInfo
        }
}