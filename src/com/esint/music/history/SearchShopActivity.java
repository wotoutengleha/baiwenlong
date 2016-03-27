package com.esint.music.history;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import name.teze.layout.lib.SwipeBackActivity;

import com.esint.music.R;
import com.esint.music.XListView.XListView;
import com.esint.music.XListView.XListView.IXListViewListener;
import com.esint.music.activity.MainFragmentActivity;
import com.esint.music.adapter.SearchResultAdapter;
import com.esint.music.model.SearchMusicInfo;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MyHttpUtils;
import com.esint.music.view.CustomProgressDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SearchShopActivity extends SwipeBackActivity implements
		OnClickListener {
	private EditText etSearch;
	private ImageView ivDeleteSearch;
	private TextView tvSearch;
	private TextView tvHistory;

	private CustomerListView lvHistory;
	private Button btnClearHistory;

	private static final String SEARCH_HISTORY = "search_history";
	private SharedPreferences sp;
	private SearchHistoryAdapter mAdapter;
	private List<SearchData> lstHistory;
	private List<SearchData> lstAllHistory;
	private XListView resultLV;
	private boolean isRefresh = true;
	private MyHttpUtils myHttpUtils;// 请求网络的工具类
	private Context context;
	private ArrayList<SearchMusicInfo> searchMusicList;
	private CustomProgressDialog customProgressDialog;
	private SearchResultAdapter resultAdapter;
	private RelativeLayout scrollView;
	private LinearLayout XlistViewLL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search);
		initViews();
		initData();
		setListener();
	}

	private void initViews() {
		tvSearch = (TextView) findViewById(R.id.tv_search);
		ivDeleteSearch = (ImageView) findViewById(R.id.iv_delete_search);
		etSearch = (EditText) findViewById(R.id.et_search);
		tvHistory = (TextView) findViewById(R.id.tv_history);
		lvHistory = (CustomerListView) findViewById(R.id.lv_history);
		btnClearHistory = (Button) findViewById(R.id.btn_clear_history);
		resultLV = (XListView) findViewById(R.id.xlv_search);
		scrollView = (RelativeLayout) findViewById(R.id.all);
		XlistViewLL = (LinearLayout) findViewById(R.id.xlv_ll);
		resultLV.setPullRefreshEnable(true);
		resultLV.setPullLoadEnable(true);
	}

	private void initData() {
		myHttpUtils = new MyHttpUtils(this);
		context = SearchShopActivity.this;
		searchMusicList = new ArrayList<SearchMusicInfo>();
		sp = getSharedPreferences(SEARCH_HISTORY, 0);
		lstAllHistory = new ArrayList<SearchData>();
		lstHistory = new ArrayList<SearchData>();
		readSearchHistory();
		lstHistory.addAll(lstAllHistory);
		mAdapter = new SearchHistoryAdapter(this, lstHistory);
		lvHistory.setAdapter(mAdapter);
		Log.i("TEST", "长度---" + lstHistory.size());
		if (lstHistory.size() < 1) {
			lvHistory.setVisibility(View.GONE);
			btnClearHistory.setVisibility(View.GONE);
		}

		resultLV.setXListViewListener(new IXListViewListener() {

			@Override
			public void onRefresh() {
				isRefresh = true;
				// 判断是否有网络
				final boolean isConnected = myHttpUtils.isConnnected(context);
				if (isConnected == false) {
					Toast.makeText(context, "当前网络没有连接网络", 0).show();
					resultLV.stopRefresh();
					return;
				}
				updateSearchMusic(0);
			}

			@Override
			public void onLoadMore() {
				isRefresh = false;
				// 判断是否有网络
				final boolean isConnected = myHttpUtils.isConnnected(context);
				if (isConnected == false) {
					Toast.makeText(context, "当前网络没有连接网络", 0).show();
					resultLV.stopLoadMore();
					return;
				}
				updateSearchMusic(searchMusicList.size());

			}
		});
	}

	// 更新搜索音乐的列表
	@SuppressWarnings("deprecation")
	private void updateSearchMusic(int offset) {

		String edSearch = etSearch.getText().toString().trim();
		List<NameValuePair> parmas = new ArrayList<NameValuePair>();
		parmas.add(new BasicNameValuePair("s", edSearch));
		parmas.add(new BasicNameValuePair("type", "1"));
		parmas.add(new BasicNameValuePair("offset", offset + ""));
		parmas.add(new BasicNameValuePair("sub", "false"));
		parmas.add(new BasicNameValuePair("limit", "8"));
		myHttpUtils.searchMusicToAPI(Constant.API_NET_SEARCH_MUSIC, parmas);
	}

	private void setListener() {
		ivDeleteSearch.setOnClickListener(this);
		tvSearch.setOnClickListener(this);
		btnClearHistory.setOnClickListener(this);

		lvHistory.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				SearchData data = (SearchData) mAdapter.getItem(position);
				etSearch.setText(data.getContent());
				tvSearch.performClick();
			}
		});
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				performFiltering(s);
				if (s.length() > 0) {
					ivDeleteSearch.setVisibility(View.VISIBLE);
					tvSearch.setText("搜索");
					tvHistory.setText("猜你想搜");
				} else {
					ivDeleteSearch.setVisibility(View.INVISIBLE);
					tvSearch.setText("取消");
					tvHistory.setText("历史记录");
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.iv_delete_search:// 清空搜索内容
			etSearch.setText("");
			ivDeleteSearch.setVisibility(View.INVISIBLE);
			XlistViewLL.setVisibility(View.GONE);
			scrollView.setVisibility(View.VISIBLE);
			break;
		case R.id.tv_search:// 搜索按钮
			String searchContent = etSearch.getText().toString().trim();
			if (TextUtils.isEmpty(searchContent)) {
				SearchShopActivity.this.finish();
			} else {
				saveSearchHistory(searchContent);
				readSearchHistory();
				if (lstHistory.size() > 0) {
					lstHistory.clear();
				}
				lstHistory.addAll(lstAllHistory);
				mAdapter.notifyDataSetChanged();
				lvHistory.setVisibility(View.VISIBLE);
				btnClearHistory.setVisibility(View.VISIBLE);
				XlistViewLL.setVisibility(View.VISIBLE);
				scrollView.setVisibility(View.GONE);
				// 判断是否有网络
				final boolean isConnected = myHttpUtils.isConnnected(context);
				String searchResult = etSearch.getText().toString().trim();
				if (TextUtils.isEmpty(searchResult)) {
					showEmptyDialog();
				} else {
					if (isConnected == false) {
						Toast.makeText(context, "当前没有连接网络", 0).show();
						return;
					}
					// progressDialog.setMessage("正在搜索，请稍后。。。");

					if (customProgressDialog == null) {
						customProgressDialog = CustomProgressDialog
								.createDialog(context);
					}

					customProgressDialog.show();

					searchMusicList.clear();
					updateSearchMusic(0);
				}

				MyHttpUtils.handler = new Handler() {

					@SuppressWarnings("unchecked")
					@Override
					public void handleMessage(Message msg) {
						super.handleMessage(msg);
						if (msg.what == Constant.WHAT_NET_HOTMUSIC_LIST) {
							onLoadEnd(resultLV);
							if (customProgressDialog != null
									&& customProgressDialog.isShowing()) {
								customProgressDialog.dismiss();
							}
							if (isRefresh == true) {
								searchMusicList.clear();
								searchMusicList
										.addAll((ArrayList<SearchMusicInfo>) msg.obj);
								resultAdapter = new SearchResultAdapter(
										context, searchMusicList);
								resultLV.setAdapter(resultAdapter);
							} else {
								ArrayList<SearchMusicInfo> dataLoadMore = ((ArrayList<SearchMusicInfo>) msg.obj);
								searchMusicList.addAll(dataLoadMore);
								resultAdapter.notifyDataSetChanged();
							}
						} else if (msg.what == Constant.WHAT_EXECEPTION) {
							Toast.makeText(context, "没有更多数据了", 0).show();
							resultLV.stopLoadMore();
						}
					}
				};
			}
			break;
		case R.id.btn_clear_history:// 清除历史记录
			clearAllSearchHistory();
			readSearchHistory();
			if (lstHistory.size() > 0) {
				lstHistory.clear();
			}
			lstHistory.addAll(lstAllHistory);
			mAdapter.notifyDataSetChanged();
			lvHistory.setVisibility(View.GONE);
			btnClearHistory.setVisibility(View.GONE);
			Toast.makeText(SearchShopActivity.this, "清空历史记录成功",
					Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}

	/**
	 * 列表上拉下拉事件结束和初始化
	 */
	public void onLoadEnd(XListView lv) {
		Date now = new Date();
		DateFormat d1 = DateFormat.getDateTimeInstance();
		lv.stopRefresh();
		lv.stopLoadMore();
		lv.setRefreshTime(d1.format(now));
	}

	/**
	 * 匹配过滤搜索内容
	 * 
	 * @param prefix
	 *            输入框中输入的内容
	 */
	public void performFiltering(CharSequence inputContent) {
		if (TextUtils.isEmpty(inputContent)) {// 搜索框内容为空的时候显示所有历史记录
			if (lstHistory.size() > 0) {
				lstHistory.clear();
			}
			lstHistory.addAll(lstAllHistory);
		} else {
			String inputContentString = inputContent.toString().toLowerCase();
			int count = lstAllHistory.size();
			List<SearchData> lstFilterHistory = new ArrayList<SearchData>(count);
			for (int i = 0; i < count; i++) {
				String value = lstAllHistory.get(i).getContent();
				String valueText = value.toLowerCase();
				if (valueText.contains(inputContentString)) {// 包含输入的内容
					// 这个和下面的判断根据设计来做选择
				}
				if (valueText.startsWith(inputContentString)) {// 以输入的内容开头
					lstFilterHistory
							.add(new SearchData().setContent(valueText));
				} else {// 判断有空格的搜索内容中某一段是否包含输入内容开头的
					String[] words = valueText.split(" ");
					int wordCount = words.length;
					for (int k = 0; k < wordCount; k++) {
						if (words[k].startsWith(inputContentString)) {
							lstFilterHistory.add(new SearchData()
									.setContent(value));
							break;
						}
					}
				}
				// if (mMaxMatch > 0) {// 过滤后的历史记录只显示mMaxMatch条
				// if (newValues.size() > mMaxMatch - 1) {
				// break;
				// }
				// }
			}
			if (lstHistory.size() > 0) {
				lstHistory.clear();
			}
			lstHistory.addAll(lstFilterHistory);
		}
		mAdapter.notifyDataSetChanged();
		if (lstHistory.size() < 1) {
			lvHistory.setVisibility(View.GONE);
			btnClearHistory.setVisibility(View.GONE);
		} else {
			lvHistory.setVisibility(View.VISIBLE);
			btnClearHistory.setVisibility(View.VISIBLE);
		}
	}

	/*
	 * 保存搜索记录
	 */
	private void saveSearchHistory(String searchContent) {
		String longhistory = sp.getString(SEARCH_HISTORY, "");
		String[] tmpHistory = longhistory.split(",");

		List<String> lstHistory = new ArrayList<String>(
				Arrays.asList(tmpHistory));
		if (lstHistory.size() > 0) {// 移除历史，添加新数据
			for (int i = 0; i < lstHistory.size(); i++) {
				if (searchContent.equals(lstHistory.get(i))) {
					lstHistory.remove(i);
					break;
				}
			}
			lstHistory.add(0, searchContent);
		}
		if (lstHistory.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < lstHistory.size(); i++) {
				sb.append(lstHistory.get(i) + ",");
			}
			sp.edit().putString(SEARCH_HISTORY, sb.toString()).commit();
		} else {// 添加新数据
			sp.edit().putString(SEARCH_HISTORY, searchContent + ",").commit();
		}
	}

	/**
	 * 读取历史搜索记录
	 */
	public void readSearchHistory() {
		if (lstAllHistory.size() > 0) {
			lstAllHistory.clear();
		}
		String longhistory = sp.getString(SEARCH_HISTORY, "");
		if (TextUtils.isEmpty(longhistory)) {
			return;
		}
		String[] hisArrays = longhistory.split(",");
		for (int i = 0; i < hisArrays.length; i++) {
			lstAllHistory.add(new SearchData().setContent(hisArrays[i]));
		}
	}

	/**
	 * 清空全部历史搜索记录
	 */
	public void clearAllSearchHistory() {
		Editor editor = sp.edit();
		editor.clear();
		editor.commit();
	}

	// 关键字为空的时候弹出来的dialog
	private void showEmptyDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();
		View emptyDialog = View.inflate(this, R.layout.dialog_empty, null);
		LinearLayout dismissLL = (LinearLayout) emptyDialog
				.findViewById(R.id.dismiss_ll);
		dialog.show();
		WindowManager m = getWindowManager();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
		params.height = (int) (d.getHeight() * 0.24); // 高度设置为屏幕的0.6
		params.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.65
		dialog.getWindow().setAttributes(params);
		dialog.setContentView(emptyDialog);
		dismissLL.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

}
