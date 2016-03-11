package com.esint.music.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.activity.MainFragmentActivity;
import com.esint.music.adapter.HotMusicListAdapter;
import com.esint.music.adapter.NewMusicListAdapter;
import com.esint.music.model.HotMusicInfo;
import com.esint.music.model.NewMusicInfo;
import com.esint.music.service.NetMusicPlayer;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MyHttpUtils;
import com.esint.music.utils.PageAction;
import com.esint.music.view.AlwaysMarqueeTextView;
import com.lidroid.xutils.BitmapUtils;

@SuppressLint("ValidFragment")
public class NetMusicFragment extends Fragment implements OnClickListener {

	private LinearLayout llNewMusic;
	private LinearLayout llHotMusuc;
	private PageAction action;
	private ArrayList<NewMusicInfo> netNewMusicList = new ArrayList<NewMusicInfo>();
	private ArrayList<HotMusicInfo> netHotMusicList = new ArrayList<HotMusicInfo>();
	private MyHttpUtils myHttpUtils;
	private MainFragmentActivity mainFragmentActivity;

	private ImageView albumImg;// 专辑图片
	private AlwaysMarqueeTextView singer;// 歌手
	private AlwaysMarqueeTextView songName;// 歌曲名字
	private ImageButton playButton;// 播放按钮
	private ImageButton pauseButton;// 暂停按钮
	private ImageButton nextButton;// 下一首
	private ProgressBar progressBar;
	private NetMusicPlayer player;
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
		llNewMusic.setOnClickListener(this);
		llHotMusuc.setOnClickListener(this);
		albumImg = (ImageView) mainFragmentActivity.findViewById(R.id.iv_album);
		singer = (AlwaysMarqueeTextView) mainFragmentActivity
				.findViewById(R.id.tv_musicName);
		songName = (AlwaysMarqueeTextView) mainFragmentActivity
				.findViewById(R.id.tv_musicArt);

		// playButton = (ImageButton) mainFragmentActivity
		// .findViewById(R.id.ib_play);
		// pauseButton = (ImageButton) mainFragmentActivity
		// .findViewById(R.id.ib_pause);
		// nextButton = (ImageButton) mainFragmentActivity
		// .findViewById(R.id.ib_next);
		progressBar = (ProgressBar) mainFragmentActivity
				.findViewById(R.id.playback_seekbar);
		// playButton.setOnClickListener(this);
		// pauseButton.setOnClickListener(this);
		// nextButton.setOnClickListener(this);
	}

	private void initData() {
		myHttpUtils = new MyHttpUtils(getActivity());
		bitmapUtils = new BitmapUtils(mainFragmentActivity);
		player = new NetMusicPlayer(progressBar);
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
				}
				if (msg.what == Constant.WHAT_HOT_SONGINFO) {
					int currnetPoi = (Integer) msg.obj;
					singer.setText(netHotMusicList.get(currnetPoi)
							.getArtistsName());
					songName.setText(netHotMusicList.get(currnetPoi).getName());
					bitmapUtils.display(albumImg,
							netHotMusicList.get(currnetPoi).getPicUrl());
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
		case R.id.ll_hotmusic:
			hotMusic();
			break;

		}
	}

	/**
	 * @Description:加载网络新歌榜
	 * @return void
	 * @author bai
	 */
	private void newMusicView() {
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
		ibFunction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(mainFragmentActivity, "点击了功能键", 0).show();

				player.pause();
				player.stop();
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
				progressBar = null;

				mainFragmentActivity.musicPlayService.pause();
				new Thread(new Runnable() {
					@Override
					public void run() {
						player.playUrl(netNewMusicList.get(currnetPoi)
								.getMp3Url());
						// 使用handler更新UI
						Message songInfo = handler.obtainMessage(
								Constant.WHAT_NEW_SONGINFO, currnetPoi);
						songInfo.sendToTarget();
					}
				}).start();
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
		// 在没有调用setAdapter之前加载不上headerView
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View hotMusicView = inflater.inflate(R.layout.activity_nethotmusiclist,
				null, false);
		View headerView = inflater.inflate(R.layout.headerview_nethotmusiclist,
				null, false);
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
				progressBar = null;

				mainFragmentActivity.musicPlayService.pause();
				new Thread(new Runnable() {
					@Override
					public void run() {
						player.playUrl(netHotMusicList.get(currnetPoi)
								.getMp3Url());
						// 使用handler更新UI
						Message songInfo = handler.obtainMessage(
								Constant.WHAT_HOT_SONGINFO, currnetPoi);
						songInfo.sendToTarget();
					}
				}).start();
			}
		});
		lv_hotMusic.setAdapter(new HotMusicListAdapter(getActivity(),
				netHotMusicList));

		action.addPage(hotMusicView);
	}

}
