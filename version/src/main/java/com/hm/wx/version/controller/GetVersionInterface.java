package com.hm.wx.version.controller;

import com.hm.wx.version.model.NativeVersionResBean;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by liangshuai on 2019/3/25.
 * HMVersionPlugin
 * com.hm.wx.version.controller
 */

public interface GetVersionInterface {

    @GET
    Call<NativeVersionResBean> getVersionRes(@Url String url, @QueryMap Map<String, String> params);

}
