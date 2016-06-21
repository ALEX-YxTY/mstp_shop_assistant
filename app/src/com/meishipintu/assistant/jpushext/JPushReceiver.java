package com.meishipintu.assistant.jpushext;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.orderdish.ActSubmittedTicket;
import com.meishipintu.assistant.ui.pay.ActPayment;
import com.milai.utils.SoundUtil;
import com.milai.utils.StringUtil;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则： 1) 默认用户会打开主界面 2) 接收不到自定义消息
 */
public class JPushReceiver extends BroadcastReceiver {
	private static final String TAG = "MyReceiver";
	public static final String PUSH_ACTION = "com.meishipintu.assistant.pushcoming";
	private SoundPool spool;

//    public static void enablePush(Context ctx) {
//        JPushInterface.setPushTime(ctx, null, 0, 23);
//    }
//    
//    public static void disablePush(Context ctx) {
//        Set<Integer> days = new HashSet<Integer>();
//        JPushInterface.setPushTime(ctx, days, 0, 23);
//    }
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();

		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			String regId = bundle
					.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			Log.d(TAG, "接收Registration Id : " + regId);
			// send the Registration Id to your server...
		}  else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
				.getAction())) {
			Log.d(TAG,
					"接收到推送下来的自定义消息: "
							+ bundle.getString(JPushInterface.EXTRA_MESSAGE));

		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
				.getAction())) {
			String extra_info = bundle.getString("cn.jpush.android.EXTRA");
			String str=JPushInterface.EXTRA_EXTRA;
			try {
				JSONObject jo = new JSONObject(extra_info);
				if (jo.has("t")) {
					int t = jo.getInt("t");
					if (t == 4 || t == 5 || t==6) {
						if (StringUtil.isNullOrEmpty(Cookies.getToken())) return ;
						SoundUtil.playFinishSound(context, R.raw.dingdong);
						Intent it = new Intent(PUSH_ACTION);
						Log.d("DD", "push received t="+t);
						it.putExtra("type", t);
						context.sendBroadcast(it);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
				.getAction())){
			//用户点击打开了通知
			String extra_info = bundle.getString("cn.jpush.android.EXTRA");
			try {
				JSONObject jo = new JSONObject(extra_info);
				Log.d("jpush_extraInfo", jo.toString());
				if (jo.has("t")) {
					int t = jo.getInt("t");
					if (t == 4) {
						if (StringUtil.isNullOrEmpty(Cookies.getToken())) return ;
						Intent i = new Intent(context, ActSubmittedTicket.class);
						i.putExtra("takeaway", 1);	
						i.putExtra("status", 1);
						i.setAction(Intent.ACTION_MAIN);
						i.addCategory(Intent.CATEGORY_LAUNCHER);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
					}else if (t == 5){ //外卖
						if (StringUtil.isNullOrEmpty(Cookies.getToken())) return ;
						Intent i = new Intent(context, ActSubmittedTicket.class);
						i.putExtra("takeaway", 2);	
						i.putExtra("status", 1);
						
						i.setAction(Intent.ACTION_MAIN);
						i.addCategory(Intent.CATEGORY_LAUNCHER);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
					}else if (t == 6){ //付款
						if (StringUtil.isNullOrEmpty(Cookies.getToken())) return ;
						Intent i = new Intent(context, ActPayment.class);
						i.setAction(Intent.ACTION_MAIN);
						i.addCategory(Intent.CATEGORY_LAUNCHER);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
					}else if(t==7)//微信点菜
					{
						if (StringUtil.isNullOrEmpty(Cookies.getToken())) return ;
						Intent i = new Intent(context, ActSubmittedTicket.class);
						i.putExtra("takeaway", 1);	
						i.putExtra("status", 1);					
						i.setAction(Intent.ACTION_MAIN);
						i.addCategory(Intent.CATEGORY_LAUNCHER);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())){
		} else {
			Log.d(TAG, "Unhandled intent - " + intent.getAction());
		}
	}

}
