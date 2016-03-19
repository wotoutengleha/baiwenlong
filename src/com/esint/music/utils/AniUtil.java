package com.esint.music.utils;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * ������
 */
public class AniUtil {

	private final static int START = 0;

	private final static int STOP = 1;

	/**
	 * ��������
	 * 
	 * @param ani
	 */
	public static void startAnimation(final AnimationDrawable ani) {
		postAnimationMessage(ani, START);
	}

	/**
	 * ֹͣ����
	 * 
	 * @param ani
	 */
	public static void stopAnimation(final AnimationDrawable ani) {
		postAnimationMessage(ani, STOP);
	}

	/**
	 * ���Ͷ�����Ϣ
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

	// ���ϲ���Ķ���
	public static void startAnimationLike(View view) {
		view.setVisibility(View.VISIBLE);
		int fromX = view.getLeft();
		int fromY = view.getTop();

		AnimationSet animSet = new AnimationSet(true);
		// ע��ABSOLUTE��ʾ�뵱ǰ�Լ���View���Ե����ص�λ
		// ʹ��RELATIVE_TO_SELF��RELATIVE_TO_PARENTʱһ���ñ�����ϵ һ����1f 0f
		// ��ʾ���������򸸿ؼ��������ƶ�
		TranslateAnimation transAnim = new TranslateAnimation(
				Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -fromX,
				Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -fromY);

		AlphaAnimation alphaAnim1 = new AlphaAnimation(0f, 1f);
		ScaleAnimation scaleAnim1 = new ScaleAnimation(0, 1, 0, 1,
				Animation.RELATIVE_TO_PARENT, Animation.RELATIVE_TO_PARENT);

		AlphaAnimation alphaAnim2 = new AlphaAnimation(1f, 0f);
		ScaleAnimation scaleAnim2 = new ScaleAnimation(1, 0, 1, 0,
				Animation.RELATIVE_TO_PARENT, Animation.RELATIVE_TO_PARENT);

		transAnim.setDuration(600);

		scaleAnim1.setDuration(600);
		alphaAnim1.setDuration(600);

		scaleAnim2.setDuration(800);
		alphaAnim2.setDuration(800);
		scaleAnim2.setStartOffset(600);
		alphaAnim2.setStartOffset(600);
		transAnim.setStartOffset(600);

		animSet.addAnimation(scaleAnim1);
		animSet.addAnimation(alphaAnim1);

		animSet.addAnimation(scaleAnim2);
		animSet.addAnimation(alphaAnim2);
		animSet.addAnimation(transAnim);
		view.startAnimation(animSet);
		view.setVisibility(View.GONE);
	}
}
