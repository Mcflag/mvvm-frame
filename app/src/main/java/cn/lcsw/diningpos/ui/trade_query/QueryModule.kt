package cn.lcsw.diningpos.ui.trade_query

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import cn.lcsw.diningpos.common.loadings.CommonLoadingViewModel
import org.kodein.di.Kodein
import org.kodein.di.android.x.AndroidLifecycleScope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

const val QUERY_MODULE_TAG = "QUERY_MODULE_TAG"

val queryKodeinModule = Kodein.Module(QUERY_MODULE_TAG) {
    bind<QueryViewModel>() with scoped<AppCompatActivity>(AndroidLifecycleScope).singleton {
        ViewModelProviders.of(context, QueryViewModelFactory.getInstance(instance()))
            .get(QueryViewModel::class.java)
    }

    bind<CommonLoadingViewModel>() with scoped<AppCompatActivity>(AndroidLifecycleScope).singleton {
        CommonLoadingViewModel.instance()
    }

    bind<QueryRemoteDataSource>() with singleton {
        QueryRemoteDataSource(instance(), instance())
    }

    bind<QueryLocalDataSource>() with singleton {
        QueryLocalDataSource(instance())
    }

    bind<QueryDataSourceRepository>() with singleton {
        QueryDataSourceRepository(instance(), instance())
    }
}