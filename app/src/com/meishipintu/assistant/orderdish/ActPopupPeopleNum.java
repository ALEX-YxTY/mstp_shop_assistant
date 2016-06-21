package com.meishipintu.assistant.orderdish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.milai.widget.AutoFiltGridView;

public class ActPopupPeopleNum extends PopupWindow {

	private Activity mParentActivity = null;
	private View mMainView;
	private AutoFiltGridView mGridPeopleNum = null;
	private int mPeople = 0;
	private EditText mEtInputNum=null;
	private TextView mTvTempContent=null;
	private Fragment mFragment=null;

	public ActPopupPeopleNum(Activity context) {
		super(context);
		mParentActivity = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMainView = inflater.inflate(R.layout.layout_people_num, null);
		setContentView(mMainView);
		TextView tv_title = (TextView) mMainView.findViewById(R.id.tv_title);
		tv_title.setText("请选择人数");

		mGridPeopleNum = (AutoFiltGridView) mMainView
				.findViewById(R.id.lv_people_num);
		mGridPeopleNum.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
		List<HashMap<String, Object>> mPeopleHashList = new ArrayList<HashMap<String, Object>>();
		for (int i = 1; i <= 11; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			if (i <=10) {
				map.put("pNum", Integer.toString(i));
			} else {
				map.put("pNum", "10人以上");
			}
			mPeopleHashList.add(map);
		}
		SimpleAdapter peopleAdapter = new SimpleAdapter(mParentActivity,
				mPeopleHashList, R.layout.people_num, new String[] { "pNum" },
				new int[] { R.id.tv_people });

		mGridPeopleNum.setAdapter(peopleAdapter);
		mGridPeopleNum.setOnItemClickListener(new ItemClickListener());
		Button bt_yes = (Button) mMainView.findViewById(R.id.bt_yes);
		Button bt_cancel = (Button) mMainView.findViewById(R.id.bt_cancle);
		mMainView.findViewById(R.id.rl_title)
				.setOnClickListener(mClickListener);
		mMainView.findViewById(R.id.btn_back)
		.setOnClickListener(mClickListener);
		initShow();
	}

	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (id) {
			case R.id.rl_title: {
				dismiss();
				break;
			}
			case R.id.btn_back:
			{
				dismiss();
				break;
			}
			case R.id.bt_yes:{
				String peopleNum=mEtInputNum.getText().toString().replace(" ", "");
				peopleNum=peopleNum.replace(".", "");
				if(peopleNum.isEmpty())
				{
					Toast.makeText(mParentActivity, "请输入人数", Toast.LENGTH_LONG).show();
				}else
				{
					int num=Integer.parseInt(peopleNum);
					if(num<1)
					{
						Toast.makeText(mParentActivity, "输入人数不能小于1", Toast.LENGTH_LONG).show();
					}else
					{
						mPeople=num;
						setPeopleNum(mPeople);
						dismiss();
					}
				}	
				break;
			}
			case R.id.bt_cancle:{
				mEtInputNum.setText("");
				mMainView.findViewById(R.id.rl_input_num).setVisibility(View.GONE);
				break;
			}
			}
		}
	};

	private void setPeopleNum(int peopleNum)
	{
		ActDishTicket.mActDishTicket.setPeopleNum(mPeople);
	}
	private class ItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			mPeople = position + 1;
			for (int i = 0; i < 10; i++) {
				View v = parent.getChildAt(i);
				TextView tv_peo = (TextView) v.findViewById(R.id.tv_people);
				if (i == position) {
					tv_peo.setSelected(true);			
				} else {
					tv_peo.setSelected(false);
				}
			}
			if(position==10)
			{
				mPeople=0;
				inputNum();
			}else
			{
				setPeopleNum(mPeople);
				dismiss();
			}		
		}
	}
	
	private void  inputNum()
	{
		mMainView.findViewById(R.id.rl_input_num).setVisibility(View.VISIBLE);
		mEtInputNum=(EditText)mMainView.findViewById(R.id.et_people_num);
		Button bt_yes=(Button)mMainView.findViewById(R.id.bt_yes);
		Button bt_cancle=(Button)mMainView.findViewById(R.id.bt_cancle);
		bt_yes.setOnClickListener(mClickListener);
		bt_cancle.setOnClickListener(mClickListener);
	}
	
	
	void initShow() {
		this.setWidth(LayoutParams.MATCH_PARENT); // 设置弹出窗体的宽
		this.setHeight(LayoutParams.MATCH_PARENT); // 设置弹出窗体的高
		this.setFocusable(true); // 设置弹出窗体可点击
//		this.setAnimationStyle(R.style.CustomDialog); // 设置弹出窗体动画效果
		ColorDrawable dw = new ColorDrawable(0x00ffffff); // 实例化一个ColorDrawable颜色为半透明
		this.setBackgroundDrawable(dw); // 设置弹出窗体的背景

		this.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				final float left = mMainView.findViewById(R.id.rl_title).getX();
				int width = mMainView.findViewById(R.id.rl_title).getWidth();
				final float right = left + width;
				// if(event.getX()<left||event.getX()>right)
				// {
				// dismiss();
				// }
				return false;
			}
		});
	}
}
