package com.meishipintu.assistant.orderdish;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.meishipintu.assistant.R;
import com.meishipintu.core.utils.ConstUtil;
import com.umeng.analytics.MobclickAgent;

public class ActLoadMenuErr extends FragmentActivity {
	private long mShopId ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.orderdish_load_menu_err);
		Intent intent = getIntent();
		mShopId = intent.getLongExtra(ConstUtil.SHOP_ID, -1);
		findViewById(R.id.btn_back).setOnClickListener(ll);
		findViewById(R.id.btn_check_net).setOnClickListener(ll);
		findViewById(R.id.btn_retry).setOnClickListener(ll);
	}

	private OnClickListener ll = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;
			case R.id.btn_retry:
			{
				Intent in = new Intent();
				in.setClass(ActLoadMenuErr.this, ActOrderdish.class);
				in.putExtra(ConstUtil.SHOP_ID, mShopId);
                startActivity(in);
                finishAndAni();
			}
				break;
			case R.id.btn_check_net:{
				if(android.os.Build.VERSION.SDK_INT > 10 ){  
				    startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));  
				}else {  
				    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));  
				} 
			}
				break;
			default:
				break;
			}

		}
	};

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
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finishAndAni();
	}

	protected void finishAndAni() {
		finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
