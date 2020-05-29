package cn.lcsw.diningpos.ui.test

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import org.kodein.di.Kodein
import org.kodein.di.android.x.AndroidLifecycleScope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

const val FLASH_MODULE_TAG = "FLASH_MODULE_TAG"

val flashKodeinModule = Kodein.Module(FLASH_MODULE_TAG) {
    bind<FlashViewModel>() with scoped<AppCompatActivity>(AndroidLifecycleScope).singleton {
        ViewModelProviders.of(context, FlashViewModelFactory.getInstance(instance())).get(FlashViewModel::class.java)
    }

    bind<CommonLoadingViewModel>() with scoped<AppCompatActivity>(AndroidLifecycleScope).singleton {
        CommonLoadingViewModel.instance()
    }

    bind<FlashRemoteDataSource>() with singleton {
        FlashRemoteDataSource(instance())
    }

    bind<FlashLocalDataSource>() with singleton {
        FlashLocalDataSource(instance())
    }

    bind<FlashDataSourceRepository>() with singleton {
        FlashDataSourceRepository(instance(), instance())
    }
}