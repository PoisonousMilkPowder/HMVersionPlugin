package com.hm.wx;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hm.wx.version.model.NativeVersionReqBean;
import com.hm.wx.version.update.NativeVersionChecker;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HashMap<String, String> params = new HashMap<>();
        params.put("clientType", "android");
        params.put("appName", "home-app");
        params.put("clientVersion", "1.0.40");
        NativeVersionChecker nvc = new NativeVersionChecker(this,
                "https://appserver.anxintrust.net/nodejs/app/checkClientVersion/",
                params);
        nvc.checkNativeUpdate();
    }
}
