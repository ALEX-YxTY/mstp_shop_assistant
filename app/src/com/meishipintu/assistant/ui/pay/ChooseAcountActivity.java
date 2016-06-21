package com.meishipintu.assistant.ui.pay;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;

public class ChooseAcountActivity extends Activity implements OnClickListener{
	private Button bt_return;
	private TextView tv_merchant_name;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chosse_acount);
		bt_return=(Button) findViewById(R.id.bt_return);
		bt_return.setOnClickListener(this);
		tv_merchant_name=(TextView) findViewById(R.id.tv_merchant_name);
		tv_merchant_name.setText(Cookies.getShopName());
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_return:
			finish();
			break;
		
		default:
			break;
		}
	}
}
