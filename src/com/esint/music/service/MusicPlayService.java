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
 * �����ƣ�PlayService service�������嵥�ļ���ע�� �����Ĵ�������ص�
 * �����������ֲ��ŵķ���   ʵ�� ���� ��һ�� ��һ��  ��ǰ���Ÿ����Ĳ��Ž���
 * �����ˣ�bai 
 * ����ʱ�䣺2016-1-10 ����9:53:16   
 *        
 */
public class MusicPlayService extends Service implements OnShakeListener,
		OnPreparedListener {

	private MediaPlayer mPlayer;// �������ֵ���
	private int currentPlayPosition;// ��ǰ���ŵ�λ��
	private ArrayList<Mp3Info> mp3Infos;// �������ֵĲ����б������

	// ����ģʽ
	public static final int PLAY_ORDER = 1; // ˳�򲥷�
	public static final int PLAY_RANDOM = 2; // �������
	public static final int PLAY_SINGLE = 3; // ����ѭ��
	public static int playMode = PLAY_ORDER;// Ĭ����˳�򲥷�
	private Random random = new Random();

	// ���ظ������ļ���
	private String target;
	private ArrayList<DownMucicInfo> downMusicList = new ArrayList<DownMucicInfo>();// �ҵ����ص������б�
	private ScreenBroadcastReceiver sOnBroadcastReciver;
	private int NOTIFICATION_ID = 0x1;
	private ShakeDetector mShakeDetector;// ҡһҡ�л�����

	public class PlayBinder extends Binder {
		public MusicPlayService getPlayService() {
			return MusicPlayService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mPlayer = new MediaPlayer();
		// ����MP3�б������
		mp3Infos = MediaUtils.getMp3Info(this);
		mp3Infos = new SortListUtil().initMyLocalMusic(mp3Infos);
		target = Environment.getExternalStorageDirectory() + "/" + "/���صĸ���";
		downMusicList = MediaUtils.GetMusicFiles(target, ".mp3", true);
		playMode = SharedPrefUtil.getInt(this, Constant.PLAY_MODE, 1);

		// ע�������������Ĺ㲥
		sOnBroadcastReciver = new ScreenBroadcastReceiver();
		IntentFilter recevierFilter = new IntentFilter();
		recevierFilter.addAction(Intent.ACTION_SCREEN_ON);
		recevierFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(sOnBroadcastReciver, recevierFilter);

		mShakeDetector = new ShakeDetector(this);
		mShakeDetector.setOnShakeListener(this);
		mPlayer.setOnPreparedListener(this);
		// ҡһҡ��ʵ�ֲ��ϵ���handler��߷�����Ϣ
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
	* @Description:����֪ͨ�� 
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
			// ����ר��ͼƬ
			remoteViews.setImageViewBitmap(R.id.image, bitmap);
		} else {
			// ����Ĭ��ͼƬ
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

	// �õ���ǰ�Ĳ��ŵĸ���
	public int getCurrentPosition() {
		return currentPlayPosition;
	}

	// ���ű��ص����� ��ͷ״̬
	public void playLocalMusic(int position) {

		if (position >= 0 && position < mp3Infos.size()) {
			Mp3Info mp3Info = mp3Infos.get(position);
			try {
				mPlayer.reset();
				// ���ò��ŵ�λ��
				mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
				mPlayer.prepare();
				mPlayer.start();
				currentPlayPosition = position;// ��¼��ǰ���ŵ�λ��
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// �����ҵ�������� ��ͷ״̬
	public void playMyFav(int position) {
		if (position >= 0
				&& position < MainFragmentActivity.likeMusciList.size()) {
			LikeMusicModel mp3Info = MainFragmentActivity.likeMusciList
					.get(position);
			try {
				mPlayer.reset();
				// ���ò��ŵ�λ��
				mPlayer.setDataSource(this, Uri.parse(mp3Info.getMusicURL()));
				mPlayer.prepare();
				mPlayer.start();
				currentPlayPosition = position;// ��¼��ǰ���ŵ�λ��
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// �����ҵ����ص����� ��ͷ״̬
	public void playMyDown(int position) {
		if (position >= 0 && position < downMusicList.size()) {
			DownMucicInfo mp3Info = downMusicList.get(position);
			try {
				mPlayer.reset();
				// ���ò��ŵ�λ��
				mPlayer.setDataSource(this,
						Uri.parse(mp3Info.getDownMusicUrl()));
				mPlayer.prepare();
				mPlayer.start();
				currentPlayPosition = position;// ��¼��ǰ���ŵ�λ��
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

	// �õ���ǰ�Ľ���
	public int getCurrentProgress() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			return mPlayer.getCurrentPosition();
		}
		return mPlayer.getCurrentPosition();
	}

	// ��ͣ
	public void pause() {
		if (mPlayer.isPlaying())
			mPlayer.pause();
	}

	// ��һ�� ���������б�
	public void next() {
		switch (playMode) {
		case PLAY_ORDER: {
			if (currentPlayPosition + 1 >= mp3Infos.size()) {
				currentPlayPosition = 0;// �ص���һ�׸�
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
			// currentPlayPosition = 0;// �ص���һ�׸�
			// } else {
			// currentPlayPosition++;
			// }
			playLocalMusic(currentPlayPosition);
			break;
		}
		}
	}

	// ��һ�� �����������ֵ���һ�׸���
	public void nextDownMusic() {
		switch (playMode) {
		case PLAY_ORDER: {
			if (currentPlayPosition + 1 >= downMusicList.size()) {
				currentPlayPosition = 0;// �ص���һ�׸�
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
			// currentPlayPosition = 0;// �ص���һ�׸�
			// } else {
			// currentPlayPosition++;
			// }
			playMyDown(currentPlayPosition);
			break;
		}
		}
	}

	// ��һ�� ����ϲ�����ֵ���һ�׸���
	public void nextLikeMusic() {
		switch (playMode) {
		case PLAY_ORDER: {
			if (currentPlayPosition + 1 >= MainFragmentActivity.likeMusciList
					.size()) {
				currentPlayPosition = 0;// �ص���һ�׸�
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
			// currentPlayPosition = 0;// �ص���һ�׸�
			// } else {
			// currentPlayPosition++;
			// }
			playMyFav(currentPlayPosition);
			break;
		}
		}
	}

	// ��һ�� ���������б�
	public void previous() {
		if (currentPlayPosition - 1 < 0) {
			currentPlayPosition = mp3Infos.size() - 1;// �ص����һ��
		} else {
			currentPlayPosition--;
		}
		playLocalMusic(currentPlayPosition);
	}

	// ��һ�� ���������б�
	public void previousDown() {
		if (currentPlayPosition - 1 < 0) {
			currentPlayPosition = downMusicList.size() - 1;// �ص����һ��
		} else {
			currentPlayPosition--;
		}
		playMyDown(currentPlayPosition);
	}

	// ��һ�� ϲ�������б�
	public void previousLike() {
		if (currentPlayPosition - 1 < 0) {
			currentPlayPosition = MainFragmentActivity.likeMusciList.size() - 1;// �ص����һ��
		} else {
			currentPlayPosition--;
		}
		playMyFav(currentPlayPosition);
	}

	// ��ʼ
	public void start() {
		// ��Ϊ�ղ��Ҳ������ڲ��ŵ�״̬����ʼ����
		if (mPlayer != null && !mPlayer.isPlaying()) {
			mPlayer.start();
		}
	}

	// ֹͣ
	public void stop() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

	/**
	* @Description:������������ 
	* @param url
	* @return void 
	* @author bai
	*/
	public void playNetMusic(String url) {
		try {
			mPlayer.reset();// ����URL
			mPlayer.setDataSource(url); // ��������Դ
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

	// ������
	public void seekTo(int msec) {
		mPlayer.seekTo(msec);
	}

	// ���ò��ŵ�ģʽ

	public static int getPlayMode() {
		return playMode;
	}

	public void setPlayMode(int playMode) {
		MusicPlayService.playMode = playMode;
	}

	// ҡһҡ�ӿ�ʵ�ֵķ���
	@Override
	public void onShake() {
		Toast.makeText(this, "ҡһҡ������", 0).show();
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
