package com.esint.music.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

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
				activity.finish();
			}
		}
	}

}
