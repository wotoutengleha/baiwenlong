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
import com.esint.music.model.RiseMusicInfo;
import com.esint.music.model.Mp3Info;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.model.NewMusicInfo;
import com.esint.music.model.RiseMusicInfo;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MyHttpUtils;
import com.esint.music.utils.SharedPrefUtil;
import com.esint.music.view.LoadingDialog;
import com.esint.music.view.LoadingDialog.DialogListener;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

/**   
* �����ƣ�RiseMusicListAdapter   
* ������������������б��������   
* �����ˣ�bai   
* ����ʱ�䣺2016-3-23 ����11:05:49         
*/
public class RiseMusicListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<RiseMusicInfo> riseMusicList;
	private LoadingDialog loadingDialog;

	public RiseMusicListAdapter(Context context,
			ArrayList<RiseMusicInfo> riseMusicList) {
		this.context = context;
		this.riseMusicList = riseMusicList;

	}

	@Override
	public int getCount() {
		return riseMusicList.size();
	}

	@Override
	public Object getItem(int position) {
		return riseMusicList.get(position);
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
		RiseMusicInfo netHotMusicInfo = riseMusicList.get(position);
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
		WindowManager m = ((Activity) context).getWindowManager();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		Display d = m.getDefaultDisplay(); // ��ȡ��Ļ������
		params.height = (int) (d.getHeight() * 0.286); // �߶�����Ϊ��Ļ��0.6
		params.width = (int) (d.getWidth() * 0.8); // �������Ϊ��Ļ��0.65
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
					Toast.makeText(context, "��ǰ����û������", 0).show();
					return;
				}
				// �ж���������
				int networkType = myHttpUtils.getNetworkType();
				Log.e("��ǰ��������", networkType + "");
				switch (networkType) {
				case 0:// ��������
					showNetAlert(position);
					break;
				case 1:// Wi-Fi����
					downloadMusic(position);
					break;

				}
			}
		});
	}
	
	/**
	* @Description:�ж��������� ��������״̬����ʾ 
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
				+ "/���صĸ���" + "/"
				+ riseMusicList.get(position).getArtistsName() + " - "
				+ riseMusicList.get(position).getName() + ".mp3";

		final String ImageTarget = Environment.getExternalStorageDirectory()
				+ "/" + "/���ص�ͼƬ" + "/"
				+ riseMusicList.get(position).getName() + ".jpg";
		final HttpUtils httpUtils = new HttpUtils();
		httpUtils.download(riseMusicList.get(position).getMp3Url(),
				MusicTarget, new RequestCallBack<File>() {
					@Override
					public void onSuccess(ResponseInfo<File> arg0) {
					
						// �������سɹ����ͼƬҲ��������
						httpUtils.download(riseMusicList.get(position)
								.getPicUrl(), ImageTarget,
								new RequestCallBack<File>() {

									@Override
									public void onSuccess(
											ResponseInfo<File> arg0) {
										if (loadingDialog.isShowing()
												&& loadingDialog != null) {
											Toast.makeText(context, "�������سɹ�", 0).show();
											loadingDialog.dismiss();
										}
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
						if (loadingDialog == null) {
							loadingDialog = new LoadingDialog(context,
									R.style.dialogloading,
									new DialogListener() {

										@Override
										public void onShowed() {
										}

										@Override
										public void onDismissed() {
										}
									});
						}
						loadingDialog.showDialog("�������أ����Ժ�~");
					}
				});

	}
}
