package com.meishipintu.assistant.ui.auth;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.milai.http.AccountHttpMgr;
import com.milai.asynctask.PostGetTask;
import com.milai.utils.StringUtil;
import com.umeng.analytics.MobclickAgent;

public class ActBindShop extends Activity {

	private EditText mEtShopCode = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bind_shop);
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setText(getString(R.string.bind_shop));
		if (Cookies.getShopId() == 0) {
			findViewById(R.id.ll_bind_prompts).setVisibility(View.VISIBLE);
		}
		mEtShopCode = (EditText) findViewById(R.id.et_shop_id);
		findViewById(R.id.btn_back).setOnClickListener(ll);
		findViewById(R.id.bt_bind).setOnClickListener(ll);
	}

	private OnClickListener ll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;
			case R.id.bt_bind:
				bindShop();
				break;
			default:
				break;
			}

		}
	};

	private void bindShop() {
		final String sc = mEtShopCode.getText().toString();
		if (StringUtil.isNullOrEmpty(sc)) {
			Toast.makeText(ActBindShop.this,
					getString(R.string.shop_id) + "不能为空", Toast.LENGTH_LONG)
					.show();
			return;
		}

		new PostGetTask<JSONObject>(this, R.string.sending_bind_req,
				R.string.send_bind_req_failed, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				return AccountHttpMgr.getInstance().bindShop(sc, Cookies.getUserId(), Cookies.getToken());
			}

			@Override
			protected void doPostJob(Exception e, JSONObject result) {
				if (e == null && result != null) {
					try {
						if (result.getInt("result") == 1) {
							Intent in = new Intent();
							in.setClass(ActBindShop.this, ActLogin.class);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
								in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
								in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							}
					        else
					        {
								in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
								in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					        }
							
							startActivity(in);
							overridePendingTransition(R.anim.left_in,
									R.anim.right_out);
							finish();
						} else {

							Toast.makeText(ActBindShop.this,
									result.getString("msg"), Toast.LENGTH_LONG)
									.show();

						}
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
			}
		}.execute();
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
		super.onBackPressed();
		finishAndAni();
	}

	protected void finishAndAni() {
		finish();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
