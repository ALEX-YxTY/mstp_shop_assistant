package com.meishipintu.core.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.milai.adapter.MyPagerAdapter;
import com.milai.viewpagerindicator.CirclePageIndicator;
import com.milai.widget.LoadableImageView;
import com.meishipintu.assistant.app.Cookies;
import com.meishipintu.assistant.ui.MainActivity;
import com.meishipintu.assistant.R;

public class ActGuide extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_guide);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        List<View> viewList = getViewList();
        MyPagerAdapter adapter = new MyPagerAdapter(viewList);
        pager.setAdapter(adapter);
        CirclePageIndicator indi = (CirclePageIndicator) findViewById(R.id.indicator);
        indi.setViewPager(pager);
    }

    private List<View> getViewList() {
        LayoutInflater inflater = LayoutInflater.from(this);
        List<View> list = new ArrayList<View>();
        View view1 = inflater.inflate(R.layout.layout_item_pager, null);
        View view2 = inflater.inflate(R.layout.layout_item_pager, null);
        View view3 = inflater.inflate(R.layout.layout_item_pager, null);
        View view4 = inflater.inflate(R.layout.layout_item_pager, null);     
        list.add(view1);
        list.add(view2);
        list.add(view3);
        list.add(view4);        
        LoadableImageView img1 = (LoadableImageView) view1.findViewById(R.id.liv_pager_img);
        LoadableImageView img2 = (LoadableImageView) view2.findViewById(R.id.liv_pager_img);
        LoadableImageView img3 = (LoadableImageView) view3.findViewById(R.id.liv_pager_img);
        LoadableImageView img4 = (LoadableImageView) view4.findViewById(R.id.liv_pager_img);        
        img1.setImageResource(R.drawable.guide_1);
        img2.setImageResource(R.drawable.guide_2);
        img3.setImageResource(R.drawable.guide_3);
        img4.setImageResource(R.drawable.guide_4);        
        img4.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	if (Cookies.getCityId() == 0){
            		Intent intent = new Intent();
    				intent.putExtra("first_use", true);
    				intent.setClass(ActGuide.this, ActCitySel.class);
    				startActivity(intent);
    				overridePendingTransition(R.anim.right_in, R.anim.left_out);
    			}else{
    				startActivity(new Intent().setClass(getBaseContext(), MainActivity.class));
    				overridePendingTransition(R.anim.right_in, R.anim.left_out);
    			}
                Cookies.saveShowGuide(false);
                finish();
            }
        });
        return list;
    }
}
