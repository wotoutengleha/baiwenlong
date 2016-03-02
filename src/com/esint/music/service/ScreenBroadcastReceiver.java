package com.esint.music.service;

import com.esint.music.activity.LockActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
		if (Intent.ACTION_SCREEN_ON.equals(action)) {
			// 开屏
			Log.e("开屏了", "开屏了");
		} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
			// 锁屏
			Log.e("锁屏了", "锁屏了");
			Intent intent1 = new Intent(context, LockActivity.class);
			intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent1);
		} else if (Intent.ACTION_USER_PRESENT.equals(action)) {
			// 解锁
			Log.e("解锁了", "锁屏了");
		}
	}
}
