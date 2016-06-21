package com.meishipintu.assistant.ui;

import java.util.LinkedHashSet;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.app.MsptApplication;
import com.milai.dao.SubmittedTicketDao;
import com.meishipintu.assistant.fragment.B0_HomeFrag;
import com.meishipintu.assistant.fragment.C0_VipFrag;
import com.meishipintu.assistant.fragment.D0_MineFrag;
import com.meishipintu.assistant.fragment.FragClickListener;
import com.meishipintu.assistant.jpushext.JPushReceiver;
import com.meishipintu.assistant.mpos.ActBluetoothConfig;
import com.epos.utilstools.MPosConfig;
import com.meishipintu.assistant.mpos.BtNotificationBroadcastReceiver;
import com.milai.processor.OrderdishProcessor;
import com.milai.processor.PaymentProcessor;
import com.meishipintu.assistant.ui.auth.ActLogin;
import com.meishipintu.assistant.ui.pay.ActPayment;
import com.meishipintu.assistant.ui.pay.PaidFailedService;
import com.milai.asynctask.PostGetTask;
import com.milai.dao.CityCodeDao;
import com.meishipintu.core.ui.ActCitySel;
import com.meishipintu.core.utils.MyDialogUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends FragmentActivity implements
		AMapLocationListener, Runnable {
	//声明AMapLocationClient类对象
	public AMapLocationClient mLocationClient = null;
	public AMapLocation aMapLocation=null;
	//声明mLocationOption对象
	public AMapLocationClientOption mLocationOption = null;

	private Handler mLbsHandler = new Handler();
	private TextView mTvNumTicketNtf = null;
	private AsyncTask<Void, Void, String> mGetDishTask = null;

	private FragmentManager fragmentManager;
	
	private TextView mTvCityName = null;
	private ProgressBar mPbLocating = null;
	final Handler mHandler = new Handler();
	private int mCityId = 0;
	private int mWaitorType = 3;
	private static String PAID_FAILED_SERVICE_NAME="com.meishipintu.assistant.ui.pay.PaidFailedService";

	private Animation mScaleAniDown=null;
	private Intent inPaidFailedService=null;
	
	public static MainActivity mActivity = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		UmengUpdateAgent.update(this);  //初始化友盟自动更新引擎
				
		setContentView(R.layout.activity_main);
		
		FeedbackAgent agent = new FeedbackAgent(MainActivity.this); //初始化友盟反馈引擎
		agent.sync();
		
		mActivity = this;	
		
		fragmentManager = getSupportFragmentManager();
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_tab);
		
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == findViewById(R.id.rb_home).getId()) {
					showFrag("home");
				} else if (checkedId == findViewById(R.id.rb_vip).getId()) {
					showFrag("vip");
				} else if (checkedId == findViewById(R.id.rb_mine).getId()) {
					showFrag("mine");
				}
			}
		});
		radioGroup.check(findViewById(R.id.rb_home).getId());

		//mWaitOrType字段意义不明
		mWaitorType = Cookies.getWaitorType();

		findViewById(R.id.rl_sel_city).setOnClickListener(ll);
		MPosConfig.m_para_hashmap.put("MerchantsID", "103126493990008");
		
		Button btn_right = (Button) findViewById(R.id.btn_right);
		btn_right.setBackgroundResource(R.drawable.pay_list_detail);
		btn_right.setOnClickListener(ll);

		TextView tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(Cookies.getShopName());

		mTvCityName = (TextView) findViewById(R.id.tv_cur_city);
		mPbLocating = (ProgressBar) findViewById(R.id.pb_locating);
		String city = Cookies.getCity();
		if (city == null || city.equals("")) {
			mTvCityName.setVisibility(View.GONE);
			mPbLocating.setVisibility(View.VISIBLE);
		} else {
			mTvCityName.setText(Cookies.getCity());
			mCityId = Cookies.getCityId();
		}

		//图片点击动态效果
		mScaleAniDown=new ScaleAnimation(0.9f, 1, 0.9f, 1, Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		mScaleAniDown.setDuration(400);

		//声明定位回调监听器
		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		if(tm!=null){
			//初始化定位
			mLocationClient = new AMapLocationClient(getApplicationContext());
			//设置定位回调监听
			mLocationClient.setLocationListener(this);
			//初始化定位参数
			mLocationOption = new AMapLocationClientOption();
			//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
			mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
			//设置是否返回地址信息（默认返回地址信息）
			mLocationOption.setNeedAddress(true);
			//设置是否只定位一次,默认为false
			mLocationOption.setOnceLocation(false);
			//设置是否强制刷新WIFI，默认为强制刷新
			mLocationOption.setWifiActiveScan(true);
			//设置是否允许模拟位置,默认为false，不允许模拟位置
			mLocationOption.setMockEnable(false);
			//设置定位间隔,单位毫秒,默认为2000ms
			mLocationOption.setInterval(2000);
			//给定位客户端对象设置定位参数
			mLocationClient.setLocationOption(mLocationOption);
			mLocationClient.startLocation();
			mLbsHandler.postDelayed(this, 12000);// 设置超过12秒还没有定位到就停止定位
		}


		Set<String> tagSet = new LinkedHashSet<String>(); //注册jpush
		tagSet.add(Cookies.getShopCode());
		JPushInterface.setAliasAndTags(MainActivity.this, Cookies.getUserId(), tagSet, mTagsCallback);

		//获取菜单、桌台列表
		//是否还需要？
		getDishes();
		getTableList();

//		mTvNumTicketNtf = (TextView) findViewById(R.id.tv_ticket_notify_num);
//		mTvNumTakeawayNtf = (TextView) findViewById(R.id.tv_takeaway_notify_num);

//		mTvNumTicketNtf.setVisibility(View.GONE);

		//是否还需要？
		if (mWaitorType == 0 || mWaitorType == 1) {
			IntentFilter inf = new IntentFilter(JPushReceiver.PUSH_ACTION);
			registerReceiver(pushReceiver, inf);
		//	updateNumNtf(4);
			JPushInterface.resumePush(getApplicationContext());
		}
		//
//		mAl = (AlarmManager) getSystemService(ALARM_SERVICE);
//		Intent intent = new Intent(this, MobilePosActionBroadcastReceiver.class);
//		intent.setAction("pos_android.intent.action.my_broadcast");
//		intent.putExtra("POSCMD", "BT_DISCONNECT");
//		mSender = PendingIntent.getBroadcast(this, 0, intent, 0);
//		long now = System.currentTimeMillis() + 1000 * 60 * 60 * 7;// 1000*60*60*8;
//		if(Cookies.getShopType()!=3)
//		{
//			mAl.setInexactRepeating(AlarmManager.RTC_WAKEUP, now,
//					1000 * 60 * 60 * 7, mSender);
//		}
		startPaidFailedService();	
	}
	
	private void showFrag(String frag) {
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		Fragment fragment = null;
		if (frag.equals("home")) {
			fragment = B0_HomeFrag.createInstance(fragClickListener);
		} else if (frag.equals("vip")) {
			fragment = C0_VipFrag.createInstance(fragClickListener);
		} else if (frag.equals("mine")) {
			fragment = D0_MineFrag.createInstance(fragClickListener);
		} 
		transaction.replace(R.id.content, fragment);
		transaction.commit();
	}

	private FragClickListener fragClickListener = new FragClickListener() {

		@Override
		public void onViewSelected(View v) {
			Intent intent = new Intent();
			switch (v.getId()) {
			//case R.id.btn_list:
			//	showFrag(ConstUtil.FRAG.MESEUM_LIST);
			//	break;
			}
		}

		@Override
		public void enableMenuDrag(boolean b) {
		}
	};

	private void startPaidFailedService()
 	{
		if(inPaidFailedService!=null)
		{
			this.startService(inPaidFailedService);
		}else{
			inPaidFailedService=new Intent();
			inPaidFailedService.setAction(PAID_FAILED_SERVICE_NAME);
			inPaidFailedService.setClass(this, PaidFailedService.class);
			this.startService(inPaidFailedService);
		}	
	}

	private void stopOrDestoryPaidService(){
		//Log.d("Mainctivity_stopOrDestoryPaidService", "停止服务开始");
		try{
			Intent inStopService=new Intent(this,PaidFailedService.class);
			inStopService.setAction(PAID_FAILED_SERVICE_NAME);
			this.stopService(inStopService);
			Log.d("Mainctivity", "程序退出，停止服务");
		}catch(Exception e)
		{
			Log.d("Mainctivity", "程序退出，停止服务失败，或者服务没有运行");
		}
	}


	//Jpush回调接口，未实现
	private final TagAliasCallback mTagsCallback = new TagAliasCallback() {

		@Override
		public void gotResult(int code, String alias, Set<String> tags) {
			switch (code) {
			case 0:
				break;
			default:
				break;
			}
		}

	};

	private OnClickListener ll = new OnClickListener() {
		Intent intent = new Intent();

		@Override
		public void onClick(View v) {
			v.setAnimation(mScaleAniDown);	
			v.startAnimation(mScaleAniDown);
			switch (v.getId()) {
				//选择城市
			case R.id.rl_sel_city:
				intent.setClass(MainActivity.this, ActCitySel.class);
				startActivity(intent);
				overridePendingTransition(R.anim.left_in, R.anim.right_out);
				break;
				//收银详情
			case R.id.btn_right:
				intent.setClass(MainActivity.this, ActPayment.class);
				startActivity(intent);
				break;

			default:
				break;
			}

		}
	};

	//退出提示框
	public void showDialogQuit(final int mode) {

		MyDialogUtil qDialog = new MyDialogUtil(MainActivity.this) {

			@Override
			public void onClickPositive() {
//				JPushInterface.stopPush(getApplicationContext());
				if (Cookies.getRemenber() != 1) {
					Cookies.clearLogin();
					MsptApplication.getInstance().removeAllDBs();// 清除数据库数据，当菜单发生变化时需要重新获取
				}

//				mAl.cancel(mSender);
				stopOrDestoryPaidService();//停止支付失败服务

				ComponentName component = new ComponentName(MainActivity.this,
						BtNotificationBroadcastReceiver.class);
				getPackageManager().setComponentEnabledSetting(component,
						PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);

				MPosConfig.m_rfcComm.colse();
				if (ActBluetoothConfig.mActivity != null)
					ActBluetoothConfig.mActivity.finish();

				finish();
				if(mode==1)
				{
					Intent in =new Intent();
					in.setClass(MainActivity.this, ActLogin.class);
					startActivity(in);
				}else
				{
					System.exit(0);
				}
				
			}

			@Override
			public void onClickNagative() {

			}
		};
		qDialog.showCustomMessage(getString(R.string.notice),
				getString(R.string.prompts_quit), getString(R.string.confirm),
				getString(R.string.cancel));
	}

	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			showDialogQuit(0);
		}
		return false;
	}

	private BroadcastReceiver pushReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
			if (JPushReceiver.PUSH_ACTION.equals(intent.getAction())) {
				int t = intent.getIntExtra("type", 4);
				Log.d("DD", "Broadcast received t=" + t);
				getNewPoolTicket(t);
			}
		}
	};

	public void updateNumNtf(int t) {
		int cnt = 0;
		switch (t) {
		case 4:
			cnt = SubmittedTicketDao.getInstance().queryCount(MainActivity.this,
					1);
			if (cnt > 0) {
				mTvNumTicketNtf.setVisibility(View.VISIBLE);
				mTvNumTicketNtf.setText(cnt + "");
			} else {
				mTvNumTicketNtf.setVisibility(View.GONE);
			}
			break;
		case 5:
			cnt = SubmittedTicketDao.getInstance().queryCount(MainActivity.this,
					1);
			if (cnt > 0) {
				mTvNumTicketNtf.setVisibility(View.VISIBLE);
				mTvNumTicketNtf.setText(cnt + "");
			} else {
				mTvNumTicketNtf.setVisibility(View.GONE);
			}
			break;
		case 6:
			// mNumPayNtf += result;
			// if (mNumPayNtf > 0) {
			// mTvNumPayNtf.setVisibility(View.VISIBLE);
			// mTvNumPayNtf.setText(mNumPayNtf + "");
			// }
			break;
		}
	}

	private void getNewPoolTicket(final int t) {
		new PostGetTask<Integer>(this) {
			@Override
			protected Integer doBackgroudJob() throws Exception {
				switch (t) {
				case 5:
					return OrderdishProcessor.getInstance()
							.getSubedTicketRefresh(MainActivity.this, 2, 1, Cookies.getUserId(), Cookies.getToken());
				case 6:
					return PaymentProcessor.getInstance().getPaymentRefresh(
							MainActivity.this, 1, Cookies.getUserId(), Cookies.getToken(), Cookies.getShopId());
				default:
				case 4:
					return OrderdishProcessor.getInstance()
							.getSubedTicketRefresh(MainActivity.this, 1, 1, Cookies.getUserId(), Cookies.getToken());
				}
			}

			@Override
			protected void doPostJob(Exception e, Integer result) {
				if (result != null && e == null) {
					updateNumNtf(t);
				}
			}
		}.execute();
	}

	//获取、写入桌子列表？
	private void getTableList() {
		new PostGetTask<Integer>(this) {
			@Override
			protected Integer doBackgroudJob() throws Exception {
				return OrderdishProcessor.getInstance().getTableList(
						MainActivity.this, Cookies.getUserId(), Cookies.getToken());
			}

			@Override
			protected void doPostJob(Exception e, Integer result) {
				if (result != null && e == null) {
				} else {
				}
			}
		}.execute();
	}

	//获取、写入菜单列表？
	private void getDishes() {
		if (mGetDishTask != null) {
			mGetDishTask.cancel(true);
			mGetDishTask = null;
		}
		mGetDishTask = new PostGetTask<String>(this) {

			@Override
			protected String doBackgroudJob() throws Exception {
				String ret = OrderdishProcessor.getInstance().getDishes(
						MainActivity.this, Cookies.getUserId(), Cookies.getToken(), Cookies.getShopId());
				return ret;
			}

			@Override
			protected void doPostJob(Exception e, String result) {
				if ((e != null) || (result == null)) {
					return;
				}
			}

		};
		mGetDishTask.execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mCityId != Cookies.getCityId()) {
			mTvCityName.setText(Cookies.getCity());
			mCityId = Cookies.getCityId();
		}
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// stopLocation();// 停止定位
		if (mWaitorType == 0 || mWaitorType == 1) {
			unregisterReceiver(pushReceiver);
		}
	}

	/**
	 * 销毁定位
	 */
	private void stopLocation() {
		mLocationClient.stopLocation();
	}


	/**
	 * 混合定位回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation location) {
		if (location != null) {
			this.aMapLocation = location;// 判断超时机制
			String city = Cookies.getCity();
			if (city == null || city.equals("")) {
				mPbLocating.setVisibility(View.GONE);
				mTvCityName.setVisibility(View.VISIBLE);
				mTvCityName.setText(location.getCity());
				Cookies.saveCity(location.getCity());
				String[] ci = CityCodeDao.getInstance().queryCityInfo(
						MainActivity.this, location.getCity());
				Cookies.saveCityId(Integer.parseInt(ci[0]));
				// getNetTags(Cookies.getCityId());
			}
			Cookies.saveLoc((float) location.getLatitude(),
					(float) location.getLongitude());
			stopLocation();
		}
	}

	@Override
	public void run() {
		if (aMapLocation == null) {
			stopLocation();// 销毁掉定位
		}
	}
}
