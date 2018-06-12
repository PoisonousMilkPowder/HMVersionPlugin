package com.hm.wx.version.model;

import java.io.Serializable;

/**
 * Created by liangshuai on 2018/6/1.
 * UpdateProject
 * com.hm.wx.version.model
 */

public class JsVersionResBean implements Serializable {

    public Data data;
    private int resCode;
    private String msg;

    public JsVersionResBean(int resCode, String msg) {
        this.resCode = resCode;
        this.msg = msg;
    }

    public JsVersionResBean(){}

    public static class Data implements Serializable {
        public String path;
        public boolean diff;
    }

}
