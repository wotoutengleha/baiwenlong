package com.esint.music.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**   
* 类名称：CustomerListView   
* 类描述：搜索界面的listView   
* 创建人：bai   
* 创建时间：2016-3-28 上午9:45:31         
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
