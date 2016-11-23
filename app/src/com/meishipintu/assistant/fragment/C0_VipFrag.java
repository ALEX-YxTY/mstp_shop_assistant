package com.meishipintu.assistant.fragment;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.orderdish.ActCaptureTicket;
import com.meishipintu.assistant.ui.ActVipCenterNew;
import com.meishipintu.core.utils.ConstUtil;
import com.meishipintu.jnilibrary.JNIUtils;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;
import com.milai.http.ServerUrlConstants;
import com.milai.model.Coupons;
import com.milai.utils.CustomProgressDialog;
import com.milai.utils.TimeUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class C0_VipFrag extends Fragment{

	private static final int VIP_SCAN = 50;
	private static final long BIG_PRIME = 9785935849l;
	private FragClickListener mFraglistener;
    
	private String mTelNum = null;
	private int mIsFrom = 0;		////0直接打开，1。从收银

	private PostGetTask<JSONObject> mGetVerifyTask = null;

	private EditText mEtVerifyTel = null;
	private Button mButtonVerifyTelOrSn = null;
	protected CustomProgressDialog mLoadingDialog;
	
    public static C0_VipFrag createInstance(FragClickListener l) {
    	C0_VipFrag f = new C0_VipFrag();
		f.mFraglistener = l;
        return f;
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.frag_vip,null);
		
		mButtonVerifyTelOrSn = (Button) view.findViewById(R.id.bt_verify);
		mButtonVerifyTelOrSn.setOnClickListener(mClickListener);

		TextView tvClearTel = (TextView) view.findViewById(R.id.tv_clear_tel);
		tvClearTel.setOnClickListener(mClickListener);

		mEtVerifyTel = (EditText) view.findViewById(R.id.et_vip_tel);
		mEtVerifyTel.setFocusable(true);
		mEtVerifyTel.requestFocus();
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		TextView tvIconScan=(TextView)view.findViewById(R.id.tv_icon_scan);
		tvIconScan.setOnClickListener(mClickListener);
		
		mEtVerifyTel.addTextChangedListener(mTextWatcher);		
		return view;
	}
	
	
	private TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			formateNumberAsTelOrSn(s);
		}
	};
	
	
	private void formateNumberAsTelOrSn(Editable s) {
		if(s.length()<3)
		{
			return;
		}
		if(s.charAt(0)=='1')
		{
			if (s.length() > 3 && s.length() <= 13) {
				if (s.charAt(3) != ' ') {
					s.insert(3, " ");
				}
				if (s.length() > 8) {
					if (s.charAt(8) != ' ') {
						s.insert(8, " ");
					}
				}
			}
			if(s.length()>13)
			{
				s.delete(12, s.length() - 1);
				selfToastShow("手机号码长度为11位");
				return;
			}		
		}else {
			if (mIsFrom == 1){
				s.delete(0, s.length());
				selfToastShow("请输入正确的手机号码");
				return;
			}
			if (s.length() > 4 && s.length() <14) {
				if (s.charAt(4) != ' ') {
					s.insert(4, " ");
				}
				if (s.length() > 9) {
					if (s.charAt(9) != ' ') {
						s.insert(9, " ");
					}
				}
			}
			
		}
	}

	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.bt_verify: {
				String getTelString=mEtVerifyTel.getText().toString();
				if(getTelString.length()>0)
				{
					setTel(getTelString);
				}else{
					selfToastShow("输入不能为空");
					return;
				}
				String tel = getTel().replace(" ", "");// 直接从vip进入的方式，此字符串有可能为Sn
				if (tel.length() == 12 && mIsFrom != 1)// 等于1的情况下不能验证sn
				{
					verifySnFromNet(tel);
				} else {
					verifyTelNative(tel);
				}
				break;
			}
			case R.id.tv_icon_scan:{
				Intent intent = new Intent();
				intent.setClass(getActivity(), ActCaptureTicket.class);
				intent.putExtra("CHECK_CODE", 4);//从会员扫码进入
				C0_VipFrag.this.startActivityForResult(intent, VIP_SCAN);
				break;
			}
			case R.id.tv_clear_tel: {
				mEtVerifyTel.setText("");
				break;
			}
			}
		}
	};

	//验证券号
	private void verifySnFromNet(final String couponSn) {
		if (couponSn.length() == 0) {
			selfToastShow("券号不能为空");
			return;
		}
		if (couponSn.length() > 12) {
			selfToastShow("券号格式不正确");
			return;
		}
		mLoadingDialog = new CustomProgressDialog(this.getActivity(), "正在验证红包");
		mLoadingDialog.show();
		new PostGetTask<JSONObject>(this.getActivity()) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jParam = new JSONObject();
				jParam.put("uid", Cookies.getUserId());
				jParam.put("totalFee", "9999");// totalfee
				jParam.put("shopId", Cookies.getShopId());// shopid
				jParam.put("token", Cookies.getToken());
				jParam.put("couponSn", couponSn);
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getCouponVerifyUrl(), jParam, true);
				Log.i("test", "jRet useCouponNet:" + jRet.toString());
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				if (exception == null && result != null) {
					try {
						if (result.getInt("result") == 1) {
							JSONObject jData = result
									.getJSONObject("couponData");
							String couponName = jData.getString("couponName");
							float couponValue = Float.parseFloat(jData
									.getString("couponValue"));
							String couponSnFromNet = jData
									.getString("couponSn");
							String couponId=jData.getString("couponId");
							long startTime = jData.getLong("startTime");
							long endTime = jData.getLong("endTime");
							if (couponSnFromNet.equals(couponSn)) {
								showDialogCouponDetail(couponName, couponValue,
										couponSnFromNet, startTime, endTime,couponId);
							} else {
								selfToastShow("券号不一致");
							}
						} else {
							if (result.has("msg")) {
								String msg = result.getString("msg");
								selfToastShow(msg);
							}
						}
					} catch (JSONException e) {
						selfToastShow( "数据解析失败");
					}
					if (mLoadingDialog.isShowing()) {
						mLoadingDialog.dismiss();
					}
				}
			}
		}.execute();
	}

	private String mCouponSn=null;
	private String mCouponId=null;
	private Dialog mDialogShowDetail= null;

	//弹出卡券详细信息窗
	private void showDialogCouponDetail(String couponName, float couponValue,
			String couponSn, long startTime, long endTime,String couponId) {
		mCouponSn=couponSn;
		mCouponId=couponId;
		mDialogShowDetail= new Dialog(this.getActivity(), R.style.dialog);
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.dialog_coupon_detail, null);

		TextView tvValue = (TextView) v.findViewById(R.id.tv_coupon_value);
		tvValue.setText(String.format("%.2f", couponValue));
		RelativeLayout rlSendSn = (RelativeLayout) v.findViewById(R.id.rl_et_verify);
		rlSendSn.setVisibility(View.GONE);
		LinearLayout llSn = (LinearLayout) v.findViewById(R.id.ll_coupon_sn);
		llSn.setVisibility(View.VISIBLE);
		
		TextView tvSn = (TextView) v.findViewById(R.id.tv_coupon_sn);
		String temp=couponSn.substring(0, 4)+" "+couponSn.substring(4, 8)+" "+couponSn.substring(8, couponSn.length());
		tvSn.setText(temp);
		
		TextView tvSep1=(TextView)v.findViewById(R.id.tv_sep_1);
		tvSep1.setVisibility(View.GONE);

		String timeEndString = TimeUtil.convertLongToFormatString(
				endTime * 1000, "yyyy.MM.dd");
		String timeStartString = TimeUtil.convertLongToFormatString(
				startTime * 1000, "yyyy.MM.dd");
		String dateLimit = timeStartString + "—" + timeEndString + "可用";

		TextView tvContent = (TextView) v.findViewById(R.id.tv_coupon_detail);
		tvContent.setVisibility(View.GONE);
		// tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());

		TextView tvDateLimit = (TextView) v.findViewById(R.id.tv_date_limit);
		tvDateLimit.setText(dateLimit);// 红包可用时间

		Button btUseCoupon = (Button) v.findViewById(R.id.bt_use_coupon);
		btUseCoupon.setVisibility(View.VISIBLE);
		btUseCoupon.setText("使用");
		btUseCoupon.setOnClickListener(onDialogButtonListener);

		mDialogShowDetail.setContentView(v);
		mDialogShowDetail.show();
	}

	private void clearCoupon()
	{
		mCouponSn="";
		mCouponId="";
		mEtVerifyTel.setText("");
		if(mDialogShowDetail!=null&&mDialogShowDetail.isShowing())
		{
			mDialogShowDetail.dismiss();
		}
	}

	private OnClickListener onDialogButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.bt_use_coupon: {
				useCouponNet(mCouponSn,mCouponId);
				break;
			}
			}
		}
	};

	//在线使用卡券
	private void useCouponNet(final String couponSn,final String couponId)
	{
		new PostGetTask<JSONObject>(this.getActivity()) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jsonParams = new JSONObject();
				jsonParams.put("shopId", Cookies.getShopId());
				jsonParams.put("totalFee", 9999);
				jsonParams.put("uid", Cookies.getUserId());
				jsonParams.put("token", Cookies.getToken());
				jsonParams.put("couponSn", couponSn);
				jsonParams.put("couponId", couponId);
				jsonParams.put("type", 1);		//仅使用卡券
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getUseCouponResult(), jsonParams, true);
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				if (exception == null && result != null) {
					try {
						if (result.getInt("result") == 1) {
							//selfToastShow("红包验证使用成功");
							mDialogShowDetail.dismiss();
							final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
							View dialogView = View.inflate(getActivity(), R.layout.mydialog_okdialogview, null);
							dialog.setContentView(dialogView);
							TextView tvTitle = (TextView) dialogView.findViewById(R.id.dialog_title);
							tvTitle.setText("提示");
							TextView tvMessage = (TextView) dialogView.findViewById(R.id.dialog_message);
							tvMessage.setText("   红包验证使用成功   ");
							Button btOk = (Button) dialogView.findViewById(R.id.ok);
							btOk.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									if ( dialog.isShowing()) {
										dialog.dismiss();
										mEtVerifyTel.setText("");
									}
								}
							});
							dialog.show();
						}else{
							String msg="红包使用失败";
							if(result.has("msg"))
							{
								try {
									msg = result.getString("msg");
								} catch (Exception e) {
								}
								
							}
							selfToastShow(msg);
						}
					} catch (JSONException e) {
						selfToastShow("数据解析失败");
					}
				}
			}
		}.execute();
	}
	private void selfToastShow(String message) {
		Toast toast=Toast.makeText(this.getActivity().getBaseContext(), "", Toast.LENGTH_LONG);
		LayoutInflater infalter=(LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v=infalter.inflate(R.layout.layout_toast, null);
		TextView tvContent=(TextView)v.findViewById(R.id.tv_toast_content);
		tvContent.setText(message);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setView(v);
		toast.show();
	}

	private void verifyTelNative(String tel) {
		if (tel != null && tel.length() > 0) {
			tel = tel.replace(" ", "");
			if (tel.length() == 11) {
				String stringCompile = "^1[3|4|5|7|8][0-9]\\d{4,8}";
				Pattern pattern = Pattern.compile(stringCompile);
				Matcher matcher = pattern.matcher(tel);
				Boolean result = matcher.find();
				if (result) {
					verifyTelFromNet(tel);
				} else {
					selfToastShow("请输入正确号码");
				}
			} else {
				selfToastShow("请输入正确号码");
			}
		} else {
			selfToastShow("号码或验证码不能为空");
		}
	}

	//从网络验证用户信息
	private void verifyTelFromNet(final String tel) {
		mGetVerifyTask = new PostGetTask<JSONObject>(this.getActivity()) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jsonParams = new JSONObject();
				jsonParams.put("shopId", Cookies.getShopId());
				jsonParams.put("mobile", tel);
				jsonParams.put("uid", Cookies.getUserId());
				jsonParams.put("token", Cookies.getToken());
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getVerifyTelUrl(), jsonParams, true);
				Log.i("test", "jRet_show" + jRet.toString());
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				if (exception == null && result != null) {
					try {
						if (result.getInt("result") == 1) {
							int items = result.getInt("items");
							String name = result.getString("name");
							int sex = 0;
							try {
								sex = result.getInt("sex");
							} catch (JSONException e1) {
								sex = 0;
							}
							JSONArray data = null;
							try {
								data = result.getJSONArray("list");
							} catch (JSONException e) {
								selfToastShow("解析红包数据错误");
							}
							String amount = result.getString("amount");
							String average = result.getString("average");
							Log.d("name:sex",
									name + ":" + Integer.toString(sex));
							//取出总米数和可用米数
							JSONObject signInfo = result.getJSONObject("signinfo");
							Log.i("test", "signIngo:" + signInfo.toString());
							int totalMi = 0;
							if (signInfo.getInt("result") == 1) {
								totalMi = signInfo.getInt("credit");
							}
							setData(tel, name, sex, items, amount, average,
									data,totalMi);
						}
					} catch (JSONException e) {
						selfToastShow("网络连接失败，请检查网络配置");
					}
				}
			}
		};
		mGetVerifyTask.execute();
	}

	//设置intent并跳转至VipCenter界面
	private void setData(String tel, String name, int sex, int items,
						 String amount, String average, JSONArray arrayJsonCoupon, int totalMi)
			throws JSONException {

		// Toast.makeText(getBaseContext(), "setData",
		// Toast.LENGTH_LONG).show();
		Intent in = new Intent();
		Bundle bd = new Bundle();
		in.setClass(getActivity(), ActVipCenterNew.class);
		in.putExtra("tel", tel);
		in.putExtra("items", items);
		in.putExtra("amount", amount);
		in.putExtra("average", average);
		in.putExtra("totalMi", totalMi);
		if (name != null && name.length() > 0 && !name.equals("null")) {
			in.putExtra("name", name);
			Log.i("name", name);
		} else {
			in.putExtra("name", "暂无");
		}

		in.putExtra("sex", sex);
		in.putExtra("From", mIsFrom);
		ArrayList<Coupons> couponArray = null;
		if (arrayJsonCoupon != null && arrayJsonCoupon.length() > 0) {
			couponArray = new ArrayList<>();
			for (int i = 0; i < arrayJsonCoupon.length(); i++) {
				JSONObject coupon = arrayJsonCoupon.getJSONObject(i);
				try {
					Coupons couponMode = new Coupons(coupon);
					if (couponMode.getMinPrice().equals("0")
							&& (Double.parseDouble(couponMode.getCouponValue()) > 0.0)) {
						couponArray.add(couponMode);
					}
				} catch (Exception e) {
					Log.i("ActVerifyVipTel", "数据解析异常");
					e.printStackTrace();
				}
			}
		}
		in.putExtra("dataArray", couponArray);
		this.startActivityForResult(in, ConstUtil.REQUEST_CODE.PAY_REQUEST_VIP_INFO);
		clearCoupon();
	}

	private void setTel(String tel) {
		if (tel.length() > 0) {
			this.mTelNum = tel;
		}
	}

	private String getTel() {
		return mTelNum;
	}

	private String formateTelString(String tel) {
		StringBuffer buffer = new StringBuffer();
		tel = tel.replace(" ", "");
		buffer.append(tel);
		String temp = "";
		if (tel.length() > 3) {
			buffer.insert(3, " ");
			if (tel.length() > 7) {
				buffer.insert(8, " ");
			}
		} else {
			temp = tel;
		}
		temp = buffer.toString();
		Log.d("format_tel", temp);
		return temp;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==Activity.RESULT_OK) {
			switch(requestCode)
			{
			case VIP_SCAN:{			//从扫码返回
				if(data!=null)
				{
					String sn=data.getStringExtra("CSN");
					mEtVerifyTel.setText(sn);
				}else{
					selfToastShow("取消扫码");
				}
				break;
			}
			case ConstUtil.REQUEST_CODE.PAY_REQUEST_VIP_INFO:{
				this.getActivity().setResult(Activity.RESULT_OK,data);
				//finish();
				break;
			}
			}
		} else if (resultCode == 100 && requestCode == VIP_SCAN) {
			//读取c端二维码
			String pwd = data.getStringExtra("pwd");
			if (checkPwd(pwd)) {
				//二维码验证成功
				Intent intent = new Intent(getActivity(), ActVipCenterNew.class);
				intent.putExtra("From", 2);        //from =2 扫描进入
				intent.putExtra("tel", mTelNum);
				startActivity(intent);
			} else {
				selfToastShow("无效二维码");
				mTelNum = "";
			}
		}
	}

	private boolean checkPwd(String pwd) {
		String rawPwd = pwd.substring(2);
		long totpGet = Long.valueOf(rawPwd) / BIG_PRIME;
		long numberGet = Long.valueOf(rawPwd) % BIG_PRIME;
		mTelNum = "1" + numberGet;
		Log.i("test", "totp:" + totpGet + ",tel:" + mTelNum);
		long timeStamp = System.currentTimeMillis() / 1000 / 60;		//步进值
//		long timeStamp = 12345678l;
		for(int i=-2;i<3;i++) {
			String totpGen = JNIUtils.getTotp(mTelNum, (int) timeStamp + i);
			Log.i("test", "totpGen:" + totpGen + ", tel:" + mTelNum);
			if (totpGen.equals(totpGet + "")) {
				return true;
			}
		}
		return false;
	}

}
