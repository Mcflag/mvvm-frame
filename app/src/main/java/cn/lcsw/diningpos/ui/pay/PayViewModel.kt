package cn.lcsw.diningpos.ui.pay

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
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import retrofit2.HttpException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class PayViewModel(
    private val repo: PayDataSourceRepository
) : BaseViewModel() {

    val loadingLayout: MutableLiveData<CommonLoadingState> = MutableLiveData()
    val error: MutableLiveData<Option<Throwable>> = MutableLiveData()
    val isShowMessage: MutableLiveData<Boolean> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()

    val authInfo: MutableLiveData<AuthInfoResult> = MutableLiveData()
    val facePay: MutableLiveData<BarCodePayResult> = MutableLiveData()
    val barcodePay: MutableLiveData<BarCodePayResult> = MutableLiveData()
    val queryResult: MutableLiveData<PayQueryResult> = MutableLiveData()

    val sumResult: MutableLiveData<QuerySumResult> = MutableLiveData()
    val refundResult: MutableLiveData<QuerySumResult> = MutableLiveData()
    val noResult: MutableLiveData<Boolean> = MutableLiveData()

    val signInfo: MutableLiveData<SignResult> = MutableLiveData()

    var mDispose: Disposable? = null

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

    fun getWxsign() {
        repo.getWxsign()
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<SignResult>(it)
                }, {
                    if (it.return_code != "01" || it.result_code != "01") {
                        SimpleViewState.error(Errors.ResultError(it.return_msg ?: "服务器返回错误"))
                    } else {
                        SimpleViewState.result(it)
                    }
                })
            }
            .startWith(SimpleViewState.idle())
            .onErrorReturn { SimpleViewState.error(it) }
            .autoDisposable(this)
            .subscribe { state ->
                when (state) {
                    is SimpleViewState.Refreshing -> applyState(loadingLayout = CommonLoadingState.LOADING)
                    is SimpleViewState.Idle -> applyState()
                    is SimpleViewState.Error -> applyState(
                        loadingLayout = CommonLoadingState.ERROR
                    )
                    is SimpleViewState.Result -> state.result.apply {
                        if (this.return_code == "01" && this.result_code == "01") {
                            applyState(signInfo = state.result.some())
                        } else {
                            applyState(
                                loadingLayout = CommonLoadingState.ERROR
                            )
                        }
                    }
                }
            }
    }

    fun getAuthInfo(rawData: String) {
        repo.getAuthInfo(rawData)
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<AuthInfoResult>(it)
                }, {
                    SimpleViewState.result(it)
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
                    is SimpleViewState.Result -> applyState(auth = state.result.some())
                }
            }
    }

    fun facePay(
        authCode: String,
        openid: String,
        outTradeNo: String,
        totalFee: String,
        orderBody: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ) {
        repo.facePay(
            authCode,
            openid,
            outTradeNo,
            totalFee,
            orderBody,
            terminalTrace,
            terminalTime,
            attach
        )
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<BarCodePayResult>(it)
                }, {
                    SimpleViewState.result(it)
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

                        if (this.return_code == "01") {
                            if (this.result_code == "01") {
                                repo.savePrefsPay(this)
                                applyState(face = state.result.some())
                            } else if (this.result_code == "03") {
                                if (this.out_trade_no.isNullOrEmpty()) {
                                    startQuery(terminalTrace, terminalTime)
                                } else {
                                    startQuery(this.out_trade_no!!)
                                }
                            } else {
                                applyState(
                                    loadingLayout = CommonLoadingState.ERROR,
                                    error = Errors.ResultError(this.return_msg ?: "服务器返回错误").some()
                                )
                            }
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

    fun barcodePay(
        authCode: String,
        totalFee: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ) {
        repo.barcodePay(authCode, totalFee, terminalTrace, terminalTime, attach)
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<BarCodePayResult>(it)
                }, {
                    SimpleViewState.result(it)
                })
            }
            .startWith(SimpleViewState.loading())
            .startWith(SimpleViewState.idle())
            .onErrorReturn { SimpleViewState.error(it) }
            .autoDisposable(this)
            .subscribe({ state ->
                when (state) {
                    is SimpleViewState.Refreshing -> applyState(loadingLayout = CommonLoadingState.LOADING)
                    is SimpleViewState.Idle -> applyState()
                    is SimpleViewState.Error -> applyState(
                        loadingLayout = CommonLoadingState.ERROR,
                        error = state.error.some()
                    )
                    is SimpleViewState.Result -> state.result.apply {
                        if (this.return_code == "01") {
                            if (this.result_code == "01") {
                                repo.savePrefsPay(this)
                                applyState(barcode = state.result.some())
                            } else if (this.result_code == "03") {
                                if (this.out_trade_no.isNullOrEmpty()) {
                                    startQuery(terminalTrace, terminalTime)
                                } else {
                                    startQuery(this.out_trade_no!!)
                                }
                            } else {
                                applyState(
                                    loadingLayout = CommonLoadingState.ERROR,
                                    error = Errors.ResultError(this.return_msg ?: "服务器返回错误").some()
                                )
                            }
                        } else {
                            applyState(
                                loadingLayout = CommonLoadingState.ERROR,
                                error = Errors.ResultError(this.return_msg ?: "服务器返回错误").some()
                            )
                        }
                    }

                }
            }, {
                applyState(
                    loadingLayout = CommonLoadingState.ERROR,
                    error = it.some()
                )
            })
    }

    fun qrPay(
        totalFee: String,
        terminalTrace: String,
        terminalTime: String,
        attach: String
    ) {
        repo.qrPay(totalFee, terminalTrace, terminalTime, attach)
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<BarCodePayResult>(it)
                }, {
                    SimpleViewState.result(it)
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
                            applyState(barcode = state.result.some())
                            startQuery(terminalTrace, terminalTime)
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

    fun querySum(
        begin_time: String,
        end_time: String
    ) {
        repo.querySum(begin_time, end_time, "1")
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<QuerySumResult>(it)
                }, {
                    SimpleViewState.result(it)
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
                            applyStateSum(sum = state.result.some())
                        } else if (this.return_code == "01" && this.result_code == "02") {
                            applyStateSum(sum = state.result.some())
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

    fun queryRefund(
        begin_time: String,
        end_time: String
    ) {
        repo.querySum(begin_time, end_time, "5")
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<QuerySumResult>(it)
                }, {
                    SimpleViewState.result(it)
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
                            applyStateRefund(refund = state.result.some())
                        } else if (this.return_code == "01" && this.result_code == "02") {
                            applyStateRefund(refund = state.result.some())
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

    private fun startQuery(payTrace: String, payTime: String) {
        Observable.interval(5, 5, TimeUnit.SECONDS)
            .take(15)
            .autoDisposable(this)
            .subscribe(object : Observer<Long> {

                override fun onSubscribe(d: Disposable) {
                    mDispose = d
                }

                override fun onNext(t: Long) {
                    query(payTrace, payTime)
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                    applyState(noresult = true.some())
                }
            })
    }

    fun startQuery(outTradeNo: String) {
        Observable.interval(5, 5, TimeUnit.SECONDS)
            .take(15)
            .autoDisposable(this)
            .subscribe(object : Observer<Long> {

                override fun onSubscribe(d: Disposable) {
                    mDispose = d
                }

                override fun onNext(t: Long) {
                    query(outTradeNo)
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                    applyState(noresult = true.some())
                }
            })
    }

     fun query(outTradeNo: String) {
        repo.queryResult(outTradeNo)
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<PayQueryResult>(it)
                }, {
                    SimpleViewState.result(it)
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
                            repo.savePrefsQuery(this)
                            applyState(query = state.result.some())
                            mDispose?.dispose()
                        } else if (this.return_code == "01" && this.result_code != "03") {
                            applyState(
                                loadingLayout = CommonLoadingState.ERROR,
                                error = Errors.ResultError(this.return_msg ?: "服务器返回错误").some()
                            )
                            mDispose?.dispose()
                        } else if (this.return_code == "02"){
                            applyState(
                                loadingLayout = CommonLoadingState.ERROR,
                                error = Errors.ResultError(this.return_msg ?: "服务器返回错误").some()
                            )
                            mDispose?.dispose()
                        }
                    }
                }
            }
    }

    private fun query(payTrace: String, payTime: String) {
        repo.queryResult(payTrace, payTime)
            .compose(globalHandleError())
            .map { either ->
                either.fold({
                    SimpleViewState.error<PayQueryResult>(it)
                }, {
                    SimpleViewState.result(it)
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
                            repo.savePrefsQuery(this)
                            applyState(query = state.result.some())
                            mDispose?.dispose()
                        } else if (this.return_code == "01" && this.result_code != "03") {
                            applyState(
                                loadingLayout = CommonLoadingState.ERROR,
                                error = Errors.ResultError(this.return_msg ?: "服务器返回错误").some()
                            )
                            mDispose?.dispose()
                        } else if (this.return_code == "02"){
                            applyState(
                                loadingLayout = CommonLoadingState.ERROR,
                                error = Errors.ResultError(this.return_msg ?: "服务器返回错误").some()
                            )
                            mDispose?.dispose()
                        }
                    }
                }
            }
    }

    private fun applyStateSum(
        loadingLayout: CommonLoadingState = CommonLoadingState.IDLE,
        sum: Option<QuerySumResult> = none()
    ){
        this.loadingLayout.postValue(loadingLayout)
        this.sumResult.postValue(sum.orNull())
    }

    private fun applyStateRefund(
        loadingLayout: CommonLoadingState = CommonLoadingState.IDLE,
        refund: Option<QuerySumResult> = none()
    ){
        this.loadingLayout.postValue(loadingLayout)
        this.refundResult.postValue(refund.orNull())
    }

    private fun applyState(
        loadingLayout: CommonLoadingState = CommonLoadingState.IDLE,
        auth: Option<AuthInfoResult> = none(),
        face: Option<BarCodePayResult> = none(),
        barcode: Option<BarCodePayResult> = none(),
        query: Option<PayQueryResult> = none(),
        error: Option<Throwable> = none(),
        noresult: Option<Boolean> = none(),
        signInfo: Option<SignResult> = none()
    ) {
        this.loadingLayout.postValue(loadingLayout)
        this.error.postValue(error)

        this.authInfo.postValue(auth.orNull())
        this.facePay.postValue(face.orNull())
        this.barcodePay.postValue(barcode.orNull())
        this.queryResult.postValue(query.orNull())

        this.noResult.postValue(noresult.orNull())
        this.signInfo.postValue(signInfo.orNull())
    }

    private fun applyErrorState(
        isShowMessage: Boolean = false,
        errorMessage: Option<String> = none()
    ) {
        this.isShowMessage.postValue(isShowMessage)
        errorMessage.whenNotNull { this.errorMessage.value = it }
    }
}

class PayViewModelFactory(
    private val repo: PayDataSourceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        PayViewModel(repo) as T

    companion object :
        SingletonHolderSingleArg<PayViewModelFactory, PayDataSourceRepository>(::PayViewModelFactory)
}