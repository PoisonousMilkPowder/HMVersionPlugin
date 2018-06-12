package com.hm.wx.version.manager.impl;

import android.content.Context;

import com.benmu.framework.BMWXEnvironment;
import com.benmu.framework.extend.adapter.WeexOkhttp3Interceptor;
import com.benmu.framework.http.BMPersistentCookieStore;
import com.benmu.framework.http.okhttp.OkHttpUtils;
import com.benmu.framework.http.okhttp.builder.GetBuilder;
import com.benmu.framework.http.okhttp.builder.OkHttpRequestBuilder;
import com.benmu.framework.http.okhttp.callback.Callback;
import com.benmu.framework.http.okhttp.cookie.CookieJarImpl;
import com.benmu.framework.http.okhttp.exception.IrregularUrlException;
import com.benmu.framework.http.okhttp.log.LoggerInterceptor;
import com.benmu.framework.manager.Manager;
import com.benmu.framework.utils.DebugableUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by liangshuai on 2018/6/12.
 * WeexFrameworkWrapper
 * com.hm.wx.version.manager.impl
 */

public class VersionAxiosManager extends Manager {

    public OkHttpClient createClient(Context context, long timeout) {
        CookieJarImpl cookieJar = new CookieJarImpl(new BMPersistentCookieStore
                (context));

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor("TAG"))
                //接口超时时间  默认3000毫秒
                .connectTimeout(timeout == 0 ? 3000L : timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout == 0 ? 30000L : timeout, TimeUnit.MILLISECONDS).cookieJar
                        (cookieJar);
        if (DebugableUtil.isDebug()) {
            builder.addNetworkInterceptor(new WeexOkhttp3Interceptor());
        }
        return builder.build();
    }

    public void initClient(Context context) {
        OkHttpUtils.initClient(createClient(context, 0));
    }

    public void get(String mUrl, HashMap<String, String> params, HashMap<String, String> header,
                    Callback callback, Object tag, long timeout) {
        if (mUrl == null) {
            if (callback != null) {
                callback.onError(null, new IrregularUrlException("url不合法"), 0);
            }
            return;
        }
        if (header == null) {
            header = new HashMap<>();
        }
        setTimeout(timeout);
        GetBuilder builder = OkHttpUtils.get().url(mUrl).tag(tag).headers(header);
        generateParams(params, builder);
        builder.build().execute(callback);
    }

    private void setTimeout(long timeout) {
        if (timeout != 0) {
            OkHttpUtils.getInstance().updateHttpClient(createClient(BMWXEnvironment
                    .mApplicationContext, timeout));
        }
    }

    private void generateParams(Map<String, String> params, OkHttpRequestBuilder builder) {
        if (params == null) {
            params = new HashMap<>();
        }
        if (builder instanceof GetBuilder) {
            GetBuilder getBuilder = (GetBuilder) builder;

            for (Map.Entry<String, String> entry : params.entrySet()) {
                getBuilder.addParams(entry.getKey().trim(), entry.getValue().trim());
            }
        }

    }
}
