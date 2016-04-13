package com.esint.music.utils;

import java.util.ArrayList;
import java.util.List;

import org.jaudiotagger.audio.mp4.atom.Mp4FtypBox.Brand;

import android.app.Activity;

import com.esint.music.activity.ScanMusicActivity.ScanSdReceiver;
import com.esint.music.receiver.ScreenBroadcastReceiver;

/**   
* �����ƣ�ActivityCollectUtil   
* ���������˳���ѫ��Activity�Ĺ�����  
* �����ˣ�bai   
* ����ʱ�䣺2016-3-12 ����9:40:54         
*/
public class ActivityCollectUtil {

	public static List<Activity> activities = new ArrayList<Activity>();

	public static void addActivity(Activity activity) {
		activities.add(activity);
	}

	public static void removeActivity(Activity activity) {
		activities.remove(activity);
	}

	public static void finishAllActi() {
		for (Activity activity : activities) {
			if (!activity.isFinishing()) {
				ScreenBroadcastReceiver broadcastReceiver = new ScreenBroadcastReceiver();
				ScanSdReceiver scanSdReceiver = new ScanSdReceiver();
				if (broadcastReceiver != null) {
					// �����Ļ�Ĺ㲥
					MyApplication.getContext().unregisterReceiver(
							broadcastReceiver);
				}
				if (scanSdReceiver != null) {
					MyApplication.getContext().unregisterReceiver(
							scanSdReceiver);
				}
				activity.finish();
			}
		}
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}
