package cn.lcsw.diningpos.ui.trade_query

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
import java.net.UnknownHostException

class QueryViewModel(
    private val repo: QueryDataSourceRepository
) : BaseViewModel() {

    val loadingLayout: MutableLiveData<CommonLoadingState> = MutableLiveData()
    val error: MutableLiveData<Option<Throwable>> = MutableLiveData()
    val isShowMessage: MutableLiveData<Boolean> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()

    val refundResult: MutableLiveData<RefundResult> = MutableLiveData()

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

    fun refund(outTradeNo: String, refundFee: String) {
        repo.refund(outTradeNo, refundFee)
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<RefundResult>(it)
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
                            applyState(refund = state.result.some())
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
        refund: Option<RefundResult> = none(),
        error: Option<Throwable> = none()
    ) {
        this.loadingLayout.postValue(loadingLayout)
        this.error.postValue(error)

        this.refundResult.postValue(refund.orNull())
    }

    private fun applyErrorState(
        isShowMessage: Boolean = false,
        errorMessage: Option<String> = none()
    ) {
        this.isShowMessage.postValue(isShowMessage)
        errorMessage.whenNotNull { this.errorMessage.value = it }
    }
}

class QueryViewModelFactory(
    private val repo: QueryDataSourceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        QueryViewModel(repo) as T

    companion object :
        SingletonHolderSingleArg<QueryViewModelFactory, QueryDataSourceRepository>(::QueryViewModelFactory)
}