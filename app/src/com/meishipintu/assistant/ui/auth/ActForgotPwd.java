package com.meishipintu.assistant.ui.auth;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.milai.http.AccountHttpMgr;
import com.milai.asynctask.PostGetTask;
import com.milai.utils.StringUtil;
import com.umeng.analytics.MobclickAgent;

public class ActForgotPwd extends Activity {
	private long mCount = 0;

	private TextView mTvSendCode = null;
	private EditText mEtTel = null;
	private EditText mEtVcode = null;
	private EditText mEtPwd = null;
	private EditText mEtReinputPwd=null;
	private String mTel = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.forgot_pwd);
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setText(getString(R.string.forgot_password));
		mEtTel = (EditText) findViewById(R.id.et_username);
		mEtVcode = (EditText) findViewById(R.id.et_v_code);
		mEtPwd = (EditText) findViewById(R.id.et_passwd);
		mEtReinputPwd=(EditText)findViewById(R.id.et_reinput_passwd);
		mTvSendCode = (TextView) findViewById(R.id.tv_verify);
		mTvSendCode.setOnClickListener(ll);
		findViewById(R.id.bt_reset).setOnClickListener(ll);
		findViewById(R.id.btn_back).setOnClickListener(ll);
	}

	private OnClickListener ll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;
			case R.id.tv_verify:
			{
				String tel = mEtTel.getText().toString();
				if (tel.length() < 11) {
					Toast.makeText(ActForgotPwd.this,
							getString(R.string.tel_digit_error),
							Toast.LENGTH_LONG).show();
					return;
				}
				if (tel.equals(mTel)) {
					Toast.makeText(ActForgotPwd.this,
							getString(R.string.prompts_same_tel),
							Toast.LENGTH_LONG).show();
					return;
				}
				if (mCount == 0) {
					mCount = 60;
					mHandler.postDelayed(mCountDownTask, 1000);
					sendVCode(tel);
				} else {
					Toast.makeText(ActForgotPwd.this,
							getString(R.string.send_wait), Toast.LENGTH_LONG)
							.show();
				}
			}
				break;
			case R.id.bt_reset:
				reset();
				break;
			default:
				break;
			}

		}
	};
	private void reset(){
		final String tel = mEtTel.getText().toString();
		if (StringUtil.isNullOrEmpty(tel)){
			Toast.makeText(ActForgotPwd.this, getString(R.string.user_name)+"不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		
		final String vCode = mEtVcode.getText().toString();
		if (StringUtil.isNullOrEmpty(vCode)){
			Toast.makeText(ActForgotPwd.this, getString(R.string.verify_code)+"不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		final String pwd = mEtPwd.getText().toString();
		if (StringUtil.isNullOrEmpty(pwd)){
			Toast.makeText(ActForgotPwd.this, getString(R.string.password)+"不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		final String pwdRe=mEtReinputPwd.getText().toString();
		if(StringUtil.isNullOrEmpty(pwdRe)||!pwd.equals(pwdRe))
		{
			Toast.makeText(ActForgotPwd.this, "两次密码输入不一致", Toast.LENGTH_LONG).show();
			return;
		}
		
		new PostGetTask<JSONObject>(this, R.string.reseting_pwd,
				R.string.reset_pwd_failed, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				return AccountHttpMgr.getInstance().resetPwd(tel, vCode, pwd);
			}

			@Override
			protected void doPostJob(Exception e, JSONObject result) {
				if (e == null && result != null) {
					try {
						if (result.getInt("result") != 1) {
							Toast.makeText(ActForgotPwd.this,
									result.getString("msg"), Toast.LENGTH_LONG)
									.show();
							return;
						}
						Toast.makeText(ActForgotPwd.this,
								"密码重置成功，请登录", Toast.LENGTH_LONG)
								.show();
						finishAndAni();
					} catch (JSONException e1) {
						Toast.makeText(ActForgotPwd.this,
								getString(R.string.reset_pwd_failed),
								Toast.LENGTH_LONG).show();
						e1.printStackTrace();
					}
				}
			}
		}.execute();
	}
	
	private void sendVCode(final String tel) {

		new PostGetTask<JSONObject>(this, R.string.sending_v_code,
				R.string.sent_v_code_fail, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				return AccountHttpMgr.getInstance().getVCode(tel);
			}

			@Override
			protected void doPostJob(Exception e, JSONObject result) {
				if (e == null && result != null) {
					try {
						if (result.getInt("result") != 1) {
							Toast.makeText(ActForgotPwd.this,
									result.getString("msg"), Toast.LENGTH_LONG)
									.show();
							mCount = 0;
						}
					} catch (JSONException e1) {
						Toast.makeText(ActForgotPwd.this,
								getString(R.string.sent_v_code_fail),
								Toast.LENGTH_LONG).show();

						
						e1.printStackTrace();
					}
				}else {
					mCount = 0;
				}
			}
		}.execute();
	}
	
	private Handler mHandler = new Handler();

	private Runnable mCountDownTask = new Runnable() {

		public void run() {
			if (mCount > 0) {
				mHandler.postDelayed(this, 1000);
				mCount--;
				mTvSendCode.setText(mCount + "秒");
			} else {
				mTvSendCode.setText(getString(R.string.verify));
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
