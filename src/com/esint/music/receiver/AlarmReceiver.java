package com.esint.music.receiver;

import com.esint.music.utils.ActivityCollectUtil;
import com.esint.music.utils.Constant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**   
* �����ƣ�AlarmReceiver   
* �����������յ��˹㲥���˳�APP   
* �����ˣ�bai   
* ����ʱ�䣺2016-3-20 ����8:12:18         
*/
public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(intent.getAction().equals(Constant.ALARM_CLOCK_BROADCAST)){
			// �˳�APP
			ActivityCollectUtil.finishAllActi();
		}
	}
}
