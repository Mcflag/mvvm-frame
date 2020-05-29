package cn.lcsw.diningpos.ui.test

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import arrow.core.Option
import arrow.core.none
import arrow.core.some
import cn.lcsw.diningpos.base.SimpleViewState
import cn.lcsw.diningpos.common.loadings.CommonLoadingState
import cn.lcsw.diningpos.entity.Errors
import cn.lcsw.diningpos.entity.InitNewResult
import cn.lcsw.diningpos.entity.InitResult
import cn.lcsw.diningpos.http.globalHandleError
import cn.lcsw.mvvm.base.viewmodel.BaseViewModel
import cn.lcsw.mvvm.ext.arrow.whenNotNull
import cn.lcsw.mvvm.ext.livedata.toReactiveStream
import cn.lcsw.mvvm.util.SingletonHolderSingleArg
import com.uber.autodispose.autoDisposable
import retrofit2.HttpException
import java.net.UnknownHostException

class FlashViewModel(
    private val repo: FlashDataSourceRepository
) : BaseViewModel() {

    val loadingLayout: MutableLiveData<CommonLoadingState> = MutableLiveData()
    val error: MutableLiveData<Option<Throwable>> = MutableLiveData()
    val isShowMessage: MutableLiveData<Boolean> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()

    val initInfo: MutableLiveData<InitResult> = MutableLiveData()
    val initNewInfo: MutableLiveData<InitNewResult> = MutableLiveData()

    init {
        error.toReactiveStream()
            .map { errorOption ->
                errorOption.flatMap {
                    when (it) {
                        is HttpException -> "网络错误".some()
                        is Errors.ResultError -> (it.message ?: "服务器返回错误").some()
                        is UnknownHostException -> "网络连接失败".some()
                        else -> "未知错误".some()
                    }
                }
            }
            .autoDisposable(this)
            .subscribe {
                applyErrorState(true, it)
            }
    }

    fun init() {
        repo.init()
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<InitResult>(it)
                }, {
                    if (it.return_code != "01" || it.result_code != "01") {
                        SimpleViewState.error(Errors.ResultError(it.return_msg ?: "服务器返回错误"))
                    } else {
                        SimpleViewState.result(it)
                    }
                })
            }
            .startWith(SimpleViewState.loading())
            .startWith(SimpleViewState.idle())
            .onErrorReturn { SimpleViewState.error(it) }
            .autoDisposable(this)
            .subscribe { state ->
                when (state) {
                    is SimpleViewState.Refreshing -> applyState(loadingLayout = CommonLoadingState.LOADING)
                    is SimpleViewState.Idle -> applyState()
                    is SimpleViewState.Error -> applyState(
                        loadingLayout = CommonLoadingState.ERROR,
                        error = state.error.some()
                    )
                    is SimpleViewState.Result -> state.result.apply {
                        if (this.return_code == "01" && this.result_code == "01") {
                            applyState(info = state.result.some())
                        } else {
                            applyState(
                                loadingLayout = CommonLoadingState.ERROR,
                                error = Errors.ResultError(this.return_msg ?: "服务器返回错误").some()
                            )
                        }
                    }
                }
            }
    }

    fun initNew(sim: String?) {
        repo.initNew(sim)
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<InitNewResult>(it)
                }, {
                    if (it.return_code != "01" || it.result_code != "01") {
                        SimpleViewState.error(Errors.ResultError(it.return_msg ?: "服务器返回错误"))
                    } else {
                        SimpleViewState.result(it)
                    }
                })
            }
            .startWith(SimpleViewState.loading())
            .startWith(SimpleViewState.idle())
            .onErrorReturn { SimpleViewState.error(it) }
            .autoDisposable(this)
            .subscribe { state ->
                when (state) {
                    is SimpleViewState.Refreshing -> applyState(loadingLayout = CommonLoadingState.LOADING)
                    is SimpleViewState.Idle -> applyState()
                    is SimpleViewState.Error -> applyState(
                        loadingLayout = CommonLoadingState.ERROR,
                        error = state.error.some()
                    )
                    is SimpleViewState.Result -> state.result.apply {
                        if (this.return_code == "01" && this.result_code == "01") {
                            applyState(infoNew = state.result.some())
                        } else {
                            applyState(
                                loadingLayout = CommonLoadingState.ERROR,
                                error = Errors.ResultError(this.return_msg ?: "服务器返回错误").some()
                            )
                        }
                    }
                }
            }
    }

    private fun applyState(
        loadingLayout: CommonLoadingState = CommonLoadingState.IDLE,
        info: Option<InitResult> = none(),
        infoNew: Option<InitNewResult> = none(),
        error: Option<Throwable> = none()
    ) {
        this.loadingLayout.postValue(loadingLayout)
        this.error.postValue(error)

        this.initInfo.postValue(info.orNull())
        this.initNewInfo.postValue(infoNew.orNull())
    }

    private fun applyErrorState(
        isShowMessage: Boolean = false,
        errorMessage: Option<String> = none()
    ) {
        this.isShowMessage.postValue(isShowMessage)
        errorMessage.whenNotNull { this.errorMessage.value = it }
    }
}

class FlashViewModelFactory(
    private val repo: FlashDataSourceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        FlashViewModel(repo) as T

    companion object :
        SingletonHolderSingleArg<FlashViewModelFactory, FlashDataSourceRepository>(::FlashViewModelFactory)
}