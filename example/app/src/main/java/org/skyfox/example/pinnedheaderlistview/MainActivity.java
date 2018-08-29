package org.skyfox.example.pinnedheaderlistview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import org.skyfox.pinnedheaderlistview.IndexPath;
import org.skyfox.pinnedheaderlistview.PinnedHeaderListView;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.skyfox.example.pinnedheaderlistview.ChineseName.randomName;

public class MainActivity extends Activity {
    private AssortView addressbook_right;
    /**
     * 泡泡
     */
    private PopupWindow popupWindow;
    TestSectionedAdapter sectionedAdapter;
    PinnedHeaderListView listView;
    private List<Contact> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (PinnedHeaderListView) findViewById(R.id.pinnedListView);
        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout header1 = (LinearLayout) inflator.inflate(R.layout.list_item, null);
        ((TextView) header1.findViewById(R.id.textItem)).setText("HEADER 1");
        LinearLayout header2 = (LinearLayout) inflator.inflate(R.layout.list_item, null);
        ((TextView) header2.findViewById(R.id.textItem)).setText("HEADER 2");
        LinearLayout footer = (LinearLayout) inflator.inflate(R.layout.list_item, null);
        ((TextView) footer.findViewById(R.id.textItem)).setText("FOOTER");
//        listView.addHeaderView(header1);
//        listView.addHeaderView(header2);
//        listView.addFooterView(footer);
        sectionedAdapter = new TestSectionedAdapter(this, null);
        listView.setAdapter(sectionedAdapter);


        listView.setOnItemClickListener(new PinnedHeaderListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, IndexPath indexPath, long id) {
                Object[] keys = sectionedAdapter.linkedHashMap.keySet().toArray();
                String key = (String) keys[indexPath.section];
                ArrayList arrayList = (ArrayList) sectionedAdapter.linkedHashMap.get(key);
                Contact contact = (Contact) arrayList.get(indexPath.row);
                Toast.makeText(MainActivity.this, contact.name, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSectionClick(AdapterView<?> adapterView, View view, int section, long id) {

            }
        });


        listView.setOnItemLongClickListener(new PinnedHeaderListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, IndexPath indexPath, long id) {
                Object[] keys = sectionedAdapter.linkedHashMap.keySet().toArray();
                String key = (String) keys[indexPath.section];
                final ArrayList arrayList = (ArrayList) sectionedAdapter.linkedHashMap.get(key);
                final Contact contact = (Contact) arrayList.get(indexPath.row);


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage("确定删除?" + contact.name);
                builder.setTitle("提示");

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Contact.delete(contact);
                        arrayList.remove(contact);
                        sectionedAdapter.notifyDataSetChanged();
                        listView.invalidate();
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });


                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }

            @Override
            public boolean onSectionLongClick(AdapterView<?> adapterView, View view, int section, long id) {
                return false;
            }
        });

        addressbook_right = (AssortView) findViewById(R.id.addressbook_right);
        // 字母按键回调
        addressbook_right.setOnTouchAssortListener(new AssortView.OnTouchAssortListener() {
            LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layoutView = inflator.inflate(R.layout.addressbook_middle_alert_dialog, null);
            TextView text = (TextView) layoutView.findViewById(R.id.addressbook_content);

            @Override
            public void onTouchAssortListener(String str) {
                //弹出泡泡
                if (popupWindow != null) {
                    text.setText(str);
                } else {
                    popupWindow = new PopupWindow(layoutView, PixelDensity.dip2px(MainActivity.this, 80), PixelDensity.dip2px(MainActivity.this, 80), false);
                    // 显示在Activity的根视图中心
                    popupWindow.showAtLocation(MainActivity.this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                }
                text.setText(str);

                //查找对应section 选中
                Object[] keys = sectionedAdapter.linkedHashMap.keySet().toArray();
                for (int i = 0; i < keys.length; i++) {
                    String findKey = (String) keys[i];
                    if (str.equalsIgnoreCase(findKey)) {
                        listView.setSelection(i, 0); // 选择到首字母出现的顶部位置
                        return;
                    }
                }

            }

            @Override
            public void onTouchAssortUP() {
                if (popupWindow != null) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
            }
        });
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadData();
            }
        });
        reloadData();
    }

    private void reloadData() {
        List list = new ArrayList();

        //排序
        for (int i = 0; i < 200; i++) {
            Contact contact = new Contact();
            contact.name = randomName(true);
            list.add(contact);
        }

        Comparator<Contact> byName = new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                Log.i("compare", "compare");
                Collator instance = Collator.getInstance(Locale.CHINA);
                return instance.compare(o1.name, o2.name);
            }
        };
        Collections.sort(list, byName);

        this.list = list;

        sectionedAdapter.setDataSource(list);
        sectionedAdapter.notifyDataSetChanged();
//        listView.invalidate();
    }

    @Override
    public void onStop() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
        super.onDestroy();
    }


}
