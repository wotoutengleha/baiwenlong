package com.esint.music.adapter;

import java.util.List;

import com.esint.music.R;
import com.esint.music.model.SearchData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**   
* 类名称：SearchHistoryAdapter   
* 类描述： 搜索listView的适配器  
* 创建人：bai   
* 创建时间：2016-3-28 上午9:46:11         
*/
public class SearchHistoryAdapter extends BaseAdapter {
	private Context mContext;
	private List<SearchData> lstHistory;

	public SearchHistoryAdapter(Context context, List<SearchData> lstHistory) {
		this.mContext = context;
		this.lstHistory = lstHistory;
	}

	@Override
	public int getCount() {
		return lstHistory == null ? 0 : lstHistory.size();
	}

	@Override
	public Object getItem(int position) {
		return lstHistory == null ? 0 : lstHistory.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_search_history, parent, false);
			holder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_content);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		SearchData data = lstHistory.get(position);
		holder.tvContent.setText(data.getContent());
		return convertView;
	}

	private class ViewHolder {
		TextView tvContent;
	}
}
