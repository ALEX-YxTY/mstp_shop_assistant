package com.meishipintu.assistant.ui.auth;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.milai.http.AccountHttpMgr;
import com.milai.http.ServerUrlConstants;
import com.meishipintu.assistant.ui.ActVipCenter;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;

public class ActPopupMemberDetail extends PopupWindow {

	private FragmentActivity mParentActivity;
	private View mMainView;

	private Button mChangePerson = null;
	private Button mSubmit = null;

	private EditText mEtNick = null;
	private EditText mEtTel = null;
	private EditText mEtName = null;
	private EditText mEtSex = null;
	private EditText mEtDate = null;
	private EditText mEtEmail = null;
	private EditText mEtAdress = null;
	private EditText mEtVerifyCode=null;
	private TextView mButtonSendVerifyCode=null;

	private String mWaiterUid = null;
	private String mTel = null;
	private String mNickName = "";
	private String mRealname = "";
	private String mGender = "";
	private int mIntGender = 0;
	private String mBrithdate = "";
	private String mEmail = "";
	private String mAddress = "";
	private int mCount=0;

	public static final String DATEPICKER_TAG = "datepicker";
	private boolean mIsChange = false;

	public ActPopupMemberDetail(FragmentActivity context, String tel) {
		super(context);
		mParentActivity = context;
		LayoutInflater inflater = (LayoutInflater) mParentActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMainView = inflater.inflate(R.layout.layout_my_information_detail,
				null);
		this.setContentView(mMainView);
		mTel = tel;
		mEtNick = (EditText) mMainView.findViewById(R.id.et_nick);
		mEtTel = (EditText) mMainView.findViewById(R.id.et_tel);
		mEtName = (EditText) mMainView.findViewById(R.id.et_name);
		mEtSex = (EditText) mMainView.findViewById(R.id.et_gender);
		mEtDate = (EditText) mMainView.findViewById(R.id.et_birthday);
		mEtEmail = (EditText) mMainView.findViewById(R.id.et_email);
		mEtAdress = (EditText) mMainView.findViewById(R.id.et_address);
		mEtVerifyCode=(EditText) mMainView.findViewById(R.id.et_verify_code);
		mButtonSendVerifyCode=(TextView) mMainView.findViewById(R.id.tv_verify);
		
		mSubmit = (Button) mMainView.findViewById(R.id.btn_submit_change);
		mSubmit.setOnClickListener(mOnClickListener);

		mChangePerson = (Button) mMainView.findViewById(R.id.btn_right);
		mChangePerson.setOnClickListener(mOnClickListener);

		Button btnBack=(Button)mMainView.findViewById(R.id.btn_left);
		btnBack.setOnClickListener(mOnClickListener);
		getData();
		initPopupShow();
	}

	private void getData() {
		new PostGetTask<JSONObject>(mParentActivity, R.string.loading,
				R.string.fail_load_more, false, true, false) {
			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jPara = new JSONObject();
				jPara.put("uid", Cookies.getUserId());
				jPara.put("shopId", Cookies.getShopId());
				jPara.put("mobile", mTel);
				jPara.put("token", Cookies.getToken());
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getMemberDetail(), jPara, false);
				return jRet;
			}

			@Override
			protected void doPostJob(Exception e, JSONObject userDetail) {
				if (userDetail != null && e == null) {
					try {
						if (userDetail.getInt("result") == 1) {
							JSONObject memberInfo = userDetail
									.getJSONObject("memberInfo");
							mNickName = memberInfo.getString("name");
							mTel = memberInfo.getString("tel");
							mRealname = memberInfo.getString("realname");
							mIntGender = memberInfo.getInt("sex");
							if (mIntGender == 0) {
								mGender = "男";
								mMainView
										.findViewById(R.id.iv_gender)
										.setBackgroundDrawable(
												mParentActivity
														.getResources()
														.getDrawable(
																R.drawable.male));
							} else if (mIntGender == 1) {
								mGender = "女";
								mMainView
										.findViewById(R.id.iv_gender)
										.setBackgroundDrawable(
												mParentActivity
														.getResources()
														.getDrawable(
																R.drawable.female));
							}
							mBrithdate = memberInfo.getString("birthdate");
							mEmail = memberInfo.getString("email");
							mAddress = memberInfo.getString("address");
							refreshView(false);
							ActVipCenter.mVipCenter.refreshChangeInfo(mNickName, mIntGender);
						}else
						{
							refreshView(true);
							String msg=userDetail.getString("msg");
							Toast.makeText(mParentActivity.getBaseContext(),
									"获取用户详情失败,"+msg, Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e1) {
						Toast.makeText(mParentActivity.getBaseContext(),
								"数据解析异常", Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(mParentActivity.getBaseContext(), "网络连接失败",
							Toast.LENGTH_LONG).show();
				}
			}
		}.execute();
	}

	private void refreshView(boolean isChange) {
		mIsChange = isChange;
		mEtNick.setEnabled(isChange);
		mEtTel.setEnabled(false);
		mEtName.setEnabled(isChange);
		mEtSex.setEnabled(isChange);
		mEtSex.setFocusable(false);
		mEtDate.setEnabled(isChange);
		mEtDate.setFocusable(false);
		
		mEtEmail.setEnabled(isChange);
		mEtAdress.setEnabled(isChange);
		
		mEtNick.setText(mNickName);
		mEtTel.setText(mTel);
		mEtName.setText(mRealname);
		if (mIntGender == 0) {
			mEtSex.setText("男");
		} else {
			mEtSex.setText("女");
		}

		mEtDate.setText(mBrithdate);
		mEtEmail.setText(mEmail);
		mEtAdress.setText(mAddress);
		if (isChange) {
			mChangePerson.setText("完成");
			mSubmit.setVisibility(View.VISIBLE);
			mMainView.findViewById(R.id.rl_verify).setVisibility(View.VISIBLE);
			mButtonSendVerifyCode.setOnClickListener(mOnClickListener);
			mEtSex.setOnClickListener(mOnClickListener);
			mEtDate.setOnClickListener(mOnClickListener);
		} else {
			mChangePerson.setText("修改");
			mSubmit.setVisibility(View.GONE);
			mMainView.findViewById(R.id.rl_verify).setVisibility(View.GONE);
		}
	}

	private void updateDate() {
		
		final String nickName=mEtNick.getText().toString();
		final String realName=mEtName.getText().toString();
		final String gender=mEtSex.getText().toString();
		final String birDate=mEtDate.getText().toString();
		final String email=mEtEmail.getText().toString();
		final String address=mEtAdress.getText().toString();
		final String verifyCode=mEtVerifyCode.getText().toString();
		if(verifyCode.length()==0)
		{
			Toast.makeText(mParentActivity, "验证码不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		new PostGetTask<JSONObject>(mParentActivity, R.string.loading,
				R.string.fail_load_more, false, true, false) {
			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jPara = new JSONObject();
				jPara.put("uid", Cookies.getUserId());
				jPara.put("shopId", Cookies.getShopId());
				jPara.put("mobile", mTel);
				jPara.put("name", nickName);
				jPara.put("realname",realName);
				if(gender.equals("男"))
				{
					mIntGender=0;
				}else{
					mIntGender=1;
				}
				jPara.put("sex", mIntGender);
				jPara.put("birthdate", birDate);
				jPara.put("email", email);
				jPara.put("address", address);
				jPara.put("token", Cookies.getToken());
				jPara.put("verify", verifyCode);
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.updateMemberDetail(), jPara, false);
				return jRet;
			}

			@Override
			protected void doPostJob(Exception e, JSONObject userDetail) {
				if (userDetail != null && e == null) {
					try {
						if(userDetail.getInt("result")==1)
						{
							getData();
							Toast.makeText(mParentActivity.getBaseContext(),
									"个人信息更新成功", Toast.LENGTH_LONG).show();
						}else{
							
							if(userDetail.has("msg"))
							{
								String msg=userDetail.getString("msg");
								Toast.makeText(mParentActivity.getBaseContext(),
										"更新用户信息失败，"+msg, Toast.LENGTH_LONG).show();
							}
						}		
					} catch (JSONException e1) {
						Toast.makeText(mParentActivity.getBaseContext(),
								"数据解析错误", Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(mParentActivity.getBaseContext(), "网络连接失败",
							Toast.LENGTH_LONG).show();
				}
			}
		}.execute();
	}
	
	private void sendVCode(final String tel) {

		new PostGetTask<JSONObject>(mParentActivity, R.string.sending_v_code,
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
							Toast.makeText(mParentActivity,
									result.getString("msg"), Toast.LENGTH_LONG)
									.show();
							mCount = 0;
						}
					} catch (JSONException e1) {
						Toast.makeText(mParentActivity,
								mParentActivity.getString(R.string.sent_v_code_fail),
								Toast.LENGTH_LONG).show();
						e1.printStackTrace();
					}
				}else {
					mCount = 0;
				}
			}
		}.execute();
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (id) {
			case R.id.btn_left:{
				dismiss();
				break;
			}
			case R.id.btn_right: {
				if (mIsChange) {
					// 提交数据
					updateDate();
				} else {
					refreshView(true);
				}
				break;
			}
			case R.id.btn_submit_change: {
				// 提交數據
				updateDate();
				break;
			}
			case R.id.tv_verify:
			{
				if (mCount == 0) {
					mCount = 60;
					mHandler.postDelayed(mCountDownTask, 1000);
					sendVCode(mTel);
				} else {
					Toast.makeText(mParentActivity,
							mParentActivity.getString(R.string.send_wait), Toast.LENGTH_LONG)
							.show();
				}
				break;
			}
			case R.id.et_gender:{
				showGenderDialog();
				break;
			}
			case R.id.et_birthday:{
				showDateDialog();
				break;
			}
			}
		}
	};
	private void showDateDialog()
	{
		final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(mDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),true);
        datePickerDialog.setYearRange(1980, 2016);
        datePickerDialog.setCloseOnSingleTapDay(false);
        
        datePickerDialog.show(mParentActivity.getSupportFragmentManager(), DATEPICKER_TAG);
//        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);
	}
	private OnDateSetListener mDateSetListener=new OnDateSetListener(){

		@Override
		public void onDateSet(DatePickerDialog datePickerDialog, int year,
				int month, int day) {
			// TODO Auto-generated method stub
//			Toast.makeText(mParentActivity, Integer.toString(year)+":"+Integer.toString(month)+":"+Integer.toString(day),Toast.LENGTH_LONG).show();
			mEtDate.setText(Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(day));
		}};
	private void showGenderDialog()
	{
		AlertDialog.Builder alert=new Builder(mParentActivity);
		alert.setTitle("请选择性别")
		.setSingleChoiceItems(new String[]{"男","女"},mIntGender,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(which==0)
				{
					mIntGender=0;
					mEtSex.setText("男");
					mMainView.findViewById(R.id.iv_gender).setBackgroundDrawable(mParentActivity.getResources().getDrawable(R.drawable.male));
				}else if(which==1)
				{
					mIntGender=1;
					mEtSex.setText("女");
					mMainView.findViewById(R.id.iv_gender).setBackgroundDrawable(mParentActivity.getResources().getDrawable(R.drawable.female));
				}
				dialog.dismiss();
			}
		}).create();
		alert.show();
	}

	private Handler mHandler = new Handler();

	private Runnable mCountDownTask = new Runnable() {

		public void run() {
			if (mCount > 0) {
				mHandler.postDelayed(this, 1000);
				mCount--;
				mButtonSendVerifyCode.setText(mCount + "秒");
			} else {
				mButtonSendVerifyCode.setText(mParentActivity.getString(R.string.verify));
			}
		}
	};
	void initPopupShow() {
		this.setWidth(LayoutParams.MATCH_PARENT); // 设置弹出窗体的宽
		this.setHeight(LayoutParams.MATCH_PARENT); // 设置弹出窗体的高
		this.setFocusable(true); // 设置弹出窗体可点击
		// this.setAnimationStyle(R.style.CustomDialog); // 设置弹出窗体动画效果
		ColorDrawable dw = new ColorDrawable(0x00ffffff); // 实例化一个ColorDrawable颜色为半透明
		this.setBackgroundDrawable(dw); // 设置弹出窗体的背景

		// this.setTouchInterceptor(new OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// final float left = mMainView.findViewById(R.id.rl_title).getX();
		// int width = mMainView.findViewById(R.id.rl_title).getWidth();
		// final float right = left + width;
		// // if(event.getX()<left||event.getX()>right)
		// // {
		// // dismiss();
		// // }
		// return false;
		// }
		// });
	}
	
}
