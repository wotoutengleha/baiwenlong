package com.esint.music.activity;

import name.teze.layout.lib.SwipeBackActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.R.color;
import com.esint.music.utils.Constant;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.view.LoadingDialog;
import com.esint.music.view.LoadingDialog.DialogListener;

public class AboutActivity extends SwipeBackActivity implements OnClickListener {

	private ImageView backBtn;
	private RelativeLayout impress;
	private RelativeLayout introduction;
	private RelativeLayout newVersion;
	private LoadingDialog dialog = null;
	private RelativeLayout aboutActionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
		initView();
		initData();
	}

	private void initView() {

		backBtn = (ImageView) findViewById(R.id.backBtn_about);
		impress = (RelativeLayout) findViewById(R.id.suixinxiang);
		introduction = (RelativeLayout) findViewById(R.id.introduction);
		newVersion = (RelativeLayout) findViewById(R.id.update);
		aboutActionBar = (RelativeLayout) findViewById(R.id.actionbar_about);
		impress.setOnClickListener(this);
		introduction.setOnClickListener(this);
		newVersion.setOnClickListener(this);
		backBtn.setOnClickListener(this);

	}

	private void initData() {
		

	}

	@Override
	protected void onResume() {
		super.onResume();
		int colorIndex = SharedPrefUtil.getInt(AboutActivity.this,
				Constant.COLOR_INDEX, -1);
		switch (colorIndex) {
		case 0:
			aboutActionBar.setBackgroundResource(color.tianyilan);
			break;
		case 1:
			aboutActionBar.setBackgroundResource(color.hidden_bitterness);
			break;
		case 2:
			aboutActionBar.setBackgroundResource(color.gorgeous);
			break;
		case 3:
			aboutActionBar.setBackgroundResource(color.romance);
			break;
		case 4:
			aboutActionBar.setBackgroundResource(color.sunset);
			break;
		case 5:
			aboutActionBar.setBackgroundResource(color.warm_colour);
			break;
		default:
			aboutActionBar.setBackgroundResource(color.holo_blue_light);
			break;
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.backBtn_about:
			finish();
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			break;
		case R.id.suixinxiang: {

		}
			break;
		case R.id.introduction: {

		}
			break;
		case R.id.update: {

			if (dialog == null) {
				dialog = new LoadingDialog(this, R.style.dialog,
						new DialogListener() {

							@Override
							public void onShowed() {
								onshow();
							}

							@Override
							public void onDismissed() {
							}
						});
			}
			dialog.showDialog("正在检查更新......");

		}
			break;
		}

	}

	private void onshow() {
		if (dialog != null && dialog.isShowing()) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(AboutActivity.this, "暂无新版本",
							Toast.LENGTH_SHORT).show();
					dialog.dismiss();
				}
			}, 5000);

		}
	}
}
