package com.meishipintu.core.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;

import com.meishipintu.assistant.R;

public class LoadMoreStickyListView extends StickyListView implements OnScrollListener {

    public interface FootViewListener {
        public void onClick();
    }

    public static final int FOOT_VIEW_STATE_MORE = 0;

    public static final int FOOT_VIEW_STATE_CLICK = 1;

    public static final int FOOT_VIEW_STATE_ALL = 2;

    public static final int FOOT_VIEW_STATE_NO_DATA = 3;

    private LayoutInflater layoutInflater;

    private View footView, footProgressBar, footllLoadMore;

    private Handler mHandler = new Handler();

    private TextView tvFootView;

    private boolean isRecored;

    private int lastCount, firstItemIndex;

    private boolean isAutoLoadMore = false;

    private FootViewListener footViewListener;

    public LoadMoreStickyListView(Context context) {
        super(context);
        init();
    }

    public LoadMoreStickyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreStickyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        View HeaderView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_header, this, false);
        setPinnedHeader(HeaderView);
        layoutInflater = LayoutInflater.from(getContext());
        footView = layoutInflater.inflate(R.layout.layout_loadable_list_foot, null);
        footllLoadMore = footView.findViewById(R.id.ll_foot_loadmore);
        footProgressBar = footView.findViewById(R.id.foot_progress_bar);
        tvFootView = (TextView) footView.findViewById(R.id.tv_text);
        footView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFootViewByState(FOOT_VIEW_STATE_CLICK);
                if (footViewListener != null) {
                    footViewListener.onClick();
                }
            }
        });
        addFooterView(footView);

        setOnScrollListener(this);
        // if (Cookies.getUpdateTime() != -1) {
        // time = TimeUtil.convertLongToFormatString(Cookies.getUpdateTime(), "yyyy-MM-dd HH:mm:ss");
        // lastUpdatedTextView.setText(getContext().getString(R.string.custom_recently, time));
        // }
    }

    public void controlPinnedHeader(int position) {
        if (null == mHeaderView) {
            return;
        }
        int pinnedHeaderState = mStikyAdapter.getPinnedHeaderState(position - 1);
        switch (pinnedHeaderState) {
        case StickyAdapter.PINNED_HEADER_GONE:
            mDrawFlag = false;
            break;
        case StickyAdapter.PINNED_HEADER_VISIBLE:
            mStikyAdapter.configurePinnedHeader(mHeaderView, position - 1, 0);
            mDrawFlag = true;
            mHeaderView.layout(0, 0, mMeasuredWidth, mMeasuredHeight);
            break;
        case StickyAdapter.PINNED_HEADER_PUSHED_UP:
            mStikyAdapter.configurePinnedHeader(mHeaderView, position - 1, 0);
            mDrawFlag = true;
            View topItem = getChildAt(0);
            if (null != topItem) {
                int bottom = topItem.getBottom();
                int height = mHeaderView.getHeight();
                int y;
                if (bottom < height) {
                    y = bottom - height;
                } else {
                    y = 0;
                }

                if (mHeaderView.getTop() != y) {
                    mHeaderView.layout(0, y, mMeasuredWidth, mMeasuredHeight + y);
                }
            }
            break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        firstItemIndex = firstVisibleItem;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void changeFootViewByState(int footViewState) {
        switch (footViewState) {
        case FOOT_VIEW_STATE_MORE:
            footView.setVisibility(View.VISIBLE);
            footllLoadMore.setVisibility(View.VISIBLE);
            footProgressBar.setVisibility(View.GONE);
            tvFootView.setText(getContext().getString(R.string.load_more));
            break;
        case FOOT_VIEW_STATE_CLICK:
            footView.setVisibility(View.VISIBLE);
            footProgressBar.setVisibility(View.VISIBLE);
            tvFootView.setText(getContext().getString(R.string.loading));
            break;
        case FOOT_VIEW_STATE_ALL:
            footView.setVisibility(View.GONE);
            footllLoadMore.setVisibility(View.GONE);
            break;
        case FOOT_VIEW_STATE_NO_DATA:
            footView.setVisibility(View.VISIBLE);
            footllLoadMore.setVisibility(View.GONE);
            // footProgressBar.setVisibility(View.GONE);
            // tvFootView.setText(getContext().getString(R.string.no_data));
            break;
        default:
            break;
        }
    }

    protected boolean onLoadMore() {
        if (footViewListener != null) {
            footViewListener.onClick();
            return true;
        }
        return false;
    }

    private Runnable mDismissOnScreenControlRunner = new Runnable() {

        @Override
        public void run() {
            onLoadMore();
        }
    };

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE:
            if (isAutoLoadMore) {
                final int childCount = getChildCount();
                final int itemCount = getAdapter().getCount() - 0;
                final int lastBottom = getChildAt(childCount - 1).getBottom();
                final int end = getHeight() - getPaddingBottom();
                final int firstVisiblePosition = getFirstVisiblePosition();
                if (firstVisiblePosition + childCount >= itemCount && lastBottom <= end) {
                    if (lastCount < itemCount) {
                        changeFootViewByState(FOOT_VIEW_STATE_CLICK);
                        mHandler.removeCallbacks(mDismissOnScreenControlRunner);
                        mHandler.postDelayed(mDismissOnScreenControlRunner, 1000);
                        lastCount = itemCount;
                    }
                }
            }
            int tempY = (int) event.getY();
            if (!isRecored && firstItemIndex == 0) {
                isRecored = true;
            }
            break;
        }
        return super.onTouchEvent(event);
    }

    public void setIsAutoLoaMore(boolean is) {
        isAutoLoadMore = is;
    }

    public void setFootViewListener(FootViewListener footViewListener) {
        this.footViewListener = footViewListener;
    }

}
