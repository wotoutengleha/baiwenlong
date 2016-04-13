package com.esint.music.adapter;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.model.Mp3Info;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.model.NewMusicInfo;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MyHttpUtils;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.view.LoadingDialog;
import com.esint.music.view.LoadingDialog.DialogListener;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

public class NewMusicListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<NewMusicInfo> netNewMusicList;
	private MyHttpUtils myHttpUtils;
	private LoadingDialog loadingDialog;

	public NewMusicListAdapter(Context context,
			ArrayList<NewMusicInfo> netNewMusicList) {
		this.context = context;
		this.netNewMusicList = netNewMusicList;

	}

	@Override
	public int getCount() {
		return netNewMusicList.size();
	}

	@Override
	public Object getItem(int position) {
		return netNewMusicList.get(position);
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
		NewMusicInfo netNewMusicInfo = netNewMusicList.get(position);
		holder.musicName.setText(netNewMusicInfo.getName());
		if (netNewMusicInfo.getAlias().equals("")) {
			holder.yuanChang.setText("");
		} else {
			holder.yuanChang.setText("(" + netNewMusicInfo.getAlias() + ")");
		}
		holder.artist.setText(netNewMusicInfo.getArtistsName());
		holder.album.setText(" - " + netNewMusicInfo.getAlbumName());
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
	@SuppressWarnings("deprecation")
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
		myHttpUtils = new MyHttpUtils(context);
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

	/**
	* @Description:下载音乐 
	* @param position
	* @return void 
	* @author bai
	*/
	protected void downloadMusic(final int position) {

		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的歌曲" + "/"
				+ netNewMusicList.get(position).getArtistsName() + " - "
				+ netNewMusicList.get(position).getName() + ".mp3";

		final String ImageTarget = Environment.getExternalStorageDirectory()
				+ "/" + "/下载的图片" + "/"
				+ netNewMusicList.get(position).getName() + ".jpg";
		final HttpUtils httpUtils = new HttpUtils();
		httpUtils.download(netNewMusicList.get(position).getMp3Url(),
				MusicTarget, new RequestCallBack<File>() {
					@Override
					public void onSuccess(ResponseInfo<File> arg0) {
						
						// 音乐下载成功后把图片也下载下来
						httpUtils.download(netNewMusicList.get(position)
								.getPicUrl(), ImageTarget,
								new RequestCallBack<File>() {
									@Override
									public void onSuccess(
											ResponseInfo<File> arg0) {
										if(loadingDialog.isShowing()&&loadingDialog!=null){
											Toast.makeText(context, "音乐下载成功", 0).show();
											loadingDialog.dismiss();
										}
									}

									@Override
									public void onFailure(HttpException arg0,
											String arg1) {
										Toast.makeText(context, arg1, 0).show();
									}
								});

					}

					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(context, "音乐下载失败" + arg1, 0).show();
					}

					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						super.onLoading(total, current, isUploading);
						
						if (loadingDialog == null) {
							loadingDialog = new LoadingDialog(context, R.style.dialogloading,
									new DialogListener() {

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
