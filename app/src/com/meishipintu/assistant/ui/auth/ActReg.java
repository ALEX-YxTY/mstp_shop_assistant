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

public class ActReg extends Activity {
	private long mCount = 0;

	private TextView mTvSendCode = null;
	private EditText mEtTel = null;
	private EditText mEtVcode = null;
	private EditText mEtPwd = null;
	private EditText mEtShopId = null;
	private EditText mEtNick = null;
	private String mTel = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setText(getString(R.string.register));
		mEtTel = (EditText) findViewById(R.id.et_username);
		mEtVcode = (EditText) findViewById(R.id.et_v_code);
		mEtPwd = (EditText) findViewById(R.id.et_passwd);
		mEtShopId = (EditText) findViewById(R.id.et_shop_id);
		mEtNick = (EditText) findViewById(R.id.et_nick_name);
		mTvSendCode = (TextView) findViewById(R.id.tv_verify);
		mTvSendCode.setOnClickListener(ll);
		findViewById(R.id.bt_reg).setOnClickListener(ll);
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
					Toast.makeText(ActReg.this,
							getString(R.string.tel_digit_error),
							Toast.LENGTH_LONG).show();
					return;
				}
				if (tel.equals(mTel)) {
					Toast.makeText(ActReg.this,
							getString(R.string.prompts_same_tel),
							Toast.LENGTH_LONG).show();
					return;
				}
				if (mCount == 0) {
					mCount = 60;
					mHandler.postDelayed(mCountDownTask, 1000);
					sendVCode(tel);
				} else {
					Toast.makeText(ActReg.this,
							getString(R.string.send_wait), Toast.LENGTH_LONG)
							.show();
				}
			}
				break;
			case R.id.bt_reg:
				reg();
				break;
			default:
				break;
			}

		}
	};
	
	private void reg(){
		final String tel = mEtTel.getText().toString();
		if (StringUtil.isNullOrEmpty(tel)){
			Toast.makeText(ActReg.this, getString(R.string.user_name)+"不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		
		final String vCode = mEtVcode.getText().toString();
		if (StringUtil.isNullOrEmpty(vCode)){
			Toast.makeText(ActReg.this, getString(R.string.verify_code)+"不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		final String pwd = mEtPwd.getText().toString();
		if (StringUtil.isNullOrEmpty(pwd)){
			Toast.makeText(ActReg.this, getString(R.string.password)+"不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		final String sid = mEtShopId.getText().toString();
		if (StringUtil.isNullOrEmpty(sid)){
			Toast.makeText(ActReg.this, getString(R.string.shop_id)+"不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		final String nickName = mEtNick.getText().toString();
		if (StringUtil.isNullOrEmpty(nickName)){
			Toast.makeText(ActReg.this, getString(R.string.nick_name)+"不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		
		new PostGetTask<JSONObject>(this, R.string.registing,
				R.string.reg_failed, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				return AccountHttpMgr.getInstance().register(tel, vCode, pwd, sid, nickName);
			}

			@Override
			protected void doPostJob(Exception e, JSONObject result) {
				if (e == null && result != null) {
					try {
						if (result.getInt("result") != 1) {
							Toast.makeText(ActReg.this,
									result.getString("msg"), Toast.LENGTH_LONG)
									.show();
							return;
						}
						Toast.makeText(ActReg.this,
								"注册成功,请等待审核", Toast.LENGTH_LONG)
								.show();
						finishAndAni();
					} catch (JSONException e1) {
						Toast.makeText(ActReg.this,
								getString(R.string.sent_v_code_fail),
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
							Toast.makeText(ActReg.this,
									result.getString("msg"), Toast.LENGTH_LONG)
									.show();
							mCount = 0;
						}
					} catch (JSONException e1) {
						Toast.makeText(ActReg.this,
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
		// TODO Auto-generated method stub
		super.onBackPressed();
		finishAndAni();
	}

	protected void finishAndAni() {
		finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
