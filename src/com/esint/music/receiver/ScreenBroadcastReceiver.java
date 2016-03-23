package com.esint.music.receiver;

import com.esint.music.activity.LockActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**  
* �����ƣ�ScreenBroadcastReceiver   
* �������� �˹㲥�����������ֻ�������������״̬��������service��ע��ù㲥  
* �����ˣ�bai   
* ����ʱ�䣺2016-3-2 ����8:42:24      
*/
public class ScreenBroadcastReceiver extends BroadcastReceiver {
	private String action = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		action = intent.getAction();
		if (action.equals("start_notifition")) {


		} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
			// ����
			Intent intent1 = new Intent(context, LockActivity.class);
			intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent1);
		}
	}
}
