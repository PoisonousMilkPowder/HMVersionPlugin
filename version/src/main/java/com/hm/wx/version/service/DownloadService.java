package com.hm.wx.version.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import com.hm.wx.version.listener.DownloadListener;
import com.hm.wx.version.task.DownloadTask;

import java.io.File;

public class DownloadService extends Service {

    private DownloadTask downloadTask;

    private String downloadUrl;

    private DownloadListener downloadListener;

    public void setProgressListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    private DownloadBinder binder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class DownloadBinder extends Binder {

        public DownloadService getService() {
            return DownloadService.this;
        }

        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(downloadListener);
                downloadTask.execute(downloadUrl);
            }
        }

    }
}
