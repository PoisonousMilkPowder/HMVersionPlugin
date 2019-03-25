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
import android.text.TextUtils;
import android.widget.Toast;

import com.hm.wx.version.controller.GetVersionInterface;
import com.hm.wx.version.listener.DownloadListener;
import com.hm.wx.version.model.NativeVersionReqBean;
import com.hm.wx.version.model.NativeVersionResBean;
import com.hm.wx.version.service.DownloadService;
import com.hm.wx.version.utils.CommonUtils;

import java.io.File;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    public static final String APK_NAME_DEFAULT = "android.apk";
    private NativeVersionResBean version;
    private int mCurrentStatus = SLEEP;

    private ProgressDialog pd;

    private boolean isConnectionBind;

    private NativeVersionReqBean nvrb;

    private Map<String, String> params;

    private String reqUrl;

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

    public NativeVersionChecker(Activity context, String reqUrl , Map<String, String> params) {
        this.reqUrl = reqUrl;
        this.params = params;
        this.context = context;
    }

    public void checkNativeUpdate() {
        if (mCurrentStatus == UPDATING) return;
        mCurrentStatus = UPDATING;

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(reqUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            GetVersionInterface request = retrofit.create(GetVersionInterface.class);

            Call<NativeVersionResBean> call = request.getVersionRes(reqUrl, params);


            call.enqueue(new Callback<NativeVersionResBean>() {
                @Override
                public void onResponse(Call<NativeVersionResBean> call, Response<NativeVersionResBean> response) {
                    version = response.body();
                    if (version == null) {
                        mCurrentStatus = SLEEP;
                        return;
                    }
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
                            break;
                        default:
                            break;

                    }
                }

                @Override
                public void onFailure(Call<NativeVersionResBean> call, Throwable t) {
                    mCurrentStatus = SLEEP;
                }
            });
        } catch (Exception e) {

        }
    }

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
        mCurrentStatus = SLEEP;

        CommonUtils.CommonAlert.showAlert(context,
                "提示",
                version.getData().getDescription(),
                "立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkPermission()) {
                            dialog.cancel();
                            createProgressDialog();
                            downloadBinder.startDownload(version.getData().getUrl());
                        }
                    }
                }, isForced ? "" : "暂不更新", isForced ? null : new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        context.unbindService(connection);
                        isConnectionBind = false;
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

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "需要允许读写存储卡权限", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
}
