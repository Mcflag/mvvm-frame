package cn.lcsw.diningpos.ui.setting

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import org.kodein.di.Kodein
import org.kodein.di.android.x.AndroidLifecycleScope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

const val SETTINGS_MODULE_TAG = "SETTINGS_MODULE_TAG"

val settingsKodeinModule = Kodein.Module(SETTINGS_MODULE_TAG) {
    bind<SettingsViewModel>() with scoped<AppCompatActivity>(AndroidLifecycleScope).singleton {
        ViewModelProviders.of(context, SettingsViewModelFactory.getInstance(instance()))
            .get(SettingsViewModel::class.java)
    }

    bind<CommonLoadingViewModel>() with scoped<AppCompatActivity>(AndroidLifecycleScope).singleton {
        CommonLoadingViewModel.instance()
    }

    bind<SettingsRemoteDataSource>() with singleton {
        SettingsRemoteDataSource(instance())
    }

    bind<SettingsLocalDataSource>() with singleton {
        SettingsLocalDataSource(instance())
    }

    bind<SettingsDataSourceRepository>() with singleton {
        SettingsDataSourceRepository(instance(), instance())
    }
}