package com.hm.wx.version.model;

import java.io.Serializable;

/**
 * Created by liangshuai on 2018/3/23.
 * WeexFrameworkWrapper
 * com.hm.wx.version.modal
 */

public class NativeVersionBean implements Serializable {

    private String resCode;
    private String msg;
    private DataBean data;

    public String getResCode() {
        return resCode;
    }

    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
