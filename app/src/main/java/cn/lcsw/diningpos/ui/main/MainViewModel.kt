package cn.lcsw.diningpos.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import arrow.core.Option
import arrow.core.none
import arrow.core.some
import cn.lcsw.diningpos.base.SimpleViewState
import cn.lcsw.diningpos.common.loadings.CommonLoadingState
import cn.lcsw.diningpos.entity.*
import cn.lcsw.diningpos.http.globalHandleError
import cn.lcsw.mvvm.base.viewmodel.BaseViewModel
import cn.lcsw.mvvm.ext.arrow.whenNotNull
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import cn.lcsw.mvvm.util.SingletonHolderSingleArg
import com.uber.autodispose.autoDisposable
import retrofit2.HttpException

class MainViewModel(
    private val repo: MainDataSourceRepository
) : BaseViewModel() {

    val loadingLayout: MutableLiveData<CommonLoadingState> = MutableLiveData()
    val error: MutableLiveData<Option<Throwable>> = MutableLiveData()
    val isShowMessage: MutableLiveData<Boolean> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()

    init {
        error.toReactiveStream()
            .map { errorOption ->
                errorOption.flatMap {
                    when (it) {
                        is Errors.EmptyInputError -> "用户名或密码不能为空".some()
                        is HttpException -> "网络错误".some()
                        is Errors.EmptyResultsError -> "请输入用户名或密码".some()
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
        error: Option<Throwable> = none()
    ) {
        this.loadingLayout.postValue(loadingLayout)
        this.error.postValue(error)
    }

    private fun applyErrorState(
        isShowMessage: Boolean = false,
        errorMessage: Option<String> = none()
    ) {
        this.isShowMessage.postValue(isShowMessage)
        errorMessage.whenNotNull { this.errorMessage.value = it }
    }
}

class MainViewModelFactory(
    private val repo: MainDataSourceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        MainViewModel(repo) as T

    companion object :
        SingletonHolderSingleArg<MainViewModelFactory, MainDataSourceRepository>(::MainViewModelFactory)
}