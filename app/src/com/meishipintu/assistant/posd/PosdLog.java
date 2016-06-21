package com.meishipintu.assistant.posd;

import android.util.Log;

public class PosdLog {
	public final static String TAG = PosdLog.class.getSimpleName();
	
	public static void d(String msg){
		Log.d(TAG, msg);
	}
	
	public static void e(String msg){
		Log.e(TAG, msg);
	}
	
	public static void v(String msg){
		Log.v(TAG, msg);
	}
}
