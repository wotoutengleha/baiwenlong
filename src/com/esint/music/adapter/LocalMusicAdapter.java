package com.esint.music.adapter;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import android.R.color;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.activity.MainFragmentActivity;
import com.esint.music.activity.ScanMusicActivity;
import com.esint.music.activity.ScanMusicActivity.ScanSdReceiver;
import com.esint.music.db.MySQLite;
import com.esint.music.dialog.Effectstype;
import com.esint.music.dialog.NiftyDialogBuilder;
import com.esint.music.fragment.MyTabMusic;
import com.esint.music.model.Mp3Info;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.SharedPrefUtil;

public class LocalMusicAdapter extends BaseAdapter {
	private List<Mp3Info> list = null;
	private Context mContext;
	private SharedPreferences sp;
	private Effectstype effect;
	private NiftyDialogBuilder dialogBuilder;

	public LocalMusicAdapter(Context mContext, List<Mp3Info> list) {
		this.mContext = mContext;
		this.list = list;
		sp = mContext.getSharedPreferences("ARROW", Context.MODE_PRIVATE);
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * 
	 * @param list
	 */
	public void updateListView(List<Mp3Info> list) {
		this.list = list;

		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	private ViewHolder viewHolder;
	private SharedPreferences spf;

	public View getView(final int position, View view, ViewGroup arg2) {
		final Mp3Info mContent = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(
					R.layout.sortlistview_item, null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.title);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			viewHolder.tvSongName = (TextView) view.findViewById(R.id.songname);
			viewHolder.ivRecord = (ImageView) view.findViewById(R.id.record);
			viewHolder.listBackGround = (LinearLayout) view
					.findViewById(R.id.backgroundRecord);
			viewHolder.lvArrowDown = (ImageView) view
					.findViewById(R.id.arrowDown);
			viewHolder.lvArrowUp = (ImageView) view.findViewById(R.id.arrowUp);
			viewHolder.menuLayout = (LinearLayout) view
					.findViewById(R.id.arrow_like_ll);
			viewHolder.ivLikeNormal = (ImageView) view
					.findViewById(R.id.ivLikeNormal);
			viewHolder.ivLikePress = (ImageView) view
					.findViewById(R.id.ivLikePress);
			viewHolder.ivDelete = (LinearLayout) view
					.findViewById(R.id.ll_arrow_delete);
			viewHolder.ivInfo = (LinearLayout) view
					.findViewById(R.id.ll_arrow_info);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		// 根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);

		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if (position == getPositionForSection(section)) {
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			viewHolder.tvLetter.setText(mContent.getSortLetters());
		} else {
			viewHolder.tvLetter.setVisibility(View.GONE);
		}

		int recordPosition = SharedPrefUtil.getInt(mContext,
				Constant.CLICKED_MUNSIC_NAME, -1);
		if (recordPosition == position) {
			viewHolder.ivRecord.setVisibility(View.VISIBLE);
			viewHolder.listBackGround
					.setBackgroundResource(R.drawable.play_bar_bottom1_p);
		} else {
			viewHolder.ivRecord.setVisibility(View.GONE);
			viewHolder.listBackGround.setBackgroundColor(color.white);
		}

		viewHolder.lvArrowDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sp.edit().putInt("ARROW", position).commit();
				notifyDataSetChanged();
			}
		});
		viewHolder.lvArrowUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sp.edit().clear().commit();
				notifyDataSetChanged();
			}
		});
		viewHolder.ivLikeNormal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String musicTitle = list.get(position).getTitle();
				String MusicArtist = list.get(position).getArtist();
				String musicTime = list.get(position).getDuration() + "";
				String MusicUrl = list.get(position).getUrl();
				// 找到当前播放音乐的大图 将图片转换成字节数组插入到数据库表
				Bitmap bitmap = MediaUtils.getArtwork(mContext,
						list.get(position).getId(), list.get(position)
								.getAlbumId(), true, false);
				byte[] imgByte = MySQLite.img(bitmap);
				ContentValues values = new ContentValues();
				values.put("MusicTitle", musicTitle);
				values.put("MusicArtist", MusicArtist);
				values.put("MusicTime", musicTime);
				values.put("MusicUrl", MusicUrl);
				values.put("MusicImg", imgByte);
				MainFragmentActivity.db.insert("Music", null, values);
				Log.e("本地音乐插入数据库成功", "本地插入数据库成功");
				Toast.makeText(mContext, "添加喜欢", 0).show();
				viewHolder.ivLikeNormal.setVisibility(View.GONE);
				viewHolder.ivLikePress.setVisibility(View.VISIBLE);
				notifyDataSetChanged();
			}
		});
		viewHolder.ivLikePress.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String musicTitle = list.get(position).getTitle();
				String whereClause = "MusicTitle=?";
				String[] whereArgs = { musicTitle };
				MainFragmentActivity.db.delete("Music", whereClause, whereArgs);
				Toast.makeText(mContext, "取消喜欢", 0).show();
				viewHolder.ivLikeNormal.setVisibility(View.VISIBLE);
				viewHolder.ivLikePress.setVisibility(View.GONE);
				Message message = MyTabMusic.mHandler.obtainMessage(
						Constant.NEXT_LEKE_MUSIC, musicTitle);
				message.sendToTarget();
				sp.edit().clear().commit();
				notifyDataSetChanged();
			}
		});
		viewHolder.ivDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				chooseDialog(position);
			}
		});
		viewHolder.ivInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				musicInfo(position);
			}
		});

		int inflatPosition = sp.getInt("ARROW", -1);
		if (inflatPosition == position) {
			viewHolder.menuLayout.setVisibility(View.VISIBLE);
			viewHolder.lvArrowUp.setVisibility(View.VISIBLE);
			viewHolder.lvArrowDown.setVisibility(View.GONE);

		} else {
			viewHolder.menuLayout.setVisibility(View.GONE);
			viewHolder.lvArrowDown.setVisibility(View.VISIBLE);
			viewHolder.lvArrowUp.setVisibility(View.GONE);

		}
		SharedPreferences spf = mContext.getSharedPreferences("LIKE",
				Context.MODE_PRIVATE);
		boolean isLikeMusic = isLikeMusic(list.get(position).getTitle());
		if (isLikeMusic == true) {
			viewHolder.ivLikePress.setVisibility(View.VISIBLE);
			viewHolder.ivLikeNormal.setVisibility(View.GONE);
		} else {
			viewHolder.ivLikePress.setVisibility(View.GONE);
			viewHolder.ivLikeNormal.setVisibility(View.VISIBLE);
		}

		viewHolder.tvTitle.setText(this.list.get(position).getArtist());
		viewHolder.tvSongName.setText(this.list.get(position).getTitle());

		return view;
	}

	final static class ViewHolder {
		TextView tvLetter;
		TextView tvTitle;
		TextView tvSongName;
		ImageView ivRecord;// 左边是竖线
		ImageView lvArrowDown;// 点击出来布局的箭头
		ImageView lvArrowUp;// 向上的箭头
		LinearLayout listBackGround;// 当前选中的歌曲背景
		LinearLayout menuLayout;// 出来的布局
		ImageView ivLikeNormal;// 喜欢按钮
		ImageView ivLikePress;// 喜欢红色
		LinearLayout ivDelete;// 删除按钮
		LinearLayout ivInfo;// 信息按钮
	}

	// 删除歌曲的dialog
	private void chooseDialog(final int position) {
		effect = Effectstype.Shake;
		dialogBuilder = new NiftyDialogBuilder(mContext, R.style.dialog_untran);
		dialogBuilder
				.withTitle("确定要删除这首歌吗？")
				.withTitleColor("#FFFFFF")
				.withDividerColor("#97E8F9")
				.withMessage(
						list.get(position).getArtist() + " - "
								+ list.get(position).getTitle())
				.withMessageColor("#FFFFFF").isCancelableOnTouchOutside(true)
				.withEffect(effect).withButton1Text("确定").withButton2Text("取消")
				.setCustomView(R.layout.custom_view, mContext)
				.setButton1Click(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialogBuilder.dismiss();
						String musicUrl = list.get(position).getUrl();
						new File(musicUrl).delete();
						Toast.makeText(mContext, "删除成功", 0).show();
						sp.edit().clear().commit();
						ScanMusicActivity.deleteMusic(position, list.get(position).getId());
						list.remove(position);
//						scanSdCard();
						notifyDataSetChanged();
					}
				}).setButton2Click(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialogBuilder.dismiss();
					}
				}).show();
	}

	private void scanSdCard() {
		String MusicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/qqmusic" + "/song";
		Log.e("MusicTarget", MusicTarget + "");
		IntentFilter intentfilter = new IntentFilter(
				Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentfilter.addDataScheme("file");
		ScanSdReceiver scanSdReceiver = new ScanSdReceiver();
		mContext.registerReceiver(scanSdReceiver, intentfilter);
		mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
				.parse("file://" + MusicTarget)));
	}

	

	// 歌曲信息的dialog
	private void musicInfo(int position) {
		View dialogView = View.inflate(mContext, R.layout.dialog_musicinfo,
				null);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		final AlertDialog dialog = builder.create();
		// 一定在show之前调用
		dialog.setView(dialogView, 0, 0, 0, 0);
		dialog.show();
		// 不能使用setContentView方法 不然没办法响应点击事件
		// dialog.setContentView(R.layout.musicinfo_dialog);
		LinearLayout dismissBtn = (LinearLayout) dialogView
				.findViewById(R.id.ll_dismiss);
		TextView musinName = (TextView) dialog.findViewById(R.id.musicNameInfo);
		TextView artistInfo = (TextView) dialog.findViewById(R.id.artistInfo);
		TextView musicTimeInfo = (TextView) dialog
				.findViewById(R.id.musicTimeInfo);
		TextView musicSizeInfo = (TextView) dialog
				.findViewById(R.id.musicSizeInfo);
		TextView musicPathInfo = (TextView) dialog
				.findViewById(R.id.musicPathInfo);
		musinName.setText(list.get(position).getTitle());
		artistInfo.setText(list.get(position).getArtist());
		musicTimeInfo.setText(MediaUtils.formatTime(list.get(position)
				.getDuration()));
		// 转换成M 保留两位小数
		DecimalFormat df2 = new DecimalFormat("##.00");
		String formatSize = df2.format(list.get(position).getSize()
				/ (1024.00 * 1024.00));
		musicSizeInfo.setText(formatSize + "M");
		musicPathInfo.setText(list.get(position).getUrl());
		dismissBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}
		return -1;
	}

	// 判断当前音乐是否为喜欢的音乐
	private boolean isLikeMusic(final String isPlayMusicTitle) {
		final Cursor cursor = MainFragmentActivity.db.query("Music", null,
				null, null, null, null, null);// 查询并获得游标
		if (cursor.moveToFirst()) {
			do {
				String musicTitle = cursor.getString(cursor
						.getColumnIndex("MusicTitle"));
				if (isPlayMusicTitle.trim().equals(musicTitle.trim())) {
					cursor.close();
					Constant.isInsert = true;
					Log.e("不断查询", "不断查询");
					return true;
				}
			} while (cursor.moveToNext());
		}
		Constant.isInsert = false;
		return false;
	}

}