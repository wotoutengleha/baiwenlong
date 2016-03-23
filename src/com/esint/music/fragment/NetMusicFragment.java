package com.esint.music.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.activity.MainFragmentActivity;
import com.esint.music.adapter.HotMusicListAdapter;
import com.esint.music.adapter.NewMusicListAdapter;
import com.esint.music.adapter.OriginalMusicListAdapter;
import com.esint.music.adapter.RiseMusicListAdapter;
import com.esint.music.model.HotMusicInfo;
import com.esint.music.model.NewMusicInfo;
import com.esint.music.model.OriginalMusicInfo;
import com.esint.music.model.RiseMusicInfo;
import com.esint.music.service.MusicPlayService;
import com.esint.music.service.NetMusicPlayer;
import com.esint.music.service.MusicPlayService.PlayBinder;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MyHttpUtils;
import com.esint.music.utils.PageAction;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.utils.TimeFromat;
import com.esint.music.view.AlwaysMarqueeTextView;
import com.lidroid.xutils.BitmapUtils;

@SuppressLint("ValidFragment")
public class NetMusicFragment extends Fragment implements OnClickListener {

	private LinearLayout llNewMusic;
	private LinearLayout llHotMusuc;
	private LinearLayout llOriginalMusic;
	private LinearLayout llRiseMusic;
	private PageAction action;
	private ArrayList<NewMusicInfo> netNewMusicList = new ArrayList<NewMusicInfo>();
	private ArrayList<HotMusicInfo> netHotMusicList = new ArrayList<HotMusicInfo>();
	private ArrayList<OriginalMusicInfo> netOriginalMusicList = new ArrayList<OriginalMusicInfo>();
	private ArrayList<RiseMusicInfo> netRiseMusicList = new ArrayList<RiseMusicInfo>();
	private MyHttpUtils myHttpUtils;
	private MainFragmentActivity mainFragmentActivity;

	private ImageView albumImg;// 专辑图片
	private AlwaysMarqueeTextView singer;// 歌手
	private AlwaysMarqueeTextView songName;// 歌曲名字
	private Handler handler;
	private BitmapUtils bitmapUtils;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mainFragmentActivity = (MainFragmentActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_net_music, null);
		mainFragmentActivity.bindService();
		initView(view);
		initData();
		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mainFragmentActivity.unBindService();
	}

	public NetMusicFragment(PageAction action) {
		this.action = action;
	}

	private void initView(View view) {
		llNewMusic = (LinearLayout) view.findViewById(R.id.ll_newmusic);
		llHotMusuc = (LinearLayout) view.findViewById(R.id.ll_hotmusic);
		llOriginalMusic = (LinearLayout) view.findViewById(R.id.original_ll);
		llRiseMusic = (LinearLayout) view.findViewById(R.id.rise_ll);
		llNewMusic.setOnClickListener(this);
		llHotMusuc.setOnClickListener(this);
		llOriginalMusic.setOnClickListener(this);
		llRiseMusic.setOnClickListener(this);
		albumImg = (ImageView) mainFragmentActivity.findViewById(R.id.iv_album);
		singer = (AlwaysMarqueeTextView) mainFragmentActivity
				.findViewById(R.id.tv_musicName);
		songName = (AlwaysMarqueeTextView) mainFragmentActivity
				.findViewById(R.id.tv_musicArt);
	}

	private void initData() {
		myHttpUtils = new MyHttpUtils(getActivity());
		bitmapUtils = new BitmapUtils(mainFragmentActivity);

		handler = new Handler() {
			@SuppressLint("NewApi")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == Constant.WHAT_NEW_SONGINFO) {
					int currnetPoi = (Integer) msg.obj;
					singer.setText(netNewMusicList.get(currnetPoi)
							.getArtistsName());
					songName.setText(netNewMusicList.get(currnetPoi).getName());
					bitmapUtils.display(albumImg,
							netNewMusicList.get(currnetPoi).getPicUrl());
					startAnim();
				} else if (msg.what == Constant.WHAT_HOT_SONGINFO) {
					int currnetPoi = (Integer) msg.obj;
					singer.setText(netHotMusicList.get(currnetPoi)
							.getArtistsName());
					songName.setText(netHotMusicList.get(currnetPoi).getName());
					bitmapUtils.display(albumImg,
							netHotMusicList.get(currnetPoi).getPicUrl());
				} else if (msg.what == Constant.WHAT_ORIGINAL_SONGINFO) {
					int currnetPoi = (Integer) msg.obj;
					singer.setText(netOriginalMusicList.get(currnetPoi)
							.getArtistsName());
					songName.setText(netOriginalMusicList.get(currnetPoi)
							.getName());
					bitmapUtils.display(albumImg,
							netOriginalMusicList.get(currnetPoi).getPicUrl());
				} else if (msg.what == Constant.WHAT_RISE_SONGINFO) {
					int currnetPoi = (Integer) msg.obj;
					singer.setText(netRiseMusicList.get(currnetPoi)
							.getArtistsName());
					songName.setText(netRiseMusicList.get(currnetPoi).getName());
					bitmapUtils.display(albumImg,
							netRiseMusicList.get(currnetPoi).getPicUrl());
				}
			}
		};
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_newmusic: {
			newMusicView();
			break;
		}
		case R.id.ll_hotmusic: {
			hotMusic();
			break;
		}
		case R.id.original_ll: {
			originalMusic();
			break;
		}
		case R.id.rise_ll: {
			riseMusic();
			break;
		}
		}
	}

	/**
	 * @Description:加载网络新歌榜
	 * @return void
	 * @author bai
	 */
	private void newMusicView() {

		String updateTime = SharedPrefUtil.getString(mainFragmentActivity,
				"hotMusicUpdateTime", "");
		String description = SharedPrefUtil.getString(mainFragmentActivity,
				"hotMusicDescription", "");
		String trackCount = SharedPrefUtil.getString(mainFragmentActivity,
				"hotMusicTrackCount", "");

		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View netNewMusicView = inflater.inflate(
				R.layout.activity_netnewmusiclist, null, false);
		final ListView lv_netnewmusic = (ListView) netNewMusicView
				.findViewById(R.id.lv_netnewmusic1);
		ImageView btnBack = (ImageView) netNewMusicView
				.findViewById(R.id.net_button_back);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mainFragmentActivity.slidingMenu.isMenuShowing()) {
					mainFragmentActivity.slidingMenu.showMenu();
				}
			}
		});
		View headerView = View.inflate(getActivity(),
				R.layout.headerview_netnewmusiclist, null);
		ImageButton ibFunction = (ImageButton) headerView
				.findViewById(R.id.ib_netnewmusic);
		TextView updateTv = (TextView) headerView
				.findViewById(R.id.tv_updatetime);
		TextView songcountTv = (TextView) headerView
				.findViewById(R.id.songcountTv);
		TextView decriptionTv = (TextView) headerView
				.findViewById(R.id.decription_tv);
		updateTv.setText("最近更新: " + TimeFromat.timeFormat(updateTime));
		songcountTv.setText("(" + "共" + trackCount + "首" + ")");
		decriptionTv.setText("      " + description);
		ibFunction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(mainFragmentActivity, "点击了功能键", 0).show();
			}
		});
		lv_netnewmusic.addHeaderView(headerView);

		myHttpUtils.netNewMusicList(Constant.API_NET_NEWMUSIC_LIST);

		// 通过handler传递过来的集合
		MyHttpUtils.handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what) {
				case Constant.WHAT_NET_NEWMUSIC_LIST:
					netNewMusicList.clear();
					netNewMusicList.addAll((ArrayList<NewMusicInfo>) msg.obj);

					lv_netnewmusic.setAdapter(new NewMusicListAdapter(
							getActivity(), netNewMusicList));

					break;

				}
			}
		};
		lv_netnewmusic.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				final int currnetPoi = position - 1;
				SharedPrefUtil.setString(mainFragmentActivity,
						Constant.MUSIC_FLAG, "NET_MUSIC");

				boolean isConnected = myHttpUtils
						.isConnnected(mainFragmentActivity);
				if (isConnected == false) {
					Toast.makeText(mainFragmentActivity, "当前网络没有连接", 0).show();
					return;
				}
				// 判断网络类型
				int networkType = myHttpUtils.getNetworkType();
				Log.e("当前的网络是", networkType + "");

				// mainFragmentActivity.musicPlayService.pause();

				switch (networkType) {
				case 0:// 流量网络
					showNetAlert(currnetPoi);
					break;
				case 1:// Wi-Fi网络 //直接播放
						// 使用handler更新UI
					Message songInfo = handler.obtainMessage(
							Constant.WHAT_NEW_SONGINFO, currnetPoi);
					songInfo.sendToTarget();
					mainFragmentActivity.musicPlayService
							.playNetMusic(netNewMusicList.get(currnetPoi)
									.getMp3Url());
					// 发送广播更新按钮
					Intent intent = new Intent(Constant.PLAYBUTTON_BROAD);
					getActivity().sendBroadcast(intent);
					break;
				}

			}
		});
		lv_netnewmusic.setAdapter(new NewMusicListAdapter(getActivity(),
				netNewMusicList));
		action.addPage(netNewMusicView);
	}

	/**
	 * @Description:网络热歌
	 * @return void
	 * @author bai
	 */
	private void hotMusic() {

		// 取值
		String hotmusic_updateTime = SharedPrefUtil.getString(
				mainFragmentActivity, "hotmusic_updateTime", "");
		String hotmusic_description = SharedPrefUtil.getString(
				mainFragmentActivity, "hotmusic_description", "");
		String hotmusic_trackCount = SharedPrefUtil.getString(
				mainFragmentActivity, "hotmusic_trackCount", "");

		// 在没有调用setAdapter之前加载不上headerView
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View hotMusicView = inflater.inflate(R.layout.activity_nethotmusiclist,
				null, false);
		View headerView = inflater.inflate(R.layout.headerview_nethotmusiclist,
				null, false);
		TextView updateTime = (TextView) headerView
				.findViewById(R.id.hot_updatetv);
		TextView songCount = (TextView) headerView
				.findViewById(R.id.hotsongConut);
		TextView description = (TextView) headerView
				.findViewById(R.id.hot_decription_tv);
		updateTime.setText("最近更新: "
				+ TimeFromat.timeFormat(hotmusic_updateTime));
		songCount.setText("(" + "共" + hotmusic_trackCount + "首" + ")");
		description.setText("      " + hotmusic_description);
		final ListView lv_hotMusic = (ListView) hotMusicView
				.findViewById(R.id.lv_nethotmusic1);
		ImageView iv_hotBack = (ImageView) hotMusicView
				.findViewById(R.id.hot_bunback);
		ImageView iv_hotMore = (ImageView) hotMusicView
				.findViewById(R.id.hot_iv_functions);
		lv_hotMusic.addHeaderView(headerView);
		iv_hotBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mainFragmentActivity.slidingMenu.isMenuShowing()) {
					mainFragmentActivity.slidingMenu.showMenu();
				}
			}
		});

		myHttpUtils.netHotMusicList(Constant.API_NET_HOTMUSIC_LIST);
		// 通过handler传递过来的集合
		MyHttpUtils.handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what) {
				case Constant.WHAT_NET_NEWMUSIC_LIST:
					netHotMusicList.clear();
					netHotMusicList.addAll((ArrayList<HotMusicInfo>) msg.obj);
					lv_hotMusic.setAdapter(new HotMusicListAdapter(
							getActivity(), netHotMusicList));

					break;

				}
			}
		};
		lv_hotMusic.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				final int currnetPoi = position - 1;

				SharedPrefUtil.setString(mainFragmentActivity,
						Constant.MUSIC_FLAG, "NET_MUSIC");

				boolean isConnected = myHttpUtils
						.isConnnected(mainFragmentActivity);
				if (isConnected == false) {
					Toast.makeText(mainFragmentActivity, "当前网络没有连接", 0).show();
					return;
				}
				// 判断网络类型
				int networkType = myHttpUtils.getNetworkType();
				Log.e("当前的网络是", networkType + "");
				// mainFragmentActivity.musicPlayService.pause();
				switch (networkType) {
				case 0:// 流量网络
					showNetAlert(currnetPoi);
					break;
				case 1:// Wi-Fi网络 //直接播放
						// 使用handler更新UI
					Message songInfo = handler.obtainMessage(
							Constant.WHAT_HOT_SONGINFO, currnetPoi);
					songInfo.sendToTarget();
					mainFragmentActivity.musicPlayService
							.playNetMusic(netHotMusicList.get(currnetPoi)
									.getMp3Url());
					// 发送广播更新按钮
					Intent intent = new Intent(Constant.PLAYBUTTON_BROAD);
					getActivity().sendBroadcast(intent);
					break;

				}
			}
		});
		lv_hotMusic.setAdapter(new HotMusicListAdapter(getActivity(),
				netHotMusicList));

		action.addPage(hotMusicView);
	}

	/**
	 * @Description:网络热歌
	 * @return void
	 * @author bai
	 */
	private void originalMusic() {

		// 取值
		String hotmusic_updateTime = SharedPrefUtil.getString(
				mainFragmentActivity, "originalmusic_updateTime", "");
		String hotmusic_description = SharedPrefUtil.getString(
				mainFragmentActivity, "originalmusic_description", "");
		String hotmusic_trackCount = SharedPrefUtil.getString(
				mainFragmentActivity, "originalmusic_trackCount", "");

		// 在没有调用setAdapter之前加载不上headerView
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View originalMusicView = inflater.inflate(
				R.layout.activity_originalmusiclist, null, false);
		View headerView = inflater.inflate(
				R.layout.headerview_originalmusiclist, null, false);
		TextView updateTime = (TextView) headerView
				.findViewById(R.id.original_updatetv);
		TextView songCount = (TextView) headerView
				.findViewById(R.id.originalsongConut);
		TextView description = (TextView) headerView
				.findViewById(R.id.original_decription_tv);
		updateTime.setText("最近更新: "
				+ TimeFromat.timeFormat(hotmusic_updateTime));
		songCount.setText("(" + "共" + hotmusic_trackCount + "首" + ")");
		description.setText("      " + hotmusic_description);
		final ListView lv_originalMusic = (ListView) originalMusicView
				.findViewById(R.id.lv_originalmusic1);
		ImageView iv_originalBack = (ImageView) originalMusicView
				.findViewById(R.id.original_bunback);
		// ImageView iv_hotMore = (ImageView) hotMusicView
		// .findViewById(R.id.hot_iv_functions);
		lv_originalMusic.addHeaderView(headerView);
		iv_originalBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mainFragmentActivity.slidingMenu.isMenuShowing()) {
					mainFragmentActivity.slidingMenu.showMenu();
				}
			}
		});

		myHttpUtils.originalMusicList(Constant.API_NET_ORIGINALMUSIC_LIST);
		// 通过handler传递过来的集合
		MyHttpUtils.handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what) {
				case Constant.WHAT_NET_ORIGINALMUSIC_LIST:
					netOriginalMusicList.clear();
					netOriginalMusicList
							.addAll((ArrayList<OriginalMusicInfo>) msg.obj);

					Log.e("1111111111111111", netOriginalMusicList.size() + "");

					lv_originalMusic.setAdapter(new OriginalMusicListAdapter(
							getActivity(), netOriginalMusicList));

					break;

				}
			}
		};
		lv_originalMusic.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				final int currnetPoi = position - 1;

				SharedPrefUtil.setString(mainFragmentActivity,
						Constant.MUSIC_FLAG, "NET_MUSIC");

				boolean isConnected = myHttpUtils
						.isConnnected(mainFragmentActivity);
				if (isConnected == false) {
					Toast.makeText(mainFragmentActivity, "当前网络没有连接", 0).show();
					return;
				}
				// 判断网络类型
				int networkType = myHttpUtils.getNetworkType();
				Log.e("当前的网络是", networkType + "");
				// mainFragmentActivity.musicPlayService.pause();
				switch (networkType) {
				case 0:// 流量网络
					showNetAlert(currnetPoi);
					break;
				case 1:// Wi-Fi网络 //直接播放
						// 使用handler更新UI
					Message songInfo = handler.obtainMessage(
							Constant.WHAT_ORIGINAL_SONGINFO, currnetPoi);
					songInfo.sendToTarget();
					mainFragmentActivity.musicPlayService
							.playNetMusic(netOriginalMusicList.get(currnetPoi)
									.getMp3Url());
					// 发送广播更新按钮
					Intent intent = new Intent(Constant.PLAYBUTTON_BROAD);
					getActivity().sendBroadcast(intent);
					break;

				}
			}
		});
		lv_originalMusic.setAdapter(new OriginalMusicListAdapter(getActivity(),
				netOriginalMusicList));

		action.addPage(originalMusicView);
	}

	/**
	 * @Description:飙升榜
	 * @return void
	 * @author bai
	 */
	private void riseMusic() {

		// 取值
		String hotmusic_updateTime = SharedPrefUtil.getString(
				mainFragmentActivity, "risemusic_updateTime", "");
		String hotmusic_description = SharedPrefUtil.getString(
				mainFragmentActivity, "risemusic_description", "");
		String hotmusic_trackCount = SharedPrefUtil.getString(
				mainFragmentActivity, "risemusic_trackCount", "");

		// 在没有调用setAdapter之前加载不上headerView
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View riseMusicView = inflater.inflate(R.layout.activity_risemusiclist,
				null, false);
		View headerView = inflater.inflate(R.layout.headerview_risemusiclist,
				null, false);
		TextView updateTime = (TextView) headerView
				.findViewById(R.id.rise_updatetv);
		TextView songCount = (TextView) headerView
				.findViewById(R.id.risesongConut);
		TextView description = (TextView) headerView
				.findViewById(R.id.rise_decription_tv);
		updateTime.setText("最近更新: "
				+ TimeFromat.timeFormat(hotmusic_updateTime));
		songCount.setText("(" + "共" + hotmusic_trackCount + "首" + ")");
		description.setText("      " + hotmusic_description);
		final ListView lv_riseMusic = (ListView) riseMusicView
				.findViewById(R.id.lv_risemusic1);
		ImageView iv_riseBack = (ImageView) riseMusicView
				.findViewById(R.id.rise_bunback);
		// ImageView iv_hotMore = (ImageView) hotMusicView
		// .findViewById(R.id.hot_iv_functions);
		lv_riseMusic.addHeaderView(headerView);
		iv_riseBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mainFragmentActivity.slidingMenu.isMenuShowing()) {
					mainFragmentActivity.slidingMenu.showMenu();
				}
			}
		});

		myHttpUtils.riseMusicList(Constant.API_NET_RISEMUSIC_LIST);
		// 通过handler传递过来的集合
		MyHttpUtils.handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what) {
				case Constant.WHAT_NET_RISEMUSIC_LIST:
					netRiseMusicList.clear();
					netRiseMusicList.addAll((ArrayList<RiseMusicInfo>) msg.obj);

					lv_riseMusic.setAdapter(new RiseMusicListAdapter(
							getActivity(), netRiseMusicList));

					break;

				}
			}
		};
		lv_riseMusic.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				final int currnetPoi = position - 1;

				SharedPrefUtil.setString(mainFragmentActivity,
						Constant.MUSIC_FLAG, "NET_MUSIC");

				boolean isConnected = myHttpUtils
						.isConnnected(mainFragmentActivity);
				if (isConnected == false) {
					Toast.makeText(mainFragmentActivity, "当前网络没有连接", 0).show();
					return;
				}
				// 判断网络类型
				int networkType = myHttpUtils.getNetworkType();
				Log.e("当前的网络是", networkType + "");
				// mainFragmentActivity.musicPlayService.pause();
				switch (networkType) {
				case 0:// 流量网络
					showNetAlert(currnetPoi);
					break;
				case 1:// Wi-Fi网络 //直接播放
						// 使用handler更新UI
					Message songInfo = handler.obtainMessage(
							Constant.WHAT_RISE_SONGINFO, currnetPoi);
					songInfo.sendToTarget();
					mainFragmentActivity.musicPlayService
							.playNetMusic(netRiseMusicList.get(currnetPoi)
									.getMp3Url());
					// 发送广播更新按钮
					Intent intent = new Intent(Constant.PLAYBUTTON_BROAD);
					getActivity().sendBroadcast(intent);
					break;

				}
			}
		});
		lv_riseMusic.setAdapter(new RiseMusicListAdapter(getActivity(),
				netRiseMusicList));

		action.addPage(riseMusicView);
	}

	// 开始旋转专辑图片的动画
	private void startAnim() {
		Animation operatingAnim = AnimationUtils.loadAnimation(getActivity(),
				R.anim.tip_play);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		if (operatingAnim != null) {
			albumImg.startAnimation(operatingAnim);
		}
	}

	/**
	* @Description:判断网络类型 ，在流量状态下提示 
	* @param position musicFlag //表是传递的是新音乐榜 还是热歌榜 飙升榜  原创榜
	* @return void 
	* @author bai
	*/
	private void showNetAlert(final int position) {

		AlertDialog.Builder builder = new AlertDialog.Builder(
				mainFragmentActivity);
		final AlertDialog dialog = builder.create();
		View alertDialogView = View.inflate(mainFragmentActivity,
				R.layout.dialog_play, null);
		TextView okTv = (TextView) alertDialogView
				.findViewById(R.id.tvplay_countinue);
		TextView cancelTv = (TextView) alertDialogView
				.findViewById(R.id.tvplay_stop);
		dialog.show();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		WindowManager m = ((Activity) mainFragmentActivity).getWindowManager();
		Display d = m.getDefaultDisplay();
		params.height = (int) (d.getHeight() * 0.286);
		params.width = (int) (d.getWidth() * 0.8);
		dialog.getWindow().setAttributes(params);
		dialog.setContentView(alertDialogView);
		okTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		cancelTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

	}

}
