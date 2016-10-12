package com.meishipintu.assistant.adapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.interfaces.OnUseCouponOrMi;
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
	private Activity context;
	private LayoutInflater inflater = null;
	private int mIsFrom = 0;  //0是vip验，1是支付时验
	private boolean isMiUse = false;	//是否使用米，仅在支付进入时发挥作用
	private ProgressDialog mProgressDialog;
	private OnUseCouponOrMi refreshListener;

	private String dateLimitCons;
	private String detailLimitCons;

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

	public AdapterCoupons(Activity ctx, ArrayList<Coupons> arraylist,
						  int isFrom) {
		inflater = LayoutInflater.from(ctx);
		this.context = ctx;
		this.dataArrayList = arraylist;
		//关联接口
		this.refreshListener = (OnUseCouponOrMi) ctx;
		Log.d("coupon list array", Integer.toString(arraylist.size()));
		this.mIsFrom = isFrom;
//		获取actVipCenter的mMiUsed值
//		ActVipCenter actVipCenter = (ActVipCenter) context;
//		this.isMiUse = actVipCenter.ismMiUsed();
		this.dialogShowCouponDetail = new Dialog(context, R.style.dialog);
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
			//两种验证模式均显示右小角小标签
//			if (mIsFrom == 0) {
//				viewHolder.btUseCoupon.setVisibility(View.GONE);
//			} else {
//				viewHolder.btUseCoupon.setVisibility(View.VISIBLE);
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
		String detail = coupon.getCouponDetail();
		if (detail == null || detail.length() == 0) {
			detail = "暂无规则介绍";
		}
		final String finalDetail = detail;
		viewHolder.tvEndTime.setText(dateLimit);// bug
		viewHolder.tvMinPrice.setText("满" + coupon.getMinPrice() + "元使用");
		viewHolder.btUseCoupon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//支付时验证
				if (mIsFrom == 1) {
					//发送券号
					if (coupon.getCouponSn().length() == 0) {
						sendSns(coupon,dateLimit, finalDetail,ActVipCenter.mVipCenter.getTel());
					} else {
						//返回支付页面并验证
						ActVipCenter.mVipCenter.payToSn(Long.toString(coupon.getId()), coupon.getCouponSn(), coupon.getCouponName(), coupon.getCouponValue(), coupon.getMinPrice());
					}
				} else {
					//会员时验证——直接验证
					if(coupon.getCouponSn().length() != 0){
						showDialogUse(coupon);
					} else {
						//会员时验证-发送券号并返回验证界面
						sendSns(coupon,dateLimit, finalDetail, ActVipCenter.mVipCenter.getTel());
					}
				}

			}
		});
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialogCouponDetail(coupon.getCouponValue(), dateLimit,
						finalDetail, coupon);

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
				//在线验证并使用卡券
				useCouponNet(coupon.getCouponSn(),coupon.getId(),0);
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
		dateLimitCons = couponDateLimit;
		detailLimitCons = detail;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.dialog_coupon_detail, null);

		RelativeLayout rlEtCouponSn = (RelativeLayout) v
				.findViewById(R.id.rl_et_verify);
		btSendSn = (Button) v.findViewById(R.id.bt_send_coupon_sn);
		etCouponSn = (EditText) v.findViewById(R.id.et_coupon_use);
		Button btUseCoupon = (Button) v.findViewById(R.id.bt_use_coupon);
		btSendSn.setOnClickListener(mOnClickListener);
		btUseCoupon.setOnClickListener(mOnClickListener);
		LinearLayout llVerify = (LinearLayout) v.findViewById(R.id.ll_verify);
		if (mIsFrom == 0) {				//从会员验证进入不显示验证信息
			llVerify.setVisibility(View.GONE);
		} else if (coupon.getCouponSn().length() != 0) {	//从支付页面进入，但直接使用的卡券只显示使用按钮
			rlEtCouponSn.setVisibility(View.GONE);
			btUseCoupon.setText("直接使用");
		}
		etCouponSn.addTextChangedListener(mTextWatcher);

//		if (mIsFrom == 0) {
//			rlEtCouponSn.setVisibility(View.VISIBLE);
//			btUseCoupon.setVisibility(View.VISIBLE);
//			btUseCoupon.setText("验证并使用");
//			btUseCoupon.setOnClickListener(mOnClickListener);
//			btSendSn.setText("发送券号");
//			btSendSn.setOnClickListener(mOnClickListener);
//		} else {
//			rlEtCouponSn.setVisibility(View.GONE);
//			btUseCoupon.setVisibility(View.GONE);
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
		if (mCount > 0) {
			mHandler.post(timeWaitRunnable);
		}
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
	private void useCouponNet(final String couponSn, final long couponId, final int flag) {
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
				jsonParams.put("type", 1);		//只使用红包
				if (flag == 1) {
					jsonParams.put("flag", 1);		//flag = 1标记仅为验证红包，并不使用红包 flag = 0 为使用卡券
				}
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getUseCouponResult(), jsonParams,
						true);
				Log.i("test", "jRet3:" + jRet);
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
							if (mIsFrom == 0 || mIsFrom == 2) {                        //会员页面进入
								MyDialogUtil qDialog = new MyDialogUtil(context) {
									@Override
									public void onClickPositive() {
										//调用接口让mainActivity刷新数据
										refreshListener.onUseCoupon();
									}

									@Override
									public void onClickNagative() {
									}
								};
								qDialog.showCustomMessageOK(context.getString(R.string.notice)
										, "红包验证使用成功", "知道了");
							} else {                            //支付界面进入
								//返回支付页面并验证
								ActVipCenter.mVipCenter.payToSn(Long.toString(couponId), couponSn
										, mCoupon.getCouponName(), mCoupon.getCouponValue(), mCoupon.getMinPrice());
							}
							clearCouponInfo();

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
	private void sendSns(final Coupons coupon, final String dateLimit, final String detail, final String tel) {
		mProgressDialog=new ProgressDialog(context);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setMessage("正在发送短信");
		mProgressDialog.show();
		new PostGetTask<JSONObject>(context) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jPara = new JSONObject();
				jPara.put("mobile", tel);
				jPara.put("couponId", coupon.getId());
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

							if (mIsFrom == 1) {			//支付时启动
//								ActVipCenter.mVipCenter.payToPayment(tel, 1);// 1表示弹出验证红包的提示页面
								mCount = 60;
								if (!dialogShowCouponDetail.isShowing()) {
									showDialogCouponDetail(coupon.getCouponValue(), dateLimit, detail, coupon);
								} else {
									mHandler.post(timeWaitRunnable);
								}

							} else if (mIsFrom == 0) {		//会员界面启动
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
				case R.id.bt_use_coupon:
					//需要发送sn号后验证
					if (mCoupon.getCouponSn().length() == 0) {
						if (etCouponSn != null) {
							String sn = etCouponSn.getText().toString();
							if (sn.length() == 0) {
								selfToastShow("红包SN不能为空");
							} else {
								sn = sn.replace(" ", "");
								//在线仅验证不适用卡券
								useCouponNet(sn, mCoupon.getId(),1);
							}
						}
					} else {		//直接验证
//						useCouponNet(mCoupon.getCouponSn(), mCoupon.getId());
						//同点击卡券右下角效果相同
						//返回支付页面并验证
						ActVipCenter.mVipCenter.payToSn(Long.toString(mCoupon.getId()), mCoupon.getCouponSn()
								, mCoupon.getCouponName(), mCoupon.getCouponValue(), mCoupon.getMinPrice());
					}
					break;

				case R.id.bt_send_coupon_sn:
					if (mCount > 0) {
						selfToastShow("验证码已发送，请稍后再试");
						return;
					}
					sendSns(mCoupon, dateLimitCons, detailLimitCons, ActVipCenter.mVipCenter.getTel());
					mCount = 60;
					mHandler.post(timeWaitRunnable);
					break;
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

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Log.i("test", "msg.what:" + msg.what);
			if (msg.what > 0) {
				btSendSn.setText("等待"+msg.what + " S");
				btSendSn.setTextColor(Color.RED);
				btSendSn.setBackgroundResource(R.drawable.bg_round_corner_gray);
			} else {
				btSendSn.setText("发送券号");
				btSendSn.setBackgroundResource(R.drawable.bg_round_corner);
				btSendSn.setTextColor(Color.WHITE);
			}
		}
	};
	private int mCount = 0;

	private Runnable timeWaitRunnable = new Runnable() {
		@Override
		public void run() {
			Thread t = new Thread(){
				@Override
				public void run() {
					super.run();
					while (mCount > 0) {
						mCount--;
						mHandler.sendEmptyMessage(mCount);
						Log.i("test", "mCount:" + mCount);
						try {
							sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			t.start();
		}
	};

}
