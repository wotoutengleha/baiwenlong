package com.esint.music.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.esint.music.utils.Constant;
import com.esint.music.utils.ShakeDetector;
import com.esint.music.utils.ShakeDetector.OnShakeListener;
import com.esint.music.utils.SharedPrefUtil;

public class NotificationService extends Service implements OnShakeListener{
	private SharedPrefUtil prefUtil;
	private ShakeDetector mShakeDetector;
	private NotificationManager mNotificationManager;
	private MusicPlayBroadcast musicPlayBroadcast;
	/** 当前是否正在播放 */
	private boolean mIsPlaying;
	/** 在设置界面是否开启了摇一摇的监听 */
	public boolean mShake;

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
		prefUtil = new SharedPrefUtil(this);
		mShakeDetector = new ShakeDetector(this);
		mShakeDetector.setOnShakeListener(this);
		musicPlayBroadcast = new MusicPlayBroadcast();
		IntentFilter filter1 = new IntentFilter();
		filter1.addAction(Constant.BROADCAST_SHAKE);
		registerReceiver(musicPlayBroadcast, filter1);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;

	}

	@Override
	public void onShake() {
	}
	private class MusicPlayBroadcast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Constant.BROADCAST_SHAKE)){
				mShake = intent.getBooleanExtra(Constant.SHAKE_ON_OFF, false);
				boolean shake = prefUtil.getShake(NotificationService.this, false);
				if(mShake){
					mShakeDetector.start();
				}
				if(shake){
					mShakeDetector.start();
				}else if(!mShake) {
					mShakeDetector.stop();
				}
			}
		}
		
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(musicPlayBroadcast!=null){
			unregisterReceiver(musicPlayBroadcast);
		}
	}

}
