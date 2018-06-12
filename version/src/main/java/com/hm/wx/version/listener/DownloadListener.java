package com.hm.wx.version.listener;

/**
 * Created by liangshuai on 2018/3/29.
 * WeexFrameworkWrapper
 * com.hm.wx.version.impl
 */

public interface DownloadListener {

    void onProgress(int progress);

    void onSuccess();

    void onFailed();

}
