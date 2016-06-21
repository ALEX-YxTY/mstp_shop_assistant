package com.meishipintu.assistant.orderdish;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.milai.model.Dishes;

public class AdapterTypes extends SimpleCursorAdapter {
    private static final int ITEM_TYPE_RIGHT = 0;

    private Activity context;

    public static class ViewHolder {
        TextView tvTypeName;
        TextView Select;
    }

    private LayoutInflater inflater;

    @SuppressWarnings("deprecation")
    public AdapterTypes(Activity context, Cursor c) {
        super(context, -1, c, new String[] {}, new int[] {});
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
//        int type = getItemViewType(position);
        if (convertView == null || convertView.getTag() == null) {
            viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_dishlist_type, null);
                viewHolder.tvTypeName = (TextView) convertView.findViewById(R.id.tv_dishtype);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Cursor c = (Cursor) getItem(position);
        String typeName = c.getString(c.getColumnIndex(Dishes.COLUMN_NAME_TYPE_NAME));
        viewHolder.tvTypeName.setText(typeName);
        
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
    	
        return 0;
    }



    @Override
    public int getViewTypeCount() {
        return 1;
    }

}
