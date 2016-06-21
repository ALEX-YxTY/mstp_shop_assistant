package com.meishipintu.assistant.ui.pay;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R.id;
import com.meishipintu.assistant.R.layout;
import com.meishipintu.assistant.adapter.AdapterCalendar;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.core.widget.CalendarView;

import java.util.Map;

public class ActChooseDate extends Activity implements OnClickListener {
	private ListView lv_calendar;
	private AdapterCalendar adapter;
	private int year = 2016;
	private int month = 1;
	CalendarView cv;
	private Button btn_back;
	private TextView tv_title, tv_right;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout.activity_act_choose_date);

		initView();
	}

	private void initView() {
		lv_calendar = (ListView) findViewById(id.lv_calendar);
		adapter = new AdapterCalendar(this,12);
		lv_calendar.setAdapter(adapter);
		btn_back = (Button) findViewById(id.btn_back);
		btn_back.setOnClickListener(this);
		tv_title = (TextView) findViewById(id.tv_title);
		tv_title.setText("起始结束日期");
		tv_right = (TextView) findViewById(id.tv_right);
		tv_right.setText("确定");
		tv_right.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case id.btn_back:
			finish();
			break;
		case id.tv_right:
			// 获取选中的时间段
			Map<String, Integer> map = adapter.getPickedDate();
			if (map != null) {
				Toast.makeText(this,
						map.get("startYear")// 起始年
								+ "." + map.get("startMonth")// 起始月
								+ "." + map.get("startDay")// 起始日
								+ "-" + map.get("endYear")// 结束年
								+ "." + map.get("endMonth")// 结束月
								+ "." + map.get("endDay"), // 结束日
						Toast.LENGTH_SHORT).show();
				String start=map.get("startYear")+"-"+map.get("startMonth")+"-"+map.get("startDay")+" 00:00";
				String end=map.get("endYear")+"-"+map.get("endMonth")+"-"+map.get("endDay")+" 23:59";
				String starttime=map.get("startYear")+"-"+map.get("startMonth")+"-"+map.get("startDay");
				String endtime=map.get("endYear")+"-"+map.get("endMonth")+"-"+map.get("endDay");
				Cookies.saveStarttime(start);
				Cookies.saveEndtime(end);
				Cookies.saveStart(starttime);
				Cookies.saveEnd(endtime);
				finish();
			}
			break;
		}
	}

}
