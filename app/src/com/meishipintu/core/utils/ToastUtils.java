package com.meishipintu.core.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;

/**
 * Created by Administrator on 2016/9/8.
 */
public class ToastUtils {

    public static void selfToastShow(Context context, String msg) {
        Toast toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
        LayoutInflater infalter = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = infalter.inflate(R.layout.layout_toast, null);
        TextView tvContent = (TextView) v.findViewById(R.id.tv_toast_content);
        tvContent.setText(msg);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(v);
        toast.show();
    }
}
