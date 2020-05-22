package cn.lcsw.diningpos.utils;

import androidx.annotation.NonNull;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Progress;
import com.vector.update_app.HttpManager;
import okhttp3.OkHttpClient;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 使用OkGo实现接口
 */

public class OkGoUpdateHttpUtil implements HttpManager {

    private static final short DEFAULT_CONNECTION_TIMEOUT = 15;
    /**
     * 异步get
     *
     * @param url      get请求地址
     * @param params   get参数
     * @param callBack 回调
     */
    @Override
    public void asyncGet(@NonNull String url, @NonNull Map<String, String> params, @NonNull final Callback callBack) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)  //全局的连接超时时间
                .readTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)     //全局的读取超时时间
                .writeTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)    //全局的写入超时时间
                .build();
        OkGo.getInstance().setOkHttpClient(client).<String>get(url).params(params).execute(new com.lzy.okgo.callback.StringCallback() {
            @Override
            public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                callBack.onResponse(response.body());
            }

            @Override
            public void onError(com.lzy.okgo.model.Response<String> response) {
                super.onError(response);
                callBack.onError("异常");
            }
        });
    }

    /**
     * 异步post
     *
     * @param url      post请求地址
     * @param params   post请求参数
     * @param callBack 回调
     */
    @Override
    public void asyncPost(@NonNull String url, @NonNull Map<String, String> params, @NonNull final Callback callBack) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)  //全局的连接超时时间
                .readTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)     //全局的读取超时时间
                .writeTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)    //全局的写入超时时间
                .build();
        JSONObject jsonObject = new JSONObject(params);
        OkGo.getInstance().setOkHttpClient(client).<String>post(url).upJson(jsonObject.toString()).execute(new com.lzy.okgo.callback.StringCallback() {
            @Override
            public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                callBack.onResponse(response.body());
            }

            @Override
            public void onError(com.lzy.okgo.model.Response<String> response) {
                super.onError(response);
                callBack.onError("异常");
            }
        });
    }

    /**
     * 下载
     *
     * @param url      下载地址
     * @param path     文件保存路径
     * @param fileName 文件名称
     * @param callback 回调
     */
    @Override
    public void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull final FileCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        OkGo.getInstance().setOkHttpClient(client).<File>get(url).execute(new com.lzy.okgo.callback.FileCallback(path, fileName) {
            @Override
            public void onSuccess(com.lzy.okgo.model.Response<File> response) {
                callback.onResponse(response.body());
            }

            @Override
            public void onStart(com.lzy.okgo.request.base.Request<File, ? extends com.lzy.okgo.request.base.Request> request) {
                super.onStart(request);
                callback.onBefore();
            }

            @Override
            public void onError(com.lzy.okgo.model.Response<File> response) {
                super.onError(response);
                callback.onError("异常");
            }

            @Override
            public void downloadProgress(Progress progress) {
                super.downloadProgress(progress);

                callback.onProgress(progress.fraction, progress.totalSize);
            }
        });
    }
}