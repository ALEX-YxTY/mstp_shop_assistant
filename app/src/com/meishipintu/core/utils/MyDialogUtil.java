package com.meishipintu.core.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.meishipintu.assistant.R;

public abstract class MyDialogUtil {
    /**
     * it will show the OK/CANCEL dialog like iphone, make sure no keyboard is visible
     * 
     * @param pTitle title for dialog
     * @param pMsg msg for body
     */
    private Context context;

    // 此处设置context是为了在局部环境创建时dialog仍然可以执行
    public MyDialogUtil(Context context) {
        this.context = context;
    }

    public abstract void onClickPositive();

    public abstract void onClickNagative();

    public void showCustomMessage(String pTitle, String pMsg, String ok, String cancel) {
        final Dialog lDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.mydialog_okcanceldialogview);
        ((TextView) lDialog.findViewById(R.id.dialog_title)).setText(pTitle);
        ((TextView) lDialog.findViewById(R.id.dialog_message)).setText(pMsg);
        ((Button) lDialog.findViewById(R.id.ok)).setText(ok);
        ((Button) lDialog.findViewById(R.id.cancel)).setText(cancel);
        ((Button) lDialog.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks CANCEL
                onClickNagative();
                lDialog.dismiss();
            }
        });
        ((Button) lDialog.findViewById(R.id.ok)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onClickPositive();
                lDialog.dismiss();
            }
        });
        lDialog.show();

    }

    /**
     * it will show the OK dialog like iphone, make sure no keyboard is visible
     * 
     * @param pTitle title for dialog
     * @param pMsg msg for body
     */
    public void showCustomMessageOK(String pTitle, String pMsg, String ok) {
        final Dialog lDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.mydialog_okdialogview);
        ((TextView) lDialog.findViewById(R.id.dialog_title)).setText(pTitle);
        ((TextView) lDialog.findViewById(R.id.dialog_message)).setText(pMsg);
        ((Button) lDialog.findViewById(R.id.ok)).setText(ok);
        ((Button) lDialog.findViewById(R.id.ok)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onClickPositive();
                lDialog.dismiss();
            }
        });
        lDialog.show();

    }

    public void showCustomMessageOK(String pTitle, String pMsg, String ok, Dialog.OnKeyListener l) {
        final Dialog lDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.mydialog_okdialogview);
        ((TextView) lDialog.findViewById(R.id.dialog_title)).setText(pTitle);
        ((TextView) lDialog.findViewById(R.id.dialog_message)).setText(pMsg);
        ((Button) lDialog.findViewById(R.id.ok)).setText(ok);
        ((Button) lDialog.findViewById(R.id.ok)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onClickPositive();
                lDialog.dismiss();
            }
        });
        lDialog.setOnKeyListener(l);
        lDialog.show();

    }
    
    public void showCustomTwoChoice(String pMsg, String btn1, String btn2, String cancel) {
        final Dialog lDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.mydialog_choice);
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

    public void showCustom3Choice(String pTitle, String pMsg, String btn1, String btn2, String cancel) {
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
        ((Button) lDialog.findViewById(R.id.cancel)).setText(cancel);
        ((Button) lDialog.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                lDialog.dismiss();
            }
        });
        lDialog.show();

    }

    public void showCustomView(int layoutID, String pTitle, String pMsg, String ok, String cancel) {
        final Dialog lDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (layoutID > 0) {
            lDialog.setContentView(layoutID);
        } else {
            lDialog.setContentView(R.layout.mydialog_okdialogview);
        }
        if (pTitle != null) {
            ((TextView) lDialog.findViewById(R.id.dialog_title)).setText(pTitle);

        }
        if (pMsg != null) {
            ((TextView) lDialog.findViewById(R.id.dialog_message)).setText(pMsg);

        }
        ((Button) lDialog.findViewById(R.id.ok)).setText(ok);
        ((Button) lDialog.findViewById(R.id.cancel)).setText(cancel);
        ((Button) lDialog.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks CANCEL
                onClickNagative();
                lDialog.dismiss();
            }
        });
        ((Button) lDialog.findViewById(R.id.ok)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // write your code to do things after users clicks OK
                onClickPositive();
                lDialog.dismiss();
            }
        });
        lDialog.show();
    }

}
