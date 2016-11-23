package com.meishipintu.assistant.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.interfaces.OnUseCouponOrMi;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;
import com.milai.http.ServerUrlConstants;
import com.milai.model.Coupons;
import com.meishipintu.assistant.adapter.AdapterCoupons;
import com.meishipintu.assistant.ui.auth.ActPopupMemberDetail;
import com.meishipintu.core.utils.MyDialogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActVipCenter extends FragmentActivity implements OnUseCouponOrMi{

	private TextView mTvTitle = null;
	private String mTel = "";
	private int isFromCode = 0;//0直接打开，1。从收银
	public static ActVipCenter mVipCenter;
	private CheckBox cbUseMi;
	private boolean mMiUsed = false;
	private  AdapterCoupons adapter = null;
	private ArrayList<Coupons> couponArray = null;

	private int discount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_vip_center);
		Intent inFrom = getIntent();
		isFromCode = inFrom.getIntExtra("From", 0);
		if (isFromCode == 1) {
			discount = inFrom.getIntExtra("discount", 0);
		}
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mTvTitle.setText("会员详情");
		Button btBack = (Button) findViewById(R.id.btn_back);
		btBack.setOnClickListener(mClickListener);
		//从收银进入显示使用米
		cbUseMi = (CheckBox) findViewById(R.id.cb_use_mi);
		if (isFromCode == 1) {
			cbUseMi.setVisibility(View.VISIBLE);
			cbUseMi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					TextView tvMiUseCheck = (TextView) findViewById(R.id.tv_mi_use_check);
					tvMiUseCheck.setText(isChecked ? "使用米币" : "未使用");
					mMiUsed = isChecked;
					Log.i("test", "isMiused" + mMiUsed);
				}
			});
		}

		Button btChangeDetail=(Button)findViewById(R.id.bt_title_right);
		btChangeDetail.setVisibility(View.VISIBLE);
		btChangeDetail.setOnClickListener(mClickListener);
		btChangeDetail.setBackgroundResource(R.drawable.btn_change_vip_deail);
		mVipCenter = this;
		
		getDataFromTel(inFrom);
	}
	
	private void getDataFromTel(Intent data)
	{
		if (data != null) {
			int items = data.getIntExtra("items", 0);
			String amount = data.getStringExtra("amount");
			String average = data.getStringExtra("average");
			mTel = data.getStringExtra("tel");
			String name = data.getStringExtra("name");
			int totalMi = data.getIntExtra("totalMi", 0);
			int sex = data.getIntExtra("sex", 0);
			couponArray = (ArrayList<Coupons>) data
					.getSerializableExtra("dataArray");
			if (couponArray != null && couponArray.size() > 0) {
				adapter = new AdapterCoupons(ActVipCenter.this,
						couponArray, isFromCode, mTel);
			}
			refreshView(mTel, name, sex, items, amount, average, totalMi);
		}else{
			if (mTel == null || mTel.length() != 11) {
				finish();
			}
		}
	}


	private void refreshView(final String tel, String name, int sex, int items,
							 String amount, String average, int totalMi) {
		mTel = tel;
		TextView tvTel = (TextView) findViewById(R.id.tv_vip_tel);
		TextView tvAmount = (TextView) findViewById(R.id.tv_amount);
		TextView tvItems = (TextView) findViewById(R.id.tv_items);
		TextView tvAverage = (TextView) findViewById(R.id.tv_average);
		TextView tvName = (TextView) findViewById(R.id.tv_user_name);
		TextView tvIconSex=(TextView)findViewById(R.id.icon_user_sex);
		TextView tvSex = (TextView) findViewById(R.id.tv_user_sex);
		TextView tvTotalMi = (TextView) findViewById(R.id.mi_amount);
		TextView tvMiDiscount = (TextView) findViewById(R.id.tv_mi_use);
		RelativeLayout rlUseMi = (RelativeLayout) findViewById(R.id.rl_use_mi);

		TextView tvNoCoupon = (TextView) findViewById(R.id.tv_no_coupon);

		findViewById(R.id.ll_tel).setOnClickListener(mClickListener);

		TextView tvPay = (TextView) findViewById(R.id.tv_pay);
		tvPay.setOnClickListener(mClickListener);
		if (isFromCode == 0)        //从会员界面进入
		{
			tvPay.setVisibility(View.GONE);
			rlUseMi.setVisibility(View.GONE);
			tvMiDiscount.setText("--");
		} else {
			tvMiDiscount.setText(discount + "");
		}

		if (name != null && name.length() > 0) {
			tvName.setText(name);
		} else {
			tvName.setText("暂无");
		}
		if (sex == 0) {
			tvIconSex.setBackgroundResource(R.drawable.icon_vip_usersex_male);
			tvSex.setText("男");
		} else {
			tvIconSex.setBackgroundResource(R.drawable.icon_vip_usersex_female);
			tvSex.setText("女");
		}
		tvTel.setText(formateTelString(tel));
		tvItems.setText(Integer.toString(items));

		tvAmount.setText(amount);
		tvAverage.setText(average);
		tvTotalMi.setText(totalMi+"");
		ListView dataListView = (ListView) findViewById(R.id.listview_coupon);
		if (adapter != null){
			dataListView.setAdapter(adapter);
		} else {
			tvNoCoupon.setVisibility(View.VISIBLE);
		}
	}

	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.btn_back: {			
				finish();
				break;
			}
			//支付，isFromCode=0时，不显示
			case R.id.tv_pay: {
				payToPayment(mTel);
				break;
			}
			case R.id.ll_tel: {
				dailDialog();
				break;
			}
			case R.id.bt_title_right:{
				ActPopupMemberDetail detail = new ActPopupMemberDetail(
						ActVipCenter.this, mTel);
				detail.showAtLocation(mTvTitle, Gravity.CENTER, 0, 0);
				break;
			}
			}
		}
	};

	//发送SN号码后返回支付页面
	public void payToPayment(String tel) {
		Intent in = new Intent();
		in.putExtra("USER_TEL", tel);
		in.putExtra("USE_MI", mMiUsed);
		Log.i("test", "use_mi:" + mMiUsed);
		ActVipCenter.this.setResult(RESULT_OK, in);
		finish();
	}

	//验证卡券并返回支付页面
	public void payToSn(String id, String sn, String name, String value, String minprice) {
		Intent in = new Intent();
		in.putExtra("USN_TEL", mTel);
		in.putExtra("USN_ID", id);
		in.putExtra("USN_SN", sn);
		in.putExtra("USN_NAME", name);
		in.putExtra("USN_VALUE", value);
		in.putExtra("USN_MINPRICE", minprice);
		in.putExtra("USE_MI", mMiUsed);
		Log.i("test", "use_mi:" + mMiUsed);
		ActVipCenter.this.setResult(RESULT_OK, in);
		finish();
	}
	
	public void refreshChangeInfo(String name,int sex)
	{
		TextView tvName = (TextView) findViewById(R.id.tv_user_name);
		TextView tvIconSex=(TextView)findViewById(R.id.icon_user_sex);
		TextView tvSex = (TextView) findViewById(R.id.tv_user_sex);
		
		if (name != null && name.length() > 0) {
			tvName.setText(name);
		} else {
			tvName.setText("暂无");
		}
		if (sex == 0) {
			tvIconSex.setBackgroundResource(R.drawable.icon_vip_usersex_male);
			tvSex.setText("男");
		} else {
			tvIconSex.setBackgroundResource(R.drawable.icon_vip_usersex_female);
			tvSex.setText("女");
		}
	}

	private void dailDialog() {
		if (mTel != null && mTel.length() > 8) {
			MyDialogUtil qDialog = new MyDialogUtil(this) {

				@Override
				public void onClickPositive() {
					Intent intent = new Intent(Intent.ACTION_DIAL,
							Uri.parse("tel:" + mTel.replace(" ", "")));
					try {
						startActivity(intent);
					} catch (Exception e) {
						Toast.makeText(getBaseContext(), "调用设备拨打电话失败",
								Toast.LENGTH_LONG).show();
					}
				}

				@Override
				public void onClickNagative() {
				}
			};
			qDialog.showCustomMessage("提示!", "即将拨打：" + mTel + "\n是否继续？", "确定",
					"取消");
		} else {
			Toast.makeText(getBaseContext(), "号码为空，请先验证号码", Toast.LENGTH_LONG)
					.show();
		}
	}

	private String formateTelString(String tel) {
		tel = tel.replace(" ", "");
		String temp = "";
		if (tel.length() == 11) {
			temp = tel.substring(0, 3) + " ";
			temp += tel.substring(3, 7) + " ";
			temp += tel.substring(7, 11);
		}
		return temp;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public boolean ismMiUsed() {
		return mMiUsed;
	}


	//重新获取卡券信息
	private void verifyTelFromNet() {
		com.milai.asynctask.PostGetTask<JSONObject> mGetVerifyTask = new PostGetTask<JSONObject>(this
				, R.string.submiting, R.string.submit_failed, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jsonParams = new JSONObject();
				jsonParams.put("shopId", Cookies.getShopId());
				jsonParams.put("mobile", mTel);
				jsonParams.put("uid", Cookies.getUserId());
				jsonParams.put("token", Cookies.getToken());
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getVerifyTelUrl(), jsonParams, true);
				Log.i("test", "jRet" + jRet.toString());
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				if (exception == null && result != null) {
					try {
						if (result.getInt("result") == 1) {

							JSONArray data = null;
							try {
								data = result.getJSONArray("list");
								List<Coupons> coupons = new ArrayList<Coupons>();
								for (int i = 0; i < data.length(); i++) {
									JSONObject coupon = data.getJSONObject(i);
									try {
										Coupons couponMode = new Coupons(coupon);
										if (couponMode.getMinPrice().equals("0")
												&& (Double.parseDouble(couponMode.getCouponValue()) > 0.0)) {
											coupons.add(couponMode);
										}
									} catch (Exception e) {
										Log.i("ActVerifyVipTel", "数据解析异常");
										e.printStackTrace();
									}
								}
								couponArray.clear();
								couponArray.addAll(coupons);
								adapter.notifyDataSetChanged();
							} catch (JSONException e) {
								Toast.makeText(ActVipCenter.this,"解析红包数据错误",Toast.LENGTH_SHORT).show();
							}
						}
					} catch (JSONException e) {
						Toast.makeText(ActVipCenter.this,"网络连接失败，请检查网络配置",Toast.LENGTH_SHORT).show();
					}
				}
			}
		};
		mGetVerifyTask.execute();
	}

	@Override
	public void onUseCoupon() {
		if (adapter != null) {
			verifyTelFromNet();
		}
	}

	@Override
	public void onUseMi() {

	}
}
