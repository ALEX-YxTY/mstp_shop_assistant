package com.meishipintu.core.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.meishipintu.assistant.R;

public abstract class MyDialogManyChoiceUtil extends MyDialogUtil {
    private Context context;

    public MyDialogManyChoiceUtil(Context context) {
        super(context);
        this.context = context;
    }

    public abstract void onChooseOne();

    public abstract void onChooseTwo();

    public abstract void onChooseThree();

    public abstract void onChooseFour();

    public void showCustomThreeChioce(String btnString1, String btnString2, String btnString3) {
        final Dialog lDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.mydialog_3_choice);
        ((Button) lDialog.findViewById(R.id.btn1)).setText(btnString1);
        ((Button) lDialog.findViewById(R.id.btn1)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onChooseOne();
                lDialog.dismiss();
            }
        });
        ((Button) lDialog.findViewById(R.id.btn2)).setText(btnString2);
        ((Button) lDialog.findViewById(R.id.btn2)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onChooseTwo();
                lDialog.dismiss();
            }
        });
        ((Button) lDialog.findViewById(R.id.btn3)).setText(btnString3);
        ((Button) lDialog.findViewById(R.id.btn3)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onChooseThree();
                lDialog.dismiss();
            }
        });
        lDialog.show();

    }

    public void showCustomManyChioce(
            String btnString1,
            String btnString2,
            String btnString3,
            String btnString4,
            String cancel) {
        final Dialog lDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.mydialog_many_choice);
        ((Button) lDialog.findViewById(R.id.btn1)).setText(btnString1);
        ((Button) lDialog.findViewById(R.id.btn1)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onChooseOne();
                lDialog.dismiss();
            }
        });
        ((Button) lDialog.findViewById(R.id.btn2)).setText(btnString2);
        ((Button) lDialog.findViewById(R.id.btn2)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onChooseTwo();
                lDialog.dismiss();
            }
        });
        ((Button) lDialog.findViewById(R.id.btn3)).setText(btnString3);
        ((Button) lDialog.findViewById(R.id.btn3)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onChooseThree();
                lDialog.dismiss();
            }
        });
        ((Button) lDialog.findViewById(R.id.btn4)).setText(btnString4);
        ((Button) lDialog.findViewById(R.id.btn4)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onChooseFour();
                lDialog.dismiss();
            }
        });
        ((Button) lDialog.findViewById(R.id.cancel)).setText(cancel);
        ((Button) lDialog.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK

                lDialog.dismiss();
            }
        });
        lDialog.show();

    }
}
