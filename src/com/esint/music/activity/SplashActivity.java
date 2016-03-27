package com.esint.music.activity;

import com.esint.music.R;
import com.esint.music.service.MusicPlayService;
import com.esint.music.utils.ActivityCollectUtil;
import com.esint.music.utils.Constant;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.content.Intent;

/*   
 *    
 * 类名称：SplashActivity   
 * 类描述： 音乐播放器的闪屏页  
 * 创建人：bai 
 * 创建时间：2016-1-10 上午11:23:15   
 *        
 */
public class SplashActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		ActivityCollectUtil.addActivity(this);
		// 3S后进入到主界面
		mHandler.sendEmptyMessageDelayed(Constant.START_ACTIVITY, 100);
		startService(new Intent(this, MusicPlayService.class));
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollectUtil.removeActivity(this);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Constant.START_ACTIVITY:
				// 跳到主页面
				startActivity(new Intent(SplashActivity.this,
						MainFragmentActivity.class));
				// 实现由左至右滑动的效果
				overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
				SplashActivity.this.finish();
				break;

			}

		}
	};

}