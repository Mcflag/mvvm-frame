package cn.lcsw.diningpos.ui.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import arrow.core.Option
import arrow.core.none
import arrow.core.some
import cn.lcsw.diningpos.common.loadings.CommonLoadingState
import cn.lcsw.diningpos.entity.Errors
import cn.lcsw.diningpos.entity.InitResult
import cn.lcsw.mvvm.base.viewmodel.BaseViewModel
import cn.lcsw.mvvm.ext.arrow.whenNotNull
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import cn.lcsw.mvvm.util.SingletonHolderSingleArg
import com.uber.autodispose.autoDisposable
import retrofit2.HttpException

class SettingsViewModel(
    private val repo: SettingsDataSourceRepository
) : BaseViewModel() {

    val loadingLayout: MutableLiveData<CommonLoadingState> = MutableLiveData()
    val error: MutableLiveData<Option<Throwable>> = MutableLiveData()
    val isShowMessage: MutableLiveData<Boolean> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()

    val initInfo: MutableLiveData<InitResult> = MutableLiveData()

    init {
        error.toReactiveStream()
            .map { errorOption ->
                errorOption.flatMap {
                    when (it) {
                        is HttpException -> "网络错误".some()
                        is Errors.ResultError -> (it.message ?: "服务器返回错误").some()
                        else -> "未知错误".some()
                    }
                }
            }
            .autoDisposable(this)
            .subscribe {
                applyErrorState(true, it)
            }
    }

    private fun applyState(
        loadingLayout: CommonLoadingState = CommonLoadingState.IDLE,
        info: Option<InitResult> = none(),
        error: Option<Throwable> = none()
    ) {
        this.loadingLayout.postValue(loadingLayout)
        this.error.postValue(error)

        this.initInfo.postValue(info.orNull())
    }

    private fun applyErrorState(
        isShowMessage: Boolean = false,
        errorMessage: Option<String> = none()
    ) {
        this.isShowMessage.postValue(isShowMessage)
        errorMessage.whenNotNull { this.errorMessage.value = it }
    }
}

class SettingsViewModelFactory(
    private val repo: SettingsDataSourceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        SettingsViewModel(repo) as T

    companion object :
        SingletonHolderSingleArg<SettingsViewModelFactory, SettingsDataSourceRepository>(::SettingsViewModelFactory)
}