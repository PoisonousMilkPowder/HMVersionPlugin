package com.hm.wx.version.model;

import java.io.Serializable;

/**
 * Created by liangshuai on 2018/3/23.
 * WeexFrameworkWrapper
 * com.hm.wx.version.modal
 */

public class NativeVersionResBean {

    private String resCode;
    private String msg;
    private DataBean data;

    public String getResCode() {
        return resCode;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {

        private String description;
        private String url;

        public String getDescription() {
            return description;
        }

        public String getUrl() {
            return url;
        }

    }
}
