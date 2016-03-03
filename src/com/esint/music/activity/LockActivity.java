package com.esint.music.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import com.esint.music.R;
import com.esint.music.model.DownMucicInfo;
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
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
import android.widget.Toast;
import name.teze.layout.lib.SwipeBackActivity;

/**  
* �����ƣ�LockActivity   
* ��������APP��������   
* �����ˣ�bai   
* ����ʱ�䣺2016-3-2 ����11:49:38       
*/
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class LockActivity extends SwipeBackActivity implements OnClickListener {

	private ImageView lockImageView;// ������ʾͼ��
	private AnimationDrawable aniLoading;
	private TextView songNameTextView;// ����
	private TextView songerTextView;// ����
	private TextView timeTextView;// ʱ��
	private TextView dateTextView;// ����
	private TextView dayTextView;// ���ڼ�
	private LockButtonRelativeLayout prewButton;
	private LockButtonRelativeLayout nextButton;
	private LockPalyOrPauseButtonRelativeLayout playOrPauseButton;
	private ImageView playImageView;
	private ImageView pauseImageView;
	private LrcView lrcView;
	private Handler mHandler;
	private ArrayList<Mp3Info> mp3List;// �������ֵ�list
	private LinearLayout lockBackGround;// ��������ı��� Ҫ���óɸ���д��
	private MusicPlayService musicPlayService;
	private int currentPosition;
	private ArrayList<DownMucicInfo> downMusicList;// �ҵ����ص����ֵ��б�
	private int recordDownMusicPosition;// ��¼����ҵ����ظ�����ߵ��б�
	private String downMusicSongTime;// ���ص����ֵĸ���ʱ�� ��������������

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_lock);
		// �����flag��Ϊ��������������������ϣ������ֲ�����������������Ϊ�˸�����ϵͳ������������
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
		playOrPauseButton.setOnClickListener(this);
		prewButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		setDate();

	}

	@SuppressWarnings("deprecation")
	private void initData() {
		AniUtil.startAnimation(aniLoading);// ��������
		// �󶨷���
		Intent intent = new Intent(this, MusicPlayService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
		// ����MP3�ļ��б�
		mp3List = MediaUtils.getMp3Info(this);
		mp3List = new SortListUtil().initMyLocalMusic(mp3List);
		currentPosition = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME, -1);
		recordDownMusicPosition = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME_DOWN, -1);
		if (currentPosition != -1) {
			songNameTextView.setText(mp3List.get(currentPosition).getTitle());
			songerTextView.setText(mp3List.get(currentPosition).getArtist());
			setLrc(songNameTextView.getText().toString());
			Bitmap bitmap = MediaUtils.getArtwork(this,
					mp3List.get(currentPosition).getId(),
					mp3List.get(currentPosition).getAlbumId(), true, false);
			Drawable drawable = new BitmapDrawable(bitmap);
			// lockBackGround.setBackground(drawable);
		}
		if (recordDownMusicPosition != -1) {
			// ���ظ������ļ���
			String MusicTarget = Environment.getExternalStorageDirectory()
					+ "/" + "/���صĸ���";
			downMusicList = MediaUtils.GetMusicFiles(MusicTarget, ".mp3", true);
			downMusicSongTime = downMusicList.get(recordDownMusicPosition)
					.getDownMusicDuration();
			songNameTextView.setText(downMusicList.get(recordDownMusicPosition)
					.getDownMusicName());
			songerTextView.setText(downMusicList.get(recordDownMusicPosition)
					.getDownMusicArtist());
			final String ImageTarget = Environment
					.getExternalStorageDirectory() + "/" + "/���ص�ͼƬ" + "/";
			Bitmap albumBit = BitmapFactory.decodeFile(ImageTarget
					+ downMusicList.get(recordDownMusicPosition)
							.getDownMusicName().trim() + ".jpg", null);
			Drawable downDraw = new BitmapDrawable(albumBit);
			// lockBackGround.setBackgroundDrawable(downDraw);
			setLrc(songNameTextView.getText().toString());
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
					setTime();
					int timeProgress = (Integer) msg.obj;
					if (downMusicSongTime != null) {
						playOrPauseButton.setMaxProgress((int) MediaUtils
								.getTrackLength(downMusicSongTime));
					} else {
						playOrPauseButton.setMaxProgress((int) mp3List.get(
								currentPosition).getDuration());
					}
					playOrPauseButton.setPlayingProgress(timeProgress);
					playOrPauseButton.invalidate();
				}
					break;
				}
			}
		};
	}

	/**
	 * ����ʱ��
	 */
	private void setTime() {
		String str = "";
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
		Calendar lastDate = Calendar.getInstance();
		str = sdfTime.format(lastDate.getTime());
		timeTextView.setText(str);
	}

	/**
	 * ��������
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
			mWay = "��";
		} else if ("2".equals(mWay)) {
			mWay = "һ";
		} else if ("3".equals(mWay)) {
			mWay = "��";
		} else if ("4".equals(mWay)) {
			mWay = "��";
		} else if ("5".equals(mWay)) {
			mWay = "��";
		} else if ("6".equals(mWay)) {
			mWay = "��";
		} else if ("7".equals(mWay)) {
			mWay = "��";
		}
		dayTextView.setText("����" + mWay);

	}

	/**
	 * ���Ƿ��ؼ�
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) { // ���ΰ���
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

			// �õ��Ƿ񲥷���
			boolean isPlaying = musicPlayService.isPlaying();
			if (isPlaying == true) {
				setLrc(songNameTextView.getText().toString());
				pauseImageView.setVisibility(View.VISIBLE);
				playImageView.setVisibility(View.INVISIBLE);
			}
		}

	};

	/**
	* @Description:���ø�� 
	* @param musicTitle
	*/
	private void setLrc(String musicTitle) {
		String target = Environment.getExternalStorageDirectory() + "/"
				+ "/���صĸ��" + "/" + musicTitle.trim() + ".lrc";
		Log.e("target", target);
		lrcView.setLrcPath(target);

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.prev_button:
			Toast.makeText(this, "�������һ��", 0).show();
			musicPlayService.previous();
			pauseImageView.setVisibility(View.VISIBLE);
			playImageView.setVisibility(View.GONE);
			int prePosition = musicPlayService.getCurrentPosition();
			SharedPrefUtil.setInt(this, Constant.CLICKED_MUNSIC_NAME,
					prePosition);
			setLrc(songNameTextView.getText().toString());
			songNameTextView.setText(mp3List.get(prePosition).getTitle());
			songerTextView.setText(mp3List.get(prePosition).getArtist());
			Bitmap preBitmap = MediaUtils.getArtwork(this,
					mp3List.get(prePosition).getId(), mp3List.get(prePosition)
							.getAlbumId(), true, false);
			@SuppressWarnings("deprecation")
			Drawable drawable = new BitmapDrawable(preBitmap);
			// lockBackGround.setBackground(drawable);
			break;
		case R.id.next_button:
			Toast.makeText(this, "�������һ��", 0).show();
			musicPlayService.next();
			pauseImageView.setVisibility(View.VISIBLE);
			playImageView.setVisibility(View.GONE);
			// �л�����
			int nextPosition = musicPlayService.getCurrentPosition();
			SharedPrefUtil.setInt(this, Constant.CLICKED_MUNSIC_NAME,
					nextPosition);
			songNameTextView.setText(mp3List.get(nextPosition).getTitle());
			songerTextView.setText(mp3List.get(nextPosition).getArtist());
			Bitmap nextBitmap = MediaUtils.getArtwork(this,
					mp3List.get(nextPosition).getId(), mp3List
							.get(nextPosition).getAlbumId(), true, false);
			Drawable drawableNext = new BitmapDrawable(nextBitmap);
			// lockBackGround.setBackground(drawableNext);
			setLrc(songNameTextView.getText().toString());

			break;
		case R.id.play_pause_button:
			boolean isPlaying = musicPlayService.isPlaying();
			if (isPlaying) {
				Toast.makeText(this, "�������ͣ", 0).show();
				musicPlayService.pause();
				pauseImageView.setVisibility(View.GONE);
				playImageView.setVisibility(View.VISIBLE);
				// ���͸�����ͣ��ť�Ĺ㲥
				Intent pauseIntent = new Intent(Constant.PAUSEBUTTON_BROAD);
				this.sendBroadcast(pauseIntent);
			} else if (!isPlaying) {
				Toast.makeText(this, "����˲���", 0).show();
				musicPlayService.start();
				pauseImageView.setVisibility(View.VISIBLE);
				playImageView.setVisibility(View.GONE);
				// ���͸�����ͣ��ť�Ĺ㲥
				Intent playIntent = new Intent(Constant.PLAYBUTTON_BROAD);
				this.sendBroadcast(playIntent);
			}
			break;
		}
	}

}
