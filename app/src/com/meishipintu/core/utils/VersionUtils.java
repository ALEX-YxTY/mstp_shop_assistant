package com.meishipintu.core.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by Administrator on 2016/6/23.
 */
public class VersionUtils {

    public static int getVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            int versionCode = pi.versionCode;
            Log.i("test", "versionCode:" + versionCode);
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }
}
