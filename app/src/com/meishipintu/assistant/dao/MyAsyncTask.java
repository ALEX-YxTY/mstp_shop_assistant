package com.meishipintu.assistant.dao;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/6/23.
 */
public class MyAsyncTask extends AsyncTask<String, Integer, File> {

    Activity activity;
    ProgressDialog mProgressDialog;
    String downloadUrl;

    public MyAsyncTask(Context context, String file) {
        this.activity = (Activity) context;
        this.downloadUrl = file;
        mProgressDialog = new ProgressDialog(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
    }

    @Override
    protected void onPreExecute() {
        if (mProgressDialog != null) {
            mProgressDialog.setTitle("下载进度");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(File file) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (file != null && file.exists()) {
            //跳转至安装页面
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            activity.startActivityForResult(intent,0);
        }
    }

    @Override
    protected File doInBackground(String...Params) {
        File file = new File(Environment.getExternalStorageDirectory(),"ecoop.apk");
        try {
            URL url = new URL(this.downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            if (conn.getResponseCode() == 200) {        //连接成功
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);

                int lengthOfFile = conn.getContentLength();
                mProgressDialog.setMax(lengthOfFile);   //设置progress最大值
                float fileLengthInFloat = lengthOfFile / 1024.0f / 1024.0f;
                byte[] buffer = new byte[1024];         //缓存数组
                int total = 0;                          //总计读取值
                int length;                         //本次读取值

                try {
                    while ((length = is.read(buffer)) != -1) {
                        fos.write(buffer);
                        fos.flush();
                        total += length;
                        mProgressDialog.setProgress(total);
                        float totalInFloat = total / 1024.0f / 1024.0f;
                        mProgressDialog.setProgressNumberFormat(String.format("%.2f MB/%.2f MB", totalInFloat, fileLengthInFloat));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    try {
                        is.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                showToast("下载出错");
                return null;
            }

        } catch (Exception e) {
            showToast("下载出错");
            return null;
        }
        return file;
    }

    private void showToast(final String string) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, string, Toast.LENGTH_SHORT).show();

            }
        });
    }
}
