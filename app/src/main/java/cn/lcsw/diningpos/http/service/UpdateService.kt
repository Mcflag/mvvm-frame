package cn.lcsw.diningpos.http.service

import cn.lcsw.diningpos.entity.BaseRequest
import cn.lcsw.diningpos.entity.QuerySumRequest
import cn.lcsw.diningpos.entity.QuerySumResult
import cn.lcsw.diningpos.entity.SignResult
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface UpdateService {
    @POST("wxsign/reportsign")
    fun getWxsign(@Body body: BaseRequest): Flowable<SignResult>

    @POST("face/110/trade/gettradedetail_all")
    fun querySum(@Body body: QuerySumRequest): Flowable<QuerySumResult>
}