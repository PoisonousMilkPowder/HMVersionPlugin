package com.hm.wx.version.listener;

import com.benmu.framework.model.VersionBean;
import com.hm.wx.version.model.JsVersionResBean;

/**
 * Created by liangshuai on 2018/5/2.
 * WeexFrameworkWrapper
 * com.hm.wx.version.listener
 */

public interface JsVersionListener {

    void onFail();

    void onSuccess(VersionBean version);

    void onDownloadSuccess();

    void onDownLoadFailed();

}
