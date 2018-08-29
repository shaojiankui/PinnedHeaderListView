package org.skyfox.pinnedheaderlistview;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public abstract class SectionedBaseAdapter extends BaseAdapter implements PinnedHeaderListView.PinnedSectionedHeaderAdapter {

    private static int HEADER_VIEW_TYPE = 0;
    private static int ITEM_VIEW_TYPE = 0;
    private SparseArray<IndexPath> mIndexPathCache;

    /**
     * Holds the calculated values of @{link getRowInSectionForPosition}
     */
    private SparseArray<Integer> mSectionRowCache;
    /**
     * Holds the calculated values of @{link getSectionForPosition}
     */
    private SparseArray<Integer> mSectionCache;
    /**
     * Holds the calculated values of @{link getCountForSection}
     */
    private SparseArray<Integer> mSectionRowCountCache;

    /**
     * Caches the item count
     */
    private int mCount;
    /**
     * Caches the section count
     */
    private int mSectionCount;

    public SectionedBaseAdapter() {
        super();
        mIndexPathCache  = new SparseArray<IndexPath>();
        mSectionCache = new SparseArray<Integer>();
        mSectionRowCache = new SparseArray<Integer>();
        mSectionRowCountCache = new SparseArray<Integer>();
        mCount = -1;
        mSectionCount = -1;
    }

    @Override
    public void notifyDataSetChanged() {
        mIndexPathCache.clear();
        mSectionCache.clear();
        mSectionRowCache.clear();
        mSectionRowCountCache.clear();
        mCount = -1;
        mSectionCount = -1;
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        mIndexPathCache.clear();
        mSectionCache.clear();
        mSectionRowCache.clear();
        mSectionRowCountCache.clear();
        mCount = -1;
        mSectionCount = -1;
        super.notifyDataSetInvalidated();
    }

    @Override
    public final int getCount() {
        if (mCount >= 0) {
            return mCount;
        }
        int count = 0;
        for (int i = 0; i < internalGetSectionCount(); i++) {
            count += internalGetRowCountForSection(i);
            count++; // for the header view
        }
        mCount = count;
        return count;
    }

    @Override
    public final Object getItem(int rawPosition) {
        return getItem(getIndexPathFowRawPosition(rawPosition));
    }

    @Override
    public final long getItemId(int rawPosition) {
        return getItemId(getIndexPathFowRawPosition(rawPosition));
    }

    @Override
    public final View getView(int rawPosition, View convertView, ViewGroup parent) {
        if (isSectionHeader(rawPosition)) {
            return getSectionHeaderView(getIndexPathFowRawPosition(rawPosition).section, convertView, parent);
        }
        return getItemView(getIndexPathFowRawPosition(rawPosition), convertView, parent);
    }

    @Override
    public final int getItemViewType(int rawPosition) {
        if (isSectionHeader(rawPosition)) {
            return getItemViewTypeCount() + getSectionHeaderViewType(getIndexPathFowRawPosition(rawPosition).section);
        }
        return getItemViewType(getIndexPathFowRawPosition(rawPosition));
    }

    @Override
    public final int getViewTypeCount() {
        return getItemViewTypeCount() + getSectionHeaderViewTypeCount();
    }

    public IndexPath getIndexPathFowRawPosition(int rawPosition){
        IndexPath indexPath = mIndexPathCache.get(rawPosition);
        if (indexPath != null){
            return   indexPath;
        }
        int sectionStart = 0;
        for (int section = 0; section < internalGetSectionCount(); section++) {
            int sectionCount = internalGetRowCountForSection(section);
            int sectionEnd = sectionStart + sectionCount + 1;
            if (rawPosition >= sectionStart && rawPosition < sectionEnd) {
                int row = rawPosition - sectionStart - 1;
                return new IndexPath(section,row,rawPosition);
            }
            sectionStart = sectionEnd;
        }
        return null;
    }

    //根据section  position 获取原始listview position
    public int getOriginalPosition(IndexPath indexPath) {
        int indeTotal = 0;
        for (int i =0;i<indexPath.section;i++){
            int indexCount =  mSectionRowCountCache.get(i);
            indeTotal += indexCount;
            indeTotal++;
        }
        indeTotal += indexPath.row;
        return indeTotal;
    }
    public final boolean isSectionHeader(int rawPosition) {
        int sectionStart = 0;
        for (int i = 0; i < internalGetSectionCount(); i++) {
            if (rawPosition == sectionStart) {
                return true;
            } else if (rawPosition < sectionStart) {
                return false;
            }
            sectionStart += internalGetRowCountForSection(i) + 1;
        }
        return false;
    }

    public int getItemViewType(IndexPath indexPath) {
        return ITEM_VIEW_TYPE;
    }

    public int getItemViewTypeCount() {
        return 1;
    }

    public int getSectionHeaderViewType(int section) {
        return HEADER_VIEW_TYPE;
    }

    public int getSectionHeaderViewTypeCount() {
        return 1;
    }

    public abstract Object getItem(IndexPath indexPath);

    public abstract long getItemId(IndexPath indexPath);

    public abstract int getSectionCount();

    public abstract int getCountForSection(int section);

    public abstract View getItemView(IndexPath indexPath, View convertView, ViewGroup parent);

    public abstract View getSectionHeaderView(int section, View convertView, ViewGroup parent);

    private int internalGetRowCountForSection(int section) {
        Integer cachedSectionCount = mSectionRowCountCache.get(section);
        if (cachedSectionCount != null) {
            return cachedSectionCount;
        }
        int sectionCount = getCountForSection(section);
        mSectionRowCountCache.put(section, sectionCount);
        return sectionCount;
    }

    private int internalGetSectionCount() {
        if (mSectionCount >= 0) {
            return mSectionCount;
        }
        mSectionCount = getSectionCount();
        return mSectionCount;
    }

}
