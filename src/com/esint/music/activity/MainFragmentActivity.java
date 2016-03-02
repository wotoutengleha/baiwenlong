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
	private int mScreenWidth;// 每一个上导航的宽
	private int mCurrentPageIndex;// 当前在哪一项
	private List<Fragment> mFragLists;
	private TextView myMusicTv, netMusicTv;
	private ImageButton menuIb;
	private ImageView main_more_functions;
	private MainFunctionPop popMenuMain;
	private Effectstype effect;
	private NiftyDialogBuilder dialogBuilder;
	private LinearLayout mainLayout;// 主页面的背景
	private LinearLayout bottomLayout;// 底部播放按钮的布局
	private RelativeLayout mainActionBar;

	private View searchView;
	private MenuAction action;
	private LinearLayout contentRelativeLayout;
	public SlidingMenu slidingMenu;
	private ImageView albumImg;// 专辑图片
	private ImageView searchButton;// 搜索按钮
	private AlwaysMarqueeTextView musicName;
	private AlwaysMarqueeTextView musicSinger;
	private int currentPlayPosition;// 得到记录的位置
	private String musciFlag;// 是本地音乐还是我的最爱

	// 汉字转换成拼音的类
	private ArrayList<Mp3Info> mp3Infos;// 本地音乐
	private List<Mp3Info> myLikeMp3Infos;// 我的最爱的list
	private MyApplication myApp;
	private ArrayList<DownMucicInfo> downMusicList;// 下载的歌曲
	private MyHttpUtils myHttpUtils;// 请求网络的工具类

	// 注意 布局设置选择器 需要设置 android:clickable="true" 成可点击的状态

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main_music);
		bindService();// 绑定服务
		initView();
		initSlidingMenu();
		initData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 解绑服务
		unBindService();
	}

	// 换背景
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
						+ "/" + "/下载的图片" + "/";
				// 下载歌曲的文件夹
				String MusicTarget = Environment.getExternalStorageDirectory()
						+ "/" + "/下载的歌曲";
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
		// 初始化tabLine
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		mScreenWidth = metrics.widthPixels / 2;
		LayoutParams lp = mTabline.getLayoutParams();
		lp.width = mScreenWidth;
		mTabline.setLayoutParams(lp);
		// 底部播放栏的控件
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
			// 排序MP3列表的数据
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

		// 下载歌曲的文件夹
		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的歌曲";
		downMusicList = MediaUtils.GetMusicFiles(MusicTarget, ".mp3", true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			Toast.makeText(this, "点击了按钮", 0).show();
			initSearch();
			break;

		}
		return true;
	}

	// 搜索栏
	private void initSearch() {
		LayoutInflater inflater = getLayoutInflater();
		searchView = inflater
				.inflate(R.layout.activity_localmusic, null, false);
		action.addPage(searchView);

	}

	private void initSlidingMenu() {
		slidingMenu = (SlidingMenu) findViewById(R.id.slidingMenu);
		// 设置是左滑还是右滑，还是左右都可以滑
		slidingMenu.setMode(SlidingMenu.LEFT);
		// 设置要使菜单滑动，触碰屏幕的范围
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setContent(R.layout.sliding_content);
		// 设置滑动时拖拽效果
		slidingMenu.setBehindScrollScale(0);
		// 设置滑动时菜单的是否淡入淡出
		slidingMenu.setFadeEnabled(true);
		// 设置淡入淡出的比例
		slidingMenu.setFadeDegree(1f);
		slidingMenu.showMenu();
		action = new MenuAction();
	}

	// 点击事件
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
			Toast.makeText(MainFragmentActivity.this, "点击了功能键", 0).show();

			break;
		case R.id.ib_menu:
			initPopuWindow();
			break;
		case R.id.btn_search:
			searchMusicView();
			break;
		case R.id.musiccontent: {
			// 传递下载歌曲的歌名和歌手
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
	private EditText searchEt;// 搜索音乐输入的关键字

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
					progressDialog.setMessage("正在搜索，请稍后。。。");
					progressDialog.show();
					searchMusicList.clear();
					updateSearchMusic(0);
					Toast.makeText(MainFragmentActivity.this, "点击了搜索", 0)
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
					Toast.makeText(MainFragmentActivity.this, "没有更多数据了", 0)
							.show();
					resultLV.stopLoadMore();
				}
			}
		};

		action.addPage(searchView);
	}

	// 更新搜索音乐的列表
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

	// 关键字为空的时候弹出来的dialog
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
	 * @Description:底部的popuwindow
	 * @return void
	 * @author bai
	 */
	private void initPopuWindow() {
		// 初始化PopupWindow
		popMenuMain = new MainFunctionPop(MainFragmentActivity.this,
				itemsOnClick);
		// 设置layout在PopupWindow中显示的位置
		popMenuMain.showAtLocation(
				MainFragmentActivity.this.findViewById(R.id.mainpopu),
				Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		if (popMenuMain != null) {
			popMenuMain.setOnDismissListener(new poponDismissListener());
			backgroundAlpha(0.5f);
		}
	}

	// 为弹出窗口实现监听类
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

	// 退出程序的dialog

	private RelativeLayout searchActionBar;// 搜索布局的状态栏

	private void exitDialog() {
		effect = Effectstype.Shake;
		dialogBuilder = new NiftyDialogBuilder(MainFragmentActivity.this,
				R.style.dialog_untran);
		dialogBuilder.withTitle("提示").withTitleColor("#FFFFFF")
				.withDividerColor("#97E8F9").withMessage("确定要退出程序吗？")
				.withMessageColor("#FFFFFF")
				.withIcon(getResources().getDrawable(R.drawable.icon))
				.isCancelableOnTouchOutside(true).withEffect(effect)
				.withButton1Text("确定").withButton2Text("取消")
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
	 * 添加新笔记时弹出的popWin关闭的事件，主要是为了将背景透明度改回来
	 */
	class poponDismissListener implements PopupWindow.OnDismissListener {
		@Override
		public void onDismiss() {
			backgroundAlpha(1f);
		}
	}

	/**
	 * 设置添加屏幕的背景透明度
	 */
	public void backgroundAlpha(float bgAlpha) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = bgAlpha; // 0.0-1.0
		getWindow().setAttributes(lp);
	}

	// viewPager的适配器
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

	// ViewPager的滑动监听事件
	class MyPagerListener implements OnPageChangeListener {
		// 滑动状态改变的时候调用
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		// 当前页面滑动的时候调用
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

		// 当前页面被选中的时候调用
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

	// 设置textView的颜色
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
				Toast.makeText(MainFragmentActivity.this, "再按一次返回键回到桌面", 0)
						.show();
				Constant.exitTime = System.currentTimeMillis();
				return false;
			} else {
				// 不销毁 后台运行
				moveTaskToBack(false);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// 拿到path
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

	// 根据path得到bitmap
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

	// 更新状态栏
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
	 * 列表上拉下拉事件结束和初始化
	 */
	public void onLoadEnd(XListView lv) {
		Date now = new Date();
		DateFormat d1 = DateFormat.getDateTimeInstance();
		lv.stopRefresh();
		lv.stopLoadMore();
		lv.setRefreshTime(d1.format(now));
	}
}
