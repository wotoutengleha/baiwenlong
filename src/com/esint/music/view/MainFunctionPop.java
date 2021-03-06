package com.esint.music.view;

import com.esint.music.R;
import com.esint.music.activity.MainFragmentActivity;
import com.esint.music.service.MusicPlayService;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MyApplication;
import com.esint.music.utils.SharedPrefUtil;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainFunctionPop extends PopupWindow {

	private View mainMenuView;
	private RelativeLayout mScanRl, mSkinRl, mAboutRl, mPlayModeRl, mSettingRl,
			mExitRl, popMenuParent;

	private RelativeLayout randomLayout;
	private RelativeLayout orderLayout;
	private RelativeLayout singleLayout;

	public MainFunctionPop(Context context, OnClickListener itemListener) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mainMenuView = inflater.inflate(R.layout.mainpopu_menu, null);
		popMenuParent = (RelativeLayout) mainMenuView
				.findViewById(R.id.popMenuParent);
		mScanRl = (RelativeLayout) mainMenuView.findViewById(R.id.menuScan);
		mSkinRl = (RelativeLayout) mainMenuView.findViewById(R.id.menuSkin);
		mAboutRl = (RelativeLayout) mainMenuView.findViewById(R.id.menuAbout);
		mPlayModeRl = (RelativeLayout) mainMenuView
				.findViewById(R.id.menuPlayMode);
		mSettingRl = (RelativeLayout) mainMenuView
				.findViewById(R.id.menuSetting);
		mExitRl = (RelativeLayout) mainMenuView.findViewById(R.id.menuExit);

		randomLayout = (RelativeLayout) mainMenuView
				.findViewById(R.id.menuRandomParent);
		orderLayout = (RelativeLayout) mainMenuView
				.findViewById(R.id.menuOrderParent);
		singleLayout = (RelativeLayout) mainMenuView
				.findViewById(R.id.menuRepeatoneParent);

		int playMode = SharedPrefUtil.getInt(MyApplication.getContext(),
				Constant.PLAY_MODE, -1);
		Log.e("playMode", playMode + "");

		switch (playMode) {
		case 1:
			orderLayout.setVisibility(View.VISIBLE);
			singleLayout.setVisibility(View.INVISIBLE);
			randomLayout.setVisibility(View.INVISIBLE);
			break;
		case 2:
			randomLayout.setVisibility(View.VISIBLE);
			singleLayout.setVisibility(View.INVISIBLE);
			orderLayout.setVisibility(View.INVISIBLE);
			break;
		case 3:
			singleLayout.setVisibility(View.VISIBLE);
			randomLayout.setVisibility(View.INVISIBLE);
			orderLayout.setVisibility(View.INVISIBLE);
			break;

		}

		mScanRl.setOnClickListener(itemListener);
		mSkinRl.setOnClickListener(itemListener);
		mAboutRl.setOnClickListener(itemListener);
		mPlayModeRl.setOnClickListener(itemListener);
		mSettingRl.setOnClickListener(itemListener);
		mExitRl.setOnClickListener(itemListener);
		// 设置SelectPicPopupWindow的View
		this.setContentView(mainMenuView);
		// 设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.FILL_PARENT);
		// 设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		// 设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		// 必须设置此属性，否则不
		this.setBackgroundDrawable(new BitmapDrawable());
		// 设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.Animationbutton);
		// mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
		mainMenuView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				int height = mainMenuView.findViewById(R.id.pop_layout)
						.getTop();
				int y = (int) event.getRawY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						dismiss();
					}
				}
				return true;
			}
		});
		popMenuParent.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_BACK) {
					dismiss();
					mainMenuView = null;
				}
				return false;
			}
		});

		MainFragmentActivity.mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				Log.e("接收到了消息", "接收到了消息");
				switch (MusicPlayService.getPlayMode()) {
				case 2:
					randomLayout.setVisibility(View.VISIBLE);
					singleLayout.setVisibility(View.INVISIBLE);
					orderLayout.setVisibility(View.INVISIBLE);
					SharedPrefUtil.setInt(MyApplication.getContext(),
							Constant.PLAY_MODE, 2);
					break;
				case 3:
					singleLayout.setVisibility(View.VISIBLE);
					randomLayout.setVisibility(View.INVISIBLE);
					orderLayout.setVisibility(View.INVISIBLE);
					SharedPrefUtil.setInt(MyApplication.getContext(),
							Constant.PLAY_MODE, 3);
					break;
				case 1:
					orderLayout.setVisibility(View.VISIBLE);
					singleLayout.setVisibility(View.INVISIBLE);
					randomLayout.setVisibility(View.INVISIBLE);
					SharedPrefUtil.setInt(MyApplication.getContext(),
							Constant.PLAY_MODE, 1);
					break;
				}

			}
		};

	}
}
