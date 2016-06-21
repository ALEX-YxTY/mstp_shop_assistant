package com.meishipintu.assistant.adapter;

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
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.ui.pay.ActTeamList;
import com.meishipintu.core.utils.MyDialogUtil;


public class AdapterTeamList extends BaseAdapter{

	private LayoutInflater mInflater;
	private Context mContext=null;
	private List<Map<String, Object>> mTeamList = new ArrayList<Map<String,Object>>();
	public static class ViewHolder {
		TextView tvTeamNum;
		TextView tvPeopleNum;
		TextView tvTime;
		TextView tvCancle;
	}
	public AdapterTeamList(Context context,List<Map<String, Object>> list)
	{ 
		this.mInflater = LayoutInflater.from(context);
		this.mTeamList=list;
		this.mContext=context;
	}
	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if(convertView == null || convertView.getTag() == null)
		{
			holder=new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_team_list, null);
			holder.tvTeamNum=(TextView)convertView.findViewById(R.id.team_num);
			holder.tvPeopleNum=(TextView)convertView.findViewById(R.id.people_num);
			holder.tvTime=(TextView)convertView.findViewById(R.id.get_num_time);
			holder.tvCancle=(TextView)convertView.findViewById(R.id.bt_cancle);
			convertView.setTag(holder);
		}else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map=mTeamList.get(position);
		String teamNum=map.get("teamNum").toString();
		String peoNum=map.get("peopleNum").toString();
		String time=map.get("time").toString();
		//
		if(peoNum!=null&&!peoNum.equals(""))
		{
			if(Integer.parseInt(peoNum)<=4)
			{
				teamNum="A"+map.get("teamNum").toString();
			}else if(Integer.parseInt(peoNum)>4&&Integer.parseInt(peoNum)<=8)
			{
				teamNum="B"+map.get("teamNum").toString();
			}else if(Integer.parseInt(peoNum)>8)
			{
				teamNum="C"+map.get("teamNum").toString();
			}
			
		}
		
		final String showTeamNum=teamNum;
		final String showPeoNum=peoNum+"人";
		final String showTime=time;
		//
		holder.tvTeamNum.setText(showTeamNum);
		holder.tvPeopleNum.setText(showPeoNum);
		holder.tvTime.setText(showTime);
		
		if(teamNum.equals(""))
		{
			holder.tvTeamNum.setVisibility(View.GONE);
			holder.tvPeopleNum.setVisibility(View.GONE);
			holder.tvCancle.setVisibility(View.GONE);			
		}else
		{
			holder.tvTeamNum.setVisibility(View.VISIBLE);
			holder.tvPeopleNum.setVisibility(View.VISIBLE);
			holder.tvCancle.setVisibility(View.VISIBLE);
		}
		final String reduceString=map.get("teamNum").toString()+","+map.get("peopleNum").toString()+","+map.get("time").toString()+";";
		final String teamNumString=teamNum;
		
		holder.tvCancle.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//调用方法删除sharePreference的相关项
				MyDialogUtil qDialog = new MyDialogUtil(mContext) {
					@Override
					public void onClickPositive() {
						ActTeamList.mTeamList.reduceTeamListNumFromNet(reduceString,teamNumString);
					}
					@Override
					public void onClickNagative() {
						return;
					}
				};
				qDialog.showCustomMessage("是否销号？",
						"号码："+showTeamNum+"\n人数："+showPeoNum+"\n时间："+showTime,
						"确认销号",
						"取消销号");
				
			}		
		});
		return convertView;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mTeamList.size();
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
