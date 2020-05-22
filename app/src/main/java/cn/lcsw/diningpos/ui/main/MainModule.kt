package cn.lcsw.diningpos.ui.main

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import org.kodein.di.Kodein
import org.kodein.di.android.x.AndroidLifecycleScope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

const val MAIN_MODULE_TAG = "MAIN_MODULE_TAG"

val mainKodeinModule = Kodein.Module(MAIN_MODULE_TAG) {
    bind<MainViewModel>() with scoped<AppCompatActivity>(AndroidLifecycleScope).singleton {
        ViewModelProviders.of(context, MainViewModelFactory.getInstance(instance())).get(MainViewModel::class.java)
    }

    bind<CommonLoadingViewModel>() with scoped<AppCompatActivity>(AndroidLifecycleScope).singleton {
        CommonLoadingViewModel.instance()
    }

    bind<MainRemoteDataSource>() with singleton {
        MainRemoteDataSource(instance(), instance())
    }

    bind<MainLocalDataSource>() with singleton {
        MainLocalDataSource(instance())
    }

    bind<MainDataSourceRepository>() with singleton {
        MainDataSourceRepository(instance(), instance())
    }
}