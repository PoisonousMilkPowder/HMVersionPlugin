package com.hm.wx.version.model;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by liangshuai on 2018/6/13.
 * HMVersionPlugin
 * com.hm.wx.version.model
 */

public class NativeVersionReqBean implements Serializable {

    private String clientVersion;

    private String appName;

    private String baseUrl;

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getClientVersion() throws Exception {
        if (TextUtils.isEmpty(clientVersion)) throw new Exception("clientVersion empty");
        return clientVersion;
    }

    public String getAppName() throws Exception {
        if (TextUtils.isEmpty(appName)) throw new Exception("appName empty");
        return appName;
    }

    public String getBaseUrl() throws Exception {
        if (TextUtils.isEmpty(baseUrl)) throw new Exception("baseUrl empty");
        return baseUrl;
    }

}
