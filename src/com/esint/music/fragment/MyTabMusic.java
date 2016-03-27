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

	// ���������б��µĿؼ�
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private LocalMusicAdapter adapter;
	private TextView songCount;
	private TextView musicnNmber;// ��������
	// ����ת����ƴ������
	private CharacterParser characterParser;
	private List<Mp3Info> mp3List;
	// ����ƴ��������ListView�����������
	private PinyinComparator pinyinComparator;

	private ImageView albumImg;// ר��ͼƬ
	private AlwaysMarqueeTextView singer;// ����
	private AlwaysMarqueeTextView songName;// ��������
	private ImageButton playButton;// ���Ű�ť
	private ImageButton pauseButton;// ��ͣ��ť
	private ImageButton nextButton;// ��һ��
	private ProgressBar progressBar;
	private int currentPlayPosition;

	private MainFragmentActivity mainActivity;
	private MyBroadcastReceiver broadcastReceiver;
	private MusicPlayService musicPlayService;
	public static ArrayList<Mp3Info> sortMyLikeMp3Infos;
	private String musicFlag;
	private ArrayList<DownMucicInfo> downMusicList;
	private DownMusicAdapter adapter_down;// �ҵ������б��������
	private LikeMusicAdapter adapter_fav;
	private String imageTarget;// ���ص�ͼƬ��ŵ�·��
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
		initAnim();// ��ʼ��������Ű�ť�Ķ���Ч��
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
		// ������
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

		// �������ݿ�
		// mySQLite = new MySQLite(getActivity(), "Music.db", null, 1);
		// db = mySQLite.getWritableDatabase();
		final int recordLikeMusicPosition = SharedPrefUtil.getInt(
				getActivity(), Constant.CLICKED_MUNSIC_NAME_LIKE, -1);
		Intent intent = new Intent(getActivity(), MusicPlayService.class);
		getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
		// ʵ��������תƴ����
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		mp3List = MediaUtils.getMp3Info(getActivity());
		mp3List = filledData(mp3List);
		// ����a-z��������Դ����
		Collections.sort(mp3List, pinyinComparator);
		musicnNmber.setText(mp3List.size() + "�׸���");

		broadCast = new MyBroadCast();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("updateLocalText");
		intentFilter.addAction("updateDownText");
		intentFilter.addAction("updateLikeText");
		getActivity().registerReceiver(broadCast, intentFilter);

		// ���ظ������ļ���
		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/���صĸ���";
		downMusicList = MediaUtils.GetMusicFiles(MusicTarget, ".mp3", true);
		imageTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/���ص�ͼƬ" + "/";

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
					// ���item��ʱ�� ���벥�ŵ�λ��
					SharedPrefUtil.setInt(getActivity(),
							Constant.CLICKED_MUNSIC_NAME,
							musicPlayService.getCurrentPosition());
					changeUIStatusOnPlay(musicPlayService.getCurrentPosition());
					break;
				}
				case Constant.NEXT_LEKE_MUSIC: {
					// �����ϲ���б���ȡ��ϲ���İ�ťʱ �жϵ�ǰ���ŵĸ����Ƿ�͵��ȡ���ĸ���λ��һ��
					String musicTitle = (String) msg.obj;
					if (musicTitle.equals(songName.getText().toString())&&MainFragmentActivity.likeMusciList.size()>=2) {
						// ������һ�׵�ϲ���������б�
						Log.e("������", "������");
						musicPlayService.nextLikeMusic();
						int currentPositionDown = musicPlayService
								.getCurrentPosition();
						// ���item��ʱ�� ���벥�ŵ�λ��
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
			Toast.makeText(getActivity(), "���Ϊ������һ�׸�", 0).show();
			randomPlayAnim();
			Random random = new Random();
			musicPlayService.playLocalMusic(random.nextInt(mp3List.size()));
			playButton.setVisibility(View.GONE);
			pauseButton.setVisibility(View.VISIBLE);
			// ���item��ʱ�� ���벥�ŵ�λ��
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
				// �õ���¼��λ��

				musicPlayService.playLocalMusic(currentPlayPosition);
				SharedPrefUtil.setString(mainActivity, Constant.MUSIC_FLAG,
						Constant.MY_LOCAL_MUSIC);
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
				startAnim();
				if (currentPlayPosition != -1) {
					// // ���ý�����
					progressBar.setMax((int) mp3List.get(currentPlayPosition)
							.getDuration());
					updateProgress();
					Constant.isFirst = false;
				}
				Log.e("����Ĳ��Ž�����Ǳ��ص�����", "������Ž�����Ǳ��ص�����");
			} else if (Constant.isFirst && musicFlag.equals("like_music")
					&& MainFragmentActivity.likeMusciList.size() != 0) {
				Constant.ISFirst_PLAY = false;
				// �õ���¼��λ��
				musicPlayService.playMyFav(recordLikeMusicPosition);
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
				startAnim();
				if (recordLikeMusicPosition != -1
						&& MainFragmentActivity.likeMusciList != null) {
					// // ���ý�����
					progressBar.setMax(new Long(
							MainFragmentActivity.likeMusciList.get(
									musicPlayService.getCurrentPosition())
									.getMusicTime()).intValue());
					updateProgress();
					Constant.isFirst = false;
					Log.e("����Ĳ��Ž������ϲ��������", "������Ž������ϲ��������");
				}
			} else if (Constant.isFirst && musicFlag.equals("down_music")) {
				Constant.ISFirst_PLAY = false;
				// �õ���¼��λ��
				musicPlayService.playMyDown(recordDownMusicPosition);
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
				startAnim();
				if (recordDownMusicPosition != -1 && downMusicList != null) {
					// // ���ý�����
					progressBar.setMax((int) MediaUtils
							.getTrackLength(downMusicList.get(
									musicPlayService.getCurrentPosition())
									.getDownMusicDuration()));
					Log.e("���뵽�����������", "���뵽�����������");
					updateProgress();
					Constant.isFirst = false;
				}
				Log.e("����Ĳ��Ž���������ص�����", "������Ž���������ص�����");
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
				// ���item��ʱ�� ���벥�ŵ�λ��
				SharedPrefUtil.setInt(getActivity(),
						Constant.CLICKED_MUNSIC_NAME, currentPosition);
				changeUIStatusOnPlay(currentPosition);

			} else if (musicFlag.equals("down_music")) {
				musicPlayService.nextDownMusic();
				int currentPositionDown = musicPlayService.getCurrentPosition();
				// ���item��ʱ�� ���벥�ŵ�λ��
				SharedPrefUtil.setInt(getActivity(),
						Constant.CLICKED_MUNSIC_NAME_DOWN, currentPositionDown);
				changeUIStatusOnPlayDown(currentPositionDown);
			} else if (musicFlag.equals("like_music")
					&& MainFragmentActivity.likeMusciList.size() != 0) {
				musicPlayService.nextLikeMusic();
				int currentPositionDown = musicPlayService.getCurrentPosition();
				// ���item��ʱ�� ���벥�ŵ�λ��
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

	// ��������ҵ����ص�ʱ�򣬵����һ�׵�ʱ����ĵ����ҵ����ص�������ߵ�����
	private void changeUIStatusOnPlayDown(int currentPositionDown) {

		Bitmap albumBit = BitmapFactory.decodeFile(imageTarget
				+ downMusicList.get(currentPositionDown).getDownMusicName()
						.trim() + ".jpg", null);
		if (albumBit != null) {
			albumImg.setImageBitmap(albumBit);
		}
		// ��ʼ��תר��ͼƬ�Ķ���
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
		// ���ý�����
		progressBar.setMax((int) MediaUtils.getTrackLength(downMusicList.get(
				currentPositionDown).getDownMusicDuration()));
		updateProgress();
		// ����֪ͨ��
		musicPlayService.updateNotification(albumBit, singer.getText()
				.toString(), songName.getText().toString());
	}

	// ��������ҵ����ص�ʱ�򣬵����һ�׵�ʱ����ĵ����ҵ����ص�������ߵ�����
	private void changeUIStatusOnPlayLike(int currentPositionDown) {
		// ��ʼ��תר��ͼƬ�Ķ���
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
		// ���ý�����
		progressBar.setMax(new Long(MainFragmentActivity.likeMusciList.get(
				musicPlayService.getCurrentPosition()).getMusicTime())
				.intValue());
		updateProgress();
		// ����֪ͨ��
		musicPlayService.updateNotification(MainFragmentActivity.likeMusciList
				.get(musicPlayService.getCurrentPosition()).getBitmap(), singer
				.getText().toString(), songName.getText().toString());

	}

	// ��ʼ��תר��ͼƬ�Ķ���
	private void startAnim() {
		Animation operatingAnim = AnimationUtils.loadAnimation(getActivity(),
				R.anim.tip);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		if (operatingAnim != null) {
			albumImg.startAnimation(operatingAnim);
		}
	}

	// �����б���Ϣ
	public void changeUIStatusOnPlay(int position) {
		Bitmap bitmap = MediaUtils.getArtwork(mainActivity,
				mp3List.get(position).getId(), mp3List.get(position)
						.getAlbumId(), true, true);
		Bitmap notifiBitmap = MediaUtils.getArtwork(mainActivity,
				mp3List.get(position).getId(), mp3List.get(position)
						.getAlbumId(), true, false);
		albumImg.setImageBitmap(bitmap);
		// ��ʼ��תר��ͼƬ�Ķ���
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
		// ���ý�����
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

	// ���ر������ֵ���ͼ
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
		// ���ؼ�
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
		// listView�Ų��ֵĲ����ļ�
		View footerView = inflater.inflate(R.layout.sortlist_footerview, null,
				false);
		sortListView = (ListView) localView.findViewById(R.id.local_songlv);
		songCount = (TextView) footerView.findViewById(R.id.songCount);
		sideBar = (SideBar) localView.findViewById(R.id.sidrbar);
		dialog = (TextView) localView.findViewById(R.id.dialog);
		sideBar.setTextView(dialog);
		sortListView.addFooterView(footerView);
		// ���ýŲ���
		songCount.setText("����" + mp3List.size() + "�׸���");

		// �����Ҳഥ������
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// ����ĸ�״γ��ֵ�λ��
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
				// ���item��ʱ�� ���벥�ŵ�λ��
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
				// ����ר��ͼƬ�͸�����Ϣ
				Bitmap bitmap = MediaUtils.getArtwork(mainActivity, mp3List
						.get(position).getId(), mp3List.get(position)
						.getAlbumId(), true, true);
				albumImg.setImageBitmap(bitmap);

				// ��ʼ��תר��ͼƬ�Ķ���
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
				// �ı���listView ���ˢ��������
				adapter.notifyDataSetChanged();
				// // ͼ��ĳ���ͣ״̬
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);
				Bitmap notifyBitmap = MediaUtils.getArtwork(mainActivity,
						mp3List.get(position).getId(), mp3List.get(position)
								.getAlbumId(), true, false);
				// ����֪ͨ��
				musicPlayService.updateNotification(notifyBitmap, singer
						.getText().toString(), songName.getText().toString());

			}
		});

		mp3List = filledData(mp3List);
		// ����a-z��������Դ����
		Collections.sort(mp3List, pinyinComparator);
		adapter = new LocalMusicAdapter(getActivity(), mp3List);
		sortListView.setAdapter(adapter);
		action.addPage(localView);
	}

	/**
	 * ΪListView�������
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
			// ����ת����ƴ��
			String pinyin = characterParser.getSelling(date.get(i).getArtist());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
			if (sortString.matches("[A-Z]")) {
				mp3Info.setSortLetters(sortString.toUpperCase());
			} else {
				mp3Info.setSortLetters("#");
			}

			mSortList.add(mp3Info);
		}
		return mSortList;
	}

	// �����ҵ������ͼ
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

			// listView�Ų��ֵĲ����ļ�
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
			// ���ýŲ���
			if (myLikeMp3Infos == null) {
				songCount.setText("����0�׸���");
				action.addPage(myFavoriteLayout);
				return;
			}
			songCount.setText("����" + myLikeMp3Infos.size() + "�׸���");
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
					// ���item��ʱ�� ���벥�ŵ�λ��
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
					// ����ר��ͼƬ�͸�����Ϣ
					albumImg.setImageBitmap(MainFragmentActivity.likeMusciList
							.get(musicPlayService.getCurrentPosition())
							.getBitmap());
					// ��ʼ��תר��ͼƬ�Ķ���
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
					// �ı���listView ���ˢ��������
					adapter_fav.notifyDataSetChanged();
					// // ͼ��ĳ���ͣ״̬
					playButton.setVisibility(View.GONE);
					pauseButton.setVisibility(View.VISIBLE);
					// ����֪ͨ��
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

	// �����ҵ����ص���ͼ
	private void myDownLoad() {
		// ���ظ������ļ���
		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/���صĸ���";
		downMusicList = MediaUtils.GetMusicFiles(MusicTarget, ".mp3", true);
		adapter_down = new DownMusicAdapter(getActivity(), downMusicList);
		final String ImageTarget = Environment.getExternalStorageDirectory()
				+ "/" + "/���ص�ͼƬ" + "/";
		View downLoadView = View.inflate(mainActivity,
				R.layout.activity_my_download, null);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// listView�Ų��ֵĲ����ļ�
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
		// ���ýŲ���
		if (downMusicList == null) {
			songCount.setText("����" + "0�׸���");
			action.addPage(downLoadView);
			return;
		}
		songCount.setText("����" + downMusicList.size() + "�׸���");
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

				// ���item��ʱ�� ���벥�ŵ�λ��
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

				// ���ý�����
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

				// ����ר��ͼƬ�͸�������Ϣ
				singer.setText(downMusicList.get(position).getDownMusicArtist());
				songName.setText(downMusicList.get(position).getDownMusicName());
				Bitmap albumBit = BitmapFactory.decodeFile(ImageTarget
						+ downMusicList.get(position).getDownMusicName().trim()
						+ ".jpg", null);
				if (albumBit != null) {
					albumImg.setImageBitmap(albumBit);
				}

				// ����֪ͨ��
				musicPlayService.updateNotification(albumBit, singer.getText()
						.toString(), songName.getText().toString());

				// ��ʼ��תר��ͼƬ�Ķ���
				Animation operatingAnim = AnimationUtils.loadAnimation(
						getActivity(), R.anim.tip);
				LinearInterpolator lin = new LinearInterpolator();
				operatingAnim.setInterpolator(lin);
				if (operatingAnim != null) {
					albumImg.startAnimation(operatingAnim);
				}
				SharedPrefUtil.setInt(mainActivity,
						Constant.CLICKED_MUNSIC_NAME_DOWN, position);
				// �ı���listView ���ˢ��������
				adapter_down.notifyDataSetChanged();
				// // ͼ��ĳ���ͣ״̬
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);
			}

		});
		sortListView_down.setAdapter(adapter_down);
		action.addPage(downLoadView);
	}

	// ������Ű�ť�Ķ���
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

	// �õ�path
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

	// ����path�õ�bitmap
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

	// ����״̬��
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

	// ���ݽ��յ��Ĺ㲥�����²��ź���ͣ�İ�ť
	public class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constant.PLAYBUTTON_BROAD)) {
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
				// ���ý�����
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
			// ��ʼ��תר��ͼƬ�Ķ���
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

	// �����̸߳��½�����
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

	// ���ܵ��㲥��������
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
					Log.e("���յ��˱��ص�����", "���յ��˱��ص�����");
				}
			} else if (intent.getAction().endsWith("updateDownText")) {
				String musicFlag = SharedPrefUtil.getString(mainActivity,
						Constant.MUSIC_FLAG, "");
				if (musicFlag.equals("down_music")) {
					Log.e("���յ�����������", "���յ�����������");
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
					Log.e("���յ���ϲ������", "���յ���ϲ������");
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
