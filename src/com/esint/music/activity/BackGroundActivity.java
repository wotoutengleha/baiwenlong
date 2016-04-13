package com.esint.music.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import name.teze.layout.lib.SwipeBackActivity;

import com.esint.music.R;
import com.esint.music.R.color;
import com.esint.music.adapter.BackImgAdapter;
import com.esint.music.dialog.Effectstype;
import com.esint.music.dialog.NiftyDialogBuilder;
import com.esint.music.model.BackImg;
import com.esint.music.utils.ActivityCollectUtil;
import com.esint.music.utils.Constant;
import com.esint.music.utils.SharedPrefUtil;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

public class BackGroundActivity extends SwipeBackActivity implements
		OnClickListener {

	private ImageView btnBack;
	private GridView mGridView;
	private List<BackImg> mBackList;
	private BackImgAdapter backImgAdapter;
	private SharedPrefUtil prefUtil;
	private Effectstype effect;
	public static String defaultPath;
	private NiftyDialogBuilder dialogBuilder;
	private RelativeLayout backActionBar;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_background);
		ActivityCollectUtil.addActivity(this);
		initView();
		initData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		int colorIndex = SharedPrefUtil.getInt(BackGroundActivity.this,
				Constant.COLOR_INDEX, -1);
		switch (colorIndex) {
		case 0:
			backActionBar.setBackgroundResource(color.tianyilan);
			break;
		case 1:
			backActionBar.setBackgroundResource(color.hidden_bitterness);
			break;
		case 2:
			backActionBar.setBackgroundResource(color.gorgeous);
			break;
		case 3:
			backActionBar.setBackgroundResource(color.romance);
			break;
		case 4:
			backActionBar.setBackgroundResource(color.sunset);
			break;
		case 5:
			backActionBar.setBackgroundResource(color.warm_colour);
			break;

		default:
			backActionBar.setBackgroundResource(color.holo_blue_light);
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollectUtil.removeActivity(this);
	}

	private void initView() {
		btnBack = (ImageView) findViewById(R.id.backBtnBG);
		mGridView = (GridView) findViewById(R.id.grid_content);
		backActionBar = (RelativeLayout) findViewById(R.id.top_layout_bg);
		btnBack.setOnClickListener(this);
	}

	private void initData() {
		getImgBack();
		// 得到保存图片的路径
		prefUtil = new SharedPrefUtil(this);
		defaultPath = prefUtil.getImgPath();
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				chooseBackImg(position);
			}
		});
		backImgAdapter = new BackImgAdapter(mBackList, this);
		mGridView.setAdapter(backImgAdapter);
	}

	// 选择背景提示的dialog
	private void chooseBackImg(final int position) {
		effect = Effectstype.Fall;
		dialogBuilder = new NiftyDialogBuilder(this, R.style.dialog_untran);
		dialogBuilder.withTitle("提示").withTitleColor("#FFFFFF")
				.withDividerColor("#97E8F9").withMessage("确定要使用此背景图片吗？")
				.withMessageColor("#FFFFFF").isCancelableOnTouchOutside(true)
				.withEffect(effect).withButton1Text("确定").withButton2Text("取消")
				.setCustomView(R.layout.custom_view, this)
				.setButton1Click(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String path = mBackList.get(position).getPath();
						defaultPath = path;
						prefUtil.saveImgPath(path);
						dialogBuilder.dismiss();
						overridePendingTransition(R.anim.slide_in_left,
								R.anim.slide_out_right);
						finish();
					}
				}).setButton2Click(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialogBuilder.dismiss();
					}
				}).show();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.backBtnBG) {
			finish();
			// 实现由左至右滑动的效果
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
		}
	}

	// 从assets拿到图片
	private void getImgBack() {
		AssetManager manager = getAssets();
		try {
			String[] drawableList = manager.list("bkgs");
			mBackList = new ArrayList<BackImg>();
			for (String path : drawableList) {
				BackImg backImg = new BackImg();
				InputStream is = manager.open("bkgs/" + path);
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				backImg.path = path;
				backImg.bitmap = bitmap;
				mBackList.add(backImg);
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
