package cn.lcsw.diningpos.http.service

data class ServiceManager(
    val payService: PayService,
    val updateService: UpdateService
)
