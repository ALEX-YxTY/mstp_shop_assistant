package com.meishipintu.assistant.ui.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.core.utils.ConstUtil;
import com.umeng.analytics.MobclickAgent;
import com.milai.utils.QrUtil;

public class ActPayQRCode extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_ticket_qr);

		Intent in = getIntent();
		long ticketId = in.getLongExtra(ConstUtil.INTENT_EXTRA_NAME.TICKET_ID, 0);
		int payMoney = in.getIntExtra(ConstUtil.INTENT_EXTRA_NAME.MONEY_AMOUNT, 0);

		String rcvShop = Cookies.getShopName();
		long shopId = Cookies.getShopId();

		((Button) findViewById(R.id.btn_back)).setOnClickListener(mClkListener);
		((TextView) findViewById(R.id.tv_rtn)).setOnClickListener(mClkListener);

		String text = getString(R.string.pay_prefix) + ticketId + ":" + shopId + ":" + rcvShop + ":" + payMoney;
		QrUtil.createQRCodeImage(text, (ImageView) this.findViewById(R.id.iv_qr_image));
	}

	private OnClickListener mClkListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				finishAndAni();
				break;
			case R.id.tv_rtn:
				setResult(RESULT_OK);
				finishAndAni();
				break;
			}
		}
	};

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		setResult(RESULT_OK);
		finishAndAni();
	}

	protected void finishAndAni() {
		finish();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}

}
