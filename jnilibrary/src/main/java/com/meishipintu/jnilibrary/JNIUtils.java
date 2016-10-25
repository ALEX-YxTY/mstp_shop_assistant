package com.meishipintu.jnilibrary;

/**
 * Created by Administrator on 2016/9/27.
 */

public class JNIUtils {

    static {
        System.loadLibrary("jnitest");
    }

    public native static String getTotp(String tel, int timeStamp);
}
