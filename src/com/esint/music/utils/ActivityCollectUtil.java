package com.esint.music.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/**   
* 类名称：ActivityCollectUtil   
* 类描述：退出程勋的Activity的工具类  
* 创建人：bai   
* 创建时间：2016-3-12 下午9:40:54         
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
