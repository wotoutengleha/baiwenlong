package com.esint.music.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.parsers.ParserConfigurationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.esint.music.model.SearchResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

public class SearchMusicUtil {

	private static final int size = 20;
	private static final String URL = Constant.BAIDU_URL
			+ Constant.BAIDU_SEARCH;
	private static SearchMusicUtil sInatance;
	private onSearchResultListener mListener;
	private ExecutorService mThreadPool;

	public synchronized static SearchMusicUtil getInstance() {

		if (sInatance == null) {
			try {
				sInatance = new SearchMusicUtil();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		return sInatance;
	}

	private SearchMusicUtil() throws ParserConfigurationException {
		mThreadPool = Executors.newSingleThreadExecutor();
	}

	public SearchMusicUtil setListener(onSearchResultListener listener) {
		mListener = listener;
		return this;
	}

	public void search(final String key, final int page) {
		final Handler handler = new Handler(MyApplication.getContext()
				.getMainLooper()) {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case Constant.SUCCESS:
					if (mListener != null) {
						mListener
								.onSearchResult((ArrayList<SearchResult>) msg.obj);
					}
					break;
				case Constant.FAILED:
					if (mListener != null) {
						mListener.onSearchResult(null);
					}
					break;
				}
			}
		};
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				ArrayList<SearchResult> results = getMusicList(key, page);
				if (results == null) {
					handler.sendEmptyMessage(Constant.FAILED);

				}
				handler.obtainMessage(Constant.SUCCESS, results).sendToTarget();
			}
		});
	}

	protected ArrayList<SearchResult> getMusicList(String key, int page) {

		final String start = String.valueOf((page - 1) * size);
		Document doc;
		try {
			doc = Jsoup
					.connect(URL)
					.data("key", key, "start", start, "size",
							String.valueOf(size))
					.userAgent(Constant.USER_AGENT).timeout(60 * 1000).get();

			Elements songTitles = doc.select("div.song-item.clearfix");
			Elements songInfos;
			ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();
			TAG: for (Element song : songTitles) {
				songInfos = song.getElementsByTag("a");
				SearchResult searchResult = new SearchResult();
				for (Element info : songInfos) {
					// 收费的歌曲
					if (info.attr("href")
							.startsWith("http://y.baidu.com/song/")) {
						continue TAG;

					}
					// 跳转到百度音乐盒的歌曲
					if (info.attr("href").equals("#")
							&& !TextUtils.isEmpty(info.attr("data-songdata"))) {
						continue TAG;
					}
					// 歌曲链接
					if (info.attr("href").startsWith("/song")) {
						searchResult.setMusicName(info.text());
						searchResult.setUrl(info.attr("href"));
					}
					// 歌手链接
					if (info.attr("href").startsWith("/data")) {
						searchResult.setArtist(info.text());
					}
					// 专辑链接
					if (info.attr("href").startsWith("/album")) {
						searchResult
								.setAlbum(info.text().replaceAll("《|》", ""));
					}
				}
				searchResults.add(searchResult);
			}
			return searchResults;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public interface onSearchResultListener {
		public void onSearchResult(ArrayList<SearchResult> results);
	}
}
