package com.meishipintu.assistant.fragment;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.orderdish.ActOrderdish;
import com.meishipintu.assistant.orderdish.ActSubmittedTicket;
import com.meishipintu.assistant.ui.ActSetting;
import com.meishipintu.assistant.ui.MainActivity;
import com.meishipintu.assistant.ui.pay.ActTeamList;
import com.meishipintu.assistant.ui.pay.ActChooseDate;
import com.meishipintu.core.ui.ActWebView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public class D0_MineFrag extends Fragment {

	private FragClickListener mFraglistener;

	public static D0_MineFrag createInstance(FragClickListener l) {
		D0_MineFrag f = new D0_MineFrag();
		f.mFraglistener = l;
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_mine, null);

		view.findViewById(R.id.rl_order_dish).setOnClickListener(ll);
		view.findViewById(R.id.rl_ticket).setOnClickListener(ll);
		view.findViewById(R.id.rl_team_list).setOnClickListener(ll);
		view.findViewById(R.id.rl_backend).setOnClickListener(ll);
		view.findViewById(R.id.rl_setting).setOnClickListener(ll);
		view.findViewById(R.id.rl_logout).setOnClickListener(ll);

		return view;
	}

	private OnClickListener ll = new OnClickListener() {
		Intent intent = new Intent();

		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.rl_order_dish:
				intent.setClass(getActivity(), ActOrderdish.class);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;
			case R.id.rl_ticket:
				 intent.putExtra("status", 3);
				 intent.putExtra("takeaway", 1);
				 intent.setClass(getActivity(), ActSubmittedTicket.class);
				 startActivity(intent);
				 getActivity().overridePendingTransition(R.anim.right_in,
				 R.anim.left_out);

				break;
			case R.id.rl_team_list:
				intent.setClass(getActivity(), ActTeamList.class);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;

			// case R.id.rl_payment_detail: {
			// intent.setClass(MainActivity.this, ActPayment.class);
			// startActivity(intent);
			// overridePendingTransition(R.anim.right_in, R.anim.left_out);
			// break;
			// }

			case R.id.rl_backend:
				intent.setClass(getActivity(), ActWebView.class);
				intent.putExtra("linkTitle", "后台管理");
				intent.putExtra("link", "http://b.milaipay.com");
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
				break;
			case R.id.rl_setting:
				intent.setClass(getActivity(), ActSetting.class);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;
			case R.id.rl_logout:
				MainActivity.mActivity.showDialogQuit(1);
				break;
			default:
				break;
			}

		}
	};
}
