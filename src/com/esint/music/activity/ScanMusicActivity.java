package com.esint.music.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import name.teze.layout.lib.SwipeBackActivity;

import com.esint.music.R;
import com.esint.music.R.color;
import com.esint.music.fragment.MyTabMusic;
import com.esint.music.model.Mp3Info;
import com.esint.music.utils.ActivityCollectUtil;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.MyApplication;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.utils.SortListUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/*   
 *    
 * 类名称：ScanMusicActivity   
 * 类描述： 扫描音乐的类  
 * 创建人：bai 
 * 创建时间：2016-1-14 下午2:57:00   
 *        
 */
public class ScanMusicActivity extends SwipeBackActivity implements
		OnClickListener {

	private Button btnScan;
	private ImageView btnBack;
	private static ProgressDialog mDialog;
	private Handler mHndler;
	private RelativeLayout scanMusicActionBar;
	private ScanSdReceiver scanSdReceiver;

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
		if (scanSdReceiver != null) {
			unregisterReceiver(scanSdReceiver);
		}
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
		mDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
		btnScan.setOnClickListener(this);
		btnBack.setOnClickListener(this);

	}

	private void initData() {
		mHndler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mDialog.dismiss();
				finish();
				overridePendingTransition(R.anim.slide_in_left,
						R.anim.slide_out_right);
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
			mDialog.setMessage("正在扫描，请勿退出。。。");
			mDialog.show();
			// mHndler.sendEmptyMessageDelayed(0, 3000);
			scanSdCard();
		} else if (v.getId() == R.id.backBtnSC) {
			finish();
			// 实现由左至右滑动的效果
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
		}
	}

	private void scanSdCard() {
		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/qqmusic" + "/song";
		Log.e("MusicTarget", MusicTarget + "");
		IntentFilter intentfilter = new IntentFilter(
				Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentfilter.addDataScheme("file");
		scanSdReceiver = new ScanSdReceiver();
		this.registerReceiver(scanSdReceiver, intentfilter);
		this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				Uri.parse("file://" + MusicTarget)));
	}

	public static void deleteMusic(int position, long ids) {
		MyApplication
				.getContext()
				.getContentResolver()
				.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						MediaStore.Audio.Media._ID + "=" + ids, null);
	}

	/**   
	* 类名称：ScanSdReceiver   
	* 类描述：扫描音乐的广播   
	* 创建人：bai   
	* 创建时间：2016-4-5 上午9:11:52         
	*/
	public static class ScanSdReceiver extends BroadcastReceiver {

		private AlertDialog.Builder builder = null;
		private AlertDialog ad = null;
		private int count1;
		private int count2;
		private int count;

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
				Cursor c1 = context.getContentResolver().query(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						new String[] { MediaStore.Audio.Media.TITLE,
								MediaStore.Audio.Media.DURATION,
								MediaStore.Audio.Media.ARTIST,
								MediaStore.Audio.Media._ID,
								MediaStore.Audio.Media.DISPLAY_NAME }, null,
						null, null);
				count1 = c1.getCount();
				System.out.println("count:" + count);
				builder = new AlertDialog.Builder(context);
				builder.setMessage("正在扫描存储卡...");
				ad = builder.create();

			} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
				Cursor c2 = context.getContentResolver().query(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						new String[] { MediaStore.Audio.Media.TITLE,
								MediaStore.Audio.Media.DURATION,
								MediaStore.Audio.Media.ARTIST,
								MediaStore.Audio.Media._ID,
								MediaStore.Audio.Media.DISPLAY_NAME }, null,
						null, null);
				count2 = c2.getCount();
				count = count2 - count1;
				if (mDialog != null && mDialog.isShowing()) {
					mDialog.dismiss();
				}
				if (count >= 0) {
					Toast.makeText(context, "共增加" + count + "首歌曲",
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(context, "共减少" + count + "首歌曲",
							Toast.LENGTH_LONG).show();
				}
			}
			SortListUtil sortListUtil = new SortListUtil();
			ArrayList<Mp3Info> mp3List = MediaUtils.getMp3Info(MyApplication
					.getContext());
			mp3List = sortListUtil.initMyLocalMusic(mp3List);
			Message message = MyTabMusic.mHandler.obtainMessage(88888, mp3List);
			message.sendToTarget();
		}
	}

}
