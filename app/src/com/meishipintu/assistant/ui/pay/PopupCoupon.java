package com.meishipintu.assistant.ui.pay;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.orderdish.ActCaptureTicket;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;
import com.milai.http.ServerUrlConstants;
import com.milai.utils.CustomProgressDialog;

public class PopupCoupon extends PopupWindow {

	private FragmentActivity mParentActivity = null;
	private View mMainView;
	protected CustomProgressDialog mLoadingDialog;
	private String mPainIn = "";
	private int mStatusCouponButton = 0;
	private Button mBtVerifyCoupon = null;
	private String mFormatCouponSn = "";
	private EditText mEtCouponSn = null;
	public static PopupCoupon mPopCoupon=null;

	private String mCouponName = "";
	private float mCouponValue = 0;
	private String mCouponSn = "";
	private boolean mUseCoupon = false;

	public PopupCoupon(FragmentActivity context, HashMap couponBundle) {

		super(context);
		mParentActivity = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMainView = inflater.inflate(R.layout.popupwindow_coupon, null);
		setContentView(mMainView);
		mPopCoupon=this;
		mBtVerifyCoupon = (Button) mMainView.findViewById(R.id.bt_get_verify);
		mBtVerifyCoupon.setOnClickListener(mClkListener);

		Button mBtDismiss = (Button) mMainView.findViewById(R.id.bt_dismiss);
		mBtDismiss.setOnClickListener(mClkListener);
		mEtCouponSn = (EditText) mMainView
				.findViewById(R.id.et_coupon_money_sn);
		
		
		mPainIn = couponBundle.get("paidIn").toString();
		mCouponName = couponBundle.get("couponName").toString();
		mCouponValue = Float.parseFloat(couponBundle.get("couponValue")
				.toString());
		mCouponSn = couponBundle.get("couponSn").toString();
		int useCoupon=(Integer)couponBundle.get("couponUse");
		Log.d("useCoupon", Integer.toString(useCoupon));
		if(useCoupon==1)
		{
			mUseCoupon=true;
		}else{
			mUseCoupon=false;
		}
		mEtCouponSn.setText(formateCouponSn(mCouponSn));
		InputMethodManager m = (InputMethodManager) mParentActivity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);// 唤起软键盘
		// mEtCouponSn.setOnKeyListener(mOnKeyListener);//只能监听硬件
		if (mUseCoupon) {
			Log.d("mUseCoupon", "true");
			changeCouponVerifyStatu(2);
			changeCouponInfo(2, mCouponName, mCouponValue);
		} else if (mUseCoupon == false&&mCouponSn.length()>=12) {
			Log.d("mUseCoupon", "false");
			changeCouponVerifyStatu(3);
			changeCouponInfo(2, mCouponName, mCouponValue);
		}
		TextView tvScanCoupon=(TextView)mMainView.findViewById(R.id.tv_scan_coupon);
		tvScanCoupon.setOnClickListener(mClkListener);

		mEtCouponSn.addTextChangedListener(mWatcher);	
		mEtCouponSn.setInputType(InputType.TYPE_CLASS_PHONE);
		initShow();
	}

	private TextWatcher mWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			if(!mUseCoupon)
			{
				clearCoupon();
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub			
			
			if(s.length()>4)
			{
				if(s.charAt(4)!=' ')
				{
					s.insert(4, " ");
				}
			}
			if(s.length()>9)
			{
				if(s.charAt(9)!=' ')
				{
					s.insert(9, " ");
				}
			}
			if(s.length()>14)
			{
				if(s.charAt(14)!=' ')
				{
					s.insert(14, " ");
				}
			}
			String sn = s.toString();
			sn = sn.replace(" ", "");
			if(sn.length()>=12)
			{
				Log.d("onTextChangeFinish", Integer.toString(sn.length()));
				closeBoard();
			}else
			{
				clearCoupon();
			}
			if (sn.contains("\n")) {
				sn = sn.replace("\n", "");
				getConpon(sn);
			}
		}
	};

	private void closeBoard()
	{
		 InputMethodManager inputManager=(InputMethodManager)mParentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
		 mParentActivity.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);//有效
	}
	private void clearCoupon() {
		mCouponName = "";
		mCouponValue = 0;
		mCouponSn = "";
		mUseCoupon = false;
		mFormatCouponSn = "";
		changeCouponVerifyStatu(0);// 0为待验证状态
		changeCouponInfo(0, "", 0);
	}

	private OnClickListener mClkListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (id) {
			case R.id.bt_get_verify: {
				if (mStatusCouponButton == 0) {
					mFormatCouponSn = mEtCouponSn.getText().toString();
					String sn = mFormatCouponSn.replace(" ", "");
					Log.d("couponSn", sn);
					getConpon(sn);
				} else if (mStatusCouponButton == 1) {// 验证失败，功能重新输入
					mFormatCouponSn = "";
					mEtCouponSn.setText("");
					changeCouponVerifyStatu(0);// 0为待验证状态
					changeCouponInfo(0, "", 0);
					mEtCouponSn.setVisibility(View.VISIBLE);
					break;
				} else if (mStatusCouponButton == 2) {// 验证成功，功能不使用
					changeCouponVerifyStatu(3);
					useConpon(false, mCouponName, mCouponValue, mCouponSn);
					dismiss();
				} else if (mStatusCouponButton == 3) {// 验证成功，使用
					changeCouponVerifyStatu(2);
					useConpon(true, mCouponName, mCouponValue, mCouponSn);
					dismiss();
				}
				break;
			}
			case R.id.bt_dismiss: {
				dismiss();
				break;
			}
			case R.id.tv_scan_coupon:
			{
				Intent intent = new Intent();
				intent.setClass(mParentActivity, ActCaptureTicket.class);
				intent.putExtra("CHECK_CODE", 3);
				mParentActivity.startActivity(intent);
				break;
			}
			}
		}
	};

	private void useConpon(boolean use, String couponName, float couponValue,
			String couponSn) {
//		ActNewPayment.mActNewPayment.setCouponAndMi(use, couponName, couponValue,
//				couponSn);
	}

	public void setCouponSnToEditText(String couponSn)
	{
		clearCoupon();
		mEtCouponSn.setText(formateCouponSn(couponSn));
	}
	
	private String formateCouponSn(String sn) {
		sn=sn.replace(" ", "");
		if(sn.length()>13){
			Toast.makeText(mParentActivity, "红包码格式不正确，请重试", Toast.LENGTH_LONG).show();
			return "";
		}
		StringBuffer stringBuffer=new StringBuffer();
		stringBuffer.append(sn);
		if(stringBuffer.length()>4)
		{
			stringBuffer.insert(4, " ");
			if(stringBuffer.length()>9){
				stringBuffer.insert(9, " ");
				if(stringBuffer.length()>14){
					stringBuffer.insert(14, " ");
				}
			}
		}
		String formateString=stringBuffer.toString();
		return formateString;
	}

	// hcs
	private void getConpon(final String couponSn) {
		if (couponSn.length() ==0) {
			Toast.makeText(mParentActivity, "红包SN码不能为空",
					Toast.LENGTH_LONG).show();
			return;
		}
		if(couponSn.length()>12){
			Toast.makeText(mParentActivity, "红包SN码不正确",
					Toast.LENGTH_LONG).show();
			return;
		}
		mLoadingDialog = new CustomProgressDialog(mParentActivity, "正在验证红包");
		mLoadingDialog.show();
		new PostGetTask<JSONObject>(mParentActivity) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				// TODO Auto-generated method stub
				JSONObject jParam = new JSONObject();
				jParam.put("uid", Cookies.getUserId());
				jParam.put("totalFee", mPainIn);// totalfee
				jParam.put("shopId", Cookies.getShopId());// shopid
				jParam.put("token", Cookies.getToken());
				jParam.put("couponSn", couponSn);
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getCouponVerifyUrl(), jParam, true);
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				// TODO Auto-generated method stub
				if (exception == null && result != null) {
					try {
						if (result.getInt("result") == 1) {
							JSONObject jData = result
									.getJSONObject("couponData");
							mCouponName = jData.getString("couponName");
							mCouponValue = (float)jData.getDouble("couponValue");
							if (!mCouponSn.equals(couponSn)) {
								useConpon(false, "", 0, "");
							}
							mCouponSn = couponSn;
							changeCouponVerifyStatu(2);
							changeCouponInfo(2, mCouponName, mCouponValue);
							useConpon(true, mCouponName, mCouponValue,
									mCouponSn);
							dismiss();
						} else {
							if (result.has("msg")) {
								String msg = result.getString("msg");
								Toast.makeText(mParentActivity, msg,
										Toast.LENGTH_LONG).show();
								mStatusCouponButton = 1;// 状态1验证失败，重新输入
								changeCouponVerifyStatu(1);
								changeCouponInfo(1, msg, 0);
								useConpon(false, "", 0, "");
							}
						}
						
					} catch (JSONException e) {
						Toast.makeText(mParentActivity, "红包验证失败",
								Toast.LENGTH_LONG).show();
					}
					if (mLoadingDialog.isShowing()) {
						mLoadingDialog.dismiss();
					}
				}
			}
		}.execute();
	}

	private void changeCouponInfo(int status, String couponName, float couponValue) {
		TextView ivCouponAble = (TextView) mMainView
				.findViewById(R.id.iv_coupon_able);
		TextView tvCouponValue = (TextView) mMainView
				.findViewById(R.id.tv_coupon_value);
		TextView tvCouponAble = (TextView) mMainView
				.findViewById(R.id.tv_coupon_able);
		switch (status) {
		case 0:
			mMainView.findViewById(R.id.ll_coupon_info)
					.setVisibility(View.GONE);
			break;
		case 1:
			ivCouponAble.setBackgroundResource(R.drawable.invisible);// (mParentActivity.getResources().getDrawable(R.drawable.invisible));
			tvCouponAble.setText("验证失败或不满足使用条件");
			tvCouponValue.setText("");
			mMainView.findViewById(R.id.ll_coupon_info).setVisibility(
					View.VISIBLE);
			break;
		case 2:
			ivCouponAble.setBackgroundResource(R.drawable.visible);
			mMainView.findViewById(R.id.ll_coupon_info).setVisibility(
					View.VISIBLE);
			tvCouponValue.setText(couponName);
			tvCouponAble.setText("，可以使用");
			break;
		}
	}

	private void changeCouponVerifyStatu(int status) {
		mStatusCouponButton = status;
		switch (status) {
		case 0: {
			mBtVerifyCoupon.setVisibility(View.VISIBLE);
			mBtVerifyCoupon.setText("验 证");
			break;
		}
		case 1: {
			mBtVerifyCoupon.setVisibility(View.VISIBLE);
			mBtVerifyCoupon.setText("重新输入");
			break;
		}
		case 2: {
			mBtVerifyCoupon.setVisibility(View.VISIBLE);
			mBtVerifyCoupon.setText("不使用");
			break;
		}
		case 3: {
			mBtVerifyCoupon.setVisibility(View.VISIBLE);
			mBtVerifyCoupon.setText("使 用");
			break;
		}
		}
	}

	void initShow() {
		this.setWidth(LayoutParams.FILL_PARENT); // 设置弹出窗体的宽
		this.setHeight(LayoutParams.FILL_PARENT); // 设置弹出窗体的高
		this.setFocusable(true); // 设置弹出窗体可点击
		ColorDrawable dw = new ColorDrawable(0x0a000000); // 实例化一个ColorDrawable颜色为半透明
		this.setBackgroundDrawable(dw); // 设置弹出窗体的背景
	}

	private void setFocus(boolean focus) {
		this.setFocusable(focus);
	}
	
}
