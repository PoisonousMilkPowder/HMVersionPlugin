package com.hm.wx.version.update;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.benmu.framework.BMWXEnvironment;
import com.benmu.framework.constant.Constant;
import com.benmu.framework.http.okhttp.callback.FileCallBack;
import com.benmu.framework.http.okhttp.callback.StringCallback;
import com.benmu.framework.manager.ManagerFactory;
import com.benmu.framework.manager.impl.AxiosManager;
import com.benmu.framework.manager.impl.FileManager;
import com.benmu.framework.manager.impl.ParseManager;
import com.benmu.framework.manager.impl.VersionManager;
import com.benmu.framework.model.JsVersionInfoBean;
import com.benmu.framework.model.Md5MapperModel;
import com.benmu.framework.model.VersionBean;
import com.benmu.framework.utils.L;
import com.benmu.framework.utils.Md5Util;
import com.benmu.framework.utils.SharePreferenceUtil;
import com.benmu.widget.utils.BaseCommonUtil;
import com.hm.wx.version.listener.JsVersionListener;
import com.hm.wx.version.manager.impl.VersionAxiosManager;
import com.hm.wx.version.model.JsVersionBean;
import com.hm.wx.version.model.JsVersionReqBean;
import com.hm.wx.version.utils.CommonUtils;
import com.hm.wx.version.utils.FileUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.ele.patch.BsPatch;
import okhttp3.Call;


/**
 * Created by liangshuai on 2018/4/28.
 * WeexFrameworkWrapper
 * com.hm.wx.version.update
 */

public class JsVersionChecker {
    private static final String TAG = "JsVersionChecker";
    public static final int UPDATING = 0;
    public static final int SLEEP = 1;
    private JsVersionBean newVersion;
    private String mUpdateUrl;
    private Activity mContext;

    private int mCurrentStatus = SLEEP;
    private JsVersionListener listener;

    private JsVersionReqBean jvrb;

    JsVersionChecker(Activity context, JsVersionReqBean reqBean) {
        this.mContext = context;
        this.jvrb = reqBean;
    }

    public void check(boolean isDiff, StringCallback callback) {
        HashMap<String, String> params = new HashMap();
        try {
            params.put("appName", jvrb.getAppName());
            params.put("android", jvrb.getAndroid());
            params.put("jsVersion", jvrb.getJsVersion());
            params.put("isDiff", isDiff ? "1" : "0");
            VersionAxiosManager axiosManager = (VersionAxiosManager)ManagerFactory.getManagerService(VersionAxiosManager.class);
            axiosManager.get(jvrb.getBaseUrl(), params, (HashMap)null, callback, jvrb.getBaseUrl(), 0L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkJsUpdate() {
        if (mCurrentStatus == UPDATING) return;
        mCurrentStatus = UPDATING;

        check(true, new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "获取更新失败");
                        mCurrentStatus = SLEEP;
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        VersionBean version = ManagerFactory.getManagerService(ParseManager
                                .class).parseObject(response,
                                VersionBean
                                        .class);
                        if (version == null) {
                            Log.e(TAG, "返回结果异常");
                            mCurrentStatus = SLEEP;
                            return;
                        }
                        Log.e(TAG, "onResponse: " + version.resCode);
                        switch (version.resCode) {
                            case 0://需要更新
                                if (!TextUtils.isEmpty(version.data.path)) {
                                    if (version.data.diff) {
                                        //更新插分包
                                        Log.e(TAG, "检查插分包");
                                        download(version, false);
                                    } else {
                                        //下载全量包
                                        Log.e(TAG, "检查全量包");
                                        downloadCompleteZip();
                                    }
                                } else {
                                    //下载全量包
                                    Log.e(TAG, "检查全量包");
                                    downloadCompleteZip();
                                }
                                break;
                            case 401:
                                Log.e(TAG, "JS文件查询失败!");
                                mCurrentStatus = SLEEP;
                                break;
                            case 4000:
                                Log.e(TAG, "当前版本已是最新!");
                                mCurrentStatus = SLEEP;
                                break;
                            default:
                                Log.e(TAG, "resCode:" + version.resCode);
                                mCurrentStatus = SLEEP;
                                break;

                        }
                    }
                });

    }


    /**
     * 下载全量包
     */
    private void downloadCompleteZip() {
        check(false, new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "检查全量包失败!，更新失败");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        VersionBean version = ManagerFactory.getManagerService(ParseManager
                                .class).parseObject(response, VersionBean
                                .class);
                        if (version != null && !TextUtils.isEmpty(version.data.path)) {
                            Log.e(TAG, "检查全量包成功!，开始下载");
                            download(version, true);
                        }
                    }
                });
    }

    /**
     * 下载包
     */
    public void download(final VersionBean version, final boolean complete) {
        try {
            if (version == null) {
                mCurrentStatus = SLEEP;
                return;
            }
            VersionManager versionManager = ManagerFactory.getManagerService(VersionManager.class);
            versionManager.downloadBundle(version.data.path, new FileCallBack(FileManager
                    .getTempBundleDir(mContext).getAbsolutePath(), version.data.diff ? FileManager
                    .PATCH_NAME : FileManager.TEMP_BUNDLE_NAME) {
                @Override
                public void onError(Call call, Exception e, int id) {
                    Log.e(TAG, "下载插分包出错");
                    if (!complete) {
                        downloadCompleteZip();
                    } else {
                        mCurrentStatus = SLEEP;
                    }
                }

                @Override
                public void onResponse(File response, int id) {
                    Log.e("version", "下载成功" + response.getAbsolutePath());
                    if (version.data.diff) {
                        bsPatch();
                    } else {
                        File download = new File(FileManager.getTempBundleDir(mContext)
                                , FileManager.TEMP_BUNDLE_NAME);
                        if (checkZipValidate(download)) {
                            RenameDeleteFile();
                            //更改本地jsversion
                            SharePreferenceUtil.setDownLoadVersion(mContext,
                                    ManagerFactory.getManagerService(ParseManager.class)
                                            .toJsonString(newVersion));
                            newVersion = null;
                            mCurrentStatus = SLEEP;
                            CommonUtils.CommonAlert.showAlert(mContext,
                                    "提示",
                                    "请重启app获取最新资源包内容",
                                    "确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    }, "", null, false);
                        } else {
                            L.e(TAG, "更新包md5校验失败，更新失败");
                            FileUtil.deleteFile(new File(FileManager.getTempBundleDir
                                    (mContext), FileManager.TEMP_BUNDLE_NAME));
                            newVersion = null;
                            mCurrentStatus = SLEEP;
                        }
                    }

                }


            });
        } catch (Exception e) {
            mCurrentStatus = SLEEP;
            e.printStackTrace();
        }


    }


    /**
     * MD5 效验
     */
    private boolean checkZipValidate(File file) {
        if (file.exists()) {
            byte[] json = FileManager.extractZip(file, "md5.json");
            try {
                Md5MapperModel mapper = ManagerFactory.getManagerService(ParseManager.class)
                        .parseObject(new String(json, "UTF-8"),
                                Md5MapperModel.class);
                //校验文件正确性
                List<Md5MapperModel.Item> lists = mapper.getFilesMd5();
                // 按照md5值从小到大排列
                Collections.sort(lists);
                //所有md5想加得到总的md5
                String total = "";
                for (Md5MapperModel.Item item : lists) {
                    total = total + item.getMd5();
                }
                String finalMd5 = Md5Util.getMd5code(total);
                //比较md5是否正确
                newVersion = new JsVersionBean(mapper.getJsVersion(), mapper.getAndroid(),
                        mapper.getTimestamp());
                return mapper.getJsVersion().equals(finalMd5);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 插分生成全量包
     */
    private void bsPatch() {

        File oldFile = new File(FileManager.getTempBundleDir(mContext), FileManager
                .BUNDLE_NAME);
        File newFile = new File(FileManager.getTempBundleDir(mContext), FileManager
                .TEMP_BUNDLE_NAME);
        File patchFile = new File(FileManager.getTempBundleDir(mContext), FileManager
                .PATCH_NAME);
        if (oldFile.exists() && patchFile.exists()) {
            BsPatch.workAsync(oldFile.getAbsolutePath(), newFile.getAbsolutePath(), patchFile
                    .getAbsolutePath(), new BsPatch.BsPatchListener() {
                @Override
                public void onSuccess(String oldPath, String newPath, String patchPath) {
                    L.e("version", "bspath 命令成功");
                    //验证下载包是否正确
                    File download = new File(FileManager.getTempBundleDir
                            (mContext), FileManager
                            .TEMP_BUNDLE_NAME);
                    boolean validate = checkZipValidate(download);
                    if (validate) {
                        L.e("version", "下载patch md5校验成功");
                        FileUtil.deleteFile(new File(FileManager.getTempBundleDir
                                (mContext), FileManager.BUNDLE_NAME));
                        FileUtil.deleteFile(new File(FileManager.getTempBundleDir
                                (mContext), FileManager.PATCH_NAME));
                        FileManager.renameFile(FileManager.getTempBundleDir
                                        (mContext),
                                FileManager.TEMP_BUNDLE_NAME, FileManager
                                        .BUNDLE_NAME);
                        //更改本地jsversion
                        SharePreferenceUtil.setDownLoadVersion(mContext, ManagerFactory
                                .getManagerService(ParseManager.class).toJsonString(newVersion));
                        newVersion = null;
                        mCurrentStatus = SLEEP;
                    } else {
                        L.e("version", "下载patch md5校验出错");
                        //删除生成的新包和patch包
                        FileUtil.deleteFile(new File(FileManager.getTempBundleDir
                                (mContext), FileManager.PATCH_NAME));
                        FileUtil.deleteFile(new File(FileManager.getTempBundleDir
                                (mContext), FileManager.TEMP_BUNDLE_NAME));
                        newVersion = null;
                        mCurrentStatus = SLEEP;
                    }


                }

                @Override
                public void onFail(String oldPath, String newPath, String patchPath, Exception e) {
                    FileUtil.deleteFile(new File(FileManager.getTempBundleDir
                            (mContext), FileManager.PATCH_NAME));
                    FileUtil.deleteFile(new File(FileManager.getTempBundleDir
                            (mContext), FileManager.TEMP_BUNDLE_NAME));
                    mCurrentStatus = SLEEP;
                }
            });
        }


    }


    /**
     * 删除旧的Js包,更新新Js包名称
     */
    private void RenameDeleteFile() {
        FileUtil.deleteFile(new File(FileManager.getTempBundleDir
                (mContext), FileManager.BUNDLE_NAME));
        FileManager.renameFile(FileManager.getTempBundleDir
                        (mContext),
                FileManager.TEMP_BUNDLE_NAME, FileManager
                        .BUNDLE_NAME);
    }

    public void startDownloadZip(VersionBean version) {
        if (!TextUtils.isEmpty(version.data.path)) {
            if (version.data.diff) {
                //更新插分包
                Log.e(TAG, "检查插分包");
                download(version, false);
            } else {
                //下载全量包
                Log.e(TAG, "检查全量包");
                downloadCompleteZip();
            }
        } else {
            //下载全量包
            Log.e(TAG, "检查全量包");
            downloadCompleteZip();
        }
    }

}
