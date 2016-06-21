package com.meishipintu.core.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.milai.utils.TimeUtil;

public class LoadableStickyListView extends StickyListView implements OnScrollListener {

    public interface FootViewListener {
        public void onClick();
    }

    public interface HeaderViewListener {
        public void onRefresh();
    }

    private final static int RELEASE_TO_REFRESH = 0;

    private final static int PULL_TO_REFRESH = 1;

    private final static int REFRESHING = 2;

    private final static int DONE = 3;

    public static final int FOOT_VIEW_STATE_MORE = 0;

    public static final int FOOT_VIEW_STATE_CLICK = 1;

    public static final int FOOT_VIEW_STATE_ALL = 2;

    public static final int FOOT_VIEW_STATE_NO_DATA = 3;

    private static final int RATIO = 2;

    private LayoutInflater layoutInflater;

    private View headView, footView, footProgressBar, footllLoadMore, progressBar;

    private Handler mHandler = new Handler();

    private TextView tvFootView, tipsTextview, lastUpdatedTextView;

    private ImageView ivHeadArrow;

    private RotateAnimation animation, reverseAnimation;

    private boolean isRecored, isBack;

    private int lastCount, headContentHeight, startY, firstItemIndex, state;

    private boolean isAutoLoadMore = false;

    private String time;

    private HeaderViewListener headerViewListener;

    private FootViewListener footViewListener;

    public LoadableStickyListView(Context context) {
        super(context);
        init();
    }

    public LoadableStickyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadableStickyListView(Context context, AttributeSet attrs, int defStyle) {
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

        headView = layoutInflater.inflate(R.layout.layout_loadable_list_head, null);
        ivHeadArrow = (ImageView) headView.findViewById(R.id.iv_head_arrow);
        progressBar = headView.findViewById(R.id.head_progressBar);
        tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
        lastUpdatedTextView = (TextView) headView.findViewById(R.id.head_lastUpdatedTextView);
        measureView(headView);
        headContentHeight = headView.getMeasuredHeight();
        headView.getMeasuredWidth();
        headView.setPadding(0, -1 * headContentHeight, 0, 0);
        headView.invalidate();
        addHeaderView(headView);
        setOnScrollListener(this);
        // if (Cookies.getUpdateTime() != -1) {
        // time = TimeUtil.convertLongToFormatString(Cookies.getUpdateTime(), "yyyy-MM-dd HH:mm:ss");
        // lastUpdatedTextView.setText(getContext().getString(R.string.custom_recently, time));
        // }
        animation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(500);
        animation.setFillAfter(true);
        reverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(500);
        reverseAnimation.setFillAfter(true);
    }
    public void controlPinnedHeader(int position) {
        if (null == mHeaderView) {
            return;
        }
        int pinnedHeaderState = mStikyAdapter.getPinnedHeaderState(position-1);
        switch (pinnedHeaderState) {
        case StickyAdapter.PINNED_HEADER_GONE:
            mDrawFlag = false;
            break;
        case StickyAdapter.PINNED_HEADER_VISIBLE:
            mStikyAdapter.configurePinnedHeader(mHeaderView, position-1, 0);
            mDrawFlag = true;
            mHeaderView.layout(0, 0, mMeasuredWidth, mMeasuredHeight);
            break;
        case StickyAdapter.PINNED_HEADER_PUSHED_UP:
            mStikyAdapter.configurePinnedHeader(mHeaderView, position-1, 0);
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

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
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

    private void changeHeaderViewByState() {
        switch (state) {
        case RELEASE_TO_REFRESH:
            ivHeadArrow.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            tipsTextview.setVisibility(View.VISIBLE);
            lastUpdatedTextView.setVisibility(View.VISIBLE);
            ivHeadArrow.clearAnimation();
            ivHeadArrow.startAnimation(animation);
            tipsTextview.setText(R.string.custom_release_to_refresh);
            break;
        case PULL_TO_REFRESH:
            progressBar.setVisibility(View.GONE);
            tipsTextview.setVisibility(View.VISIBLE);
            lastUpdatedTextView.setVisibility(View.VISIBLE);
            ivHeadArrow.clearAnimation();
            ivHeadArrow.setVisibility(View.VISIBLE);
            if (isBack) {
                isBack = false;
                ivHeadArrow.clearAnimation();
                ivHeadArrow.startAnimation(reverseAnimation);
                tipsTextview.setText(R.string.custom_pull_refresh);
            } else {
                ivHeadArrow.setVisibility(View.GONE);// jiade
                tipsTextview.setText(R.string.custom_pull_refresh);
            }
            break;
        case REFRESHING:
            headView.setPadding(0, 0, 0, 0);
            headView.invalidate();
            progressBar.setVisibility(View.VISIBLE);
            ivHeadArrow.clearAnimation();
            ivHeadArrow.setVisibility(View.GONE);
            tipsTextview.setText(R.string.refreshing_and_waiting);
            lastUpdatedTextView.setVisibility(View.GONE);
            // Toast.makeText(getContext(), "Refreshing", Toast.LENGTH_SHORT).show();
            break;
        case DONE:
            headView.setPadding(0, -1 * headContentHeight, 0, 0);
            headView.invalidate();
            ivHeadArrow.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            ivHeadArrow.clearAnimation();
            ivHeadArrow.setImageResource(R.drawable.list_more_arrow_down);
            tipsTextview.setText(R.string.custom_pull_refresh);
            lastUpdatedTextView.setVisibility(View.VISIBLE);
            break;
        }
    }

    public void onRefreshComplete() {
        state = DONE;
        time = TimeUtil.convertLongToFormatString(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
        lastUpdatedTextView.setText(getContext().getString(R.string.custom_recently, time));
        changeHeaderViewByState();
    }

    public void refreshFromApp() {
        state = REFRESHING;
        changeHeaderViewByState();
        onRefresh();
    }

    private void onRefresh() {
        if (headerViewListener != null) {
            headerViewListener.onRefresh();
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
        case MotionEvent.ACTION_DOWN:
            if (firstItemIndex == 0 && !isRecored) {
                startY = (int) event.getY();
                isRecored = true;
            }
            break;
        case MotionEvent.ACTION_UP:
            if (state != REFRESHING) {
                if (state == DONE) {
                }
                if (state == PULL_TO_REFRESH) {
                    state = DONE;
                    changeHeaderViewByState();
                }
                if (state == RELEASE_TO_REFRESH) {
                    state = REFRESHING;
                    changeHeaderViewByState();
                    onRefresh();
                }
            }
            isRecored = false;
            isBack = false;
            break;
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
                startY = tempY;
            }
            if (state != REFRESHING && isRecored) {
                if (state == RELEASE_TO_REFRESH) {
                    if (((tempY - startY) / RATIO < headContentHeight) && (tempY - startY) > 0) {
                        state = PULL_TO_REFRESH;
                        changeHeaderViewByState();
                    } else if (tempY - startY <= 0) {
                        state = DONE;
                        changeHeaderViewByState();
                    } else {
                    }
                }
                if (state == PULL_TO_REFRESH) {
                    if ((tempY - startY) / RATIO >= headContentHeight) {
                        state = RELEASE_TO_REFRESH;
                        isBack = true;
                        changeHeaderViewByState();
                    } else if (tempY - startY <= 0) {
                        state = DONE;
                        changeHeaderViewByState();
                    }
                }
                if (state == DONE) {
                    if (tempY - startY > 0) {
                        state = PULL_TO_REFRESH;
                        changeHeaderViewByState();
                    }
                }
                if (state == PULL_TO_REFRESH) {
                    headView.setPadding(0, -1 * headContentHeight + (tempY - startY) / RATIO, 0, 0);
                    headView.invalidate();
                }
                if (state == RELEASE_TO_REFRESH) {
                    headView.setPadding(0, (tempY - startY) / RATIO - headContentHeight, 0, 0);
                    headView.invalidate();
                }
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

    public void setHeaderViewListener(HeaderViewListener refreshListener) {
        this.headerViewListener = refreshListener;
    }

}
