package com.hm.wx.version.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by liangshuai on 2018/6/13.
 * HMVersionPlugin
 * com.hm.wx.version.model
 */

public class JsVersionReqBean implements Serializable {

    private String jsVersion;

    private String appName;

    private String android;

    private String baseUrl;

    public void setJsVersion(String jsVersion) {
        this.jsVersion = jsVersion;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAndroid(String android) {
        this.android = android;
    }


    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getJsVersion() throws Exception {
        if (TextUtils.isEmpty(jsVersion)) throw new Exception(jsVersion + "is empty");
        return jsVersion;
    }

    public String getAppName() throws Exception {
        if (TextUtils.isEmpty(appName)) throw new Exception(appName + "is empty");
        return appName;
    }

    public String getAndroid() throws Exception {
        if (TextUtils.isEmpty(android)) throw new Exception(android + "is empty");
        return android;
    }

    public String getBaseUrl() throws Exception {
        if (TextUtils.isEmpty(baseUrl)) throw new Exception(baseUrl + "is empty");
        return baseUrl;
    }
}
