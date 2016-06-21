package com.meishipintu.core.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.meishipintu.assistant.R;

public class StickyListView extends ListView {

    public interface StickyAdapter {

        public static final int PINNED_HEADER_GONE = 0;

        public static final int PINNED_HEADER_VISIBLE = 1;

        public static final int PINNED_HEADER_PUSHED_UP = 2;

        int getPinnedHeaderState(int position);

        void configurePinnedHeader(View headerView, int position, int alpaha);
    }

    View mHeaderView;

    int mMeasuredWidth, mMeasuredHeight;

    boolean mDrawFlag = true;

    StickyAdapter mStikyAdapter;

    public StickyListView(Context context) {
        super(context);
        init();
    }

    public StickyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setPinnedHeader(View pHeader) {
        mHeaderView = pHeader;
        requestLayout();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        mStikyAdapter = (StickyAdapter) adapter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (null != mHeaderView) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mMeasuredWidth = mHeaderView.getMeasuredWidth();
            mMeasuredHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (null != mHeaderView) {
            mHeaderView.layout(0, 0, mMeasuredWidth, mMeasuredHeight);
            controlPinnedHeader(getFirstVisiblePosition());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (null != mHeaderView && mDrawFlag) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

    public void controlPinnedHeader(int position) {
        if (null == mHeaderView) {
            return;
        }
        int pinnedHeaderState = mStikyAdapter.getPinnedHeaderState(position);
        switch (pinnedHeaderState) {
        case StickyAdapter.PINNED_HEADER_GONE:
            mDrawFlag = false;
            break;
        case StickyAdapter.PINNED_HEADER_VISIBLE:
            mStikyAdapter.configurePinnedHeader(mHeaderView, position, 0);
            mDrawFlag = true;
            mHeaderView.layout(0, 0, mMeasuredWidth, mMeasuredHeight);
            break;
        case StickyAdapter.PINNED_HEADER_PUSHED_UP:
            mStikyAdapter.configurePinnedHeader(mHeaderView, position, 0);
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

    private void init() {
        View HeaderView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_header, this, false);
        setPinnedHeader(HeaderView);

    }

}
