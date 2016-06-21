package com.meishipintu.assistant.orderdish;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.milai.model.ShopTable;

public class AdapterTableList extends SimpleCursorAdapter {

	private Activity context;

	public static class ViewHolder {
		TextView tvTableName;
	}

	private LayoutInflater inflater;

	@SuppressWarnings("deprecation")
	public AdapterTableList(Activity context, Cursor c) {
		super(context, -1, c, new String[] {}, new int[] {});
		inflater = LayoutInflater.from(context);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null || convertView.getTag() == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_table_name, null);
			viewHolder.tvTableName = (TextView) convertView
					.findViewById(R.id.tv_table_name);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Cursor c = (Cursor) getItem(position);
		String tableName = c.getString(c.getColumnIndex(ShopTable.COLUMN_NAME_TABLE_NAME));
		viewHolder.tvTableName.setText(tableName);
		return convertView;
	}
}
