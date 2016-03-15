package com.esint.music.utils;

import com.lidroid.xutils.DbUtils;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class MyApplication extends Application {
	// Ĭ��û����������
	public static boolean mIsSleepClockSetting = false;
	private static Context context;
	public DbUtils dbUtils;

	@Override
	public void onCreate() {
		super.onCreate();
		dbUtils = DbUtils.create(getApplicationContext(), Constant.DB_NAME);
		context = getApplicationContext();
	}
	// ��ȡȫ�ֵ�context
	public static Context getContext() {
		return context;
	}

}
