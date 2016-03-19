package com.esint.music.adapter;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.model.HotMusicInfo;
import com.esint.music.model.Mp3Info;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.model.NewMusicInfo;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MyHttpUtils;
import com.esint.music.utils.SharedPrefUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

public class HotMusicListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<HotMusicInfo> hotNewMusicList;

	public HotMusicListAdapter(Context context,
			ArrayList<HotMusicInfo> hotNewMusicList) {
		this.context = context;
		this.hotNewMusicList = hotNewMusicList;

	}

	@Override
	public int getCount() {
		return hotNewMusicList.size();
	}

	@Override
	public Object getItem(int position) {
		return hotNewMusicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private ViewHolder holder;

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.ilv_netnewmusic, null);
			holder.musicName = (TextView) convertView
					.findViewById(R.id.tv_musicname);
			holder.yuanChang = (TextView) convertView
					.findViewById(R.id.tv_yuanchang);
			holder.artist = (TextView) convertView.findViewById(R.id.tv_artist);
			holder.album = (TextView) convertView.findViewById(R.id.tv_album);
			holder.position = (TextView) convertView
					.findViewById(R.id.tv_position);
			holder.ib_download = (ImageView) convertView
					.findViewById(R.id.ib_download);
			holder.rl_newmusic = (RelativeLayout) convertView
					.findViewById(R.id.rl_newmusic);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		HotMusicInfo netHotMusicInfo = hotNewMusicList.get(position);
		holder.musicName.setText(netHotMusicInfo.getName());
		if (netHotMusicInfo.getAlias().equals("")) {
			holder.yuanChang.setText("");
		} else {
			holder.yuanChang.setText("(" + netHotMusicInfo.getAlias() + ")");
		}
		holder.artist.setText(netHotMusicInfo.getArtistsName());
		holder.album.setText(" - " + netHotMusicInfo.getAlbumName());
		holder.position.setText(position + 1 + "");

		holder.ib_download.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDownTips(position);
			}
		});

		return convertView;
	}

	class ViewHolder {
		TextView musicName;
		TextView yuanChang;
		TextView artist;
		TextView album;
		TextView position;
		ImageView ib_download;
		RelativeLayout rl_newmusic;
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
					downloadMusic(position);
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
				downloadMusic(position);
			}
		});
		cancelTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

	}
	


	protected void downloadMusic(final int position) {

		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的歌曲" + "/"
				+ hotNewMusicList.get(position).getArtistsName() + " - "
				+ hotNewMusicList.get(position).getName() + ".mp3";

		final String ImageTarget = Environment.getExternalStorageDirectory()
				+ "/" + "/下载的图片" + "/"
				+ hotNewMusicList.get(position).getName() + ".jpg";
		final HttpUtils httpUtils = new HttpUtils();
		httpUtils.download(hotNewMusicList.get(position).getMp3Url(),
				MusicTarget, new RequestCallBack<File>() {
					@Override
					public void onSuccess(ResponseInfo<File> arg0) {
						Toast.makeText(context, "音乐下载成功", 0).show();
						// 音乐下载成功后把图片也下载下来
						httpUtils.download(hotNewMusicList.get(position)
								.getPicUrl(), ImageTarget,
								new RequestCallBack<File>() {

									@Override
									public void onSuccess(
											ResponseInfo<File> arg0) {
									}

									@Override
									public void onFailure(HttpException arg0,
											String arg1) {
									}
								});

					}

					@Override
					public void onFailure(HttpException arg0, String arg1) {

					}

					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						super.onLoading(total, current, isUploading);
					}
				});

	}
}
