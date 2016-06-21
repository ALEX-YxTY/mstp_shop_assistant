package com.meishipintu.assistant.orderdish;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.milai.dao.DishesDao;
import com.milai.model.Dishes;
import com.milai.utils.StringUtil;

public class AdapterSelectedDish extends SimpleCursorAdapter {

	private Activity context;

	private ViewHolder viewHolder;
	private String commentString ;

	private ViewGroup viewGroup;
	private String mDishComment;

	public static class ViewHolder {
		TextView tvDishName;
		TextView tvDishPrice;
		Button btnPlus;
		Button btnSub;
		TextView tv_comment;
		TextView tvQuantity;
	}

	private LayoutInflater inflater;

	@SuppressWarnings("deprecation")
	public AdapterSelectedDish(Activity context, Cursor c) {
		super(context, -1, c, new String[] {}, new int[] {});
		inflater = LayoutInflater.from(context);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ViewHolder viewHolder;
		viewGroup = parent;
		if (convertView == null || convertView.getTag() == null) {
			viewHolder = new ViewHolder();

			convertView = inflater.inflate(
					R.layout.item_orderdish_selected_dishes, null);

			viewHolder.tvDishName = (TextView) convertView
					.findViewById(R.id.tv_dish_name);
			viewHolder.tvDishPrice = (TextView) convertView
					.findViewById(R.id.tv_dish_price);
			viewHolder.btnPlus = (Button) convertView
					.findViewById(R.id.btn_dish_plus);
			viewHolder.btnSub = (Button) convertView
					.findViewById(R.id.btn_dish_sub);
			viewHolder.tvQuantity = (TextView) convertView
					.findViewById(R.id.tv_dish_quantity);
			viewHolder.tv_comment = (TextView) convertView
					.findViewById(R.id.tv_comment);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Cursor c = (Cursor) getItem(position);

		String comment = c.getString(c
				.getColumnIndex(Dishes.COLUMN_NAME_COMMENT));// hcs
		if (comment.isEmpty()||comment.length()==0) {
			viewHolder.tv_comment.setVisibility(View.GONE);
		} else {
			viewHolder.tv_comment.setVisibility(View.VISIBLE);
		}
		final String dishName = c.getString(c
				.getColumnIndex(Dishes.COLUMN_NAME_DISH_NAME));
		viewHolder.tvDishName.setText(dishName);
		int dishPrice = c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_PRICE));
		if (dishPrice == 0) {
			viewHolder.tvDishPrice.setText(R.string.prompts_price_unknown);
		} else {
			viewHolder.tvDishPrice.setText(context.getString(R.string.unit_rmb)
					+ StringUtil.getPriceString(dishPrice));
		}

		int dishQty = c.getInt(c
				.getColumnIndex(Dishes.COLUMN_NAME_DISH_QUANTITY));
		if (dishQty > 0) {
			viewHolder.tvQuantity.setVisibility(View.VISIBLE);
			viewHolder.btnSub.setVisibility(View.VISIBLE);
		} else {
			viewHolder.btnSub.setVisibility(View.INVISIBLE);
			viewHolder.tvQuantity.setVisibility(View.INVISIBLE);
		}
		viewHolder.tvQuantity.setText("" + dishQty);
		final long dishId = c.getLong(c
				.getColumnIndex(Dishes.COLUMN_NAME_DISH_ID));
		// hcs2015-1-16
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ActOrderdish.mActOrderDish.popupWaitorNote1(dishId,dishName);
			}
		});
		
		viewHolder.btnPlus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateQuantity(dishId, true);
			}
		});
		viewHolder.btnSub.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateQuantity(dishId, false);
			}
		});
		return convertView;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	private void updateQuantity(long id, boolean plus) {
		DishesDao.getInstance().updateQuantity(mContext, id, plus);
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	public void setComment(String comment, long id) {
		if (comment.isEmpty()||comment.length()<1) {
			viewHolder.tv_comment.setVisibility(View.GONE);
			DishesDao.getInstance().updateDishComment(mContext, id, "");
		} else {			
			DishesDao.getInstance().updateDishComment(mContext, id, comment);
			viewHolder.tv_comment.setVisibility(View.VISIBLE);
		}
	}

}
