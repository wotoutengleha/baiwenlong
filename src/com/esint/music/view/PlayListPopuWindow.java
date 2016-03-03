package com.esint.music.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.esint.music.R;
import com.esint.music.model.Mp3Info;
import com.esint.music.utils.MediaUtils;

public class PlayListPopuWindow implements OnItemClickListener {

	private ArrayList<Mp3Info> mp3List;
	private PopupWindow popupWindow;
	private ListView mListView;
	private LayoutInflater inflater;
	private Context context;
	private OnItemClickListener listener;

	public PlayListPopuWindow(Context context) {

		this.context = context;
		mp3List = MediaUtils.getMp3Info(context);

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.play_list_popupwindow, null);
		TextView playList = (TextView) view.findViewById(R.id.playList);
		playList.setText("≤•∑≈¡–±Ì("+mp3List.size()+")" );
		mListView = (ListView) view.findViewById(R.id.listView1);
		mListView.setOnItemClickListener(this);
		mListView.setAdapter(new PopAdapter());
		popupWindow = new PopupWindow(view, 800,
				1000);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (listener != null) {
			listener.onItemClick(position);
		}
		popupWindow.dismiss();
	}

	public interface OnItemClickListener {
		public void onItemClick(int index);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	public void showAsDropDown(View parent) {

		popupWindow.showAsDropDown(parent);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.update();
	}
	private final class PopAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return mp3List.size();
		}

		@Override
		public Object getItem(int position) {
			return mp3List.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.ilv_play_list_popupwindow, null);
				holder = new ViewHolder();
				convertView.setTag(holder);
				holder.musicNumber = (TextView) convertView
						.findViewById(R.id.musicNumber);
				holder.musicName = (TextView) convertView
						.findViewById(R.id.popupMusicname);
				holder.musicSinger = (TextView) convertView
						.findViewById(R.id.popupSinger);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.musicNumber.setText(position+"");
			holder.musicName.setText(mp3List.get(position).getTitle());
			holder.musicSinger.setText(mp3List.get(position).getArtist());
			Log.e("111111", mp3List.get(position).getArtist());
			return convertView;
		}

		private final class ViewHolder {
			TextView musicNumber;
			TextView musicName;
			TextView musicSinger;
			
		}
	}
}
