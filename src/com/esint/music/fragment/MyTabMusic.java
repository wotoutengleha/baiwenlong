package com.esint.music.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.R.color;
import com.esint.music.activity.MainFragmentActivity;
import com.esint.music.activity.MusicPlayAvtivity;
import com.esint.music.adapter.DownMusicAdapter;
import com.esint.music.adapter.LikeMusicAdapter;
import com.esint.music.adapter.LocalMusicAdapter;
import com.esint.music.db.MySQLite;
import com.esint.music.model.LikeMusicModel;
import com.esint.music.model.Mp3Info;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.service.MusicPlayService;
import com.esint.music.service.MusicPlayService.PlayBinder;
import com.esint.music.sortlistview.CharacterParser;
import com.esint.music.sortlistview.PinyinComparator;
import com.esint.music.sortlistview.SideBar;
import com.esint.music.sortlistview.SideBar.OnTouchingLetterChangedListener;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.MyApplication;
import com.esint.music.utils.PageAction;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.view.AlwaysMarqueeTextView;
import com.lidroid.xutils.exception.DbException;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint({ "ValidFragment", "DefaultLocale" })
public class MyTabMusic extends Fragment implements OnClickListener {
	private RelativeLayout mLocalMusic;
	private LinearLayout mFaviorite;
	private LinearLayout mMyDownload;
	private ImageView randomPlayIcon;
	private PageAction action;

	// 本地音乐列表下的控件
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private LocalMusicAdapter adapter;
	private TextView songCount;
	private TextView musicnNmber;// 音乐数量
	// 汉字转换成拼音的类
	private CharacterParser characterParser;
	private List<Mp3Info> mp3List;
	// 根据拼音来排列ListView里面的数据类
	private PinyinComparator pinyinComparator;

	private ImageView albumImg;// 专辑图片
	private AlwaysMarqueeTextView singer;// 歌手
	private AlwaysMarqueeTextView songName;// 歌曲名字
	private ImageButton playButton;// 播放按钮
	private ImageButton pauseButton;// 暂停按钮
	private ImageButton nextButton;// 下一首
	private ProgressBar progressBar;
	private int currentPlayPosition;

	private MainFragmentActivity mainActivity;
	private MyBroadcastReceiver broadcastReceiver;
	private MusicPlayService musicPlayService;
	public static ArrayList<Mp3Info> sortMyLikeMp3Infos;
	private String musicFlag;
	private ArrayList<DownMucicInfo> downMusicList;
	private DownMusicAdapter adapter_down;// 我的下载列表的适配器
	private LikeMusicAdapter adapter_fav;
	private String imageTarget;// 下载的图片存放的路径
	private MyBroadCast broadCast;
	public static Handler mHandler;

	// private MySQLite mySQLite;
	// public static SQLiteDatabase db;
	// public static ArrayList<LikeMusicModel> likeMusciList = new
	// ArrayList<LikeMusicModel>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mytab_music, null);
		initView(view);
		initData();
		initAnim();// 初始化随机播放按钮的动画效果
		return view;

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainFragmentActivity) activity;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 解绑服务
		// mainActivity.unBindService();
		// mainActivity.unregisterReceiver(broadcastReceiver);
		getActivity().unbindService(connection);
		getActivity().unregisterReceiver(broadcastReceiver);
		getActivity().unregisterReceiver(broadCast);

	}

	public MyTabMusic(PageAction action) {
		this.action = action;
	}

	private void initView(View view) {
		mLocalMusic = (RelativeLayout) view.findViewById(R.id.my_localmusic_rl);
		mFaviorite = (LinearLayout) view.findViewById(R.id.my_faviorite_ll);
		mMyDownload = (LinearLayout) view.findViewById(R.id.my_download_ll);
		randomPlayIcon = (ImageView) view.findViewById(R.id.ib_random_play);
		musicnNmber = (TextView) view.findViewById(R.id.musicnumber);

		mLocalMusic.setOnClickListener(this);
		mFaviorite.setOnClickListener(this);
		mMyDownload.setOnClickListener(this);
		randomPlayIcon.setOnClickListener(this);

		albumImg = (ImageView) mainActivity.findViewById(R.id.iv_album);
		singer = (AlwaysMarqueeTextView) mainActivity
				.findViewById(R.id.tv_musicArt);
		songName = (AlwaysMarqueeTextView) mainActivity
				.findViewById(R.id.tv_musicName);

		playButton = (ImageButton) mainActivity.findViewById(R.id.ib_play);
		pauseButton = (ImageButton) mainActivity.findViewById(R.id.ib_pause);
		nextButton = (ImageButton) mainActivity.findViewById(R.id.ib_next);
		progressBar = (ProgressBar) mainActivity
				.findViewById(R.id.playback_seekbar);
		playButton.setOnClickListener(this);
		pauseButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
	}

	private void initData() {

		// 创建数据库
		// mySQLite = new MySQLite(getActivity(), "Music.db", null, 1);
		// db = mySQLite.getWritableDatabase();
		final int recordLikeMusicPosition = SharedPrefUtil.getInt(
				getActivity(), Constant.CLICKED_MUNSIC_NAME_LIKE, -1);
		Intent intent = new Intent(getActivity(), MusicPlayService.class);
		getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		mp3List = MediaUtils.getMp3Info(getActivity());
		mp3List = filledData(mp3List);
		// 根据a-z进行排序源数据
		Collections.sort(mp3List, pinyinComparator);
		musicnNmber.setText(mp3List.size() + "首歌曲");

		broadCast = new MyBroadCast();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("updateLocalText");
		intentFilter.addAction("updateDownText");
		intentFilter.addAction("updateLikeText");
		getActivity().registerReceiver(broadCast, intentFilter);

		// 下载歌曲的文件夹
		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的歌曲";
		downMusicList = MediaUtils.GetMusicFiles(MusicTarget, ".mp3", true);
		imageTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的图片" + "/";

		broadcastReceiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.PLAYBUTTON_BROAD);
		filter.addAction(Constant.PAUSEBUTTON_BROAD);
		getActivity().registerReceiver(broadcastReceiver, filter);
		musicFlag = SharedPrefUtil.getString(mainActivity, Constant.MUSIC_FLAG,
				"");

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what) {
				case Constant.SHAKE_MUSIC: {
					// 点击item的时候 存入播放的位置
					SharedPrefUtil.setInt(getActivity(),
							Constant.CLICKED_MUNSIC_NAME,
							musicPlayService.getCurrentPosition());
					changeUIStatusOnPlay(musicPlayService.getCurrentPosition());
					break;
				}
				case Constant.NEXT_LEKE_MUSIC: {
					// 当点击喜欢列表里取消喜欢的按钮时 判断当前播放的歌曲是否和点击取消的歌曲位置一致
					String musicTitle = (String) msg.obj;
					if (musicTitle.equals(songName.getText().toString())&&MainFragmentActivity.likeMusciList.size()>=2) {
						// 调用下一首的喜欢的音乐列表
						Log.e("进来了", "进来了");
						musicPlayService.nextLikeMusic();
						int currentPositionDown = musicPlayService
								.getCurrentPosition();
						// 点击item的时候 存入播放的位置
						SharedPrefUtil.setInt(getActivity(),
								Constant.CLICKED_MUNSIC_NAME_LIKE,
								currentPositionDown);
						changeUIStatusOnPlayLike(currentPositionDown);
					}
				}
					break;
				}
			}
		};
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.my_localmusic_rl:
			localMusic();
			break;
		case R.id.my_faviorite_ll:
			myFavorite();
			break;
		case R.id.my_download_ll:
			myDownLoad();
			break;
		case R.id.ib_random_play:
			Toast.makeText(getActivity(), "随机为您点了一首歌", 0).show();
			randomPlayAnim();
			Random random = new Random();
			musicPlayService.playLocalMusic(random.nextInt(mp3List.size()));
			playButton.setVisibility(View.GONE);
			pauseButton.setVisibility(View.VISIBLE);
			// 点击item的时候 存入播放的位置
			SharedPrefUtil.setInt(getActivity(), Constant.CLICKED_MUNSIC_NAME,
					musicPlayService.getCurrentPosition());
			changeUIStatusOnPlay(musicPlayService.getCurrentPosition());

			break;
		case R.id.ib_pause: {
			String musicFlag = SharedPrefUtil.getString(mainActivity,
					Constant.MUSIC_FLAG, "");

			if (musicFlag.equals("NET_MUSIC")) {
				musicPlayService.pause();
				playButton.setVisibility(View.VISIBLE);
				pauseButton.setVisibility(View.GONE);
				return;
			}

			musicPlayService.pause();
			albumImg.clearAnimation();
			playButton.setVisibility(View.VISIBLE);
			pauseButton.setVisibility(View.GONE);
			int progress = progressBar.getProgress();
			progressBar.setProgress(progress);
			break;
		}
		case R.id.ib_play: {
			String musicFlag = SharedPrefUtil.getString(mainActivity,
					Constant.MUSIC_FLAG, "");
			if (musicFlag.equals("NET_MUSIC")) {
				musicPlayService.start();
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);
				return;
			}

			currentPlayPosition = SharedPrefUtil.getInt(getActivity(),
					Constant.CLICKED_MUNSIC_NAME, -1);
			int recordDownMusicPosition = SharedPrefUtil.getInt(getActivity(),
					Constant.CLICKED_MUNSIC_NAME_DOWN, -1);
			int recordLikeMusicPosition = SharedPrefUtil.getInt(getActivity(),
					Constant.CLICKED_MUNSIC_NAME_LIKE, -1);
			if (currentPlayPosition == -1 && recordDownMusicPosition == -1) {
				return;
			}

			if (Constant.isFirst && musicFlag.equals("local_music")) {
				Constant.ISFirst_PLAY = false;
				// 得到记录的位置

				musicPlayService.playLocalMusic(currentPlayPosition);
				SharedPrefUtil.setString(mainActivity, Constant.MUSIC_FLAG,
						Constant.MY_LOCAL_MUSIC);
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
				startAnim();
				if (currentPlayPosition != -1) {
					// // 设置进度条
					progressBar.setMax((int) mp3List.get(currentPlayPosition)
							.getDuration());
					updateProgress();
					Constant.isFirst = false;
				}
				Log.e("点击的播放进入的是本地的音乐", "点击播放进入的是本地的音乐");
			} else if (Constant.isFirst && musicFlag.equals("like_music")
					&& MainFragmentActivity.likeMusciList.size() != 0) {
				Constant.ISFirst_PLAY = false;
				// 得到记录的位置
				musicPlayService.playMyFav(recordLikeMusicPosition);
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
				startAnim();
				if (recordLikeMusicPosition != -1
						&& MainFragmentActivity.likeMusciList != null) {
					// // 设置进度条
					progressBar.setMax(new Long(
							MainFragmentActivity.likeMusciList.get(
									musicPlayService.getCurrentPosition())
									.getMusicTime()).intValue());
					updateProgress();
					Constant.isFirst = false;
					Log.e("点击的播放进入的是喜欢的音乐", "点击播放进入的是喜欢的音乐");
				}
			} else if (Constant.isFirst && musicFlag.equals("down_music")) {
				Constant.ISFirst_PLAY = false;
				// 得到记录的位置
				musicPlayService.playMyDown(recordDownMusicPosition);
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
				startAnim();
				if (recordDownMusicPosition != -1 && downMusicList != null) {
					// // 设置进度条
					progressBar.setMax((int) MediaUtils
							.getTrackLength(downMusicList.get(
									musicPlayService.getCurrentPosition())
									.getDownMusicDuration()));
					Log.e("进入到了这里的条件", "进入到了这里的条件");
					updateProgress();
					Constant.isFirst = false;
				}
				Log.e("点击的播放进入的是下载的音乐", "点击播放进入的是下载的音乐");
			}

			else if (!Constant.isFirst) {
				musicPlayService.start();
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
				startAnim();
			}

			break;
		}
		case R.id.ib_next: {
			String musicFlag = SharedPrefUtil.getString(mainActivity,
					Constant.MUSIC_FLAG, "");
			currentPlayPosition = SharedPrefUtil.getInt(getActivity(),
					Constant.CLICKED_MUNSIC_NAME, -1);
			if (musicFlag.equals("NET_MUSIC")) {
				return;
			}
			if (currentPlayPosition == -1) {
				return;
			}

			playButton.setVisibility(View.GONE);
			pauseButton.setVisibility(View.VISIBLE);

			if (musicFlag.equals("local_music")) {
				musicPlayService.next();
				int currentPosition = musicPlayService.getCurrentPosition();
				// 点击item的时候 存入播放的位置
				SharedPrefUtil.setInt(getActivity(),
						Constant.CLICKED_MUNSIC_NAME, currentPosition);
				changeUIStatusOnPlay(currentPosition);

			} else if (musicFlag.equals("down_music")) {
				musicPlayService.nextDownMusic();
				int currentPositionDown = musicPlayService.getCurrentPosition();
				// 点击item的时候 存入播放的位置
				SharedPrefUtil.setInt(getActivity(),
						Constant.CLICKED_MUNSIC_NAME_DOWN, currentPositionDown);
				changeUIStatusOnPlayDown(currentPositionDown);
			} else if (musicFlag.equals("like_music")
					&& MainFragmentActivity.likeMusciList.size() != 0) {
				musicPlayService.nextLikeMusic();
				int currentPositionDown = musicPlayService.getCurrentPosition();
				// 点击item的时候 存入播放的位置
				SharedPrefUtil.setInt(getActivity(),
						Constant.CLICKED_MUNSIC_NAME_LIKE, currentPositionDown);
				if (MainFragmentActivity.likeMusciList.size() != 0) {
					changeUIStatusOnPlayLike(currentPositionDown);
				}
			}
			break;
		}
		}
	}

	// 当点击了我的下载的时候，点击下一首的时候更改的是我的下载的音乐里边的数据
	private void changeUIStatusOnPlayDown(int currentPositionDown) {

		Bitmap albumBit = BitmapFactory.decodeFile(imageTarget
				+ downMusicList.get(currentPositionDown).getDownMusicName()
						.trim() + ".jpg", null);
		if (albumBit != null) {
			albumImg.setImageBitmap(albumBit);
		}
		// 开始旋转专辑图片的动画
		Animation operatingAnim = AnimationUtils.loadAnimation(getActivity(),
				R.anim.tip);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		if (operatingAnim != null) {
			albumImg.startAnimation(operatingAnim);
		}
		singer.setText(downMusicList.get(currentPositionDown)
				.getDownMusicArtist());
		songName.setText(downMusicList.get(currentPositionDown)
				.getDownMusicName());
		if (adapter_down != null) {
			adapter_down.notifyDataSetChanged();
		}
		// 设置进度条
		progressBar.setMax((int) MediaUtils.getTrackLength(downMusicList.get(
				currentPositionDown).getDownMusicDuration()));
		updateProgress();
		// 设置通知栏
		musicPlayService.updateNotification(albumBit, singer.getText()
				.toString(), songName.getText().toString());
	}

	// 当点击了我的下载的时候，点击下一首的时候更改的是我的下载的音乐里边的数据
	private void changeUIStatusOnPlayLike(int currentPositionDown) {
		// 开始旋转专辑图片的动画
		Animation operatingAnim = AnimationUtils.loadAnimation(getActivity(),
				R.anim.tip);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		if (operatingAnim != null) {
			albumImg.setImageBitmap(MainFragmentActivity.likeMusciList.get(
					musicPlayService.getCurrentPosition()).getBitmap());
			albumImg.startAnimation(operatingAnim);
		}
		singer.setText(MainFragmentActivity.likeMusciList.get(
				currentPositionDown).getMusicArtist());
		songName.setText(MainFragmentActivity.likeMusciList.get(
				currentPositionDown).getMusicName());
		if (adapter_fav != null) {
			adapter_fav.notifyDataSetChanged();
		}
		// 设置进度条
		progressBar.setMax(new Long(MainFragmentActivity.likeMusciList.get(
				musicPlayService.getCurrentPosition()).getMusicTime())
				.intValue());
		updateProgress();
		// 设置通知栏
		musicPlayService.updateNotification(MainFragmentActivity.likeMusciList
				.get(musicPlayService.getCurrentPosition()).getBitmap(), singer
				.getText().toString(), songName.getText().toString());

	}

	// 开始旋转专辑图片的动画
	private void startAnim() {
		Animation operatingAnim = AnimationUtils.loadAnimation(getActivity(),
				R.anim.tip);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		if (operatingAnim != null) {
			albumImg.startAnimation(operatingAnim);
		}
	}

	// 更改列表信息
	public void changeUIStatusOnPlay(int position) {
		Bitmap bitmap = MediaUtils.getArtwork(mainActivity,
				mp3List.get(position).getId(), mp3List.get(position)
						.getAlbumId(), true, true);
		Bitmap notifiBitmap = MediaUtils.getArtwork(mainActivity,
				mp3List.get(position).getId(), mp3List.get(position)
						.getAlbumId(), true, false);
		albumImg.setImageBitmap(bitmap);
		// 开始旋转专辑图片的动画
		Animation operatingAnim = AnimationUtils.loadAnimation(getActivity(),
				R.anim.tip);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		if (operatingAnim != null) {
			albumImg.startAnimation(operatingAnim);
		}
		singer.setText(mp3List.get(position).getArtist());
		songName.setText(mp3List.get(position).getTitle());
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		// 设置进度条
		progressBar.setMax((int) mp3List.get(position).getDuration());
		updateProgress();
		musicPlayService.updateNotification(notifiBitmap, singer.getText()
				.toString(), songName.getText().toString());

	}

	@Override
	public void onResume() {
		super.onResume();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		if (adapter_down != null) {
			adapter_down.notifyDataSetChanged();
		}
		if (adapter_fav != null) {
			adapter_fav.notifyDataSetChanged();
		}
	}

	// 加载本地音乐的视图
	private void localMusic() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View localView = inflater.inflate(R.layout.activity_localmusic, null,
				false);
		localMusicBg = (RelativeLayout) localView
				.findViewById(R.id.localmusicBg);
		localTopLayout = (RelativeLayout) localView
				.findViewById(R.id.actionbar);
		ImageView backBtnImg = (ImageView) localView
				.findViewById(R.id.backBtn_local);
		// 返回键
		backBtnImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mainActivity.slidingMenu.isMenuShowing()) {
					mainActivity.slidingMenu.showMenu();
				}
			}
		});
		updateActionBar(1);
		getBitmapPath(1);
		// listView脚布局的布局文件
		View footerView = inflater.inflate(R.layout.sortlist_footerview, null,
				false);
		sortListView = (ListView) localView.findViewById(R.id.local_songlv);
		songCount = (TextView) footerView.findViewById(R.id.songCount);
		sideBar = (SideBar) localView.findViewById(R.id.sidrbar);
		dialog = (TextView) localView.findViewById(R.id.dialog);
		sideBar.setTextView(dialog);
		sortListView.addFooterView(footerView);
		// 设置脚布局
		songCount.setText("共有" + mp3List.size() + "首歌曲");

		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}

			}
		});
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == mp3List.size()) {
					return;
				}
				// 点击item的时候 存入播放的位置
				SharedPrefUtil.setString(mainActivity, Constant.MUSIC_FLAG,
						Constant.MY_LOCAL_MUSIC);
				SharedPrefUtil.setInt(getActivity(),
						Constant.CLICKED_MUNSIC_NAME, position);
				int recordPosition = SharedPrefUtil.getInt(mainActivity,
						Constant.CLICKED_MUNSIC_NAME, -1);
				Constant.isFirst = false;
				Constant.ISFirst_PLAY = false;
				Constant.isDownStop = false;
				Constant.isLocalStop = true;
				Constant.isLikeStop = false;
				if (musicPlayService.getCurrentPosition() == recordPosition) {
					return;
				}

				progressBar.setMax((int) mp3List.get(position).getDuration());
				updateProgress();
				musicPlayService.playLocalMusic(position);
				// 设置专辑图片和歌曲信息
				Bitmap bitmap = MediaUtils.getArtwork(mainActivity, mp3List
						.get(position).getId(), mp3List.get(position)
						.getAlbumId(), true, true);
				albumImg.setImageBitmap(bitmap);

				// 开始旋转专辑图片的动画
				Animation operatingAnim = AnimationUtils.loadAnimation(
						getActivity(), R.anim.tip);
				LinearInterpolator lin = new LinearInterpolator();
				operatingAnim.setInterpolator(lin);
				if (operatingAnim != null) {
					albumImg.startAnimation(operatingAnim);
				}
				singer.setText(mp3List.get(position).getArtist());
				songName.setText(mp3List.get(position).getTitle());
				SharedPrefUtil.setInt(mainActivity,
						Constant.CLICKED_MUNSIC_NAME, position);
				// 改变了listView 务必刷新适配器
				adapter.notifyDataSetChanged();
				// // 图标改成暂停状态
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);
				Bitmap notifyBitmap = MediaUtils.getArtwork(mainActivity,
						mp3List.get(position).getId(), mp3List.get(position)
								.getAlbumId(), true, false);
				// 更新通知栏
				musicPlayService.updateNotification(notifyBitmap, singer
						.getText().toString(), songName.getText().toString());

			}
		});

		mp3List = filledData(mp3List);
		// 根据a-z进行排序源数据
		Collections.sort(mp3List, pinyinComparator);
		adapter = new LocalMusicAdapter(getActivity(), mp3List);
		sortListView.setAdapter(adapter);
		action.addPage(localView);
	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private ArrayList<Mp3Info> filledData(List<Mp3Info> date) {
		ArrayList<Mp3Info> mSortList = new ArrayList<Mp3Info>();
		for (int i = 0; i < date.size(); i++) {
			Mp3Info mp3Info = new Mp3Info();
			mp3Info.setArtist(date.get(i).getArtist());
			mp3Info.setTitle(date.get(i).getTitle());
			mp3Info.setId(date.get(i).getId());
			mp3Info.setAlbumId(date.get(i).getAlbumId());
			mp3Info.setDuration(date.get(i).getDuration());
			mp3Info.setSize(date.get(i).getSize());
			mp3Info.setUrl(date.get(i).getUrl());
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(date.get(i).getArtist());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				mp3Info.setSortLetters(sortString.toUpperCase());
			} else {
				mp3Info.setSortLetters("#");
			}

			mSortList.add(mp3Info);
		}
		return mSortList;
	}

	// 加载我的最爱的视图
	private void myFavorite() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View myFavoriteLayout = inflater.inflate(R.layout.activity_my_favorite,
				null, false);
		myFavoriteBg = (RelativeLayout) myFavoriteLayout
				.findViewById(R.id.myfaviorBg);
		myFavActionBar = (RelativeLayout) myFavoriteLayout
				.findViewById(R.id.actionbar_fav);
		updateActionBar(2);
		getBitmapPath(2);
		ImageView backBtnFav = (ImageView) myFavoriteLayout
				.findViewById(R.id.backBtn_favorite);
		backBtnFav.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mainActivity.slidingMenu.isMenuShowing()) {
					mainActivity.slidingMenu.showMenu();
				}
			}
		});

		try {
			final List<LikeMusicModel> myLikeMp3Infos = MainFragmentActivity
					.getFavMusicFromDB();
			Log.e("myLikeMp3Infos", myLikeMp3Infos.size() + "");

			// listView脚布局的布局文件
			View footerView = inflater.inflate(R.layout.sortlist_footerview,
					null, false);
			final ListView sortListView_fav = (ListView) myFavoriteLayout
					.findViewById(R.id.my_favorite_songlv);
			songCount = (TextView) footerView.findViewById(R.id.songCount);
			SideBar sideBar_fav = (SideBar) myFavoriteLayout
					.findViewById(R.id.sidrbar_fav);
			dialog = (TextView) myFavoriteLayout.findViewById(R.id.dialog_fav);
			sideBar_fav.setTextView(dialog);
			sortListView_fav.addFooterView(footerView);
			// 设置脚布局
			if (myLikeMp3Infos == null) {
				songCount.setText("共有0首歌曲");
				action.addPage(myFavoriteLayout);
				return;
			}
			songCount.setText("共有" + myLikeMp3Infos.size() + "首歌曲");
			adapter_fav = new LikeMusicAdapter(getActivity(), myLikeMp3Infos);
			sortListView_fav.setAdapter(adapter_fav);
			sortListView_fav.setOnItemClickListener(new OnItemClickListener() {

				@SuppressLint("UseValueOf")
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (position == myLikeMp3Infos.size()) {
						return;
					}
					// 点击item的时候 存入播放的位置
					SharedPrefUtil.setString(mainActivity, Constant.MUSIC_FLAG,
							Constant.MY_LIKE_MUSIC);
					SharedPrefUtil.setInt(getActivity(),
							Constant.CLICKED_MUNSIC_NAME_LIKE, position);
					int recordPosition = SharedPrefUtil.getInt(mainActivity,
							Constant.CLICKED_MUNSIC_NAME_LIKE, -1);

					Constant.isFirst = false;
					Constant.ISFirst_PLAY = false;
					Constant.isDownStop = false;
					Constant.isLocalStop = false;
					Constant.isLikeStop = true;

					if (musicPlayService.getCurrentPosition() == recordPosition) {
						return;
					}
					musicPlayService.playMyFav(position);
					progressBar.setMax(new Long(
							MainFragmentActivity.likeMusciList.get(
									musicPlayService.getCurrentPosition())
									.getMusicTime()).intValue());
					updateProgress();
					// 设置专辑图片和歌曲信息
					albumImg.setImageBitmap(MainFragmentActivity.likeMusciList
							.get(musicPlayService.getCurrentPosition())
							.getBitmap());
					// 开始旋转专辑图片的动画
					Animation operatingAnim = AnimationUtils.loadAnimation(
							getActivity(), R.anim.tip);
					LinearInterpolator lin = new LinearInterpolator();
					operatingAnim.setInterpolator(lin);
					if (operatingAnim != null) {
						albumImg.startAnimation(operatingAnim);
					}
					singer.setText(myLikeMp3Infos.get(position)
							.getMusicArtist());
					songName.setText(myLikeMp3Infos.get(position)
							.getMusicName());
					// SharedPrefUtil.setInt(mainActivity,
					// Constant.CLICKED_MUNSIC_NAME, position);
					// 改变了listView 务必刷新适配器
					adapter_fav.notifyDataSetChanged();
					// // 图标改成暂停状态
					playButton.setVisibility(View.GONE);
					pauseButton.setVisibility(View.VISIBLE);
					// 更新通知栏
					musicPlayService.updateNotification(
							MainFragmentActivity.likeMusciList.get(
									musicPlayService.getCurrentPosition())
									.getBitmap(), singer.getText().toString(),
							songName.getText().toString());

				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		action.addPage(myFavoriteLayout);
	}

	// 加载我的下载的视图
	private void myDownLoad() {
		// 下载歌曲的文件夹
		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的歌曲";
		downMusicList = MediaUtils.GetMusicFiles(MusicTarget, ".mp3", true);
		adapter_down = new DownMusicAdapter(getActivity(), downMusicList);
		final String ImageTarget = Environment.getExternalStorageDirectory()
				+ "/" + "/下载的图片" + "/";
		View downLoadView = View.inflate(mainActivity,
				R.layout.activity_my_download, null);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// listView脚布局的布局文件
		View footerView = inflater.inflate(R.layout.sortlist_footerview, null,
				false);
		MydownLoadBg = (RelativeLayout) downLoadView
				.findViewById(R.id.myDownLoadBg);
		myDownAction = (RelativeLayout) downLoadView
				.findViewById(R.id.actionbar_down);
		sideBar = (SideBar) downLoadView.findViewById(R.id.mydown_sidrbar);
		dialog = (TextView) downLoadView.findViewById(R.id.mydown_dialog);
		sideBar.setTextView(dialog);
		updateActionBar(3);
		getBitmapPath(3);
		ImageView btnBackDown = (ImageView) downLoadView
				.findViewById(R.id.backBtn_local);
		btnBackDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mainActivity.slidingMenu.isMenuShowing()) {
					mainActivity.slidingMenu.showMenu();
				}
			}
		});
		songCount = (TextView) footerView.findViewById(R.id.songCount);
		// 设置脚布局
		if (downMusicList == null) {
			songCount.setText("共有" + "0首歌曲");
			action.addPage(downLoadView);
			return;
		}
		songCount.setText("共有" + downMusicList.size() + "首歌曲");
		final ListView sortListView_down = (ListView) downLoadView
				.findViewById(R.id.my_down_songlv);
		sortListView_down.addFooterView(footerView);
		sortListView_down.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == downMusicList.size()) {
					return;
				}

				// 点击item的时候 存入播放的位置
				SharedPrefUtil.setString(mainActivity, Constant.MUSIC_FLAG,
						Constant.MY_DOWN_MUSIC);
				SharedPrefUtil.setInt(getActivity(),
						Constant.CLICKED_MUNSIC_NAME_DOWN, position);
				int recordPosition = SharedPrefUtil.getInt(mainActivity,
						Constant.CLICKED_MUNSIC_NAME_DOWN, -1);
				Constant.isFirst = false;
				Constant.ISFirst_PLAY = false;
				Constant.isLocalStop = false;
				Constant.isDownStop = true;
				Constant.isLikeStop = false;

				if (musicPlayService.getCurrentPosition() == recordPosition) {
					return;
				}

				mainActivity.musicPlayService.playMyDown(position);

				// 设置进度条
				progressBar.setMax((int) MediaUtils
						.getTrackLength(downMusicList.get(recordPosition)
								.getDownMusicDuration()));
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							progressBar
									.setProgress(mainActivity.musicPlayService
											.getCurrentProgress());
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();

				// 设置专辑图片和歌曲的信息
				singer.setText(downMusicList.get(position).getDownMusicArtist());
				songName.setText(downMusicList.get(position).getDownMusicName());
				Bitmap albumBit = BitmapFactory.decodeFile(ImageTarget
						+ downMusicList.get(position).getDownMusicName().trim()
						+ ".jpg", null);
				if (albumBit != null) {
					albumImg.setImageBitmap(albumBit);
				}

				// 更新通知栏
				musicPlayService.updateNotification(albumBit, singer.getText()
						.toString(), songName.getText().toString());

				// 开始旋转专辑图片的动画
				Animation operatingAnim = AnimationUtils.loadAnimation(
						getActivity(), R.anim.tip);
				LinearInterpolator lin = new LinearInterpolator();
				operatingAnim.setInterpolator(lin);
				if (operatingAnim != null) {
					albumImg.startAnimation(operatingAnim);
				}
				SharedPrefUtil.setInt(mainActivity,
						Constant.CLICKED_MUNSIC_NAME_DOWN, position);
				// 改变了listView 务必刷新适配器
				adapter_down.notifyDataSetChanged();
				// // 图标改成暂停状态
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);
			}

		});
		sortListView_down.setAdapter(adapter_down);
		action.addPage(downLoadView);
	}

	// 随机播放按钮的动画
	private void randomPlayAnim() {
		randomPlayIcon.startAnimation(leftAnim);
	}

	private RotateAnimation leftAnim;
	private RotateAnimation rightAnim;
	private RotateAnimation leftBackAnim;
	private RelativeLayout localMusicBg, myFavoriteBg, MydownLoadBg;
	private RelativeLayout localTopLayout;
	private RelativeLayout myFavActionBar;
	private RelativeLayout myDownAction;

	private void initAnim() {
		leftAnim = new RotateAnimation(0f, -20f, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		leftAnim.setFillAfter(true);
		leftAnim.setDuration(300);
		leftAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				randomPlayIcon.clearAnimation();
				randomPlayIcon.startAnimation(rightAnim);
			}
		});
		rightAnim = new RotateAnimation(-20f, 40f, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rightAnim.setFillAfter(true);
		rightAnim.setDuration(300);
		rightAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				randomPlayIcon.clearAnimation();
				randomPlayIcon.startAnimation(leftBackAnim);
			}
		});

		leftBackAnim = new RotateAnimation(40f, 0f, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		leftBackAnim.setFillAfter(true);
		leftBackAnim.setDuration(300);

	}

	// 拿到path
	private void getBitmapPath(int flag) {
		SharedPreferences sp = getActivity().getSharedPreferences(
				Constant.SP_NAME, Context.MODE_PRIVATE);
		String path = sp.getString(Constant.BACK_IMG, "");
		Bitmap bitmap = getBitmapByPath(path);
		if (bitmap != null && flag == 1) {
			localMusicBg.setBackground(new BitmapDrawable(this.getResources(),
					bitmap));
		} else if (bitmap != null && flag == 2) {
			myFavoriteBg.setBackground(new BitmapDrawable(this.getResources(),
					bitmap));
		} else if (bitmap != null && flag == 3) {
			MydownLoadBg.setBackground(new BitmapDrawable(this.getResources(),
					bitmap));
		}
	}

	// 根据path得到bitmap
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private Bitmap getBitmapByPath(String path) {
		AssetManager manager = getActivity().getAssets();
		Bitmap bitmap = null;
		try {
			InputStream is = manager.open("bkgs/" + path);
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	// 更新状态栏
	private void updateActionBar(int index) {
		int colorIndex = SharedPrefUtil.getInt(getActivity(),
				Constant.COLOR_INDEX, -1);
		if (colorIndex != -1 && index == 1) {
			switch (colorIndex) {
			case 0:
				localTopLayout.setBackgroundResource(color.tianyilan);
				break;
			case 1:
				localTopLayout.setBackgroundResource(color.hidden_bitterness);
				break;
			case 2:
				localTopLayout.setBackgroundResource(color.gorgeous);
				break;
			case 3:
				localTopLayout.setBackgroundResource(color.romance);
				break;
			case 4:
				localTopLayout.setBackgroundResource(color.sunset);
				break;
			case 5:
				localTopLayout.setBackgroundResource(color.warm_colour);
				break;

			default:
				localTopLayout.setBackgroundResource(color.holo_blue_light);
				break;
			}
		} else if (colorIndex != -1 && index == 2) {
			switch (colorIndex) {
			case 0:
				myFavActionBar.setBackgroundResource(color.tianyilan);
				break;
			case 1:
				myFavActionBar.setBackgroundResource(color.hidden_bitterness);
				break;
			case 2:
				myFavActionBar.setBackgroundResource(color.gorgeous);
				break;
			case 3:
				myFavActionBar.setBackgroundResource(color.romance);
				break;
			case 4:
				myFavActionBar.setBackgroundResource(color.sunset);
				break;
			case 5:
				myFavActionBar.setBackgroundResource(color.warm_colour);
				break;

			default:
				myFavActionBar.setBackgroundResource(color.holo_blue_light);
				break;
			}
		} else if (colorIndex != -1 && index == 3) {
			switch (colorIndex) {
			case 0:
				myDownAction.setBackgroundResource(color.tianyilan);
				break;
			case 1:
				myDownAction.setBackgroundResource(color.hidden_bitterness);
				break;
			case 2:
				myDownAction.setBackgroundResource(color.gorgeous);
				break;
			case 3:
				myDownAction.setBackgroundResource(color.romance);
				break;
			case 4:
				myDownAction.setBackgroundResource(color.sunset);
				break;
			case 5:
				myDownAction.setBackgroundResource(color.warm_colour);
				break;

			default:
				myDownAction.setBackgroundResource(color.holo_blue_light);
				break;
			}
		}

	}

	// 根据接收到的广播来更新播放和暂停的按钮
	public class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constant.PLAYBUTTON_BROAD)) {
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
				// 设置进度条
				if (currentPlayPosition != -1) {
					progressBar.setMax((int) mp3List.get(currentPlayPosition)
							.getDuration());
					updateProgress();
				}

			} else if (intent.getAction().equals(Constant.PAUSEBUTTON_BROAD)) {
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			} else if (intent.getAction().equals(Constant.PLAYBUTTON_BROAD)) {
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
			}
			// 开始旋转专辑图片的动画
			Animation operatingAnim = AnimationUtils.loadAnimation(
					getActivity(), R.anim.tip);
			LinearInterpolator lin = new LinearInterpolator();
			operatingAnim.setInterpolator(lin);
			if (operatingAnim != null) {
				albumImg.startAnimation(operatingAnim);
			}
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

		};

	};

	// 开启线程更新进度条
	private void updateProgress() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					progressBar.setProgress(mainActivity.musicPlayService
							.getCurrentProgress());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	// 接受到广播更新数据
	public class MyBroadCast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals("updateLocalText")) {
				String musicFlag = SharedPrefUtil.getString(mainActivity,
						Constant.MUSIC_FLAG, "");
				if (musicFlag.equals("local_music")) {
					SharedPrefUtil.setInt(mainActivity,
							Constant.CLICKED_MUNSIC_NAME,
							musicPlayService.getCurrentPosition());
					changeUIStatusOnPlay(musicPlayService.getCurrentPosition());
					Log.e("接收到了本地的音乐", "接收到了本地的音乐");
				}
			} else if (intent.getAction().endsWith("updateDownText")) {
				String musicFlag = SharedPrefUtil.getString(mainActivity,
						Constant.MUSIC_FLAG, "");
				if (musicFlag.equals("down_music")) {
					Log.e("接收到了下载音乐", "接收到了下载音乐");
					SharedPrefUtil.setInt(mainActivity,
							Constant.CLICKED_MUNSIC_NAME_DOWN,
							musicPlayService.getCurrentPosition());
					changeUIStatusOnPlayDown(musicPlayService
							.getCurrentPosition());
				}
			} else if (intent.getAction().endsWith("updateLikeText")) {
				String musicFlag = SharedPrefUtil.getString(mainActivity,
						Constant.MUSIC_FLAG, "");
				if (musicFlag.equals("like_music")
						&& MainFragmentActivity.likeMusciList.size() != 0) {
					Log.e("接收到了喜欢音乐", "接收到了喜欢音乐");
					SharedPrefUtil.setInt(mainActivity,
							Constant.CLICKED_MUNSIC_NAME_LIKE,
							musicPlayService.getCurrentPosition());
					changeUIStatusOnPlayLike(musicPlayService
							.getCurrentPosition());
				}
			}
		}
	}

}
