package com.meishipintu.assistant.adapter;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.milai.model.Coupons;
import com.meishipintu.assistant.ui.ActVipCenter;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.core.utils.MyDialogUtil;
import com.milai.http.ServerUrlConstants;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;
import com.milai.utils.TimeUtil;

public class AdapterCoupons extends BaseAdapter {

	private ArrayList<Coupons> dataArrayList = null;
	private FragmentActivity context;
	private LayoutInflater inflater = null;
	private int mIsFrom = 0;  //0是vip验，1是支付时验
	private ProgressDialog mProgressDialog;

	public static class ViewHolder {
		TextView tvCouponValue;
		TextView tvShopName;
		TextView tvEndTime;
		TextView tvMinPrice;
		TextView tvStartTime;
		TextView tvBordor;
		Button btUseCoupon;
		ImageView ivTopSep;
	}

	public AdapterCoupons(FragmentActivity ctx, ArrayList<Coupons> arraylist,
			int isFrom) {
		inflater = LayoutInflater.from(ctx);
		this.context = ctx;
		this.dataArrayList = arraylist;
		Log.d("coupon list array", Integer.toString(arraylist.size()));
		this.mIsFrom = isFrom;
	}

	@Override
	public int getCount() {
		return dataArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		Coupons coupon = dataArrayList.get(position);
		return coupon;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder;
		if (convertView == null || convertView.getTag() == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_coupon_layout, null);
			viewHolder.tvCouponValue = (TextView) convertView
					.findViewById(R.id.tv_coupon_value);
			viewHolder.tvShopName = (TextView) convertView
					.findViewById(R.id.tv_coupon_shopname);
			viewHolder.tvEndTime = (TextView) convertView
					.findViewById(R.id.tv_end_time);
			viewHolder.tvMinPrice = (TextView) convertView
					.findViewById(R.id.tv_min_price);
			viewHolder.btUseCoupon = (Button) convertView
					.findViewById(R.id.bt_use_coupon);
//			if (mIsFrom == 0) {
//				viewHolder.btUseCoupon.setVisibility(View.GONE);
//			} else {
			//两种验证模式均显示右小角小标签
			viewHolder.btUseCoupon.setVisibility(View.VISIBLE);
//			}
			viewHolder.ivTopSep = (ImageView) convertView
					.findViewById(R.id.iv_bg_top_sep);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final Coupons coupon = dataArrayList.get(position);
		
//		if(mIsFrom==1) {
		//两种模式下均显示
		if(coupon.getCouponSn().length()>0) {
			viewHolder.btUseCoupon.setText("直接使用");
		}
		else {
			viewHolder.btUseCoupon.setText("发送券号");
		}
//		}
		viewHolder.tvCouponValue.setText(coupon.getCouponValue());
		viewHolder.tvShopName.setText(coupon.getCouponName());
		String timeEndString = TimeUtil.convertLongToFormatString(
				coupon.getEndTime() * 1000, "yyyy.MM.dd");
		String timeStartString = TimeUtil.convertLongToFormatString(
				coupon.getStartTime() * 1000, "yyyy.MM.dd");
		final String dateLimit = timeStartString + "-" + timeEndString + "可用";
		viewHolder.tvEndTime.setText(dateLimit);// bug
		viewHolder.tvMinPrice.setText("满" + coupon.getMinPrice() + "元使用");
		viewHolder.btUseCoupon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//支付时验证
				if (mIsFrom == 1) {
					//发送券号
					if (coupon.getCouponSn().length() == 0) {
						sendSns(coupon.getId(), ActVipCenter.mVipCenter.getTel());
					} else {
						//返回支付页面并验证
						ActVipCenter.mVipCenter.payToSn(Long.toString(coupon.getId()), coupon.getCouponSn(), coupon.getCouponName(), coupon.getCouponValue(), coupon.getMinPrice());
					}
				} else {
					//
					//会员时验证——直接验证
					if(coupon.getCouponSn().length() != 0){
						showDialogUse(coupon);
					} else {
						//会员时验证-发送券号并返回验证界面
						sendSns(coupon.getId(), ActVipCenter.mVipCenter.getTel());
					}
				}

			}
		});
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String detail = coupon.getCouponDetail();
				if (detail == null || detail.length() == 0) {
					detail = "暂无规则介绍";
				}
				showDialogCouponDetail(coupon.getCouponValue(), dateLimit,
						detail, coupon);

			}
		});
		return convertView;
	}

	//显示使用卡券弹窗
	private void showDialogUse(final Coupons coupon) {
		// 显示是否验证对话框
		MyDialogUtil qDialog = new MyDialogUtil(context) {
			@Override
			public void onClickPositive() {
				//在线验证卡券
				useCouponNet(coupon.getCouponSn(),coupon.getId());
			}

			@Override
			public void onClickNagative() {
			}
		};
		qDialog.showCustomMessage(context.getString(R.string.notice), "是否需要验证红包？",
				"确定","取消");
	}

	//显示卡券详细信息
	private void showDialogCouponDetail(String value, String couponDateLimit,
										String detail, final Coupons coupon) {
		setCoupon(coupon);
		dialogShowCouponDetail = new Dialog(context, R.style.dialog);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.dialog_coupon_detail, null);

		RelativeLayout rlEtCouponSn = (RelativeLayout) v
				.findViewById(R.id.rl_et_verify);
		btSendSn = (Button) v.findViewById(R.id.bt_send_coupon_sn);
		etCouponSn = (EditText) v.findViewById(R.id.et_coupon_use);
		etCouponSn.addTextChangedListener(mTextWatcher);

		Button btUseCoupon = (Button) v.findViewById(R.id.bt_use_coupon);
//		if (mIsFrom == 0) {
//			rlEtCouponSn.setVisibility(View.VISIBLE);
//			btUseCoupon.setVisibility(View.VISIBLE);
//			btUseCoupon.setText("验证并使用");
//			btUseCoupon.setOnClickListener(mOnClickListener);
//			btSendSn.setText("发送券号");
//			btSendSn.setOnClickListener(mOnClickListener);
//		} else {
		/*
			不在此显示验证信息，只显示卡券信息
		 */
		rlEtCouponSn.setVisibility(View.GONE);
		btUseCoupon.setVisibility(View.GONE);
//		}
		TextView tvContent = (TextView) v.findViewById(R.id.tv_coupon_detail);
		tvContent.setText(detail);
		tvContent.setMaxHeight(context.getResources().getDimensionPixelSize(
				R.dimen.margin_200));
		tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());

		TextView tvDateLimit = (TextView) v.findViewById(R.id.tv_date_limit);
		tvDateLimit.setText(couponDateLimit);

		TextView tvValue = (TextView) v.findViewById(R.id.tv_coupon_value);
		tvValue.setText(value);
		dialogShowCouponDetail.setContentView(v);
		dialogShowCouponDetail.show();
		mCount=0;
	}
	//Toast工具
	private void selfToastShow(String message) {
		Toast toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
		LayoutInflater infalter = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = infalter.inflate(R.layout.layout_toast, null);
		TextView tvContent = (TextView) v.findViewById(R.id.tv_toast_content);
		tvContent.setText(message);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setView(v);
		toast.show();
	}

	//在线验证卡券
	private void useCouponNet(final String couponSn, final long couponId) {
		if (couponSn == null || couponSn.length() == 0) {
			this.selfToastShow("SN错误");
			return;
		}
		new PostGetTask<JSONObject>(context) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jsonParams = new JSONObject();
				jsonParams.put("shopId", Cookies.getShopId());
				jsonParams.put("totalFee", 9999);
				jsonParams.put("uid", Cookies.getUserId());
				jsonParams.put("token", Cookies.getToken());
				jsonParams.put("couponSn", couponSn);
				jsonParams.put("couponId", couponId);
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getUseCouponResult(), jsonParams,
						true);
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				if (exception == null && result != null) {
					try {
						if (result.getInt("result") == 1) {
							if (dialogShowCouponDetail != null
									&& dialogShowCouponDetail.isShowing()) {
								dialogShowCouponDetail.dismiss();
							}
							clearCouponInfo();
							//selfToastShow("红包验证使用成功");

							MyDialogUtil qDialog = new MyDialogUtil(context) {
								@Override
								public void onClickPositive() {
									ActVipCenter.mVipCenter.finish();
								}

								@Override
								public void onClickNagative() {
								}
							};
							qDialog.showCustomMessageOK(context.getString(R.string.notice),"红包验证使用成功", "知道了");

						} else {
							String msg = "红包使用失败";
							if (result.has("msg")) {
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

	//发送卡券号到用户手机
	private void sendSns(final long couponId, final String tel) {
		mProgressDialog=new ProgressDialog(context);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setMessage("正在发送短信");
		mProgressDialog.show();
		new PostGetTask<JSONObject>(context) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jPara = new JSONObject();
				jPara.put("mobile", tel);
				jPara.put("couponId", couponId);
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getUseCouponSnsUrl(), jPara, true);
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject jsonResult) {
				if(exception==null&&jsonResult!=null){
					try {
						if (jsonResult.getInt("result") == 1) {
							Intent in = new Intent();
							in.putExtra("USER_TEL", tel);
							// 此处需要添加发送号码到支付界面的功能

							if (mIsFrom == 1) {
								ActVipCenter.mVipCenter.payToPayment(tel, 1);// 1表示弹出验证红包的提示页面
							} else if (mIsFrom == 0) {
								// 启动按钮计时
								selfToastShow("红包验证码已发送至用户手机,请注意查收");
//								mHandler.postDelayed(mButtonCount, 1000);
								//延时一秒结束页面
								mHandler.postDelayed(new Runnable() {
									@Override
									public void run() {
										context.finish();
									}
								}, 1000);
							}

						} else {
							mCount=0;
							selfToastShow("红包验证码发送失败，请点击重新发送");
						}
					} catch (JSONException e) {
						e.printStackTrace();
						mCount=0;
						selfToastShow("网络请求失败，请检查网络连接");
					}
				}
				if(mProgressDialog!=null&&mProgressDialog.isShowing())
				{
					mProgressDialog.dismiss();
				}
			}
		}.execute();
	}

	private Button btSendSn = null;
	private Dialog dialogShowCouponDetail = null;
	private Coupons mCoupon = null;
	private EditText etCouponSn = null;

	private void setCoupon(Coupons coupon) {
		this.mCoupon = coupon;
	}

	private Coupons getCoupon() {
		return this.mCoupon;
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.bt_use_coupon: {
				if (etCouponSn != null) {
					String sn = etCouponSn.getText().toString();
					if (sn.length() == 0) {
						selfToastShow("红包SN不能为空");
					} else {
						sn = sn.replace(" ", "");
						useCouponNet(sn, mCoupon.getId());
					}
				}
				break;
			}
			case R.id.bt_send_coupon_sn: {
				if(mCount>0)
				{
					selfToastShow("验证码已发送，请稍后再试");
					return;
				}
				mCount = 90;
				sendSns(mCoupon.getId(), ActVipCenter.mVipCenter.getTel());
				break;
			}
			}

		}
	};

	private void clearCouponInfo() {
		mCoupon = null;
		mCount=0;
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
//			Log.d("dialog_coupon_detail_after", Integer.toString(s.length()));
			if (s.length() > 4) {
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
	};

	private Handler mHandler = new Handler();
	private int mCount = 0;
	private Runnable mButtonCount = new Runnable() {

		@Override
		public void run() {
			if (mCount > 0) {
				mHandler.postDelayed(this, 1000);
				mCount--;
				if (btSendSn != null) {
					btSendSn.setText(mCount + "s");
				}
			} else {
				if (btSendSn != null) {
					btSendSn.setText("重发");
					btSendSn.setClickable(true);
				}
			}

		}
	};

}
