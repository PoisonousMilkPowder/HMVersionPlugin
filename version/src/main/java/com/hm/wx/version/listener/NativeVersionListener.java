package com.hm.wx.version.listener;

/**
 * Created by liangshuai on 2018/4/3.
 * WeexFrameworkWrapper
 * com.hm.wx.version.impl
 */

public interface NativeVersionListener {

    void onFail();

    void onSuccess(int flag, String response);

}
