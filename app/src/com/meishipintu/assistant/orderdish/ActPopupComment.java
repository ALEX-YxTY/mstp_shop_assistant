package com.meishipintu.assistant.orderdish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.milai.dao.DishesDao;

public class ActPopupComment extends PopupWindow {

	private FragmentActivity mParentActivity = null;
	private View mMainView;
	private ListView mCommentListView;
	public EditText mEtComment;
	public TextView mTvComment;
	private String mComment = "";
	private HashMap selectedCommentHM = null;
	// private HashMap selectedTemp=null;
	private ArrayList commentArray = new ArrayList<String>();

	public static ActPopupComment actPC = null;
	private List<HashMap<Integer, String>> mCommentHashList = null;
	private boolean[] mSelectorStatus = null;
	public String[] mShopComment;
	private long mDishId = 0;

	public ActPopupComment(FragmentActivity context, long dishId,
			String dishName) {
		super(context);
		mParentActivity = context;
		actPC = this;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMainView = inflater.inflate(R.layout.popup_comment, null);
		setContentView(mMainView);
		mDishId = dishId;
		TextView tv_title = (TextView) mMainView.findViewById(R.id.tv_title);

		if (dishId != 0) {
			tv_title.setText("请选择备注" + "(" + dishName + ")");
		} else {
			tv_title.setText("请选择备注");
		}
		mTvComment = (TextView) mMainView.findViewById(R.id.tv_check_comment);
		mEtComment = (EditText) mMainView.findViewById(R.id.et_comment);
		getCommentData();
		String dishComment;
		if (dishId != 0) {
			dishComment = DishesDao.getInstance().getComment(mParentActivity,
					dishId);
		} else {
			dishComment = ActDishTicket.mActDishTicket.mEtWaitorNote.getText().toString();
		}
		checkTextIsVisible(dishComment);

		mCommentListView = (ListView) mMainView.findViewById(R.id.lv_comment);
		mCommentListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		AdapterComment adapterComment = new AdapterComment(mParentActivity,
				mCommentHashList, mSelectorStatus);
		mCommentListView.setAdapter(adapterComment);
		mCommentListView.setOnItemClickListener(choiceListener);
		Button bt_yes = (Button) mMainView.findViewById(R.id.bt_yes);
		Button bt_cancel = (Button) mMainView.findViewById(R.id.bt_cancle);
		mMainView.findViewById(R.id.btn_back).setOnClickListener(btListener);
		mMainView.findViewById(R.id.rl_title).setOnClickListener(btListener);
		bt_yes.setOnClickListener(btListener);
		bt_cancel.setOnClickListener(btListener);
		initShow();
	}

	private void getCommentData() {
		String str_comment = Cookies.getShopComment();
		mShopComment = str_comment.split("#");
		String[] initialselector = { "加微辣", "加中辣", "加重辣", "三分熟", "四分熟", "五分熟",
				"六分熟" };
		if (mShopComment[0].equals("")) {
			mShopComment = initialselector;
		}
		mSelectorStatus = new boolean[mShopComment.length];
		for (int i = 0; i < mShopComment.length; i++) {
			mSelectorStatus[i] = false;
		}
		mCommentHashList = new ArrayList<HashMap<Integer, String>>();
		selectedCommentHM = new HashMap<String, Integer>();
		for (int i = 0; i < mShopComment.length; i++) {
			HashMap<Integer, String> map = new HashMap<Integer, String>();
			map.put(i, mShopComment[i]);
			mCommentHashList.add(map);
			selectedCommentHM.put(mShopComment[i], i);
		}
	}

	private OnItemClickListener choiceListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			TextView tv_comment = (TextView) view.findViewById(R.id.tv_comment);
			CheckBox cb_comment = (CheckBox) view
					.findViewById(R.id.check_comment);
			if (cb_comment.isChecked()) {

				cb_comment.setChecked(false);
				// view.setSelected(false);
				mSelectorStatus[position] = false;
				setData(position, false);
			} else {
				cb_comment.setChecked(true);
				// view.setSelected(true);
				mSelectorStatus[position] = true;
				setData(position, true);
			}
		}
	};

	public void setData(int position, boolean plus) {
		if (!plus) {
			mComment = mComment.replace(mShopComment[position] + ",", "");
			mComment = mComment.replace("," + mShopComment[position], "");
			mComment = mComment.replace(mShopComment[position], "");
		} else {
			if (mComment.isEmpty()) {
				mComment = mShopComment[position];
			} else {
				mComment += "," + mShopComment[position];
			}
		}
		mTvComment.setText(mComment);
	}

	private void checkTextIsVisible(String comSel) {
		if (comSel.isEmpty()) {
			return;
		}
		String commentInput = "";
		String selComment = "";
		if (comSel.endsWith(",")) {
			comSel = comSel.substring(0, comSel.length() - 1);
		}
		String[] selArray = comSel.split(",");
		for (int i = 0; i < selArray.length; i++) {
			if (!selArray[i].isEmpty()) {
				if (selectedCommentHM.containsKey(selArray[i])) {
					mSelectorStatus[(Integer) selectedCommentHM
							.get(selArray[i])] = true;
					if (selComment.isEmpty()) {
						selComment = selArray[i];
					} else {
						selComment += "," + selArray[i];
					}
				} else {
					if (commentInput.isEmpty()) {
						commentInput = selArray[i];
					} else {
						commentInput += "," + selArray[i];
					}
				}
			}

		}

		mEtComment.setText(commentInput);
		mTvComment.setText(selComment);
		mComment = selComment;
	}

	private void writeToDB()
	{
		String inputComment = mEtComment.getText().toString();
		String selComment = mTvComment.getText().toString();
		String comment=null;
		if (inputComment.endsWith(",")) {
			inputComment = inputComment.substring(0,
					inputComment.length() - 1);
		}
		if(selComment.isEmpty())
		{
			comment=inputComment;
		}else
		{
			if(inputComment.isEmpty())
			{
				comment=selComment;
			}else
			{
				comment=selComment+","+inputComment;
			}
			
		}
		if (mDishId != 0) {			
			ActOrderdish.mActOrderDish.mAdapterSelDish.setComment(
					comment, mDishId);
		} else {
			ActDishTicket.mActDishTicket.mEtWaitorNote.setText(comment);
			//将备注写入订单详情页中的
		}
	}

	private OnClickListener btListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (id) {
			case R.id.bt_yes: {
				writeToDB();
				dismiss();
				break;
			}
			case R.id.bt_cancle: {
				dismiss();
				break;
			}
			case R.id.btn_back: {
				dismiss();
				break;
			}
			case R.id.rl_title: {
				dismiss();
			}
				break;
			}
		}
	};

	void initShow() {
		this.setWidth(LayoutParams.MATCH_PARENT); // 设置弹出窗体的宽
		this.setHeight(LayoutParams.MATCH_PARENT); // 设置弹出窗体的高
		this.setFocusable(true);// 设置弹出窗体可点击
//		this.setAnimationStyle(R.style.DialogAnimation); // 设置弹出窗体动画效果
		ColorDrawable dw = new ColorDrawable(0x00ffffff); // 实例化一个ColorDrawable颜色为半透明
		this.setBackgroundDrawable(dw); // 设置弹出窗体的背景

		this.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				final float left = mMainView.findViewById(R.id.rl_title).getX();
				int width = mMainView.findViewById(R.id.rl_title).getWidth();
				final float right = left + width;
				
				return false;
			}
		});
	}
}
