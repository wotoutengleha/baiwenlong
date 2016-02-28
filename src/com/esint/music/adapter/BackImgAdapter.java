package com.esint.music.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.esint.music.R;
import com.esint.music.model.BackImg;
import com.esint.music.utils.SharedPrefUtil;

public class BackImgAdapter extends BaseAdapter {

	private List<BackImg> backImgList;
	private Resources resources;
	private Context context;
	private SharedPrefUtil prefUtil;
	private String defaultPath;

	public BackImgAdapter(List<BackImg> mBackList, Context context) {
		this.context = context;
		this.backImgList = mBackList;
		this.resources = context.getResources();
		// 得到保存图片的路径
		prefUtil = new SharedPrefUtil(context);
		defaultPath = prefUtil.getImgPath();
	}

	@Override
	public int getCount() {
		return backImgList.size();

	}

	@Override
	public Object getItem(int position) {
		return backImgList.get(position);

	}

	@Override
	public long getItemId(int position) {
		return position;

	}

	private ViewHolder viewHolder;

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.ilv_background_gridview, null);
			viewHolder.ivBackImg = (ImageView) convertView
					.findViewById(R.id.gridview_item_iv);
			viewHolder.ivChecked = (ImageView) convertView
					.findViewById(R.id.gridview_item_checked_iv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Bitmap bitmap = backImgList.get(position).getBitmap();
		String path = backImgList.get(position).getPath();
		if (path.equals(defaultPath)) {
			viewHolder.ivChecked.setVisibility(View.VISIBLE);
		} else {
			viewHolder.ivChecked.setVisibility(View.GONE);
		}

		viewHolder.ivBackImg
				.setBackground(new BitmapDrawable(resources, bitmap));
		return convertView;
	}

	class ViewHolder {
		public ImageView ivChecked;
		public ImageView ivBackImg;
	}

}
