package com.meishipintu.assistant.orderdish;

import android.annotation.SuppressLint;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.milai.model.SubmittedTicket;
import com.milai.processor.OrderdishProcessor;
import com.milai.asynctask.LoadMoreTask;
import com.milai.asynctask.RefreshTask;
import com.meishipintu.core.utils.ConstUtil;
import com.milai.utils.CustomProgressDialog;
import com.meishipintu.core.widget.LoadableListView;
import com.meishipintu.core.widget.LoadableListView.FootViewListener;
import com.meishipintu.core.widget.LoadableListView.HeaderViewListener;
import com.umeng.analytics.MobclickAgent;

public class ActSubmittedTicket extends FragmentActivity {
	private LoadableListView mLiv;
	private AdapterSubmittedTicket mAdapter;
	private boolean mIsAll;
	private LoadMoreTask mloadMoreTask = null;
	protected CustomProgressDialog mLoadingDialog = null;

	private int mStatus;
	private int mTakeaway;
	private int mWaitorType = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submitted_ticket);

		Intent intent = getIntent();
		mStatus = intent.getIntExtra("status", 3);
		mTakeaway = intent.getIntExtra("takeaway", 1);
		mWaitorType = Cookies.getWaitorType();
		refreshView();
		
	}
	private void refreshView()
	{
		Button btnBack = (Button) findViewById(R.id.btn_back); // 返回按钮
		btnBack.setOnClickListener(mClkListener);
		TextView tvTitle = (TextView) findViewById(R.id.tv_title); // title
		Button btTakeawayAndOrderChange=(Button)findViewById(R.id.btn_takeaway_change);
		
		btTakeawayAndOrderChange.setOnClickListener(mClkListener);
		Button qr_ticket=(Button)findViewById(R.id.btn_qr);
		qr_ticket.setOnClickListener(mClkListener);
		if (mTakeaway == 1) {
			tvTitle.setText(getString(R.string.orderInshop)); // title名字
			btTakeawayAndOrderChange.setBackgroundResource(R.drawable.btn_order_takeaway_selector);
			qr_ticket.setVisibility(View.VISIBLE);
		} else {
			tvTitle.setText(getString(R.string.orderTakeaway)); // title名字
			btTakeawayAndOrderChange.setBackgroundResource(R.drawable.btn_takeaway_order_selector);
			qr_ticket.setVisibility(View.GONE);
		}
		mLiv = (LoadableListView) findViewById(R.id.llv_orderdish);

		mAdapter = new AdapterSubmittedTicket(this, null, mTakeaway);
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
			public void onItemClick(AdapterView<?> parent, View convertView,
					int position, long id) {
				Cursor c = (Cursor) parent.getAdapter().getItem(position);

				Intent in = new Intent();
				in.setClass(getBaseContext(), ActDishTicket.class);

				long ticketId = c.getLong(c
						.getColumnIndex(SubmittedTicket.COLUMN_NAME_DISH_TICKET_ID));
				String userName = c.getString(c
						.getColumnIndex(SubmittedTicket.COLUMN_NAME_USER_NAME));
				String userTel = c.getString(c
						.getColumnIndex(SubmittedTicket.COLUMN_NAME_USER_TEL));
				long ct = c.getLong(c
						.getColumnIndex(SubmittedTicket.COLUMN_NAME_CREATE_TIME));
				long pt = c.getLong(c
						.getColumnIndex(SubmittedTicket.COLUMN_NAME_PAY_TIME));

				in.putExtra(ConstUtil.DISH_TICKET_ID, ticketId);
				in.putExtra(ConstUtil.USER_NAME, userName);
				in.putExtra(ConstUtil.USER_TEL, userTel);
				in.putExtra(ConstUtil.CREATE_TIME, ct);
				in.putExtra(ConstUtil.PAY_TIME, pt);
				in.putExtra(ConstUtil.STATUS, mStatus);
				in.putExtra("takeaway", mTakeaway);

				if (mTakeaway == 1) {
					long tableId = c.getLong(c
							.getColumnIndex(SubmittedTicket.COLUMN_NAME_TABLE_ID));
					String tableName = c.getString(c
							.getColumnIndex(SubmittedTicket.COLUMN_NAME_TABLE_NAME));
					String userNote = c.getString(c
							.getColumnIndex(SubmittedTicket.COLUMN_NAME_USER_NOTE));
					String waiterNote = c.getString(c
							.getColumnIndex(SubmittedTicket.COLUMN_NAME_WAITER_NOTE));
					int peopleNum=c.getInt(c.getColumnIndex(SubmittedTicket.COLUMN_NAME_PEOPLE_NUM));

					in.putExtra(ConstUtil.TABLE_ID, tableId);
					in.putExtra(ConstUtil.TABLE_NAME, tableName);
					in.putExtra(ConstUtil.USER_NOTE, userNote);
					in.putExtra(ConstUtil.WAITER_NOTE, waiterNote);
					in.putExtra(ConstUtil.PEOPLE_NUM, peopleNum);
				} else {
					long atime = c.getLong(c
							.getColumnIndex(SubmittedTicket.COLUMN_NAME_ARRIVAL_TIME));
					String addr = c.getString(c
							.getColumnIndex(SubmittedTicket.COLUMN_NAME_TABLE_NAME));
					in.putExtra(ConstUtil.INTENT_EXTRA_NAME.ADDRESS, addr);
					in.putExtra(ConstUtil.INTENT_EXTRA_NAME.ARRIVAL_TIME, atime);
				}
				startActivity(in);
			}
		});

		RadioGroup rg = (RadioGroup) findViewById(R.id.rg_time_filter);
		rg.setOnCheckedChangeListener(ckListener);
		RadioButton rb_1 = (RadioButton) findViewById(R.id.rb_status1);
		RadioButton rb_2 = (RadioButton) findViewById(R.id.rb_status2);
		RadioButton rb_2_2 =(RadioButton)findViewById(R.id.rb_status2_2);;
		RadioButton rb_3 = (RadioButton) findViewById(R.id.rb_status3);
		RadioButton rb_4 = (RadioButton) findViewById(R.id.rb_status4);
		Bundle b = new Bundle();
		if(mTakeaway==2)
		{		
			rb_2.setVisibility(View.GONE);
			rb_2_2.setVisibility(View.GONE);
			rb_3.setText("未收货");		
		}else
		{
			rb_2.setVisibility(View.VISIBLE);
			rb_2_2.setVisibility(View.VISIBLE);
			rb_3.setText("未结账");
		}
		if(mStatus==7)//来自微信订单
		{
			rb_2_2.setChecked(true);
			b.putInt("filter", 2);
			b.putInt("payTime", 1);
		}else if(mStatus==1)
		{
			rb_1.setChecked(true);
			b.putInt("filter", 1);
		}else
		{
			rb_3.setChecked(true);
			b.putInt("filter", 3);
		}
		
		if (mWaitorType == 2) {
			rb_1.setClickable(false);
			rb_2_2.setClickable(false);		
			rb_2.setClickable(false);	
			rb_4.setClickable(false);
		}
		Log.d("refreshView", "do supportManager");
		if(getSupportLoaderManager().getLoader(1)!=null)
		{
			getSupportLoaderManager().restartLoader(1, b,ticketLoaderCB);
		}else
		{
			getSupportLoaderManager().initLoader(1, b, ticketLoaderCB);
		}
	}

	private OnCheckedChangeListener ckListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			Bundle b = new Bundle();
			switch (checkedId) {
			case R.id.rb_status1:
//				mStatus=1;
				if (mWaitorType != 2) {
					b.putInt("filter", 1);
					b.putInt("payTime", 0);
					getSupportLoaderManager().restartLoader(1, b,
							ticketLoaderCB);
				} else {
					Toast.makeText(getBaseContext(), "对不起，当前用户无此权限",
							Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.rb_status2_2:
				if (mWaitorType != 2) {
					b.putInt("filter",2);
					b.putInt("payTime", 1);
					getSupportLoaderManager().restartLoader(1, b,
							ticketLoaderCB);
				} else {
					Toast.makeText(getBaseContext(), "对不起，当前用户无此权限",
							Toast.LENGTH_LONG).show();
				}
				break;			
			case R.id.rb_status2:
				if (mWaitorType != 2) {
					b.putInt("filter", 2);
					b.putInt("payTime", 0);
					getSupportLoaderManager().restartLoader(1, b,
							ticketLoaderCB);
				} else {
					Toast.makeText(getBaseContext(), "对不起，当前用户无此权限",
							Toast.LENGTH_LONG).show();
				}

				break;
			case R.id.rb_status3:
				b.putInt("filter", 3);
				b.putInt("payTime", 0);
				getSupportLoaderManager().restartLoader(1, b, ticketLoaderCB);
				break;
			case R.id.rb_status4:
				if (mWaitorType != 2) {
					b.putInt("filter", 4);
					b.putInt("payTime", 0);
					getSupportLoaderManager().restartLoader(1, b,
							ticketLoaderCB);
				} else {
					Toast.makeText(getBaseContext(), "对不起，当前用户无此权限",
							Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	};
	
	
	private LoaderCallbacks<Cursor> ticketLoaderCB = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			mLoadingDialog = new CustomProgressDialog(ActSubmittedTicket.this,
					"正在加载中");
			mLoadingDialog.show();
			mStatus = arg1.getInt("filter");
			int payStatus=arg1.getInt("payTime");
			String sql = null;
			
			sql = SubmittedTicket.COLUMN_NAME_TYPE + "="
					+ Integer.toString(mTakeaway) + " AND "
					+ SubmittedTicket.COLUMN_NAME_STATUS + "="
					+ Integer.toString(mStatus);
			
			if (mWaitorType == 2) {
				
				sql = sql+" AND "+ SubmittedTicket.COLUMN_NAME_TABLE_ID + "="+ Cookies.getTableId();
			}
			if(mStatus==2&&payStatus==1)
			{
				sql = sql + " AND " + "("+SubmittedTicket.COLUMN_NAME_PAY_TIME
						+ ">0"+" OR "+"LENGTH("+SubmittedTicket.COLUMN_NAME_USER_NAME+")"+">0"+")";
			}else if(mStatus==2&&payStatus==0)
			{
				sql = sql + " AND " + SubmittedTicket.COLUMN_NAME_PAY_TIME
						+ "=0"+" AND "+"LENGTH("+SubmittedTicket.COLUMN_NAME_USER_NAME+")"+"=0";
			}
			if (mTakeaway == 1) {
				sql = sql + " AND " + SubmittedTicket.COLUMN_NAME_IS_SHOW
						+ "=1";
			}
			mLoadingDialog.dismiss();
//			return new CursorLoader(mParentActivity,
//					SubmittedTicket.CONTENT_URI, null, sql, null,
//					SubmittedTicket.COLUMN_NAME_CREATE_TIME + " DESC ");
			return new CursorLoader(ActSubmittedTicket.this,
					SubmittedTicket.CONTENT_URI, null, sql, null,
					SubmittedTicket.COLUMN_NAME_UPDATE_TIME + " DESC ");
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

	private OnClickListener mClkListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;
			case R.id.btn_qr:
				Intent intent = new Intent();
				intent.setClass(ActSubmittedTicket.this, ActCaptureTicket.class);
				startActivity(intent);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;
			case R.id.btn_takeaway_change:
			{
				Intent in=new Intent();
				if(mTakeaway==1)
				{
					mStatus=3;
					mTakeaway = 2;
					
				}else
				{
					mStatus=3;
					mTakeaway = 1;
				}
				refreshView();
				break;
			}
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
				return OrderdishProcessor.getInstance().getSubedTicketRefresh(
						ActSubmittedTicket.this, mTakeaway, mStatus, Cookies.getUserId(), Cookies.getToken());
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
				return OrderdishProcessor.getInstance().getSubedTicketMore(
						ActSubmittedTicket.this, mTakeaway, mStatus, Cookies.getUserId(), Cookies.getToken());
			}
		};
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mloadMoreTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			mloadMoreTask.execute();
	}

	private void requeryCursor() {
		if (!mIsAll) {
			mIsAll = OrderdishProcessor.getInstance().isSubedTicketAll(this,
					mTakeaway, mStatus, Cookies.getUserId());
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
	protected void onStart() {
		super.onStart();
		doRefresh();
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
		finish();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
