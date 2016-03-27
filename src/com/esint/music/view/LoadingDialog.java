package com.esint.music.view;

import com.esint.music.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadingDialog extends Dialog {
	/**
	 * 旋转动画
	 */
	private Animation rotateAnimation;
	private ImageView loadingImageView;

	private DialogListener listener;
	/**
	 * 提示
	 */
	private TextView loadingTip;

	public LoadingDialog(Context context, int theme, DialogListener listener) {
		super(context, theme);
		this.listener = listener;
	}

	public LoadingDialog(Context context, int theme) {
		super(context, theme);
	}

	protected LoadingDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public LoadingDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_dialog);

		WindowManager windowManager = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.width = (int) (display.getWidth()); // 设置宽度
		getWindow().setAttributes(lp);

		setCanceledOnTouchOutside(false);

		loadingImageView = (ImageView) findViewById(R.id.loadingImageView);
		rotateAnimation = AnimationUtils.loadAnimation(getContext(),
				R.anim.anim_rotate);
		rotateAnimation.setInterpolator(new LinearInterpolator());// 匀速

		setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				// 停止动画
				loadingImageView.clearAnimation();
				if (listener != null) {
					listener.onDismissed();
				}
			}
		});
		setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface arg0) {
				loadingImageView.clearAnimation();
				loadingImageView.startAnimation(rotateAnimation);
				if (listener != null) {
					listener.onShowed();
				}
			}
		});

		loadingTip = (TextView) findViewById(R.id.loadingTip);
	}

	private Handler mhandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			show();
			String text = (String) msg.obj;
			loadingTip.setText(text);
		}

	};

	/**
	 * 提示
	 * 
	 * @param text
	 */
	public void showDialog(String text) {
		Message msg = new Message();
		msg.obj = text;
		mhandler.sendMessage(msg);
	}

	public interface DialogListener {
		public void onShowed();
		public void onDismissed();
	}

}
