package com.meishipintu.assistant.orderdish;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.milai.model.Dishes;
import com.milai.utils.StringUtil;

public class AdapterDishTicket extends SimpleCursorAdapter {

	private Activity context;
	private final static int VIEW_TYPE_DISH = 0;
	private final static int VIEW_TYPE_ADD_DISH = 1;
	private final static int VIEW_TYPE_COUNT = 2;

	public static class ViewHolder {
		TextView tvDishName;
		TextView tvDishPrice;
		TextView tvQuantity;
		TextView tvTotalQty;
		TextView tvTotalPrice;
		TextView tvModify;
		TextView tvComment;
	}

	private int mPrice;
	private long mQuantity;

	private LayoutInflater inflater;

	@SuppressWarnings("deprecation")
	public AdapterDishTicket(Activity context, Cursor c,
			int price, int quantity) {
		super(context, -1, c, new String[] {}, new int[] {});
		inflater = LayoutInflater.from(context);
		this.context = context;
		mPrice = price;
		mQuantity = quantity;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		int type = getItemViewType(position);
		if (convertView == null || convertView.getTag() == null) {
			viewHolder = new ViewHolder();
			switch (type) {
			case VIEW_TYPE_DISH:
				convertView = inflater.inflate(
						R.layout.item_dish_ticket_detail, null);
				viewHolder.tvDishName = (TextView) convertView
						.findViewById(R.id.tv_dish_name);
				viewHolder.tvDishPrice = (TextView) convertView
						.findViewById(R.id.tv_dish_price);
				viewHolder.tvQuantity = (TextView) convertView
						.findViewById(R.id.tv_dish_quantity);
				viewHolder.tvComment = (TextView) convertView
						.findViewById(R.id.tv_comment);			
				break;
			case VIEW_TYPE_ADD_DISH:
				convertView = inflater.inflate(R.layout.item_dish_ticket_sum,
						null);
				viewHolder.tvTotalQty = (TextView) convertView
						.findViewById(R.id.tv_total_dish);
				viewHolder.tvTotalPrice = (TextView) convertView
						.findViewById(R.id.tv_total_price);
				viewHolder.tvModify = (TextView)convertView.findViewById(R.id.tv_modify);
				break;
			}
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		switch (type) {
		case VIEW_TYPE_DISH: {
			Cursor c = (Cursor) getItem(position);

			String dishName = c.getString(c
					.getColumnIndex(Dishes.COLUMN_NAME_DISH_NAME));
			viewHolder.tvDishName.setText(dishName);
			int dishPrice = c.getInt(c
					.getColumnIndex(Dishes.COLUMN_NAME_PRICE));
			viewHolder.tvDishPrice.setText(context.getString(R.string.unit_rmb)
					+ StringUtil.getPriceString(dishPrice));
			int dishQty = c.getInt(c
					.getColumnIndex(Dishes.COLUMN_NAME_DISH_QUANTITY));
			viewHolder.tvQuantity.setText("x" + dishQty);

			String comment=c.getString(c
					.getColumnIndex(Dishes.COLUMN_NAME_COMMENT));
			if(comment.length()>0)
			{
				viewHolder.tvComment.setVisibility(View.VISIBLE);
				Log.d("dishName", comment);
			}else
			{
				viewHolder.tvComment.setVisibility(View.GONE);
			}
		}
			break;
		case VIEW_TYPE_ADD_DISH: {
			viewHolder.tvTotalQty.setText(context.getString(
					R.string.total_dishes_param, mQuantity));
			viewHolder.tvTotalPrice.setText(context
					.getString(R.string.tag_total_price)
					+ StringUtil.getPriceString(mPrice));
			viewHolder.tvModify.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					context.finish();
					context.overridePendingTransition(R.anim.left_in, R.anim.right_out);
				}
			});
			
		}
			break;

		}

		return convertView;
	}

	@Override
	public int getItemViewType(int position) {
		int count = getCount();
		if (position < (count - 1)) {
			return VIEW_TYPE_DISH;
		}
		return VIEW_TYPE_ADD_DISH;
	}

	@Override
	public int getCount() {
		return super.getCount() + 1;
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}

}
