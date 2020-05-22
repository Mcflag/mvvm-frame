package cn.lcsw.diningpos.ui.trade_query

import arrow.core.Either
import cn.lcsw.diningpos.entity.Errors
import cn.lcsw.diningpos.entity.RefundRequest
import cn.lcsw.diningpos.entity.RefundResult
import cn.lcsw.diningpos.http.service.ServiceManager
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.utils.KeySign
import cn.lcsw.diningpos.utils.SignBeanUtil
import cn.lcsw.mvvm.base.repository.BaseRepositoryBoth
import cn.lcsw.mvvm.base.repository.ILocalDataSource
import cn.lcsw.mvvm.base.repository.IRemoteDataSource
import cn.lcsw.mvvm.util.RxSchedulers
import io.reactivex.Flowable
import java.text.SimpleDateFormat
import java.util.*

interface IQueryRemoteDataSource : IRemoteDataSource {
    fun refund(outTradeNo: String, totalFee: String): Flowable<Either<Errors, RefundResult>>
}

interface IQueryLocalDataSource : ILocalDataSource {
}

class QueryDataSourceRepository(
    remoteDataSource: IQueryRemoteDataSource,
    localDataSource: IQueryLocalDataSource
) : BaseRepositoryBoth<IQueryRemoteDataSource, IQueryLocalDataSource>(remoteDataSource, localDataSource) {

    fun refund(outTradeNo: String, totalFee: String): Flowable<Either<Errors, RefundResult>> =
        remoteDataSource.refund(outTradeNo, totalFee)
            .doOnNext { either ->
                either.fold({

                }, {

                })
            }
}

class QueryRemoteDataSource(
    private val serviceManager: ServiceManager, private val prefs: PrefsHelper
) : IQueryRemoteDataSource {
    override fun refund(
        outTradeNo: String,
        totalFee: String
    ): Flowable<Either<Errors, RefundResult>> {
        val req = RefundRequest()

        req.out_trade_no = outTradeNo
        req.refund_fee = totalFee
        req.merchant_no = prefs.initNewCache.merchant_no
        req.terminal_id = prefs.initNewCache.terminal_id

        req.terminal_trace = KeySign.getRandomUUID()
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        val curDate = Date(System.currentTimeMillis())
        val str = formatter.format(curDate)
        req.terminal_time = str

        req.key_sign = SignBeanUtil.sginAccessToken(req, prefs.initNewCache.access_token)
        return serviceManager.payService
            .refund(req)
            .subscribeOn(RxSchedulers.io)
            .map {
                Either.right(it)
            }
    }
}

class QueryLocalDataSource(
    private val prefs: PrefsHelper
) : IQueryLocalDataSource {
}