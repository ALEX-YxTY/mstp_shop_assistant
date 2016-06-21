package com.meishipintu.core.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.MsptApplication;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;

public class ActAboutUs extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.about_us);
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setText(getString(R.string.about));
		TextView tvVer = (TextView) findViewById(R.id.tv_app_ver);
        String verName = null;
        try {
            verName = MsptApplication.getInstance().getPackageManager()
                    .getPackageInfo(MsptApplication.getInstance().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }		
		tvVer.setText(getString(R.string.version_name_tip, verName));
		
		findViewById(R.id.btn_back).setOnClickListener(ll);
		findViewById(R.id.rl_shop_contact).setOnClickListener(ll);
		findViewById(R.id.rl_service_contract).setOnClickListener(ll);
		findViewById(R.id.rl_feedback).setOnClickListener(ll);
	}

	private OnClickListener ll = new OnClickListener() {

		Intent in = null;
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;
			case R.id.rl_service_contract:
				in = new Intent();
				in.setClass(ActAboutUs.this, ActSvcContract.class);
				startActivity(in);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;

			case R.id.rl_feedback:
				FeedbackAgent agent = new FeedbackAgent(ActAboutUs.this);
			    agent.startFeedbackActivity();
				break;
			case R.id.rl_shop_contact:
				in = new Intent();
				in.setClass(ActAboutUs.this, ActShopContactUs.class);
				startActivity(in);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
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
		// TODO Auto-generated method stub
		super.onBackPressed();
		finishAndAni();
	}

	protected void finishAndAni() {
		finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
