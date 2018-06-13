package com.hm.wx.version.update;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.benmu.framework.http.okhttp.callback.StringCallback;
import com.benmu.framework.manager.ManagerFactory;
import com.benmu.framework.manager.impl.ParseManager;
import com.benmu.widget.utils.BaseCommonUtil;
import com.hm.wx.version.listener.DownloadListener;
import com.hm.wx.version.manager.impl.VersionAxiosManager;
import com.hm.wx.version.model.JsVersionReqBean;
import com.hm.wx.version.model.NativeVersionReqBean;
import com.hm.wx.version.model.NativeVersionResBean;
import com.hm.wx.version.service.DownloadService;
import com.hm.wx.version.utils.CommonUtils;

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
    public static final String APK_NAME_DEFAULT = "wx-eros-android.apk";
    private NativeVersionResBean version;
    private int mCurrentStatus = SLEEP;

    private ProgressDialog pd;

    private boolean isConnectionBind;

    private NativeVersionReqBean nvrb;

    private JsVersionReqBean jvrb;


    public NativeVersionChecker(Activity context, NativeVersionReqBean nvrb) {
        this.nvrb = nvrb;
        this.context = context;
    }

    public NativeVersionChecker(Activity context, NativeVersionReqBean nvrb, JsVersionReqBean jvrb) {
        this.context = context;
        this.nvrb = nvrb;
        this.jvrb = jvrb;
    }

    private void check(StringCallback callback){
        HashMap<String, String> params = new HashMap<>();
        try {
            params.put("appName", nvrb.getAppName());
            params.put("clientVersion", nvrb.getClientVersion());
            params.put("clientType", "android");
            VersionAxiosManager axiosManager = ManagerFactory.getManagerService(VersionAxiosManager.class);
            axiosManager.get(nvrb.getBaseUrl(), params, null, callback, nvrb.getBaseUrl(), 0);
        } catch (Exception e) {
            Log.e(TAG,  e.getMessage());
        }
    }

    public void checkNativeUpdate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                CommonUtils.CommonAlert.showAlert(context,
                        "提示",
                        "需要允许读写存储卡权限",
                        "确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }, "", null, false);
                return;
            }
        }

        if (mCurrentStatus == UPDATING) return;
        mCurrentStatus = UPDATING;

        check(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                mCurrentStatus = SLEEP;
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response, int id) {
                version = ManagerFactory.getManagerService(ParseManager
                        .class).parseObject(response, NativeVersionResBean.class);
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
                        if (jvrb != null) {
                            JsVersionChecker jsVersionChecker = new JsVersionChecker(context, jvrb);
                            jsVersionChecker.checkJsUpdate();
                        }
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
        public void onSuccess(String fileName) {
            pd.cancel();
            context.unbindService(connection);
            isConnectionBind = false;
            mCurrentStatus = SLEEP;
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 24) {
                Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".apkFile", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(
                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)),
                        "application/vnd.android.package-archive");
            }
            context.startActivity(intent);
            context.finish();
        }

        @Override
        public void onFailed() {
            pd.cancel();
            context.unbindService(connection);
            isConnectionBind = false;
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
                        if (jvrb != null) {
                            JsVersionChecker jsVersionChecker = new JsVersionChecker(context, jvrb);
                            jsVersionChecker.checkJsUpdate();
                        }
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
