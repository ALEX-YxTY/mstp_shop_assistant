package com.meishipintu.assistant.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;

public class AdapterPayPath extends BaseAdapter{

	private Context mContext;
	private LayoutInflater inflater;
	private String[] pathArray;
	private String pathString;
	
	public AdapterPayPath(Context context)
	{
		this.mContext=context;
		pathString=Cookies.getShopType();	//支付方式以字串保存在SP中
		pathArray=pathString.split(",");
		inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		for (String path : pathArray) {
			Log.i("test", path);
		}
	}
	
	public static class ViewHolder{
		ImageView ivPayPath;
		ImageView ivPayPath_T;
		ImageView ivPayPath_B;
		TextView tvPayPath;
	}
	
	@Override
	public int getCount() {
		int count=0;
		if(pathArray.length>0){
			count=pathArray.length;
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {		
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if(convertView==null||convertView.getTag()==null){
			viewHolder=new ViewHolder();
			convertView=inflater.inflate(R.layout.item_button_layout_paypath, null);
			viewHolder.ivPayPath=(ImageView)convertView.findViewById(R.id.iv_pay_path);
			viewHolder.ivPayPath_T=(ImageView)convertView.findViewById(R.id.iv_pay_path_T);
			viewHolder.ivPayPath_B=(ImageView)convertView.findViewById(R.id.iv_pay_path_B);
			viewHolder.tvPayPath=(TextView)convertView.findViewById(R.id.tv_pay_path);
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)convertView.getTag();
		}
		String path=pathArray[position];

		int paytype = Cookies.getPayType(); //当前的默认支付方式
		
		if(path.equals("bfu")){
			if(paytype==5) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.pos));
			viewHolder.tvPayPath.setText("百富刷卡");
		}else if(path.equals("ldi")){
			if(paytype==5) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.pos));		
			viewHolder.tvPayPath.setText("联迪刷卡");
		}
		else if(path.equals("lkl")){
			if(paytype==5) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.pos));
			viewHolder.tvPayPath.setText("拉卡拉支付");
		}
		else if(path.equals("wpos")){
			if(paytype==5) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.pos));
			viewHolder.tvPayPath.setText("旺POS支付");
		}
		else if(path.equals("cas"))
		{
			if(paytype==4) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cash));
			viewHolder.tvPayPath.setText("现金支付");
		}else if(path.equals("mil"))
		{
			if(paytype==0) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.mi_pay));
			viewHolder.tvPayPath.setText("米来支付");
		}else if(path.equals("wx1"))
		{
			if(paytype==9) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}				
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_weixin_qr));
			viewHolder.tvPayPath.setText("微信码");
		}else if(path.equals("al1"))
		{
			if(paytype==6) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}		
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_payment_aliqr));
			viewHolder.tvPayPath.setText("支付宝码");
		}else if(path.equals("al2"))
		{
			if(paytype==7) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_payment_aliscan));
			viewHolder.tvPayPath.setText("支付宝扫码");
		}else if(path.equals("wx2"))
		{
			if(paytype==12) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_weixin_scan));
			viewHolder.tvPayPath.setText("微信扫码");
		}else if(path.equals("uni"))
		{
			if(paytype==11) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.union_paypos));
			viewHolder.tvPayPath.setText("银联刷卡");
		}else if(path.equals("icc"))
		{
			if(paytype==10) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_pay));
			viewHolder.tvPayPath.setText("IC刷卡");
		}else if(path.equals("oth"))
		{
			if(paytype==14) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_paypath_more));
			viewHolder.tvPayPath.setText("其他方式");
		}else if(path.equals("al3"))
		{
			if(paytype==15) {
				viewHolder.ivPayPath_T.setVisibility(View.VISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.VISIBLE);
			}
			else {
				viewHolder.ivPayPath_T.setVisibility(View.INVISIBLE);
				viewHolder.ivPayPath_B.setVisibility(View.INVISIBLE);
			}				
			viewHolder.ivPayPath.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_payment_aliqr));
			viewHolder.tvPayPath.setText("支付宝转账");
		}	
		return convertView;
	}


	public String getPayTypeString(int position)
	{
		String path=null;
		if(pathArray.length>0&&position<pathArray.length){
			path=pathArray[position];
		}
		return path;
	}
}
