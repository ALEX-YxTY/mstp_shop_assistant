package com.meishipintu.assistant.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.bean.MiActive;
import com.meishipintu.assistant.interfaces.OnUseCouponOrMi;
import com.meishipintu.core.utils.DateUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/9/7.
 */
public class AdapterMiActive extends BaseAdapter {

    private Context context;
    private ArrayList<MiActive> list;
    private int totalMi;

    public AdapterMiActive(Context context, ArrayList<MiActive> dataList,int totalMi) {
        this.context = context;
        this.list = dataList;
        this.totalMi = totalMi;
    }

    public void setTotalMi(int mi) {
        this.totalMi = mi;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MiActive active = list.get(position);
        ViewHolder viewHolder;
        if (convertView == null|| convertView.getTag() == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_mi_active_layout,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.tvMiValue = (TextView) convertView.findViewById(R.id.tv_coupon_value);
            viewHolder.tvString = (TextView) convertView.findViewById(R.id.tv_active_name);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_end_time);
            viewHolder.ll = (LinearLayout) convertView.findViewById(R.id.ll_active);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (active.getRice() > totalMi) {
            viewHolder.ll.setBackgroundResource(R.drawable.btn_mi_unenable);
        } else {
            viewHolder.ll.setBackgroundResource(R.drawable.btn_mi_enable);
        }
        viewHolder.tvMiValue.setText(active.getRice() + "");
        viewHolder.tvString.setText(active.getTitle());
        viewHolder.tvTime.setText(DateUtils.getTimePeriod(active.getStart_time(),active.getEnd_time())+"可用");
        return convertView;
    }

    static class ViewHolder {
        TextView tvMiValue;
        TextView tvString;
        TextView tvTime;
        LinearLayout ll;
    }
}
