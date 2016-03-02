package com.esint.music.adapter;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esint.music.R;
import com.esint.music.model.SearchMusicInfo;
import com.esint.music.utils.DownMusicUtils;
import com.esint.music.utils.DownMusicUtils.OnDownLoadListener;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
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
	private HttpUtils httpUtils = new HttpUtils();

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
				getDownUrl(position);
				// downloadMusic(position);
			}
		});
	}

	// 根据点击歌曲的位置通过音乐的ID找到音乐的链接和图片的链接
	private void getDownUrl(final int position) {
		// http://music.163.com/api/song/detail/?id=29818120&ids=[29818120]
		String musicID = netDataList.get(position).getMusicID();
		String musicUrl = "http://music.163.com/api/song/detail/?id=" + musicID
				+ "&ids=[" + musicID + "]";
		httpUtils.send(HttpMethod.GET, musicUrl, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				Toast.makeText(context, "请求下载链接失败了", 0).show();
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				String response = arg0.result;
				Log.e("请求的网络地址", getRequestUrl());
				parseJsonResult(position,response);
			}
		});

	}

	/**
	 * @Description:解析请求的链接返回的数据 目的是为了获取MP3 和图片的下载链接
	 * @return void
	 * @author bai
	 */
	protected void parseJsonResult(int position,String response) {

		try {
			JSONObject result = new JSONObject(response);
			String songs = result.getString("songs");
			JSONArray songArray = new JSONArray(songs);
			for (int i = 0; i < songArray.length(); i++) {
				JSONObject object = (JSONObject) songArray.get(0);
				String mp3Url = object.getString("mp3Url");
				String album = object.getString("album");
				JSONObject picObject = new JSONObject(album);
				String picUrl = picObject.getString("picUrl");
				downloadMusic(position,mp3Url, picUrl);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	// 下载音乐

	private void downloadMusic(final int position, String mp3Url,
			final String picUrl) {

		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的歌曲" + "/" + netDataList.get(position).getMusicArtist()
				+ " - " + netDataList.get(position).getMusicName() + ".mp3";

		final String ImageTarget = Environment.getExternalStorageDirectory()
				+ "/" + "/下载的图片" + "/"
				+ netDataList.get(position).getMusicName() + ".jpg";
		final HttpUtils httpUtils = new HttpUtils();
		httpUtils.download(mp3Url, MusicTarget, new RequestCallBack<File>() {
			@Override
			public void onSuccess(ResponseInfo<File> arg0) {
				Toast.makeText(context, "音乐下载成功", 0).show();
				// 音乐下载成功后把图片也下载下来
				httpUtils.download(picUrl, ImageTarget,
						new RequestCallBack<File>() {

							@Override
							public void onSuccess(ResponseInfo<File> arg0) {
								Toast.makeText(context, "图片下载成功", 0).show();
							}

							@Override
							public void onFailure(HttpException arg0,
									String arg1) {
								Toast.makeText(context, "图片下载失败", 0).show();
							}
						});

			}

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				Toast.makeText(context, "音乐下载失败", 0).show();
				Log.e("下载失败", arg1);

			}

			@Override
			public void onLoading(long total, long current, boolean isUploading) {
				super.onLoading(total, current, isUploading);
				Log.e("current", current + "  /  " + total);
			}
		});

	}
}
