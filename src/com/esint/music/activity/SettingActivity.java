package com.esint.music.activity;

import name.teze.layout.lib.SwipeBackActivity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.R.color;
import com.esint.music.utils.ActivityCollectUtil;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MyApplication;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.view.SetupBGButton;
import com.esint.music.view.SetupColorBGButton;
import com.esint.music.view.SetupDesktoplyricsButton;
import com.esint.music.view.SetupLockScreenButton;
import com.esint.music.view.SetupWifiButton;

public class SettingActivity extends SwipeBackActivity implements
		OnClickListener {

	private SetupBGButton[] soundBGButton;// 音质按钮
	private int soundIndex = Constant.soundIndex;// 音质索引
	private SetupColorBGButton[] colorBGButton;// 标题颜色
	private String[] colorBGColorStr = Constant.colorBGColorStr;// 标题颜色集合
	private int colorIndex = Constant.colorIndex;// 标题颜色索引
	private SetupBGButton sleepButton;// 是否开启睡眠定时
	private SetupBGButton shakeButton;// 是否开启摇一摇模式
	private SetupBGButton otherControl;//辅助操作
	private SetupWifiButton WifiButton;// 仅WiFi按钮
	private SetupDesktoplyricsButton deskLrcButton;// 桌面歌词
	private SetupLockScreenButton lockLrcButton;// 锁屏歌词

	private ImageView backImv;
	private RelativeLayout settingActionBar;
	public static Handler mHandler = new Handler();
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);
		ActivityCollectUtil.addActivity(this);
		initView();
		initData();
		initComponent();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollectUtil.removeActivity(this);
	}

	private void initView() {
		backImv = (ImageView) findViewById(R.id.backBtn);
		settingActionBar = (RelativeLayout) findViewById(R.id.actionbar);
		sleepButton = (SetupBGButton) findViewById(R.id.sleep);
		shakeButton = (SetupBGButton) findViewById(R.id.shake);
		otherControl = (SetupBGButton) findViewById(R.id.easytouch);
		WifiButton = (SetupWifiButton) findViewById(R.id.wifiBtn);
		deskLrcButton = (SetupDesktoplyricsButton) findViewById(R.id.deskBtn);
		lockLrcButton = (SetupLockScreenButton) findViewById(R.id.lockBtn);

		backImv.setOnClickListener(this);
		sleepButton.setOnClickListener(this);
		shakeButton.setOnClickListener(this);
		WifiButton.setOnClickListener(this);
		otherControl.setOnClickListener(this);
		deskLrcButton.setOnClickListener(this);
		lockLrcButton.setOnClickListener(this);
		if (MyApplication.mIsSleepClockSetting == true) {
			sleepButton.setSelect(true);
		}
		boolean shakeFlag = SharedPrefUtil.getBoolean(SettingActivity.this,
				Constant.SHAKE_ON_OFF, false);
		boolean isWifi = SharedPrefUtil.getBoolean(SettingActivity.this,
				Constant.IS_WIFI, false);
		boolean isDeskLrc = SharedPrefUtil.getBoolean(SettingActivity.this,
				Constant.DESK_LRC, false);
		boolean isLockLrc = SharedPrefUtil.getBoolean(SettingActivity.this,
				Constant.LOCK_LRC, false);

		if (shakeFlag == true) {
			shakeButton.setSelect(true);
			Message message  = mHandler.obtainMessage(Constant.WHAT_SHAKE, true);
			message.sendToTarget();
		} else {
			shakeButton.setSelect(false);
		}
		if (isWifi == true) {
			WifiButton.setSelect(true);
		} else {
			WifiButton.setSelect(false);
		}
		if (isDeskLrc == true) {
			deskLrcButton.setSelect(true);
		} else {
			deskLrcButton.setSelect(false);
		}
		if (isLockLrc == true) {
			lockLrcButton.setSelect(true);
		} else {
			lockLrcButton.setSelect(false);
		}
	}

	private void initData() {
		// 注册广播
//		Intent intent = new Intent();
//		intent.setAction(Constant.ALARM_CLOCK_BROADCAST);
//		sendBroadcast(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		int colorIndex = SharedPrefUtil.getInt(SettingActivity.this,
				Constant.COLOR_INDEX, -1);
		int colorIndexSelect = SharedPrefUtil.getInt(SettingActivity.this,
				Constant.COLOR_INDEX_SELECT, -1);
		if (colorIndexSelect == -1) {
			colorBGButton[0].setSelect(true);
		} else {
			colorBGButton[colorIndexSelect].setSelect(true);
		}
		switch (colorIndex) {
		case 0:
			settingActionBar.setBackgroundResource(color.tianyilan);
			break;
		case 1:
			settingActionBar.setBackgroundResource(color.hidden_bitterness);
			break;
		case 2:
			settingActionBar.setBackgroundResource(color.gorgeous);
			break;
		case 3:
			settingActionBar.setBackgroundResource(color.romance);
			break;
		case 4:
			settingActionBar.setBackgroundResource(color.sunset);
			break;
		case 5:
			settingActionBar.setBackgroundResource(color.warm_colour);
			break;

		default:
			settingActionBar.setBackgroundResource(color.holo_blue_light);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backBtn: {
			finish();
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			break;
		}
		case R.id.sleep: {
			showSleepDialog();
			break;
		}
		case R.id.shake: {
			if (shakeButton.isSelect()) {
				shakeButton.setSelect(false);
				Toast.makeText(SettingActivity.this, "关闭摇一摇", 0).show();
			} else {
				Toast.makeText(SettingActivity.this, "开启摇一摇", 0).show();
				shakeButton.setSelect(true);
			}
			SharedPrefUtil.setBoolean(SettingActivity.this,
					Constant.SHAKE_ON_OFF, shakeButton.isSelect());
			Message message  = mHandler.obtainMessage(Constant.WHAT_SHAKE, shakeButton.isSelect());
			message.sendToTarget();
			break;
		}
		case R.id.wifiBtn: {
			if (WifiButton.isSelect()) {
				WifiButton.setSelect(false);
			} else {
				WifiButton.setSelect(true);
			}
			SharedPrefUtil.setBoolean(SettingActivity.this,
					Constant.IS_WIFI, WifiButton.isSelect());
		}
			break;
		case R.id.deskBtn: {
			if (deskLrcButton.isSelect()) {
				deskLrcButton.setSelect(false);
			} else {
				deskLrcButton.setSelect(true);
			}
			SharedPrefUtil.setBoolean(SettingActivity.this,
					Constant.DESK_LRC, deskLrcButton.isSelect());
		}
			break;
		case R.id.lockBtn: {
			if (lockLrcButton.isSelect()) {
				lockLrcButton.setSelect(false);
			} else {
				lockLrcButton.setSelect(true);
			}
			SharedPrefUtil.setBoolean(SettingActivity.this,
					Constant.LOCK_LRC, lockLrcButton.isSelect());
		}
			break;
		case R.id.easytouch:{
			if (otherControl.isSelect()) {
				otherControl.setSelect(false);
			} else {
				otherControl.setSelect(true);
			}
			break;
		}
		}
	}

	private void showSleepDialog() {
		if (MyApplication.mIsSleepClockSetting) {
			cancleSleepTime();
			Toast.makeText(SettingActivity.this, "已取消睡眠模式", 0).show();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(
				SettingActivity.this);
		final AlertDialog dialog = builder.create();
		View view = View.inflate(SettingActivity.this,
				R.layout.dialog_sleep_time, null);
		dialog.setView(view, 0, 0, 0, 0);
		final EditText etTime = (EditText) view.findViewById(R.id.time_et);
		Button btnOk = (Button) view.findViewById(R.id.ok_btn);
		Button btnCancle = (Button) view.findViewById(R.id.cancle_btn);

		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String sleepTime = etTime.getText().toString();
				// 判断输入的内容
				if (TextUtils.isEmpty(sleepTime)
						|| Integer.parseInt(sleepTime) == 0) {
					Toast.makeText(SettingActivity.this, "输入无效", 0).show();
					return;
				}
				dialog.dismiss();
				setSleepTime(sleepTime);
				sleepButton.setSelect(true);
			}

		});
		btnCancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	// 设置睡眠的时间
	private void setSleepTime(String sleepTime) {
		Intent intent = new Intent(Constant.ALARM_CLOCK_BROADCAST);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				SettingActivity.this, 0, intent, 0);
		// 设置时间后退出程序
		int time = Integer.parseInt(sleepTime);
		long longTime = time * 60 * 1000L;
		AlarmManager am = (AlarmManager) SettingActivity.this
				.getSystemService(FragmentActivity.ALARM_SERVICE);
		am.set(AlarmManager.RTC, System.currentTimeMillis() + longTime,
				pendingIntent);
		MyApplication.mIsSleepClockSetting = true;
		Toast.makeText(SettingActivity.this, "将在" + sleepTime + "分钟后退出软件",
				Toast.LENGTH_SHORT).show();

	}

	// 取消闹钟的时间
	private void cancleSleepTime() {
		Intent intent = new Intent(Constant.ALARM_CLOCK_BROADCAST);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				SettingActivity.this, 0, intent, 0);
		AlarmManager am = (AlarmManager) SettingActivity.this
				.getSystemService(FragmentActivity.ALARM_SERVICE);
		am.cancel(pendingIntent);
		MyApplication.mIsSleepClockSetting = false;
		sleepButton.setSelect(false);
	}


	private void initComponent() {
		soundBGButton = new SetupBGButton[6];
		int i = 0;
		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound0);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound1);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound2);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound3);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound4);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound5);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[soundIndex].setSelect(true);

		i = 0;
		colorBGButton = new SetupColorBGButton[colorBGColorStr.length];

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color0);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color1);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color2);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color3);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color4);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color5);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		// colorBGButton[colorIndex].setSelect(true);
	}

	private OnClickListener soundOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int index = 0;
			switch (v.getId()) {
			case R.id.sound0:
				index = 0;
				break;
			case R.id.sound1:
				index = 1;
				break;
			case R.id.sound2:
				index = 2;
				break;
			case R.id.sound3:
				index = 3;
				break;
			case R.id.sound4:
				index = 4;
				break;
			case R.id.sound5:
				index = 5;
				break;
			}
			if (soundIndex == index) {
				return;
			}
			soundBGButton[soundIndex].setSelect(false);
			soundIndex = index;
			soundBGButton[soundIndex].setSelect(true);

			Constant.soundIndex = soundIndex;
		}
	};

	private OnClickListener colorOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int index = 0;
			switch (v.getId()) {
			case R.id.color0:
				index = 0;
				settingActionBar.setBackgroundResource(color.tianyilan);// 优雅
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			case R.id.color1:
				index = 1;
				settingActionBar.setBackgroundResource(color.hidden_bitterness);// 幽怨
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			case R.id.color2:
				index = 2;
				settingActionBar.setBackgroundResource(color.gorgeous);// 绚丽
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			case R.id.color3:
				index = 3;
				settingActionBar.setBackgroundResource(color.romance);// 浪漫
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			case R.id.color4:
				index = 4;
				settingActionBar.setBackgroundResource(color.sunset);// 夕阳
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			case R.id.color5:
				index = 5;
				settingActionBar.setBackgroundResource(color.warm_colour);// 暖色
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			}
			if (colorIndex == index) {
				return;
			}
			colorBGButton[colorIndex].setSelect(false);
			colorIndex = index;
			SharedPrefUtil.setInt(SettingActivity.this,
					Constant.COLOR_INDEX_SELECT, colorIndex);
			colorBGButton[colorIndex].setSelect(true);
		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
}
