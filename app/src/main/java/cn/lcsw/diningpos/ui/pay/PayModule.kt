package cn.lcsw.diningpos.ui.pay

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import org.kodein.di.Kodein
import org.kodein.di.android.x.AndroidLifecycleScope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

const val PAY_MODULE_TAG = "PAY_MODULE_TAG"

val payKodeinModule = Kodein.Module(PAY_MODULE_TAG) {
    bind<PayViewModel>() with scoped<AppCompatActivity>(AndroidLifecycleScope).singleton {
        ViewModelProviders.of(context, PayViewModelFactory.getInstance(instance())).get(PayViewModel::class.java)
    }

    bind<QRPayViewModel>() with scoped<AppCompatActivity>(AndroidLifecycleScope).singleton {
        ViewModelProviders.of(context, QRPayViewModelFactory.getInstance(instance())).get(QRPayViewModel::class.java)
    }

    bind<CommonLoadingViewModel>() with scoped<AppCompatActivity>(AndroidLifecycleScope).singleton {
        CommonLoadingViewModel.instance()
    }

    bind<PayRemoteDataSource>() with singleton {
        PayRemoteDataSource(instance(), instance())
    }

    bind<PayLocalDataSource>() with singleton {
        PayLocalDataSource(instance())
    }

    bind<PayDataSourceRepository>() with singleton {
        PayDataSourceRepository(instance(), instance())
    }
}