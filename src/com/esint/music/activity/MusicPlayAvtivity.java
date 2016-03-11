package com.esint.music.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import name.teze.layout.lib.SwipeBackActivity;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.fragment.MyTabMusic;
import com.esint.music.fragment.MyTabMusic.MyBroadCast;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.model.Mp3Info;
import com.esint.music.model.SearchResult;
import com.esint.music.service.MusicPlayService;
import com.esint.music.service.MusicPlayService.PlayBinder;
import com.esint.music.utils.Constant;
import com.esint.music.utils.DownMusicUtils;
import com.esint.music.utils.GaussianBlurUtil;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.MyApplication;
import com.esint.music.utils.SearchMusicUtil;
import com.esint.music.utils.SortListUtil;
import com.esint.music.utils.SearchMusicUtil.onSearchResultListener;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.view.AlwaysMarqueeTextView;
import com.esint.music.view.LrcView;
import com.esint.music.view.PlayListPopuWindow;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

public class MusicPlayAvtivity extends SwipeBackActivity implements
		OnClickListener, OnSeekBarChangeListener {

	private ImageButton btnBack, btnFunction, btnPrev, btnPlay, btnPause,
			btnNext, btnPalyList, btnPlayMode, btnPlayShared, btnLike;
	private AlwaysMarqueeTextView musicName;
	private TextView musicSinger;
	private TextView musicTime, startMusicTime;
	private ViewPager mPlayViewPager;
	private SeekBar seekBar;
	private ArrayList<View> mViewList;
	private int oldPage = 0;// 上一次显示的页
	private ArrayList<View> dots = null;// 点
	private ArrayList<Mp3Info> mp3List;// 本地音乐的list
	private ImageView albumIV;
	private int currentPosition;
	private int currentPositionDown;// 记录我的下载的音乐里边的标志
	private String musicFlag;
	private  Handler mHandler;
	private MusicPlayService musicPlayService;
	private RelativeLayout playMusicBg;
	private MyApplication myApp;
	private LrcView mLrcViewOnSecondPage; // 7 lines lrc
	private LrcView mLrcViewOnFirstPage; // single line lrc
	private String intentMusicName;// 从主界面通过intent传递过来的歌曲的名字,目的是为了使用歌名搜索歌词
	private String downMusicSongTime;// 从主界面传递过来下载音乐的歌曲的时长
	private ArrayList<DownMucicInfo> downMusicList;// 我的下载的音乐的列表
	private int recordDownMusicPosition;// 记录点击我的下载歌曲里边的列表
	private MyBroadCast broadCast;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_music_play);
		initView();
		initData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(connection);
		unregisterReceiver(broadCast);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (intentMusicName != null) {
			setLrc(intentMusicName);
		}
	}

	private void initView() {
		playMusicBg = (RelativeLayout) findViewById(R.id.play_music_bg);
		mPlayViewPager = (ViewPager) findViewById(R.id.vp_play_container);
		btnBack = (ImageButton) findViewById(R.id.play_button_back);
		btnFunction = (ImageButton) findViewById(R.id.play_more_functions);
		btnPalyList = (ImageButton) findViewById(R.id.play_list);
		btnPlay = (ImageButton) findViewById(R.id.ib_play_start);
		btnPause = (ImageButton) findViewById(R.id.ib_play_pause);
		btnPrev = (ImageButton) findViewById(R.id.ib_play_pre);
		btnNext = (ImageButton) findViewById(R.id.ib_play_next);
		btnPlayMode = (ImageButton) findViewById(R.id.play_mode);
		musicName = (AlwaysMarqueeTextView) findViewById(R.id.tv_musicName_play);
		musicSinger = (TextView) findViewById(R.id.singer_play);
		musicTime = (TextView) findViewById(R.id.musicTime);
		startMusicTime = (TextView) findViewById(R.id.startMusicTime);
		btnPlayShared = (ImageButton) findViewById(R.id.play_shared);
		btnLike = (ImageButton) findViewById(R.id.ivLikeNormal);
		seekBar = (SeekBar) findViewById(R.id.play_progress);
		// 获取显示的点（即文字下方的点，表示当前是第几张）
		dots = new ArrayList<View>();
		dots.add(findViewById(R.id.dot1));
		dots.add(findViewById(R.id.dot2));
		seekBar.setOnSeekBarChangeListener(this);
		btnBack.setOnClickListener(this);
		btnFunction.setOnClickListener(this);
		btnPalyList.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnPause.setOnClickListener(this);
		btnPrev.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		btnPlayMode.setOnClickListener(this);
		btnPlayShared.setOnClickListener(this);
		btnLike.setOnClickListener(this);

		mViewList = new ArrayList<View>();
		LayoutInflater inflater = this.getLayoutInflater();
		View CDViewLayout = inflater.inflate(R.layout.play_vp_item1, null);
		albumIV = (ImageView) CDViewLayout.findViewById(R.id.infoOperating);
		View LRCLayout = inflater.inflate(R.layout.play_vp_item2, null);
		mLrcViewOnSecondPage = (LrcView) LRCLayout
				.findViewById(R.id.play_first_lrc_2);
		mLrcViewOnFirstPage = (LrcView) CDViewLayout
				.findViewById(R.id.play_first_lrc);
		mViewList.add(CDViewLayout);
		mViewList.add(LRCLayout);

	}

	@SuppressWarnings("deprecation")
	private void initData() {
		musicFlag = SharedPrefUtil.getString(this, Constant.MUSIC_FLAG,
				Constant.MY_LIKE_MUSIC);
		recordDownMusicPosition = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME_DOWN, -1);

		myApp = (MyApplication) getApplication();
		Intent intent = new Intent(this, MusicPlayService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
		
		broadCast = new MyBroadCast();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("updateText");
		registerReceiver(broadCast, intentFilter);
		
		mPlayViewPager.setAdapter(new MyAdapter());
		mPlayViewPager.setOnPageChangeListener(new MyListener());
		currentPosition = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME, -1);
		currentPositionDown = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME_DOWN, -1);
		mp3List = MediaUtils.getMp3Info(this);
		mp3List = new SortListUtil().initMyLocalMusic(mp3List);
		intentMusicName = getIntent().getStringExtra("Music_name");
		if (currentPosition != -1 && musicFlag.equals("local_music")) {
			musicName.setText(intentMusicName);
			musicSinger.setText("一   "
					+ mp3List.get(currentPosition).getArtist() + "  一");
			musicTime.setText(MediaUtils.formatTime(mp3List
					.get(currentPosition).getDuration()));
			Bitmap preBitmap = MediaUtils.getArtwork(this,
					mp3List.get(currentPosition).getId(),
					mp3List.get(currentPosition).getAlbumId(), true, false);
			Drawable boxBlurFilter = GaussianBlurUtil.BoxBlurFilter(preBitmap);
			albumIV.setImageBitmap(preBitmap);
			playMusicBg.setBackgroundDrawable(boxBlurFilter);
			startAnim();
		}
		if (MyTabMusic.sortMyLikeMp3Infos != null) {
			if (currentPosition != -1 && musicFlag.equals("like_music")) {
				musicName.setText(MyTabMusic.sortMyLikeMp3Infos.get(
						currentPosition).getTitle());
				musicSinger.setText("一   "
						+ MyTabMusic.sortMyLikeMp3Infos.get(currentPosition)
								.getArtist() + "  一");
				musicTime.setText(MediaUtils
						.formatTime(MyTabMusic.sortMyLikeMp3Infos.get(
								currentPosition).getDuration()));
				Bitmap preBitmap = MediaUtils.getArtwork(this,
						MyTabMusic.sortMyLikeMp3Infos.get(currentPosition)
								.getId(),
						MyTabMusic.sortMyLikeMp3Infos.get(currentPosition)
								.getAlbumId(), true, false);
				Drawable boxBlurFilter = GaussianBlurUtil
						.BoxBlurFilter(preBitmap);
				albumIV.setImageBitmap(preBitmap);
				playMusicBg.setBackgroundDrawable(boxBlurFilter);
				startAnim();
			}
		}
		if (recordDownMusicPosition != -1 && musicFlag.equals("down_music")) {
			// 下载歌曲的文件夹
			String MusicTarget = Environment.getExternalStorageDirectory()
					+ "/" + "/下载的歌曲";
			downMusicList = MediaUtils.GetMusicFiles(MusicTarget, ".mp3", true);
			String artist = getIntent().getStringExtra("Music_artist");
			downMusicSongTime = downMusicList.get(recordDownMusicPosition)
					.getDownMusicDuration();
			musicName.setText(intentMusicName);
			musicSinger.setText("一   " + artist + "  一");
			musicTime.setText(downMusicSongTime);
			final String ImageTarget = Environment
					.getExternalStorageDirectory() + "/" + "/下载的图片" + "/";

			Bitmap albumBit = BitmapFactory.decodeFile(ImageTarget
					+ downMusicList.get(recordDownMusicPosition)
							.getDownMusicName().trim() + ".jpg", null);
			if (albumBit != null) {
				albumIV.setImageBitmap(albumBit);
			}
			// 开始旋转专辑图片的动画
			Animation operatingAnim = AnimationUtils.loadAnimation(this,
					R.anim.tip);
			LinearInterpolator lin = new LinearInterpolator();
			operatingAnim.setInterpolator(lin);
			if (operatingAnim != null) {
				albumIV.startAnimation(operatingAnim);
			}
		}

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case Constant.UPDATA_TIME:
					int progress = (Integer) msg.obj;
					String formatTime = MediaUtils.formatTime(progress);
					startMusicTime.setText(formatTime);
					if (musicFlag.equals("local_music")) {
						if (seekBar.getProgress() <= mp3List.get(
								musicPlayService.getCurrentPosition())
								.getDuration()
								&& seekBar.getProgress() >= mp3List.get(
										musicPlayService.getCurrentPosition())
										.getDuration() - 1500) {
							
							musicPlayService.next();
//							localMusicNextOrPre();
							Log.e("走到了发送本地音乐", "走到了发送本地音乐");
							// 发送更新播放按钮的广播
							Intent intent = new Intent("updateText");
							sendBroadcast(intent);
							
						}
					} else if (musicFlag.equals("down_music")) {
						if (seekBar.getProgress() <= (MediaUtils
								.getTrackLength(downMusicList.get(
										musicPlayService.getCurrentPosition())
										.getDownMusicDuration()))
								&& seekBar.getProgress() >= (MediaUtils
										.getTrackLength(downMusicList.get(
												musicPlayService
														.getCurrentPosition())
												.getDownMusicDuration()) - 1500)) {
							musicPlayService.nextDownMusic();
//							downMusicNextOrPre();
							Log.e("走到了发送下载音乐", "走到了发送下载音乐");
							// 发送更新播放按钮的广播
							Intent intent = new Intent("updateText");
							sendBroadcast(intent);
						
						}
					}

					break;
				case DownMusicUtils.SUCCESS_LRC:
					String targrt1 = (String) msg.obj;
					setLrc(targrt1);
					break;
				case Constant.UPTATE_LRC:
					int progress1 = (Integer) msg.obj;
					if (mLrcViewOnSecondPage.hasLrc())
						mLrcViewOnSecondPage.changeCurrent(progress1);
					if (mLrcViewOnFirstPage.hasLrc())
						mLrcViewOnFirstPage.changeCurrent(progress1);
					break;
				case DownMusicUtils.FAILED_LRC:
					Toast.makeText(MusicPlayAvtivity.this, "歌词下载失败", 0).show();
					break;
				case Constant.SUCCESS:
					String targrt = (String) msg.obj;
					setLrc(targrt);
					break;
				}
			}
		};

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("ShowToast")
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.play_button_back: {
			finish();
			break;
		}
		case R.id.play_list:
			PlayListPopuWindow popuWindow = new PlayListPopuWindow(this);
			popuWindow.showAsDropDown(v);
			break;
		case R.id.ib_play_start: {
			if (currentPosition == -1) {
				return;
			}
			if (currentPosition != -1 && Constant.ISFirst_PLAY) {
				Constant.isFirst = false;
				musicPlayService.playLocalMusic(currentPosition);
				// 发送更新播放按钮的广播
				Intent intent = new Intent(Constant.PLAYBUTTON_BROAD);
				this.sendBroadcast(intent);
				musicPlayService.start();
				btnPause.setVisibility(View.VISIBLE);
				btnPlay.setVisibility(View.GONE);
				Constant.ISFirst_PLAY = false;
			} else if (!Constant.ISFirst_PLAY) {
				// 发送更新播放按钮的广播
				Intent intent = new Intent(Constant.PLAYBUTTON_BROAD);
				this.sendBroadcast(intent);
				musicPlayService.start();
				btnPause.setVisibility(View.VISIBLE);
				btnPlay.setVisibility(View.GONE);
			}
			break;
		}
		case R.id.ib_play_pause: {
			musicPlayService.pause();
			// 发送更新暂停按钮的广播
			Intent pauseIntent = new Intent(Constant.PAUSEBUTTON_BROAD);
			this.sendBroadcast(pauseIntent);
			btnPause.setVisibility(View.GONE);
			btnPlay.setVisibility(View.VISIBLE);
			break;
		}
		case R.id.ib_play_pre: {

			if (musicFlag.equals("local_music")) {
				musicPlayService.previous();
				localMusicNextOrPre();
			} else if (musicFlag.equals("down_music")) {
				musicPlayService.previousDown();
				downMusicNextOrPre();
			}
			break;
		}
		case R.id.ib_play_next: {

			if (musicFlag.equals("local_music")) {
				musicPlayService.next();
				localMusicNextOrPre();
			} else if (musicFlag.equals("down_music")) {
				musicPlayService.nextDownMusic();
				downMusicNextOrPre();
			}

			break;
		}
		case R.id.play_mode: {
			int tag = (Integer) btnPlayMode.getTag();
			switch (tag) {
			case MusicPlayService.PLAY_ORDER:
				btnPlayMode
						.setImageResource(R.drawable.player_btn_random_normal);
				btnPlayMode.setTag(MusicPlayService.PLAY_RANDOM);
				musicPlayService.setPlayMode(MusicPlayService.PLAY_RANDOM);
				Toast.makeText(this, "随机播放", 0).show();
				break;
			case MusicPlayService.PLAY_RANDOM:
				btnPlayMode
						.setImageResource(R.drawable.player_btn_repeatone_highlight);
				btnPlayMode.setTag(MusicPlayService.PLAY_SINGLE);
				musicPlayService.setPlayMode(MusicPlayService.PLAY_SINGLE);
				Toast.makeText(this, "单曲循环", 0).show();
				break;
			case MusicPlayService.PLAY_SINGLE:
				btnPlayMode
						.setImageResource(R.drawable.player_btn_repeat_highlight);
				btnPlayMode.setTag(MusicPlayService.PLAY_ORDER);
				musicPlayService.setPlayMode(MusicPlayService.PLAY_ORDER);
				Toast.makeText(this, "列表循环", 0).show();
				break;
			}
		}
			break;
		case R.id.play_shared: {
		}
			break;
		case R.id.ivLikeNormal: {

			if (musicFlag.equals("local_music")) {
				Mp3Info mp3Info = mp3List.get(musicPlayService
						.getCurrentPosition());
				try {
					Mp3Info myLikeMp3Info = myApp.dbUtils.findFirst(Selector
							.from(Mp3Info.class).where("mp3InfoId", "=",
									mp3Info.getId()));
					if (myLikeMp3Info == null) {
						mp3Info.setMp3InfoId(mp3Info.getId());
						myApp.dbUtils.save(mp3Info);
						btnLike.setImageResource(R.drawable.player_btn_favorited_normal);
					} else {
						myApp.dbUtils.deleteById(Mp3Info.class,
								myLikeMp3Info.getId());

						btnLike.setImageResource(R.drawable.player_btn_favorite_highlight);
					}
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
			// else if (musicFlag.equals("down_music")) {
			// DownMucicInfo downMucicInfo = downMusicList
			// .get(musicPlayService.getCurrentPosition());
			// try {
			//
			// DownMucicInfo myLikeDownMp3Info = myApp.dbUtils
			// .findFirst(Selector.from(DownMucicInfo.class)
			// .where("downMp3InfoName",
			// "=",
			// downMucicInfo
			// .getMyLikeDownMusicName()));
			// if (myLikeDownMp3Info == null) {
			// downMucicInfo.setMyLikeDownMusicName(downMucicInfo
			// .getMyLikeDownMusicName());
			// myApp.dbUtils.save(downMucicInfo);
			// btnLike.setImageResource(R.drawable.player_btn_favorited_normal);
			// } else {
			// myApp.dbUtils.deleteById(DownMucicInfo.class,
			// downMucicInfo.getMyLikeDownMusicName());
			// btnLike.setImageResource(R.drawable.player_btn_favorite_highlight);
			// }
			// } catch (DbException e) {
			// e.printStackTrace();
			// }
			// }

		}
			break;
		}
	}

	// 当是在本地音乐列表的是点击下一首调用的方法
	private void localMusicNextOrPre() {
		if (currentPosition == -1) {
			return;
		}

		intentMusicName = mp3List.get(musicPlayService.getCurrentPosition())
				.getTitle();
		updateProgress();
		 setLrc(intentMusicName);
		btnPause.setVisibility(View.VISIBLE);
		btnPlay.setVisibility(View.GONE);
		// 切换数据
		int nextPosition = musicPlayService.getCurrentPosition();
		SharedPrefUtil.setInt(this, Constant.CLICKED_MUNSIC_NAME, nextPosition);
		musicName.setText(mp3List.get(nextPosition).getTitle());
		musicSinger.setText("一   " + mp3List.get(nextPosition).getArtist()
				+ "  一");
		musicTime.setText(MediaUtils.formatTime(mp3List.get(nextPosition)
				.getDuration()));
		Bitmap nextBitmap = MediaUtils.getArtwork(this,
				mp3List.get(nextPosition).getId(), mp3List.get(nextPosition)
						.getAlbumId(), true, false);
		albumIV.setImageBitmap(nextBitmap);
		Drawable boxBlurFilter1 = GaussianBlurUtil.BoxBlurFilter(nextBitmap);
		playMusicBg.setBackgroundDrawable(boxBlurFilter1);
		startAnim();
	}

	// 当是在我的下载的音乐列表点击下一首的时候调用的方法
	private void downMusicNextOrPre() {
		if (currentPositionDown == -1) {
			return;
		}
		intentMusicName = downMusicList.get(
				musicPlayService.getCurrentPosition()).getDownMusicName();
		 updateProgress();
		 setLrc(intentMusicName);
		btnPause.setVisibility(View.VISIBLE);
		btnPlay.setVisibility(View.GONE);
		// 切换数据
		int nextPosition = musicPlayService.getCurrentPosition();
		SharedPrefUtil.setInt(this, Constant.CLICKED_MUNSIC_NAME_DOWN,
				nextPosition);
		musicName.setText(downMusicList.get(nextPosition).getDownMusicName());
		musicSinger.setText("一   "
				+ downMusicList.get(nextPosition).getDownMusicArtist() + "  一");
		musicTime.setText(MediaUtils.formatTime(MediaUtils
				.getTrackLength(downMusicList.get(nextPosition)
						.getDownMusicDuration())));
		// 下载歌曲的文件夹
		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的歌曲";
		downMusicList = MediaUtils.GetMusicFiles(MusicTarget, ".mp3", true);
		final String ImageTarget = Environment.getExternalStorageDirectory()
				+ "/" + "/下载的图片" + "/";
		// 得到当前播放的歌曲
		int curr = musicPlayService.getCurrentPosition();
		Bitmap albumBit = BitmapFactory.decodeFile(ImageTarget
				+ downMusicList.get(curr).getDownMusicName().trim() + ".jpg",
				null);
		if (albumBit != null) {
			albumIV.setImageBitmap(albumBit);
			Drawable boxBlurFilter1 = GaussianBlurUtil.BoxBlurFilter(albumBit);
			playMusicBg.setBackgroundDrawable(boxBlurFilter1);
		}
		startAnim();
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
			updateProgress();
			// 拿到是否播放了
			boolean isPlaying = musicPlayService.isPlaying();
			if (isPlaying == true) {
				btnPause.setVisibility(View.VISIBLE);
				btnPlay.setVisibility(View.GONE);
			} else {
				btnPause.setVisibility(View.GONE);
				btnPlay.setVisibility(View.VISIBLE);
			}

			initPlayMode();
			// 初始化收藏歌曲的状态
			Mp3Info mp3Info = mp3List
					.get(musicPlayService.getCurrentPosition());
			try {
				Mp3Info myLikeMp3Info = myApp.dbUtils
						.findFirst(Selector.from(Mp3Info.class).where(
								"mp3InfoId", "=", mp3Info.getId()));
				if (myLikeMp3Info != null) {
					btnLike.setImageResource(R.drawable.player_btn_favorited_normal);
				} else {
					btnLike.setImageResource(R.drawable.player_btn_favorite_highlight);
				}
			} catch (DbException e) {
				e.printStackTrace();
			}
			updateLRC();
			Log.e("重新绑定了", "重新绑定了");
		}

	};



	// 开启线程 更新进度条
	private void updateProgress() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {

						if (downMusicSongTime != null) {
							seekBar.setMax((int) MediaUtils
									.getTrackLength(downMusicList.get(
											musicPlayService
													.getCurrentPosition())
											.getDownMusicDuration()));
							seekBar.setProgress(musicPlayService
									.getCurrentProgress());

						} else {
							// 设置的是当前播放歌曲的最大值
							seekBar.setMax((int) mp3List.get(
									musicPlayService.getCurrentPosition())
									.getDuration());
							seekBar.setProgress(musicPlayService
									.getCurrentProgress());
						}

						Message message1 = mHandler.obtainMessage(
								Constant.UPDATA_TIME,
								musicPlayService.getCurrentProgress());
						Message message2 = mHandler.obtainMessage(
								Constant.UPTATE_LRC,
								musicPlayService.getCurrentProgress());
						message1.sendToTarget();
						message2.sendToTarget();
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	* @Description更新歌词 
	* @return void 
	* @author bai
	*/
	private void updateLRC() {
		new Thread(new Runnable() {
			// 初始化收藏歌曲的状态
			@Override
			public void run() {
				// 歌词
				String LrcPath = Environment.getExternalStorageDirectory()
						+ "/" + "/下载的歌词" + "/" + intentMusicName + ".lrc";
				File lrcFile = new File(LrcPath);
				if (!lrcFile.exists()) {
					// 下载
					SearchMusicUtil.getInstance()
							.setListener(new onSearchResultListener() {

								@Override
								public void onSearchResult(
										ArrayList<SearchResult> results) {
									if (results != null && results.size() != 0) {

										SearchResult searchResult = results
												.get(0);
										String url = Constant.BAIDU_URL
												+ searchResult.getUrl();
										DownMusicUtils
												.getInstance()
												.downLoadLRC(
														url,
														searchResult
																.getMusicName(),
														mHandler);
									}
								}
							}).search(intentMusicName, 1);
				} else {
					setLrc(intentMusicName);
				}
			}
		}).start();
	}

	/**
	* @Description:设置歌词 
	* @param musicTitle
	* @return void 
	* @author bai
	*/
	private void setLrc(String musicTitle) {
		String target = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的歌词" + "/" + intentMusicName.trim() + ".lrc";
		mLrcViewOnSecondPage.setLrcPath(target);
		mLrcViewOnFirstPage.setLrcPath(target);
	}

	/**
	* @Description:初始化播放模式 
	* @return void 
	* @author bai
	*/
	private void initPlayMode() {
		switch (musicPlayService.getPlayMode()) {
		case MusicPlayService.PLAY_ORDER:
			btnPlayMode
					.setImageResource(R.drawable.player_btn_repeat_highlight);
			btnPlayMode.setTag(MusicPlayService.PLAY_ORDER);
			break;
		case MusicPlayService.PLAY_RANDOM:
			btnPlayMode.setImageResource(R.drawable.player_btn_random_normal);
			btnPlayMode.setTag(MusicPlayService.PLAY_RANDOM);
			break;

		case MusicPlayService.PLAY_SINGLE:
			btnPlayMode
					.setImageResource(R.drawable.player_btn_repeatone_highlight);
			btnPlayMode.setTag(MusicPlayService.PLAY_SINGLE);
			break;
		}
	}

	// 拖动进度条要实现的方法
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (fromUser) {
			musicPlayService.seekTo(progress);
			startMusicTime.setText(MediaUtils.formatTime(musicPlayService
					.getCurrentProgress()));
			// 拖动的时候暂停
			musicPlayService.pause();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		seekBar.setProgress(musicPlayService.getCurrentProgress());
		// 拖动完成后继续播放
		musicPlayService.start();
		int progress = seekBar.getProgress();
		mLrcViewOnSecondPage.onDrag(progress);
		mLrcViewOnFirstPage.onDrag(progress);

	}

	// 开始旋转专辑图片的动画
	private void startAnim() {
		Animation operatingAnim = AnimationUtils.loadAnimation(this,
				R.anim.tip_play);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		if (operatingAnim != null) {
			albumIV.startAnimation(operatingAnim);
		}
	}

	class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mViewList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view2 = mViewList.get(position);
			container.addView(view2);
			return view2;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}

	class MyListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int arg0) {
			int currentItem = mPlayViewPager.getCurrentItem();
			switch (currentItem) {
			case 0:
				mPlayViewPager.setCurrentItem(0);
				break;
			case 1:
				mPlayViewPager.setCurrentItem(1);
				break;
			}
			// 改变点的状态
			dots.get(arg0).setBackgroundResource(R.drawable.dot_focused);
			dots.get(oldPage).setBackgroundResource(R.drawable.dot_normal);
			// 记录的页面
			oldPage = arg0;
		}
	}
	
	// 接受到广播更新数据
	public class MyBroadCast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("updateText")) {
				if (musicFlag.equals("local_music")) {
					localMusicNextOrPre();
				} else if (musicFlag.equals("down_music")) {
					downMusicNextOrPre();
				}
			}
		}

	}

}
