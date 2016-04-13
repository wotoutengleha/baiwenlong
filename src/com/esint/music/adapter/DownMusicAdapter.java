package com.esint.music.adapter;

import java.util.ArrayList;

import com.esint.music.R;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.utils.Constant;
import com.esint.music.utils.SharedPrefUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DownMusicAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<DownMucicInfo> netMusicList;

	public DownMusicAdapter(Context context,
			ArrayList<DownMucicInfo> netMusicList) {
		this.context = context;
		this.netMusicList = netMusicList;
	}

	@Override
	public int getCount() {
		return netMusicList.size();
	}

	@Override
	public Object getItem(int position) {
		return netMusicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private ViewHolder holder;

	@SuppressLint("ResourceAsColor")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.ilv_downmusic, null);
			holder.musicName = (TextView) convertView
					.findViewById(R.id.musicname_down);
			holder.artist = (TextView) convertView
					.findViewById(R.id.artist_down);
			holder.musicDura = (TextView) convertView
					.findViewById(R.id.tv_duration);
			holder.arrow = (ImageView) convertView
					.findViewById(R.id.iv_arrowdown);
			holder.songNumber = (TextView) convertView
					.findViewById(R.id.tv_songnumber);
			holder.songTime = (TextView) convertView
					.findViewById(R.id.tv_duration);
			holder.ivRecord = (ImageView) convertView
					.findViewById(R.id.record_down);
			holder.listBackGround = (LinearLayout) convertView
					.findViewById(R.id.backgroundRecord_down);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		DownMucicInfo downMusicInfo = netMusicList.get(position);
		holder.musicName.setText(downMusicInfo.getDownMusicName());
		holder.artist.setText(downMusicInfo.getDownMusicArtist());
		holder.songTime.setText(downMusicInfo.getDownMusicDuration());
		holder.songNumber.setText(position + 1 + ".");

		int recordPosition = SharedPrefUtil.getInt(context,
				Constant.CLICKED_MUNSIC_NAME_DOWN, -1);
		if (recordPosition == position) {
			holder.ivRecord.setVisibility(View.VISIBLE);
			holder.listBackGround
					.setBackgroundResource(R.drawable.play_bar_bottom1_p);
		} else {
			holder.ivRecord.setVisibility(View.GONE);
			holder.listBackGround.setBackgroundColor(android.R.color.white);
		}
		return convertView;
	}

	class ViewHolder {
		TextView musicName;
		TextView artist;
		TextView musicDura;
		TextView songNumber;
		TextView songTime;
		ImageView arrow;
		ImageView ivRecord;
		LinearLayout listBackGround;
	}
}
