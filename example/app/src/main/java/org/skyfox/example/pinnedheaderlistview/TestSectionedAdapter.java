package org.skyfox.example.pinnedheaderlistview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.skyfox.pinnedheaderlistview.IndexPath;
import org.skyfox.pinnedheaderlistview.SectionedBaseAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class TestSectionedAdapter extends SectionedBaseAdapter {
    private Context context;
    private List list;
    public LinkedHashMap linkedHashMap = new LinkedHashMap();

    public TestSectionedAdapter(Context context, List list) {
        this.context = context;

        if (list == null) {
            list = new ArrayList();
        }
        this.list = list;
        this.linkedHashMap = sortList(this.list);
    }

    public void setDataSource(List list) {
        // TODO Auto-generated method stub
        if (list == null) {
            list = new ArrayList();
        }
        this.list = list;
        this.linkedHashMap = sortList(this.list);
        this.notifyDataSetChanged();
    }

    public static LinkedHashMap<String, ArrayList> sortList(List<Contact> list) {
        LinkedHashMap<String, ArrayList> sortedMap = new LinkedHashMap<>();
        for (Contact customer : list) {
            String key = PinyinUtils.getFirstSpell(customer.name);

            ArrayList<Contact> tempList = sortedMap.get(key);
            /*如果取不到数据,那么直接new一个空的ArrayList**/
            if (tempList == null) {
                tempList = new ArrayList<>();
                tempList.add(customer);
                sortedMap.put(key, tempList);
            } else {
                /*某个sku之前已经存放过了,则直接追加数据到原来的List里**/
                tempList.add(customer);
            }
        }

        return sortedMap;
    }

    @Override
    public Object getItem(IndexPath indexPath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(IndexPath indexPath) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getSectionCount() {
        return this.linkedHashMap.size();
    }

    @Override
    public int getCountForSection(int section) {
        // TODO Auto-generated method stub
        Object[] keys = linkedHashMap.keySet().toArray();
        String key = (String) keys[section];
        ArrayList arrayList = (ArrayList) linkedHashMap.get(key);
        return arrayList.size();
    }

    @Override
    public View getItemView(IndexPath indexPath, View convertView, ViewGroup parent) {
        LinearLayout layout = null;
        if (convertView == null) {
//            LayoutInflater inflator = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            layout = (LinearLayout) inflator.inflate(R.layout.list_item, null);

            layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.list_item, null);
        } else {
            layout = (LinearLayout) convertView;
        }
        Object[] keys = linkedHashMap.keySet().toArray();
        String key = (String) keys[indexPath.section];
        ArrayList arrayList = (ArrayList) linkedHashMap.get(key);

        Contact contact = (Contact) arrayList.get(indexPath.row);

        ((TextView) layout.findViewById(R.id.textItem)).setText(contact.name);
        return layout;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        LinearLayout layout = null;
        if (convertView == null) {
//            LayoutInflater inflator = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            layout = (LinearLayout) inflator.inflate(R.layout.header_item, null);
            layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.header_item, null);

        } else {
            layout = (LinearLayout) convertView;
        }
        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, //宽度
                PixelDensity.dip2px(this.context, 70));//高度
        layout.setLayoutParams(layoutParams);

        Object[] keys = linkedHashMap.keySet().toArray();
        String key = (String) keys[section];

        ((TextView) layout.findViewById(R.id.textItem)).setText("" + key);
        return layout;
    }

}
