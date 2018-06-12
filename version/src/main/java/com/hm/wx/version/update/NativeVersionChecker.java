package com.hm.wx.version.update;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.benmu.framework.BMWXEnvironment;
import com.benmu.framework.http.okhttp.callback.StringCallback;
import com.benmu.framework.manager.ManagerFactory;
import com.benmu.framework.manager.impl.AxiosManager;
import com.benmu.framework.manager.impl.ParseManager;
import com.benmu.widget.utils.BaseCommonUtil;
import com.hm.wx.version.listener.DownloadListener;
import com.hm.wx.version.manager.impl.VersionAxiosManager;
import com.hm.wx.version.model.NativeVersionBean;
import com.hm.wx.version.service.DownloadService;
import com.hm.wx.version.utils.CommonUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import okhttp3.Call;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by liangshuai on 2018/3/23.
 * WeexFrameworkWrapper
 * com.hm.wx.version.utils
 */

public class NativeVersionChecker {
    public static final int UPDATING = 0;
    public static final int SLEEP = 1;

    private Activity context;
    private static final String TAG = "NativeVersionChecker";
    private NativeVersionBean version;
    private int mCurrentStatus = SLEEP;

    private ProgressDialog pd;

    private boolean isConnectionBind;

    public NativeVersionChecker(Activity context) {
        this.context = context;
    }

    private void check(String url, String appName, StringCallback
            callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("appName", appName);
        params.put("clientVersion", BaseCommonUtil.getVersionName(context));
        params.put("clientType", "android");
        VersionAxiosManager axiosManager = ManagerFactory.getManagerService(VersionAxiosManager.class);
        axiosManager.get(url, params, null, callback, url, 0);
    }

    public void checkNativeUpdate(String baseUrl, String appName) {
        if (mCurrentStatus == UPDATING) return;
        mCurrentStatus = UPDATING;

        check(baseUrl, appName, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                mCurrentStatus = SLEEP;
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response, int id) {
                version = ManagerFactory.getManagerService(ParseManager
                        .class).parseObject(response, NativeVersionBean.class);
                if (version == null) {
                    mCurrentStatus = SLEEP;
                    Log.e(TAG, "version == null");
                    return;
                }
                Log.e(TAG, "onResponse: " + JSON.toJSONString(version));
                switch (version.getResCode()) {
                    case "APP0003":
                        //非强制更新
                        if (!TextUtils.isEmpty(version.getData().getUrl())) {
                            showConfirmDialog(false);
                        }
                        break;
                    case "APP0002":
                        //强制更新
                        if (!TextUtils.isEmpty(version.getData().getUrl())) {
                            showConfirmDialog(true);
                        }
                        break;
                    case "APP0001":
                        //版本最新
                        mCurrentStatus = SLEEP;
                        JsVersionChecker jsVersionChecker = new JsVersionChecker(context);
                        jsVersionChecker.checkJsUpdate();
                        break;
                    default:
                        break;

                }
            }
        });
    }

    private DownloadService.DownloadBinder downloadBinder;

    private DownloadService downloadService;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
            downloadService = downloadBinder.getService();
            downloadService.setProgressListener(new DownloadListenerImpl());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private class DownloadListenerImpl implements DownloadListener {

        @Override
        public void onProgress(int progress) {
            pd.setProgress(progress);
        }

        @Override
        public void onSuccess() {
            pd.cancel();
            context.unbindService(connection);
            isConnectionBind = false;
            mCurrentStatus = SLEEP;
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "axxthome.apk");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 24) {
                Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".apkFile", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(
                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "axxthome.apk")),
                        "application/vnd.android.package-archive");
            }
            context.startActivity(intent);
            context.finish();
        }

        @Override
        public void onFailed() {
            pd.cancel();
            Toast.makeText(context, "安装包下载失败", Toast.LENGTH_SHORT).show();
            mCurrentStatus = SLEEP;
        }
    }

    private void showConfirmDialog(boolean isForced) {
        Intent intent = new Intent(context, DownloadService.class);
        context.bindService(intent, connection, BIND_AUTO_CREATE);
        isConnectionBind = true;

        CommonUtils.CommonAlert.showAlert(context,
                "提示",
                version.getData().getDescription(),
                "立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        createProgressDialog();
                        downloadBinder.startDownload(version.getData().getUrl());
                    }
                }, isForced ? "" : "暂不更新", isForced ? null : new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        context.unbindService(connection);
                        isConnectionBind = false;
                        mCurrentStatus = SLEEP;
                    }
                }, false);
    }

    private void createProgressDialog() {
        pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.setTitle("下载中");
        pd.setMax(100);
        pd.show();
    }

    public void releaseAll() {
        CommonUtils.CommonAlert.closeAlert();
        if (isConnectionBind && connection != null) {
            context.unbindService(connection);
        }
    }
}
