package com.esint.music.activity;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.R.color;
import com.esint.music.XListView.XListView;
import com.esint.music.XListView.XListView.IXListViewListener;
import com.esint.music.adapter.SearchResultAdapter;
import com.esint.music.dialog.Effectstype;
import com.esint.music.dialog.NiftyDialogBuilder;
import com.esint.music.fragment.MyTabMusic;
import com.esint.music.fragment.NetMusicFragment;
import com.esint.music.model.DownImageInfo;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.model.Mp3Info;
import com.esint.music.model.SearchMusicInfo;
import com.esint.music.model.SearchResult;
import com.esint.music.slidemenu.SlidingMenu;
import com.esint.music.sortlistview.CharacterParser;
import com.esint.music.sortlistview.PinyinComparator;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.MusicAPI;
import com.esint.music.utils.MyApplication;
import com.esint.music.utils.MyHttpUtils;
import com.esint.music.utils.PageAction;
import com.esint.music.utils.SearchMusicUtil;
import com.esint.music.utils.SortListUtil;
import com.esint.music.utils.SearchMusicUtil.onSearchResultListener;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.view.AlwaysMarqueeTextView;
import com.esint.music.view.MainFunctionPop;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.http.RequestParams;

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
	private List<Mp3Info> myLikeMp3Infos;// �ҵ����list
	private MyApplication myApp;
	private ArrayList<DownMucicInfo> downMusicList;// ���صĸ���
	private MyHttpUtils myHttpUtils;// ��������Ĺ�����

	// ע�� ��������ѡ���� ��Ҫ���� android:clickable="true" �ɿɵ����״̬

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main_music);
		bindService();// �󶨷���
		initView();
		initSlidingMenu();
		initData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// ������
		unBindService();
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
		musciFlag = SharedPrefUtil.getString(this, Constant.MUSIC_FLAG, "");

		if (currentPlayPosition != -1 && musciFlag.equals("local_music")) {
			musicName.setText(mp3Infos.get(currentPlayPosition).getTitle());
			musicSinger.setText(mp3Infos.get(currentPlayPosition).getArtist());
			Bitmap bitmap = MediaUtils.getArtwork(this,
					mp3Infos.get(currentPlayPosition).getId(),
					mp3Infos.get(currentPlayPosition).getAlbumId(), true, true);
			albumImg.setImageBitmap(bitmap);
		}
		if (currentPlayPosition != -1 && musciFlag.equals("like_music")) {
			musicName.setText(myLikeMp3Infos.get(currentPlayPosition)
					.getTitle());
			musicSinger.setText(myLikeMp3Infos.get(currentPlayPosition)
					.getArtist());
			Bitmap bitmap = MediaUtils.getArtwork(this,
					myLikeMp3Infos.get(currentPlayPosition).getId(),
					myLikeMp3Infos.get(currentPlayPosition).getAlbumId(), true,
					true);
			albumImg.setImageBitmap(bitmap);
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
	}

	private void initData() {
		mFragLists = new ArrayList<Fragment>();
		myApp = (MyApplication) getApplication();
		mp3Infos = MediaUtils.getMp3Info(MainFragmentActivity.this);
		myHttpUtils = new MyHttpUtils(MainFragmentActivity.this);
		try {
			// ����MP3�б������
			mp3Infos = MediaUtils.getMp3Info(this);
			mp3Infos = new SortListUtil().initMyLocalMusic(mp3Infos);
			myLikeMp3Infos = myApp.dbUtils.findAll(Mp3Info.class);
			myLikeMp3Infos = new SortListUtil().initMyLocalMusic(mp3Infos);
		} catch (DbException e) {
			e.printStackTrace();
		}

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
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			Toast.makeText(this, "����˰�ť", 0).show();
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
			Toast.makeText(MainFragmentActivity.this, "����˹��ܼ�", 0).show();

			break;
		case R.id.ib_menu:
			initPopuWindow();
			break;
		case R.id.btn_search:
			searchMusicView();
			break;
		case R.id.musiccontent: {
			// �������ظ����ĸ����͸���
			Intent intent = new Intent(MainFragmentActivity.this,
					MusicPlayAvtivity.class);
			intent.putExtra("Music_name", musicName.getText().toString());
			intent.putExtra("Music_artist", musicSinger.getText().toString());
			startActivity(intent);
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

			break;
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
				updateSearchMusic(0);
			}

			@Override
			public void onLoadMore() {
				isRefresh = false;
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
				String searchResult = searchEt.getText().toString();
				if (TextUtils.isEmpty(searchResult)) {
					showEmptyDialog();
				} else {
					progressDialog.setMessage("�������������Ժ󡣡���");
					progressDialog.show();
					searchMusicList.clear();
					updateSearchMusic(0);
					Toast.makeText(MainFragmentActivity.this, "���������", 0)
							.show();
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
				break;
			case R.id.menuPlayMode:
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
				.withMessageColor("#FFFFFF")
				.withIcon(getResources().getDrawable(R.drawable.icon))
				.isCancelableOnTouchOutside(true).withEffect(effect)
				.withButton1Text("ȷ��").withButton2Text("ȡ��")
				.setCustomView(R.layout.custom_view, MainFragmentActivity.this)
				.setButton1Click(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MainFragmentActivity.this.finish();
						popMenuMain.dismiss();

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
				Toast.makeText(MainFragmentActivity.this, "�ٰ�һ�η��ؼ��ص�����", 0)
						.show();
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
}
