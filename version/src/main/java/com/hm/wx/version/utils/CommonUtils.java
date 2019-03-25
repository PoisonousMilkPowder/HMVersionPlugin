package com.hm.wx.version.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import java.io.File;
import java.util.List;


/**
 * Created by liangshuai on 2018/4/10.
 * WeexFrameworkWrapper
 * com.anxintrust.home.utils
 */

public class CommonUtils {

    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception var3) {
            return "";
        }
    }

    public static String getTopActivity(Context context) {
        String topActivityClassName = null;
        ActivityManager activityManager =
                (ActivityManager) (context.getSystemService(Context
                        .ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
            ComponentName f = runningTaskInfos.get(0).topActivity;
            topActivityClassName = f.getClassName();
        }
        return topActivityClassName;
    }

    public static boolean isAPPRunningForeground(Context context) {
        if (context != null) {
            String packageName = context.getPackageName();
            String topName = getTopActivity(context);
            return packageName != null && topName != null && topName.startsWith(packageName);
        } else {
            return false;
        }
    }

    public static class CommonAlert {
        private static AlertDialog alertDialog = null;

        public static void showAlert(Context context, String title, String message, String okBtn,
                                     DialogInterface.OnClickListener okListenner, String cancelBtn,
                                     DialogInterface.OnClickListener cancelListenner, boolean isCancelable) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title).setMessage(message).setPositiveButton(okBtn, okListenner).setCancelable(isCancelable);
            if (!TextUtils.isEmpty(cancelBtn)) {
                builder.setNegativeButton(cancelBtn, cancelListenner);
            }
            alertDialog = builder.create();
            if (alertDialog != null && !alertDialog.isShowing() && !((Activity) context).isFinishing()) {
                alertDialog.show();
            }
        }

        public static void closeAlert() {
            if (alertDialog != null) {
                alertDialog.cancel();
            }
        }
    }
}
