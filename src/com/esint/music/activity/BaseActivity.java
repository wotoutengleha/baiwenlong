package com.esint.music.activity;

import com.esint.music.service.MusicPlayService;
import com.esint.music.service.MusicPlayService.PlayBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

public class BaseActivity extends FragmentActivity implements OnClickListener {

	public MusicPlayService musicPlayService;
	private boolean isBind = false;

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicPlayService = null;
			isBind = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			PlayBinder playBinder = (PlayBinder) service;
			musicPlayService = playBinder.getPlayService();
		}
	};

	// 绑定服务
	public void bindService() {
		if (!isBind) {
			Intent intent = new Intent(this, MusicPlayService.class);
			bindService(intent, connection, Context.BIND_AUTO_CREATE);
			isBind = true;
		}
	}

	// 解除绑定服务
	public void unBindService() {
		if (isBind == true) {
			unbindService(connection);
			isBind = false;
		}
	}

	@Override
	public void onClick(View v) {
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
}
