package com.meishipintu.core.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.umeng.analytics.MobclickAgent;

public class ActSvcContract extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.svc_contract);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.service_contract));

        View btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	finishAndAni();
            }
        });

        WebView wv = (WebView) findViewById(R.id.web);
        wv.setBackgroundColor(getResources().getColor(R.color.bg_app));
        wv.loadUrl("file:///android_asset/svc_contract.html");
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
