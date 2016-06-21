package com.meishipintu.assistant.adapter;

import java.util.Calendar;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.milai.model.Payment;
import com.milai.utils.TimeUtil;

public class AdapterPayment extends SimpleCursorAdapter {

	private Activity context;

	public static class ViewHolder {
		TextView tvTime;
		TextView tvDate;
		TextView tvTel;
//		TextView tvOutTradeNo;
//		TextView tvTradeNo;
//		TextView tvPaymentId;
//		TextView userId;
//		TextView tvSubject;
		TextView tvStatus;
		TextView tvPaytype;
		TextView tvPayPath;
		TextView tvPayPathImg;
		TextView tvNick;
		TextView tvPrice;
	}

	private LayoutInflater inflater;
	private int selectedPosition=-1;

	@SuppressWarnings("deprecation")
	public AdapterPayment(Activity context, Cursor c) {
		super(context, -1, c, new String[] {}, new int[] {});
		inflater = LayoutInflater.from(context);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null || convertView.getTag() == null) {
			viewHolder = new ViewHolder();

			convertView = inflater.inflate(R.layout.item_payment, null);
			viewHolder.tvTime = (TextView) convertView
					.findViewById(R.id.tv_time);
			viewHolder.tvTel = (TextView) convertView.findViewById(R.id.tv_tel);
			viewHolder.tvNick = (TextView) convertView.findViewById(R.id.tv_nick);
			TextPaint tv_paint = viewHolder.tvNick.getPaint();
			tv_paint.setFakeBoldText(true);
			viewHolder.tvStatus=(TextView) convertView.findViewById(R.id.tv_status);
//			viewHolder.tvSubject = (TextView) convertView.findViewById(R.id.tv_subject);
			viewHolder.tvPrice = (TextView) convertView.findViewById(R.id.tv_total_fee);
			viewHolder.tvPaytype = (TextView) convertView.findViewById(R.id.tv_paytype);
			viewHolder.tvPayPath= (TextView) convertView.findViewById(R.id.tv_paypath);
			tv_paint=viewHolder.tvPayPath.getPaint();
			tv_paint.setFakeBoldText(true);
			viewHolder.tvPayPathImg =(TextView) convertView.findViewById(R.id.tv_paypath_img);
//			viewHolder.tvTradeNo=(TextView) convertView.findViewById(R.id.tv_tradeno);
//			viewHolder.tvOutTradeNo=(TextView) convertView.findViewById(R.id.tv_outtradeno);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Cursor c = (Cursor) getItem(position);
		long curMili = c.getLong(c
				.getColumnIndex(Payment.COLUMN_NAME_CREATE_TIME));
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(curMili);
		String nick = c.getString(c.getColumnIndex(Payment.COLUMN_NAME_USER_NAME));
		String tel = c.getString(c.getColumnIndex(Payment.COLUMN_NAME_USER_TEL));
		String price = c.getString(c.getColumnIndex(Payment.COLUMN_NAME_TOTAL_PRICE));
//		String subject=c.getString(c.getColumnIndex(Payment.COLUMN_NAME_SUBJECT));
		int paymentType = c.getInt(c.getColumnIndex(Payment.COLUMN_NAME_TYPE));
		int status = c.getInt(c.getColumnIndex(Payment.COLUMN_NAME_STATUS));
//		String tradeNo=c.getString(c.getColumnIndex(Payment.COLUMN_NAME_TRADE_NO));//支付单号
//		String outTradeNo=c.getString(c.getColumnIndex(Payment.COLUMN_NAME_OUT_TRADE_NO));//订单号

		String timestr = TimeUtil.convertLongToFormatString(curMili,
				"yyyy-MM-dd HH:mm:ss");
		viewHolder.tvTime.setText(timestr);
		// viewHolder.tvDate.setText(TimeUtil.convertLongToFormatString(curMili,
		// "dd"));
		if(status==1)
		{
			viewHolder.tvStatus.setText("已收款");
			viewHolder.tvStatus.setBackgroundResource(R.drawable.bg_button_yellow_nor);
		}else if(status==3)
		{
			viewHolder.tvStatus.setText("交易关闭");
			viewHolder.tvStatus.setBackgroundResource(R.drawable.bg_button_gray);
		}else if(status==2){
			viewHolder.tvStatus.setText("已退款");
			viewHolder.tvStatus.setBackgroundResource(R.drawable.bg_button_gray);
		}
		if(tel.equals("null"))
		{
			tel="";
		}
		viewHolder.tvTel.setText(context.getString(R.string.tel_prefix) + tel);
		if(nick.equals("null"))
		{
			nick="";
		}
		viewHolder.tvNick.setText(nick);
//		viewHolder.tvSubject.setText(subject);
		viewHolder.tvPrice.setText("支付金额" + "￥" + price);

//		if(!tradeNo.equals("null"))
//		{
//			tradeNo=tradeNo.replace("T", "");
		String shopName=Cookies.getShopName();
			if(paymentType==1)
			{
				viewHolder.tvPaytype.setText("用户支付");
				viewHolder.tvPayPath.setText("（支付宝）");
				viewHolder.tvPayPathImg.setVisibility(View.VISIBLE);
				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.ali_nor_l);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.alipay_color));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.alipay_color));
			}else if(paymentType==2)
			{
				viewHolder.tvPaytype.setText("用户支付");
				viewHolder.tvPayPath.setText("（微信）");
				viewHolder.tvPayPathImg.setVisibility(View.VISIBLE);
				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.weixin_nor_l);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.weixin_color));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.weixin_color));
			}else if(paymentType==3)
			{
				viewHolder.tvPaytype.setText("用户支付");
				viewHolder.tvPayPath.setText("（银联）");
				viewHolder.tvPayPathImg.setVisibility(View.VISIBLE);
				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.icon_pay_unionpay);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.upay_color));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.upay_color));
			}else if(paymentType==4)
			{
				viewHolder.tvNick.setText(shopName);
				viewHolder.tvPaytype.setText("");
				viewHolder.tvPayPath.setText("现金支付");
				viewHolder.tvPayPathImg.setVisibility(View.VISIBLE);
				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.cash_nor_l);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.cash_color));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.cash_color));
			}else if(paymentType==5)
			{
				viewHolder.tvNick.setText(shopName);
				viewHolder.tvPaytype.setText("");
				viewHolder.tvPayPath.setText("刷卡支付");
				viewHolder.tvPayPathImg.setVisibility(View.VISIBLE);
				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.pos_nor_l);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.pos_color));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.pos_color));
			}else if(paymentType==11)
			{
				viewHolder.tvNick.setText(shopName);
				viewHolder.tvPaytype.setText("");
				viewHolder.tvPayPath.setText("银联刷卡");
				viewHolder.tvPayPathImg.setVisibility(View.VISIBLE);
				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.union_paypos);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.pos_color));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.pos_color));
			}else if(paymentType==6||paymentType==7||paymentType==8)
			{
				viewHolder.tvNick.setText(shopName);
				viewHolder.tvPaytype.setText("");
				viewHolder.tvPayPath.setText("支付宝当面付");
				viewHolder.tvPayPathImg.setVisibility(View.VISIBLE);
				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.ali_nor_l);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.faceToface_color));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.faceToface_color));
			}
			else if(paymentType==15)
			{
				viewHolder.tvNick.setText(shopName);
				viewHolder.tvPaytype.setText("");
				viewHolder.tvPayPath.setText("支付宝个人转账");
				viewHolder.tvPayPathImg.setVisibility(View.VISIBLE);
				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.ali_nor_l);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.faceToface_color));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.faceToface_color));
			}
			else if(paymentType==9)
			{
				viewHolder.tvNick.setText(shopName);
				viewHolder.tvPaytype.setText("");
				viewHolder.tvPayPath.setText("微信扫码");
				viewHolder.tvPayPathImg.setVisibility(View.VISIBLE);
				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.weixin_nor_l);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.weixin_color));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.weixin_color));
			}else if(paymentType==12)
			{
				viewHolder.tvNick.setText(shopName);
				viewHolder.tvPaytype.setText("");
				viewHolder.tvPayPath.setText("微信刷卡");
				viewHolder.tvPayPathImg.setVisibility(View.VISIBLE);
				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.weixin_nor_l);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.weixin_color));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.weixin_color));
			}else if(paymentType==13)
			{
				viewHolder.tvNick.setText(shopName);
				viewHolder.tvPaytype.setText("用户支付");
				viewHolder.tvPayPath.setText("（百度钱包）");
				viewHolder.tvPayPathImg.setVisibility(View.VISIBLE);
				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.baidu_pay_nor);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.red));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.red));
			}else if(paymentType==14)
			{
				viewHolder.tvNick.setText(shopName);
				viewHolder.tvPaytype.setText("");
				viewHolder.tvPayPath.setText("其他支付");
//				viewHolder.tvPayPathImg.setBackgroundResource(R.drawable.cash_nor_l);
				viewHolder.tvPayPathImg.setVisibility(View.GONE);
				viewHolder.tvPaytype.setTextColor(context.getResources().getColor(R.color.yellow));
				viewHolder.tvPayPath.setTextColor(context.getResources().getColor(R.color.yellow));
			}
			
//		viewHolder.tvTradeNo.setText("支付单号："+tradeNo);
//		viewHolder.tvOutTradeNo.setText("订单号："+outTradeNo);
		// viewHolder.tvPrice.setText("金额：" + "￥"
		// + String.format("%.2f", (float) price / 100) + "元");
		
			Log.d("position== selectedPosition", position+":"+selectedPosition);
			if ((position+1)== selectedPosition) {
				convertView.setBackgroundResource(R.color.light_light_gray);
	        }   
	        else {
	        	convertView.setBackgroundResource(R.color.white);        	
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

	 public void setSelectedPosition(int position) {  
         selectedPosition = position;  
     }  
}
