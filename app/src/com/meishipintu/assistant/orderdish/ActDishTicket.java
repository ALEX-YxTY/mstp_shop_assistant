package com.meishipintu.assistant.orderdish;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.milai.dao.DishesDao;
import com.milai.dao.SubmittedTicketDao;
import com.milai.dao.TicketDetailDao;
import com.milai.http.ServerUrlConstants;
import com.milai.model.Dishes;
import com.milai.model.MsptProvider.DatabaseHelper;
import com.milai.model.TicketDetail;
import com.milai.processor.OrderdishProcessor;
import com.meishipintu.assistant.ui.MainActivity;
import com.meishipintu.assistant.ui.pay.ActNewPayment;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;
import com.meishipintu.core.utils.ConstUtil;
import com.milai.utils.CustomProgressDialog;
import com.meishipintu.core.utils.MyDialogUtil;
import com.milai.utils.StringUtil;
import com.milai.utils.TimeUtil;
import com.umeng.analytics.MobclickAgent;

public class ActDishTicket extends FragmentActivity {

	private ListView mListDish = null;
	private AdapterDishTicketDetail mAdapterDishTicketDetail = null;
	private AdapterDishTicket mAdapterDishTicket = null;
	// private AdapterTakeawayDetail mAdapterTakeaway = null;
	protected CustomProgressDialog mLoadingDialog = null;

	private View mHeaderView = null;
	private View mFooterView = null;
	private TextView mTvTableNum = null;
	private TextView mTvPeopleNum = null;

	private int mShopCategory=1;
	private int mTakeaway = 0;
	private int mStatus = 0;
	private long mTableId = -1;
	private String mTableName = "";
	private long mTicketId = -1;
	private String mUserTel = "";
	private String mUserName = "";
	private String mUserNote = "";
	private String mWaiterNote = "";// 商户备注
	private String mInvoice = "";
	private long mCreateTime = 0;
	private long mPayTime = 0;
	private String mArrivalTime = "";
	private String mArrivalAddr = "";
	private PostGetTask<JSONObject> mSubStatusTicketTask = null;
	private PostGetTask<JSONObject> mGetTicketTask = null;
	private String mDishList = null;
	private int mPrice = 0;
	private String mTotalFee = "";

	private TextView mButtonCheckAndSaveTemp = null;
	private TextView mButtonSubmite = null;
	private TextView mCancleAndReject = null;

	private int mPeopleNum = 0;

	private String waitorNote = "";// 用于存储商家备注的字符串
	private View mDish;
	public EditText mEtWaitorNote = null;
	Context context = ActDishTicket.this;
	public static ActDishTicket mActDishTicket = null;
	// 复选对话框
	private TextView mTvWaiterNoteSelector = null;
	//
	private int mPayType = 0;
	private String mTradeNo = null;
	private int mWaitorType = 3;

	private int mModify = 0;

	// /////////////////////////////////////////////////////////////////////
	@Override
	protected void onCreate(Bundle savedsInstanceState) {
		super.onCreate(savedsInstanceState);
		mActDishTicket = this;
		mWaitorType = Cookies.getWaitorType();
		mShopCategory=Cookies.getShopCategory();
		Intent intent = getIntent();
		if (intent.hasExtra("modify")) {
			mModify = intent.getIntExtra("modify", 0);
		}
		mTakeaway = intent.getIntExtra("takeaway", 0);
		mTicketId = intent.getLongExtra(ConstUtil.DISH_TICKET_ID, -1);
		mUserTel = intent.getStringExtra(ConstUtil.USER_TEL);
		if (mUserTel == null) {
			mUserTel = "";
		}
		mUserName = intent.getStringExtra(ConstUtil.USER_NAME);
		if (mUserName == null) {
			mUserName = "";
		}
		mUserNote = intent.getStringExtra(ConstUtil.USER_NOTE);
		if (mUserNote == null)
			mUserNote = "";
		mCreateTime = intent.getLongExtra(ConstUtil.CREATE_TIME, 0);
		mPayTime = intent.getLongExtra(ConstUtil.PAY_TIME, 0);
		mStatus = intent.getIntExtra(ConstUtil.STATUS, 0);
		if (mPayTime > 0) {
			mTotalFee = intent.getStringExtra(ConstUtil.TOTAL_FEE);
			mTradeNo = intent.getStringExtra(ConstUtil.TRADE_NUM);
		}

		if (mTakeaway == 1) { // 堂食订单
			mPeopleNum = intent.getIntExtra(ConstUtil.PEOPLE_NUM, 0);
			mTableId = intent.getLongExtra(ConstUtil.TABLE_ID, 0);
			mTableName = intent.getStringExtra(ConstUtil.TABLE_NAME);
			if (mTableName == null)
				mTableName = "";
			mWaiterNote = intent.getStringExtra(ConstUtil.WAITER_NOTE);
			if (mWaiterNote == null)
				mWaiterNote = "";

		} else {
			long arrival = intent.getLongExtra(
					ConstUtil.INTENT_EXTRA_NAME.ARRIVAL_TIME, 0);
			String timestr = TimeUtil.convertLongToFormatString(arrival * 1000,
					"HH:mm");
			String timestr2 = TimeUtil.convertLongToFormatString(arrival * 1000
					+ 15 * 60 * 1000, "HH:mm");
			mArrivalTime = "送达时间：" + timestr + "~" + timestr2;
			mArrivalAddr = intent
					.getStringExtra(ConstUtil.INTENT_EXTRA_NAME.ADDRESS);
		}

		setContentView(R.layout.dish_ticket_detail);

		mListDish = (ListView) findViewById(R.id.lv_ordered_dishes);// hcs
		mButtonCheckAndSaveTemp = (TextView) findViewById(R.id.tv_checkout);
		mButtonSubmite = (TextView) findViewById(R.id.tv_submit);
		mCancleAndReject = (TextView) findViewById(R.id.tv_cancel);

		mButtonCheckAndSaveTemp.setOnClickListener(mClkListener);
		mButtonSubmite.setOnClickListener(mClkListener);
		mCancleAndReject.setOnClickListener(mClkListener);
		findViewById(R.id.btn_back).setOnClickListener(mClkListener);

		TextView tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText(R.string.ticket_detail);
		Button bt_title_right = (Button) this.findViewById(R.id.bt_title_right);
		if (mTicketId != -1) {
			bt_title_right.setVisibility(View.VISIBLE);
			bt_title_right.setOnClickListener(mClkListener);
		}

		checkButtonStatus(mStatus, mPayTime);
		getTicketDetail();
	}

	private void getTicketDetail() {
		mLoadingDialog = new CustomProgressDialog(this,
				"正在加载中");
		mLoadingDialog.show();
		if (mTicketId == -1) {
			String sqlOrderedDish = Dishes.COLUMN_NAME_DISH_QUANTITY + ">0";
			Cursor dishCs = managedQuery(Dishes.CONTENT_URI, null,
					sqlOrderedDish, null, Dishes.COLUMN_NAME_DISH_ORDER);

			mAdapterDishTicket = new AdapterDishTicket(this, dishCs, DishesDao
					.getInstance().totalPrice(this), DishesDao.getInstance()
					.totalQuantity(this));
			refreshView(mUserName, mUserTel, mCreateTime, mArrivalAddr,
					mArrivalTime, mTableName, mPeopleNum, mUserNote,
					mWaiterNote, mTradeNo, mInvoice, mTotalFee,mAdapterDishTicket);
		} else {
			doRefresh();
		}

	}

	private void checkButtonStatus(int status, long payTime) {
		// mButtonCheckAndSaveTemp
		// mButtonSubmite
		// mCancleAndReject
		mButtonCheckAndSaveTemp.setVisibility(View.VISIBLE);// 暂存，确认订单，接受
		mButtonSubmite.setVisibility(View.VISIBLE);// 提交，结账
		mCancleAndReject.setVisibility(View.VISIBLE);// 拒绝，退单
		if (mTicketId == -1) {
			mCancleAndReject.setVisibility(View.GONE);
		}
		if (status == 0) {
			mButtonCheckAndSaveTemp.setText("确认订单");// 用户端在线支付需要确认订单，确认订单后直接到已完成
			mButtonSubmite.setVisibility(View.GONE); // 提交按钮不显示
			mCancleAndReject.setText("退单");
		} else if (status == 1) { // 未处理状态
			mButtonSubmite.setVisibility(View.GONE); // 提交按钮不显示
			mButtonCheckAndSaveTemp.setText(R.string.accept); // “暂存”按钮改为“接受”
			mCancleAndReject.setText(R.string.reject); // 按钮叫“拒绝”
		} else if (status == 2) { // 已接受
			if (payTime != 0) {
				mButtonCheckAndSaveTemp.setText("确认订单");// 用户端在线支付需要确认订单，确认订单后直接到已完成
				mButtonSubmite.setVisibility(View.GONE); // 提交按钮不显示
				mCancleAndReject.setText("退单");
			} else {
				if (mUserName != null && mUserName.length() > 0) {
					mButtonCheckAndSaveTemp.setText("确认订单");// 用户端在线支付需要确认订单，确认订单后直接到已完成
					mButtonSubmite.setVisibility(View.GONE); // 提交按钮不显示
					mCancleAndReject.setText("退单");
				} else {
					mButtonCheckAndSaveTemp.setText(R.string.savetemp); // 按钮叫“暂存”
					if (mWaitorType == 1) {
						mButtonCheckAndSaveTemp.setVisibility(View.GONE);
						mCancleAndReject.setVisibility(View.GONE);
						mButtonSubmite.setText("结账");
					} else if (mWaitorType == 2) {
						mButtonCheckAndSaveTemp.setVisibility(View.GONE);
						mCancleAndReject.setVisibility(View.GONE);
					}
				}
			}

		} else if (status == 3) {
			// 商户端确认收货由系统完成
			if (mTakeaway == 2) {
//				findViewById(R.id.rl_ticket_op).setVisibility(View.GONE);
				mButtonCheckAndSaveTemp.setVisibility(View.GONE);// 暂存，确认订单，接受
				mButtonSubmite.setVisibility(View.GONE);// 提交，结账
				mCancleAndReject.setText("退单");
			} else {
				mButtonCheckAndSaveTemp.setVisibility(View.GONE);
				if (mModify == 1) {
					mButtonSubmite.setText("提交");
					mCancleAndReject.setText("取消");
				} else {
					mButtonSubmite.setText("结账");
					mCancleAndReject.setText("退单");
				}

			}
		} else if (status == 4) { // 已结账状态
			findViewById(R.id.rl_ticket_op).setVisibility(View.GONE);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (Cookies.getTable()) {
			if (mTakeaway == 1) {
				mTableName = Cookies.getTableName();
				mTableId = Cookies.getTableId();
				mTvTableNum.setText(mTableName);
			}
		} else {
			if (mTableId == -1 && mTakeaway == 1) { // 先要选择桌号
				popupTableWindow();
			}
		}
	}

	public void popupWaitorNote() {

		ActPopupComment actComment = new ActPopupComment(ActDishTicket.this, 0,
				"");
		actComment.showAtLocation(mListDish, Gravity.CENTER_VERTICAL, 0, 0);
	}

	private void popupPeopleNum() {
//		Toast.makeText(getBaseContext(), "请选择正确人数", Toast.LENGTH_LONG).show();
		ActPopupPeopleNum actPeopleNum = new ActPopupPeopleNum(
				ActDishTicket.this);
		actPeopleNum.showAtLocation(mListDish, Gravity.CENTER_VERTICAL, 0, 0);
	}

	public void setPeopleNum(int peopleNum) {
		if (peopleNum > 0) {
			mPeopleNum = peopleNum;
			mTvPeopleNum.setText("人数：" + Integer.toString(peopleNum));
		} else {
			mTvPeopleNum.setText("");
		}
	}

	protected void showDialogError(String err) {

		MyDialogUtil errDialog = new MyDialogUtil(ActDishTicket.this) {

			@Override
			public void onClickPositive() {
				finishAndAni();
			}

			@Override
			public void onClickNagative() {
			}
		};
		errDialog.showCustomMessageOK(getString(R.string.notice), err,
				getString(R.string.confirm), keyListener);
	}

	Dialog.OnKeyListener keyListener = new Dialog.OnKeyListener() {

		@Override
		public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				arg0.dismiss();
				finishAndAni();
			}
			return true;
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGetTicketTask != null) {
			mGetTicketTask.cancel(true);
			mGetTicketTask = null;
		}
		if (mSubStatusTicketTask != null) {
			mSubStatusTicketTask.cancel(true);
			mSubStatusTicketTask = null;
		}
	}

	private void refreshView(String userName, String userTel, long createTime,
			String address, String arriveTime, String tableName, int peopleNum,
			String userNote, String waitorNote, String tradeNo,
			String invoiceHead, String totalFee,SimpleCursorAdapter adapter) {
		mHeaderView = LayoutInflater.from(this).inflate(
				R.layout.dish_ticket_detail_head_view, null);
		mFooterView = LayoutInflater.from(this).inflate(
				R.layout.dish_ticket_detail_foot_view, null);

		mListDish = (ListView) findViewById(R.id.lv_ordered_dishes);
		mListDish.addHeaderView(mHeaderView);
		mListDish.addFooterView(mFooterView, null, false);
		mListDish.setAdapter(adapter);
		
		// head
		RelativeLayout rl_address = (RelativeLayout) mHeaderView
				.findViewById(R.id.rl_address);// 配送地址信息
		RelativeLayout rl_user_tel = (RelativeLayout) mHeaderView
				.findViewById(R.id.rl_user_tel);// 用户电话号码
		RelativeLayout rl_user_name = (RelativeLayout) mHeaderView
				.findViewById(R.id.rl_user_name);// 用户号码
		RelativeLayout rl_arrive_time = (RelativeLayout) mHeaderView
				.findViewById(R.id.rl_arrive_time);// 外卖送达时间
		LinearLayout ll_table_people = (LinearLayout) mHeaderView
				.findViewById(R.id.ll_tno_pnum);// 桌号及人数
		RelativeLayout rl_table_num = (RelativeLayout) mHeaderView
				.findViewById(R.id.rl_table_num);// 桌号按钮
		RelativeLayout rl_people_num = (RelativeLayout) mHeaderView
				.findViewById(R.id.rl_people_num);// 人数按钮

		TextView tv_userName = (TextView) mHeaderView
				.findViewById(R.id.tv_user_name);// 用户名
		TextView tv_userTel = (TextView) mHeaderView
				.findViewById(R.id.tv_user_tel);// 用户电话号码
		TextView tv_address = (TextView) mHeaderView
				.findViewById(R.id.tv_address);// 地址信息
		TextView tv_createTime = (TextView) mHeaderView
				.findViewById(R.id.tv_create_time);// 创建时间
		TextView tv_arrive_time = (TextView) mHeaderView
				.findViewById(R.id.tv_arrive_time);// 送达时间
		if(mShopCategory!=1)
		{
			ll_table_people.setVisibility(View.GONE);
		}else{
			mTvTableNum = (TextView) mHeaderView.findViewById(R.id.tv_table_num);
			mTvTableNum.getPaint().setFakeBoldText(true);
			mTvPeopleNum = (TextView) mHeaderView.findViewById(R.id.tv_people_num);
			mTvPeopleNum.getPaint().setFakeBoldText(true);
		}
		
		

		// foot

		RelativeLayout rl_user_note = (RelativeLayout) mFooterView
				.findViewById(R.id.rl_user_note);// 用户备注
		RelativeLayout rl_shop_note = (RelativeLayout) mFooterView
				.findViewById(R.id.rl_shop_note);// 商家备注
		RelativeLayout rl_trade_num = (RelativeLayout) mFooterView
				.findViewById(R.id.rl_trade_num);// 交易单号
		RelativeLayout rl_invoice_head = (RelativeLayout) mFooterView
				.findViewById(R.id.rl_invoice_head);// 交易单号
		RelativeLayout rl_paid_in = (RelativeLayout) mFooterView
				.findViewById(R.id.rl_paid_in);// 实付金额

		rl_people_num.setOnClickListener(mClkListener);
		rl_table_num.setOnClickListener(mClkListener);

		mEtWaitorNote = (EditText) mFooterView
				.findViewById(R.id.et_waiter_note);

		TextView tv_user_note = (TextView) mFooterView
				.findViewById(R.id.tv_user_note);
		TextView tv_trade_no = (TextView) mFooterView
				.findViewById(R.id.tv_trade_no);
		TextView tv_invoice_head = (TextView) mFooterView
				.findViewById(R.id.tv_invoice_head);
		TextView tv_pain_in = (TextView) mFooterView
				.findViewById(R.id.tv_pain_in);

		Log.d("ActDishTicket_refreshview", waitorNote);
		mEtWaitorNote.setText(waitorNote);
		TextView waiterNoteSelector = (TextView) mFooterView
				.findViewById(R.id.tv_show_waiter_note);
		waiterNoteSelector.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popupWaitorNote();
			}
		});
		if (mTicketId == -1) {		
			mListDish.setAdapter(mAdapterDishTicket);
			tv_createTime.setVisibility(View.GONE);// 新订单没有创建时间
			rl_user_name.setVisibility(View.GONE);
			rl_user_tel.setVisibility(View.GONE);
			rl_address.setVisibility(View.GONE);
			rl_arrive_time.setVisibility(View.GONE);

			rl_user_note.setVisibility(View.GONE);
			rl_trade_num.setVisibility(View.GONE);
			rl_invoice_head.setVisibility(View.GONE);
			rl_paid_in.setVisibility(View.GONE);
		} else {
			// 判断用户名称是否为空，并赋值
			tv_createTime
					.setText(getString(R.string.submitted_ticket_time_prefix)
							+ TimeUtil.convertLongToFormatString(createTime,
									"yyyy-MM-dd HH:mm"));
			if (userName.isEmpty()) {
				rl_user_name.setVisibility(View.GONE);
			} else {
				rl_user_name.setVisibility(View.VISIBLE);
				rl_user_name.setClickable(false);
				tv_userName.setText(userName);
			}
			// 判断用户手机号码是否为空，并赋值
			if (userTel.isEmpty()) {
				rl_user_tel.setVisibility(View.GONE);
			} else {
				rl_user_tel.setVisibility(View.VISIBLE);
				rl_user_tel.setOnClickListener(mClkListener);
				tv_userTel.setText(userTel);
			}
			// 用户备注
			// mUserNote = SubmittedTicketDao.getInstance().queryUserNote(
			// ActDishTicket.this, mTicketId);

			if (userNote.isEmpty()) {
				rl_user_note.setVisibility(View.GONE);
			} else {
				rl_user_note.setVisibility(View.VISIBLE);
				tv_user_note.setText(userNote);
			}
			if (tradeNo == null || tradeNo.isEmpty()) {
				rl_trade_num.setVisibility(View.GONE);
			} else {
				tradeNo = tradeNo.replace("T", "");
				switch (mPayType) {
				case 1:
					tradeNo += "(支付宝)";
					break;
				case 2:
					tradeNo += "(微信)";
					break;
				}
				rl_trade_num.setVisibility(View.VISIBLE);
				tv_trade_no.setText(tradeNo);
			}
			if (mStatus == 4) {
				if (totalFee.isEmpty()) {
					rl_paid_in.setVisibility(View.GONE);
				} else {
					rl_paid_in.setVisibility(View.VISIBLE);
					tv_pain_in.setText("￥" + totalFee);
				}
			} else {
				rl_paid_in.setVisibility(View.GONE);
			}
			if (mTakeaway == 1) {
				rl_address.setVisibility(View.GONE);
				rl_arrive_time.setVisibility(View.GONE);
				rl_invoice_head.setVisibility(View.GONE);
				rl_shop_note.setVisibility(View.VISIBLE);
				if(mShopCategory!=1)
				{
					ll_table_people.setVisibility(View.GONE);
				}else{
					ll_table_people.setVisibility(View.VISIBLE);
					// 设置桌号及人数
					if (peopleNum == 0) {
						mTvPeopleNum.setText("");
					} else {
						mTvPeopleNum.setText("人数：" + Integer.toString(peopleNum));
					}
					Log.d("peopleNum刷新订单列表", Integer.toString(mPeopleNum));

					if (tableName == null || tableName.length() == 0) {
						mTvTableNum.setText("请选择桌号");
					} else {
						mTvTableNum.setText(tableName);
					}
				}
			} else {
				// 外卖
				rl_address.setVisibility(View.VISIBLE);
				rl_arrive_time.setVisibility(View.VISIBLE);
				ll_table_people.setVisibility(View.GONE);
				rl_shop_note.setVisibility(View.GONE);

				tv_address.setText(address);
				tv_arrive_time.setText(arriveTime);// mArrivalTime
				// String invoice = SubmittedTicketDao.getInstance()
				// .queryWaiterNoteInvoice(this, mTicketId);
				if (invoiceHead.length() > 0) {
					rl_invoice_head.setVisibility(View.GONE);
				} else {
					rl_invoice_head.setVisibility(View.VISIBLE);
					tv_invoice_head.setVisibility(View.VISIBLE);
					tv_invoice_head.setText(invoiceHead);
				}
			}
		}
		if(mLoadingDialog!=null&&mLoadingDialog.isShowing())
		{
			mLoadingDialog.dismiss();
		}
	}

	private OnClickListener mClkListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;
			case R.id.rl_user_tel: {
				if(mUserTel.length()>=8)
				{
					Uri uri = Uri.parse("tel:" + mUserTel);
					Intent it = new Intent(Intent.ACTION_DIAL, uri);
					startActivity(it);
				}else
				{
					Toast.makeText(getBaseContext(), "用户号码格式不正确，无法拨号..", Toast.LENGTH_LONG).show();
				}
				
			}
				break;

			case R.id.rl_table_num:
				popupTableWindow();
				break;

			case R.id.rl_people_num:
				popupPeopleNum();
				break;
			case R.id.tv_checkout:
				if (mStatus == 0) {
					changeStatus(3);// 用户暂存订单确认到未结账
				} else if (mStatus == 1) { // 如果是未处理或者点菜中状态
					if (mTakeaway == 1)
						changeStatus(2); // 接受，或暂存
					else if (mTakeaway == 2)
						changeStatus(3); // 直接变为提交状态
				} else if (mStatus == 2) {
					if (mTicketId == -1) {
						changeStatus(2);//初始化按钮，需要在status=2
						return;
					}
					if (mPayTime != 0) {
						changeStatus(4);// 确认订单到已完成
					} else {
						if (mModify == 1) {// 修改订单
							changeStatus(2);// 已经暂存的，修改后仍然暂存
						} else {
							changeStatus(3);// 未支付
						}

					}
				}
				break;

			case R.id.tv_submit:
				if (mWaitorType == 2 || mWaitorType == 0) {
					if (mStatus == 3) {
						if (mModify == 1) {
							changeStatus(mStatus);
						} else {
							changeStatus(4);
						}
					} else {
						changeStatus(3);// 用户点菜，最高权限点菜提交订单到未结账
					}
				} else if (mWaitorType == 1) {
					changeStatus(4);// 果然店，直接结账
				}
				break;

			case R.id.tv_cancel:
				if (mModify == 1) {
					finishAndAni();
				} else {
					changeStatus(5);
				}
				break;
			case R.id.bt_title_right: {
				String fee = null;
				if (mTotalFee.length() > 0) {
					fee = mTotalFee;
				} else if (mPrice > 0) {
					fee = StringUtil.getPriceString(mPrice);
				}
				printOrderInfo(mTicketId, fee);
				break;
			}
			}
		}
	};

	private void doRefresh() {

		if (mTicketId == -1)
			return;

		if (mGetTicketTask != null) {
			mGetTicketTask.cancel(true);
			mGetTicketTask = null;
		}
		mGetTicketTask = new PostGetTask<JSONObject>(this) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				JSONObject jSon = OrderdishProcessor.getInstance()
						.getTicketDetail(ActDishTicket.this, mTicketId, Cookies.getUserId(), Cookies.getToken());
				return jSon;
			}

			@Override
			protected void doPostJob(Exception e, JSONObject result) {
				if ((e == null) && (result != null)) {
					try {
						if (result.getInt("result") == 1) {
							if (mTakeaway == 1) {
								try {
									mPeopleNum = result.getInt("people");
								} catch (JSONException e1) {
									mPeopleNum = 0;
								}
								Log.d("peopleNum获取订单列表",
										Integer.toString(mPeopleNum));
							} else {
								try {
									JSONObject takeawayInfo = result
											.getJSONObject("contactInfo");
									mInvoice = takeawayInfo
											.getString("invoice");
								} catch (JSONException e1) {
									mInvoice = "";
								}
							}
							try {
								mPayType = result.getInt("payType");
							} catch (JSONException e1) {
								// e1.printStackTrace();
								mPayType = 0;								
							}
							try {
								mTradeNo = result.getString("tradeNo");
								if(mTradeNo.endsWith("null"))
								{
									mTradeNo="";
								}
							}catch(JSONException e1){
								mTradeNo = "";
							}
							try {
								mWaiterNote=result.getString("waiterComment");
							}catch(JSONException e1){
								mWaiterNote = "";
							}
							if (mStatus == 4) {
								try {
									mTotalFee = result.getString("totalFee");
									if (mTotalFee.equals("null")) {
										mTotalFee = "";
									}
								} catch (JSONException e1) {
									mTotalFee = "";
								}

							}
							// 拿到菜单数据
							String sqlOrderedDish = TicketDetail.COLUMN_NAME_TICKET_ID
									+ "=?";
							String[] arg = { mTicketId + "" };
							Cursor dishCs = managedQuery(
									TicketDetail.CONTENT_URI, null,
									sqlOrderedDish, arg, null);
							Log.d("dishCs=null?", dishCs == null ? "null"
									: "not null");
							boolean modify = false;
							// 判断菜单是否可更改
							if (mTakeaway == 1) {
								if (mStatus == 2 || mStatus == 3) {
									modify = true;
									if (mPayTime != 0) {
										modify = false;
									}
								}
							} else {
								modify = false;
							}
							mAdapterDishTicketDetail = new AdapterDishTicketDetail(
									ActDishTicket.this, dishCs, mTicketId,
									modify);
							Log.d("ActDishTicket_dorefresh", "refreshView");
							refreshView(mUserName, mUserTel, mCreateTime,
									mArrivalAddr, mArrivalTime, mTableName,
									mPeopleNum, mUserNote, mWaiterNote,
									mTradeNo, mInvoice, mTotalFee,mAdapterDishTicketDetail);
						} else {
							Toast.makeText(getBaseContext(), "订单请求错误",
									Toast.LENGTH_LONG).show();
						}

					} catch (JSONException ej) {
						Toast.makeText(getBaseContext(), "获取订单失败，请检查网络并重新登录",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		};
		mGetTicketTask.execute();
	}

	private void onChangeStatus(final int status) {

		if (mTicketId == -1) {
			mPrice = DishesDao.getInstance().totalPrice(ActDishTicket.this);
		} else {
			mPrice = TicketDetailDao.getInstance().totalPrice(
					ActDishTicket.this, mTicketId);
		}
		if (mSubStatusTicketTask != null) {
			mSubStatusTicketTask.cancel(true);
			mSubStatusTicketTask = null;
		}

		mSubStatusTicketTask = new PostGetTask<JSONObject>(this,
				R.string.submiting, R.string.submit_failed, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				if (mTakeaway == 1) {
					mWaiterNote = ((EditText) mFooterView
							.findViewById(R.id.et_waiter_note)).getText()
							.toString();// 外卖订单不需要
				}
				mUserTel = ((TextView) mHeaderView
						.findViewById(R.id.tv_user_tel)).getText().toString();

				int subStatus = status;
				// if (mWaitorType != 1){
				// if (status == 5 || mTakeaway == 2){
				// return OrderdishProcessor.getInstance()
				// .updateDishTicketStatus(ActDishTicket.this,
				// mTicketId, status);
				// }
				// }
				if (mTicketId == -1) {
					if (mWaitorType == 1 && status == 4) {
						subStatus = 2;
					}
					return OrderdishProcessor.getInstance().addDishTicket(
							ActDishTicket.this, Cookies.getShopId(), mTableId,
							mPeopleNum, mUserTel, mWaiterNote, subStatus, Cookies.getUserId(), Cookies.getToken());
				} else {
					if (status == 4) {
						if (mPayTime == 0) {
							subStatus = mStatus;
						}
					}
					return OrderdishProcessor.getInstance().updateDishTicket(
							ActDishTicket.this, mTicketId, mTableId,
							mPeopleNum, mUserTel, mWaiterNote, subStatus, Cookies.getUserId(), Cookies.getToken());
				}
			}

			@Override
			protected void doPostJob(Exception e, JSONObject result) {

				String msg = "操作成功";
				if ((e == null) && (result != null)) {
					try {
						if (result.getInt("result") == 1) {
							if (mTicketId == -1) { // 新订单
								mTicketId = result.getLong("orderDishId");
								mPrice = result.getInt("priceTotal");
								Log.d("新订单提交成功后拿到订单数据",
										Long.toString(mTicketId) + ":"
												+ Integer.toString(mPrice));
								DishesDao.getInstance().resetQuaity(
										ActDishTicket.this);
							} else { // 非新订单
								// 修改菜品

								SubmittedTicketDao.getInstance()
										.updateWaiterNoteTable(
												ActDishTicket.this, mTicketId,
												mWaiterNote, mTableId,
												mTableName);
								// 需要将状态顺便更新一下
								SubmittedTicketDao.getInstance().updateStatus(
										ActDishTicket.this, mTicketId,
										(byte) status);

							}
						}
					} catch (JSONException e1) {
					}

				} else {
					msg = e.getMessage();
				}

				if (mStatus == 1 && MainActivity.mActivity != null) {
					MainActivity.mActivity.updateNumNtf(mTakeaway + 3);
				}

				Toast.makeText(ActDishTicket.this, msg, Toast.LENGTH_LONG)
						.show();
				if (msg.equals("操作成功")) {
					if (status == 4 && mPayTime == 0) { // 如果是结账，并且还未支付，就跳转支付页面
						pay();
					}
					if (status == 3 && mPayTime == 0) {
						if (mWaitorType == 0 || mWaitorType == 1) {
							showDialogToPay(mTicketId, mPrice);
						}
					} else {
						finishAndAni();
					}
				}
			}
		};

		mSubStatusTicketTask.execute();
	}

	private void showDialogToPay(long ticketId, int price) {
		MyDialogUtil showToPayDialog = new MyDialogUtil(ActDishTicket.this) {

			@Override
			public void onClickPositive() {
				// TODO Auto-generated method stub
				pay();
			}

			@Override
			public void onClickNagative() {
				// TODO Auto-generated method stub
				finishAndAni();
			}
		};
		showToPayDialog.showCustomMessage("提示", "订单提交成功，是否结账", "去结账", "暂不结账");
	}

	private void changeStatus(final int status) // 结账
	{
		if (mTakeaway == 1 && status != 5&&status>2&&mShopCategory==1) { // 如果桌号是0，且不是取消订单，不是暂存或接受订单，先选择桌号
			if (status != 5) {
				if (mTableId == 0) {
					if (status != 2) {// 不是接受订单
						popupTableWindow();
						return;
					} else {
						if (mPayTime == 0) {//
							popupTableWindow();
							return;
						}
					}
				}
				if (mPeopleNum == 0) {
					if (status != 2) {
						popupPeopleNum();
						return;
					} else {
						if (mPayTime == 0) {//
							popupPeopleNum();
							return;
						}
					}
				}
			}
		}
		AlertDialog.Builder db = new AlertDialog.Builder(this);
		db.setTitle("确认提示").setMessage("是否确认进行操作？");
		db.setPositiveButton(getString(R.string.confirm),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onChangeStatus(status);
					}
				});
		db.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// finish();
					}
				});
		Dialog dialog = db.create();
		dialog.show();
	}

	private void pay() {
		finish();
		Intent in = new Intent();
		in.putExtra(ConstUtil.DISH_TICKET_ID, mTicketId);
		in.putExtra(ConstUtil.INTENT_EXTRA_NAME.MONEY_AMOUNT, mPrice);
		in.setClass(ActDishTicket.this, ActNewPayment.class);
		startActivityForResult(in, ConstUtil.REQUEST_CODE.PAY_TICKET);
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ConstUtil.REQUEST_CODE.SELECT_TABLE:
				mTableName = data.getStringExtra("tableName");
				mTableId = data.getLongExtra("tableNoId", -1);
				mTvTableNum.setText(mTableName);
				if (mPeopleNum == 0) {
					popupPeopleNum();
				}
				break;
			case ConstUtil.REQUEST_CODE.SELECT_DISHES:
				mDishList = data.getStringExtra("dishList");
				ArrayList<TicketDetail> detailList = new ArrayList<TicketDetail>();
				SQLiteDatabase sqLiteDatabase = new DatabaseHelper(
						ActDishTicket.this).getWritableDatabase();
				sqLiteDatabase.beginTransaction();
				try {
					JSONArray ja = new JSONArray(mDishList);
					for (int i = 0; i < ja.length(); i++) {
						JSONObject j = ja.getJSONObject(i);
						TicketDetail detail = new TicketDetail(j, mTicketId);
						detailList.add(detail);
					}
					TicketDetailDao.getInstance().deleteTicket(sqLiteDatabase,
							mTicketId);
					TicketDetailDao.getInstance().insertOrUpdate(
							sqLiteDatabase, detailList);

					sqLiteDatabase.setTransactionSuccessful();
					getContentResolver().notifyChange(TicketDetail.CONTENT_URI,
							null);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					sqLiteDatabase.endTransaction();
				}
				mModify = data.getIntExtra("modify", 0);
				checkButtonStatus(mStatus, mPayTime);
				break;
			}
		} else if (resultCode == RESULT_CANCELED) {
			switch (requestCode) {
			case ConstUtil.REQUEST_CODE.PAY_TICKET:
				finishAndAni();
				break;
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finishAndAni();
	}

	protected void finishAndAni() {
		setResult(RESULT_OK);
		finish();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}

	private void popupTableWindow() {
//		Toast.makeText(getBaseContext(), "请选择桌号", Toast.LENGTH_SHORT).show();
		Intent it = new Intent();
		it.setClass(ActDishTicket.this, ActSelectTable.class);
		startActivityForResult(it, ConstUtil.REQUEST_CODE.SELECT_TABLE);
	}

	private void printOrderInfo(final long ticketId, final String totalFee) {
		if (ticketId <= 0) {
			Toast.makeText(getBaseContext(), "该订单为新订单，请提交或暂存后补打印", 15000)
					.show();
			return;
		}
		new PostGetTask<JSONObject>(this, R.string.submiting,
				R.string.submit_failed, true, true, false) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				// TODO Auto-generated method stub
				JSONObject jpara = new JSONObject();
				jpara.put("shopId", Cookies.getShopId());
				jpara.put("uid", Cookies.getUserId());
				jpara.put("orderDishId", ticketId);
				jpara.put("totalFee", totalFee);
				jpara.put("token", Cookies.getToken());
				JSONObject jRet = null;
				jRet = HttpMgr.getInstance().postJson(
						ServerUrlConstants.getPrintOrder(), jpara, true);
				return jRet;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				// TODO Auto-generated method stub
				if (exception == null && result != null) {
					try {
						if (result.getInt("result") == 1) {
							Toast.makeText(ActDishTicket.this,
									"打印订单成功，请等待打印机出票", 15000).show();
						} else {
							Toast.makeText(ActDishTicket.this, "打印订单失败，请重新打印",
									15000).show();
						}
					} catch (JSONException e) {
						Toast.makeText(ActDishTicket.this, "打印订单失败，请重新打印",
								15000).show();
					}
				} else {
					Toast.makeText(getBaseContext(), "打印订单失败，请检查网络或重新登陆", 15000)
							.show();
				}
			}
		}.execute();
	};
	// /////////////////////////////////////
	// ////////////////////////////////////
}
