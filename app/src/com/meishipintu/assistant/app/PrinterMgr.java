package com.meishipintu.assistant.app;

import com.lkl.cloudpos.util.Debug;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class PrinterMgr {
	public void bindService(Context context, ServiceConnection conn) {
		Intent intent = new Intent();
		intent.setAction("lkl_cloudpos_mid_service");
		boolean flag = context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
		if (flag) {
			Debug.d("服务绑定成功");
		} else {
			Debug.d("服务绑定失败");
		}
	}
}
