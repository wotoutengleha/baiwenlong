package com.esint.music.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.esint.music.R;
import com.esint.music.activity.LockActivity;
import com.esint.music.activity.MainFragmentActivity;
import com.esint.music.fragment.MyTabMusic;
import com.esint.music.model.Mp3Info;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.sortlistview.CharacterParser;
import com.esint.music.sortlistview.PinyinComparator;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.MyApplication;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.utils.SortListUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/*   
 *    
 * 类名称：PlayService service必须在清单文件中注册 这是四大组件的特点
 * 类描述：音乐播放的服务   实现 播放 上一首 下一首  当前播放歌曲的播放进度
 * 创建人：bai 
 * 创建时间：2016-1-10 下午9:53:16   
 *        
 */
public class MusicPlayService extends Service implements OnCompletionListener,
		OnErrorListener {

	private MediaPlayer mPlayer;// 播放音乐的类
	private int currentPlayPosition;// 当前播放的位置
	private ArrayList<Mp3Info> mp3Infos;// 本地音乐的播放列表的数据

	// 播放模式
	public static final int PLAY_ORDER = 1; // 顺序播放
	public static final int PLAY_RANDOM = 2; // 随机播放
	public static final int PLAY_SINGLE = 3; // 单曲循环
	public int playMode = PLAY_ORDER;// 默认是顺序播放
	private Random random = new Random();

	// 下载歌曲的文件夹
	private String target;
	private ArrayList<DownMucicInfo> netMusicList;// 我的下载的音乐列表
	private ScreenBroadcastReceiver sOnBroadcastReciver;
	private NotificationManager notificationManager;// 通知栏
	private static final String PAUSE_BROADCAST_NAME = "com.esint.music.pause.broadcast";
	private static final String NEXT_BROADCAST_NAME = "com.esint.music.next.broadcast";
	private static final String PRE_BROADCAST_NAME = "com.esint.music.pre.broadcast";
	private static final int PAUSE_FLAG = 0x1;
	private static final int NEXT_FLAG = 0x2;
	private static final int PRE_FLAG = 0x3;
	private int NOTIFICATION_ID = 0x1;
	private static final String  START_NOTIFITION = "start_notifition";

	public class PlayBinder extends Binder {
		public MusicPlayService getPlayService() {
			return MusicPlayService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mPlayer = new MediaPlayer();
		// 排序MP3列表的数据
		mp3Infos = MediaUtils.getMp3Info(this);
		mp3Infos = new SortListUtil().initMyLocalMusic(mp3Infos);
		// mPlayer.setOnCompletionListener(this);
		// mPlayer.setOnErrorListener(this);
		target = Environment.getExternalStorageDirectory() + "/" + "/下载的歌曲";
		netMusicList = MediaUtils.GetMusicFiles(target, ".mp3", true);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		//注册监听锁屏界面的广播
		sOnBroadcastReciver = new ScreenBroadcastReceiver();
		IntentFilter recevierFilter = new IntentFilter();
		recevierFilter.addAction(Intent.ACTION_SCREEN_ON);
		recevierFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(sOnBroadcastReciver, recevierFilter);
		//注册通知栏的广播
		ControlBroadcast controlBroadcast = new ControlBroadcast();
		IntentFilter notifiFilter = new IntentFilter();
		notifiFilter.addAction(PAUSE_BROADCAST_NAME);
		notifiFilter.addAction(NEXT_BROADCAST_NAME);
		notifiFilter.addAction(PRE_BROADCAST_NAME);
		registerReceiver(controlBroadcast, notifiFilter);
		
	}
	private class ControlBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int flag = intent.getIntExtra("FLAG", -1);
			switch(flag) {
			case PAUSE_FLAG:
//				MediaService.this.stopForeground(true);
				Log.e("pause", "next");
				pause();
				break;
			case NEXT_FLAG:
				Log.e("next", "next");
				next();
				break;
			case PRE_FLAG:
				previous();
				Log.e("previous", "previous");
				break;
			}
		}
	}

	/**
	* @Description:更新通知栏 
	* @return void 
	* @author bai
	*/
	public void updateNotification(Bitmap bitmap, String musicTitle,
			String musicArtist) {
		Intent intent = new Intent(getApplicationContext(),
				MainFragmentActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
				R.layout.notification);
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = musicTitle;
		notification.contentIntent = pendingIntent;
		notification.contentView = remoteViews;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		if (bitmap != null) {
			// 设置专辑图片
			remoteViews.setImageViewBitmap(R.id.image, bitmap);
		} else {
			// 设置默认图片
			remoteViews.setImageViewResource(R.id.image,
					R.drawable.img_album_background);
		}
		remoteViews.setTextViewText(R.id.title, musicTitle);
		remoteViews.setTextViewText(R.id.text, musicArtist);

		// 此处action不能是一样的 如果一样的 接受的flag参数只是第一个设置的值
		Intent pauseIntent = new Intent(PAUSE_BROADCAST_NAME);
		pauseIntent.putExtra("FLAG", PAUSE_FLAG);
		PendingIntent pausePIntent = PendingIntent.getBroadcast(this, 0,
				pauseIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.iv_pause, pausePIntent);
		
		Log.e("ddddddddddddd", "dddddddd");

		Intent nextIntent = new Intent(NEXT_BROADCAST_NAME);
		nextIntent.putExtra("FLAG", NEXT_FLAG);
		PendingIntent nextPIntent = PendingIntent.getBroadcast(this, 0,
				nextIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.iv_next, nextPIntent);

		Intent preIntent = new Intent(PRE_BROADCAST_NAME);
		preIntent.putExtra("FLAG", PRE_FLAG);
		PendingIntent prePIntent = PendingIntent.getBroadcast(this, 0,
				preIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.iv_previous, prePIntent);
		startForeground(NOTIFICATION_ID, notification);

	}
	private void cancelNotification() {
		stopForeground(true);
		notificationManager.cancel(NOTIFICATION_ID);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mPlayer.release();
		mPlayer = null;
		unregisterReceiver(sOnBroadcastReciver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new PlayBinder();
	}

	// 得到当前的播放的歌曲
	public int getCurrentPosition() {
		return currentPlayPosition;
	}

	// 播放本地的音乐 从头状态
	public void playLocalMusic(int position) {

		if (position >= 0 && position < mp3Infos.size()) {
			Mp3Info mp3Info = mp3Infos.get(position);
			try {
				mPlayer.reset();
				// 设置播放的位置
				mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
				mPlayer.prepare();
				mPlayer.start();
				currentPlayPosition = position;// 记录当前播放的位置
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 播放我的最爱的音乐 从头状态
	public void playMyFav(int position) {
		if (position >= 0 && position < MyTabMusic.sortMyLikeMp3Infos.size()) {
			Mp3Info mp3Info = MyTabMusic.sortMyLikeMp3Infos.get(position);
			try {
				mPlayer.reset();
				// 设置播放的位置
				mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
				mPlayer.prepare();
				mPlayer.start();
				currentPlayPosition = position;// 记录当前播放的位置
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 播放我的下载的音乐 从头状态
	public void playMyDown(int position) {
		if (position >= 0 && position < netMusicList.size()) {
			DownMucicInfo mp3Info = netMusicList.get(position);
			try {
				mPlayer.reset();
				// 设置播放的位置
				mPlayer.setDataSource(this,
						Uri.parse(mp3Info.getDownMusicUrl()));
				mPlayer.prepare();
				mPlayer.start();
				currentPlayPosition = position;// 记录当前播放的位置
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isPlaying() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			return true;
		} else {
			return false;
		}
	}

	// 得到当前的进度
	public int getCurrentProgress() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			return mPlayer.getCurrentPosition();
		}
		return mPlayer.getCurrentPosition();
	}

	// 暂停
	public void pause() {
		if (mPlayer.isPlaying())
			mPlayer.pause();
		Log.e("暂停", "暂停");
	}

	// 下一首
	public void next() {
		Log.e("下一首", "下一首");
		int currentPlayMode = getPlayMode();
		switch (currentPlayMode) {
		case PLAY_ORDER: {
			if (currentPlayPosition + 1 >= mp3Infos.size()) {
				currentPlayPosition = 0;// 回到第一首歌
			} else {
				currentPlayPosition++;
			}
			playLocalMusic(currentPlayPosition);
			break;
		}
		case PLAY_RANDOM: {
			playLocalMusic(random.nextInt(mp3Infos.size()));
			break;
		}
		case PLAY_SINGLE: {
			if (currentPlayPosition + 1 >= mp3Infos.size()) {
				currentPlayPosition = 0;// 回到第一首歌
			} else {
				currentPlayPosition++;
			}
			playLocalMusic(currentPlayPosition);
			break;
		}
		}
	}

	// 上一首
	public void previous() {
		if (currentPlayPosition - 1 < 0) {
			currentPlayPosition = mp3Infos.size() - 1;// 回到最后一首
		} else {
			currentPlayPosition--;
		}
		Log.e("上一首", "上一首");
		playLocalMusic(currentPlayPosition);
	}

	// 开始
	public void start() {
		// 不为空并且不是正在播放的状态，开始播放
		if (mPlayer != null && !mPlayer.isPlaying()) {
			mPlayer.start();
		}
	}

	// 停止
	public void stop() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

	/**
	 * 
	 * @param url
	 *            url地址
	 */
	public void playUrl(String url) {
		try {
			Log.e("1111111111", "111111");
			mPlayer.reset();
			mPlayer.setDataSource(url); // 设置数据源
			mPlayer.prepare(); // prepare自动播放
			Log.e("2222222", "333333");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getDuration() {
		return mPlayer.getDuration();
	}

	// 跳到哪
	public void seekTo(int msec) {
		mPlayer.seekTo(msec);
	}

	// 设置播放的模式

	public int getPlayMode() {
		return playMode;
	}

	public void setPlayMode(int playMode) {
		this.playMode = playMode;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mPlayer.reset();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {

		switch (playMode) {
		case PLAY_ORDER: {
			next();
			break;
		}
		case PLAY_RANDOM: {
			playLocalMusic(random.nextInt(mp3Infos.size()));
			break;
		}
		case PLAY_SINGLE: {
			playLocalMusic(currentPlayPosition);
			break;
		}
		}
	}
}
