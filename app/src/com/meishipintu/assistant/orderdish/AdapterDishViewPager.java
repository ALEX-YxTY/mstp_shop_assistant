package com.meishipintu.assistant.orderdish;

import java.util.ArrayList;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meishipintu.assistant.R;
import com.milai.widget.LoadableImageView;

public class AdapterDishViewPager extends PagerAdapter {
	private int mChildCount = 0;
	private ArrayList<String> mListUrls = new ArrayList<String>();	
	
	private LayoutInflater mInflater;

	public AdapterDishViewPager(Activity context, int count) {
		mInflater = LayoutInflater.from(context);
		mChildCount = count;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Log.d("DD", "instantiateItem:pos:" + position);
		
		View view = mInflater.inflate(R.layout.item_viewpager_dish_detail, null); //新建view		
				
		LoadableImageView img = (LoadableImageView) view.findViewById(R.id.liv_dishes);
		img.load(mListUrls.get(position));
		
		container.addView(view, 0);  		
		return view; 
	}
	
	public void addUrl(String url) {
		mListUrls.add(url);
	}

	@Override
	public int getCount() {
		return mChildCount;
	}

//	public void swapCursor(Cursor cursor) {
//		this.c = cursor;
//	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return POSITION_NONE;
	}
}
