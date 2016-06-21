package com.meishipintu.assistant.ui;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;
import com.milai.http.ServerUrlConstants;
import com.milai.model.Coupons;
import com.meishipintu.assistant.adapter.AdapterCoupons;
import com.meishipintu.assistant.ui.auth.ActPopupMemberDetail;
import com.meishipintu.core.utils.MyDialogUtil;

import org.json.JSONObject;

public class ActVipCenter extends FragmentActivity {

	private TextView mTvTitle = null;
	private String mTel = "";
	private int isFromCode = 0;//0直接打开，1。从收银
	public static ActVipCenter mVipCenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_vip_center);
		Intent inFrom = getIntent();
		isFromCode = inFrom.getIntExtra("From", 0);
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mTvTitle.setText("会员详情");
		Button btBack = (Button) findViewById(R.id.btn_back);
		btBack.setOnClickListener(mClickListener);
		
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
			int sex = data.getIntExtra("sex", 0);
			ArrayList<Coupons> couponArray = null;
			AdapterCoupons adapter = null;
			couponArray = (ArrayList<Coupons>) data
					.getSerializableExtra("dataArray");
			if (couponArray != null && couponArray.size() > 0) {
				adapter = new AdapterCoupons(ActVipCenter.this,
						couponArray,isFromCode);
			}
			refreshView(mTel, name, sex, items, amount, average,
					adapter);
		}else{
			if (mTel == null || mTel.length() != 11) {
				finish();
			}
		}
	}


	private void refreshView(final String tel, String name, int sex, int items,
							 String amount, String average, AdapterCoupons adapter) {
		mTel = tel;
		TextView tvTel = (TextView) findViewById(R.id.tv_vip_tel);
		TextView tvAmount = (TextView) findViewById(R.id.tv_amount);
		TextView tvItems = (TextView) findViewById(R.id.tv_items);
		TextView tvAverage = (TextView) findViewById(R.id.tv_average);
		TextView tvName = (TextView) findViewById(R.id.tv_user_name);
		TextView tvIconSex=(TextView)findViewById(R.id.icon_user_sex);
		TextView tvSex = (TextView) findViewById(R.id.tv_user_sex);

		TextView tvNoCoupon = (TextView) findViewById(R.id.tv_no_coupon);

		findViewById(R.id.ll_tel).setOnClickListener(mClickListener);

		TextView tvPay = (TextView) findViewById(R.id.tv_pay);
		tvPay.setOnClickListener(mClickListener);
		if(isFromCode==0)
		{
			tvPay.setVisibility(View.GONE);
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
		ListView dataListView = (ListView) findViewById(R.id.listview_coupon);
		if (adapter != null){
			final AdapterCoupons adapterFinal = adapter;
			dataListView.setAdapter(adapter);
			dataListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
										int position, long id) {
					// Coupons coupon = (Coupons) adapterFinal
					// .getItem(position);
					// String detail=coupon.getCouponDetail();
					// if(detail==null||detail.length()==0)
					// {
					// detail="暂无红包描述";
					// }
					// Toast toast=Toast.makeText(getBaseContext(), detail,
					// 10000);
					// toast.show();
					// sendSns(coupon.getId(), tel);
				}
			});
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
				payToPayment(mTel,0);
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
	public void payToPayment(String tel,int show) {
		Intent in = new Intent();
		in.putExtra("USER_TEL", tel);
		Log.d("ActVipCenter_topayment", tel);
		in.putExtra("SHOW_MESSAGE_DIALOG", show);
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

	public String getTel() {
		return this.mTel;
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
}
