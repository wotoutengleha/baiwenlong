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
 * �����ƣ�PlayService service�������嵥�ļ���ע�� �����Ĵ�������ص�
 * �����������ֲ��ŵķ���   ʵ�� ���� ��һ�� ��һ��  ��ǰ���Ÿ����Ĳ��Ž���
 * �����ˣ�bai 
 * ����ʱ�䣺2016-1-10 ����9:53:16   
 *        
 */
public class MusicPlayService extends Service implements OnCompletionListener,
		OnErrorListener {

	private MediaPlayer mPlayer;// �������ֵ���
	private int currentPlayPosition;// ��ǰ���ŵ�λ��
	private ArrayList<Mp3Info> mp3Infos;// �������ֵĲ����б������

	// ����ģʽ
	public static final int PLAY_ORDER = 1; // ˳�򲥷�
	public static final int PLAY_RANDOM = 2; // �������
	public static final int PLAY_SINGLE = 3; // ����ѭ��
	public int playMode = PLAY_ORDER;// Ĭ����˳�򲥷�
	private Random random = new Random();

	// ���ظ������ļ���
	private String target;
	private ArrayList<DownMucicInfo> netMusicList;// �ҵ����ص������б�
	private ScreenBroadcastReceiver sOnBroadcastReciver;
	private NotificationManager notificationManager;// ֪ͨ��
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
		// ����MP3�б������
		mp3Infos = MediaUtils.getMp3Info(this);
		mp3Infos = new SortListUtil().initMyLocalMusic(mp3Infos);
		// mPlayer.setOnCompletionListener(this);
		// mPlayer.setOnErrorListener(this);
		target = Environment.getExternalStorageDirectory() + "/" + "/���صĸ���";
		netMusicList = MediaUtils.GetMusicFiles(target, ".mp3", true);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		//ע�������������Ĺ㲥
		sOnBroadcastReciver = new ScreenBroadcastReceiver();
		IntentFilter recevierFilter = new IntentFilter();
		recevierFilter.addAction(Intent.ACTION_SCREEN_ON);
		recevierFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(sOnBroadcastReciver, recevierFilter);
		//ע��֪ͨ���Ĺ㲥
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
		notification.icon = R.drawable.ic_launcher;
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

		// �˴�action������һ���� ���һ���� ���ܵ�flag����ֻ�ǵ�һ�����õ�ֵ
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
		if (position >= 0 && position < MyTabMusic.sortMyLikeMp3Infos.size()) {
			Mp3Info mp3Info = MyTabMusic.sortMyLikeMp3Infos.get(position);
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

	// �����ҵ����ص����� ��ͷ״̬
	public void playMyDown(int position) {
		if (position >= 0 && position < netMusicList.size()) {
			DownMucicInfo mp3Info = netMusicList.get(position);
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
		Log.e("��ͣ", "��ͣ");
	}

	// ��һ��
	public void next() {
		Log.e("��һ��", "��һ��");
		int currentPlayMode = getPlayMode();
		switch (currentPlayMode) {
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
			if (currentPlayPosition + 1 >= mp3Infos.size()) {
				currentPlayPosition = 0;// �ص���һ�׸�
			} else {
				currentPlayPosition++;
			}
			playLocalMusic(currentPlayPosition);
			break;
		}
		}
	}

	// ��һ��
	public void previous() {
		if (currentPlayPosition - 1 < 0) {
			currentPlayPosition = mp3Infos.size() - 1;// �ص����һ��
		} else {
			currentPlayPosition--;
		}
		Log.e("��һ��", "��һ��");
		playLocalMusic(currentPlayPosition);
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
	 * 
	 * @param url
	 *            url��ַ
	 */
	public void playUrl(String url) {
		try {
			Log.e("1111111111", "111111");
			mPlayer.reset();
			mPlayer.setDataSource(url); // ��������Դ
			mPlayer.prepare(); // prepare�Զ�����
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

	// ������
	public void seekTo(int msec) {
		mPlayer.seekTo(msec);
	}

	// ���ò��ŵ�ģʽ

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
