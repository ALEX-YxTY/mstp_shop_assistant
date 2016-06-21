package com.meishipintu.assistant.mpos;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.epos.bertlv.MisComm;
import com.epos.bertlv.MisConstants;
import com.epos.bertlv.MisDataResult;
import com.epos.bertlv.MisTLV;
import com.epos.bertlv.MisTLVValue;
import com.epos.bertlv.MisTLVValueConvert;
import com.epos.bertlv.MisDataCenter;
import com.epos.utilstools.ConvertTools;
import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.core.utils.ConstUtil;
import com.umeng.analytics.MobclickAgent;

//public class ActPayTicketPay extends BaseActAlipay implements IWXAPIEventHandler {
public class ActMPosSetting extends Activity {
	private String mRcvShop = null;
	private long mShopId = 0;

	public static final int Trade_Login = 0; // 签到
	public static final int Trade_DownLoadPara = 1; // 参数下载
	public static final int Trade_DownLoadMainKey = 2; // 下载主密钥
	public static final int Trade_Account = 3; // 结算
	public static final int Trade_PrintTest = 4;
	public static final int Trade_PrintStatus = 5;
	public static final int Trade_ElectticSigna = 6;
	public static final int Trade_LeftMoney = 8; // 查询余额
	final int LANDI_POS_REQUEST_CODE = 75;//

	private ProgressDialog m_progressDialog;

	// private IWXAPI api;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mpos_setting);

		findViewById(R.id.btn_back).setOnClickListener(mClkListener);

		Button btbluetoothStart = (Button) findViewById(R.id.bt_mpos_bluetoothStart);
		btbluetoothStart.setOnClickListener(mClkListener);

		Button btsignIn = (Button) findViewById(R.id.bt_mpos_signIn);
		btsignIn.setOnClickListener(mClkListener);

		Button btLeftMoney = (Button) findViewById(R.id.bt_mpos_leftmoney);
		btLeftMoney.setOnClickListener(mClkListener);

		Button btAccount = (Button) findViewById(R.id.bt_mpos_account);
		btAccount.setOnClickListener(mClkListener);

		Button btLandiManagement = (Button) findViewById(R.id.bt_mpos_liandi_management);
		btLandiManagement.setOnClickListener(mClkListener);

		Button btLandiPrintOrder=(Button)findViewById(R.id.bt_mpos_landi_print);
		btLandiPrintOrder.setOnClickListener(mClkListener);
		
		Button btSettingPos=(Button)findViewById(R.id.bt_mpos_liandi_setting);
		btSettingPos.setOnClickListener(mClkListener);
		
		if(Cookies.getShopType().contains("lan"))
		{
			btSettingPos.setText("设置pos类型（当前Landi）");
			btbluetoothStart.setVisibility(View.GONE);		
		}else if(Cookies.getShopType().contains("bfu"))
		{
			btSettingPos.setText("设置pos类型（当前D200）");
			btLandiManagement.setVisibility(View.GONE);
			btLandiPrintOrder.setVisibility(View.GONE);
		}else{
			btSettingPos.setVisibility(View.GONE);
			btLandiManagement.setVisibility(View.GONE);
			btLandiPrintOrder.setVisibility(View.GONE);
		}
		// Button btdownloadKey = (Button)
		// findViewById(R.id.bt_mpos_downloadKey);
		// btdownloadKey.setOnClickListener(mClkListener);
		//
		// Button btdownloadPara = (Button)
		// findViewById(R.id.bt_mpos_downloadPara);
		// btdownloadPara.setOnClickListener(mClkListener);
		//
		// Button btpasswordMod = (Button)
		// findViewById(R.id.bt_mpos_passwordMod);
		// btpasswordMod.setOnClickListener(mClkListener);

		// Intent in = getIntent();
		// mTicketId = in.getLongExtra(ConstUtil.INTENT_EXTRA_NAME.TICKET_ID,
		// 0);
		// mPayMoney = in.getIntExtra(ConstUtil.INTENT_EXTRA_NAME.MONEY_AMOUNT,
		// 0);
		//
		// mRcvShop = Cookies.getShopName();
		// mShopId = Cookies.getShopId();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private OnClickListener mClkListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent in = new Intent();
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;

			case R.id.bt_mpos_bluetoothStart:
				bluetoothStart();
				break;

			case R.id.bt_mpos_signIn:
				if (Cookies.getShopType().contains("lan")) {
					startLandi("签到");
				} else if(Cookies.getShopType().contains("bfu")){
					processThread(Trade_Login, "正在签到,请稍候...");
				}
				break;
			case R.id.bt_mpos_leftmoney: {
				if (Cookies.getShopType().contains("lan")) {
					startLandi("余额查询");
				} else if(Cookies.getShopType().contains("bfu")){
					processThread(Trade_LeftMoney, "正在查询余额,请稍候...");
				}
				break;
			}
			case R.id.bt_mpos_account: {
				if (Cookies.getShopType().contains("lan")) {
					startLandi("结算");
				} else if(Cookies.getShopType().contains("bfu")){
					processThread(Trade_Account, "正在结算,请稍候...");
				}
				break;
			}
			case R.id.bt_mpos_liandi_management: {
				startLandi("系统管理");
				break;
			}
			case R.id.bt_mpos_landi_print:{
				startLandi("打印");
				break;
			}
			case R.id.bt_mpos_liandi_setting:
			{
				if(Cookies.getShopType().contains("lan"))
				{
					((Button)v).setText("设置pos类型（当前D200）");
//					Cookies.setShopType(0);
				}else
				{
					((Button)v).setText("设置pos类型（当前LANDI）");
//					Cookies.setShopType(3);
				}
				break;
			}
			//
			// case R.id.bt_mpos_downloadKey:
			// processThread(Trade_DownLoadMainKey,"正在下载主密钥,请稍候...");
			// break;

			// case R.id.bt_mpos_downloadPara:
			// processThread(Trade_DownLoadPara,"正在下载终端参数,请稍候...");
			// break;

			// case R.id.bt_mpos_passwordMod:
			// //
			// ((BaseContainerFragment)getParentFragment()).replaceFragment(new
			// ChangePwdFragment(), true);
			// break;
			}
		}
	};

	private void startLandi(String traceName) {
		Intent in = new Intent();
		try {
			ComponentName cp = new ComponentName(
					"com.landicorp.android.ccbpay",
					"com.landicorp.android.ccbpay.MainActivity");
			in.setComponent(cp);
			in.putExtra("transName", traceName);
			startActivityForResult(in, LANDI_POS_REQUEST_CODE);
		} catch (Exception e) {
			Toast.makeText(this, "非联迪设备支持店铺，请联系客服", Toast.LENGTH_LONG).show();
		}

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ConstUtil.REQUEST_CODE.PAY_TICKET_QR:
				setResult(RESULT_CANCELED);
				finishAndAni();
				break;
			}
		}
	}

	// @Override
	// public void onReq(BaseReq req) {
	// }

	// @Override
	// public void onResp(BaseResp resp) {
	// Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);
	//
	// if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
	// AlertDialog.Builder builder = new AlertDialog.Builder(this);
	// builder.setTitle(R.string.app_tip);
	// builder.setMessage(getString(R.string.pay_result_callback_msg,
	// String.valueOf(resp.errCode)));
	// builder.show();
	// OnAlipayOK();
	// }
	//
	// if (resp.errCode==0) {
	// OnAlipayOK();
	// //调用远程接口更新payment信息
	// //更新本地数据库
	// }
	//
	// }

	private void bluetoothStart() {
		Intent intent = new Intent();
		intent.setClass(ActMPosSetting.this, ActBluetoothConfig.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
				| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@SuppressLint({ "HandlerLeak", "HandlerLeak" })
	private Handler m_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			m_progressDialog.dismiss();
			MisDataResult dataResult = (MisDataResult) msg.getData()
					.getSerializable("DATARESULT");
			int nAction = msg.getData().getInt("ACTION");
			switch (msg.what) {
			case 0: {
				switch (nAction) {
				case 7:
					if (dataResult.tlvList != null) {
						int n = dataResult.tlvList.size();
						String strTLVValue = "";
						for (int i = 0; i < n; i++) {
							MisTLV misTLV = dataResult.tlvList.get(i);
							MisTLVValue misTLVValue = MisTLVValueConvert
									.getMisTLVValueName(misTLV);
							strTLVValue = strTLVValue
									+ misTLVValue.getTagInfo() + ":"
									+ misTLVValue.getValue() + "\r\n";
						}
						showDialog(-1, "提示", strTLVValue);
					} else
						showDialog(-1, "提示", "TLV EMPTY");
					break;
				case Trade_LeftMoney:
					if (dataResult.tlvList != null) {
						int n = dataResult.tlvList.size();
						String strTLVValue = "";
						for (int i = 0; i < n; i++) {

							if (Arrays.equals(dataResult.tlvList.get(i)
									.getmTag(),
									MisConstants.TagConstants.TRADE_LEFTMONEY)) {
								MisTLVValue misRefTLVValue = MisTLVValueConvert
										.getMisTLVValueName(dataResult.tlvList
												.get(i));
								String lefmoney = misRefTLVValue.getValue();
								lefmoney = ConvertTools.asciiToString(lefmoney);
								strTLVValue = "可用余额：" + lefmoney;
							} else {
								// MisTLV misTLV = dataResult.tlvList.get(i);
								// MisTLVValue misTLVValue = MisTLVValueConvert
								// .getMisTLVValueName(misTLV);
								// strTLVValue = strTLVValue
								// + misTLVValue.getTagInfo() + ":"
								// + misTLVValue.getValue() + "\r\n";
								strTLVValue = dataResult.strMsg;
							}
						}
						showDialog(-1, "提示", strTLVValue);
					} else
						showDialog(-1, "提示", "TLV EMPTY");
					break;
				case Trade_PrintStatus:
					if (dataResult.tlvList != null) {

						int n = dataResult.tlvList.size();
						String strTLVValue = "";
						for (int i = 0; i < n; i++) {
							MisTLV misTLV = dataResult.tlvList.get(i);
							if (Arrays.equals(misTLV.getmTag(), new byte[] {
									(byte) 0xFF, (byte) 0x84 })) {
								strTLVValue = "返回值:    "
										+ String.format("%02X",
												misTLV.getmData()[0]);
								switch (misTLV.getmData()[0]) {
								case 0:
									strTLVValue += "(打印机未连接)";
									break;
								case 1:
									strTLVValue += "(打印机正常)";
									break;
								case 5:
									strTLVValue += "(打印机缺纸)";
									break;
								}

								break;
							}

						}
						showDialog(-1, "提示", strTLVValue);
					}
					break;
				default:
					showDialog(-1, "提示", dataResult.strMsg);
					break;
				}
			}
				break;
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
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
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

	private void processThread(final int nAction, String strMsg) {

		m_progressDialog = new ProgressDialog(ActMPosSetting.this);
		m_progressDialog.setMessage(strMsg);
		m_progressDialog.setCancelable(false);
		m_progressDialog.setCanceledOnTouchOutside(false);
		m_progressDialog.show();
		new Thread() {
			public void run() {
				MisDataResult dataResult = null;
				switch (nAction) {
				case Trade_Login:
					dataResult = MisDataCenter.dataSwitch(ActMPosSetting.this,
							MisComm.getLoginArray().getEncoder());
					break;
				case Trade_DownLoadPara:
					dataResult = MisDataCenter.dataSwitch(ActMPosSetting.this,
							MisComm.getDownLoadParaArray().getEncoder());
					break;
				case Trade_DownLoadMainKey:
					dataResult = MisDataCenter.dataSwitch(ActMPosSetting.this,
							MisComm.getDownLoadMainKeyArray().getEncoder());
					break;
				case Trade_Account:
					dataResult = MisDataCenter.dataSwitch(ActMPosSetting.this,
							MisComm.getAccountArray().getEncoder());
					break;
				case Trade_ElectticSigna:
					dataResult = MisDataCenter.dataSwitch(ActMPosSetting.this,
							MisComm.getElectticSignatureUpLoadArray()
									.getEncoder());
					break;
				case Trade_PrintTest:
					String strPrintTest = "打印测试\r\n打印测试.\r\n打印测试..\r\n打印测试...\r\n打印测试....\r\n打印测试...........\r\n\r\n\r\n\r\n";
					byte[] bytePrintTest = null;
					try {
						bytePrintTest = strPrintTest.getBytes("GBK");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						dataResult = new MisDataResult();
						dataResult.bSuccess = false;
						dataResult.strMsg = "打印字符转码失败!";
					}
					if (dataResult == null)
						dataResult = MisDataCenter
								.dataSwitch(
										ActMPosSetting.this,
										MisComm.getPrintCateringInfoArray(
												bytePrintTest).getEncoder());
					break;
				case Trade_PrintStatus: {
					dataResult = MisDataCenter.dataSwitch(ActMPosSetting.this,
							MisComm.getPrintInfoArray().getEncoder());
					String strTLVValue = "";
					byte[] byteData = null;
					// add 140403 lxd
					if (dataResult.tlvList != null) {
						// byteData=new byte[]{0x01,0x02};
						int n = dataResult.tlvList.size();
						for (int i = 0; i < n; i++) {
							MisTLV misTLV = dataResult.tlvList.get(i);
							if (Arrays.equals(misTLV.getmTag(), new byte[] {
									(byte) 0xFF, (byte) 0x84 })) {
								strTLVValue = "返回值:    "
										+ String.format("%02X",
												misTLV.getmData()[0]);
								Log.e("打印机状态", strTLVValue);
								byteData = misTLV.getmData();
								break;
							}
						}
					}
					break;
				}

				case Trade_LeftMoney:
					dataResult = MisDataCenter.dataSwitch(ActMPosSetting.this,
							MisComm.getLeftMoneyArray().getEncoder());
				}
				boolean bRet = dataResult.bSuccess;
				Bundle bundle = new Bundle();
				Message msg = new Message();
				if (bRet) {
					msg.what = 0;
				} else
					msg.what = 1;
				bundle.putSerializable("DATARESULT", dataResult);
				bundle.putInt("ACTION", nAction);
				msg.setData(bundle);
				m_handler.sendMessage(msg);
				// m_handler.sendEmptyMessage(0);
			}
		}.start();
	}

}
