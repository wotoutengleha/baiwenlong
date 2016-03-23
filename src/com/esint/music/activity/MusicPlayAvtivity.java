package com.esint.music.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import name.teze.layout.lib.SwipeBackActivity;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
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

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.OnekeyShareTheme;

import com.esint.music.R;
import com.esint.music.db.MySQLite;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.model.Mp3Info;
import com.esint.music.model.SearchMusicInfo;
import com.esint.music.model.SearchResult;
import com.esint.music.service.MusicPlayService;
import com.esint.music.service.MusicPlayService.PlayBinder;
import com.esint.music.utils.ActivityCollectUtil;
import com.esint.music.utils.AniUtil;
import com.esint.music.utils.Constant;
import com.esint.music.utils.DownMusicUtils;
import com.esint.music.utils.GaussianBlurUtil;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.MyHttpUtils;
import com.esint.music.utils.SearchMusicUtil;
import com.esint.music.utils.SortListUtil;
import com.esint.music.utils.SearchMusicUtil.onSearchResultListener;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.view.AlwaysMarqueeTextView;
import com.esint.music.view.LrcView;
import com.esint.music.view.PlayListDownPopu;
import com.esint.music.view.PlayListLikePopu;
import com.esint.music.view.PlayListPopuWindow;
import com.esint.music.view.PlayListPopuWindow.OnItemClickListener;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

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
	private int currentPositionLike;// 记录我的喜欢的音乐里边的标志
	private String musicFlag;
	private Handler mHandler;
	private MusicPlayService musicPlayService;
	private RelativeLayout playMusicBg;
	private LrcView mLrcViewOnSecondPage; // 7 lines lrc
	private LrcView mLrcViewOnFirstPage; // single line lrc
	private String intentMusicName;// 从主界面通过intent传递过来的歌曲的名字,目的是为了使用歌名搜索歌词
	private String intentArtist;// 从主界面通过intent传递过来的歌手的名字,目的是为了使用歌名搜索歌词
	private String downMusicSongTime;// 从主界面传递过来下载音乐的歌曲的时长
	private ArrayList<DownMucicInfo> downMusicList;// 我的下载的音乐的列表
	private int recordDownMusicPosition;// 记录点击我的下载歌曲里边的列表
	private int recordLikeMusicPosition;// 记录我的喜欢的歌曲里边的列表
	private MyBroadCast broadCast;
	private ImageView mMoveIv;// 添加喜欢的音乐时的动画图片
	private String imageTarget;// 下载的图片的文件路径
	private String musicTarget;// 下载音乐的路径
	private MyHttpUtils myHttpUtils;
	private HttpUtils httpUtils;// XUtils
	private ArrayList<SearchMusicInfo> searchMusicList = new ArrayList<SearchMusicInfo>();
	private String mp3Url;// 用来分享的音乐的地址
	private String picUrl;// 用来分享音乐的图片的地址

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_music_play);
		ActivityCollectUtil.addActivity(this);
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
		mMoveIv = (ImageView) findViewById(R.id.move_iv);
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
		recordLikeMusicPosition = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME_LIKE, -1);
		myHttpUtils = new MyHttpUtils(this);
		httpUtils = new HttpUtils();
		Intent intent = new Intent(this, MusicPlayService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);

		broadCast = new MyBroadCast();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("updateLocalText");
		intentFilter.addAction("updateDownText");
		intentFilter.addAction("updateLikeText");
		registerReceiver(broadCast, intentFilter);

		mPlayViewPager.setAdapter(new MyAdapter());
		mPlayViewPager.setOnPageChangeListener(new MyListener());
		currentPosition = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME, -1);
		currentPositionDown = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME_DOWN, -1);
		currentPositionLike = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME_LIKE, -1);
		mp3List = MediaUtils.getMp3Info(this);
		mp3List = new SortListUtil().initMyLocalMusic(mp3List);

		musicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的歌曲";
		downMusicList = MediaUtils.GetMusicFiles(musicTarget, ".mp3", true);
		imageTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的图片" + "/";

		intentMusicName = getIntent().getStringExtra("Music_name");
		intentArtist = getIntent().getStringExtra("Music_artist");
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
		if (recordLikeMusicPosition != -1 && musicFlag.equals("like_music")
				&& MainFragmentActivity.likeMusciList.size() != 0) {
			musicName.setText(MainFragmentActivity.likeMusciList.get(
					recordLikeMusicPosition).getMusicName());
			musicSinger.setText("一   "
					+ MainFragmentActivity.likeMusciList.get(
							recordLikeMusicPosition).getMusicArtist() + "  一");
			Long musicTime2 = MainFragmentActivity.likeMusciList.get(
					recordLikeMusicPosition).getMusicTime();
			Log.e("musicTime2", musicTime2 + "");
			musicTime.setText(MediaUtils
					.formatTime(MainFragmentActivity.likeMusciList.get(
							recordLikeMusicPosition).getMusicTime()));

			// 模糊效果
			Drawable boxBlurFilter = GaussianBlurUtil
					.BoxBlurFilter(MainFragmentActivity.likeMusciList.get(
							recordLikeMusicPosition).getBitmap());
			playMusicBg.setBackgroundDrawable(boxBlurFilter);
			albumIV.setImageBitmap(MainFragmentActivity.likeMusciList.get(
					recordLikeMusicPosition).getBitmap());
			startAnim();
		}
		if (recordDownMusicPosition != -1 && musicFlag.equals("down_music")) {

			downMusicSongTime = downMusicList.get(recordDownMusicPosition)
					.getDownMusicDuration();
			musicName.setText(intentMusicName);
			musicSinger.setText("一   " + intentArtist + "  一");
			musicTime.setText(downMusicSongTime);

			Bitmap albumBit = BitmapFactory.decodeFile(imageTarget
					+ downMusicList.get(recordDownMusicPosition)
							.getDownMusicName().trim() + ".jpg", null);
			if (albumBit != null) {
				albumIV.setImageBitmap(albumBit);
				// 模糊效果
				Drawable boxBlurFilter = GaussianBlurUtil
						.BoxBlurFilter(albumBit);
				playMusicBg.setBackgroundDrawable(boxBlurFilter);
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

					int a = musicPlayService.getCurrentPosition();

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
							// localMusicNextOrPre();
							Log.e("走到了发送本地音乐", "走到了发送本地音乐");
							SharedPrefUtil.setString(MusicPlayAvtivity.this,
									Constant.MUSIC_FLAG,
									Constant.MY_LOCAL_MUSIC);
							// 发送更新播放按钮的广播
							Intent intent = new Intent("updateLocalText");
							sendBroadcast(intent);

						}
					} else if (musicFlag.equals("down_music")
							&& a <= downMusicList.size()) {
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
							// downMusicNextOrPre();
							Log.e("走到了发送下载音乐", "走到了发送下载音乐");
							SharedPrefUtil
									.setString(MusicPlayAvtivity.this,
											Constant.MUSIC_FLAG,
											Constant.MY_DOWN_MUSIC);
							// 发送更新播放按钮的广播
							Intent intent = new Intent("updateDownText");
							sendBroadcast(intent);

						}
					} else if (musicFlag.equals("like_music")
							&& MainFragmentActivity.likeMusciList.size() != 0) {

						if (seekBar.getProgress() <= MainFragmentActivity.likeMusciList
								.get(musicPlayService.getCurrentPosition())
								.getMusicTime()
								&& seekBar.getProgress() >= MainFragmentActivity.likeMusciList
										.get(musicPlayService
												.getCurrentPosition())
										.getMusicTime() - 1500) {

							musicPlayService.nextLikeMusic();
							// downMusicNextOrPre();
							Log.e("走到了发送喜欢音乐", "走到了发送喜欢音乐");
							SharedPrefUtil
									.setString(MusicPlayAvtivity.this,
											Constant.MUSIC_FLAG,
											Constant.MY_LIKE_MUSIC);
							// 发送更新播放按钮的广播
							Intent intent = new Intent("updateLikeText");
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
		MyHttpUtils.handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case Constant.WHAT_NET_HOTMUSIC_LIST: {
					Log.e("接收到了消息", "接收到了消息");
					searchMusicList
							.addAll((ArrayList<SearchMusicInfo>) msg.obj);
					String musicID = searchMusicList.get(0).getMusicID();
					getDownUrl(searchMusicList);
				}

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

			if (musicFlag.equals("local_music")) {

				int isPlayingPosi = musicPlayService.getCurrentPosition();

				PlayListPopuWindow popuWindow = new PlayListPopuWindow(this,
						isPlayingPosi);
				Log.e("isPlayingPosi", isPlayingPosi + "");
				popuWindow.showAsDropDown(v);
				popuWindow.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(int index) {
						musicPlayService.playLocalMusic(index);
						setLrc(intentMusicName);
						localMusicNextOrPre();

					}
				});
			} else if (musicFlag.equals("down_music")) {

				int isPlayingPosi = musicPlayService.getCurrentPosition();

				PlayListDownPopu popuWindow = new PlayListDownPopu(this,
						isPlayingPosi);
				Log.e("isPlayingPosi", isPlayingPosi + "");
				popuWindow.showAsDropDown(v);
				popuWindow
						.setOnItemClickListener(new PlayListDownPopu.OnItemClickListener() {

							@Override
							public void onItemClick(int index) {
								musicPlayService.playMyDown(index);
								setLrc(intentMusicName);
								downMusicNextOrPre();
							}
						});
			} else if (musicFlag.equals("like_music")
					&& MainFragmentActivity.likeMusciList.size() != 0) {

				int isPlayingPosi = musicPlayService.getCurrentPosition();
				PlayListLikePopu popuWindow = new PlayListLikePopu(this,
						isPlayingPosi);
				Log.e("isPlayingPosi", isPlayingPosi + "");
				popuWindow.showAsDropDown(v);
				popuWindow
						.setOnItemClickListener(new PlayListLikePopu.OnItemClickListener() {

							@Override
							public void onItemClick(int index) {
								musicPlayService.playMyFav(index);
								setLrc(intentMusicName);
								likeMusicNextOrPre();
							}
						});
			}

			break;
		case R.id.ib_play_start: {
			if (currentPosition == -1) {
				return;
			}
			if (currentPosition != -1 && Constant.ISFirst_PLAY
					&& musicFlag.equals("local_music")) {
				Constant.isFirst = false;
				musicPlayService.playLocalMusic(currentPosition);
				// 发送更新播放按钮的广播
				Intent intent = new Intent(Constant.PLAYBUTTON_BROAD);
				this.sendBroadcast(intent);
				musicPlayService.start();
				btnPause.setVisibility(View.VISIBLE);
				btnPlay.setVisibility(View.GONE);
				Constant.ISFirst_PLAY = false;
				updateLocalProgress();
			} else if (currentPositionDown != -1 && Constant.ISFirst_PLAY
					&& musicFlag.equals("down_music")) {
				Constant.isFirst = false;
				musicPlayService.playMyDown(currentPositionDown);
				// 发送更新播放按钮的广播
				Intent intent = new Intent(Constant.PLAYBUTTON_BROAD);
				this.sendBroadcast(intent);
				musicPlayService.start();
				btnPause.setVisibility(View.VISIBLE);
				btnPlay.setVisibility(View.GONE);
				Constant.ISFirst_PLAY = false;
				updateDownProgress();
			} else if (currentPositionLike != -1 && Constant.ISFirst_PLAY
					&& musicFlag.equals("like_music")
					&& MainFragmentActivity.likeMusciList.size() != 0) {
				Constant.isFirst = false;
				musicPlayService.playMyFav(currentPositionDown);
				// 发送更新播放按钮的广播
				Intent intent = new Intent(Constant.PLAYBUTTON_BROAD);
				this.sendBroadcast(intent);
				musicPlayService.start();
				btnPause.setVisibility(View.VISIBLE);
				btnPlay.setVisibility(View.GONE);
				Constant.ISFirst_PLAY = false;
				updateLikeProgress();
			}

			else if (!Constant.ISFirst_PLAY) {
				// 发送更新播放按钮的广播
				Intent intent = new Intent(Constant.PLAYBUTTON_BROAD);
				this.sendBroadcast(intent);
				musicPlayService.start();
				btnPause.setVisibility(View.VISIBLE);
				btnPlay.setVisibility(View.GONE);
			}

			// updateProgress();
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
				Constant.isInsert = false;
			} else if (musicFlag.equals("down_music")) {
				musicPlayService.previousDown();
				downMusicNextOrPre();
				Constant.isInsert = false;
			} else if (musicFlag.equals("like_music")
					&& MainFragmentActivity.likeMusciList.size() != 0) {
				musicPlayService.previousLike();
				likeMusicNextOrPre();
				Constant.isInsert = false;
			}
			break;
		}
		case R.id.ib_play_next: {

			if (musicFlag.equals("local_music")) {
				musicPlayService.next();
				Constant.isInsert = false;
				localMusicNextOrPre();
			} else if (musicFlag.equals("down_music")) {
				musicPlayService.nextDownMusic();
				Constant.isInsert = false;
				downMusicNextOrPre();
			} else if (musicFlag.equals("like_music")
					&& MainFragmentActivity.likeMusciList.size() != 0) {
				musicPlayService.nextLikeMusic();
				Constant.isInsert = false;
				likeMusicNextOrPre();
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
			MainFragmentActivity.mHandler.sendEmptyMessage(0000);
		}
			break;
		case R.id.play_shared: {

			searchShakeMusic();

		}
			break;
		case R.id.ivLikeNormal: {
			// 得到当前播放的歌曲
			int playPosi = musicPlayService.getCurrentPosition();
			boolean isInsert = Constant.isInsert;
			if (isInsert == true) {
				if (musicFlag.equals("local_music")) {
					String title = mp3List.get(playPosi).getTitle();
					// 取消喜欢 删除数据
					String whereClause = "MusicTitle=?";// 删除的条件
					String[] whereArgs = { title };// 删除的条件参数
					MainFragmentActivity.db.delete("Music", whereClause,
							whereArgs);// 执行删除
					// 取消喜欢成功
					Log.e("取消本地音乐喜欢成功", "取消本地音乐喜欢成功");
					Toast.makeText(MusicPlayAvtivity.this, "取消喜欢", 0).show();
					btnLike.setImageResource(R.drawable.player_btn_favorite_highlight);
					Constant.isInsert = false;

				} else if (musicFlag.equals("down_music")) {
					String title = downMusicList.get(playPosi)
							.getDownMusicName();
					// 取消喜欢 删除数据
					String whereClause = "MusicTitle=?";// 删除的条件
					String[] whereArgs = { title };// 删除的条件参数
					MainFragmentActivity.db.delete("Music", whereClause,
							whereArgs);// 执行删除
					// 取消喜欢成功
					Toast.makeText(MusicPlayAvtivity.this, "取消喜欢", 0).show();
					Log.e("取消下载音乐喜欢成功", "取消下载音乐喜欢成功");
					btnLike.setImageResource(R.drawable.player_btn_favorite_highlight);
					Constant.isInsert = false;
				}

			} else {

				ContentValues values = new ContentValues();
				if (musicFlag.equals("local_music")
						&& Constant.isInsert == false) {
					String musicTitle = mp3List.get(playPosi).getTitle();
					String MusicArtist = mp3List.get(playPosi).getArtist();
					String musicTime = mp3List.get(playPosi).getDuration() + "";
					String MusicUrl = mp3List.get(playPosi).getUrl();
					// 找到当前播放音乐的大图 将图片转换成字节数组插入到数据库表
					Bitmap bitmap = MediaUtils.getArtwork(this,
							mp3List.get(playPosi).getId(), mp3List
									.get(playPosi).getAlbumId(), true, false);
					byte[] imgByte = MySQLite.img(bitmap);

					values.put("MusicTitle", musicTitle);
					values.put("MusicArtist", MusicArtist);
					values.put("MusicTime", musicTime);
					values.put("MusicUrl", MusicUrl);
					values.put("MusicImg", imgByte);
					MainFragmentActivity.db.insert("Music", null, values);
					Log.e("本地音乐插入数据库成功", "本地插入数据库成功");
					Toast.makeText(MusicPlayAvtivity.this, "添加喜欢", 0).show();
					AniUtil.startAnimationLike(mMoveIv);
					btnLike.setImageResource(R.drawable.player_btn_favorited_normal);
					Constant.isInsert = true;
				} else if (musicFlag.equals("down_music")
						&& Constant.isInsert == false) {

					String downMusicName = downMusicList.get(playPosi)
							.getDownMusicName();
					String downMusicArtist = downMusicList.get(playPosi)
							.getDownMusicArtist();
					String downMusicUrl = downMusicList.get(playPosi)
							.getDownMusicUrl();

					Bitmap albumBit = BitmapFactory.decodeFile(imageTarget
							+ downMusicList.get(playPosi).getDownMusicName()
									.trim() + ".jpg", null);
					byte[] imgByte = MySQLite.img(albumBit);
					// 转换一下时间格式
					long downMusicTime = MediaUtils
							.getTrackLength(downMusicList.get(playPosi)
									.getDownMusicDuration());
					Log.e("插入下载音乐的时间", downMusicTime + "");
					values.put("MusicTitle", downMusicName);
					values.put("MusicArtist", downMusicArtist);
					values.put("MusicTime", downMusicTime);
					values.put("MusicUrl", downMusicUrl);
					values.put("MusicImg", imgByte);
					MainFragmentActivity.db.insert("Music", null, values);
					Toast.makeText(MusicPlayAvtivity.this, "添加喜欢", 0).show();
					Log.e("下载音乐插入数据库成功", "下载插入数据库成功");
					btnLike.setImageResource(R.drawable.player_btn_favorited_normal);
					AniUtil.startAnimationLike(mMoveIv);
					Constant.isInsert = true;
				}
			}
		}
			break;
		}
	}

	// 判断当前音乐是否为喜欢的音乐
	private boolean isLikeMusic(String isPlayMusicTitle) {

		Cursor cursor = MainFragmentActivity.db.query("Music", null, null,
				null, null, null, null);// 查询并获得游标
		if (cursor.moveToFirst()) {
			do {
				String musicTitle = cursor.getString(cursor
						.getColumnIndex("MusicTitle"));
				if (isPlayMusicTitle.trim().equals(musicTitle.trim())) {
					cursor.close();
					Constant.isInsert = true;
					return true;
				}
			} while (cursor.moveToNext());

		}
		Constant.isInsert = false;
		return false;
	}

	// 当是在本地音乐列表的是点击下一首调用的方法
	private void localMusicNextOrPre() {
		if (currentPosition == -1) {
			return;
		}
		intentMusicName = mp3List.get(musicPlayService.getCurrentPosition())
				.getTitle();
		updateLocalProgress();
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
		// 更新通知栏
		musicPlayService.updateNotification(nextBitmap,
				mp3List.get(nextPosition).getTitle(), mp3List.get(nextPosition)
						.getArtist());
		boolean likeMusic = isLikeMusic(musicName.getText().toString());
		if (likeMusic == true) {
			btnLike.setImageResource(R.drawable.player_btn_favorited_normal);
		} else {
			btnLike.setImageResource(R.drawable.player_btn_favorite_highlight);
		}
	}

	// 当是在我的下载的音乐列表点击下一首的时候调用的方法
	private void downMusicNextOrPre() {
		if (currentPositionDown == -1) {
			return;
		}
		intentMusicName = downMusicList.get(
				musicPlayService.getCurrentPosition()).getDownMusicName();
		updateDownProgress();
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
		musicPlayService.updateNotification(albumBit,
				downMusicList.get(nextPosition).getDownMusicName(),
				downMusicList.get(nextPosition).getDownMusicArtist());
		boolean likeMusic = isLikeMusic(musicName.getText().toString());
		if (likeMusic == true) {
			btnLike.setImageResource(R.drawable.player_btn_favorited_normal);
		} else {
			btnLike.setImageResource(R.drawable.player_btn_favorite_highlight);
		}
	}

	// 当是在我的喜欢的音乐列表点击下一首的时候调用的方法
	private void likeMusicNextOrPre() {
		if (currentPositionLike == -1) {
			return;
		}
		intentMusicName = MainFragmentActivity.likeMusciList.get(
				musicPlayService.getCurrentPosition()).getMusicName();
		updateLikeProgress();
		setLrc(intentMusicName);
		btnPause.setVisibility(View.VISIBLE);
		btnPlay.setVisibility(View.GONE);
		// 切换数据
		int nextPosition = musicPlayService.getCurrentPosition();
		SharedPrefUtil.setInt(this, Constant.CLICKED_MUNSIC_NAME_LIKE,
				nextPosition);
		musicName.setText(MainFragmentActivity.likeMusciList.get(nextPosition)
				.getMusicName());
		musicSinger.setText("一   "
				+ MainFragmentActivity.likeMusciList.get(nextPosition)
						.getMusicArtist() + "  一");
		musicTime.setText(MediaUtils
				.formatTime(MainFragmentActivity.likeMusciList.get(
						recordLikeMusicPosition).getMusicTime()));
		int curr = musicPlayService.getCurrentPosition();
		Drawable boxBlurFilter1 = GaussianBlurUtil
				.BoxBlurFilter(MainFragmentActivity.likeMusciList.get(curr)
						.getBitmap());
		playMusicBg.setBackgroundDrawable(boxBlurFilter1);
		albumIV.setImageBitmap(MainFragmentActivity.likeMusciList.get(curr)
				.getBitmap());
		startAnim();
		musicPlayService.updateNotification(null,
				MainFragmentActivity.likeMusciList.get(nextPosition)
						.getMusicName(), MainFragmentActivity.likeMusciList
						.get(nextPosition).getMusicArtist());
		boolean likeMusic = isLikeMusic(musicName.getText().toString());
		if (likeMusic == true) {
			btnLike.setImageResource(R.drawable.player_btn_favorited_normal);
		} else {
			btnLike.setImageResource(R.drawable.player_btn_favorite_highlight);
		}
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
			// 拿到是否播放了
			boolean isPlaying = musicPlayService.isPlaying();
			if (isPlaying == true) {
				if (musicFlag.equals("local_music")) {
					updateLocalProgress();
				} else if (musicFlag.equals("down_music")) {
					updateDownProgress();
				} else if (musicFlag.equals("like_music")
						&& MainFragmentActivity.likeMusciList.size() != 0) {
					updateLikeProgress();
				}
				btnPause.setVisibility(View.VISIBLE);
				btnPlay.setVisibility(View.GONE);
			} else {
				btnPause.setVisibility(View.GONE);
				btnPlay.setVisibility(View.VISIBLE);
			}
			if (musicFlag.equals("local_music")) {
				boolean likeMusic = isLikeMusic(mp3List.get(
						musicPlayService.getCurrentPosition()).getTitle());
				if (likeMusic == true) {
					btnLike.setImageResource(R.drawable.player_btn_favorited_normal);
				} else {
					btnLike.setImageResource(R.drawable.player_btn_favorite_highlight);
				}

			} else if (musicFlag.equals("down_music")) {
				boolean likeMusic = isLikeMusic(downMusicList.get(
						musicPlayService.getCurrentPosition())
						.getDownMusicName());
				if (likeMusic == true) {
					btnLike.setImageResource(R.drawable.player_btn_favorited_normal);
				} else {
					btnLike.setImageResource(R.drawable.player_btn_favorite_highlight);
				}
			} else if (musicFlag.equals("like_music")
					&& MainFragmentActivity.likeMusciList.size() != 0) {
				boolean likeMusic = isLikeMusic(MainFragmentActivity.likeMusciList
						.get(musicPlayService.getCurrentPosition())
						.getMusicName());
				if (likeMusic == true) {
					btnLike.setImageResource(R.drawable.player_btn_favorited_normal);
				} else {
					btnLike.setImageResource(R.drawable.player_btn_favorite_highlight);
				}
			}

			initPlayMode();
			updateLRC();
		}

	};

	// 开启线程 更新本地音乐进度条
	private void updateLocalProgress() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (Constant.isLocalStop) {
					try {

						if (musicFlag.equals("local_music")) {
							// 设置的是当前播放歌曲的最大值
							seekBar.setMax((int) mp3List.get(
									musicPlayService.getCurrentPosition())
									.getDuration());
							seekBar.setProgress(musicPlayService
									.getCurrentProgress());
							// Log.e("本地音乐的进度条", "本地音乐的进度条");
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

	// 开启线程 更新下载音乐的进度条
	private void updateDownProgress() {

		new Thread(new Runnable() {
			@Override
			public void run() {

				while (Constant.isDownStop) {
					try {

						if (musicFlag.equals("down_music")) {
							seekBar.setMax((int) MediaUtils
									.getTrackLength(downMusicList.get(
											musicPlayService
													.getCurrentPosition())
											.getDownMusicDuration()));
							seekBar.setProgress(musicPlayService
									.getCurrentProgress());
							// Log.e("下载音乐的进度条", "下载音乐的进度条");
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

	// 开启线程 更新喜欢的音乐的进度条
	private void updateLikeProgress() {

		new Thread(new Runnable() {
			@Override
			public void run() {

				while (Constant.isLikeStop) {
					try {

						if (musicFlag.equals("like_music")
								&& MainFragmentActivity.likeMusciList.size() != 0) {
							seekBar.setMax(new Long(
									MainFragmentActivity.likeMusciList.get(
											musicPlayService
													.getCurrentPosition())
											.getMusicTime()).intValue());
							seekBar.setProgress(musicPlayService
									.getCurrentProgress());
						}

						// Log.e("喜欢音乐的进度条", "喜欢音乐的进度条");

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
		switch (MusicPlayService.getPlayMode()) {
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
			if (intent.getAction().equals("updateLocalText")) {
				if (musicFlag.equals("local_music")) {
					localMusicNextOrPre();
				}
			} else if (intent.getAction().equals("updateDownText")) {
				if (musicFlag.equals("down_music")) {
					downMusicNextOrPre();
				}

			} else if (intent.getAction().equals("updateLikeText")) {
				if (musicFlag.equals("like_music")
						&& MainFragmentActivity.likeMusciList.size() != 0) {
					likeMusicNextOrPre();
				}
			}

		}
	}

	private void showShare(int position) {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		oks.setTheme(OnekeyShareTheme.CLASSIC);
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();

		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
		// oks.setNotification(R.drawable.ic_launcher,
		// getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(intentMusicName);
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl(mp3Url);
		// text是分享文本，所有平台都需要这个字段
		oks.setText(intentArtist);
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		// oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
		// url仅在微信（包括好友和朋友圈）中使用
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		// oks.setComment("我是测试评论文本");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl(mp3Url);
		oks.setUrl(mp3Url);
		oks.setImageUrl(picUrl);
		oks.setMusicUrl(mp3Url);
		Log.e("分享的 MP3地址", mp3Url + "");
		Log.e("分享的 MP3图片的地址", picUrl + "");
		// 启动分享GUI
		oks.show(this);
	}

	// 根据歌曲名字上搜索音乐 用来分享音乐的链接
	private void searchShakeMusic() {
		List<NameValuePair> parmas = new ArrayList<NameValuePair>();
		parmas.add(new BasicNameValuePair("s", intentMusicName + " "
				+ intentArtist));
		parmas.add(new BasicNameValuePair("type", "1"));
		parmas.add(new BasicNameValuePair("offset", "0"));
		parmas.add(new BasicNameValuePair("sub", "false"));
		parmas.add(new BasicNameValuePair("limit", "5"));
		myHttpUtils.searchMusicToAPI(Constant.API_NET_SEARCH_MUSIC, parmas);
	}

	// 根据点击歌曲的位置通过音乐的ID找到音乐的链接和图片的链接
	private void getDownUrl(ArrayList<SearchMusicInfo> searchMusicList) {
		// http://music.163.com/api/song/detail/?id=29818120&ids=[29818120]
		String musicID = searchMusicList.get(0).getMusicID();
		String musicUrl = "http://music.163.com/api/song/detail/?id=" + musicID
				+ "&ids=[" + musicID + "]";
		httpUtils.send(HttpMethod.GET, musicUrl, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				Toast.makeText(MusicPlayAvtivity.this, "请求下载链接失败了", 0).show();
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				String response = arg0.result;
				Log.e("请求的网络地址", getRequestUrl());
				parseJsonResult(response);
			}
		});

	}

	/**
	 * @Description:解析请求的链接返回的数据 目的是为了获取MP3 和图片的下载链接
	 * @return void
	 * @author bai
	 */
	protected void parseJsonResult(String response) {

		try {
			JSONObject result = new JSONObject(response);
			String songs = result.getString("songs");
			JSONArray songArray = new JSONArray(songs);
			for (int i = 0; i < songArray.length(); i++) {
				JSONObject object = (JSONObject) songArray.get(0);
				mp3Url = object.getString("mp3Url");
				String album = object.getString("album");
				JSONObject picObject = new JSONObject(album);
				picUrl = picObject.getString("picUrl");
				showShare(musicPlayService.getCurrentPosition());
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
}
