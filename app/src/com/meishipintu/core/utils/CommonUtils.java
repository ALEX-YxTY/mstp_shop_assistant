package com.meishipintu.core.utils;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {
    public static String DateUtilSecond(Long timestamp) {
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(timestamp));
        return date;

    }
    public static String DateUtilOne(Long timestamp) {
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:MM:ss").format(new java.util.Date(timestamp));
        return date;

    }

}
