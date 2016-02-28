package com.esint.music.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.Fragment;
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
import com.esint.music.adapter.DownMusicAdapter;
import com.esint.music.model.DownImageInfo;
import com.esint.music.model.Mp3Info;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.service.MusicPlayService;
import com.esint.music.service.MusicPlayService.PlayBinder;
import com.esint.music.sortlistview.CharacterParser;
import com.esint.music.sortlistview.PinyinComparator;
import com.esint.music.sortlistview.SideBar;
import com.esint.music.sortlistview.SideBar.OnTouchingLetterChangedListener;
import com.esint.music.sortlistview.SortAdapter;
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
	private SortAdapter adapter;
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

		broadcastReceiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.PLAYBUTTON_BROAD);
		filter.addAction(Constant.PAUSEBUTTON_BROAD);
		getActivity().registerReceiver(broadcastReceiver, filter);

		musicFlag = SharedPrefUtil.getString(mainActivity, Constant.MUSIC_FLAG,
				"");
		
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
			randomPlay();

			break;
		case R.id.ib_pause: {
			Toast.makeText(mainActivity, "点击了暂停按钮", 0).show();
			musicPlayService.pause();
			albumImg.clearAnimation();
			playButton.setVisibility(View.VISIBLE);
			pauseButton.setVisibility(View.GONE);
			int progress = progressBar.getProgress();
			progressBar.setProgress(progress);
			break;
		}
		case R.id.ib_play: {
			currentPlayPosition = SharedPrefUtil.getInt(getActivity(),
					Constant.CLICKED_MUNSIC_NAME, -1);
			int recordDownMusicPosition = SharedPrefUtil.getInt(getActivity(),
					Constant.CLICKED_MUNSIC_NAME_DOWN, -1);
			if (currentPlayPosition == -1) {
				return;
			}

			if (Constant.isFirst && musicFlag.equals("local_music")) {
				Constant.ISFirst_PLAY = false;
				// 得到记录的位置
				Toast.makeText(getActivity(), "开始进来点击播放按钮了", 0).show();
				musicPlayService.playLocalMusic(currentPlayPosition);
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.INVISIBLE);
				startAnim();
				if (currentPlayPosition != -1) {
					// // 设置进度条
					progressBar.setMax((int) mp3List.get(currentPlayPosition)
							.getDuration());
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
					Constant.isFirst = false;
				}
			} else if (Constant.isFirst && musicFlag.equals("like_music")) {
				Constant.ISFirst_PLAY = false;
				// 得到记录的位置
				Toast.makeText(getActivity(), "开始进来点击播放按钮了", 0).show();
				musicPlayService.playLocalMusic(currentPlayPosition);
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.INVISIBLE);
				startAnim();
				if (currentPlayPosition != -1&&sortMyLikeMp3Infos!=null) {
					// // 设置进度条
					progressBar.setMax((int) sortMyLikeMp3Infos.get(
							currentPlayPosition).getDuration());
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
					Constant.isFirst = false;
				}
			} else if (Constant.isFirst && musicFlag.equals("down_music")) {
				Constant.ISFirst_PLAY = false;
				// 得到记录的位置
				Toast.makeText(getActivity(), "开始进来点击播放按钮了", 0).show();
				musicPlayService.playMyDown(recordDownMusicPosition);
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.INVISIBLE);
				startAnim();
				if (recordDownMusicPosition != -1&&downMusicList!=null) {
					// // 设置进度条
					progressBar.setMax((int) MediaUtils
							.getTrackLength(downMusicList.get(
									recordDownMusicPosition)
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
					Constant.isFirst = false;
				}
			}

			else if (!Constant.isFirst) {
				Toast.makeText(getActivity(), "点击播放按钮了", 0).show();
				musicPlayService.start();
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.INVISIBLE);
				startAnim();
			}

			break;
		}
		case R.id.ib_next: {
			Toast.makeText(getActivity(), "点击了下一首按钮", 0).show();
			currentPlayPosition = SharedPrefUtil.getInt(getActivity(),
					Constant.CLICKED_MUNSIC_NAME, -1);
			
			if (currentPlayPosition == -1) {
				return;
			}

			playButton.setVisibility(View.INVISIBLE);
			pauseButton.setVisibility(View.VISIBLE);
			musicPlayService.next();
			int currentPosition = musicPlayService.getCurrentPosition();
			// 点击item的时候 存入播放的位置
			SharedPrefUtil.setInt(getActivity(), Constant.CLICKED_MUNSIC_NAME,
					currentPosition);
			changeUIStatusOnPlay(currentPosition);
			break;
		}
		}
	}

	private void startAnim() {
		// 开始旋转专辑图片的动画
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

	@Override
	public void onResume() {
		super.onResume();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
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
				// musicPlayer = new NetMusicPlayer(progressBar);
				// musicPlayer.pause();
				// musicPlayer.stop();
				// 点击item的时候 存入播放的位置
				SharedPrefUtil.setString(mainActivity, Constant.MUSIC_FLAG,
						Constant.MY_LOCAL_MUSIC);
				SharedPrefUtil.setInt(getActivity(),
						Constant.CLICKED_MUNSIC_NAME, position);
				int recordPosition = SharedPrefUtil.getInt(mainActivity,
						Constant.CLICKED_MUNSIC_NAME, -1);
				Constant.isFirst = false;
				Constant.ISFirst_PLAY = false;
				if (musicPlayService.getCurrentPosition() == recordPosition) {
					return;
				}

				progressBar.setMax((int) mp3List.get(position).getDuration());
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							progressBar.setProgress(musicPlayService
									.getCurrentProgress());
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();

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

			}
		});

		mp3List = filledData(mp3List);
		// 根据a-z进行排序源数据
		Collections.sort(mp3List, pinyinComparator);
		adapter = new SortAdapter(getActivity(), mp3List);
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

		MyApplication app = (MyApplication) getActivity().getApplication();
		try {
			List<Mp3Info> myLikeMp3Infos = app.dbUtils.findAll(Mp3Info.class);
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
				songCount.setText("共有" + "0首歌曲");
				action.addPage(myFavoriteLayout);
				return;
			}
			songCount.setText("共有" + myLikeMp3Infos.size() + "首歌曲");
			sortMyLikeMp3Infos = filledData(myLikeMp3Infos);
			// 根据a-z进行排序源数据
			Collections.sort(sortMyLikeMp3Infos, pinyinComparator);
			final SortAdapter adapter_fav = new SortAdapter(getActivity(),
					sortMyLikeMp3Infos);
			sortListView_fav.setAdapter(adapter_fav);

			// 设置右侧触摸监听
			sideBar_fav
					.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

						@Override
						public void onTouchingLetterChanged(String s) {
							// 该字母首次出现的位置
							int position = adapter_fav.getPositionForSection(s
									.charAt(0));
							if (position != -1) {
								sortListView_fav.setSelection(position);
							}

						}
					});
			sortListView_fav.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (position == sortMyLikeMp3Infos.size()) {
						return;
					}
					Toast.makeText(getActivity(), "点击的位置" + position, 0).show();
					// 点击item的时候 存入播放的位置
					SharedPrefUtil.setString(mainActivity, Constant.MUSIC_FLAG,
							Constant.MY_LIKE_MUSIC);
					Constant.isFirst = false;
					Constant.ISFirst_PLAY = false;
					progressBar.setMax((int) sortMyLikeMp3Infos.get(position)
							.getDuration());
					new Thread(new Runnable() {
						@Override
						public void run() {
							while (true) {
								progressBar.setProgress(musicPlayService
										.getCurrentProgress());
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}).start();
					musicPlayService.playMyFav(position);
					// 设置专辑图片和歌曲信息
					Bitmap bitmap = MediaUtils.getArtwork(mainActivity,
							sortMyLikeMp3Infos.get(position).getId(),
							sortMyLikeMp3Infos.get(position).getAlbumId(),
							true, true);
					albumImg.setImageBitmap(bitmap);
					// 开始旋转专辑图片的动画
					Animation operatingAnim = AnimationUtils.loadAnimation(
							getActivity(), R.anim.tip);
					LinearInterpolator lin = new LinearInterpolator();
					operatingAnim.setInterpolator(lin);
					if (operatingAnim != null) {
						albumImg.startAnimation(operatingAnim);
					}
					singer.setText(sortMyLikeMp3Infos.get(position).getArtist());
					songName.setText(sortMyLikeMp3Infos.get(position)
							.getTitle());
					SharedPrefUtil.setInt(mainActivity,
							Constant.CLICKED_MUNSIC_NAME, position);
					// 改变了listView 务必刷新适配器
					adapter_fav.notifyDataSetChanged();
					// // 图标改成暂停状态
					playButton.setVisibility(View.GONE);
					pauseButton.setVisibility(View.VISIBLE);

				}
			});

		} catch (DbException e) {
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
		final DownMusicAdapter adapter_down = new DownMusicAdapter(
				getActivity(), downMusicList);
		final String ImageTarget = Environment.getExternalStorageDirectory()
				+ "/" + "/下载的图片" + "/";
		final ArrayList<DownImageInfo> imagFilesPath = MediaUtils.GetImagFiles(
				ImageTarget, ".jpg", true);

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
				if (musicPlayService.getCurrentPosition() == recordPosition) {
					return;
				}

				Toast.makeText(mainActivity, "点击了第" + position + "个", 0).show();
				mainActivity.musicPlayService.playMyDown(position);

				// 设置进度条
				progressBar.setMax((int) MediaUtils
						.getTrackLength(downMusicList.get(recordPosition)
								.getDownMusicDuration()));
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

				// 设置专辑图片和歌曲的信息
				singer.setText(downMusicList.get(position).getDownMusicArtist());
				songName.setText(downMusicList.get(position).getDownMusicName());
				Bitmap albumBit;
				for (int i = 0; i < imagFilesPath.size(); i++) {
					if (imagFilesPath
							.get(i)
							.getDownImagePath()
							.equals(ImageTarget
									+ downMusicList.get(position)
											.getDownMusicName().trim() + ".jpg")) {
						albumBit = BitmapFactory.decodeFile(imagFilesPath
								.get(i).getDownImagePath(), null);
						albumImg.setImageBitmap(albumBit);
					}
				}
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
	private void randomPlay() {
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
				}

			} else if (intent.getAction().equals(Constant.PAUSEBUTTON_BROAD)) {
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
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
			// 拿到是否播放了
			boolean isPlaying = musicPlayService.isPlaying();
			if (isPlaying == true) {
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
			} else {
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			}
		}
	};

}
