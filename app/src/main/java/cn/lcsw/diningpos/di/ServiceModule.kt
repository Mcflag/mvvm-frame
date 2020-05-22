package cn.lcsw.diningpos.di

import cn.lcsw.diningpos.http.service.ServiceManager
import cn.lcsw.diningpos.http.service.PayService
import cn.lcsw.diningpos.http.service.UpdateService
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import retrofit2.Retrofit

private const val SERVICE_MODULE_TAG = "serviceModule"

val serviceModule = Kodein.Module(SERVICE_MODULE_TAG) {

    bind<ServiceManager>() with singleton {
        ServiceManager(instance(), instance())
    }

    bind<PayService>() with singleton {
        instance<Retrofit>().create(PayService::class.java)
    }

    bind<UpdateService>() with singleton {
        instance<Retrofit>(WX_UPDATE_RETROFIT).create(UpdateService::class.java)
    }
}