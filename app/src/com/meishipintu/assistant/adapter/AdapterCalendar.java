package com.meishipintu.assistant.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.meishipintu.core.widget.CalendarView;
import com.meishipintu.core.widget.CalendarView.OnItemClickListener;

import android.R.integer;
import android.content.Context;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

public class AdapterCalendar extends BaseAdapter implements OnItemClickListener {
	private Context context;
	private int count;
	private int currentYear;
	private int currentMonth;
	private boolean startPick = false, endPick = false;
	private List<CalendarView> pickDateListenrs;
	private boolean startSet = false, endSet = false;
	private int startYear = 0, startMonth = 0, startDay = 0, endYear = 0, endMonth = 0, endDay = 0;


	public void addPickDateListenr(CalendarView pickDateListenr) {
		pickDateListenrs.add(pickDateListenr);
	}

	public AdapterCalendar(Context context, int count) {
		this.context = context;
		this.count = count;
		pickDateListenrs = new ArrayList<CalendarView>();
		Time t = new Time("GMT+8");
		t.setToNow();
		t.normalize(true);
		currentYear = t.year;
		currentMonth = t.month + 1;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int m, y;
		if (currentMonth - (count - position - 1) <= 0) {
			m = 12 - ((count - position - 1) - currentMonth);
			y = currentYear - 1;
		} else {
			m = currentMonth - (count - position - 1);
			y = currentYear;
		}
		if (convertView == null) {
			convertView = new CalendarView(context, y, m);
			((CalendarView) convertView).setOnItemClickListener(AdapterCalendar.this);
			addPickDateListenr((CalendarView) convertView);
		} else {
			((CalendarView) convertView).setYearMonth(y, m);
			((CalendarView) convertView).changeBackground();
		}
		return convertView;
	}

	@Override
	public void onClick(int year, int month, int day) {
		/*
			还未设置开始日期或已经设完起止日期
			此时点击为重设开始日期
		 */
		if ((!startSet) || (startSet && endSet)) {
			for (CalendarView p : pickDateListenrs) {
				p.onPickDate(year, month, day, true);
			}
			startYear = year;
			startMonth = month;
			startDay = day;
			startSet = true;
			endSet = false;
		} else {
			/*
				已设开始未设结束日期
			 */
			if (!endSet) {
				if (year > startYear || ((year == startYear) && (month > startMonth))
						|| ((year == startYear) && (month == startMonth) && (day > startDay))
						|| ((year == startYear) && (month == startMonth) && (day == startDay))) {	//增加选择当天
					for (CalendarView p : pickDateListenrs) {
						p.onPickDate(year, month, day, false);		//设置结尾日期，并修改背景
					}
					endYear = year;
					endMonth = month;
					endDay = day;
					endSet = true;
				} else {
					for (CalendarView p : pickDateListenrs) {
						p.onPickDate(year, month, day, true);
					}
					startYear = year;
					startMonth = month;
					startDay = day;
					startSet = true;
					endSet = false;
				}
			}
		}
	}

	public Map<String, Integer> getPickedDate() {
		if (startSet && endSet) {
			int sd, ed;
			Time t = new Time("GMT+8");
			t.set(1, startMonth - 1, startYear);
			t.normalize(true);
			sd = startDay + 1 - t.weekDay;
			t.set(1, endMonth - 1, endYear);
			t.normalize(true);
			ed = endDay + 1 - t.weekDay;
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("startYear", startYear);
			map.put("startMonth", startMonth);
			map.put("startDay", sd);
			map.put("endYear", endYear);
			map.put("endMonth", endMonth);
			map.put("endDay", ed);
			return map;
		} else {
			return null;
		}
	}
}
