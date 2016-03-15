package com.esint.music.view;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.esint.music.R;
import com.esint.music.model.DownMucicInfo;
import com.esint.music.utils.MediaUtils;

/**   
* 类名称：PlayListDownPopu   
* 类描述： 下载的音乐的播放列表  
* 创建人：bai   
* 创建时间：2016-3-15 下午8:47:21         
*/
public class PlayListDownPopu implements android.widget.AdapterView.OnItemClickListener {

	private ArrayList<DownMucicInfo> downMp3List;
	private PopupWindow popupWindow;
	private ListView mListView;
	private LayoutInflater inflater;
	private Context context;
	private OnItemClickListener listener;
	private PopAdapter popAdapter;
	private String musicTarget;// 下载歌曲的文件夹

	public PlayListDownPopu(Context context, int posi) {

		this.context = context;

		initData(posi);
	}

	private void initData(int posi) {
		musicTarget = Environment.getExternalStorageDirectory() + "/"
				+ "/下载的歌曲";
		downMp3List = MediaUtils.GetMusicFiles(musicTarget, ".mp3", true);

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.play_list_popupwindow, null);
		TextView playList = (TextView) view.findViewById(R.id.playList);
		playList.setText("播放列表(" + downMp3List.size() + ")");
		mListView = (ListView) view.findViewById(R.id.listView1);
		mListView.setOnItemClickListener(this);
		popAdapter = new PopAdapter();
		mListView.setAdapter(popAdapter);
		popAdapter.setSelectItem(posi);
		popupWindow = new PopupWindow(view, 800, 1000);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (listener != null) {
			listener.onItemClick(position);
			popAdapter.notifyDataSetChanged();
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

	public final class PopAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return downMp3List.size();
		}

		private int selectItem = -1;

		public void setSelectItem(int selectItem) {
			this.selectItem = selectItem;
		}

		@Override
		public Object getItem(int position) {
			return downMp3List.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("ResourceAsColor")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.ilv_play_list_popupwindow, null);
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
			holder.musicNumber.setText(position + 1 + "");
			holder.musicName.setText(downMp3List.get(position).getDownMusicName());
			holder.musicSinger.setText(downMp3List.get(position).getDownMusicArtist());

			if (position == selectItem) {
				holder.musicNumber.setTextColor(context.getResources()
						.getColor(R.color.gorgeous));
				holder.musicName.setTextColor(context.getResources().getColor(
						R.color.gorgeous));
				holder.musicSinger.setTextColor(context.getResources()
						.getColor(R.color.gorgeous));
			} else {
				holder.musicNumber.setTextColor(context.getResources()
						.getColor(R.color.white));
				holder.musicName.setTextColor(context.getResources().getColor(
						R.color.white));
				holder.musicSinger.setTextColor(context.getResources()
						.getColor(R.color.white));
			}

			return convertView;
		}

		public final class ViewHolder {
			public TextView musicNumber;
			public TextView musicName;
			public TextView musicSinger;

		}
	}
}
