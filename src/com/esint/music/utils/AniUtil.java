package com.esint.music.utils;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;

/**
 * 动画类
 */
public class AniUtil {

	private final static int START = 0;

	private final static int STOP = 1;

	/**
	 * 开启动画
	 * 
	 * @param ani
	 */
	public static void startAnimation(final AnimationDrawable ani) {
		postAnimationMessage(ani, START);
	}

	/**
	 * 停止动画
	 * 
	 * @param ani
	 */
	public static void stopAnimation(final AnimationDrawable ani) {
		postAnimationMessage(ani, STOP);
	}

	/**
	 * 发送动画消息
	 * 
	 * @param what
	 * @param ani
	 */
	private static void postAnimationMessage(final AnimationDrawable ani,
			final int what) {
		aniHandler.postDelayed(new Runnable() {

			public void run() {
				Message msg = Message.obtain();
				msg.what = what;
				msg.obj = ani;
				aniHandler.sendMessage(msg);
			}
		}, 5);
	}

	private static Handler aniHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (!(msg.obj instanceof AnimationDrawable)) {
				return;
			}
			AnimationDrawable ani = (AnimationDrawable) msg.obj;
			if (msg.what == START) {
				ani.start();
			} else {
				ani.stop();
			}
		};
	};
}
