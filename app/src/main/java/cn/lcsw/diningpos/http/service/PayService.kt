package cn.lcsw.diningpos.http.service

import cn.lcsw.diningpos.entity.*
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface PayService {
    @POST("tabapp/100/base/init")
    fun initDevice(@Body body: InitRequest): Flowable<InitResult>

    @POST("openpos/100/canteenposinit")
    fun initDeviceNew(@Body body: InitNewRequest): Flowable<InitNewResult>

    @POST("pay/110/faceinfo")
    fun getAuthInfo(@Body body: AuthInfoRequest): Flowable<AuthInfoResult>

    @POST("pay/110/facepay")
    fun facePay(@Body body: BarCodePayRequest): Flowable<BarCodePayResult>

    @POST("pay/110/barcodepay")
    fun barCodePay(@Body body: BarCodePayRequest): Flowable<BarCodePayResult>

    @POST("pay/110/qrpay")
    fun qrPay(@Body body: BarCodePayRequest): Flowable<BarCodePayResult>

    @POST("pay/110/query")
    fun queryResult(@Body body: PayQueryRequest): Flowable<PayQueryResult>

    @POST("pay/110/refund")
    fun refund(@Body body: RefundRequest): Flowable<RefundResult>
}