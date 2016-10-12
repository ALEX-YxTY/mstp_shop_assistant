package com.meishipintu.assistant.orderdish;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.zxing.client.android.CaptureActivity;
import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.milai.dao.ShopTableDao;
import com.milai.dao.SubmittedTicketDao;
import com.milai.model.SubmittedTicket;
import com.milai.processor.OrderdishProcessor;
import com.meishipintu.assistant.ui.pay.ActNewPayment;
import com.meishipintu.assistant.ui.pay.PopupCoupon;
import com.milai.asynctask.PostGetTask;
import com.milai.asynctask.RefreshTask;
import com.meishipintu.core.utils.ConstUtil;
import com.milai.utils.CustomProgressDialog;
import com.meishipintu.core.utils.MyDialogUtil;
import com.milai.utils.StringUtil;
import com.umeng.analytics.MobclickAgent;

public class ActCaptureTicket extends CaptureActivity {

	private static final int LOAD_USER_PWD = 100;
	private int mTakeaway = 0;
	private int mStatus = 0;
	private long mTicketId = -1;
	private String mSign = null;
	private String mUserName = "";
	private String mUserTel = "";

	private PostGetTask<Integer> mVerifyTicketTask = null;
	private PostGetTask<JSONObject> mGetTicketDetailTask = null;
	protected CustomProgressDialog mLoadingDialog = null;

	private long mCreateTime = 0;
	private int mTableId = 0;
	private String mTableName = "";
	private String mUserNote = "";
	private String mWaiterNote = "";
	private int mPeopleNum = 0;
	private long mPayTime = 0;
	private int mPayType = 0;
	private String mTotalFee = "";
	private String mTradeNo = "";

	private int mCheckCode = 0;
	private int mCheckCodeFrom=-1;

	private EditText etTextPayCode = null;
	private SubmittedTicket st = null;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Intent in = getIntent();
		mCheckCode = in.getIntExtra("CHECK_CODE", 0);// 0。验证订单获取订单；1.扫描用户支付宝钱包；2。扫描暂存订单到米来支付页面；3.收银扫描红包码，4验证扫红包码
		mCheckCodeFrom=in.getIntExtra("CHECK_CODE_FROM", -1);
		TextView tv = (TextView) findViewById(R.id.status_view);
		tv.setText("请扫用户手机上的订单二维码");
		doRefresh();
//		etTextPayCode = (EditText) findViewById(R.id.et_scancode_gun);
//		if (mCheckCode == 1 || mCheckCode == 2) {
//			etTextPayCode.setVisibility(View.VISIBLE);
//			etTextPayCode.addTextChangedListener(txtWatcher);
//			closeBoard();
//		}
	}

	private void closeBoard()
	{
		 InputMethodManager inputManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		 getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);//有效
	}
	private TextWatcher txtWatcher = new TextWatcher() {

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
			String sign = etTextPayCode.getText().toString();
			if (sign.isEmpty()) {
				Toast.makeText(getBaseContext(), "凭证不能为空", Toast.LENGTH_SHORT)
						.show();
			} else {
				if (sign.contains("\n")) {
					sign = sign.replace("\n", "");
					HandleDecoded(sign);
				}
				// 将数据写到actNewPayment
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	// @Override
	// protected void resetStatusView() {
	// super.resetStatusView();
	// TextView tv = (TextView) findViewById(R.id.status_view);
	// tv.setText("请扫收银台付款二维码");
	// }

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	public boolean HandleDecoded(String str) {
		if (!StringUtil.isNullOrEmpty(str)) {
			if (mCheckCode == 1) {
				Intent data = new Intent();
				data.putExtra("dynamicid", str);
				data.putExtra("CHECK_CODE_FROM", mCheckCodeFrom);
				Log.d("ActCaptureTicket", "扫描完毕，mCheckCodeFrom="+Integer.toString(mCheckCodeFrom));
				this.setResult(RESULT_OK, data);
				finish();
				return true;
			}
			if(mCheckCode==3)
			{
				PopupCoupon.mPopCoupon.setCouponSnToEditText(str);
				finish();
				return true;
			}
			if(mCheckCode==2)
			{
				String temp=str.replace(" ", "");
				if(temp.length()==12&&isNumeric(temp))
				{
					Intent in=new Intent();
					in.putExtra("COUPON_SN", temp);
					this.setResult(RESULT_OK, in);
					finish();
					return true;
				}
			}
			if(mCheckCode==4)		//从会员扫码进入
			{
				if (str.startsWith("47")) {
					//从c端扫码进入
					Intent data=new Intent();
					data.putExtra("pwd", str);
					ActCaptureTicket.this.setResult(LOAD_USER_PWD, data);
				}else if(str!=null&&(str.length()==14||str.length()==12))
				{
					String temp=str.replace(" ", "");
					if(temp.length()==12)
					{
						Log.i("test", "CSN" + str);
						Intent data=new Intent();
						data.putExtra("CSN", str);
						ActCaptureTicket.this.setResult(RESULT_OK, data);
					}else{
						selfToastShow("非米来红包二维码，请确认后重试");
					}	
				}else{
					selfToastShow("无效的红包二维码");
				}
				finish();
				return true;
			}
			String magic_pre = getString(R.string.pay_prefix);
			if (str.startsWith(magic_pre)) {
				String[] shopinfo = str.split(":");
				if (shopinfo.length == 4) {
					try {
						String ticketId = shopinfo[2].replace("sign", "");
						mTicketId = Long.parseLong(ticketId);
						mSign = shopinfo[3];

						getTicketDetail(mTicketId);

						return true;
					} catch (NumberFormatException ne) {
						Toast.makeText(this, "无效二维码", Toast.LENGTH_LONG)
								.show();
						restartPreviewAfterDelay(0L);
						return false;
					}
				}
			}
		}
		Toast.makeText(this, "无效二维码", Toast.LENGTH_LONG).show();
		restartPreviewAfterDelay(0L);
		return false;
	}

	public boolean isNumeric(String str){ 
		   Pattern pattern = Pattern.compile("[0-9]*"); 
		   Matcher isNum = pattern.matcher(str);
		   if( !isNum.matches() ){
		       return false; 
		   } 
		   return true; 
		}

	private void getTicketDetail(final long ticketId) {
		mLoadingDialog = new CustomProgressDialog(this, "正在查询订单");
		mLoadingDialog.show();
		mGetTicketDetailTask = new PostGetTask<JSONObject>(
				ActCaptureTicket.this) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				// TODO Auto-generated method stub
				return OrderdishProcessor.getInstance().getTicketDetail(
						ActCaptureTicket.this, ticketId, Cookies.getUserId(), Cookies.getToken());
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				// TODO Auto-generated method stub
				if (result != null && exception == null) {
					try {
						if (result.getInt("result") == 1) {
							if (result.getInt("shopId") == Cookies.getShopId()) {
								setTicketDetailFromNetWork(result);// 拿到订单数据
								if (mCheckCode == 0) {
									verifyQrcode();
								}
								if (mCheckCode == 2) {
									if (result.getInt("payTime") == 0) {
										int totalPrice = result
												.getInt("priceTotal");
										if (mLoadingDialog.isShowing()) {
											mLoadingDialog.dismiss();
										}
										finish();
										ActNewPayment.mActNewPayment
												.setTicketDetail(mTicketId,
														totalPrice);
									} else {
										failedGetTicket();
										Toast.makeText(getBaseContext(),
												"该订单已经支付", Toast.LENGTH_LONG)
												.show();
									}
								}

							} else {
								Toast.makeText(ActCaptureTicket.this,
										"无该订单数据，非本店订单", Toast.LENGTH_LONG)
										.show();
							}
						} else {
							failedGetTicket();
							Toast.makeText(getBaseContext(), "获取订单失败",
									Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e) {
						failedGetTicket();
						e.printStackTrace();
					}
				}
			}
		};
		mGetTicketDetailTask.execute();
		if (mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
	}

	private void setTicketDetailFromNetWork(JSONObject result) {
		try {
			mUserName = result.getString("memberName");
			mUserTel = result.getString("memberTel");
			mPayTime = result.getLong("payTime");
			mTakeaway = result.getInt("type");
			mStatus = result.getInt("status");
			mTableName = result.getString("tablenoName");
			if (mTableName.equals("null")) {
				mTableName = "";
				mTableId = 0;
			} else {
				mTableId = ShopTableDao.getInstance().queryTableIdByName(this,
						mTableName);
			}

			mPeopleNum = result.getInt("people");
			mCreateTime = result.getLong("createTime");

			if (result.has("totalFee")) {
				mTotalFee = result.getString("totalFee");
				if (mTotalFee.equals("null")) {
					mTotalFee = "";
				}
			} else {
				mTotalFee = "";
			}

			mUserNote = result.getString("comment");
			if (mUserNote.equals("null")) {
				mUserNote = "";
			}
			mWaiterNote = result.getString("waiterComment");
			if (mWaiterNote.equals("null")) {
				mWaiterNote = "";
			}
			if (result.has("payType") && result.has("tradeNo")) {
				try {
					mPayType = result.getInt("payType");
					mTradeNo = result.getString("tradeNo");
					mTradeNo = mTradeNo.replace("T", "");
					switch (mPayType) {
					case 1:
						mTradeNo += "(支付宝)";
						break;
					case 2:
						mTradeNo += "(微信)";
						break;
					}
				} catch (JSONException e2) {
					mPayType = 0;
					mTradeNo = null;
				}
			}
		} catch (JSONException e) {
			Toast.makeText(getBaseContext(), "解析数据失败", Toast.LENGTH_LONG)
					.show();
			Log.d("ActCaptureHd", "解析数据失败");
		}

	}

	private void failedGetTicket() {
		if (mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
		finish();
		ActNewPayment.mActNewPayment.setTicketDetail(-1, 0);
	}

	private void setDataToTicketDetail(String userName, String userTel,
			int takeaway, int status, long ticketId, long createTime,
			String tableName, int tableId, String userNote, String waiterNote,
			long payTime, int peopleNum, String totalFee, String tradeNo) {
		Intent in = new Intent();
		in.setClass(this, ActDishTicket.class);

		in.putExtra(ConstUtil.DISH_TICKET_ID, ticketId);//
		in.putExtra(ConstUtil.USER_NAME, userName);//
		in.putExtra(ConstUtil.USER_TEL, userTel);//
		in.putExtra(ConstUtil.CREATE_TIME, createTime);//
		in.putExtra(ConstUtil.PAY_TIME, payTime);//
		
		in.putExtra(ConstUtil.TOTAL_FEE, mTotalFee);//
		in.putExtra(ConstUtil.TRADE_NUM, mTradeNo);//
		in.putExtra(ConstUtil.STATUS, status);//
		in.putExtra("takeaway", mTakeaway);//

		in.putExtra(ConstUtil.PEOPLE_NUM, mPeopleNum);//
		in.putExtra(ConstUtil.TABLE_ID, tableId);//
		in.putExtra(ConstUtil.TABLE_NAME, tableName);//
		in.putExtra(ConstUtil.USER_NOTE, userNote);//
		in.putExtra(ConstUtil.WAITER_NOTE, waiterNote);//

		// 外卖订单不需要二维码验证
		// else {
		// in.putExtra(ConstUtil.INTENT_EXTRA_NAME.ADDRESS, st.getTableName());
		// in.putExtra(ConstUtil.INTENT_EXTRA_NAME.ARRIVAL_TIME,
		// st.getArrivalTime());
		// }
		//
		startActivity(in);
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	private void verifyQrcode() {
		mLoadingDialog = new CustomProgressDialog(this, "正在验证订单");
		mLoadingDialog.show();

		mVerifyTicketTask = new PostGetTask<Integer>(ActCaptureTicket.this) {

			@Override
			protected Integer doBackgroudJob() throws Exception {
				// TODO Auto-generated method stub
				return OrderdishProcessor.getInstance().verifyQrcode(mTicketId,
						mSign, Cookies.getUserId(), Cookies.getToken());
			}

			@Override
			protected void doPostJob(Exception exception, Integer result) {
				// TODO Auto-generated method stub
				if (result == 1) {
					if (mStatus != 0) {
						st = SubmittedTicketDao.getInstance().query(
								ActCaptureTicket.this, mTicketId);
						if (st == null) {
							if (mLoadingDialog.isShowing()) {
								mLoadingDialog.dismiss();
							}
							ShowCheckMessage("无该订单状态或该订单无效,请联系餐厅管理人员查询");
						}
					}
					String orderStatus = "";
					switch (mStatus) {
					case 0:
						orderStatus = "订单暂存中";
						break;
					case 1:
						orderStatus = "订单已提交";
						break;
					case 2:
						orderStatus = "商家已接受";
						break;
					case 3:
						orderStatus = "用餐中";
						break;
					case 4:
						orderStatus = "用餐成功";
						break;
					default:
						orderStatus = "无该订单状态或该订单无效,请联系餐厅管理人员查询";
					}
					if (mLoadingDialog.isShowing()) {
						mLoadingDialog.dismiss();
					}
					ShowOrderMessage("用户：" + mUserName + "\n" + "手机号码："
							+ mUserTel + "\n" + "订单状态：" + orderStatus);
					Log.d("sign", mSign);
				} else {
					if (mLoadingDialog.isShowing()) {
						mLoadingDialog.dismiss();
					}
					ShowCheckMessage("二维码验证失败" + "订单号：" + mTicketId);
				}
			}
		};
		mVerifyTicketTask.execute();
	}

	public void ShowCheckMessage(String message) {
		MyDialogUtil qDialog = new MyDialogUtil(this) {

			@Override
			public void onClickPositive() {
				restartPreviewAfterDelay(0L);
			}

			@Override
			public void onClickNagative() {
				finish();
			}
		};
		qDialog.showCustomMessage(getString(R.string.notice), message, "重新扫描",
				"返回");
	}

	private void DialogUpdateSaveTempStatusToStatus2(final long ticketId) {
		new PostGetTask<Long>(this, R.string.submiting, R.string.submit_failed,
				true, true, false) {

			@Override
			protected Long doBackgroudJob() throws Exception {
				return OrderdishProcessor.getInstance().updateDishTicketStatus(
						ActCaptureTicket.this, ticketId, 3, Cookies.getUserId(), Cookies.getToken());
			}

			@Override
			protected void doPostJob(Exception e, Long result) {
				if ((e == null) && (result != null)) {
					if (result == 0) {
						Toast.makeText(ActCaptureTicket.this,
								"暂存定单验证成功，请前往“店内订单”-->“未结账”列表中查看订单状态",
								Toast.LENGTH_LONG).show();
						finish();
					} else {
						Toast.makeText(ActCaptureTicket.this, "暂存定单验证失败，请检查网络",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(ActCaptureTicket.this, "暂存定单验证失败，请检查网络",
							Toast.LENGTH_LONG).show();
				}
				if (mLoadingDialog.isShowing()) {
					mLoadingDialog.dismiss();
				}
			}
		}.execute();
	}

	public void ShowOrderMessage(String message) {
		MyDialogUtil qDialog = new MyDialogUtil(this) {

			@Override
			public void onClickPositive() {
				finish();
				setDataToTicketDetail(mUserName, mUserTel, mTakeaway, mStatus,
						mTicketId, mCreateTime, mTableName, mTableId,
						mUserNote, mWaiterNote, mPayTime, mPeopleNum,
						mTotalFee, mTradeNo);
			}

			@Override
			public void onClickNagative() {
				finish();
			}
		};
		qDialog.showCustomMessage(getString(R.string.notice), message, "确认订单",
				"返回");
	}

	private void doRefresh() {

		RefreshTask refreshTask = new RefreshTask(this) {

			@Override
			public void doRequeryCursor() {

			}

			@Override
			public void doOnRefreshFinish() {
			}

			@Override
			public int doGetNetRefresh() throws Exception {
				return OrderdishProcessor.getInstance().getSubedTicketRefresh(
						ActCaptureTicket.this, 1, 0, Cookies.getUserId(), Cookies.getToken());
			}
		};
		refreshTask.execute();
	}
	
	private void selfToastShow(String message) {
		Toast toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		LayoutInflater infalter = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = infalter.inflate(R.layout.layout_toast, null);
		TextView tvContent = (TextView) v.findViewById(R.id.tv_toast_content);
		tvContent.setText(message);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setView(v);
		toast.show();
	}
}