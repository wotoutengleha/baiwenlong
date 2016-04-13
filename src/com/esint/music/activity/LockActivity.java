package com.esint.music.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esint.music.R;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.model.Mp3Info;
import com.esint.music.service.MusicPlayService;
import com.esint.music.service.MusicPlayService.PlayBinder;
import com.esint.music.utils.ActivityCollectUtil;
import com.esint.music.utils.AniUtil;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.utils.SortListUtil;
import com.esint.music.view.LockButtonRelativeLayout;
import com.esint.music.view.LockPalyOrPauseButtonRelativeLayout;
import com.esint.music.view.LrcView;
import com.esint.music.view.SystemStatusManager;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
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
import android.widget.RelativeLayout;
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
	private MusicPlayService musicPlayService;
	private int currentPosition;
	private ArrayList<DownMucicInfo> downMusicList;// �ҵ����ص����ֵ��б�
	private int recordDownMusicPosition;// ��¼����ҵ����ظ�����ߵ��б�
	private int recordLikeMusicPosition;// ��¼����ҵ�ϲ��������ߵ��б�
	private String imageTarget;// ��������ͼƬ�����·��
	private String musicFlag;// ���Ǳ������ֻ������ص�����
	private MyBroadCast broadCast;
	private HttpUtils httpUtils = new HttpUtils();
	private ImageView backImg;
	private BitmapUtils bitmapUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTranslucentStatus();
		setContentView(R.layout.activity_lock);
		ActivityCollectUtil.addActivity(this);

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
		unregisterReceiver(broadCast);
		ActivityCollectUtil.removeActivity(this);
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
		lrcView = (LrcView) findViewById(R.id.locklrcview);
		lockImageView = (ImageView) findViewById(R.id.tip_image);
		playOrPauseButton = (LockPalyOrPauseButtonRelativeLayout) findViewById(R.id.play_pause_button);
		backImg = (ImageView) findViewById(R.id.backImg);

		// backImg.setImageAlpha(200);
		// View v =
		// findViewById(R.id.kscManyLineLyricsViewParent);//�ҵ���Ҫ��͸��������layout ��id
		// v.getBackground().setAlpha(100);//0~255͸����ֵ

		// lockBackGround.getBackground().setAlpha(100);

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
		AniUtil.startAnimation(aniLoading);
		// �󶨷���
		Intent intent = new Intent(this, MusicPlayService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
		broadCast = new MyBroadCast();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("updateLocalText");
		intentFilter.addAction("updateDownText");
		intentFilter.addAction("updateLikeText");
		registerReceiver(broadCast, intentFilter);

		bitmapUtils = new BitmapUtils(this);

		// ����MP3�ļ��б�
		mp3List = MediaUtils.getMp3Info(this);
		mp3List = new SortListUtil().initMyLocalMusic(mp3List);
		imageTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/���ص�ͼƬ" + "/";
		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/���صĸ���";
		downMusicList = MediaUtils.GetMusicFiles(MusicTarget, ".mp3", true);

		currentPosition = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME, -1);
		recordDownMusicPosition = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME_DOWN, -1);
		recordLikeMusicPosition = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME_LIKE, -1);
		musicFlag = SharedPrefUtil.getString(this, Constant.MUSIC_FLAG,
				"The music Flag is Empty");
		if (currentPosition != -1 && musicFlag.equals("local_music")) {
			songNameTextView.setText(mp3List.get(currentPosition).getTitle());
			songerTextView.setText(mp3List.get(currentPosition).getArtist());
			setLrc(songNameTextView.getText().toString());
			Log.e("�������ֵĸ���", songerTextView.getText().toString());
			downLoadArtistImag(songerTextView.getText().toString());

		} else if (recordDownMusicPosition != -1
				&& musicFlag.equals("down_music")) {

			songNameTextView.setText(downMusicList.get(recordDownMusicPosition)
					.getDownMusicName());
			songerTextView.setText(downMusicList.get(recordDownMusicPosition)
					.getDownMusicArtist());
			setLrc(songNameTextView.getText().toString());

			Log.e("�������ֵĸ���", songerTextView.getText().toString());
			downLoadArtistImag(songerTextView.getText().toString());
		} else if (recordLikeMusicPosition != -1
				&& musicFlag.equals("like_music")
				&& MainFragmentActivity.likeMusciList.size() != 0) {

			songNameTextView.setText(MainFragmentActivity.likeMusciList.get(
					recordLikeMusicPosition).getMusicName());
			songerTextView.setText(MainFragmentActivity.likeMusciList.get(
					recordLikeMusicPosition).getMusicArtist());
			setLrc(songNameTextView.getText().toString());

			Log.e("ϲ�������ֵĸ���", songerTextView.getText().toString());
			downLoadArtistImag(songerTextView.getText().toString());
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
					if (musicFlag.equals("down_music")
							&& musicPlayService.getCurrentPosition() <= downMusicList
									.size()) {
						playOrPauseButton.setMaxProgress((int) MediaUtils
								.getTrackLength(downMusicList.get(
										musicPlayService.getCurrentPosition())
										.getDownMusicDuration()));

					} else if (musicFlag.equals("local_music")
							&& musicPlayService.getCurrentPosition() <= mp3List
									.size()) {
						playOrPauseButton.setMaxProgress((int) mp3List.get(
								musicPlayService.getCurrentPosition())
								.getDuration());
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
	private String wpurl;
	private String bkurl;

	/**
	* @Description:���ø�� 
	* @param musicTitle
	*/
	private void setLrc(String musicTitle) {
		String target = Environment.getExternalStorageDirectory() + "/"
				+ "/���صĸ��" + "/" + musicTitle.trim() + ".lrc";
		lrcView.setLrcPath(target);

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.prev_button:

			if (musicFlag.equals("local_music")) {
				musicPlayService.previous();
				localMusicNextOrPre();
			} else if (musicFlag.equals("down_music")) {
				musicPlayService.previousDown();
				downMusicNextOrPre();
			} else if (musicFlag.equals("like_music")
					&& MainFragmentActivity.likeMusciList.size() != 0) {
				musicPlayService.previousLike();
				likeMusicNextOrPre();
			}

			break;
		case R.id.next_button:
			if (musicFlag.equals("local_music")) {
				musicPlayService.next();
				localMusicNextOrPre();
			} else if (musicFlag.equals("down_music")) {
				musicPlayService.nextDownMusic();
				downMusicNextOrPre();
			} else if (musicFlag.equals("like_music")
					&& MainFragmentActivity.likeMusciList.size() != 0) {
				musicPlayService.nextLikeMusic();
				likeMusicNextOrPre();
			}
			break;
		case R.id.play_pause_button:
			playOrPause();
			break;
		}
	}

	// �ڱ��������б��µ����һ��ʱ���õķ���
	private void localMusicNextOrPre() {

		pauseImageView.setVisibility(View.VISIBLE);
		playImageView.setVisibility(View.GONE);
		// �л�����
		int nextPosition = musicPlayService.getCurrentPosition();
		SharedPrefUtil.setInt(this, Constant.CLICKED_MUNSIC_NAME, nextPosition);
		songNameTextView.setText(mp3List.get(nextPosition).getTitle());
		songerTextView.setText(mp3List.get(nextPosition).getArtist());
		setLrc(songNameTextView.getText().toString());
		Log.e("�������������һ�׵�ʱ����ֵ�����", songerTextView.getText().toString());
		downLoadArtistImag(songerTextView.getText().toString());
	}

	// �����ص����ֵ����һ�׵�ʱ����õķ���
	private void downMusicNextOrPre() {

		pauseImageView.setVisibility(View.VISIBLE);
		playImageView.setVisibility(View.GONE);
		// �л�����
		int nextPosition = musicPlayService.getCurrentPosition();
		SharedPrefUtil.setInt(this, Constant.CLICKED_MUNSIC_NAME_DOWN,
				nextPosition);
		songNameTextView.setText(downMusicList.get(nextPosition)
				.getDownMusicName());
		songerTextView.setText(downMusicList.get(nextPosition)
				.getDownMusicArtist());
		Bitmap albumBit = BitmapFactory.decodeFile(imageTarget
				+ downMusicList.get(recordDownMusicPosition).getDownMusicName()
						.trim() + ".jpg", null);
		if (albumBit != null) {
			Drawable drawableNext = new BitmapDrawable(albumBit);
		}
		// lockBackGround.setBackground(drawableNext);
		setLrc(songNameTextView.getText().toString());
		Log.e("������ص�������һ�׵�ʱ����ֵ�����", songerTextView.getText().toString());
		downLoadArtistImag(songerTextView.getText().toString());
	}

	// ��ϲ���ĵ����ֵ����һ�׵�ʱ����õķ���
	private void likeMusicNextOrPre() {

		pauseImageView.setVisibility(View.VISIBLE);
		playImageView.setVisibility(View.GONE);
		// �л�����
		int nextPosition = musicPlayService.getCurrentPosition();
		SharedPrefUtil.setInt(this, Constant.CLICKED_MUNSIC_NAME_LIKE,
				nextPosition);
		songNameTextView.setText(MainFragmentActivity.likeMusciList.get(
				nextPosition).getMusicName());
		songerTextView.setText(MainFragmentActivity.likeMusciList.get(
				nextPosition).getMusicArtist());
		// Bitmap albumBit = BitmapFactory.decodeFile(imageTarget
		// + downMusicList.get(recordDownMusicPosition).getDownMusicName()
		// .trim() + ".jpg", null);
		// if (albumBit != null) {
		// Drawable drawableNext = new BitmapDrawable(albumBit);
		// }
		// lockBackGround.setBackground(drawableNext);
		setLrc(songNameTextView.getText().toString());
		Log.e("���ϲ����������һ�׵�ʱ����ֵ�����", songerTextView.getText().toString());
		downLoadArtistImag(songerTextView.getText().toString());
	}

	// �����ͣ���߲��Ű�ť ��ʱ����õķ���
	private void playOrPause() {
		boolean isPlaying = musicPlayService.isPlaying();
		if (isPlaying) {
			musicPlayService.pause();
			pauseImageView.setVisibility(View.GONE);
			playImageView.setVisibility(View.VISIBLE);
			// ���͸�����ͣ��ť�Ĺ㲥
			Intent pauseIntent = new Intent(Constant.PAUSEBUTTON_BROAD);
			this.sendBroadcast(pauseIntent);
		} else if (!isPlaying) {
			musicPlayService.start();
			pauseImageView.setVisibility(View.VISIBLE);
			playImageView.setVisibility(View.GONE);
			// ���͸�����ͣ��ť�Ĺ㲥
			Intent playIntent = new Intent(Constant.PLAYBUTTON_BROAD);
			this.sendBroadcast(playIntent);
		}
	}

	// ���ܵ��㲥��������
	public class MyBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("updateLocalText")) {
				Log.e("������������ܵ��˱������ֹ㲥", "������������ܵ��˱������ֹ㲥");
				if (musicFlag.equals("local_music")) {
					SharedPrefUtil.setInt(LockActivity.this,
							Constant.CLICKED_MUNSIC_NAME,
							musicPlayService.getCurrentPosition());
					songNameTextView.setText(mp3List.get(
							musicPlayService.getCurrentPosition()).getTitle());
					songerTextView.setText(mp3List.get(
							musicPlayService.getCurrentPosition()).getArtist());
					downLoadArtistImag(songNameTextView.getText().toString());
					setLrc(songNameTextView.getText().toString());
				}
			} else if (intent.getAction().endsWith("updateDownText")) {
				if (musicFlag.equals("down_music")) {
					Log.e("������������ܵ����������ֹ㲥", "������������ܵ����������ֹ㲥");
					SharedPrefUtil.setInt(LockActivity.this,
							Constant.CLICKED_MUNSIC_NAME_DOWN,
							musicPlayService.getCurrentPosition());
					songNameTextView.setText(downMusicList.get(
							musicPlayService.getCurrentPosition())
							.getDownMusicName());
					songerTextView.setText(downMusicList.get(
							musicPlayService.getCurrentPosition())
							.getDownMusicArtist());
					setLrc(songNameTextView.getText().toString());
					downLoadArtistImag(songNameTextView.getText().toString());
				}
			} else if (intent.getAction().endsWith("updateLikeText")) {
				if (musicFlag.equals("like_music")
						&& MainFragmentActivity.likeMusciList.size() != 0) {
					Log.e("������������ܵ���ϲ�����ֹ㲥", "������������ܵ���ϲ�����ֹ㲥");
					SharedPrefUtil.setInt(LockActivity.this,
							Constant.CLICKED_MUNSIC_NAME_LIKE,
							musicPlayService.getCurrentPosition());
					songNameTextView.setText(MainFragmentActivity.likeMusciList
							.get(musicPlayService.getCurrentPosition())
							.getMusicName());
					songerTextView.setText(MainFragmentActivity.likeMusciList
							.get(musicPlayService.getCurrentPosition())
							.getMusicArtist());
					setLrc(songNameTextView.getText().toString());
					downLoadArtistImag(songNameTextView.getText().toString());
				}
			}
		}

	}

	/**
	* @Description: ���ݸ��ֵ��������ظ��ֵ�д��
	* @return void 
	* @author bai
	*/
	private void downLoadArtistImag(String musicName) {

		String URL = "http://artistpicserver.kuwo.cn/pic.web?type=big_artist_pic&pictype=url&content=list&&id=0&name="
				+ musicName.trim()
				+ "&from=pc&json=1&version=1&width=480&height=800";

		Log.e("musicName", musicName);
		Log.e("URL11111111", URL);
		httpUtils.send(HttpMethod.GET, URL, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				Toast.makeText(LockActivity.this, "����ʧ����" + arg1, 0).show();
				bitmapUtils.display(backImg,
						"assets/img/profile_default_bg.jpg");
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				String result = arg0.result;
				Log.e("����ĵ�ַ��", this.getRequestUrl());
				parseJsonResult(result);
			}
		});
	}

	/**
	* @Description:����ͼƬ��ַ ����ͼƬ 
	* @param result
	* @return void 
	* @author bai
	*/
	protected void parseJsonResult(String result) {

		try {
			JSONObject object = new JSONObject(result);
			String array = object.getString("array");
			JSONArray jsonArray = new JSONArray(array);

			Log.e("�����˽����ķ���", "�����˽����ķ���");

			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject objectUrl = (JSONObject) jsonArray.get(i);
				if (!objectUrl.isNull("wpurl")) {
					Log.e("wpurl", wpurl + "");
					wpurl = objectUrl.getString("wpurl");
				} else if (!objectUrl.isNull("bkurl")) {
					bkurl = objectUrl.getString("bkurl");
					Log.e("wpurl", bkurl + "");
				}
			}
			if (wpurl != null) {
				bitmapUtils.display(backImg, wpurl);
				Log.e("wpurl���õ�", "wpurl���õ�");
			}

			if (bkurl != null) {
				Log.e("bkurl���õ�", "bkurl���õ�");
				bitmapUtils.display(backImg, bkurl);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** * ����״̬������״̬ */
	@SuppressLint("InlinedApi")
	private void setTranslucentStatus() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window win = getWindow();
			WindowManager.LayoutParams winParams = win.getAttributes();
			final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
			winParams.flags |= bits;
			win.setAttributes(winParams);
			SystemStatusManager tintManager = new SystemStatusManager(this);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setStatusBarTintResource(0);// ״̬���ޱ���
		}
	}
}
