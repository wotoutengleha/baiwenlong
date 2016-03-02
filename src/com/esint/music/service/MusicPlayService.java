package com.esint.music.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.esint.music.activity.LockActivity;
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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

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

		sOnBroadcastReciver = new ScreenBroadcastReceiver();
		IntentFilter recevierFilter = new IntentFilter();
		recevierFilter.addAction(Intent.ACTION_SCREEN_ON);
		recevierFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(sOnBroadcastReciver, recevierFilter);
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
	}

	// 下一首
	public void next() {

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
