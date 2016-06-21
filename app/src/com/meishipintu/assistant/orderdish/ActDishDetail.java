package com.meishipintu.assistant.orderdish;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meishipintu.assistant.R;
import com.meishipintu.assistant.app.Cookies;
import com.milai.dao.DishesDao;
import com.milai.model.Dishes;
import com.meishipintu.core.utils.ConstUtil;
import com.milai.utils.ScreenParam;
import com.milai.utils.StringUtil;

public class ActDishDetail extends PopupWindow {
    private Activity mParentActivity = null;
    public static ActDishDetail mDishDetail = null;
    private AdapterDishViewPager mAdapterPagerDish = null;
    private ViewPager mDishViewPager = null;
    private View mMainView;
    private Cursor mCursor = null;
    private LayoutInflater mInflater = null;
    public ListView mLvSelDishes = null; // 已选择的菜品列表
    private RelativeLayout mSelDishLayout = null;

    private long mDishId = 0;
    private String mDishName;
    private int mOrigDishPrice;
    private int mDishPrice;
    private String mContent;
    private int mPosition;
    private int mCount;
    private int mIsShow;

    private Animation mScaleAniDown = null;

    private static final int MSG_DB_CHANGED = 1001;

    public TextView mTotalPrice = null;
    public TextView mSubPrice = null;
    public TextView mDishesTotalNum = null;
    private TextView mDishesSelTitle = null;
    private TextView mDishesSelTitle2 = null;
    private TextView mTvSelDish;

    public ActDishDetail() {
    }

    public ActDishDetail(Activity context, long dishId) {
        super(context);
        mParentActivity = context;
        mDishDetail = this;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMainView = mInflater.inflate(R.layout.orderdish_dish_detail, null);
        mCursor = mParentActivity.managedQuery(Dishes.CONTENT_URI, null, null,
                null, Dishes.COLUMN_NAME_TYPE_ORDER + ","
                        + Dishes.COLUMN_NAME_DISH_ORDER);
        setContentView(mMainView);
        mMainView.findViewById(R.id.btn_back).setOnClickListener(ll);
        TextView tv_title = (TextView) mMainView.findViewById(R.id.tv_title);
        tv_title.setText(Cookies.getShopName());
        mDishViewPager = (ViewPager) mMainView.findViewById(R.id.pager_dish);
        mTotalPrice = (TextView) mMainView
                .findViewById(R.id.tv_dish_orig_price);
        mSubPrice = (TextView) mMainView.findViewById(R.id.tv_sub_price);
        mDishesTotalNum = (TextView) mMainView.findViewById(R.id.tv_dishes_num);
        mDishesSelTitle = (TextView) mMainView.findViewById(R.id.tv_dish_sel_title);
        mDishesSelTitle2 = (TextView) mMainView.findViewById(R.id.tv_dish_sel_title2);
        if (mCursor.moveToFirst()) {
            mCount = mCursor.getCount();
            mAdapterPagerDish = new AdapterDishViewPager(context, mCount); // 新建pager的adapter

            for (int i = 0; i < mCursor.getCount(); i++) {
                mCursor.moveToPosition(i);
                if (mCursor.getInt(mCursor
                        .getColumnIndex(Dishes.COLUMN_NAME_DISH_ID)) == dishId)
                    mPosition = i;
                mAdapterPagerDish.addUrl(ConstUtil.getFileServerUrl(mCursor
                        .getString(mCursor
                                .getColumnIndex(Dishes.COLUMN_NAME_PICTURE))));
            }
            mDishViewPager.setAdapter(mAdapterPagerDish); // pager设置adapter
            // mDishViewPager.setOffscreenPageLimit(5);
            mDishViewPager.setOnPageChangeListener(mPagerChangeListener);
            mDishViewPager.setCurrentItem(mPosition, true);
            if (mPosition == 0) {
                doRefresh();
            }
            mMainView.findViewById(R.id.rl_dish_plus).setOnClickListener(ll);
            mSelDishLayout = (RelativeLayout) mMainView
                    .findViewById(R.id.rl_dish_sel);
            mLvSelDishes = (ListView) mMainView
                    .findViewById(R.id.pop_selected_dishlist);
            mLvSelDishes.setAdapter(ActOrderdish.mActOrderDish.mAdapterSelDish);
            TextView tv_selDish = (TextView) mMainView
                    .findViewById(R.id.tv_dish_selectes_btn);
            tv_selDish.setOnClickListener(ll);
        }
        mScaleAniDown = new ScaleAnimation(0.9f, 1, 0.9f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleAniDown.setDuration(200);
        updateTotal();
        initShow();
    }

    private OnPageChangeListener mPagerChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageSelected(int arg0) {
            // TODO Auto-generated method stub
            mPosition = arg0;
            doRefresh();
        }
    };

    //title悬浮效果
    @SuppressLint("NewApi")
    public void titleSuspension() {
        if (mSelDishLayout.isShown()) {
            int listHeight = mLvSelDishes.getHeight() + mDishesSelTitle.getHeight();
            int mSelDishLayoutHeight = 0;
            mSelDishLayoutHeight = mSelDishLayout.getHeight();
            if (listHeight >= mSelDishLayoutHeight) {
                mDishesSelTitle2.setVisibility(View.VISIBLE);
            } else {
                mDishesSelTitle2.setVisibility(View.GONE);
            }
        } else {
            mDishesSelTitle2.setVisibility(View.GONE);
        }

    }

    public void doRefresh() {

        mCursor.moveToPosition(mPosition);
        mDishId = mCursor.getLong(mCursor
                .getColumnIndex(Dishes.COLUMN_NAME_DISH_ID));
        mDishName = mCursor.getString(mCursor
                .getColumnIndex(Dishes.COLUMN_NAME_DISH_NAME));
        mOrigDishPrice = mCursor.getInt(mCursor
                .getColumnIndex(Dishes.COLUMN_NAME_PRICE_ORIG));
        mDishPrice = mCursor.getInt(mCursor
                .getColumnIndex(Dishes.COLUMN_NAME_PRICE));
        mContent = mCursor.getString(mCursor
                .getColumnIndex(Dishes.COLUMN_NAME_CONTENT));
        mIsShow = mCursor.getInt(mCursor
                .getColumnIndex(Dishes.COLUMN_NAME_IS_SHOW));

        TextView tv = (TextView) mMainView.findViewById(R.id.tv_dish_name);
        tv.setText(mDishName);
        tv = (TextView) mMainView.findViewById(R.id.tv_position);
        tv.setText(Integer.toString(mPosition + 1));
        tv = (TextView) mMainView.findViewById(R.id.tv_total_num);
        tv.setText(Integer.toString(mCount));
        tv = (TextView) mMainView.findViewById(R.id.tv_content);
        tv.setText(mContent);
        tv = (TextView) mMainView.findViewById(R.id.tv_dish_plus);
        if (mIsShow == 0) {
            mMainView.findViewById(R.id.rl_dish_plus).setSelected(true);
            tv.setText("已售罄");
        } else {
            mMainView.findViewById(R.id.rl_dish_plus).setSelected(false);//.setBackgroundDrawable(mParentActivity.getResources().getDrawable(R.drawable.bg_round_corner_cancle))
            tv.setText("加入清单");
        }
        tv = (TextView) mMainView.findViewById(R.id.tv_value_price_orig);
        if (mOrigDishPrice == 0) {
            tv.setText(R.string.prompts_price_unknown);
        } else {
            tv.setText(mParentActivity.getString(R.string.unit_rmb)
                    + StringUtil.getPriceString(mOrigDishPrice));
        }
        tv = (TextView) mMainView.findViewById(R.id.tv_value_price);
        if (mDishPrice == 0) {
            tv.setText(R.string.prompts_price_unknown);
        } else {
            tv.setText(mParentActivity.getString(R.string.unit_rmb)
                    + StringUtil.getPriceString(mDishPrice));
        }
        TextView tv_seperate = (TextView) mMainView
                .findViewById(R.id.tv_value_price_seperate);
        if (mDishPrice != mOrigDishPrice) {
            tv_seperate.setVisibility(View.VISIBLE);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv_seperate.setVisibility(View.INVISIBLE);
            tv.setVisibility(View.INVISIBLE);
        }
    }

    void initShow() {
        Display display = mParentActivity.getWindowManager()
                .getDefaultDisplay();
        int resourceId = mParentActivity.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        int result = mParentActivity.getResources().getDimensionPixelSize(
                resourceId);
        this.setWidth(display.getWidth()); // 设置弹出窗体的宽
        this.setHeight(display.getHeight() - result); // 设置弹出窗体的高
        this.setFocusable(true); // 设置弹出窗体可点击
        ColorDrawable dw = new ColorDrawable(0xb0000000); // 实例化一个ColorDrawable颜色为半透明
        this.setBackgroundDrawable(dw); // 设置弹出窗体的背景

        this.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    private void updateTotal() {
        boolean isDishSel = DishesDao.getInstance().hasDishSelected(
                mParentActivity);
        titleSuspension();
        if (isDishSel) { // 如果选了菜
            int tp = DishesDao.getInstance().totalPrice(mParentActivity);
            int tpOrig = DishesDao.getInstance()
                    .totalPriceOrig(mParentActivity);
            int tpsub = tpOrig - tp;
            int tq = DishesDao.getInstance().totalQuantity(mParentActivity);
            mTotalPrice.setText("总计：" + StringUtil.getPriceString(tp));
            mSubPrice.setText("已优惠：" + StringUtil.getPriceString(tpsub));
            mDishesTotalNum.setVisibility(View.VISIBLE);
            mDishesTotalNum.setText(Integer.toString(tq));

            LayoutParams p = mLvSelDishes.getLayoutParams();
            if (tq > 7) {
                p.height = ScreenParam.getInstance().getScreenHeight() / 2;
                mLvSelDishes.setLayoutParams(p);
            } else {
                p.height = LayoutParams.WRAP_CONTENT;
                mLvSelDishes.setLayoutParams(p);
            }
        } else {
            mTotalPrice.setText("总计：" + StringUtil.getPriceString(0));
            mSubPrice.setText("已优惠：" + StringUtil.getPriceString(0));
            mDishesTotalNum.setVisibility(View.GONE);
            mDishesTotalNum.setText(Integer.toString(0));
        }
    }

    // @Override
    // protected void onCreate(Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    // setContentView(R.layout.orderdish_dish_detail);
    // Intent in = getIntent();
    // String dishName = null;
    // if (in.hasExtra("dishName")) {
    // dishName = in.getStringExtra("dishName");
    // }
    // String content = null;
    // if (in.hasExtra("content")) {
    // content = in.getStringExtra("content");
    // }
    // int price = 0;
    // if (in.hasExtra("price")) {
    // price = in.getIntExtra("price", 0);
    // }
    // int priceOrig = 0;
    // if (in.hasExtra("priceOrig")) {
    // priceOrig = in.getIntExtra("priceOrig", 0);
    // }
    // String pic = null;
    // if (in.hasExtra("pic")) {
    // pic = in.getStringExtra("pic");
    // }
    // TextView tv = (TextView) findViewById(R.id.tv_title);
    // tv.setText(getString(R.string.tag_dish_detail));
    // findViewById(R.id.btn_back).setOnClickListener(ll);
    // tv = (TextView) findViewById(R.id.tv_dish_name);
    // tv.setText(dishName);
    // tv = (TextView) findViewById(R.id.tv_content);
    // tv.setText(content);
    // tv = (TextView) findViewById(R.id.tv_value_price_orig);
    // if (priceOrig == 0) {
    // tv.setText(R.string.prompts_price_unknown);
    // } else {
    // tv.setText(getString(R.string.unit_rmb)+StringUtil.getPriceString(priceOrig));
    // }
    // tv = (TextView) findViewById(R.id.tv_value_price);
    // if (price == 0) {
    // tv.setText(R.string.prompts_price_unknown);
    // } else {
    // tv.setText(getString(R.string.unit_rmb)+StringUtil.getPriceString(price));
    // }
    //
    // TextView tv_seperate = (TextView)
    // findViewById(R.id.tv_value_price_seperate);
    // if(price!=priceOrig) {
    // tv_seperate.setVisibility(View.VISIBLE);
    // tv.setVisibility(View.VISIBLE);
    // }
    // else {
    // tv_seperate.setVisibility(View.INVISIBLE);
    // tv.setVisibility(View.INVISIBLE);
    // }
    //
    // LoadableImageView liv = (LoadableImageView)findViewById(R.id.liv_dish);
    // liv.load(ConstUtil.getFileServerUrl(pic));
    // }

    private OnClickListener ll = new OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setAnimation(mScaleAniDown);
            v.startAnimation(mScaleAniDown);
            switch (v.getId()) {
                case R.id.btn_back:
                    dismiss();
                    break;

                case R.id.rl_dish_plus: {
                    if (mIsShow == 1) {
                        updateQuantity(mDishId, true);
                        Toast.makeText(mParentActivity,
                                "“" + mDishName + "”" + "已加入清单", Toast.LENGTH_SHORT)
                                .show();

                    } else {
                        Toast.makeText(mParentActivity,
                                "“" + mDishName + "”" + "已售罄", Toast.LENGTH_SHORT).show();
                    }
                    updateTotal();
                }
                break;
                case R.id.tv_dish_selectes_btn: {
                    if (mSelDishLayout.isShown()) {
                        mSelDishLayout.setVisibility(View.GONE);
                        mDishesSelTitle2.setVisibility(View.GONE);
                    } else {
                        mSelDishLayout.setVisibility(View.VISIBLE);
                    }
                    titleSuspension();
                }
                break;
                default:
                    break;
            }

        }
    };

    private void updateQuantity(long id, boolean plus) {
        DishesDao.getInstance().updateQuantity(mParentActivity, id, plus);
    }

}
