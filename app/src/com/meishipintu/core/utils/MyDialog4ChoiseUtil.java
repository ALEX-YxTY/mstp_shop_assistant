package com.meishipintu.core.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.meishipintu.assistant.R;

public abstract class MyDialog4ChoiseUtil {
    /**
     * it will show the OK/CANCEL dialog like iphone, make sure no keyboard is visible
     * 
     * @param pTitle title for dialog
     * @param pMsg msg for body
     */
    private Context context;

    // 此处设置context是为了在局部环境创建时dialog仍然可以执行
    public MyDialog4ChoiseUtil(Context context) {
        this.context = context;
    }

    public abstract void onClickPositive();

    public abstract void onClickNagative();

    public abstract void onClickBtn3();

    public abstract void onClickCancel();

    public void showCustom4Choice(String pTitle, String pMsg, String btn1, String btn2, String btn3, String cancel) {
        final Dialog lDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.mydialog_choice_4_choise);
        ((TextView) lDialog.findViewById(R.id.dialog_title)).setText(pTitle);
        ((TextView) lDialog.findViewById(R.id.dialog_message)).setText(pMsg);
        ((Button) lDialog.findViewById(R.id.btn1)).setText(btn1);
        ((Button) lDialog.findViewById(R.id.btn1)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onClickPositive();
                lDialog.dismiss();
            }
        });
        ((Button) lDialog.findViewById(R.id.btn2)).setText(btn2);
        ((Button) lDialog.findViewById(R.id.btn2)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onClickNagative();
                lDialog.dismiss();
            }
        });
        ((Button) lDialog.findViewById(R.id.btn3)).setText(btn3);
        ((Button) lDialog.findViewById(R.id.btn3)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onClickBtn3();
                lDialog.dismiss();
            }
        });
        ((Button) lDialog.findViewById(R.id.cancel)).setText(cancel);
        ((Button) lDialog.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCancel();
                lDialog.dismiss();
            }
        });
        lDialog.show();

    }

}
