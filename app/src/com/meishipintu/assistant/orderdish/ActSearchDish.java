package com.meishipintu.assistant.orderdish;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.milai.model.Dishes;
import com.meishipintu.core.utils.ConstUtil;
import com.umeng.analytics.MobclickAgent;

public class ActSearchDish extends FragmentActivity {
	private String mShopName = null;
	private ListView mListDish = null;
	private AdapterDishes mAdapterDish = null;
	private EditText mEtSearchKey = null;
	private boolean mNoPic = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.orderdish_dish_search);
		findViewById(R.id.bt_done).setOnClickListener(mClkListener);

		Intent in = getIntent();

		mShopName = Cookies.getShopName();
		if (in.hasExtra(ConstUtil.SHOP_NAME)) {
			mShopName = in.getStringExtra(ConstUtil.SHOP_NAME);
		}
		if (in.hasExtra("disp_pic")) {
			mNoPic = in.getBooleanExtra("disp_pic", false);
		}
		// getSupportLoaderManager().initLoader(0, null, dishLoadCB);
		mListDish = (ListView) findViewById(R.id.lv_dishes);
		mAdapterDish = new AdapterDishes(this, null);
		mAdapterDish.switchPicDisp(mNoPic);
		mListDish.setAdapter(mAdapterDish);

		mEtSearchKey = (EditText) findViewById(R.id.et_search_key);
		mEtSearchKey.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String str = mEtSearchKey.getText().toString().trim();
				Bundle b = new Bundle();
				b.putString("key", str);
				getSupportLoaderManager().restartLoader(1, b, dishLoadCB);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	private OnClickListener mClkListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.rl_dish_total:
			case R.id.tv_num_dishes: {
				Intent in = new Intent();
				in.setClass(ActSearchDish.this, ActDishTicket.class);
				
				in.putExtra(ConstUtil.SHOP_NAME, mShopName);
				startActivity(in);
				overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
				finish();
			}
				break;
			case R.id.bt_done:
				finishAndAni();
				break;
			case R.id.btn_right:

				break;
			}

		}
	};

	private LoaderCallbacks<Cursor> dishLoadCB = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			String sqlWhere = "";
			if (arg0 == 1) {
				String key = arg1.getString("key");
				if (key != null)
					sqlWhere = Dishes.COLUMN_NAME_DISH_NAME
							+ " LIKE '%" + key + "%'";
			}
			return new CursorLoader(ActSearchDish.this, Dishes.CONTENT_URI,
					null, sqlWhere, null, Dishes.COLUMN_NAME_TYPE_ORDER + ","
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finishAndAni();
	}

	protected void finishAndAni() {
		finish();
		overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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