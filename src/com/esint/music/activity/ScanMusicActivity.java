package com.esint.music.activity;

import name.teze.layout.lib.SwipeBackActivity;

import com.esint.music.R;
import com.esint.music.R.color;
import com.esint.music.utils.ActivityCollectUtil;
import com.esint.music.utils.Constant;
import com.esint.music.utils.SharedPrefUtil;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/*   
 *    
 * �����ƣ�ScanMusicActivity   
 * �������� ɨ�����ֵ���  
 * �����ˣ�bai 
 * ����ʱ�䣺2016-1-14 ����2:57:00   
 *        
 */
public class ScanMusicActivity extends SwipeBackActivity implements
		OnClickListener {

	private Button btnScan;
	private ImageView btnBack;
	private ProgressDialog mDialog;
	private Handler mHndler;
	private RelativeLayout scanMusicActionBar;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_scanmusic);
		ActivityCollectUtil.addActivity(this);
		initView();
		initData();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollectUtil.removeActivity(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		int colorIndex = SharedPrefUtil.getInt(ScanMusicActivity.this,
				Constant.COLOR_INDEX, -1);
		switch (colorIndex) {
		case 0:
			scanMusicActionBar.setBackgroundResource(color.tianyilan);
			break;
		case 1:
			scanMusicActionBar.setBackgroundResource(color.hidden_bitterness);
			break;
		case 2:
			scanMusicActionBar.setBackgroundResource(color.gorgeous);
			break;
		case 3:
			scanMusicActionBar.setBackgroundResource(color.romance);
			break;
		case 4:
			scanMusicActionBar.setBackgroundResource(color.sunset);
			break;
		case 5:
			scanMusicActionBar.setBackgroundResource(color.warm_colour);
			break;

		default:
			scanMusicActionBar.setBackgroundResource(color.holo_blue_light);
			break;
		}
	}

	private void initView() {
		btnScan = (Button) findViewById(R.id.scanBtn);
		btnBack = (ImageView) findViewById(R.id.backBtnSC);
		scanMusicActionBar = (RelativeLayout) findViewById(R.id.topLayout);
		mDialog = new ProgressDialog(this);
		btnScan.setOnClickListener(this);
		btnBack.setOnClickListener(this);

	}

	private void initData() {
		mHndler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mDialog.dismiss();
			}
		};

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.scanBtn) {
			mDialog.setMessage("����ɨ�裬�����˳�������");
			mDialog.show();
			mHndler.sendEmptyMessageDelayed(0, 3000);
		} else if (v.getId() == R.id.backBtnSC) {
			finish();
			// ʵ���������һ�����Ч��
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
		}
	}
}
