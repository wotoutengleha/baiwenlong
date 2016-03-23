package com.esint.music.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.activity.MainFragmentActivity;
import com.esint.music.activity.SettingActivity;
import com.esint.music.fragment.MyTabMusic;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.model.LikeMusicModel;
import com.esint.music.model.Mp3Info;
import com.esint.music.receiver.ScreenBroadcastReceiver;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.ShakeDetector;
import com.esint.music.utils.ShakeDetector.OnShakeListener;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.utils.SortListUtil;

/*   
 *    
 * 类名称：PlayService service必须在清单文件中注册 这是四大组件的特点
 * 类描述：音乐播放的服务   实现 播放 上一首 下一首  当前播放歌曲的播放进度
 * 创建人：bai 
 * 创建时间：2016-1-10 下午9:53:16   
 *        
 */
public class MusicPlayService extends Service implements OnShakeListener,
		OnPreparedListener {

	private MediaPlayer mPlayer;// 播放音乐的类
	private int currentPlayPosition;// 当前播放的位置
	private ArrayList<Mp3Info> mp3Infos;// 本地音乐的播放列表的数据

	// 播放模式
	public static final int PLAY_ORDER = 1; // 顺序播放
	public static final int PLAY_RANDOM = 2; // 随机播放
	public static final int PLAY_SINGLE = 3; // 单曲循环
	public static int playMode = PLAY_ORDER;// 默认是顺序播放
	private Random random = new Random();

	// 下载歌曲的文件夹
	private String target;
	private ArrayList<DownMucicInfo> downMusicList = new ArrayList<DownMucicInfo>();// 我的下载的音乐列表
	private ScreenBroadcastReceiver sOnBroadcastReciver;
	private int NOTIFICATION_ID = 0x1;
	private ShakeDetector mShakeDetector;// 摇一摇切换音乐

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
		target = Environment.getExternalStorageDirectory() + "/" + "/下载的歌曲";
		downMusicList = MediaUtils.GetMusicFiles(target, ".mp3", true);
		playMode = SharedPrefUtil.getInt(this, Constant.PLAY_MODE, 1);

		// 注册监听锁屏界面的广播
		sOnBroadcastReciver = new ScreenBroadcastReceiver();
		IntentFilter recevierFilter = new IntentFilter();
		recevierFilter.addAction(Intent.ACTION_SCREEN_ON);
		recevierFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(sOnBroadcastReciver, recevierFilter);

		mShakeDetector = new ShakeDetector(this);
		mShakeDetector.setOnShakeListener(this);
		mPlayer.setOnPreparedListener(this);
		// 摇一摇的实现不断的在handler里边发送消息
		SettingActivity.mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == Constant.WHAT_SHAKE) {
					Boolean isSelect = (Boolean) msg.obj;
					if (isSelect == true) {
						mShakeDetector.start();
						Message message = SettingActivity.mHandler
								.obtainMessage(Constant.WHAT_SHAKE, true);
						message.sendToTarget();
					} else {
						mShakeDetector.stop();
						SettingActivity.mHandler
								.removeMessages(Constant.WHAT_SHAKE);
					}
				}
			}

		};

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
		notification.icon = R.drawable.myapplogo;
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
		startForeground(NOTIFICATION_ID, notification);
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
		if (position >= 0
				&& position < MainFragmentActivity.likeMusciList.size()) {
			LikeMusicModel mp3Info = MainFragmentActivity.likeMusciList
					.get(position);
			try {
				mPlayer.reset();
				// 设置播放的位置
				mPlayer.setDataSource(this, Uri.parse(mp3Info.getMusicURL()));
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
		if (position >= 0 && position < downMusicList.size()) {
			DownMucicInfo mp3Info = downMusicList.get(position);
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
	}

	// 下一首 本地音乐列表
	public void next() {
		switch (playMode) {
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
			// if (currentPlayPosition + 1 >= mp3Infos.size()) {
			// currentPlayPosition = 0;// 回到第一首歌
			// } else {
			// currentPlayPosition++;
			// }
			playLocalMusic(currentPlayPosition);
			break;
		}
		}
	}

	// 下一首 播放下载音乐的下一首歌曲
	public void nextDownMusic() {
		switch (playMode) {
		case PLAY_ORDER: {
			if (currentPlayPosition + 1 >= downMusicList.size()) {
				currentPlayPosition = 0;// 回到第一首歌
			} else {
				currentPlayPosition++;
			}
			playMyDown(currentPlayPosition);
			break;
		}
		case PLAY_RANDOM: {
			playMyDown(random.nextInt(downMusicList.size()));
			break;
		}
		case PLAY_SINGLE: {
			// if (currentPlayPosition + 1 >= downMusicList.size()) {
			// currentPlayPosition = 0;// 回到第一首歌
			// } else {
			// currentPlayPosition++;
			// }
			playMyDown(currentPlayPosition);
			break;
		}
		}
	}

	// 下一首 播放喜欢音乐的下一首歌曲
	public void nextLikeMusic() {
		switch (playMode) {
		case PLAY_ORDER: {
			if (currentPlayPosition + 1 >= MainFragmentActivity.likeMusciList
					.size()) {
				currentPlayPosition = 0;// 回到第一首歌
			} else {
				currentPlayPosition++;
			}
			playMyFav(currentPlayPosition);
			break;
		}
		case PLAY_RANDOM: {
			playMyFav(random.nextInt(MainFragmentActivity.getFavMusicFromDB()
					.size()));
			break;
		}
		case PLAY_SINGLE: {
			// if (currentPlayPosition + 1 >= downMusicList.size()) {
			// currentPlayPosition = 0;// 回到第一首歌
			// } else {
			// currentPlayPosition++;
			// }
			playMyFav(currentPlayPosition);
			break;
		}
		}
	}

	// 上一首 本地音乐列表
	public void previous() {
		if (currentPlayPosition - 1 < 0) {
			currentPlayPosition = mp3Infos.size() - 1;// 回到最后一首
		} else {
			currentPlayPosition--;
		}
		playLocalMusic(currentPlayPosition);
	}

	// 上一首 下载音乐列表
	public void previousDown() {
		if (currentPlayPosition - 1 < 0) {
			currentPlayPosition = downMusicList.size() - 1;// 回到最后一首
		} else {
			currentPlayPosition--;
		}
		playMyDown(currentPlayPosition);
	}

	// 上一首 喜欢音乐列表
	public void previousLike() {
		if (currentPlayPosition - 1 < 0) {
			currentPlayPosition = MainFragmentActivity.likeMusciList.size() - 1;// 回到最后一首
		} else {
			currentPlayPosition--;
		}
		playMyFav(currentPlayPosition);
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
	* @Description:播放网络音乐 
	* @param url
	* @return void 
	* @author bai
	*/
	public void playNetMusic(String url) {
		try {
			mPlayer.reset();// 重置URL
			mPlayer.setDataSource(url); // 设置数据源
			mPlayer.prepareAsync();
			Log.e("url", url);
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

	public static int getPlayMode() {
		return playMode;
	}

	public void setPlayMode(int playMode) {
		MusicPlayService.playMode = playMode;
	}

	// 摇一摇接口实现的方法
	@Override
	public void onShake() {
		Toast.makeText(this, "摇一摇换歌了", 0).show();
		Random random = new Random();
		playLocalMusic(random.nextInt(mp3Infos.size()));
		Message message = MyTabMusic.mHandler
				.obtainMessage(Constant.SHAKE_MUSIC);
		message.sendToTarget();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();

	}
}
