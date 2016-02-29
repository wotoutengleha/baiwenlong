package com.esint.music.adapter;

import java.util.ArrayList;

import com.esint.music.R;
import com.esint.music.model.SearchMusicInfo;
import com.esint.music.utils.DownMusicUtils;
import com.esint.music.utils.DownMusicUtils.OnDownLoadListener;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchResultAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<SearchMusicInfo> netDataList;

	public SearchResultAdapter(Context context,
			ArrayList<SearchMusicInfo> searchResult) {
		this.context = context;
		this.netDataList = searchResult;
	}

	@Override
	public int getCount() {
		return netDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return netDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private ViewHolder viewHolder;

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.ilv_netmusic, null);
			viewHolder.musicName = (TextView) convertView
					.findViewById(R.id.title_net);
			viewHolder.artist = (TextView) convertView
					.findViewById(R.id.songname_net);
			viewHolder.arrowDown = (ImageView) convertView
					.findViewById(R.id.arrowDown);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		SearchMusicInfo searchResult = netDataList.get(position);
		viewHolder.musicName.setText(searchResult.getMusicName());
		viewHolder.artist.setText(searchResult.getMusicArtist());
		viewHolder.arrowDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDownTips(position);
			}
		});
		return convertView;
	}

	class ViewHolder {
		TextView musicName;
		TextView artist;
		ImageView arrowDown;
	}

	// 展示下载音乐的对话框
	private void showDownTips(final int position) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final AlertDialog dialog = builder.create();
		View dialogDownload = View.inflate(context, R.layout.dialog_download,
				null);
		TextView cancle = (TextView) dialogDownload
				.findViewById(R.id.tv_dismiss);
		TextView ok = (TextView) dialogDownload.findViewById(R.id.tv_ok);
		dialog.show();
		dialog.setContentView(dialogDownload);
		cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				downloadMusic(position);
			}
		});
	}

	// 下载音乐

	private void downloadMusic(int position) {

//		Toast.makeText(context,
//				"正在下载" + netDataList.get(position).getMusicName(), 0).show();
//		DownMusicUtils.getInstance().setListener(new OnDownLoadListener() {
//			@Override
//			public void onFailed(String error) {
//				Toast.makeText(context, error, 0).show();
//			}
//			@Override
//			public void onDownLoad(String mp3Url) {
//				Toast.makeText(context, "下载成功", 0).show();
//			}
//		}).down(netDataList.get(position));
	}
}
