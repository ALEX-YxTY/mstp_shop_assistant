package com.meishipintu.assistant.orderdish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.R.color;

public class AdapterComment extends BaseAdapter{

		private LayoutInflater mInflater;
		private Context mContext=null;
		private ViewGroup vgp=null;
		
		private boolean[] checkStatus=null;
		
		private List<HashMap<Integer, String>> mCommentList = new ArrayList<HashMap<Integer,String>>();
		public static class ViewHolder {
			TextView tvComment;
			CheckBox cbCheck;
		}
		public AdapterComment(Context context,List<HashMap<Integer, String>> list,boolean[] status)
		{ 
			this.mInflater = LayoutInflater.from(context);
			this.mCommentList=list;
			this.mContext=context;
			this.checkStatus=status;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if(convertView == null || convertView.getTag() == null)
			{
				holder=new ViewHolder();
				convertView = mInflater.inflate(R.layout.item_comment, null);
				holder.tvComment=(TextView)convertView.findViewById(R.id.tv_comment);
				holder.cbCheck=(CheckBox)convertView.findViewById(R.id.check_comment);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			Map<Integer, String> map = new HashMap<Integer, String>();
			map=mCommentList.get(position);
			holder.tvComment.setText(map.get(position));
			holder.cbCheck.setTag(Integer.toString(position)+"check");//
//			if(checkStatus[position])
//			{
//				holder.cbCheck.setChecked(true);
//				holder.cbCheck.setTag(position);
//			}else
//			{
//				holder.cbCheck.setChecked(false);
//			}
			final int positioncheck=position;
			holder.cbCheck.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (((CheckBox)v).isChecked()){
						checkStatus[positioncheck]=true;
						((CheckBox)v).setChecked(true);//
						ActPopupComment.actPC.setData(positioncheck,true);
					} else {
						checkStatus[positioncheck]=false;
						((CheckBox)v).setChecked(false);//
						ActPopupComment.actPC.setData(positioncheck, false);
					}
				}});
			
			if(checkStatus[position])
			{
				holder.cbCheck.setChecked(true);
				holder.tvComment.setTextColor(color.order_dish);
				convertView.setSelected(true);
			}else
			{
				holder.cbCheck.setChecked(false);
				holder.tvComment.setTextColor(color.mid_gray);
				convertView.setSelected(false);
			}
			return convertView;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mCommentList.size();
		}

		private void setDataToPop(){
			
		}
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

	}
