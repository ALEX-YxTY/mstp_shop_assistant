package com.meishipintu.assistant.ui.pay;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.epos.bertlv.MisComm;
import com.epos.bertlv.MisConstants;
import com.epos.bertlv.MisDataResult;
import com.epos.bertlv.MisTLVValue;
import com.epos.bertlv.MisTLVValueConvert;
import com.epos.bertlv.MisDataCenter;
import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.core.utils.CashierSign;
import com.meishipintu.core.utils.WposServiceUtils;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;
import com.milai.http.ServerUrlConstants;
import com.milai.utils.Des2;
import com.milai.utils.TimeUtil;
import com.newland.MPosSignatureVerify;
import com.newland.jsums.paylib.NLPay;
import com.newland.jsums.paylib.model.CancelRequest;
import com.newland.jsums.paylib.model.NLResult;
import com.newland.jsums.paylib.model.OrderInfo;
import com.newland.jsums.paylib.model.ResultData;

import cn.weipass.pos.sdk.BizServiceInvoker;
import cn.weipass.service.bizInvoke.RequestInvoke;
import cn.weipass.service.bizInvoke.RequestResult;

public class ActPaymentDetail extends Activity {

	private TextView mTvNick = null;
	private TextView mTvTel = null;
	private TextView mTvTime = null;
	private TextView mTvSubject = null;
	private TextView mTvTotalPrice = null;
	private TextView mTvTradeNo = null;
	private TextView mTvOutTradeNo = null;
	private TextView mTvRefundButton = null;
	private TextView mTvPayType = null;
	private TextView mTvCouponSn = null;

	private ProgressDialog m_progressDialog;

	private String mNick = null;
	private String mTel = null;
	private long mTime = 0;
	private String mPrice = null;
	private String mSubject = null;
	private int mPayType = 0;
	private String mTradeNo = null;
	private String mOutTradeNo = null;
	private long mPaymentId = 0;
	private int mStatus = 0;
	private String mCouponSn = null;

	//wpos服务Invoker
	private BizServiceInvoker mBizServiceInvoker = null;
	private String mCashierTradeNo = null;

	final int LANDI_POS_REQUEST_CODE = 75;//
	final int LAKALA_POS_REQUEST_CODE = 76;//

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBizServiceInvoker = WposServiceUtils.getServiceInvokerInstance();
		setContentView(R.layout.layout_payment_detail);
		Intent in = getIntent();
		mPaymentId = in.getLongExtra("paymentId", 0);
		mNick = in.getStringExtra("nick");
		mTel = in.getStringExtra("tel");
		mTime = in.getLongExtra("time", 0);
		mSubject = in.getStringExtra("subject");
		mTradeNo = in.getStringExtra("tradeNo");

		mOutTradeNo = in.getStringExtra("outTradeNo");
		mPrice = in.getStringExtra("price");
		mPayType = in.getIntExtra("payType", 0);
		mStatus = in.getIntExtra("status", 0);
		mCouponSn = in.getStringExtra("couponSn");

		mTvNick = (TextView) findViewById(R.id.tv_nick);
		mTvTel = (TextView) findViewById(R.id.tv_tel);
		mTvTime = (TextView) findViewById(R.id.tv_time);
		mTvSubject = (TextView) findViewById(R.id.tv_subject);
		mTvTotalPrice = (TextView) findViewById(R.id.tv_total_fee);
		mTvTradeNo = (TextView) findViewById(R.id.tv_tradeno);
		mTvOutTradeNo = (TextView) findViewById(R.id.tv_outtradeno);
		mTvPayType = (TextView) findViewById(R.id.tv_paytype);
		mTvCouponSn = (TextView) findViewById(R.id.tv_coupon_sn);
		mTvRefundButton = (TextView) findViewById(R.id.tv_refund);
		mTvRefundButton.setOnClickListener(mClkListener);

		Button bt_back = (Button) findViewById(R.id.btn_back);
		bt_back.setOnClickListener(mClkListener);

		doRefreshDetail(mNick, mTel, mTime, mSubject, mPrice, mTradeNo, mOutTradeNo, mPayType, mCouponSn);
	}

	private void doRefreshDetail(String nick, String tel, long time, String subject, String price, String tradeNo,
			String outTradeNo, int payType, String couponSn) {
		if (nick.equals("null") || nick.isEmpty()) {
			nick = "";
		}
		mTvNick.setText(nick);
		if (tel.equals("null") || tel.isEmpty()) {
			tel = "";
		}
		mTvTel.setText(tel);
		if (time != 0) {
			String timestr = TimeUtil.convertLongToFormatString(time, "yyyy-MM-dd HH:mm:ss");
			mTvTime.setText("支付时间：" + timestr);
		}
		if (!subject.isEmpty()) {
			mTvSubject.setText(subject);
		}
		mTvTotalPrice.setText("￥" + price);
		String shopName = Cookies.getShopName();
		if (!tradeNo.isEmpty()) {
			tradeNo = tradeNo.replace("T", "");
		}
		if (payType == 1 || payType == 2 || payType == 3 || payType == 15) {
			mTvRefundButton.setVisibility(View.GONE);
		} else {
			if (mStatus == 1) {
				mTvRefundButton.setVisibility(View.VISIBLE);
			} else {
				mTvRefundButton.setVisibility(View.GONE);
			}

		}
		if (tradeNo.equals("null")) {
			tradeNo = "";
		}
		if (payType == 1) {
			mTvPayType.setText("用户支付(支付宝)");
			mTvPayType.setTextColor(getResources().getColor(R.color.alipay_color));
			tradeNo += "(支付宝)";
		} else if (payType == 2) {
			mTvPayType.setText("用户支付(微信)");
			mTvPayType.setTextColor(getResources().getColor(R.color.weixin_color));
			tradeNo += "(微信)";
		} else if (payType == 3) {
			mTvPayType.setText("用户支付(银联)");
			mTvPayType.setTextColor(getResources().getColor(R.color.upay_color));
			tradeNo += "(银联)";
		} else if (payType == 4) {
			mTvNick.setText(shopName);
			mTvPayType.setText("商户收款(现金)");
			mTvPayType.setTextColor(getResources().getColor(R.color.cash_color));
			// tradeNo+="(现金)";
		} else if (payType == 5) {
			mTvNick.setText(shopName);
			mTvPayType.setText("商户收款(刷卡)");
			mTvPayType.setTextColor(getResources().getColor(R.color.pos_color));
			tradeNo += "(刷卡)";
		} else if (payType == 11) {
			mTvNick.setText(shopName);
			mTvPayType.setText("商户收款(银联刷卡)");
			mTvPayType.setTextColor(getResources().getColor(R.color.pos_color));
			tradeNo += "(银联刷卡)";
		} else if (payType == 6 || payType == 7 || payType == 8) {
			mTvNick.setText(shopName);
			mTvPayType.setText("商户收款(支付宝当面付)");
			mTvPayType.setTextColor(getResources().getColor(R.color.faceToface_color));
			tradeNo += "(支付宝)";
		} else if (payType == 9) {
			mTvNick.setText(shopName);
			mTvPayType.setText("商户收款(微信扫码)");
			mTvPayType.setTextColor(getResources().getColor(R.color.weixin_color));
			tradeNo += "(微信)";
		} else if (payType == 12) {
			mTvNick.setText(shopName);
			mTvPayType.setText("商户收款(微信刷卡)");
			mTvPayType.setTextColor(getResources().getColor(R.color.weixin_color));
			tradeNo += "(微信)";
		} else if (payType == 13) {
			mTvNick.setText(shopName);
			mTvPayType.setText("用户支付(百度钱包)");
			mTvPayType.setTextColor(getResources().getColor(R.color.red));
			tradeNo += "(百度钱包)";
		} else if (payType == 14) {
			mTvNick.setText(shopName);
			mTvPayType.setText("商户收款(其他支付)");
			mTvPayType.setTextColor(getResources().getColor(R.color.yellow));
			tradeNo += "(其他支付)";
		}

		mTvTradeNo.setText(tradeNo);
		if (outTradeNo.equals("null") || outTradeNo.isEmpty()) {
			outTradeNo = "";
		}
		mTvOutTradeNo.setText("订单号：" + outTradeNo);
		if (couponSn != null && couponSn.length() > 0) {
			StringBuilder sb = new StringBuilder(couponSn);
			sb.insert(4, " ");
			sb.insert(9, " ");
			String tempString = sb.toString();
			findViewById(R.id.ll_coupon_sn).setVisibility(View.VISIBLE);
			mTvCouponSn.setText("券号：" + tempString);
		} else {
			findViewById(R.id.ll_coupon_sn).setVisibility(View.GONE);
			mTvCouponSn.setText("");
		}
	}

	private OnClickListener mClkListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.tv_refund: {
				if (mPayType == 5) {
					showDialogWith2EditText(1);
					InputMethodManager m = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				} else {
					showDialogWith2EditText(2);
				}
			}
			}

		}
	};

	private void showDialogWith2EditText(final int type) {
		final Dialog dia = new Dialog(this, R.style.dialog);
		LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.my_dialog_with_2editor, null);
		dia.setContentView(v);

		TextView title = (TextView) v.findViewById(R.id.tv_dialogtitle);
		title.setText("退款提示");

		TextView titleIcon = (TextView) v.findViewById(R.id.tv_title_icon);
		final EditText et_sign = (EditText) v.findViewById(R.id.pos_sign);
		if (type == 1)// 1.普通刷卡退款，2.其他退款
		{
			et_sign.setVisibility(View.VISIBLE);
		} else {
			et_sign.setVisibility(View.GONE);
			titleIcon.setBackgroundResource(R.drawable.img_app);
		}
		final EditText et_password = (EditText) v.findViewById(R.id.pos_password);
		Button btYes = (Button) v.findViewById(R.id.ok);
		btYes.setText("确认");
		btYes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String pwd = et_password.getText().toString();
				String voutureId = et_sign.getText().toString();
				String password = Des2.decodeValue("meishipintu", Cookies.getPassword());
				if (type == 1) {
					if (voutureId.isEmpty()) {
						Toast.makeText(getBaseContext(), "凭证不能为空", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				if (pwd == null && pwd.length() == 0) {
					Toast.makeText(getBaseContext(), "密碼不能为空！", Toast.LENGTH_LONG).show();
					return;
				}
				if (pwd.equals(password)) {
					if (type == 1) {
						//刷卡退款
						cardRefund(voutureId); // 弹窗后再确定按钮中调用
					} else {
						if (mPayType == 11) {
							//unionPay退款
							cardUnionPayMPosRefundSign(mOutTradeNo, mPayType);
						} else {
							//一般退款
							refund(mOutTradeNo, mPayType);
						}
					}
					dia.dismiss();
				} else {
					Toast.makeText(getBaseContext(), "密码输入错误,请联系工作人员", Toast.LENGTH_LONG).show();
				}
			}
		});
		Button btCancle = (Button) v.findViewById(R.id.cancel);
		btCancle.setText("取消");
		btCancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dia.dismiss();
			}
		});
		dia.show();

	}

	private void cardUnionPayMPosRefundSign(final String outTradeNo, final int type) {
		new PostGetTask<JSONObject>(this, R.string.loading, R.string.fail_refund, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jParam = new JSONObject();
				jParam.put("uid", Cookies.getUserId());
				jParam.put("shopId", Cookies.getShopId());
				jParam.put("outTradeNo", outTradeNo);
				jParam.put("type", type);
				jParam.put("token", Cookies.getToken());
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(ServerUrlConstants.getMPosPayRefundSign(), jParam, true);// hcs
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				if (result != null && exception == null) {
					try {
						int resultCode = result.getInt("result");
						if (resultCode == 1) {
							String extOrderNo = result.getString("extOrderNo");
							String orderNo = result.getString("orderNo");
							String merchNo = result.getString("mrchNo");
							String signatureString = result.getString("signData");
							String uniPublicKey = result.getString("uniPublicKey");
							String appAccessKey = result.getString("appAccessKeyId");

							mTvRefundButton.setEnabled(false);

							Bundle data = new Bundle();
							data.putString("extOrderNo", extOrderNo);
							data.putString("orderNo", orderNo);
							data.putString("merchNo", merchNo);
							data.putString("appAccessKey", appAccessKey);
							data.putString("signatureString", signatureString);
							data.putString("uniPublicKey", changePublicKey(uniPublicKey));
							callUniPayMPosRefund(data);
						} else {
							Toast.makeText(getBaseContext(), "退款失败,请咨询店铺管理人员", Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e1) {
						Toast.makeText(getBaseContext(), "退款失败，数据解析错误", Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(getBaseContext(), "退款失败，请检查网络", Toast.LENGTH_LONG).show();
				}
			}
		}.execute();
	}

	private void callUniPayMPosRefund(Bundle data) {
		NLPay pay = NLPay.getInstance(); // 取一个支付实例
		String ext01 = "";
		String ext02 = "";
		String ext03 = "";
		// 取消消費,退款
		String exOrderNo = data.getString("extOrderNo");
		String orderNo = data.getString("orderNo");
		String merchantNo = data.getString("merchNo");
		String appAccessKey = data.getString("appAccessKey");
		String publicUnionKey = data.getString("uniPublicKey");
		ActPaymentDetail.unionPublicKey = publicUnionKey;

		CancelRequest request = new CancelRequest();
		request.setAppAccessKeyId(appAccessKey); // 设置appSecretKey应用接入秘钥
		request.setExtOrderNo(exOrderNo); // 设置外部订单号
		// request.setOrderNo(orderNo);
		request.setMrchNo(merchantNo); // 设置商户号
		request.setExt01(ext01);
		request.setExt02(ext02);
		request.setExt03(ext03);
		String signatureResult = data.getString("signatureString");
		request.setSignature(signatureResult);// 传入私钥生成签名数据,这个要放在最后
		try {
			pay.cancel(this, request);
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "您还没有安装  银联左收右付  请在“应用汇和360手机市场搜索下载”", Toast.LENGTH_LONG).show();
		}
		Log.d("debug", request.toString());
	}

	private static String unionPublicKey = null;

	private String changePublicKey(String oringlePublicKey) {
		String finalPublicKey = "";
		oringlePublicKey = oringlePublicKey.replace("-----BEGIN PUBLIC KEY-----", "");
		oringlePublicKey = oringlePublicKey.replace("-----END PUBLIC KEY-----", "");
		finalPublicKey = oringlePublicKey.replaceAll("\n", "");
		Log.d("ActNewPayment", finalPublicKey);
		return finalPublicKey;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case LANDI_POS_REQUEST_CODE: {
				Toast.makeText(getBaseContext(), "银行处理退款成功", Toast.LENGTH_LONG).show();
				// mOutTradeNo=data.getStringExtra("traceNo");
				refund(mOutTradeNo, 5);

				break;
			}
			case LAKALA_POS_REQUEST_CODE: {

				Toast.makeText(getBaseContext(), "银行处理退款成功", Toast.LENGTH_LONG).show();
				// mOutTradeNo=data.getStringExtra("traceNo");
				refund(mOutTradeNo, 5);

				break;
			}
			}
		} else if (resultCode == com.newland.jsums.paylib.utils.Key.RESULT_SUCCESS) {
			if (data != null) {
				NLResult payResult = (NLResult) data.getParcelableExtra("result");
				showResult(payResult);
			}
		} else {
			switch (requestCode) {
			case LANDI_POS_REQUEST_CODE: {
				Toast.makeText(getBaseContext(), "银行处理退款失败，请重试", Toast.LENGTH_LONG).show();
				break;
			}
			}
		}

	}

	private OrderInfo lastOrderInfo = null;

	public void showResult(NLResult rslt) {
		String resultMsg = "";
		StringBuilder sb = new StringBuilder();
		sb.append("返回码：" + rslt.getResultCode() + " \n");
		if (rslt.getResultCode() == 6000 && rslt.getData() != null) {
			// 有订单信息返回的时候需对期进行验签，防止数据被篡改
			ResultData data = rslt.getData();
			if (MPosSignatureVerify.verifySignDatatest(ActPaymentDetail.unionPublicKey, data, rslt.getSignData())) {
				// 验签成功
				sb.append("订单信息：" + data.toString() + "\n");
				// resultMsg = sb.toString();
				// 存入上次调用成功的信息
				if (rslt.getData() instanceof OrderInfo) {
					lastOrderInfo = (OrderInfo) rslt.getData();
					switch (Integer.parseInt(lastOrderInfo.getOrderStatus())) {
					case 3: {
						String outTradeNo = lastOrderInfo.getExtOrderNo();
						refund(outTradeNo, 11);
						resultMsg = "退款申请成功，款项将在审核通过后返还到您的支付账户";
						break;
					}
					}
				}
			} else {
				// 验签失败
				resultMsg = "验签失败";
			}
		} else {
			// 调用失败
			sb.append("错误信息：" + rslt.getResultMsg() + "\n");
			resultMsg = sb.toString();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(resultMsg).setTitle("调用结果").setPositiveButton("确定", null).create().show();
	}

	private void refund(final String outTradeNo, final int type) {
		new PostGetTask<JSONObject>(this, R.string.loading, R.string.fail_refund, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jParam = new JSONObject();
				jParam.put("uid", Cookies.getUserId());
				jParam.put("shopId", Cookies.getShopId());
				jParam.put("outTradeNo", outTradeNo);
				jParam.put("type", type);
				jParam.put("token", Cookies.getToken());
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(ServerUrlConstants.getPayRefund(), jParam, true);// hcs
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				if (result != null && exception == null) {
					try {
						int resultCode = result.getInt("result");
						if (resultCode == 1) {
							Toast.makeText(getBaseContext(), "退款成功", Toast.LENGTH_LONG).show();
							mTvRefundButton.setVisibility(View.GONE);
						} else {
							Toast.makeText(getBaseContext(), "退款失败，" + result.getString("msg"), Toast.LENGTH_LONG)
									.show();
						}
					} catch (JSONException e1) {
						Toast.makeText(getBaseContext(), "订单状态变更，请检查网络", Toast.LENGTH_LONG).show();
					}
				}
			}
		}.execute();
	}

	//刷卡退款
	private void cardRefund(final String val) {

		if (Cookies.getShopType().contains("lkl")) {

			Intent intent = new Intent();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMDDhhmmss", Locale.CHINA);
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String time = formatter.format(curDate);

			try {
				ComponentName componet = new ComponentName("com.lkl.cloudpos.payment",
						"com.lkl.cloudpos.payment.activity.MainMenuActivity");
				Bundle b = new Bundle();
				b.putString("msg_tp", "0200");// 报文类型
				b.putString("pay_tp", "0");// 支付方式0-银行卡 1-微信 2-支付宝 3-银联钱包
				b.putString("proc_tp", "00");// 消费类型 00消费 01授权
				b.putString("proc_cd", "200000");// 交易处理码 000000
				b.putString("amt", mPrice);// 金额
				if (!mTradeNo.isEmpty()) {
					mTradeNo = mTradeNo.replace("T", "");
				}
				b.putString("order_no", mTradeNo);// 订单号
				b.putString("appid", "com.meishipintu.assistant");// 包名
				b.putString("time_stamp", time);// 时间
				b.putString("order_info", "消费撤销");// 订单信息
				intent.setComponent(componet);
				intent.putExtras(b);
				startActivityForResult(intent, LAKALA_POS_REQUEST_CODE);

			} catch (Exception e) {
				Toast.makeText(getBaseContext(), "非拉卡拉设备支持店铺，请联系客服", Toast.LENGTH_LONG).show();
			}
		}else if (Cookies.getShopType().contains("wpos")) {
			if (mBizServiceInvoker == null) {
				Toast.makeText(this, "初始化服务调用失败", Toast.LENGTH_SHORT).show();
				return;
			}

			if (!mTradeNo.isEmpty()) {
				String[] strings = mTradeNo.split(",");
				mTradeNo = strings[0];
				mTradeNo = mTradeNo.replace("T", "");

				if (strings.length > 1) {
					mCashierTradeNo = strings[1];
				} else {
					Toast.makeText(this, "该笔消费不是在本机产生", Toast.LENGTH_SHORT).show();
					return;
				}

				Log.i("test", "outTradeNo :" + mTradeNo);
				Log.i("test", "CashierTradeNO :" + mCashierTradeNo);

			}

			refundRequestCashier();

		} else if (Cookies.getShopType().contains("lan")) {

			try {

				Intent in = new Intent();
				ComponentName cp = new ComponentName("com.landicorp.android.ccbpay",
						"com.landicorp.android.ccbpay.MainActivity");
				in.setComponent(cp);
				in.putExtra("transName", "消费撤销");
				in.putExtra("oldTrace", val);
				startActivityForResult(in, LANDI_POS_REQUEST_CODE);
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), "非联迪pos支持店铺，请联系客服", Toast.LENGTH_LONG).show();
			}
		} else {

			m_progressDialog = new ProgressDialog(this);
			m_progressDialog.setMessage("正在退款,请稍候...");
			m_progressDialog.setCancelable(false);
			m_progressDialog.setCanceledOnTouchOutside(false);
			m_progressDialog.show();
			new Thread() {
				public void run() {
					MisDataResult dataResult = null;
					dataResult = MisDataCenter.dataSwitch(ActPaymentDetail.this,
							MisComm.getConsumeRedoArray(val).getEncoder());
					boolean bRet = dataResult.bSuccess;
					Bundle bundle = new Bundle();
					Message msg = new Message();
					if (bRet) {
						msg.what = 0;
					} else
						msg.what = 1;
					bundle.putSerializable("DATARESULT", dataResult);
					msg.setData(bundle);
					m_handler.sendMessage(msg);
					// m_handler.sendEmptyMessage(0);
				}
			}.start();
		}
	}

	//wpos调起退款服务
	private void refundRequestCashier() {
		// 发起请求，outradeNo不能相同，相同在收银会提示有存在订单
		try {
			RequestInvoke cashierReq = new RequestInvoke();
			cashierReq.pkgName = this.getPackageName();
			cashierReq.sdCode = "CASH002";// 收银服务的sdcode信息
			cashierReq.bpId = CashierSign.InvokeCashier_BPID;
			cashierReq.launchType = 0;
			cashierReq.params = CashierSign.sign(
					CashierSign.InvokeCashier_BPID,
					CashierSign.InvokeCashier_KEY, mTradeNo, mCashierTradeNo, this);
			cashierReq.seqNo = "1";

			mBizServiceInvoker.setOnResponseListener(new BizServiceInvoker.OnResponseListener() {
				@Override
				public void onResponse(String s, String s1, byte[] bytes) {
					Log.i("test", "sdCode = " + s
							+ " , token = " + s1 + " , data = " + new String(bytes));
					try {
						JSONObject jsonObject = new JSONObject(new String(bytes));
						if (jsonObject.getString("errCode").equals("0")) {
//							Toast.makeText(getBaseContext(), "银行处理退款成功", Toast.LENGTH_LONG).show();
							// mOutTradeNo=data.getStringExtra("traceNo");
							refund(mOutTradeNo, 5);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFinishSubscribeService(boolean b, String s) {

				}
			});

			// 发送调用请求
			RequestResult r = mBizServiceInvoker.request(cashierReq);
			Log.i("test", r.token + "," + r.seqNo + ","
					+ r.result);

			if (r != null) {
				Log.i("test", "request result:" + r.result
						+ "|launchType:" + cashierReq.launchType);
				String err = null;
				switch (r.result) {
					case BizServiceInvoker.REQ_SUCCESS: {
						// 调用成功
						Toast.makeText(this, "退款服务调用成功", Toast.LENGTH_SHORT).show();
						break;
					}
					case BizServiceInvoker.REQ_ERR_INVAILD_PARAM: {
						Toast.makeText(this, "请求参数错误！", Toast.LENGTH_SHORT).show();
						break;
					}
					case BizServiceInvoker.REQ_ERR_NO_BP: {
						Toast.makeText(this, "未知的合作伙伴！", Toast.LENGTH_SHORT).show();
						break;
					}
					case BizServiceInvoker.REQ_ERR_NO_SERVICE: {
						Toast.makeText(this, "未找到合适的服务！", Toast.LENGTH_SHORT)
								.show();
						break;
					}
					case BizServiceInvoker.REQ_NONE: {
						Toast.makeText(this, "请求未知错误！", Toast.LENGTH_SHORT).show();
						break;
					}
				}
				if (err != null) {
					Log.w("requestCashier", "serviceInvoker request err:" + err);
				}
			} else {
				Log.i("test", "result r == null");
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@SuppressLint({ "HandlerLeak", "HandlerLeak" })
	private Handler m_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			m_progressDialog.dismiss();
			MisDataResult dataResult = (MisDataResult) msg.getData().getSerializable("DATARESULT");
			switch (msg.what) {
			case 0: {
				MisTLVValue misResponseTLVValue = null;
				if (dataResult.tlvList != null) {
					for (int i = 0; i < dataResult.tlvList.size(); i++) {
						Log.e(MisDataCenter.getByteInfo(dataResult.tlvList.get(i).getmTag()),
								MisDataCenter.getByteInfo(dataResult.tlvList.get(i).getmData()));

						if (Arrays.equals(dataResult.tlvList.get(i).getmTag(),
								MisConstants.TagConstants.REPONSE_CODE)) {
							misResponseTLVValue = MisTLVValueConvert.getMisTLVValueName(dataResult.tlvList.get(i));
							String resp = misResponseTLVValue.getValue();
							if (!resp.equals("00")) {
								dataResult.bSuccess = false;
								break;
							}
						}
					}
				}
				if (dataResult.bSuccess) {
					Log.d("m_handler", "dataResult.bSuccess=" + (dataResult.bSuccess ? "true" : "false"));
					refund(mOutTradeNo, mPayType);
				} else {
					showDialog(-1, "提示", dataResult.strMsg);
				}
				break;
			}
			case 1:
				showDialog(-1, "提示", dataResult.strMsg);
				break;
			}
		}
	};

	void showDialog(int nConfirmDialogID, String strTitle, String strMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(strTitle);
		builder.setMessage(strMessage);
		switch (nConfirmDialogID) {
		case -1:
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					return;
				}
			});
			break;
		}
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
}
