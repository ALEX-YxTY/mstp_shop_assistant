


package com.meishipintu.assistant.ui.pay;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.adapter.AdapterPayment;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.ui.pay.TransactionSummaryActivity;
import com.milai.model.Payment;
import com.milai.processor.PaymentProcessor;
import com.milai.asynctask.LoadMoreTask;
import com.milai.asynctask.RefreshTask;
import com.meishipintu.core.widget.LoadableListView;
import com.meishipintu.core.widget.LoadableListView.FootViewListener;
import com.meishipintu.core.widget.LoadableListView.HeaderViewListener;
import com.umeng.analytics.MobclickAgent;

public class ActPayment extends FragmentActivity {

	private LoadableListView mLiv;
	private AdapterPayment mAdapter;
	private boolean mIsAll;
	private LoadMoreTask mloadMoreTask = null;

	public static ActPayment mPayment = null;
	private ProgressDialog m_progressDialog;

	private TextView mTvNick = null;
	private TextView mTvTel = null;
	private TextView mTvTime = null;
	private TextView mTvSubject = null;
	private TextView mTvTotalPrice = null;
	private TextView mTvTradeNo = null;
	private TextView mTvOutTradeNo = null;
	private TextView mTvRefundButton = null;
	private TextView mTvPayType = null;
	private ImageButton ib_report_forms;
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
	private String mCouponSn;
	private Animation mScaleAniDown=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_payment_list);
		mScaleAniDown=new ScaleAnimation(0.9f, 1, 0.9f, 1, Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		mScaleAniDown.setDuration(400);
		//
		// mTvNick = (TextView)findViewById(R.id.tv_nick);
		// mTvTel = (TextView)findViewById(R.id.tv_tel);
		// mTvTime = (TextView)findViewById(R.id.tv_time);
		// mTvSubject = (TextView)findViewById(R.id.tv_subject);
		// mTvTotalPrice = (TextView)findViewById(R.id.tv_total_fee);
		// mTvTradeNo = (TextView)findViewById(R.id.tv_tradeno);
		// mTvOutTradeNo = (TextView)findViewById(R.id.tv_outtradeno);
		// mTvPayType = (TextView)findViewById(R.id.tv_paytype);
		// mTvRefundButton = (TextView)findViewById(R.id.tv_refund);
		// mTvRefundButton.setOnClickListener(mClkListener);
		//
		TextView tv_title = (TextView) this.findViewById(R.id.tv_title);
		tv_title.setText("收银详情");
		mLiv = (LoadableListView) findViewById(R.id.llv_payment_list);
		mAdapter = new AdapterPayment(this, null);
		ib_report_forms=(ImageButton) findViewById(R.id.ib_report_forms);
		ib_report_forms.setVisibility(View.VISIBLE);
		ib_report_forms.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				v.setAnimation(mScaleAniDown);	
				v.startAnimation(mScaleAniDown);
				switch (v.getId()) {
				case R.id.ib_report_forms:
					Intent i=new Intent(ActPayment.this,TransactionSummaryActivity.class);
					startActivity(i);
					break;

				default:
					break;
				}
			}
		});
		findViewById(R.id.btn_back).setOnClickListener(mClkListener);
		mLiv.setAdapter(mAdapter);
		mLiv.changeFootViewByState(LoadableListView.FOOT_VIEW_STATE_MORE);
		mLiv.setHeaderViewListener(new HeaderViewListener() {
			@Override
			public void onRefresh() {
				doRefresh();
			}
		});
		mLiv.setFootViewListener(new FootViewListener() {

			@Override
			public void onClick() {
				doLoadMore();
			}
		});
		mLiv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				mAdapter.setSelectedPosition(position);
				mAdapter.notifyDataSetChanged();
				Cursor c = (Cursor) parent.getAdapter().getItem(position);
				// mLiv.setSelection(position);

				mPaymentId = c.getLong(c
						.getColumnIndex(Payment.COLUMN_NAME_PAYMENT_ID));
				mNick = c.getString(c
						.getColumnIndex(Payment.COLUMN_NAME_USER_NAME));
				mTel = c.getString(c
						.getColumnIndex(Payment.COLUMN_NAME_USER_TEL));
				mTime = c.getLong(c
						.getColumnIndex(Payment.COLUMN_NAME_UPDATE_TIME));
				mSubject = c.getString(c
						.getColumnIndex(Payment.COLUMN_NAME_SUBJECT));
				mTradeNo = c.getString(c
						.getColumnIndex(Payment.COLUMN_NAME_TRADE_NO));
				mOutTradeNo = c.getString(c
						.getColumnIndex(Payment.COLUMN_NAME_OUT_TRADE_NO));
				mPrice = c.getString(c
						.getColumnIndex(Payment.COLUMN_NAME_TOTAL_PRICE));
				mPayType = c.getInt(c.getColumnIndex(Payment.COLUMN_NAME_TYPE));
				mStatus = c.getInt(c.getColumnIndex(Payment.COLUMN_NAME_STATUS));
				mCouponSn=c.getString(c.getColumnIndex(Payment.COLUMN_NAME_COUPON_SN));
				
				Log.d("PaymentListinfo" + Integer.toString(position), mNick
						+ ":" + mTel + ":" + Long.toString(mTime) + ":"
						+ mSubject + ":" + mPrice + ":" + mTradeNo + ":"
						+ mOutTradeNo + ":" + Integer.toString(mPayType)+":"+mCouponSn);
				// doRefreshDetail(mNick, mTel, mTime, mSubject, mPrice,
				// mTradeNo,
				// mOutTradeNo, mPayType);//手机版本加入页面调转
				Intent in = new Intent();
				in.putExtra("paymentId", mPaymentId);
				in.putExtra("nick", mNick);
				in.putExtra("tel", mTel);
				in.putExtra("time", mTime);
				in.putExtra("subject", mSubject);
				in.putExtra("tradeNo", mTradeNo);
				in.putExtra("outTradeNo", mOutTradeNo);
				in.putExtra("price", mPrice);
				in.putExtra("payType", mPayType);
				in.putExtra("status", mStatus);
				in.putExtra("couponSn", mCouponSn);
				in.setClass(ActPayment.this, ActPaymentDetail.class);
				startActivity(in);
			}
		});

		RadioGroup rg = (RadioGroup) findViewById(R.id.rg_time_filter);
		rg.setOnCheckedChangeListener(ckListener);
		int rbi = R.id.rb_status1;
		int paytype = Cookies.getPayType();
		switch(paytype) {
			case 0:
			case 1:
			case 2:
			case 3:		
			case 13:					
				rbi = R.id.rb_status1;
				break;
			case 4:		
			case 14:
				rbi = R.id.rb_status2;
				break;	
			case 5:	
			case 10:
			case 11:
				rbi = R.id.rb_status3;
				break;		
			case 6:	
			case 7:
			case 8:
			case 15:				
				rbi = R.id.rb_status4;
				break;		
			case 9:	
			case 12:			
				rbi = R.id.rb_status5;
				break;					
		}
			
		RadioButton rb = (RadioButton) findViewById(rbi);
		rb.setChecked(true);
		Bundle b = new Bundle();
		b.putInt("filter", 1);
		
		getSupportLoaderManager().initLoader(1, b, paymentLoaderCB);
		doRefresh();
	}

	private OnCheckedChangeListener ckListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			Bundle b = new Bundle();
			mAdapter.setSelectedPosition(-1);
			switch (checkedId) {
			case R.id.rb_status1:
				b.putInt("filter", 1);
				getSupportLoaderManager().restartLoader(1, b, paymentLoaderCB);
				break;
			case R.id.rb_status2:
				b.putInt("filter", 2);
				getSupportLoaderManager().restartLoader(1, b, paymentLoaderCB);
				break;
			case R.id.rb_status3:
				b.putInt("filter", 3);
				getSupportLoaderManager().restartLoader(1, b, paymentLoaderCB);
				break;
			case R.id.rb_status4:
				b.putInt("filter", 4);
				getSupportLoaderManager().restartLoader(1, b, paymentLoaderCB);
				break;
			case R.id.rb_status5:
				b.putInt("filter", 5);
				getSupportLoaderManager().restartLoader(1, b, paymentLoaderCB);
				break;
			}
		}
	};

	private OnClickListener mClkListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;
			}

		}
	};

	private void doRefresh() {
		RefreshTask refreshTask = new RefreshTask(this) {

			@Override
			public void doRequeryCursor() {
				requeryCursor();
			}

			@Override
			public void doOnRefreshFinish() {
				mLiv.onRefreshComplete();
				updateFootView();
			}

			@Override
			public int doGetNetRefresh() throws Exception {
				mIsAll = false;
				return PaymentProcessor.getInstance().getPaymentRefresh(
						ActPayment.this, mPayType, Cookies.getUserId(), Cookies.getToken(), Cookies.getShopId());
			}
		};
		refreshTask.execute();
	}

	@SuppressLint("NewApi")
	private void doLoadMore() {
		if (mloadMoreTask != null) {
			mloadMoreTask.cancel(true);
			mloadMoreTask = null;
		}
		mloadMoreTask = new LoadMoreTask(this) {

			@Override
			protected void doUpdateFootView() {
				updateFootView();
			}

			@Override
			protected void doRequeryCursor() {
				requeryCursor();
			}

			@Override
			protected int doGetNetLoadMore() throws Exception {
				return PaymentProcessor.getInstance().getPaymentMore(
						ActPayment.this, mPayType, Cookies.getUserId(), Cookies.getToken(), Cookies.getShopId());
			}
		};
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mloadMoreTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			mloadMoreTask.execute();
	}

	private LoaderCallbacks<Cursor> paymentLoaderCB = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			int mode = arg1.getInt("filter");
			String sql = "";
			if (mode == 1) {
				sql = Payment.COLUMN_NAME_TYPE + " = 1" + " OR "
						+ Payment.COLUMN_NAME_TYPE + " = 2" + " OR "
						+ Payment.COLUMN_NAME_TYPE + " = 3"+" OR "+Payment.COLUMN_NAME_TYPE+" =13";
			} else if (mode == 2) {
				sql = Payment.COLUMN_NAME_TYPE + " = 4"+" OR "+Payment.COLUMN_NAME_TYPE+"=14";
			} else if (mode == 3) {
				sql = Payment.COLUMN_NAME_TYPE + " = 5"+" OR "+Payment.COLUMN_NAME_TYPE+"=11";
			} else if (mode == 4) {
				sql = Payment.COLUMN_NAME_TYPE + " = 6" + " OR "
						+ Payment.COLUMN_NAME_TYPE + " = 7" + " OR "
						+ Payment.COLUMN_NAME_TYPE + " = 8" + " OR "
						+ Payment.COLUMN_NAME_TYPE + "= 15";
			} else if (mode == 5) {
				sql = Payment.COLUMN_NAME_TYPE + " = 9"+" OR "+Payment.COLUMN_NAME_TYPE+"=12";
			}
			return new CursorLoader(ActPayment.this, Payment.CONTENT_URI, null,
					sql, null, Payment.COLUMN_NAME_UPDATE_TIME + " DESC ");
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			mAdapter.swapCursor(arg1);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.swapCursor(null);
		}

	};

	private void requeryCursor() {
		if (!mIsAll) {
			mIsAll = PaymentProcessor.getInstance().isAll(this, mPayType, Cookies.getUserId());
		}
	}

	protected void updateFootView() {
		if (mIsAll) {
			mLiv.changeFootViewByState(LoadableListView.FOOT_VIEW_STATE_ALL);
		} else {
			mLiv.changeFootViewByState(LoadableListView.FOOT_VIEW_STATE_MORE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		ib_report_forms.setVisibility(View.VISIBLE);
		mAdapter.setSelectedPosition(-1);
		MobclickAgent.onResume(this);
		doRefresh();
	}

	
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		ib_report_forms.setVisibility(View.VISIBLE);
		
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
		finish();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ib_report_forms.setVisibility(View.GONE);
	}
}
