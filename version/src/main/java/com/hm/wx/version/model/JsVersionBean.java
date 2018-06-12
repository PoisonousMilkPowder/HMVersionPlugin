package com.hm.wx.version.model;

import java.io.Serializable;

/**
 * Created by liangshuai on 2018/4/28.
 * WeexFrameworkWrapper
 * com.hm.wx.version.model
 */

public class JsVersionBean implements Serializable {
    private String jsVersion;
    private String android;
    private String timestamp;

    public JsVersionBean(String jsVersion, String android, String timestamp) {
        this.jsVersion = jsVersion;
        this.android = android;
        this.timestamp = timestamp;
    }

    public JsVersionBean() {
    }

    public String getJsVersion() {
        return jsVersion;
    }

    public void setJsVersion(String jsVersion) {
        this.jsVersion = jsVersion;
    }

    public String getAndroid() {
        return android;
    }

    public void setAndroid(String android) {
        this.android = android;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
