package com.esint.music.adapter;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.esint.music.utils.SharedPrefUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

public class NewMusicListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<NewMusicInfo> netNewMusicList;

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

	// չʾ�������ֵĶԻ���
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

	protected void downloadMusic(final int position) {

		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/���صĸ���" + "/"
				+ netNewMusicList.get(position).getArtistsName() + " - "
				+ netNewMusicList.get(position).getName() + ".mp3";

		final String ImageTarget = Environment.getExternalStorageDirectory()
				+ "/" + "/���ص�ͼƬ" + "/"
				+ netNewMusicList.get(position).getName() + ".jpg";
		final HttpUtils httpUtils = new HttpUtils();
		httpUtils.download(netNewMusicList.get(position).getMp3Url(),
				MusicTarget, new RequestCallBack<File>() {
					@Override
					public void onSuccess(ResponseInfo<File> arg0) {
						Toast.makeText(context, "�������سɹ�", 0).show();
						// �������سɹ����ͼƬҲ��������
						httpUtils.download(netNewMusicList.get(position)
								.getPicUrl(), ImageTarget,
								new RequestCallBack<File>() {

									@Override
									public void onSuccess(
											ResponseInfo<File> arg0) {
										Toast.makeText(context, "ͼƬ���سɹ�", 0)
												.show();
									}

									@Override
									public void onFailure(HttpException arg0,
											String arg1) {
										Toast.makeText(context, "ͼƬ����ʧ��", 0)
												.show();
									}
								});

					}

					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(context, "��������ʧ��", 0).show();
						Log.e("����ʧ��", arg1);

					}

					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						super.onLoading(total, current, isUploading);
						Log.e("current", current + "  /  " + total);
					}
				});

	}
}
