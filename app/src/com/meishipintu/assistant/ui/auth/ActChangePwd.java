package com.meishipintu.assistant.ui.auth;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class ActChangePwd extends Activity {

	private EditText mEtOld = null;
	private EditText mEtNew = null;
	private EditText mEtNewRe=null;
	
	private TextView mTvToast=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.change_pwd);
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setText(getString(R.string.change_pwd));
		mEtOld = (EditText) findViewById(R.id.et_old_pwd);
		mEtNew = (EditText) findViewById(R.id.et_new_pwd);
		mEtNewRe=(EditText) findViewById(R.id.et_new_pwd_reapt);
		mEtNewRe.addTextChangedListener(mTextWatcher);
		mTvToast=(TextView)findViewById(R.id.tv_change_toast);
		findViewById(R.id.bt_confirm).setOnClickListener(ll);
		findViewById(R.id.btn_back).setOnClickListener(ll);
	}

    private TextWatcher mTextWatcher=new TextWatcher(){

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
			if(mTvToast.isShown())
			{
				mTvToast.setVisibility(View.INVISIBLE);
			}
		}};
	private OnClickListener ll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;
			
			case R.id.bt_confirm:
				change();
				break;
			default:
				break;
			}

		}
	};
	private void change(){
		final String old = mEtOld.getText().toString();
		if (StringUtil.isNullOrEmpty(old)){
			Toast.makeText(ActChangePwd.this, getString(R.string.tag_old_password)+"不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		
		final String newpwd = mEtNew.getText().toString();
		if (StringUtil.isNullOrEmpty(newpwd)){
			Toast.makeText(ActChangePwd.this, getString(R.string.tag_new_password)+"不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		
		String newPwdReapt=mEtNewRe.getText().toString();
		if(newPwdReapt.isEmpty()||!newpwd.equals(newPwdReapt))
		{
			mTvToast.setVisibility(View.VISIBLE);
			mTvToast.setText("*新密码两次输入不一致");
			return;
		}
		
		
		new PostGetTask<JSONObject>(this, R.string.reseting_pwd,
				R.string.reset_pwd_failed, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				return AccountHttpMgr.getInstance().changePwd(Cookies.getUserId(),Cookies.getToken(),Cookies.getAccount(), old, newpwd);
			}

			@Override
			protected void doPostJob(Exception e, JSONObject result) {
				if (e == null && result != null) {
					try {
						if (result.getInt("result") != 1) {
							Toast.makeText(ActChangePwd.this,
									result.getString("msg"), Toast.LENGTH_LONG)
									.show();
							return;
						}
						Toast.makeText(ActChangePwd.this,
								"密码修改成功", Toast.LENGTH_LONG)
								.show();
						finishAndAni();
					} catch (JSONException e1) {
						Toast.makeText(ActChangePwd.this,
								getString(R.string.change_pwd_failed),
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
