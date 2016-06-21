package com.meishipintu.assistant.orderdish;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.milai.model.SubmittedTicket;
import com.milai.utils.TimeUtil;

public class AdapterSubmittedTicket extends SimpleCursorAdapter {

	private Activity context;
	private int mType = 0;

	public static class ViewHolder {
		TextView tvTime;
		TextView tvPaid;
		TextView tvDate;
		TextView tvTel;
		TextView tvNick;
		TextView tvTable;
		TextView tv_address;
	}
	private LayoutInflater inflater;

	@SuppressWarnings("deprecation")
	public AdapterSubmittedTicket(Activity context, Cursor c, int type) {
		super(context, -1, c, new String[] {}, new int[] {});
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.mType = type;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null || convertView.getTag() == null) {
			viewHolder = new ViewHolder();

			convertView = inflater.inflate(R.layout.item_submitted_ticket, null);
			viewHolder.tvTime = (TextView) convertView
					.findViewById(R.id.tv_time);
			viewHolder.tvDate = (TextView) convertView
					.findViewById(R.id.tv_date);
			viewHolder.tvPaid = (TextView) convertView.findViewById(R.id.tv_paid);
			viewHolder.tvTel = (TextView) convertView.findViewById(R.id.tv_tel);
			viewHolder.tvNick = (TextView) convertView
					.findViewById(R.id.tv_nick);
			viewHolder.tvTable = (TextView) convertView
					.findViewById(R.id.tv_table);
			viewHolder.tv_address = (TextView) convertView
					.findViewById(R.id.tv_address); 
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Cursor c = (Cursor) getItem(position);
		String nick = c.getString(c
				.getColumnIndex(SubmittedTicket.COLUMN_NAME_USER_NAME));
		String tel = c.getString(c
				.getColumnIndex(SubmittedTicket.COLUMN_NAME_USER_TEL));
		String table = c.getString(c
				.getColumnIndex(SubmittedTicket.COLUMN_NAME_TABLE_NAME));  //address for takeaway
		long paid = c.getLong(c
				.getColumnIndex(SubmittedTicket.COLUMN_NAME_PAY_TIME));
		long curMili = c.getLong(c
				.getColumnIndex(SubmittedTicket.COLUMN_NAME_CREATE_TIME));
		int status = c.getInt(c
				.getColumnIndex(SubmittedTicket.COLUMN_NAME_STATUS));

		if(mType==1) {
			viewHolder.tv_address.setVisibility(View.GONE);
			viewHolder.tvTable.setText(table);			
			String timestr = TimeUtil.convertLongToFormatString(curMili,"HH:mm");	
			viewHolder.tvTime.setText(context.getString(R.string.submitted_ticket_time_prefix)+timestr);
		}
		else {
			viewHolder.tvTable.setVisibility(View.GONE);
			viewHolder.tv_address.setText(table);				
			long arrival = c.getLong(c.getColumnIndex(SubmittedTicket.COLUMN_NAME_ARRIVAL_TIME));
			String timestr1 = TimeUtil.convertLongToFormatString(arrival*1000, "HH:mm");
			String timestr2 = TimeUtil.convertLongToFormatString(arrival*1000+15*60*1000, "HH:mm");			
			viewHolder.tvTime.setText("送达时间："+timestr1 + "~"+timestr2);		
		}
		viewHolder.tvDate.setText(TimeUtil.convertLongToFormatString(curMili,"dd"));
		viewHolder.tvTel.setText(context.getString(R.string.tel_prefix)+tel);
		viewHolder.tvNick.setText(nick);
		
		if(paid!=0 || status==4) {
			viewHolder.tvPaid.setTextColor(Color.GREEN);
			viewHolder.tvPaid.setText("已支付  ");
		}
		else {
			viewHolder.tvPaid.setTextColor(Color.RED);
			viewHolder.tvPaid.setText("未支付  ");	
		}
		
		return convertView;
	}

	@Override
	public int getItemViewType(int position) {
		return 1;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

}
