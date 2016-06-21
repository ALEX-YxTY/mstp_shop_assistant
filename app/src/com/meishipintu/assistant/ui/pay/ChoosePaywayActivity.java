package com.meishipintu.assistant.ui.pay;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;

public class ChoosePaywayActivity extends Activity implements OnClickListener,OnCheckedChangeListener{
	private Button bt_return;
	private LinearLayout ll_all_payway,ll_alipay,ll_wechat,ll_cup
	,ll_payment_card,ll_cash_payment,ll_baidu_wallet,ll_cards;
	private CheckBox cb_all_payway,cb_alipay,cb_wechat,cb_cup,cb_payment_card
	,cb_cash_payment,cb_baidu_wallet,cb_cards;
	private TextView tv_right;
	private String payway;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_payway);
		ll_all_payway=(LinearLayout) findViewById(R.id.ll_all_payway);
		ll_alipay=(LinearLayout) findViewById(R.id.ll_alipay);
		ll_wechat=(LinearLayout) findViewById(R.id.ll_wechat);
		ll_cup=(LinearLayout) findViewById(R.id.ll_cup);
		ll_payment_card=(LinearLayout) findViewById(R.id.ll_payment_card);
		ll_cash_payment=(LinearLayout) findViewById(R.id.ll_cash_payment);
		ll_baidu_wallet=(LinearLayout) findViewById(R.id.ll_baidu_wallet);
		ll_cards= (LinearLayout) findViewById(R.id.ll_cards);
		cb_all_payway=(CheckBox) findViewById(R.id.cb_all_payway);
		cb_alipay=(CheckBox) findViewById(R.id.cb_alipay);
		cb_wechat=(CheckBox) findViewById(R.id.cb_wechat);
		cb_cup=(CheckBox) findViewById(R.id.cb_cup);
		cb_payment_card=(CheckBox) findViewById(R.id.cb_payment_card);
		cb_cash_payment=(CheckBox) findViewById(R.id.cb_cash_payment);
		cb_baidu_wallet=(CheckBox) findViewById(R.id.cb_baidu_wallet);
		cb_cards= (CheckBox) findViewById(R.id.cb_cards);
		cb_all_payway.setOnClickListener(this);
		cb_alipay.setOnClickListener(this);
		cb_wechat.setOnClickListener(this);
		cb_cup.setOnClickListener(this);
		cb_payment_card.setOnClickListener(this);
		cb_cash_payment.setOnClickListener(this);
		cb_baidu_wallet.setOnClickListener(this);
		cb_cards.setOnClickListener(this);
		ll_all_payway.setOnClickListener(this);
		ll_alipay.setOnClickListener(this);
		ll_wechat.setOnClickListener(this);
		ll_cup.setOnClickListener(this);
		ll_payment_card.setOnClickListener(this);
		ll_cash_payment.setOnClickListener(this);
		ll_baidu_wallet.setOnClickListener(this);
		ll_cards.setOnClickListener(this);
		bt_return=(Button) findViewById(R.id.bt_return);
		bt_return.setOnClickListener(this);
		tv_right = (TextView) findViewById(R.id.tv_right);
		tv_right.setText("确定");
		tv_right.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.bt_return:
			finish();
			break;
		case R.id.ll_all_payway:
			if(cb_all_payway.isChecked()){
				cb_all_payway.setChecked(false);
				cb_alipay.setChecked(false);
				cb_wechat.setChecked(false);
				cb_cup.setChecked(false);
				cb_payment_card.setChecked(false);
				cb_cash_payment.setChecked(false);
				cb_baidu_wallet.setChecked(false);
				cb_cards.setChecked(false);
			}else{
				cb_all_payway.setChecked(true);
				cb_alipay.setChecked(true);
				cb_wechat.setChecked(true);
				cb_cup.setChecked(true);
				cb_payment_card.setChecked(true);
				cb_cash_payment.setChecked(true);
				cb_baidu_wallet.setChecked(true);
				cb_cards.setChecked(true);
			}
			break;
		case R.id.ll_alipay:
			cb_alipay.setChecked(!cb_alipay.isChecked());

			
			if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_cup.isChecked()&&cb_payment_card.isChecked
					()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
				cb_all_payway.setChecked(true);
			}else{
				cb_all_payway.setChecked(false);
			}
			break;
		case R.id.ll_wechat:
			cb_wechat.setChecked(!cb_wechat.isChecked());
			if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_cup.isChecked()&&cb_payment_card.isChecked
					()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
				cb_all_payway.setChecked(true);
			}else{
				cb_all_payway.setChecked(false);
			}

			break;
		case R.id.ll_cup:
			cb_cup.setChecked(!cb_cup.isChecked());
			if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_cup.isChecked()&&cb_payment_card.isChecked
					()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
				cb_all_payway.setChecked(true);
			}else{
				cb_all_payway.setChecked(false);
			}

			break;
		case R.id.ll_payment_card:
			cb_payment_card.setChecked(!cb_payment_card.isChecked());
			
			if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_cup.isChecked()&&cb_payment_card.isChecked
					()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
				cb_all_payway.setChecked(true);
			}else {
				cb_all_payway.setChecked(false);
			}

			break;
		case R.id.ll_cash_payment:
			cb_cash_payment.setChecked(!cb_cash_payment.isChecked());
			if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_cup.isChecked()&&cb_payment_card.isChecked
					()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
				cb_all_payway.setChecked(true);
			}else{
				cb_all_payway.setChecked(false);
			}

			break;
		case R.id.ll_baidu_wallet:
			cb_baidu_wallet.setChecked(!cb_baidu_wallet.isChecked());
			if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_cup.isChecked()&&cb_payment_card.isChecked
					()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
				cb_all_payway.setChecked(true);
			}else{
				cb_all_payway.setChecked(false);
			}
			break;
		case  R.id.ll_cards:
			cb_cards.setChecked(!cb_baidu_wallet.isChecked());
			if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_cup.isChecked()&&cb_payment_card.isChecked
					()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
				cb_all_payway.setChecked(true);
			}else{
				cb_all_payway.setChecked(false);
			}
			break;


		case R.id.cb_all_payway:
			if(cb_all_payway.isChecked()){
				cb_all_payway.setChecked(true);
				cb_alipay.setChecked(true);
				cb_wechat.setChecked(true);
				cb_cup.setChecked(true);
				cb_payment_card.setChecked(true);
				cb_cash_payment.setChecked(true);
				cb_baidu_wallet.setChecked(true);
				cb_cards.setChecked(true);
			}else{
				cb_all_payway.setChecked(false);
				cb_alipay.setChecked(false);
				cb_wechat.setChecked(false);
				cb_cup.setChecked(false);
				cb_payment_card.setChecked(false);
				cb_cash_payment.setChecked(false);
				cb_baidu_wallet.setChecked(false);
				cb_cards.setChecked(false);
			}
			break;
		case R.id.cb_alipay:
			if(cb_alipay.isChecked()){
				if(cb_wechat.isChecked()&&cb_cup.isChecked()&&cb_payment_card.isChecked
						()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
					cb_all_payway.setChecked(true);
				}else{
					cb_all_payway.setChecked(false);
				}

			}else{
				cb_all_payway.setChecked(false);
			}
			break;
			
		case R.id.cb_wechat:
			if(cb_wechat.isChecked()){
				if(cb_alipay.isChecked()&&cb_cup.isChecked()&&cb_payment_card.isChecked
						()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
					cb_all_payway.setChecked(true);
				}else{
					cb_all_payway.setChecked(false);
				}

			}else{
				cb_all_payway.setChecked(false);
			}
			break;
		case R.id.cb_cup:
			if(cb_cup.isChecked()){
				if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_payment_card.isChecked
						()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
					cb_all_payway.setChecked(true);
				}else{
					cb_all_payway.setChecked(false);
				}
			}else{
				cb_all_payway.setChecked(false);
			}
			break;
		case R.id.cb_payment_card:
			if(cb_payment_card.isChecked()){
				if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_cup.isChecked
						()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
					cb_all_payway.setChecked(true);
				}else{
					cb_all_payway.setChecked(false);
				}
			}else{
				cb_all_payway.setChecked(false);
			}
			break;
		case R.id.cb_cash_payment:
			if(cb_cash_payment.isChecked()){
				if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_cup.isChecked
						()&&cb_payment_card.isChecked()&&cb_baidu_wallet.isChecked()&&cb_cards.isChecked()){
					cb_all_payway.setChecked(true);
				}else{
					cb_all_payway.setChecked(false);
				}
			}else{
				cb_all_payway.setChecked(false);
			}
			break;
		case R.id.cb_baidu_wallet:
			if(cb_baidu_wallet.isChecked()){
				if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_cup.isChecked
						()&&cb_payment_card.isChecked()&&cb_cash_payment.isChecked()&&cb_cards.isChecked()){
					cb_all_payway.setChecked(true);
				}else{
					cb_all_payway.setChecked(false);
				}
			}else{
				cb_all_payway.setChecked(false);
			}
			break;
			case R.id.cb_cards:
				if(cb_cards.isChecked()){
					if(cb_alipay.isChecked()&&cb_wechat.isChecked()&&cb_cup.isChecked
							()&&cb_payment_card.isChecked()&&cb_cash_payment.isChecked()&&cb_baidu_wallet.isChecked()){
						cb_all_payway.setChecked(true);
					}else{
						cb_all_payway.setChecked(false);
					}
				}else{
					cb_all_payway.setChecked(false);
				}
				break;
			case R.id.tv_right:
			StringBuilder py=new StringBuilder();
			if(cb_all_payway.isChecked()){
				payway="1,2,3,4,5,6,7,8,9,11,12,13";
				Cookies.savePayway(payway);
			}else {
				if(cb_alipay.isChecked()){
					py.append("1,6,7,8,");
				}else if(cb_wechat.isChecked()){
					py.append("2,9,12,");
				}else if(cb_cup.isChecked()){
					py.append("3,11,");
				}else if(cb_payment_card.isChecked()){
					py.append("10,");
				}else if(cb_cash_payment.isChecked()){
					py.append("4,");
				}else if(cb_baidu_wallet.isChecked()){
					py.append("13,");
				}else if(cb_cards.isChecked()){
					py.append("5,");
				}
				Cookies.savePayway(py.toString());
			}
			Toast.makeText(ChoosePaywayActivity.this,"支付通道已经设置", Toast.LENGTH_SHORT).show();
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


		
	}
}
