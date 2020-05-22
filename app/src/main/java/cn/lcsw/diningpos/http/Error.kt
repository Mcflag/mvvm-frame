package cn.lcsw.diningpos.http

import com.github.qingmei2.core.GlobalErrorTransformer
import retrofit2.HttpException
import timber.log.Timber

fun <T> globalHandleError(): GlobalErrorTransformer<T> = GlobalErrorTransformer(
    globalDoOnErrorConsumer = { error ->
        when (error) {
            is HttpException -> {
                when (error.code()) {
                    401 -> Timber.e("401 Unauthorized")
                    404 -> Timber.e("404 error")
                    500 -> Timber.e("500 server error")
                    else -> Timber.e("network error")
                }
            }
            else -> Timber.e("network error")
        }
    }
)