package com.meishipintu.assistant.ui.pay;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.meishipintu.assistant.app.Cookies;
import com.milai.dao.PaidFailedDao;
import com.milai.http.PaymentHttpMgr;
import com.milai.model.PaidFailed;

public class PaidFailedService extends Service {

	private Timer mTimer;
	private static String PAID_FAILED_SERVICE_NAME = "com.meishipintu.assistant.ui.pay.PaidFailedService";
	public static PaidFailedService mPaidFailedService = null;
	private ArrayList<PaidFailed> mFailedList=null;

	private boolean isWork = true;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mPaidFailedService = this;
		if (!isServiceWork()) {
			Log.d("PaidFailedService", "启动服务");
			mTimer = new Timer();
			mTimer.schedule(mReQuestTimerTask, 5000, 60000);
		}
	}

	private TimerTask mReQuestTimerTask = new TimerTask() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d("PaidFailedService", "运行计时任务");
			if(mFailedList!=null&&mFailedList.size()>0)
			{
				int position =getRandomPosition();
				PaidFailed member=mFailedList.get(position);
				try{
					int result=PaymentHttpMgr.getInstance().reSendPaidFailed(PaidFailedService.this, member, Cookies.getUserId(), Cookies.getToken(), Cookies.getShopId(), Cookies.getShopName());
					if(result==1)
					{
						mFailedList.remove(position);
					}
				}catch(Exception e)
				{
					Log.d("PaidFailedService", "重新提交数据到服务器异常");
				}

			}else{
				getFailedList();
			}
		}
	};


	private void getFailedList()
	{
		Log.d("PaidFailedService", "获取paidFailed列表");
		mFailedList=PaidFailedDao.getInstance().getPaidFailedArrayList(PaidFailedService.this);
		Log.d("PaidFailedService", Integer.toString(mFailedList.size()));
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}




	private boolean isServiceWork() {
		ActivityManager activityManager = (ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> listRunningService = (ArrayList<RunningServiceInfo>) activityManager
				.getRunningServices(40);
		for (int i = 0; i < listRunningService.size(); i++) {
			if (listRunningService.get(i).service.getClassName().toString() == PAID_FAILED_SERVICE_NAME) {
				isWork = true;
			} else {
				isWork = false;
			}
		}
		Log.d("检测服务是否已经启动", isWork ? "已经运行" : "没有运行");
		return isWork;
	}


	private int getRandomPosition()
	{
		int rand=mFailedList.size();
		Random rdm=new Random();
		int randomNum=rdm.nextInt(rand);
		return randomNum;
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("PaidFailedService", "终止计时任务");
		if (mTimer != null) {
			mTimer.cancel();
			Log.d("PaidFailedService", "终止计时任务成功");
		}
	}

}
