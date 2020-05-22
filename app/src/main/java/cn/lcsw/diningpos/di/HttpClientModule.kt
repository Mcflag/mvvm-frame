package cn.lcsw.diningpos.di

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

const val WX_UPDATE_RETROFIT = "wxUpdateRetrofit"
private const val HTTP_CLIENT_MODULE_TAG = "httpClientModule"
const val HTTP_CLIENT_MODULE_INTERCEPTOR_LOG_TAG = "http_client_module_interceptor_log_tag"
private const val HTTP_SSL_SOCKET_FACTORY = "sslSocketFactory"
private const val HTTP_SSL_CONTEXT = "sslContext"

const val TIME_OUT_SECONDS = 15
//const val BASE_URL = "http://test.lcsw.cn:8032/lcsw/"
const val BASE_URL = "https://pay.lcsw.cn/lcsw/"
const val BASE_URL2 = "https://api.lcsw.cn/lcsw/"

val httpClientModule = Kodein.Module(HTTP_CLIENT_MODULE_TAG) {

    bind<Retrofit.Builder>() with provider { Retrofit.Builder() }

    bind<OkHttpClient.Builder>() with provider { OkHttpClient.Builder() }

    bind<Retrofit>() with singleton {
        instance<Retrofit.Builder>()
            .baseUrl(BASE_URL)
            .client(instance())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    bind<Retrofit>(WX_UPDATE_RETROFIT) with singleton {
        instance<Retrofit.Builder>()
            .baseUrl(BASE_URL2)
            .client(instance())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    bind<Interceptor>(HTTP_CLIENT_MODULE_INTERCEPTOR_LOG_TAG) with singleton {
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    bind<SSLContext>(HTTP_SSL_CONTEXT) with singleton {
        var sc = SSLContext.getInstance("SSL")
        sc.init(
            null,
            arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }
            }),
            SecureRandom()
        )
        sc
    }

    bind<SSLSocketFactory>(HTTP_SSL_SOCKET_FACTORY) with singleton {
        instance<SSLContext>(HTTP_SSL_CONTEXT).getSocketFactory()
    }

    bind<OkHttpClient>() with singleton {
        instance<OkHttpClient.Builder>()
            .connectTimeout(
                TIME_OUT_SECONDS.toLong(),
                TimeUnit.SECONDS
            )
            .readTimeout(
                TIME_OUT_SECONDS.toLong(),
                TimeUnit.SECONDS
            )
            .addInterceptor(
                instance(HTTP_CLIENT_MODULE_INTERCEPTOR_LOG_TAG)
            )
            .sslSocketFactory(instance(HTTP_SSL_SOCKET_FACTORY))
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    bind<Gson>() with singleton { Gson() }
}