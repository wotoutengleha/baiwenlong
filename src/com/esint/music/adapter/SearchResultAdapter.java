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
import com.esint.music.utils.MyHttpUtils;
import com.esint.music.view.LoadingDialog;
import com.esint.music.view.LoadingDialog.DialogListener;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**   
* 类名称：SearchResultAdapter   
* 类描述：  搜索音乐列表的适配器 
* 创建人：bai   
* 创建时间：2016-3-17 下午4:16:41         
*/
public class SearchResultAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<SearchMusicInfo> netDataList;
	private HttpUtils httpUtils = new HttpUtils();
	private LoadingDialog loadingDialog;

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
		WindowManager m = ((Activity) context).getWindowManager();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
		params.height = (int) (d.getHeight() * 0.286); // 高度设置为屏幕的0.6
		params.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.65
		dialog.getWindow().setAttributes(params);
		dialog.setContentView(dialogDownload);
		cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		final MyHttpUtils myHttpUtils = new MyHttpUtils(context);
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				// getDownUrl(position);
				boolean isConnected = myHttpUtils.isConnnected(context);
				if (isConnected == false) {
					Toast.makeText(context, "当前网络没有连接", 0).show();
					return;
				}
				// 判断网络类型
				int networkType = myHttpUtils.getNetworkType();
				Log.e("当前的网络是", networkType + "");
				switch (networkType) {
				case 0:// 流量网络
					showNetAlert(position);
					break;
				case 1:// Wi-Fi网络
					getDownUrl(position);
					break;

				}
			}
		});
	}

	/**
	* @Description:判断网络类型 ，在流量状态下提示 
	* @param position
	* @return void 
	* @author bai
	*/
	private void showNetAlert(final int position) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final AlertDialog dialog = builder.create();
		View alertDialogView = View.inflate(context, R.layout.dialog_nettips,
				null);
		TextView okTv = (TextView) alertDialogView
				.findViewById(R.id.tv_countinue);
		TextView cancelTv = (TextView) alertDialogView
				.findViewById(R.id.tv_stop);
		dialog.show();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		WindowManager m = ((Activity) context).getWindowManager();
		Display d = m.getDefaultDisplay();
		params.height = (int) (d.getHeight() * 0.286);
		params.width = (int) (d.getWidth() * 0.8);
		dialog.getWindow().setAttributes(params);
		dialog.setContentView(alertDialogView);
		okTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				getDownUrl(position);
			}
		});
		cancelTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
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
				parseJsonResult(position, response);
			}
		});

	}

	/**
	 * @Description:解析请求的链接返回的数据 目的是为了获取MP3 和图片的下载链接
	 * @return void
	 * @author bai
	 */
	protected void parseJsonResult(int position, String response) {

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
				downloadMusic(position, mp3Url, picUrl);
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

				
				// 音乐下载成功后把图片也下载下来
				httpUtils.download(picUrl, ImageTarget,
						new RequestCallBack<File>() {

							@Override
							public void onSuccess(ResponseInfo<File> arg0) {
								if (loadingDialog.isShowing()
										&& loadingDialog != null) {
									Toast.makeText(context, "音乐下载成功", 0).show();
									loadingDialog.dismiss();
								}
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
				if (loadingDialog == null) {
					loadingDialog = new LoadingDialog(context,
							R.style.dialogloading, new DialogListener() {

								@Override
								public void onShowed() {
								}

								@Override
								public void onDismissed() {
								}
							});
				}
				loadingDialog.showDialog("正在下载，请稍后~");
			}
		});

	}
}
