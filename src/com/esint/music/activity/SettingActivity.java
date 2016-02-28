package com.esint.music.activity;

import name.teze.layout.lib.SwipeBackActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.esint.music.R;
import com.esint.music.R.color;
import com.esint.music.utils.Constant;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.view.SetupBGButton;
import com.esint.music.view.SetupColorBGButton;

public class SettingActivity extends SwipeBackActivity implements
		OnClickListener {

	private SetupBGButton[] soundBGButton;// 音质按钮
	private int soundIndex = Constant.soundIndex;// 音质索引
	private SetupColorBGButton[] colorBGButton;// 标题颜色
	private String[] colorBGColorStr = Constant.colorBGColorStr;// 标题颜色集合
	private int colorIndex = Constant.colorIndex;// 标题颜色索引

	private ImageView backImv;
	private RelativeLayout settingActionBar;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);
		initView();
		initData();
		initComponent();
	}

	private void initView() {
		backImv = (ImageView) findViewById(R.id.backBtn);
		settingActionBar = (RelativeLayout) findViewById(R.id.actionbar);
		backImv.setOnClickListener(this);
	}

	private void initData() {
	}

	@Override
	protected void onResume() {
		super.onResume();
		int colorIndex = SharedPrefUtil.getInt(SettingActivity.this,
				Constant.COLOR_INDEX, -1);
		int colorIndexSelect = SharedPrefUtil.getInt(SettingActivity.this,
				Constant.COLOR_INDEX_SELECT, -1);
		if (colorIndexSelect == -1) {
			colorBGButton[0].setSelect(true);
		} else {
			colorBGButton[colorIndexSelect].setSelect(true);
		}
		switch (colorIndex) {
		case 0:
			settingActionBar.setBackgroundResource(color.tianyilan);
			break;
		case 1:
			settingActionBar.setBackgroundResource(color.hidden_bitterness);
			break;
		case 2:
			settingActionBar.setBackgroundResource(color.gorgeous);
			break;
		case 3:
			settingActionBar.setBackgroundResource(color.romance);
			break;
		case 4:
			settingActionBar.setBackgroundResource(color.sunset);
			break;
		case 5:
			settingActionBar.setBackgroundResource(color.warm_colour);
			break;

		default:
			settingActionBar.setBackgroundResource(color.holo_blue_light);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backBtn: {
			finish();
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			break;
		}
		}
	}

	private void initComponent() {
		soundBGButton = new SetupBGButton[6];
		int i = 0;
		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound0);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound1);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound2);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound3);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound4);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[i] = (SetupBGButton) findViewById(R.id.sound5);
		soundBGButton[i++].setOnClickListener(soundOnClickListener);

		soundBGButton[soundIndex].setSelect(true);

		i = 0;
		colorBGButton = new SetupColorBGButton[colorBGColorStr.length];

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color0);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color1);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color2);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color3);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color4);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		colorBGButton[i] = (SetupColorBGButton) findViewById(R.id.color5);
		colorBGButton[i].setDefColorStr(colorBGColorStr[i]);
		colorBGButton[i++].setOnClickListener(colorOnClickListener);

		// colorBGButton[colorIndex].setSelect(true);
	}

	private OnClickListener soundOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int index = 0;
			switch (v.getId()) {
			case R.id.sound0:
				index = 0;
				break;
			case R.id.sound1:
				index = 1;
				break;
			case R.id.sound2:
				index = 2;
				break;
			case R.id.sound3:
				index = 3;
				break;
			case R.id.sound4:
				index = 4;
				break;
			case R.id.sound5:
				index = 5;
				break;
			}
			if (soundIndex == index) {
				return;
			}
			soundBGButton[soundIndex].setSelect(false);
			soundIndex = index;
			soundBGButton[soundIndex].setSelect(true);

			Constant.soundIndex = soundIndex;
		}
	};

	private OnClickListener colorOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int index = 0;
			switch (v.getId()) {
			case R.id.color0:
				index = 0;
				settingActionBar.setBackgroundResource(color.tianyilan);// 优雅
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			case R.id.color1:
				index = 1;
				settingActionBar.setBackgroundResource(color.hidden_bitterness);// 幽怨
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			case R.id.color2:
				index = 2;
				settingActionBar.setBackgroundResource(color.gorgeous);// 绚丽
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			case R.id.color3:
				index = 3;
				settingActionBar.setBackgroundResource(color.romance);// 浪漫
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			case R.id.color4:
				index = 4;
				settingActionBar.setBackgroundResource(color.sunset);// 夕阳
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			case R.id.color5:
				index = 5;
				settingActionBar.setBackgroundResource(color.warm_colour);// 暖色
				SharedPrefUtil.setInt(SettingActivity.this,
						Constant.COLOR_INDEX, index);
				break;
			}
			if (colorIndex == index) {
				return;
			}
			colorBGButton[colorIndex].setSelect(false);
			colorIndex = index;
			SharedPrefUtil.setInt(SettingActivity.this,
					Constant.COLOR_INDEX_SELECT, colorIndex);
			colorBGButton[colorIndex].setSelect(true);
		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
}
