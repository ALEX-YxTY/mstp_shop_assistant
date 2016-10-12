package com.meishipintu.assistant.app;

import java.io.File;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import cn.jpush.android.api.JPushInterface;

import com.milai.model.Dishes;
import com.milai.model.Payment;
import com.milai.model.ShopTable;
import com.milai.model.SubmittedTicket;
import com.milai.model.TicketDetail;
import com.milai.model.VersionTable;
import com.milai.download.DownloadMgr;
import com.milai.download.ImageDownloader;
import com.milai.download.ThumbImgDownloader;
import com.meishipintu.assistant.ui.auth.ActLogin;
import com.milai.http.HttpMgr;
import com.milai.processor.BaseProcessor;
import com.milai.utils.LocalImageLoader;
import com.meishipintu.core.utils.ConstUtil;
import com.milai.model.MsptProvider.DatabaseHelper;
import com.milai.model.MsptProvider;

public class MsptApplication extends Application {

    private static final String LOG_TAG = MsptApplication.class.getSimpleName();

    private static MsptApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (!initDirs()) {
            Log.e(LOG_TAG, "create dir error");
        }

        HttpMgr.getInstance().setContext(instance, ActLogin.class);
        BaseProcessor.setContext(instance);
        LocalImageLoader.getInstance().setContext(instance);
        ImageDownloader.getInstance().setContext(instance);
        ThumbImgDownloader.getInstance().setContext(instance);
        
        MsptProvider.setAuthority("com.meishipintu.assistant");
        
        JPushInterface.setDebugMode(false);//debug模式
        JPushInterface.init(this);
    }


    private boolean initDirs() {
        boolean flag = true;
        String downloadCachePath = DownloadMgr.getDownloadCacheDirPath(instance);
        File downloadCacheDir = new File(downloadCachePath);
        if (!downloadCacheDir.exists()) {
            flag &= downloadCacheDir.mkdirs();
        }

        String uploadCachePath = ConstUtil.getUploadCacheDirPath();
        File uploadCacheDir = new File(uploadCachePath);
        if (!uploadCacheDir.exists()) {
            flag &= uploadCacheDir.mkdirs();
        }

        String msptDir = ConstUtil.getAppDir();
        File fMsptDir = new File(msptDir);
        if (!fMsptDir.exists()) {
            flag &= fMsptDir.mkdirs();
        }

        return flag;
    }

    public static MsptApplication getInstance() {
        return instance;
    }

    public static String getTopScreenActivityPackageName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return cn.getPackageName();
    }

    public static String getTopScreenActivityClassName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return cn.getClassName();
    }

    public void removeAllDBs()
    {
    	DatabaseHelper helper = new DatabaseHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase(); 
        db.delete(Dishes.TABLE_NAME, null, null);
        db.delete(ShopTable.TABLE_NAME, null, null);
        db.delete(SubmittedTicket.TABLE_NAME, null, null);
        db.delete(TicketDetail.TABLE_NAME, null, null);
        db.delete(VersionTable.TABLE_NAME, null, null);
        db.delete(Payment.TABLE_NAME, null, null);
        db.close();        
    }

}
