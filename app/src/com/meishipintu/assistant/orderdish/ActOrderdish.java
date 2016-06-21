package com.meishipintu.assistant.orderdish;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.milai.dao.DishesDao;
import com.milai.model.Dishes;
import com.milai.model.TicketDetail;
import com.milai.processor.OrderdishProcessor;
import com.milai.asynctask.PostGetTask;
import com.meishipintu.core.utils.ConstUtil;
import com.milai.utils.CustomProgressDialog;
import com.milai.utils.ScreenParam;
import com.milai.utils.StringUtil;
import com.meishipintu.core.widget.StickyListView;
import com.umeng.analytics.MobclickAgent;

public class ActOrderdish extends FragmentActivity {
	private ListView mListType = null;
	private StickyListView mListDish = null;
	private ListView mLvSelDishes = null;
	public AdapterSelectedDish mAdapterSelDish = null;
	private AdapterTypes mAdapterType = null;
	private AdapterDishes mAdapterDish = null;
	private static final int MSG_DB_CHANGED = 1001;
	private LinearLayout mRlTotal = null;
	private TextView mTvNumDishes = null;
	private TextView mTvTotalPrice = null;
	private TextView mTvTotalPriceOrig = null;	
	private HashMap<Integer, Integer> mTypeDishIndexMap = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> mDishTypeIndexMap = new HashMap<Integer, Integer>();
	private LinearLayout mLlLivContainer = null;
	private int mCurSelType = 0;
	private CustomProgressDialog mProgress = null;
	private AsyncTask<Void, Void, Boolean> mGetDishTask = null;
	private long mStartTime = 0;
	private String mShopName = null;
	private int mCheckedType = 0;
	private boolean mNoPic = false;
	private Button mBtDone = null;
	private Button mBtSwitch = null;
	private boolean mShopInfoExpaned = false;
	private boolean mSelDishesVisible = false;
	private PostGetTask<Integer> mGetShopInfoTask = null;
	private long mTicketId = -1;
	
	public static ActOrderdish mActOrderDish=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActOrderDish=this;
		setContentView(R.layout.layout_orderdish);
		TextView tvTitle = (TextView) findViewById(R.id.tv_title);

		findViewById(R.id.btn_back).setOnClickListener(mClkListener);
		findViewById(R.id.ll_search).setOnClickListener(mClkListener);
		findViewById(R.id.bt_select_done).setOnClickListener(mClkListener);
		findViewById(R.id.bt_dish_selected).setOnClickListener(mClkListener);
		findViewById(R.id.ll_dish_total).setOnClickListener(mClkListener);
		findViewById(R.id.tv_blank_fill).setOnClickListener(mClkListener);
		mBtDone = (Button) findViewById(R.id.bt_select_done);
		mBtSwitch = (Button) findViewById(R.id.btn_switch_disp);
		mBtSwitch.setOnClickListener(mClkListener);
		mBtSwitch.setBackgroundResource(R.drawable.bg_disp_pic);
		Intent in = getIntent();
		if (in.hasExtra(ConstUtil.DISH_TICKET_ID)) {
			mTicketId = in.getLongExtra(ConstUtil.DISH_TICKET_ID, -1);
		}
		mShopName = Cookies.getShopName();
		tvTitle.setText(mShopName);
//		tvTitle.setOnClickListener(mClkListener);
//		tvTitle.setClickable(true);
		TextView tv = (TextView) findViewById(R.id.tv_shop_tel);
		tv.setText(Cookies.getShopTel());
		tv = (TextView) findViewById(R.id.tv_shop_addr);
		tv.setText(Cookies.getShopAddr());
		findViewById(R.id.rl_shop_addr).setClickable(true);
		findViewById(R.id.rl_shop_addr).setOnClickListener(mClkListener);
		findViewById(R.id.rl_shop_tel).setClickable(true);
		findViewById(R.id.rl_shop_tel).setOnClickListener(mClkListener);
		DishesDao.getInstance().resetQuaity(ActOrderdish.this);
		String sqlOrderedDish = Dishes.COLUMN_NAME_DISH_QUANTITY + ">0";
		Cursor dishCs = managedQuery(Dishes.CONTENT_URI, null, sqlOrderedDish,
				null, Dishes.COLUMN_NAME_DISH_ORDER);
		mLvSelDishes = (ListView) findViewById(R.id.lv_dish_selected);
		mAdapterSelDish = new AdapterSelectedDish(ActOrderdish.this, dishCs);
		mLvSelDishes.setAdapter(mAdapterSelDish);
		getSupportLoaderManager().initLoader(0, null, dishLoadCB);
		getSupportLoaderManager().initLoader(1, null, typeLoadCB);
		mListDish = (StickyListView) findViewById(R.id.lv_dishes);
		mListType = (ListView) findViewById(R.id.lv_dish_type);
		mAdapterType = new AdapterTypes(this, null);
		mAdapterDish = new AdapterDishes(this, null);
		mListDish.setAdapter(mAdapterDish);
		mListType.setAdapter(mAdapterType);
		mLlLivContainer = (LinearLayout) findViewById(R.id.ll_list_container);
		prepareDishes(mTicketId);
		mListType.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mCurSelType = 0;
		mListType.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View convertView,
					int position, long id) {
				mListType.setItemChecked(position, true);
				mCheckedType = position;
				mCurSelType = position;
				Integer dishIdx = mTypeDishIndexMap.get(position);
				if (dishIdx != null)
					mListDish.setSelection(dishIdx);
			}
		});
		mListDish.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mDishTypeIndexMap.containsKey(firstVisibleItem)) {
					int typeIdx = mDishTypeIndexMap.get(firstVisibleItem);
					if (typeIdx != mCurSelType) {
						mListType.setItemChecked(typeIdx, true);
						mCheckedType = typeIdx;
						int last = mListType.getLastVisiblePosition();
						int first = mListType.getFirstVisiblePosition();
						if (last > 0 && typeIdx > (last - 1)) {
							mListType.smoothScrollToPosition(typeIdx);
						} else if (typeIdx < (first + 1)) {
							mListType.smoothScrollToPosition(typeIdx);
						}
						mCurSelType = typeIdx;
					}
				}
			}
		});

		getContentResolver().registerContentObserver(Dishes.CONTENT_URI, false,
				mMyObserver);
		mRlTotal = (LinearLayout) findViewById(R.id.rl_dish_total);
		mTvTotalPrice = (TextView) findViewById(R.id.tv_total_price);
		mTvTotalPriceOrig = (TextView) findViewById(R.id.tv_total_price_orig);		
		mTvNumDishes = (TextView) findViewById(R.id.tv_num_dishes);
		mStartTime = System.currentTimeMillis();
		LocalBroadcastManager.getInstance(this).registerReceiver(
				finishReceiver, new IntentFilter("order-dish-done"));
	}

	private OnClickListener mClkListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_select_done: {
				if (mTicketId != -1) {
					try {
						JSONArray dishList = DishesDao.getInstance()
								.getDishTicketList(ActOrderdish.this,
										Cookies.getShopId());
						Intent in = new Intent();
						in.putExtra("dishList", dishList.toString());
						in.putExtra("modify",1);//表示当前订单为“更改”状态。hcs
						Log.d("dishlist to ticket", dishList.toString());
						setResult(RESULT_OK, in);
						finishAndAni();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Intent in = new Intent();
					in.setClass(ActOrderdish.this, ActDishTicket.class);
					in.putExtra(ConstUtil.SHOP_NAME, mShopName);
					in.putExtra("takeaway", 1);
					in.putExtra(ConstUtil.STATUS, 2);
					startActivityForResult(in, ConstUtil.REQUEST_CODE.TICKET_DRAFT);
					overridePendingTransition(R.anim.right_in, R.anim.left_out);
				}
			}
				break;
			case R.id.btn_back:
				finishAndAni();
				break;
			case R.id.ll_search: {
				Intent in = new Intent();
				in.setClass(ActOrderdish.this, ActSearchDish.class);
				in.putExtra(ConstUtil.SHOP_NAME, mShopName);
				in.putExtra("disp_pic", mNoPic);
				startActivity(in);
				overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
			}

				break;
			case R.id.btn_switch_disp:
				if (mNoPic) {
					mAdapterDish.switchPicDisp(false);
					mNoPic = false;
					mBtSwitch.setBackgroundResource(R.drawable.bg_disp_pic);
				} else {
					mAdapterDish.switchPicDisp(true);
					mNoPic = true;
					mBtSwitch.setBackgroundResource(R.drawable.bg_nopic);
				}
				mAdapterDish.notifyDataSetChanged();
				break;
			case R.id.tv_blank_fill:
			case R.id.tv_title:
				TextView tvTitle = (TextView) findViewById(R.id.tv_title);
				if (mShopInfoExpaned) {
					findViewById(R.id.ll_shop_info).setVisibility(
							View.INVISIBLE);
					mShopInfoExpaned = false;
					tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0,
							R.drawable.bg_expand, 0);
				} else {
					findViewById(R.id.ll_shop_info).setVisibility(View.VISIBLE);
					mShopInfoExpaned = true;
					tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0,
							R.drawable.bg_collapse, 0);
				}
				break;

			case R.id.rl_shop_tel: {
				Uri uri = Uri.parse("tel:" + Cookies.getShopTel());
				Intent it = new Intent(Intent.ACTION_DIAL, uri);
				startActivity(it);
			}
				break;
			case R.id.rl_shop_addr:

				try {
					Uri uri = Uri.parse("geo:" + Cookies.getShopLat() + ","
							+ Cookies.getShopLon() + " ?q="
							+ Cookies.getShopLat() + "," + Cookies.getShopLon()
							+ "(" + mShopName + ")");
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(ActOrderdish.this, "您没有安装任何地图APP哦！", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
				break;
			case R.id.ll_dish_total:
			case R.id.bt_dish_selected:
				Button bt = (Button) findViewById(R.id.bt_dish_selected);
				if (mSelDishesVisible) {
					mLvSelDishes.setVisibility(View.GONE);
					mSelDishesVisible = false;
					bt.setBackgroundResource(R.drawable.bg_triangle);
				} else {
					mLvSelDishes.setVisibility(View.VISIBLE);
					mSelDishesVisible = true;
					bt.setBackgroundResource(R.drawable.bg_triangle_rvs);
				}
				break;
			}

		}
	};

	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_DB_CHANGED:
				updateTotal();
				break;
			default:
				break;
			}
		}
	};

	private BroadcastReceiver finishReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};

	private void updateTotal() {
		
		boolean isDishSel = DishesDao.getInstance().hasDishSelected(this);
		ActDishDetail orderDishDetail = null;
		if (ActDishDetail.mDishDetail != null) {
			orderDishDetail = ActDishDetail.mDishDetail;
			orderDishDetail.titleSuspension();
		}
		if (isDishSel) {
			mRlTotal.setVisibility(View.VISIBLE);
			int tp = DishesDao.getInstance().totalPrice(this);
			int tpOrig = DishesDao.getInstance().totalPriceOrig(this);			
			int tq = DishesDao.getInstance().totalQuantity(this);
			int tpsub = tpOrig - tp;
			if (orderDishDetail != null) {
				orderDishDetail.mTotalPrice.setText("总计："
						+ StringUtil.getPriceString(tp));
				orderDishDetail.mSubPrice.setText("已优惠："
						+ StringUtil.getPriceString(tpsub));
				orderDishDetail.mDishesTotalNum.setVisibility(View.VISIBLE);
				orderDishDetail.mDishesTotalNum.setText(Integer.toString(tq));
			}
			mTvNumDishes.setText(tq + getString(R.string.unit_dishes));
			mTvTotalPriceOrig.setText(getString(R.string.tag_total_price_orig)
					+ StringUtil.getPriceString(tpOrig));
			mTvTotalPrice.setText(getString(R.string.tag_total_price_sub)
					+ StringUtil.getPriceString(tpsub));			
			LayoutParams p = mLvSelDishes.getLayoutParams();
			if (tq > 7) {
				p.height = ScreenParam.getInstance().getScreenHeight() / 2;
				mLvSelDishes.setLayoutParams(p);	
			} else {
				p.height = LayoutParams.WRAP_CONTENT;
				mLvSelDishes.setLayoutParams(p);
			}
			mBtDone.setText(getString(R.string.tag_total_price_done) + StringUtil.getPriceString(tp));
		} else {
			mRlTotal.setVisibility(View.GONE);
			if (orderDishDetail != null) {
				orderDishDetail.mTotalPrice.setText("总计："
						+ StringUtil.getPriceString(0));
				orderDishDetail.mSubPrice.setText("已优惠："
						+ StringUtil.getPriceString(0));
				orderDishDetail.mDishesTotalNum.setVisibility(View.GONE);
				orderDishDetail.mDishesTotalNum.setText(Integer.toString(0));
			}
		}
	}

	private MyObserver mMyObserver = new MyObserver(myHandler);

	private class MyObserver extends ContentObserver {
		Handler obsHandler = null;

		public MyObserver(Handler handler) {
			super(handler);
			obsHandler = handler;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			obsHandler.obtainMessage(MSG_DB_CHANGED).sendToTarget();
		}

	};

	private void prepareDishes(final long tid) {
		mProgress = new CustomProgressDialog(ActOrderdish.this,
				getString(R.string.loading));
		mLlLivContainer.setVisibility(View.INVISIBLE);
		mProgress.show();
		if (mGetDishTask != null) {
			mGetDishTask.cancel(true);
			mGetDishTask = null;
		}
		mGetDishTask = new PostGetTask<Boolean>(this) {

			@Override
			protected Boolean doBackgroudJob() throws Exception {				
				// 计算菜品跟分类在列表里的位置
				String ret = null;
				String[] prj = { Dishes.COLUMN_NAME_TYPE_ID,
						Dishes.COLUMN_NAME_DISH_ID };

				String order = Dishes.COLUMN_NAME_TYPE_ORDER + ","
						+ Dishes.COLUMN_NAME_DISH_ORDER;
				Cursor c = getBaseContext().getContentResolver().query(
						Dishes.CONTENT_URI, prj, null, null, order);
				if (c == null || c.getCount() == 0){
					ret = OrderdishProcessor.getInstance().getDishes(
							ActOrderdish.this, Cookies.getUserId(), Cookies.getToken(), Cookies.getShopId());
					if (c.getCount() == 0 ) c.close();
					c = getBaseContext().getContentResolver().query(
							Dishes.CONTENT_URI, prj, null, null, order);
				}
				if (c == null || c.getCount() == 0){
					return false;
				}
				mTypeDishIndexMap.clear();
				mDishTypeIndexMap.clear();
				c.moveToFirst();
				int firstType = -1;
				int idxType = -1;
				int idxDish = 0;
				do {
					int type = c.getInt(0);
					if (type != firstType) {
						idxType++;
						mTypeDishIndexMap.put(idxType, idxDish);

						firstType = type;

					}

					mDishTypeIndexMap.put(idxDish, idxType);
					idxDish++;

				} while (c.moveToNext());
				c.close();
				return true;
			}

			@Override
			protected void doPostJob(Exception e, Boolean result) {
				long curTime = System.currentTimeMillis();
				long timeDelay = 0;

				if ((e != null) || (result == null)) {
					// 网络出问题了，再试试
					if ((curTime - mStartTime) < 500) {
						timeDelay = 500;
					}

					mHandler.postDelayed(startNetCheck, timeDelay);

					return;
				}
				{
					mLlLivContainer.setVisibility(View.VISIBLE);
					mShopName = Cookies.getShopName();
					TextView tvTitle = (TextView) findViewById(R.id.tv_title);
					tvTitle.setText(mShopName);
				}
				// 看看是不是修改，如果是修改要更新已有数量
				if (tid != -1) {
					// 同时更新点菜数据库，为修改做准备
					String[] prj2 = { TicketDetail.COLUMN_NAME_DISH_ID,
							TicketDetail.COLUMN_NAME_DISH_QUANTITY ,TicketDetail.COLUMN_NAME_COMMENT};
					String sel = TicketDetail.COLUMN_NAME_TICKET_ID + "=" + tid;

					Cursor c2 = getBaseContext().getContentResolver().query(
							TicketDetail.CONTENT_URI, prj2, sel, null, null);
					if (c2 != null && c2.getCount() != 0) {
						c2.moveToFirst();
						do {
							DishesDao.getInstance()
							.updateDraftQuantity(ActOrderdish.this,
									c2.getInt(0), c2.getInt(1),c2.getString(2));
						} while (c2.moveToNext());
					}
				}

				mAdapterDish.notifyDataSetChanged();
				mAdapterType.notifyDataSetChanged();
				mProgress.dismiss();
			}

		};
		mGetDishTask.execute();
	}

	public void popupWaitorNote1(long dishId,String dishName)
	{
		ActPopupComment actComment=new ActPopupComment(ActOrderdish.this,dishId,dishName);
		actComment.showAtLocation(mLvSelDishes, Gravity.CENTER
				,0,0);
	}
	
	private Handler mHandler = new Handler();

	private Runnable startNetCheck = new Runnable() {

		@Override
		public void run() {
			mProgress.dismiss();
			Intent in = new Intent();
			/*
			 * if (mMsgId != 0) in.putExtra(ConstUtil.MSG_ID, mMsgId);
			 */
			in.putExtra(ConstUtil.SHOP_NAME, mShopName);
			in.setClass(ActOrderdish.this, ActLoadMenuErr.class);
			startActivity(in);
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
			finish();
		}
	};

	private LoaderCallbacks<Cursor> dishLoadCB = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			return new CursorLoader(ActOrderdish.this, Dishes.CONTENT_URI,
					null, null, null, Dishes.COLUMN_NAME_TYPE_ORDER + ","
							+ Dishes.COLUMN_NAME_DISH_ORDER);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			mAdapterDish.swapCursor(arg1);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapterDish.swapCursor(null);
		}

	};

	private LoaderCallbacks<Cursor> typeLoadCB = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader onCreateLoader(int arg0, Bundle arg1) {
			return new CursorLoader(ActOrderdish.this, Dishes.CONTENT_TYPE_URI,
					null, null, null, Dishes.COLUMN_NAME_TYPE_ORDER);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			mAdapterType.swapCursor(arg1);
			mListType.setItemChecked(mCheckedType, true);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapterType.swapCursor(null);
		}

	};

	@Override
	public void onBackPressed() {

		if (mShopInfoExpaned) {
			findViewById(R.id.ll_shop_info).setVisibility(View.GONE);
			mShopInfoExpaned = false;
		} else if (mSelDishesVisible) {
			mLvSelDishes.setVisibility(View.GONE);
			mSelDishesVisible = false;
		} else {
			super.onBackPressed();
			finishAndAni();
		}

	}

	protected void finishAndAni() {
		finish();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(mMyObserver);
		if (mGetDishTask != null) {
			mGetDishTask.cancel(true);
			mGetDishTask = null;
		}
		if (mGetShopInfoTask != null) {
			mGetShopInfoTask.cancel(true);
			mGetShopInfoTask = null;
		}
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				finishReceiver);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ConstUtil.REQUEST_CODE.TICKET_DRAFT:
				finish();
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
}