package com.meishipintu.assistant.ui.auth;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.app.MsptApplication;
import com.meishipintu.assistant.ui.ActShopCheckIn;
import com.meishipintu.assistant.ui.RegisterActivity;
import com.milai.http.AccountHttpMgr;
import com.meishipintu.assistant.ui.MainActivity;
import com.milai.asynctask.PostGetTask;
import com.milai.utils.Des2;
import com.milai.utils.StringUtil;
import com.umeng.analytics.MobclickAgent;

public class ActLogin extends Activity {

	private EditText mEtTel = null;
	private EditText mEtPwd = null;
	private int mRemenber = 0;
	private CheckBox cBRemenber = null;
	private int osVersion = 0;
	private int mIsRelogin = 0;
	private TextView tvClearName;
	private TextView tvClearPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setText(getString(R.string.app_name));
		Intent in = getIntent();
		// if(in.hasExtra("RELOGIN"))
		// {
		// mIsRelogin=in.getIntExtra("RELOGIN", 0);
		// if(mIsRelogin==1)
		// {
		// Toast.makeText(getBaseContext(), "登陆信息失效，请重新登陆",
		// Toast.LENGTH_LONG).show();
		// }
		// }
		mEtTel = (EditText) findViewById(R.id.et_username);
		mEtTel.setText(Cookies.getAccount());
		mEtPwd = (EditText) findViewById(R.id.et_passwd);
		cBRemenber = (CheckBox) findViewById(R.id.remenber_password);
		mRemenber = Cookies.getRemenber();
		osVersion = android.os.Build.VERSION.SDK_INT;
		Log.d("version", Integer.toString(osVersion));
		if (osVersion < 17) {
			cBRemenber.setPadding(40, -3, 0, 0);
			// cBRemenber.setLayoutParams(LayoutParams.);
		}
		if (mRemenber == 1) {
			String pwdDes = null;
			try {
				pwdDes = Des2.decodeValue("meishipintu", Cookies.getPassword());
			} catch (Exception e) {
				e.printStackTrace();
			}
			mEtPwd.setText(pwdDes);
			cBRemenber.setChecked(true);
		} else {
			mEtPwd.setText("");
			cBRemenber.setChecked(false);
		}
		mEtTel.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				mEtPwd.setText("");
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length()==0)
				{
					tvClearName.setVisibility(View.INVISIBLE);
				}else{
					tvClearName.setVisibility(View.VISIBLE);
				}
			}
		});
		
		mEtPwd.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length()==0)
				{
					tvClearPassword.setVisibility(View.INVISIBLE);
				}else{
					tvClearPassword.setVisibility(View.VISIBLE);
				}
			}});

		tvClearName=(TextView)findViewById(R.id.tv_clear);
		tvClearPassword=(TextView)findViewById(R.id.tv_clear_pwd);
		
		tvClearPassword.setOnClickListener(ll);
		tvClearName.setOnClickListener(ll);
		
		findViewById(R.id.btn_back).setOnClickListener(ll);
		findViewById(R.id.tv_reg).setOnClickListener(ll);//用户注册于20160613隐藏
		findViewById(R.id.tv_forgot_pwd).setOnClickListener(ll);
		findViewById(R.id.bt_login).setOnClickListener(ll);
		findViewById(R.id.shop_check_in).setOnClickListener(ll);
	}

	private OnClickListener ll = new OnClickListener() {

		Intent in = new Intent();

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;
			case R.id.tv_reg:
				in.setClass(ActLogin.this, RegisterActivity.class);
				startActivity(in);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;//用户注册于20160613隐藏
			case R.id.tv_forgot_pwd:
				in.setClass(ActLogin.this, ActForgotPwd.class);
				startActivity(in);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;

			case R.id.bt_login:
				login();
				break;
			case R.id.tv_clear:
				mEtTel.setText("");
				break;
			case R.id.tv_clear_pwd:
				mEtPwd.setText("");
				break;
			case R.id.shop_check_in:
				in.setClass(ActLogin.this, ActShopCheckIn.class);
				startActivity(in);
				break;
			default:
				break;
			}

		}
	};

	private void login() {
		final String tel = mEtTel.getText().toString();
		if (StringUtil.isNullOrEmpty(tel)) {
			Toast.makeText(ActLogin.this,
					getString(R.string.user_name) + "不能为空", Toast.LENGTH_LONG)
					.show();
			return;
		}

		final String pwd = mEtPwd.getText().toString();
		if (StringUtil.isNullOrEmpty(pwd)) {
			Toast.makeText(ActLogin.this,
					getString(R.string.password) + "不能为空", Toast.LENGTH_LONG)
					.show();
			return;
		}

		new PostGetTask<JSONObject>(this, R.string.logining,
				R.string.login_failed, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				return AccountHttpMgr.getInstance().login(tel, pwd);
			}

			@Override
			protected void doPostJob(Exception e, JSONObject result) {
				if (e == null && result != null) {
					try {
						Log.i("test", "result:" + result.toString());
						if (result.getInt("result") == 1) {
							JSONObject uinfo = result
									.getJSONObject("waiterInfo");
							String shopComment = uinfo.getString("shopComment");
							// hcs
							if (shopComment != null || !shopComment.equals("")) {
								Cookies.setShopComment(shopComment);
							}
							String old_account = Cookies.getAccount();
							int waitorType = uinfo.getInt("type");// hcs
							long old_shopId = Cookies.getShopId();
							String savePwd = null;
							if (cBRemenber.isChecked() == false) {
								savePwd = "";
								Cookies.setRemenber(0);
							} else {
								savePwd = pwd;
								Cookies.setRemenber(1);
							}
							Log.i("test", "shopId:" + uinfo.getString("shopId"));
							Cookies.saveUserInfo(uinfo.getString("uid"),
									uinfo.getString("token"), pwd, tel,
									uinfo.getString("nickname"),
									uinfo.getLong("shopId"),
									uinfo.getString("shopCode"),
									uinfo.getString("shopName"), waitorType,
									uinfo.getString("shopType"),uinfo.getInt("shopCategory"));
							if (!old_account.equals(Cookies.getAccount())
									|| (old_shopId != Cookies.getShopId())) {
								MsptApplication.getInstance().removeAllDBs();
							}

							Intent in = new Intent();
							if (uinfo.getLong("shopId") == 0) {
								in.setClass(ActLogin.this, ActBindShop.class);
							} else {
								in.setClass(ActLogin.this, MainActivity.class);
							}
							startActivity(in);
							overridePendingTransition(R.anim.right_in,
									R.anim.left_out);
							finish();
						} else {
							Toast.makeText(ActLogin.this,
									result.getString("msg"), Toast.LENGTH_LONG)
									.show();
						}
					} catch (JSONException e1) {
						Toast.makeText(ActLogin.this,
								getString(R.string.login_failed),
								Toast.LENGTH_LONG).show();
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
