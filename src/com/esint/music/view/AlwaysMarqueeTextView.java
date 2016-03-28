package com.esint.music.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/*   
 *    
 * 类名称：AlwaysMarqueeTextView   
 * 类描述： 跑马灯效果的textView  
 * 创建人：bai 
 * 创建时间：2016-1-12 下午1:17:14   
 *        
 */
public class AlwaysMarqueeTextView extends TextView {

	public AlwaysMarqueeTextView(Context context) {
		super(context);
	}

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean isFocused() {
		return true;
	}

}
