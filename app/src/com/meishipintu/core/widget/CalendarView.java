package com.meishipintu.core.widget;

import java.util.ArrayList;
import java.util.List;

import com.meishipintu.assistant.R;

import android.content.Context;
import android.text.format.Time;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarView extends LinearLayout {
	private Context context;
	private boolean isCurrentMonth = false;
	private int year;
	private int month;
	private int todayMonthDay;
	private int todayWeekDay;
	private int firstWeekDay;
	private int maxDay;
	private Time t;
	private TextView tv_year_month;
	private List<LinearLayout> items;
	private List<TextView> tv_tags;
	private List<TextView> tv_days;
	private int lines = 0;
	private int colorOrange, colorWhite, colorGray, colorLightGray;
	private OnItemClickListener onItemClickListener;
	private boolean startSet = false, endSet = false;
	private int startYear, startMonth, startDay, endYear, endMonth, endDay;

	public interface OnItemClickListener {
		void onClick(int year, int month, int day);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public CalendarView(Context context, int year, int month) {
		super(context);
		this.context = context;
		this.year = year;
		this.month = month;
		colorOrange = context.getResources().getColor(R.color.light_orange);
		colorWhite = context.getResources().getColor(R.color.white);
		colorLightGray = context.getResources().getColor(R.color.light_gray);
		colorGray = context.getResources().getColor(R.color.dark_gray);
		setOrientation(VERTICAL);
		setGravity(Gravity.CENTER_VERTICAL);
		t = new Time("GMT+8");
		tv_year_month = new TextView(context);
		tv_year_month.setTextColor(colorLightGray);
		tv_year_month.setGravity(Gravity.CENTER);
		tv_year_month.setBackgroundColor(colorWhite);
		tv_year_month.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				context.getResources().getDimensionPixelSize(R.dimen.tv_46));
		tv_year_month.setPadding(0, context.getResources().getDimensionPixelSize(R.dimen.margin_20), 0, 0);
		tv_year_month.setGravity(Gravity.CENTER);
		items = new ArrayList<LinearLayout>();
		tv_tags = new ArrayList<TextView>();
		tv_days = new ArrayList<TextView>();
		addView(tv_year_month);

		setYearMonth(year, month);
	}

	public void setYearMonth(int year, int month) {
		this.year = year;
		this.month = month;
		tv_year_month.setText(year + "年" + month + "月");
		month--;
		t.setToNow();
		t.normalize(true);
		System.out.println("today " + t.monthDay + "year " + t.year);
		if (year == t.year && month == t.month) {
			isCurrentMonth = true;
			todayMonthDay = t.monthDay;
			todayWeekDay = t.weekDay;
		} else {
			isCurrentMonth = false;
		}
		t.set(1, month, year);
		t.normalize(true);
		firstWeekDay = t.weekDay;
		maxDay = getMaxDay(month);
		int l = (int) Math.ceil(((double) (maxDay + firstWeekDay)) / 7.0d);
		if (l > lines) {
			for (int i = lines; i < l; i++) {
				LinearLayout ll = new LinearLayout(context);
				ll.setOrientation(HORIZONTAL);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				ll.setLayoutParams(params);
				for (int j = 0; j < 7; j++) {
					LinearLayout item = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.calendar_item, ll,
							false);
					TextView tv_tag = (TextView) item.findViewById(R.id.tv_tag);
					TextView tv_day = (TextView) item.findViewById(R.id.tv_day);
					ll.addView(item);
					items.add(item);
					tv_tags.add(tv_tag);
					tv_days.add(tv_day);
				}
				addView(ll);
			}
		} else if (l < lines) {
			for (int i = l; i < lines; i++) {
				for (int j = 0; j < 7; j++) {
					items.remove(items.size() - 1);
					tv_tags.remove(tv_tags.size() - 1);
					tv_days.remove(tv_days.size() - 1);
				}
				removeViewAt(this.getChildCount() - 1);
			}
		}
		lines = l;
		for (int i = 0; i < firstWeekDay; i++) {
			 items.get(i).setBackgroundColor(colorWhite);
			 tv_tags.get(i).setVisibility(INVISIBLE);
			 tv_days.get(i).setVisibility(INVISIBLE);
		}
		for (int i = firstWeekDay; i < maxDay + firstWeekDay; i++) {
			items.get(i).setBackgroundColor(colorWhite);
			tv_tags.get(i).setVisibility(VISIBLE);
			tv_days.get(i).setVisibility(VISIBLE);
			tv_days.get(i).setText((i + 1 - firstWeekDay) + "");
			tv_days.get(i).setTextColor(colorLightGray);

		}
		for (int i = maxDay + firstWeekDay; i < (7 * lines); i++) {
			 items.get(i).setBackgroundColor(colorWhite);
			 tv_tags.get(i).setVisibility(INVISIBLE);
			 tv_days.get(i).setVisibility(INVISIBLE);
		}
		for (int i = 0; i < items.size(); i++) {
			final int copy_i = i;
			items.get(i).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (onItemClickListener != null) {
						onItemClickListener.onClick(CalendarView.this.year, CalendarView.this.month, copy_i);
					}
				}
			});
		}
		if (isCurrentMonth) {
			tv_days.get(todayMonthDay + firstWeekDay - 1).setText("今天");
			tv_days.get(todayMonthDay + firstWeekDay - 1).setTextColor(colorOrange);
			for (int i = todayMonthDay + firstWeekDay; i < items.size(); i++) {
				items.get(i).setClickable(false);
			}
		} else {
			for (int i = maxDay + firstWeekDay; i < items.size(); i++) {
				items.get(i).setClickable(false);
			}
		}
		for (int i = 0; i < firstWeekDay; i++) {
			items.get(i).setClickable(false);
		}
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	private int getMaxDay(int month) {
		month++;
		int m;
		if (month == 2) {
			if (year % 4 == 0)
				m = 29;
			else
				m = 28;
		} else {
			if (month <= 7)
				if (month % 2 == 0)
					m = 30;
				else
					m = 31;
			else if (month % 2 == 0)
				m = 31;
			else
				m = 30;
		}
		return m;
	}

	public void changeBackground() {
		if (startSet && (!endSet)) {
			if (this.month == startMonth && this.year == startYear) {
				setYearMonth(year, month);
				items.get(startDay).setBackgroundColor(colorOrange);
				tv_tags.get(startDay).setText("起始");
				tv_days.get(startDay).setTextColor(colorWhite);
			}
		} else if (startSet && endSet) {
			if (year == startYear && startYear == endYear && startMonth == month && endMonth == month) {
				//增加选择当日
				if (startDay == endDay) {
					items.get(startDay).setBackgroundColor(colorOrange);
					tv_days.get(startDay).setTextColor(colorWhite);
					tv_tags.get(startDay).setVisibility(INVISIBLE);
					tv_tags.get(startDay).setText("起始/结束");
					tv_tags.get(startDay).setVisibility(VISIBLE);
				} else {
					for (int i = startDay; i <= endDay; i++) {
						items.get(i).setBackgroundColor(colorOrange);
						tv_days.get(i).setTextColor(colorWhite);
						tv_tags.get(i).setVisibility(INVISIBLE);
					}
					tv_tags.get(startDay).setText("起始");
					tv_tags.get(startDay).setVisibility(VISIBLE);
					tv_tags.get(endDay).setText("结束");
					tv_tags.get(endDay).setVisibility(VISIBLE);
				}

			} else if ((startYear == year && startMonth < month && endMonth == month && endYear == year)
					|| (startYear < year && endMonth == month && endYear == year)) {
				for (int i = 0; i <= endDay; i++) {
					items.get(i).setBackgroundColor(colorOrange);
					tv_days.get(i).setTextColor(colorWhite);
					tv_tags.get(i).setVisibility(INVISIBLE);
				}
				tv_tags.get(endDay).setText("结束");
				tv_tags.get(endDay).setVisibility(VISIBLE);
			} else if ((startMonth == month && month < endMonth && startYear == year && startYear == endYear)
					|| (endYear > year && startMonth == month)) {
				for (int i = startDay; i < items.size(); i++) {
					items.get(i).setBackgroundColor(colorOrange);
					tv_days.get(i).setTextColor(colorWhite);
					tv_tags.get(i).setVisibility(INVISIBLE);
				}
				tv_tags.get(startDay).setText("起始");
				tv_tags.get(startDay).setVisibility(VISIBLE);
			} else if ((startYear == year && startYear == endYear && startMonth < month && month < endMonth)
					|| (startYear < year && endYear == year && month < endMonth)
					|| (startYear == year && endYear > year && startMonth < month)
					|| (startYear < year && endYear > year)) {
				for (int i = 0; i < items.size(); i++) {
					items.get(i).setBackgroundColor(colorOrange);
					tv_days.get(i).setTextColor(colorWhite);
					tv_tags.get(i).setVisibility(INVISIBLE);
				}
			}
		}
	}

	public void onPickDate(int year, int month, int day, boolean start) {
		if (start) {
			startYear = year;
			startMonth = month;
			startDay = day;
			startSet = true;
			endSet = false;
		} else {
			endYear = year;
			endMonth = month;
			endDay = day;
			startSet = true;
			endSet = true;
		}
		setYearMonth(this.year, this.month);
		changeBackground();
	}
}
