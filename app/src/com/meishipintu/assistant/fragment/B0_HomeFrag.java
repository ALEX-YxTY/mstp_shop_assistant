package com.meishipintu.assistant.fragment;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.adapter.AdapterPayPath;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.orderdish.ActCaptureTicket;
import com.meishipintu.assistant.ui.pay.ActNewPayment;
import com.meishipintu.assistant.ui.pay.ActPayQRCode;
import com.meishipintu.assistant.ui.pay.PrintActivity;
import com.meishipintu.core.utils.CommonUtils;
import com.meishipintu.core.utils.ConstUtil;
import com.meishipintu.core.utils.MyDialogUtil;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class B0_HomeFrag extends Fragment {

	private FragClickListener mFraglistener;
	private AdapterPayPath adapterPayPath;
    
    public static B0_HomeFrag createInstance(FragClickListener l) {
    	B0_HomeFrag f = new B0_HomeFrag();
		f.mFraglistener = l;
        return f;
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.frag_home,null);
		String time = CommonUtils.DateUtilOne(System.currentTimeMillis());

		final TextView tvPrice = (TextView) view.findViewById(R.id.tv_price);
		GridView gv = (GridView)view.findViewById(R.id.grid_pay_path);
		
		if (adapterPayPath == null) {
			adapterPayPath = new AdapterPayPath(this.getActivity());
		}
		
		gv.setAdapter(adapterPayPath);

		Log.i("test", "shoptype:" + Cookies.getShopType());
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String path = adapterPayPath.getPayTypeString(position);
								
				if (path.equals("bfu")) { //百富
					Cookies.setPayType(5);
					tvPrice.setText("百富支付");
				} else if(path.equals("ldi")){	//联迪
					Cookies.setPayType(5);
					tvPrice.setText("联迪支付");

				} else if(path.equals("lkl")){	//拉卡拉
					Cookies.setPayType(5);
					tvPrice.setText("拉卡拉支付");

				} else if(path.equals("wpos")){	//旺POS
					Cookies.setPayType(5);
					tvPrice.setText("旺POS支付");

				}else if (path.equals("cas")) {  //现金
					Cookies.setPayType(4);
					tvPrice.setText("现金支付");

				} else if (path.equals("mil")) {  //米来支付
					Cookies.setPayType(0);
					tvPrice.setText("米来支付");

				} else if (path.equals("wx1")) { //微信扫码支付
					Cookies.setPayType(9);
					tvPrice.setText("微信扫码支付");

				} else if (path.equals("al1")) { //支付宝扫码支付
					Cookies.setPayType(6);
					tvPrice.setText("支付宝扫码支付");

				} else if (path.equals("al2")) { //支付宝条码支付
					Cookies.setPayType(7);
					tvPrice.setText("支付宝条码支付");

				} else if (path.equals("wx2")) { //微信刷卡支付
					Cookies.setPayType(12);
					tvPrice.setText("微信刷卡支付");

				} else if (path.equals("uni")) { //银联刷卡支付
					Cookies.setPayType(11);
					tvPrice.setText("银联刷卡支付");

				} else if (path.equals("icc")) {  //ic射频卡支付
					Cookies.setPayType(10);
					tvPrice.setText("ic射频卡支付");

				}else if (path.equals("oth")) { //其它支付方式
					Cookies.setPayType(14);
					tvPrice.setText("其它支付方式");

				} else if (path.equals("al3")) { //支付宝转账模式
					Cookies.setPayType(15);
					tvPrice.setText("支付宝转账模式");

				} 
				
				adapterPayPath.notifyDataSetChanged();
			}
		});
		
		view.findViewById(R.id.rl_price).setOnClickListener(mClkListener);
		
		return view;
	}
	
	private OnClickListener mClkListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.rl_price:
					int paytype = Cookies.getPayType();			//default = -1
					if(paytype==-1) {
						Toast.makeText(B0_HomeFrag.this.getActivity(), getString(R.string.selpaytype),
								Toast.LENGTH_LONG).show();
					}
					else {
						Intent in = new Intent(getActivity(), ActNewPayment.class);
						//Intent in=new Intent(getActivity(), PrintActivity.class);
						startActivity(in);

						B0_HomeFrag.this.getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
					}
					break;
			}

		}
	};

	public void refreshFragment() {
		if (adapterPayPath != null) {
			adapterPayPath.updateData();
			adapterPayPath.notifyDataSetChanged();
		}
	}
}
