package com.esint.music.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import com.esint.music.R;
import com.esint.music.model.Mp3Info;
import com.esint.music.service.MusicPlayService;
import com.esint.music.service.MusicPlayService.PlayBinder;
import com.esint.music.utils.AniUtil;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.utils.SortListUtil;
import com.esint.music.view.LockButtonRelativeLayout;
import com.esint.music.view.LockPalyOrPauseButtonRelativeLayout;
import com.esint.music.view.LrcView;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import name.teze.layout.lib.SwipeBackActivity;

/**  
* 类名称：LockActivity   
* 类描述：APP锁屏界面   
* 创建人：bai   
* 创建时间：2016-3-2 上午11:49:38       
*/
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class LockActivity extends SwipeBackActivity implements OnClickListener {

	private ImageView lockImageView;// 滑动提示图标
	private AnimationDrawable aniLoading;
	private TextView songNameTextView;// 歌名
	private TextView songerTextView;// 歌手
	private TextView timeTextView;// 时间
	private TextView dateTextView;// 日期
	private TextView dayTextView;// 星期几
	private LockButtonRelativeLayout prewButton;
	private LockButtonRelativeLayout nextButton;
	private LockPalyOrPauseButtonRelativeLayout playOrPauseButton;
	private ImageView playImageView;
	private ImageView pauseImageView;
	private LrcView lrcView;
	private Handler mHandler;
	private ArrayList<Mp3Info> mp3List;// 本地音乐的list
	private LinearLayout lockBackGround;// 锁屏界面的背景 要设置成歌手写真
	private MusicPlayService musicPlayService;
	private int currentPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_lock);
		// 加入此flag是为了在有密码的锁屏界面上，此音乐播放器的锁屏界面是为了覆盖在系统解锁界面上面
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		initView();
		initData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(connection);
	}

	@Override
	public void finish() {
		super.finish();
		AniUtil.stopAnimation(aniLoading);
	}

	private void initView() {
		timeTextView = (TextView) findViewById(R.id.time);
		dateTextView = (TextView) findViewById(R.id.date);
		dayTextView = (TextView) findViewById(R.id.day);
		playImageView = (ImageView) findViewById(R.id.play);
		pauseImageView = (ImageView) findViewById(R.id.pause);
		songNameTextView = (TextView) findViewById(R.id.songName);
		songerTextView = (TextView) findViewById(R.id.songer);
		prewButton = (LockButtonRelativeLayout) findViewById(R.id.prev_button);
		nextButton = (LockButtonRelativeLayout) findViewById(R.id.next_button);
		lockBackGround = (LinearLayout) findViewById(R.id.kscManyLineLyricsViewParent);
		lrcView = (LrcView) findViewById(R.id.locklrcview);
		lockImageView = (ImageView) findViewById(R.id.tip_image);
		playOrPauseButton = (LockPalyOrPauseButtonRelativeLayout) findViewById(R.id.play_pause_button);
		aniLoading = (AnimationDrawable) lockImageView.getBackground();
		playOrPauseButton.setPlayingProgress(0);
		playOrPauseButton.setMaxProgress(0);
		playOrPauseButton.invalidate();
		prewButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		setDate();
		setTime();
	}

	private void initData() {
		AniUtil.startAnimation(aniLoading);// 开启动画
		// 绑定服务
		Intent intent = new Intent(this, MusicPlayService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
		// 排序MP3文件列表
		mp3List = MediaUtils.getMp3Info(this);
		mp3List = new SortListUtil().initMyLocalMusic(mp3List);
		currentPosition = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME, -1);
		if (currentPosition != -1) {
			songNameTextView.setText(mp3List.get(currentPosition).getTitle());
			songerTextView.setText(mp3List.get(currentPosition).getArtist());
			Bitmap preBitmap = MediaUtils.getArtwork(this,
					mp3List.get(currentPosition).getId(),
					mp3List.get(currentPosition).getAlbumId(), true, false);
			// Drawable boxBlurFilter =
			// GaussianBlurUtil.BoxBlurFilter(preBitmap);
			// albumIV.setImageBitmap(preBitmap);
			// Drawable drawable = new BitmapDrawable(preBitmap);
			// lockBackGround.setBackground(drawable);
		}

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case Constant.UPTATE_LRC_LOCK:
					int progress = (Integer) msg.obj;
					if (lrcView.hasLrc())
						lrcView.changeCurrent(progress);
					break;
				case Constant.UPDATE_LOCKTIME: {
					int timeProgress = (Integer) msg.obj;
					playOrPauseButton.setMaxProgress((int) mp3List.get(
							currentPosition).getDuration());
					playOrPauseButton.setPlayingProgress(timeProgress);
					playOrPauseButton.invalidate();

				}
					break;

				}
			}
		};

	}

	/**
	 * 设置时间
	 */
	private void setTime() {
		String str = "";
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
		Calendar lastDate = Calendar.getInstance();
		str = sdfTime.format(lastDate.getTime());
		timeTextView.setText(str);
	}

	/**
	 * 设置日期
	 */
	private void setDate() {
		String str = "";
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd");
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

		Calendar lastDate = Calendar.getInstance();
		str = sdfDate.format(lastDate.getTime());
		dateTextView.setText(str);
		str = sdfTime.format(lastDate.getTime());
		timeTextView.setText(str);

		String mWay = String.valueOf(lastDate.get(Calendar.DAY_OF_WEEK));
		if ("1".equals(mWay)) {
			mWay = "日";
		} else if ("2".equals(mWay)) {
			mWay = "一";
		} else if ("3".equals(mWay)) {
			mWay = "二";
		} else if ("4".equals(mWay)) {
			mWay = "三";
		} else if ("5".equals(mWay)) {
			mWay = "四";
		} else if ("6".equals(mWay)) {
			mWay = "五";
		} else if ("7".equals(mWay)) {
			mWay = "六";
		}
		dayTextView.setText("星期" + mWay);

	}

	/**
	 * 覆盖返回键
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) { // 屏蔽按键
		if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicPlayService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			PlayBinder playBinder = (PlayBinder) service;
			musicPlayService = playBinder.getPlayService();
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {

							Message message = mHandler.obtainMessage(
									Constant.UPTATE_LRC_LOCK,
									musicPlayService.getCurrentProgress());
							Message timeMessage = mHandler.obtainMessage(
									Constant.UPDATE_LOCKTIME,
									musicPlayService.getCurrentProgress());
							message.sendToTarget();
							timeMessage.sendToTarget();
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();

			// 拿到是否播放了
			boolean isPlaying = musicPlayService.isPlaying();
			if (isPlaying == true) {
				setLrc(songNameTextView.getText().toString());
				pauseImageView.setVisibility(View.VISIBLE);
				playImageView.setVisibility(View.INVISIBLE);
			}
		}

	};

	/**
	* @Description:设置歌词 
	* @param musicTitle
	*/
	private void setLrc(String musicTitle) {
		String target = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的歌词" + "/" + musicTitle.trim() + ".lrc";
		Log.e("target", target);
		lrcView.setLrcPath(target);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.prev_button:
			Toast.makeText(this, "点击了上一首", 0).show();
			musicPlayService.previous();
			break;
		case R.id.play_pause_button:
			Toast.makeText(this, "点击了播放或者暂停", 0).show();
			// musicPlayService.p
			break;
		case R.id.next_button:
			Toast.makeText(this, "点击了下一首", 0).show();
			musicPlayService.next();
			break;
		}
	}

}
