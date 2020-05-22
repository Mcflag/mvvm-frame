package cn.lcsw.diningpos.ui.pay

import arrow.core.Either
import arrow.core.getOrElse
import cn.lcsw.diningpos.entity.*
import cn.lcsw.diningpos.http.service.ServiceManager
import cn.lcsw.diningpos.manager.PrefsHelper
import cn.lcsw.diningpos.utils.KeySign
import cn.lcsw.diningpos.utils.SignBeanUtil
import cn.lcsw.mvvm.base.repository.BaseRepositoryBoth
import cn.lcsw.mvvm.base.repository.ILocalDataSource
import cn.lcsw.mvvm.base.repository.IRemoteDataSource
import cn.lcsw.mvvm.util.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Flowable
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

interface IPayRemoteDataSource : IRemoteDataSource {
    fun getWxsign(): Flowable<Either<Errors, SignResult>>

    fun getAuthInfo(rawData: String): Flowable<Either<Errors, AuthInfoResult>>

    fun facePay(
        authCode: String,
        openid: String,
        outTradeNo: String,
        totalFee: String,
        orderBody: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ): Flowable<Either<Errors, BarCodePayResult>>

    fun barcodePay(
        authCode: String,
        totalFee: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ): Flowable<Either<Errors, BarCodePayResult>>

    fun qrPay(
        totalFee: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ): Flowable<Either<Errors, BarCodePayResult>>

    fun query(outTradeNo: String): Flowable<Either<Errors, PayQueryResult>>

    fun query(payTrace: String, payTime: String): Flowable<Either<Errors, PayQueryResult>>

    fun querySum(
        begin_time: String,
        end_time: String,
        pay_status_code: String
    ): Flowable<Either<Errors, QuerySumResult>>
}

interface IPayLocalDataSource : ILocalDataSource {
    fun savePrefsSign(sign: SignResult): Completable

    fun savePrefsAuth(authInfo: AuthInfoResult): Completable

    fun savePrefsPay(pay: BarCodePayResult): Completable

    fun savePrefsQuery(query: PayQueryResult): Completable
}

class PayDataSourceRepository(
    remoteDataSource: IPayRemoteDataSource,
    localDataSource: IPayLocalDataSource
) : BaseRepositoryBoth<IPayRemoteDataSource, IPayLocalDataSource>(
    remoteDataSource,
    localDataSource
) {
    fun getWxsign(): Flowable<Either<Errors, SignResult>> =
        remoteDataSource.getWxsign()
            .doOnNext { either ->
                either.fold({

                }, {

                })
            }.flatMap {
                localDataSource.savePrefsSign(it.getOrElse { SignResult() })
                    .andThen(Flowable.just(it))
            }

    fun getAuthInfo(rawData: String): Flowable<Either<Errors, AuthInfoResult>> =
        remoteDataSource.getAuthInfo(rawData)
            .doOnNext { either ->
                either.fold({

                }, {

                })
            }.flatMap {
                localDataSource.savePrefsAuth(it.getOrElse { AuthInfoResult() })
                    .andThen(Flowable.just(it))
            }

    fun facePay(
        authCode: String,
        openid: String,
        outTradeNo: String,
        totalFee: String,
        orderBody: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ): Flowable<Either<Errors, BarCodePayResult>> =
        remoteDataSource.facePay(
            authCode,
            openid,
            outTradeNo,
            totalFee,
            orderBody,
            terminalTrace,
            terminalTime,
            attach
        ).doOnNext { either ->
                either.fold({

                }, {

                })
            }

    fun barcodePay(
        authCode: String,
        totalFee: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ): Flowable<Either<Errors, BarCodePayResult>> =
        remoteDataSource.barcodePay(authCode, totalFee, terminalTrace, terminalTime, attach)
            .doOnNext { either ->
                either.fold({

                }, {

                })
            }

    fun qrPay(
        totalFee: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ): Flowable<Either<Errors, BarCodePayResult>> =
        remoteDataSource.qrPay(totalFee, terminalTrace, terminalTime, attach)
            .doOnNext { either ->
                either.fold({

                }, {

                })
            }

    fun queryResult(outTradeNo: String): Flowable<Either<Errors, PayQueryResult>> =
        remoteDataSource.query(outTradeNo)
            .doOnNext { either ->
                either.fold({

                }, {

                })
            }

    fun queryResult(payTrace: String, payTime: String): Flowable<Either<Errors, PayQueryResult>> =
        remoteDataSource.query(payTrace, payTime)
            .doOnNext { either ->
                either.fold({

                }, {

                })
            }

    fun querySum(begin_time: String, end_time: String, pay_status_code: String): Flowable<Either<Errors, QuerySumResult>> =
        remoteDataSource.querySum(begin_time, end_time, pay_status_code)
            .doOnNext { either ->
                either.fold({

                }, {

                })
            }

    fun savePrefsPay(pay: BarCodePayResult): Completable =
        localDataSource.savePrefsPay(pay)

    fun savePrefsQuery(query: PayQueryResult): Completable =
        localDataSource.savePrefsQuery(query)
}

class PayRemoteDataSource(
    private val serviceManager: ServiceManager,
    private val prefs: PrefsHelper
) : IPayRemoteDataSource {

    override fun querySum(
        begin_time: String,
        end_time: String,
        pay_status_code: String
    ): Flowable<Either<Errors, QuerySumResult>> {
        val req = QuerySumRequest()
        req.merchant_no = prefs.initNewCache.merchant_no
        req.terminal_no = prefs.initNewCache.terminal_id
        req.trace_no = KeySign.getRandomUUID()

        req.begin_time = begin_time
        req.end_time = end_time
        req.pay_status_code = pay_status_code

        req.key_sign = SignBeanUtil.sginAccessToken(req, prefs.initNewCache.access_token)

        return serviceManager.updateService
            .querySum(req)
            .subscribeOn(RxSchedulers.io)
            .map {
                Either.right(it)
            }
    }

    override fun getWxsign(): Flowable<Either<Errors, SignResult>> {
        val req = BaseRequest()
        req.merchant_no = prefs.initNewCache.merchant_no
        req.terminal_no = prefs.initNewCache.terminal_id
        req.trace_no = KeySign.getRandomUUID()

        req.key_sign = SignBeanUtil.sginAccessToken(req, prefs.initNewCache.access_token)

        return serviceManager.updateService
            .getWxsign(req)
            .subscribeOn(RxSchedulers.io)
            .map {
                Either.right(it)
            }
    }

    override fun getAuthInfo(rawData: String): Flowable<Either<Errors, AuthInfoResult>> {
        val req = AuthInfoRequest()
        req.merchant_no = prefs.initNewCache.merchant_no
        req.terminal_no = prefs.initNewCache.terminal_id
        req.trace_no = KeySign.getRandomUUID()
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        val curDate = Date(System.currentTimeMillis())
        val str = formatter.format(curDate)
        req.terminal_time = str

        req.pay_ver = "110"
        req.pay_type = "010"
        req.rawdata = rawData
        req.key_sign = SignBeanUtil.sginAccessToken(req, prefs.initNewCache.access_token)

        return serviceManager.payService
            .getAuthInfo(req)
            .subscribeOn(RxSchedulers.io)
            .map {
                Either.right(it)
            }
    }

    override fun facePay(
        authCode: String,
        openid: String,
        outTradeNo: String,
        totalFee: String,
        orderBody: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ): Flowable<Either<Errors, BarCodePayResult>> {
        val req = BarCodePayRequest()
        req.pay_type = "010" //固定微信
        req.service_id = "016"
        req.auth_no = authCode
        req.open_id = openid
        req.out_trade_no = outTradeNo
        req.total_fee = totalFee
        req.order_body = orderBody
        req.merchant_no = prefs.initNewCache.merchant_no
        req.terminal_id = prefs.initNewCache.terminal_id
        req.attach = attach

        req.terminal_trace = terminalTrace
        req.terminal_time = terminalTime

        req.key_sign = SignBeanUtil.sginAccessToken(req, prefs.initNewCache.access_token)

        return serviceManager.payService
            .facePay(req)
            .subscribeOn(RxSchedulers.io)
            .map {
                Either.right(it)
            }
    }

    override fun barcodePay(
        authCode: String,
        totalFee: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ): Flowable<Either<Errors, BarCodePayResult>> {
        val req = BarCodePayRequest()
        req.pay_type = "000"
        req.service_id = "010"
        req.auth_no = authCode
        req.total_fee = totalFee
        req.order_body = ""
        req.merchant_no = prefs.initNewCache.merchant_no
        req.terminal_id = prefs.initNewCache.terminal_id

        req.attach = attach

        req.terminal_trace = terminalTrace
        req.terminal_time = terminalTime

        req.key_sign = SignBeanUtil.sginAccessToken(req, prefs.initNewCache.access_token)

        return serviceManager.payService
            .barCodePay(req)
            .subscribeOn(RxSchedulers.io)
            .map {
                Either.right(it)
            }
    }

    override fun qrPay(
        totalFee: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ): Flowable<Either<Errors, BarCodePayResult>> {
        val req = BarCodePayRequest()
        req.pay_type = "000"
        req.service_id = "010"
        req.total_fee = totalFee
        req.order_body = ""
        req.merchant_no = prefs.initNewCache.merchant_no
        req.terminal_id = prefs.initNewCache.terminal_id

        req.attach = attach

        req.terminal_trace = terminalTrace
        req.terminal_time = terminalTime

        req.key_sign = SignBeanUtil.sginAccessToken(req, prefs.initNewCache.access_token)

        return serviceManager.payService
            .qrPay(req)
            .subscribeOn(RxSchedulers.io)
            .map {
                Either.right(it)
            }
    }

    override fun query(outTradeNo: String): Flowable<Either<Errors, PayQueryResult>> {
        val req = PayQueryRequest()
        req.pay_type = "000"
        req.out_trade_no = outTradeNo
        req.merchant_no = prefs.initNewCache.merchant_no
        req.terminal_id = prefs.initNewCache.terminal_id
        req.terminal_trace = KeySign.getRandomUUID()
        req.pay_trace = req.terminal_trace
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        val curDate = Date(System.currentTimeMillis())
        val str = formatter.format(curDate)
        req.terminal_time = str
        req.pay_time = str

        req.key_sign = SignBeanUtil.sginAccessToken(req, prefs.initNewCache.access_token)

        return serviceManager.payService
            .queryResult(req)
            .subscribeOn(RxSchedulers.io)
            .map {
                Either.right(it)
            }
    }

    override fun query(
        payTrace: String,
        payTime: String
    ): Flowable<Either<Errors, PayQueryResult>> {
        val req = PayQueryRequest()
        req.pay_type = "000"
        req.out_trade_no = ""
        req.merchant_no = prefs.initNewCache.merchant_no
        req.terminal_id = prefs.initNewCache.terminal_id
        req.terminal_trace = KeySign.getRandomUUID()
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        val curDate = Date(System.currentTimeMillis())
        val str = formatter.format(curDate)
        req.terminal_time = str

        req.pay_trace = payTrace
        req.pay_time = payTime

        req.key_sign = SignBeanUtil.sginAccessToken(req, prefs.initNewCache.access_token)

        return serviceManager.payService
            .queryResult(req)
            .subscribeOn(RxSchedulers.io)
            .map {
                Either.right(it)
            }
    }
}

class PayLocalDataSource(
    private val prefs: PrefsHelper
) : IPayLocalDataSource {
    override fun savePrefsSign(sign: SignResult): Completable =
        Completable.fromAction {
            prefs.signResult = sign
            prefs.signExpire = System.currentTimeMillis()
        }

    override fun savePrefsAuth(authInfo: AuthInfoResult): Completable =
        Completable.fromAction {
            prefs.authCache = authInfo
        }

    override fun savePrefsPay(pay: BarCodePayResult): Completable =
        Completable.fromAction {
            prefs.payResult = pay
            prefs.outTradeNo = pay.out_trade_no ?: ""
            prefs.channelTradeNo = pay.channel_trade_no ?: ""
        }

    override fun savePrefsQuery(query: PayQueryResult): Completable =
        Completable.fromAction {
            prefs.queryResult = query
            prefs.outTradeNo = query.out_trade_no ?: ""
            prefs.channelTradeNo = query.channel_trade_no ?: ""
        }
}