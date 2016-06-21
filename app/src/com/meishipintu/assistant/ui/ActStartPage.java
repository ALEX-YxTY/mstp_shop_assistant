package com.meishipintu.assistant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import cn.jpush.android.api.InstrumentedActivity;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.ui.auth.ActBindShop;
import com.meishipintu.assistant.ui.auth.ActLogin;
import com.meishipintu.core.ui.ActCitySel;
import com.meishipintu.core.ui.ActGuide;
import com.meishipintu.core.utils.ConstUtil;
import com.milai.utils.ScreenParam;
import com.umeng.analytics.MobclickAgent;

public class ActStartPage extends InstrumentedActivity {

	private Handler handler = new Handler();

	private Runnable startPgRun = new Runnable() {

		@Override
		public void run() {
			Intent intent = new Intent();
			
			if (Cookies.showGuide()) {
				intent.setClass(getBaseContext(), ActGuide.class);//跳转资讯
			} else if (Cookies.getCityId() == 0){
				intent.putExtra("first_use", true);
				intent.setClass(ActStartPage.this, ActCitySel.class);//跳转定位
			} else if(Cookies.getUserId().length()>0) {
				if (Cookies.getShopId() == 0) {
					intent.setClass(ActStartPage.this, ActBindShop.class);//跳转商户
				} else {
					intent.setClass(ActStartPage.this, MainActivity.class);
				}			
			} else {
				intent.setClass(ActStartPage.this, ActLogin.class);//登录失败
			}
			
			startActivity(intent);
			//切换动画
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_start_page);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		//矫正屏幕尺寸，padding，margin跟随像素密度变化而变化的设置
		ScreenParam.getInstance().setParam(screenWidth, screenHeight);
		handler.postDelayed(startPgRun, ConstUtil.DYNAMIC_START_PAGE_TIMEOUT);
	}


	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
