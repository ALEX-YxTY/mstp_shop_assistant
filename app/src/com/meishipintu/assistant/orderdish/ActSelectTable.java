package com.meishipintu.assistant.orderdish;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.milai.model.ShopTable;
import com.umeng.analytics.MobclickAgent;

public class ActSelectTable extends FragmentActivity {
	private AdapterTableList mAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_table);
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setText(getString(R.string.tag_sel_table));
		findViewById(R.id.btn_back).setOnClickListener(ll);
		GridView gv = (GridView) findViewById(R.id.gv_tables);
		mAdapter = new AdapterTableList(this, null);
		gv.setAdapter(mAdapter);
		getSupportLoaderManager().initLoader(0, null, tablesLoaderCB);
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor c = (Cursor) parent.getAdapter().getItem(position);
				long tableId = c.getLong(c.getColumnIndex(ShopTable.COLUMN_NAME_TABLE_ID));
				String tableName = c.getString(c.getColumnIndex(ShopTable.COLUMN_NAME_TABLE_NAME));
				Intent i = new Intent();
				i.putExtra("tableNoId", tableId);
				i.putExtra("tableName", tableName);
				setResult(RESULT_OK, i);
				finishAndAni();
			}
		});
	}

	private LoaderCallbacks<Cursor> tablesLoaderCB = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

			return new CursorLoader(ActSelectTable.this, ShopTable.CONTENT_URI,
					null, null, null, ShopTable.COLUMN_NAME_TABLE_NAME);
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

	private OnClickListener ll = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;

			default:
				break;
			}

		}
	};

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
//		overridePendingTransition(R.anim.bottom_out, R.anim.top_in);
	}
}
