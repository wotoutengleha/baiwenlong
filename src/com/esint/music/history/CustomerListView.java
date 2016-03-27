package com.esint.music.history;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
/**
 * 自定义ListView
 * @author 周辉
 * @version 1.4
 * @created 2014-9-1
 */
public class CustomerListView extends ListView {
	public CustomerListView(Context context) {
        super(context);
    }
 
    public CustomerListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    public CustomerListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
 
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
 

}
