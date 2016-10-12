package com.meishipintu.assistant.ui.pay;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.zxing.client.android.QrUtil;
import com.meishipintu.assistant.R;

public class ActAliPay extends PopupWindow {

    private FragmentActivity mParentActivity = null;
    private View mMainView;
    private ProgressDialog mLoadingDialog = null;
    private int m_mode;

    public ActAliPay() {
    }

    public ActAliPay(FragmentActivity context, String qr_code, int mode) {
        super(context);
        mParentActivity = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMainView = inflater.inflate(R.layout.layout_alipay, null);
        setContentView(mMainView);
        TextView tv = (TextView) mMainView.findViewById(R.id.tv_title);
        TextView tv_toast = (TextView) mMainView.findViewById(R.id.tv_tag);
        m_mode = mode;
        if (mode == 1) {
            tv.setText("支付宝扫码支付");
            tv_toast.setText("请用    支付宝客户端“扫码功能”\n扫描下方二维码");
        } else if (mode == 2) {
            tv.setText("微信扫码支付");
            tv_toast.setText("请用   微信客户端“扫一扫功能”\n扫描下方二维码");
        } else if (mode == 3) {
            tv.setText("支付宝转账支付");
            tv_toast.setText("请用    支付宝客户端“个人扫码功能”\n扫描下方二维码");
        }
        Button bt = (Button) mMainView.findViewById(R.id.btn_back);
        bt.setOnClickListener(ll);
        Button bt_finish = (Button) mMainView.findViewById(R.id.bt_finish);
        bt_finish.setOnClickListener(ll);
        if (qr_code != null && !qr_code.isEmpty()) {
            QrUtil.createQRCodeImage(qr_code, (ImageView) mMainView.findViewById(R.id.iv_alipay));
        }
        initShow();
    }

    private OnClickListener ll = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.btn_back:
                    dismiss();
                    break;
                case R.id.bt_finish:
                    dismiss();

                    if (m_mode == 3) {
                        ActNewPayment.mActNewPayment.ShowPayMessage("请确定支付宝是否收到正确金额", "已收到", "未收到", 15);
                    } else {
                        Log.d("zzzzz", "xzxzxzxzxz");
                        ActNewPayment.mActNewPayment.printOrder();
                        ActNewPayment.mActNewPayment.clearMoney();
                        ActNewPayment.mActNewPayment.finish();
                    }
            }
        }
    };

    void initShow() {
        this.setWidth(LayoutParams.FILL_PARENT); // 设置弹出窗体的宽
        this.setHeight(LayoutParams.FILL_PARENT); // 设置弹出窗体的高
        this.setFocusable(true); // 设置弹出窗体可点击
        ColorDrawable dw = new ColorDrawable(0xffffffff); // 实例化一个ColorDrawable颜色为半透明
        this.setBackgroundDrawable(dw); // 设置弹出窗体的背景
    }

}
