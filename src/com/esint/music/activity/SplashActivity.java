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
 * �����ƣ�SplashActivity   
 * �������� ���ֲ�����������ҳ  
 * �����ˣ�bai 
 * ����ʱ�䣺2016-1-10 ����11:23:15   
 *        
 */
public class SplashActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		ActivityCollectUtil.addActivity(this);
		// 3S����뵽������
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
				// ������ҳ��
				startActivity(new Intent(SplashActivity.this,
						MainFragmentActivity.class));
				// ʵ���������һ�����Ч��
				overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
				SplashActivity.this.finish();
				break;

			}

		}
	};

}