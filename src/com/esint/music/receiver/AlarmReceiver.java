package com.esint.music.receiver;

import com.esint.music.utils.ActivityCollectUtil;
import com.esint.music.utils.Constant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**   
* 类名称：AlarmReceiver   
* 类描述：接收到此广播后退出APP   
* 创建人：bai   
* 创建时间：2016-3-20 下午8:12:18         
*/
public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(intent.getAction().equals(Constant.ALARM_CLOCK_BROADCAST)){
			// 退出APP
			ActivityCollectUtil.finishAllActi();
		}
	}
}
