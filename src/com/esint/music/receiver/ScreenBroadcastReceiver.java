package com.esint.music.receiver;

import com.esint.music.activity.LockActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**  
* 类名称：ScreenBroadcastReceiver   
* 类描述： 此广播是用来监听手机开屏和锁屏的状态，必须在service中注册该广播  
* 创建人：bai   
* 创建时间：2016-3-2 上午8:42:24      
*/
public class ScreenBroadcastReceiver extends BroadcastReceiver {
	private String action = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		action = intent.getAction();
		if (action.equals("start_notifition")) {


		} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
			// 锁屏
			Intent intent1 = new Intent(context, LockActivity.class);
			intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent1);
		}
	}
}
