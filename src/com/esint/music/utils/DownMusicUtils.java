package com.esint.music.utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.esint.music.model.SearchResult;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

/**
 * 类名称：DownMusicUtils 类描述：下载歌曲 创建人：bai 创建时间：2016-2-14 下午5:23:02
 */
public class DownMusicUtils {

	public static final int SUCCESS_LRC = 1;
	public static final int FAILED_LRC = 2;
	public static final int SUCCESS_MP3 = 3;
	public static final int FAILED_MP3 = 4;
	public static final int GET_SUCC_MP3URL = 5;
	public static final int GET_FAILED_MP3URL = 6;
	public static final int MUSIC_EXISTS = 7;
	private OnDownLoadListener mListener;
	private static DownMusicUtils mInstance;
	private ExecutorService mThreadPool;
	public static Handler handler;

	// 设置回调监听对象
	public DownMusicUtils setListener(OnDownLoadListener mListener) {
		this.mListener = mListener;
		return this;
	}

	// 获取下载的实例
	public synchronized static DownMusicUtils getInstance() {

		if (mInstance == null) {
			try {
				mInstance = new DownMusicUtils();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		return mInstance;
	}

	private DownMusicUtils() throws ParserConfigurationException {
		mThreadPool = Executors.newSingleThreadExecutor();
	}

	// 下载的具体业务的方法
	public void down(final SearchResult searchResult) {

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case SUCCESS_LRC:
					if (mListener != null)
						mListener.onDownLoad("歌词下载成功");
					break;
				case FAILED_LRC:
					if (mListener != null)
						mListener.onFailed("歌词下载失败");
					break;
				case GET_FAILED_MP3URL:
					if (mListener != null)
						mListener.onFailed("下载失败，该歌曲VIP才能下载");
					break;
				case SUCCESS_MP3:
					if (mListener != null)
						mListener.onDownLoad(searchResult.getMusicName()
								+ "已下载");
					String url = Constant.BAIDU_URL + searchResult.getUrl();
					Log.e("lrcurl", url);
					downLoadLRC(url, searchResult.getMusicName(), this);
					break;

				case FAILED_MP3:
					if (mListener != null)
						mListener
								.onFailed(searchResult.getMusicName() + "下载失败");
					break;
				}

			}
		};
		// getDownLoadMusicURL(searchResult, handler);
	}

	// private void getDownLoadMusicURL(final SearchResult searchResult,
	// final Handler handler) {
	// mThreadPool.execute(new Runnable() {
	//
	// private String url111;
	//
	// @Override
	// public void run() {
	// String url = searchResult.getUrl();
	// try {
	// Document doc = Jsoup.connect(url)
	// .userAgent(Constant.USER_AGENT).timeout(6000).get();
	// Elements targetElements = doc.select("div.songOther");
	// Document divcontions = Jsoup.parse(targetElements
	// .toString());
	// Elements element = divcontions.getElementsByTag("li");
	// for (int i = 0; i < element.size(); i++) {
	// String nextDownUrl = element.get(1).toString();
	// Document divcontions1 = Jsoup.parse(nextDownUrl
	// .toString());
	// Elements element1 = divcontions1.getElementsByTag("a");
	// url111 = element1.attr("href");
	// }
	//
	// String newUrl = Constant.ND + url111;
	// Document doc1 = Jsoup.connect(newUrl)
	// .userAgent(Constant.USER_AGENT).timeout(6000).get();
	// Elements targetElements1 = doc1.select("li.songOtherDown");
	// Document divcontions1 = Jsoup.parse(targetElements1
	// .toString());
	// Elements element1 = divcontions1.getElementsByTag("a");
	// String link = element1.attr("href");
	// Log.e("link", link + "");
	// Message message = handler.obtainMessage(GET_SUCC_MP3URL,
	// link);
	// message.sendToTarget();
	// } catch (IOException e) {
	// e.printStackTrace();
	// handler.obtainMessage(GET_FAILED_MP3URL).sendToTarget();
	// }
	// }
	// });
	//
	// }

	// 下载歌词
	public void downLoadLRC(final String url, final String musicName,
			final Handler handler) {
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Document doc = Jsoup.connect(url)
							.userAgent(Constant.USER_AGENT).timeout(6000).get();
					Elements lrcTag = doc.select("div.lyric-content");
					String lrcUrl = lrcTag.attr("data-lrclink");
					File lrcDirFile = new File(Environment
							.getExternalStorageDirectory() + Constant.LRC_MUSIC);
					if (!lrcDirFile.exists()) {
						lrcDirFile.mkdir();
					}
					lrcUrl = Constant.BAIDU_URL + lrcUrl;
					final String target = Environment
							.getExternalStorageDirectory()
							+ "/"
							+ "/下载的歌词"
							+ "/" + musicName + ".lrc";
					HttpUtils utils = new HttpUtils();
					utils.download(lrcUrl, target, new RequestCallBack<File>() {
						@Override
						public void onLoading(long total, long current,
								boolean isUploading) {
							super.onLoading(total, current, isUploading);
							System.out.println("current:" + current + "/"
									+ total);
						}

						@Override
						public void onSuccess(ResponseInfo<File> arg0) {
							Toast.makeText(MyApplication.getContext(),
									"歌词下载成功了", 0).show();
							handler.obtainMessage(SUCCESS_LRC, musicName)
									.sendToTarget();
							Log.e("target下载的时候", target);
						}

						@Override
						public void onFailure(HttpException arg0, String arg1) {
							Toast.makeText(MyApplication.getContext(), "下载失败",
									0).show();
						}
					});
				} catch (IOException e) {
					handler.obtainMessage(FAILED_LRC).sendToTarget();
					e.printStackTrace();
				}
			}
		});
	}

	public interface OnDownLoadListener {
		public void onDownLoad(String mp3Url);

		public void onFailed(String error);
	}
}
