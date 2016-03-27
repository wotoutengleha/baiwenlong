package com.esint.music.activity;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.R.color;
import com.esint.music.XListView.XListView;
import com.esint.music.XListView.XListView.IXListViewListener;
import com.esint.music.adapter.SearchResultAdapter;
import com.esint.music.db.MySQLite;
import com.esint.music.dialog.Effectstype;
import com.esint.music.dialog.NiftyDialogBuilder;
import com.esint.music.fragment.MyTabMusic;
import com.esint.music.fragment.NetMusicFragment;
import com.esint.music.history.SearchShopActivity;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.model.LikeMusicModel;
import com.esint.music.model.Mp3Info;
import com.esint.music.model.SearchMusicInfo;
import com.esint.music.service.MusicPlayService;
import com.esint.music.service.MusicPlayService.PlayBinder;
import com.esint.music.slidemenu.SlidingMenu;
import com.esint.music.utils.ActivityCollectUtil;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.MyApplication;
import com.esint.music.utils.MyHttpUtils;
import com.esint.music.utils.PageAction;
import com.esint.music.utils.SortListUtil;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.view.AlwaysMarqueeTextView;
import com.esint.music.view.MainFunctionPop;
import com.esint.music.view.SystemStatusManager;

@SuppressLint("DefaultLocale")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainFragmentActivity extends BaseActivity implements
		OnClickListener {
	private ViewPager mMianVp;
	private ImageView mTabline;
	private int mScreenWidth;// ÿһ���ϵ����Ŀ�
	private int mCurrentPageIndex;// ��ǰ����һ��
	private List<Fragment> mFragLists;
	private TextView myMusicTv, netMusicTv;
	private ImageButton menuIb;
	private ImageView main_more_functions;
	private MainFunctionPop popMenuMain;
	private Effectstype effect;
	private NiftyDialogBuilder dialogBuilder;
	private LinearLayout mainLayout;// ��ҳ��ı���
	private LinearLayout bottomLayout;// �ײ����Ű�ť�Ĳ���
	private RelativeLayout mainActionBar;

	private View searchView;
	private MenuAction action;
	private LinearLayout contentRelativeLayout;
	public SlidingMenu slidingMenu;
	private ImageView albumImg;// ר��ͼƬ
	private ImageView searchButton;// ������ť
	private AlwaysMarqueeTextView musicName;
	private AlwaysMarqueeTextView musicSinger;
	private int currentPlayPosition;// �õ���¼��λ��
	private String musciFlag;// �Ǳ������ֻ����ҵ��
	// ����ת����ƴ������
	private ArrayList<Mp3Info> mp3Infos;// ��������
	private ArrayList<DownMucicInfo> downMusicList;// ���صĸ���
	private MyHttpUtils myHttpUtils;// ��������Ĺ�����
	public MusicPlayService musicPlayService;

	private RelativeLayout randomLayout;
	private RelativeLayout orderLayout;
	private RelativeLayout singleLayout;
	public static Handler mHandler;
	private TextView tvPlayMode;
	public static ArrayList<LikeMusicModel> likeMusciList = new ArrayList<LikeMusicModel>();
	private MySQLite mySQLite;
	public static SQLiteDatabase db;

	// ע�� ��������ѡ���� ��Ҫ���� android:clickable="true" �ɿɵ����״̬

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main_music);
		ActivityCollectUtil.addActivity(this);
		bindService();// �󶨷���
		initView();
		initSlidingMenu();
		initData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// ������
		unbindService(connection);
		int playMode = MusicPlayService.getPlayMode();
		SharedPrefUtil.setInt(this, Constant.PLAY_MODE, playMode);
		ActivityCollectUtil.removeActivity(this);
	}

	// ������
	@Override
	public void onResume() {
		super.onResume();
		getBitmapPath();
		currentPlayPosition = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME, -1);
		int currentPlayPositionDown = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME_DOWN, -1);
		int currentPlayPositionLike = SharedPrefUtil.getInt(this,
				Constant.CLICKED_MUNSIC_NAME_LIKE, -1);
		musciFlag = SharedPrefUtil.getString(this, Constant.MUSIC_FLAG, "");

		if (currentPlayPosition != -1 && musciFlag.equals("local_music")) {
			musicName.setText(mp3Infos.get(currentPlayPosition).getTitle());
			musicSinger.setText(mp3Infos.get(currentPlayPosition).getArtist());
			Bitmap bitmap = MediaUtils.getArtwork(this,
					mp3Infos.get(currentPlayPosition).getId(),
					mp3Infos.get(currentPlayPosition).getAlbumId(), true, true);
			albumImg.setImageBitmap(bitmap);
		} else if (currentPlayPositionLike != -1
				&& musciFlag.equals("like_music")
				&& MainFragmentActivity.likeMusciList.size() != 0) {
			musicName.setText(MainFragmentActivity.likeMusciList.get(
					currentPlayPositionLike).getMusicName());
			musicSinger.setText(MainFragmentActivity.likeMusciList.get(
					currentPlayPositionLike).getMusicArtist());
			albumImg.setImageBitmap(MainFragmentActivity.likeMusciList.get(
					currentPlayPositionLike).getBitmap());
		} else {

			if (currentPlayPositionDown != -1 && musciFlag.equals("down_music")) {
				musicName.setText(downMusicList.get(currentPlayPositionDown)
						.getDownMusicName());
				musicSinger.setText(downMusicList.get(currentPlayPositionDown)
						.getDownMusicArtist());
				String ImageTarget = Environment.getExternalStorageDirectory()
						+ "/" + "/���ص�ͼƬ" + "/";
				// ���ظ������ļ���
				String MusicTarget = Environment.getExternalStorageDirectory()
						+ "/" + "/���صĸ���";
				ArrayList<DownMucicInfo> downMusicList = MediaUtils
						.GetMusicFiles(MusicTarget, ".mp3", true);
				Bitmap albumBit = BitmapFactory.decodeFile(ImageTarget
						+ downMusicList.get(currentPlayPositionDown)
								.getDownMusicName().trim() + ".jpg", null);
				if (albumBit != null) {
					albumImg.setImageBitmap(albumBit);
				}
			}
		}
		int colorIndex = SharedPrefUtil.getInt(MainFragmentActivity.this,
				Constant.COLOR_INDEX, -1);
		switch (colorIndex) {
		case 0:
			mainActionBar.setBackgroundResource(color.tianyilan);
			break;
		case 1:
			mainActionBar.setBackgroundResource(color.hidden_bitterness);
			break;
		case 2:
			mainActionBar.setBackgroundResource(color.gorgeous);
			break;
		case 3:
			mainActionBar.setBackgroundResource(color.romance);
			break;
		case 4:
			mainActionBar.setBackgroundResource(color.sunset);
			break;
		case 5:
			mainActionBar.setBackgroundResource(color.warm_colour);
			break;

		default:
			mainActionBar.setBackgroundResource(color.holo_blue_light);
			break;
		}

	}

	private void initView() {
		mTabline = (ImageView) findViewById(R.id.id_iv_tabline);
		mMianVp = (ViewPager) findViewById(R.id.vp_main);
		myMusicTv = (TextView) findViewById(R.id.id_tv_mymusic);
		netMusicTv = (TextView) findViewById(R.id.id_tv_recommand);
		menuIb = (ImageButton) findViewById(R.id.ib_menu);
		main_more_functions = (ImageView) findViewById(R.id.main_more_functions);
		mainLayout = (LinearLayout) findViewById(R.id.mainpopu);
		mainActionBar = (RelativeLayout) findViewById(R.id.actionbar);

		slidingMenu = (SlidingMenu) findViewById(R.id.slidingMenu);
		initSlidingContent();
		myMusicTv.setOnClickListener(this);
		netMusicTv.setOnClickListener(this);
		main_more_functions.setOnClickListener(this);
		menuIb.setOnClickListener(this);
		// ��ʼ��tabLine
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		mScreenWidth = metrics.widthPixels / 2;
		LayoutParams lp = mTabline.getLayoutParams();
		lp.width = mScreenWidth;
		mTabline.setLayoutParams(lp);
		// �ײ��������Ŀؼ�
		searchButton = (ImageView) findViewById(R.id.btn_search);
		albumImg = (ImageView) findViewById(R.id.iv_album);
		musicName = (AlwaysMarqueeTextView) findViewById(R.id.tv_musicName);
		musicSinger = (AlwaysMarqueeTextView) findViewById(R.id.tv_musicArt);
		bottomLayout = (LinearLayout) findViewById(R.id.musiccontent);
		searchButton.setOnClickListener(this);
		bottomLayout.setOnClickListener(this);

		// �ҵ�popuwindow��Ŀؼ�
		View popuView = View.inflate(MainFragmentActivity.this,
				R.layout.mainpopu_menu, null);
		tvPlayMode = (TextView) popuView.findViewById(R.id.tv_play_random);
		randomLayout = (RelativeLayout) popuView
				.findViewById(R.id.menuRandomParent);
		orderLayout = (RelativeLayout) popuView
				.findViewById(R.id.menuOrderParent);
		singleLayout = (RelativeLayout) popuView
				.findViewById(R.id.menuRepeatoneParent);

		// btnPlayMode.setOnClickListener(this);

	}

	private void initData() {
		mySQLite = new MySQLite(MainFragmentActivity.this, "Music.db", null, 1);
		db = mySQLite.getWritableDatabase();
		likeMusciList = getFavMusicFromDB();
		Intent intent = new Intent(this, MusicPlayService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
		mFragLists = new ArrayList<Fragment>();
		mp3Infos = MediaUtils.getMp3Info(MainFragmentActivity.this);
		myHttpUtils = new MyHttpUtils(MainFragmentActivity.this);
		// ����MP3�б������
		mp3Infos = MediaUtils.getMp3Info(this);
		mp3Infos = new SortListUtil().initMyLocalMusic(mp3Infos);

		MyTabMusic myMusic = new MyTabMusic(action);
		NetMusicFragment netMusic = new NetMusicFragment(action);

		mFragLists.add(myMusic);
		mFragLists.add(netMusic);
		mMianVp.setCurrentItem(0, true);
		mMianVp.setAdapter(new myAdapter(getSupportFragmentManager(),
				mFragLists));
		mMianVp.setOnPageChangeListener(new MyPagerListener());

		// ���ظ������ļ���
		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/���صĸ���";
		downMusicList = MediaUtils.GetMusicFiles(MusicTarget, ".mp3", true);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				Log.e("���ܵ�����Ϣ", "���յ�����Ϣ");

				Log.e("��ǰ�Ĳ���ģʽ��", MusicPlayService.getPlayMode() + "");

				switch (MusicPlayService.getPlayMode()) {
				case MusicPlayService.PLAY_ORDER:
					randomLayout.setVisibility(View.VISIBLE);
					singleLayout.setVisibility(View.INVISIBLE);
					orderLayout.setVisibility(View.INVISIBLE);
					tvPlayMode.setText("�������");
					Log.e("�����", "�����");
					break;
				case MusicPlayService.PLAY_RANDOM:
					singleLayout.setVisibility(View.VISIBLE);
					randomLayout.setVisibility(View.INVISIBLE);
					orderLayout.setVisibility(View.INVISIBLE);
					Log.e("������", "������");
					tvPlayMode.setText("����ѭ��");
					break;
				case MusicPlayService.PLAY_SINGLE:
					orderLayout.setVisibility(View.INVISIBLE);
					singleLayout.setVisibility(View.VISIBLE);
					randomLayout.setVisibility(View.INVISIBLE);
					tvPlayMode.setText("�б�ѭ��");
					Log.e("�б��� ", "�б���");
					break;
				}

			}
		};
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			initSearch();
			break;

		}
		return true;
	}

	// ������
	private void initSearch() {
		LayoutInflater inflater = getLayoutInflater();
		searchView = inflater
				.inflate(R.layout.activity_localmusic, null, false);
		action.addPage(searchView);

	}

	private void initSlidingMenu() {
		slidingMenu = (SlidingMenu) findViewById(R.id.slidingMenu);
		// �������󻬻����һ����������Ҷ����Ի�
		slidingMenu.setMode(SlidingMenu.LEFT);
		// ����Ҫʹ�˵�������������Ļ�ķ�Χ
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setContent(R.layout.sliding_content);
		// ���û���ʱ��קЧ��
		slidingMenu.setBehindScrollScale(0);
		// ���û���ʱ�˵����Ƿ��뵭��
		slidingMenu.setFadeEnabled(true);
		// ���õ��뵭���ı���
		slidingMenu.setFadeDegree(1f);
		slidingMenu.showMenu();
		action = new MenuAction();
	}

	// ����¼�
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_tv_mymusic:
			mMianVp.setCurrentItem(0, true);
			break;
		case R.id.id_tv_recommand:
			mMianVp.setCurrentItem(1, true);
			break;
		case R.id.main_more_functions:

			break;
		case R.id.ib_menu:
			initPopuWindow();
			break;
		case R.id.btn_search:
//			searchMusicView();
			startActivity(new Intent(MainFragmentActivity.this,SearchShopActivity.class));
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			break;
		case R.id.musiccontent: {
			String musciFlag = SharedPrefUtil.getString(this,
					Constant.MUSIC_FLAG, "");
			if (musciFlag.equals("NET_MUSIC")) {
				return;
			} else {
				// �������ظ����ĸ����͸���
				Intent intent = new Intent(MainFragmentActivity.this,
						MusicPlayAvtivity.class);
				intent.putExtra("Music_name", musicName.getText().toString());
				intent.putExtra("Music_artist", musicSinger.getText()
						.toString());
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
				break;
			}
		}
		}
	}

	private ProgressDialog progressDialog;
	private boolean isRefresh = true;
	private SearchResultAdapter resultAdapter;
	private EditText searchEt;// ������������Ĺؼ���

	private void searchMusicView() {

		progressDialog = new ProgressDialog(this);
		final ArrayList<SearchMusicInfo> searchMusicList = new ArrayList<SearchMusicInfo>();
		LayoutInflater inflater = this.getLayoutInflater();
		View searchView = inflater.inflate(R.layout.activity_searchmusic, null,
				false);
		searchActionBar = (RelativeLayout) searchView
				.findViewById(R.id.actionbar);
		updateActionBar();
		ImageView btnBack = (ImageView) searchView
				.findViewById(R.id.backBtn_search);
		searchEt = (EditText) searchView.findViewById(R.id.et_search);
		ImageView btnSearch = (ImageView) searchView
				.findViewById(R.id.btn_search);
		final XListView resultLV = (XListView) searchView
				.findViewById(R.id.lv_search);
		resultLV.setPullRefreshEnable(true);
		resultLV.setPullLoadEnable(true);
		resultLV.setXListViewListener(new IXListViewListener() {

			@Override
			public void onRefresh() {
				isRefresh = true;
				// �ж��Ƿ�������
				final boolean isConnected = myHttpUtils
						.isConnnected(MainFragmentActivity.this);
				if (isConnected == false) {
					Toast.makeText(MainFragmentActivity.this, "��ǰ����û����������", 0)
							.show();
					resultLV.stopRefresh();
					return;
				}
				updateSearchMusic(0);
			}

			@Override
			public void onLoadMore() {
				isRefresh = false;
				// �ж��Ƿ�������
				final boolean isConnected = myHttpUtils
						.isConnnected(MainFragmentActivity.this);
				if (isConnected == false) {
					Toast.makeText(MainFragmentActivity.this, "��ǰ����û����������", 0)
							.show();
					resultLV.stopLoadMore();
					return;
				}
				updateSearchMusic(searchMusicList.size());

			}
		});

		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!slidingMenu.isMenuShowing()) {
					slidingMenu.showMenu();
				}
			}
		});

		btnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// �ж��Ƿ�������
				final boolean isConnected = myHttpUtils
						.isConnnected(MainFragmentActivity.this);
				String searchResult = searchEt.getText().toString();
				if (TextUtils.isEmpty(searchResult)) {
					showEmptyDialog();
				} else {
					if (isConnected == false) {
						Toast.makeText(MainFragmentActivity.this, "��ǰû����������", 0)
								.show();
						return;
					}
					progressDialog.setMessage("�������������Ժ󡣡���");
					progressDialog.show();
					searchMusicList.clear();
					updateSearchMusic(0);
				}
			}
		});
		MyHttpUtils.handler = new Handler() {

			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == Constant.WHAT_NET_HOTMUSIC_LIST) {
					onLoadEnd(resultLV);
					progressDialog.dismiss();
					if (isRefresh == true) {
						searchMusicList.clear();
						searchMusicList
								.addAll((ArrayList<SearchMusicInfo>) msg.obj);
						resultAdapter = new SearchResultAdapter(
								MainFragmentActivity.this, searchMusicList);
						resultLV.setAdapter(resultAdapter);
					} else {
						ArrayList<SearchMusicInfo> dataLoadMore = ((ArrayList<SearchMusicInfo>) msg.obj);
						searchMusicList.addAll(dataLoadMore);
						resultAdapter.notifyDataSetChanged();
					}
				} else if (msg.what == Constant.WHAT_EXECEPTION) {
					Toast.makeText(MainFragmentActivity.this, "û�и���������", 0)
							.show();
					resultLV.stopLoadMore();
				}
			}
		};

		action.addPage(searchView);
	}

	// �����������ֵ��б�
	@SuppressWarnings("deprecation")
	private void updateSearchMusic(int offset) {

		String edSearch = searchEt.getText().toString();
		List<NameValuePair> parmas = new ArrayList<NameValuePair>();
		parmas.add(new BasicNameValuePair("s", edSearch));
		parmas.add(new BasicNameValuePair("type", "1"));
		parmas.add(new BasicNameValuePair("offset", offset + ""));
		parmas.add(new BasicNameValuePair("sub", "false"));
		parmas.add(new BasicNameValuePair("limit", "6"));
		myHttpUtils.searchMusicToAPI(Constant.API_NET_SEARCH_MUSIC, parmas);
	}

	// �ؼ���Ϊ�յ�ʱ�򵯳�����dialog
	private void showEmptyDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();
		View emptyDialog = View.inflate(this, R.layout.dialog_empty, null);
		LinearLayout dismissLL = (LinearLayout) emptyDialog
				.findViewById(R.id.dismiss_ll);
		dialog.show();
		WindowManager m = getWindowManager();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		Display d = m.getDefaultDisplay(); // ��ȡ��Ļ������
		params.height = (int) (d.getHeight() * 0.24); // �߶�����Ϊ��Ļ��0.6
		params.width = (int) (d.getWidth() * 0.8); // �������Ϊ��Ļ��0.65
		dialog.getWindow().setAttributes(params);
		dialog.setContentView(emptyDialog);
		dismissLL.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	/**
	 * @Description:�ײ���popuwindow
	 * @return void
	 * @author bai
	 */
	private void initPopuWindow() {
		// ��ʼ��PopupWindow
		popMenuMain = new MainFunctionPop(MainFragmentActivity.this,
				itemsOnClick);
		// ����layout��PopupWindow����ʾ��λ��
		popMenuMain.showAtLocation(
				MainFragmentActivity.this.findViewById(R.id.mainpopu),
				Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		if (popMenuMain != null) {
			popMenuMain.setOnDismissListener(new poponDismissListener());
			backgroundAlpha(0.5f);
		}
	}

	// Ϊ��������ʵ�ּ�����
	private OnClickListener itemsOnClick = new OnClickListener() {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.menuScan:
				startActivity(new Intent(MainFragmentActivity.this,
						ScanMusicActivity.class));
				overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
				popMenuMain.dismiss();
				break;
			case R.id.menuSkin:
				startActivity(new Intent(MainFragmentActivity.this,
						BackGroundActivity.class));
				overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
				popMenuMain.dismiss();
				break;
			case R.id.menuAbout:
				startActivity(new Intent(MainFragmentActivity.this,
						AboutActivity.class));
				overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
				popMenuMain.dismiss();
				break;
			case R.id.menuPlayMode: {
				switch (MusicPlayService.getPlayMode()) {
				case MusicPlayService.PLAY_ORDER:
					musicPlayService.setPlayMode(MusicPlayService.PLAY_RANDOM);
					Toast.makeText(MainFragmentActivity.this, "�������", 0).show();
					break;
				case MusicPlayService.PLAY_RANDOM:
					musicPlayService.setPlayMode(MusicPlayService.PLAY_SINGLE);
					Toast.makeText(MainFragmentActivity.this, "����ѭ��", 0).show();
					break;
				case MusicPlayService.PLAY_SINGLE:
					musicPlayService.setPlayMode(MusicPlayService.PLAY_ORDER);
					Toast.makeText(MainFragmentActivity.this, "�б�ѭ��", 0).show();
					break;
				}
				mHandler.sendEmptyMessage(0000);
			}

				break;
			case R.id.menuSetting:
				startActivity(new Intent(MainFragmentActivity.this,
						SettingActivity.class));
				overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
				popMenuMain.dismiss();
				break;
			case R.id.menuExit:
				exitDialog();
				break;
			}

		}

	};

	// �˳������dialog

	private RelativeLayout searchActionBar;// �������ֵ�״̬��

	private void exitDialog() {
		effect = Effectstype.Shake;
		dialogBuilder = new NiftyDialogBuilder(MainFragmentActivity.this,
				R.style.dialog_untran);
		dialogBuilder.withTitle("��ʾ").withTitleColor("#FFFFFF")
				.withDividerColor("#97E8F9").withMessage("ȷ��Ҫ�˳�������")
				.withMessageColor("#FFFFFF").isCancelableOnTouchOutside(true)
				.withEffect(effect).withButton1Text("ȷ��").withButton2Text("ȡ��")
				.setCustomView(R.layout.custom_view, MainFragmentActivity.this)
				.setButton1Click(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						popMenuMain.dismiss();
						// �˳�APP
						ActivityCollectUtil.finishAllActi();

					}
				}).setButton2Click(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialogBuilder.dismiss();
					}
				}).show();

	}

	/**
	 * ����±ʼ�ʱ������popWin�رյ��¼�����Ҫ��Ϊ�˽�����͸���ȸĻ���
	 */
	class poponDismissListener implements PopupWindow.OnDismissListener {
		@Override
		public void onDismiss() {
			backgroundAlpha(1f);
		}
	}

	/**
	 * ���������Ļ�ı���͸����
	 */
	public void backgroundAlpha(float bgAlpha) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = bgAlpha; // 0.0-1.0
		getWindow().setAttributes(lp);
	}

	// viewPager��������
	class myAdapter extends FragmentPagerAdapter {
		private List<Fragment> mFragList;

		public myAdapter(FragmentManager fm, List<Fragment> mFragList) {
			super(fm);
			this.mFragList = mFragList;
		}

		@Override
		public Fragment getItem(int arg0) {
			return mFragList.get(arg0);
		}

		@Override
		public int getCount() {
			return mFragList.size();
		}
	}

	// ViewPager�Ļ��������¼�
	class MyPagerListener implements OnPageChangeListener {
		// ����״̬�ı��ʱ�����
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		// ��ǰҳ�滬����ʱ�����
		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPx) {
			LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) mTabline
					.getLayoutParams();
			if (mCurrentPageIndex == 0 && position == 0) { // 0-->1
				lp.leftMargin = (int) (positionOffset * mScreenWidth + mCurrentPageIndex
						* mScreenWidth);
			} else if (mCurrentPageIndex == 1 && position == 0) {// 1-->0
				lp.leftMargin = (int) (mCurrentPageIndex * mScreenWidth + (positionOffset - 1)
						* mScreenWidth);
			}
			mTabline.setLayoutParams(lp);
		}

		// ��ǰҳ�汻ѡ�е�ʱ�����
		@Override
		public void onPageSelected(int position) {
			resetTextView();
			switch (position) {
			case 0:
				myMusicTv.setTextColor(Color.parseColor("#008000"));
				break;
			case 1:
				netMusicTv.setTextColor(Color.parseColor("#008000"));
				break;
			}
			mCurrentPageIndex = position;
		}
	}

	// ����textView����ɫ
	private void resetTextView() {
		myMusicTv.setTextColor(Color.BLACK);
		netMusicTv.setTextColor(Color.BLACK);
	}

	private void initSlidingContent() {

	}

	class MenuAction implements PageAction {
		View view;

		@Override
		public void addPage(View view) {
			this.view = view;
			contentRelativeLayout = (LinearLayout) findViewById(R.id.content_linearLayout);
			contentRelativeLayout.removeAllViews();
			@SuppressWarnings("deprecation")
			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT);
			contentRelativeLayout.addView(view, p);
			contentRelativeLayout.invalidate();
			slidingMenu.showContent();
		}

		@Override
		public View getPage() {
			return view;
		}

		@Override
		public void finish() {
			slidingMenu.showMenu();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_BACK) {
			if (!slidingMenu.isMenuShowing()) {
				slidingMenu.showMenu();
				return true;
			}
		}
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - Constant.exitTime) > 2000) {
				Toast.makeText(MainFragmentActivity.this, "�ٰ�һ�λص�����", 0).show();
				Constant.exitTime = System.currentTimeMillis();
				return false;
			} else {
				// ������ ��̨����
				moveTaskToBack(false);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// �õ�path
	private void getBitmapPath() {
		SharedPreferences sp = this.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		String path = sp.getString(Constant.BACK_IMG, "");
		Bitmap bitmap = getBitmapByPath(path);
		if (bitmap != null) {
			mainLayout.setBackground(new BitmapDrawable(this.getResources(),
					bitmap));
		}
	}

	// ����path�õ�bitmap
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private Bitmap getBitmapByPath(String path) {
		AssetManager manager = this.getAssets();
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
	private void updateActionBar() {
		int colorIndex = SharedPrefUtil.getInt(MainFragmentActivity.this,
				Constant.COLOR_INDEX, -1);
		switch (colorIndex) {
		case 0:
			searchActionBar.setBackgroundResource(color.tianyilan);
			break;
		case 1:
			searchActionBar.setBackgroundResource(color.hidden_bitterness);
			break;
		case 2:
			searchActionBar.setBackgroundResource(color.gorgeous);
			break;
		case 3:
			searchActionBar.setBackgroundResource(color.romance);
			break;
		case 4:
			searchActionBar.setBackgroundResource(color.sunset);
			break;
		case 5:
			searchActionBar.setBackgroundResource(color.warm_colour);
			break;

		default:
			searchActionBar.setBackgroundResource(color.holo_blue_light);
			break;
		}
	}

	/**
	 * �б����������¼������ͳ�ʼ��
	 */
	public void onLoadEnd(XListView lv) {
		Date now = new Date();
		DateFormat d1 = DateFormat.getDateTimeInstance();
		lv.stopRefresh();
		lv.stopLoadMore();
		lv.setRefreshTime(d1.format(now));
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
			initPlatMode();
		}
	};

	protected void initPlatMode() {

		switch (MusicPlayService.getPlayMode()) {
		case MusicPlayService.PLAY_ORDER:
			randomLayout.setVisibility(View.VISIBLE);
			singleLayout.setVisibility(View.INVISIBLE);
			orderLayout.setVisibility(View.INVISIBLE);
			break;
		case MusicPlayService.PLAY_RANDOM:
			singleLayout.setVisibility(View.VISIBLE);
			randomLayout.setVisibility(View.INVISIBLE);
			orderLayout.setVisibility(View.INVISIBLE);
			break;
		case MusicPlayService.PLAY_SINGLE:
			orderLayout.setVisibility(View.INVISIBLE);
			singleLayout.setVisibility(View.VISIBLE);
			randomLayout.setVisibility(View.INVISIBLE);
			break;
		}

	}

	// �����ݿ��в�ѯ����ϲ���ĸ���
	public static ArrayList<LikeMusicModel> getFavMusicFromDB() {

		Cursor cursor = db.query("Music", null, null, null, null, null, null);// ��ѯ������α�

		if (likeMusciList != null) {
			likeMusciList.clear();
		}
		if (cursor.moveToFirst()) {
			do {
				LikeMusicModel likeMusicModel = new LikeMusicModel();
				String musicTitle = cursor.getString(cursor
						.getColumnIndex("MusicTitle"));
				String musicArtist = cursor.getString(cursor
						.getColumnIndex("MusicArtist"));
				Long musicTime = cursor.getLong(cursor
						.getColumnIndex("MusicTime"));
				String musicUrl = cursor.getString(cursor
						.getColumnIndex("MusicUrl"));
				// ��������ת����ͼƬ
				byte[] musicLocalImg = cursor.getBlob(cursor
						.getColumnIndex("MusicImg"));
				// ������ת����bitMap
				Bitmap bmp = MySQLite.getBmp(musicLocalImg);
				likeMusicModel.setMusicName(musicTitle);
				likeMusicModel.setMusicArtist(musicArtist);
				likeMusicModel.setMusicTime(musicTime);
				likeMusicModel.setMusicURL(musicUrl);
				likeMusicModel.setMusicLocalImg(musicLocalImg);
				likeMusicModel.setBitmap(bmp);
				likeMusciList.add(likeMusicModel);
			} while (cursor.moveToNext());
		}
		return likeMusciList;
	}

	private void setTranslucentStatus(int colorIndex) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// ͸��״̬��
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			// ͸��������
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			SystemStatusManager tintManager = new SystemStatusManager(this);
			tintManager.setStatusBarTintEnabled(true);
			getWindow().getDecorView().setFitsSystemWindows(true);
			if (colorIndex == 0) {
				tintManager.setStatusBarTintResource(color.tianyilan);
			} else if (colorIndex == 1) {
				tintManager.setStatusBarTintResource(color.hidden_bitterness);
			} else if (colorIndex == 2) {
				tintManager.setStatusBarTintResource(color.gorgeous);
			} else if (colorIndex == 3) {
				tintManager.setStatusBarTintResource(color.romance);
			} else if (colorIndex == 4) {
				tintManager.setStatusBarTintResource(color.sunset);
			} else if (colorIndex == 5) {
				tintManager.setStatusBarTintResource(color.warm_colour);
			} else {
				tintManager.setStatusBarTintResource(color.holo_blue_light);
			}

			
		}
	}

}
