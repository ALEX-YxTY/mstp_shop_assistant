package com.meishipintu.assistant.ui.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.bean.Payrepost;
import com.milai.asynctask.PostGetTask;
import com.milai.http.HttpMgr;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class TransactionSummaryActivity extends Activity implements
		OnClickListener {
	private Button bt_return;
	private LinearLayout ll_date, ll_payway;
	private Intent intent;
	private TextView tv_store;
	private Map<String, String> map;
	private PostGetTask<JSONObject> task;
	private String domain = "http://a.milaipay.com/merchant/stataction/business";
	private Gson gson;
	private Payrepost repost;
	private TextView tv_amount_receivable, tv_amount_paid_up,
			tv_order_quantity, tv_amount_refund, tv_refund_quantity,tv_date;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transaction_summary);
		bt_return = (Button) findViewById(R.id.bt_return);
		bt_return.setOnClickListener(this);
		tv_date=(TextView) findViewById(R.id.tv_date);
		ll_date = (LinearLayout) findViewById(R.id.ll_date);
		ll_payway = (LinearLayout) findViewById(R.id.ll_payway);

		tv_amount_receivable = (TextView) findViewById(R.id.tv_amount_receivable);
		tv_amount_paid_up = (TextView) findViewById(R.id.tv_amount_paid_up);
		tv_order_quantity = (TextView) findViewById(R.id.tv_order_quantity);
		tv_amount_refund = (TextView) findViewById(R.id.tv_amount_refund);
		tv_refund_quantity = (TextView) findViewById(R.id.tv_refund_quantity);
		ll_date.setOnClickListener(this);
		ll_payway.setOnClickListener(this);


	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(Cookies.getStarttime()==null||Cookies.getEndtime()==null){
			tv_date.setText("点击选择日期");
		}else{
			tv_date.setText(Cookies.getStart()+"至"+Cookies.getEnd());
		}
		getData();
	}


	void getData() {
		new PostGetTask<JSONObject>(this) {

			@Override
			protected JSONObject doBackgroudJob() throws Exception {
				StringBuilder type=null;
				JSONObject jParam = new JSONObject();
				jParam.put("uid",Cookies.getUserId());
				jParam.put("token",Cookies.getToken());
				jParam.put("type",Cookies.getPayway());

				jParam.put("start_time",Cookies.getStarttime());
				jParam.put("end_time",Cookies.getEndtime());
				JSONObject resp = HttpMgr.getInstance().postJson(
						domain,
						jParam, false);
				Log.d("zcz",jParam.toString());
				return resp;
			}

			@Override
			protected void doPostJob(Exception exception, JSONObject result) {
				try {

					Log.d("zcz", result.getJSONObject("data").toString());
						if(result.getJSONObject("data").getString("sum_total_fee")==null){
                            tv_amount_receivable.setText("0元");
						}else {
							tv_amount_receivable.setText(result.getJSONObject("data").getString("sum_total_fee")+"元");
						}
                        if(result.getJSONObject("data").getString("sum_real_price")==null){
                            tv_amount_paid_up.setText("0元");
                        }else{
							tv_amount_paid_up.setText(result.getJSONObject("data").getString("sum_real_price")+"元");
						}

					
						tv_order_quantity.setText(result.getJSONObject("data").getString("total_business_count")+"笔");
                        if(result.getJSONObject("data").getString("sum_refund_fee")==null){
                            tv_amount_refund.setText("0元");
                        }else{

							tv_amount_refund.setText(result.getJSONObject("data").getString("sum_refund_fee")+"元");
						}

					
						tv_refund_quantity.setText(result.getJSONObject("data").getString("sum_refund_count")+"笔");

					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

		}.execute();
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_date:
			intent=new Intent(this,ActChooseDate.class);
			startActivity(intent);
			break;
		case R.id.ll_payway:
			intent=new Intent(this,ChoosePaywayActivity.class);
			startActivity(intent);
			break;
		case R.id.bt_return:
			
			finish();
			break;
		default:
			break;
		}
		
	}
}
