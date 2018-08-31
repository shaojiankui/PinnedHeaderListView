package org.skyfox.pinnedheaderlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.skyfox.pinnedheaderlistview.IndexPath;

import java.util.ArrayList;
import java.util.List;

public class PinnedHeaderListView extends ListView implements OnScrollListener {

    private OnScrollListener mOnScrollListener;

    public static interface PinnedSectionedHeaderAdapter {
        public boolean isSectionHeader(int rawPosition);

        public IndexPath getIndexPathFowRawPosition(int rawPosition);

        public View getSectionHeaderView(int section, View convertView, ViewGroup parent);

        public int getSectionHeaderViewType(int section);

        public int getCount();

    }

    private PinnedSectionedHeaderAdapter mAdapter;
    private View mCurrentHeader;
    private int mCurrentHeaderViewType = 0;
    private float mHeaderOffset;
    private boolean mShouldPin = true;
    private int mCurrentSection = 0;
    private int mWidthMode;
    private int mHeightMode;

    public PinnedHeaderListView(Context context) {
        super(context);
        super.setOnScrollListener(this);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnScrollListener(this);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setOnScrollListener(this);
    }

    public void setPinHeaders(boolean shouldPin) {
        mShouldPin = shouldPin;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        mCurrentHeader = null;
        mAdapter = (PinnedSectionedHeaderAdapter) adapter;
        super.setAdapter(adapter);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

        if (mAdapter == null || mAdapter.getCount() == 0 || !mShouldPin || (firstVisibleItem < getHeaderViewsCount())) {
            mCurrentHeader = null;
            mHeaderOffset = 0.0f;
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                View header = getChildAt(i);
                if (header != null) {
                    header.setVisibility(VISIBLE);
                }
            }
            return;
        }

        firstVisibleItem -= getHeaderViewsCount();

        int section = mAdapter.getIndexPathFowRawPosition(firstVisibleItem).section;
        int viewType = mAdapter.getSectionHeaderViewType(section);
        mCurrentHeader = getSectionHeaderView(section, mCurrentHeaderViewType != viewType ? null : mCurrentHeader);
        ensurePinnedHeaderLayout(mCurrentHeader);
        mCurrentHeaderViewType = viewType;

        mHeaderOffset = 0.0f;

        for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
            if (mAdapter.isSectionHeader(i)) {
                View header = getChildAt(i - firstVisibleItem);
                float headerTop = header.getTop();
                float pinnedHeaderHeight = mCurrentHeader.getMeasuredHeight();
                header.setVisibility(VISIBLE);
                if (pinnedHeaderHeight >= headerTop && headerTop > 0) {
                    mHeaderOffset = headerTop - header.getHeight();
                } else if (headerTop <= 0) {
                    header.setVisibility(INVISIBLE);
                }
            }
        }

        invalidate();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    private View getSectionHeaderView(int section, View oldView) {
        boolean shouldLayout = section != mCurrentSection || oldView == null;

        View view = mAdapter.getSectionHeaderView(section, oldView, this);
        if (shouldLayout) {
            // a new section, thus a new header. We should lay it out again
            ensurePinnedHeaderLayout(view);
            mCurrentSection = section;
        }
        return view;
    }

    private void ensurePinnedHeaderLayout(View header) {
        if (header.isLayoutRequested()) {
            int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), mWidthMode);

            int heightSpec;
            ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
            if (layoutParams != null && layoutParams.height > 0) {
                heightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
            } else {
                heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            }
            header.measure(widthSpec, heightSpec);
            header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mAdapter == null || !mShouldPin || mCurrentHeader == null)
            return;
        int saveCount = canvas.save();
        canvas.translate(0, mHeaderOffset);
        canvas.clipRect(0, 0, getWidth(), mCurrentHeader.getMeasuredHeight()); // needed
        // for
        // <
        // HONEYCOMB
        mCurrentHeader.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        mHeightMode = MeasureSpec.getMode(heightMeasureSpec);
    }

    public static abstract class OnItemClickListener implements AdapterView.OnItemClickListener {
        public abstract void onItemClick(AdapterView<?> adapterView, View view, IndexPath indexPath, long id);

        public abstract void onSectionClick(AdapterView<?> adapterView, View view, int section, long id);

        public void onItemClick(AdapterView<?> adapterView, View view, int rawPosition, long id) {

        }
    }

    public void setOnItemClickListener(final PinnedHeaderListView.OnItemClickListener listener) {
        super.setOnItemClickListener(new android.widget.ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int rawPosition, long id) {
                SectionedBaseAdapter adapter;
                if (adapterView.getAdapter().getClass().equals(HeaderViewListAdapter.class)) {
                    HeaderViewListAdapter wrapperAdapter = (HeaderViewListAdapter) adapterView.getAdapter();
                    adapter = (SectionedBaseAdapter) wrapperAdapter.getWrappedAdapter();
                } else {
                    adapter = (SectionedBaseAdapter) adapterView.getAdapter();
                }
                rawPosition = rawPosition - getHeaderViewsCount();
                if (rawPosition < 0 || rawPosition >= adapter.getCount())//if have headerViews or FooterViews They didn't click event
                    return;
                IndexPath indexPath = adapter.getIndexPathFowRawPosition(rawPosition);

                if (indexPath.row == -1) {
                    listener.onSectionClick(adapterView, view, indexPath.section, id);
                } else {
                    listener.onItemClick(adapterView, view, indexPath, id);
                }
            }
        });


    }

    public static abstract class OnItemLongClickListener implements AdapterView.OnItemLongClickListener {
        public abstract boolean onItemLongClick(AdapterView<?> adapterView, View view, IndexPath indexPath, long id);

        public abstract boolean onSectionLongClick(AdapterView<?> adapterView, View view, int section, long id);

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int rawPosition, long id) {
            return false;
        }
    }


    public void setOnItemLongClickListener(final PinnedHeaderListView.OnItemLongClickListener listener) {
        super.setOnItemLongClickListener(new android.widget.ListView.OnItemLongClickListener() {


            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int rawPosition, long id) {
                SectionedBaseAdapter adapter;
                if (adapterView.getAdapter().getClass().equals(HeaderViewListAdapter.class)) {
                    HeaderViewListAdapter wrapperAdapter = (HeaderViewListAdapter) adapterView.getAdapter();
                    adapter = (SectionedBaseAdapter) wrapperAdapter.getWrappedAdapter();
                } else {
                    adapter = (SectionedBaseAdapter) adapterView.getAdapter();
                }
                rawPosition = rawPosition - getHeaderViewsCount();
                if (rawPosition < 0 || rawPosition >= adapter.getCount())//if have headerViews or FooterViews They didn't click event
                    return false;
                IndexPath indexPath = adapter.getIndexPathFowRawPosition(rawPosition);


                if (indexPath.row == -1) {
                    return listener.onSectionLongClick(adapterView, view, indexPath.section, id);
                } else {
                    return listener.onItemLongClick(adapterView, view, indexPath, id);
                }
            }
        });

    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
    }

    //根据section  row 获取原始listview position
    public void setSelection(int section, int row) {
        setSelectionIndexPath(new IndexPath(section, row, -1));
    }

    //根据IndexPath获取原始listview position
    public void setSelectionIndexPath(IndexPath indexPath) {
        SectionedBaseAdapter adapter = (SectionedBaseAdapter) mAdapter;

        int originalosition = adapter.getOriginalPosition(indexPath) + getHeaderViewsCount();
        super.setSelection(originalosition);
    }

    public List<IndexPath> getCheckedIndexPaths() {
        SparseBooleanArray checkedItemPositions = super.getCheckedItemPositions();

        List<IndexPath> list = new ArrayList<>();

        for (int i = 0; i < checkedItemPositions.size(); i++) {
            int rawPosition = checkedItemPositions.keyAt(i);
            rawPosition = rawPosition - getHeaderViewsCount();

            boolean value = checkedItemPositions.valueAt(i);
            if (value && !mAdapter.isSectionHeader(rawPosition)) {
                IndexPath indexPath = mAdapter.getIndexPathFowRawPosition(rawPosition);
                if (indexPath != null && indexPath.row >= 0 && indexPath.section >= 0) {
                    list.add(indexPath);
                }
            }
        }
        return list;
    }
}
