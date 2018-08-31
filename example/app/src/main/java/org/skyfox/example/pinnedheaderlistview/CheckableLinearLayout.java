package org.skyfox.example.pinnedheaderlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout  implements Checkable {
    private boolean mChecked;
    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
        if (checked){
            setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }else {
            setBackgroundColor(getResources().getColor(R.color.white));
        }
//        Color.parseColor()
    }
    @Override
    public boolean isChecked() {
        return mChecked;
    }
    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}
