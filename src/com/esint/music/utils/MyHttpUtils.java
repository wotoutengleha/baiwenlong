package com.esint.music.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.esint.music.model.HotMusicInfo;
import com.esint.music.model.NewMusicInfo;
import com.esint.music.model.OriginalMusicInfo;
import com.esint.music.model.RiseMusicInfo;
import com.esint.music.model.SearchMusicInfo;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

/**   
* 类名称：MyHttpUtils   
* 类描述：请求服务器返回数据 判断网络状态   
* 创建人：bai   
* 创建时间：2016-3-3 上午9:03:09         
*/
public class MyHttpUtils {

	private Context context;
	private com.lidroid.xutils.HttpUtils httpUtils;
	public static Handler handler;
	private String aliasName;// 排行榜下解析数据的原唱歌手
	private String artistName;// 排行榜数据下接续数据的音乐演唱者

	private static final String COOKIE_APP_VERSION = "appver=2.6.1";
	private static final String HTTP_REFERER = "http://music.163.com";
	private static final String TAG = "MyHttpUtil";

	public MyHttpUtils(Context context) {
		this.context = context;
		httpUtils = new com.lidroid.xutils.HttpUtils();
//		handler = new Handler();
	}

	public static final int NETWORN_NONE = 0;
	public static final int NETWORN_WIFI = 1;
	public static final int NETWORN_MOBILE = 2;

	public static int getNetworkState(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		// Wifi
		State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		if (state == State.CONNECTED || state == State.CONNECTING) {
			return NETWORN_WIFI;
		}

		// 3G
		state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState();
		if (state == State.CONNECTED || state == State.CONNECTING) {
			return NETWORN_MOBILE;
		}
		return NETWORN_NONE;
	}

	/**
	 * 网络连接是否可用
	 */
	public boolean isConnnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null != connectivityManager) {
			NetworkInfo networkInfo[] = connectivityManager.getAllNetworkInfo();

			if (null != networkInfo) {
				for (NetworkInfo info : networkInfo) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						Log.e(TAG, "the net is ok");
						return true;
					}
				}
			}
		}
		return false;
	}

	private final int NETTYPE_WIFI = 0x01;
	private final int NETTYPE_CMWAP = 0x02;
	private final int NETTYPE_CMNET = 0x03;
	private SearchMusicInfo musicInfo;

	/**
	 * 获取当前网络类型
	 * 
	 * @return 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
	 */
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if (extraInfo != null && extraInfo.equals("")) {
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}

	/**
	 * @Description:请求新音乐的网络地址
	 * @param url
	 * @return void
	 * @author bai
	 */
	public void netNewMusicList(String url) {
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				Toast.makeText(context, "请求失败了" + arg0, 0).show();
				System.out.println(arg1);
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				System.out.println(arg0.result);

				try {
					parseNewMusicJson(arg0.result);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

		});

	}

	// 解析新歌榜的json数据
	private void parseNewMusicJson(String jsonResult) throws JSONException {
		ArrayList<NewMusicInfo> netNewMusicList = new ArrayList<NewMusicInfo>();
		JSONObject jsonObject = new JSONObject(jsonResult);
		String result = jsonObject.getString("result");
		JSONObject object = new JSONObject(result);
		String updateTime = object.getString("updateTime");// 更新时间
		String description = object.getString("description");// 描述
		String trackCount = object.getString("trackCount");// 歌曲总数
		String tracks = object.getString("tracks");// 歌曲
		// 保存起来
		SharedPrefUtil.setString(context, "hotMusicUpdateTime", updateTime);
		SharedPrefUtil.setString(context, "hotMusicDescription", description);
		SharedPrefUtil.setString(context, "hotMusicTrackCount", trackCount);
		JSONArray tracksArray = new JSONArray(tracks);
		for (int i = 0; i < tracksArray.length(); i++) {
			JSONObject object2 = (JSONObject) tracksArray.get(i);
			String mp3Url = object2.getString("mp3Url");// 歌曲地址
			String musicName = object2.getString("name");// 歌曲名字
			String duration = object2.getString("duration");// 歌曲时长
			String alias = object2.getString("alias");// 原唱 后边解析
			String artists = object2.getString("artists");// 歌手 后边解析
			String album = object2.getString("album");// 专辑 后边解析

			// 过滤没有原唱的标签
			if (alias.equals("[]")) {
				aliasName = "";
			} else {
				JSONArray aliasArray = new JSONArray(alias);
				for (int j = 0; j < aliasArray.length(); j++) {
					aliasName = aliasArray.getString(j);
				}
			}

			JSONArray artistsArray = new JSONArray(artists);
			for (int k = 0; k < artistsArray.length(); k++) {
				JSONObject artistsObject = artistsArray.getJSONObject(k);
				artistName = artistsObject.getString("name");
			}

			JSONObject albumObject = new JSONObject(album);
			String albumName = albumObject.getString("name");
			String picUrl = albumObject.getString("picUrl");

			NewMusicInfo netNewMusicInfo = new NewMusicInfo(mp3Url, musicName,
					duration, aliasName, artistName, albumName, trackCount,
					picUrl);
			netNewMusicList.add(netNewMusicInfo);
		}
		Message message = handler.obtainMessage(
				Constant.WHAT_NET_NEWMUSIC_LIST, netNewMusicList);
		message.sendToTarget();
	}

	/**
	 * @Description:加载网络热歌排行榜的url
	 * @param url
	 * @return void
	 * @author bai
	 */
	public void netHotMusicList(String url) {

		httpUtils.send(HttpMethod.GET, Constant.API_NET_HOTMUSIC_LIST,
				new RequestCallBack<String>() {

					@Override
					public void onFailure(HttpException arg0, String arg1) {

					}

					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						String hotMusicJson = arg0.result;
						try {
							parseHotMusicJson(hotMusicJson);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

	}

	/**
	 * @Description:解析数据，将数据通过handler返回
	 * @param jsonResult
	 * @throws JSONException
	 * @return void
	 * @author bai
	 */
	private void parseHotMusicJson(String jsonResult) throws JSONException {
		ArrayList<HotMusicInfo> netNewMusicList = new ArrayList<HotMusicInfo>();
		JSONObject jsonObject = new JSONObject(jsonResult);
		String result = jsonObject.getString("result");
		JSONObject object = new JSONObject(result);
		String updateTime = object.getString("updateTime");// 更新时间
		String description = object.getString("description");// 描述
		String trackCount = object.getString("trackCount");// 歌曲总数
		String tracks = object.getString("tracks");// 歌曲
		// 保存起来
		SharedPrefUtil.setString(context, "hotmusic_updateTime", updateTime);
		SharedPrefUtil.setString(context, "hotmusic_description", description);
		SharedPrefUtil.setString(context, "hotmusic_trackCount", trackCount);

		JSONArray tracksArray = new JSONArray(tracks);
		for (int i = 0; i < tracksArray.length(); i++) {
			JSONObject object2 = (JSONObject) tracksArray.get(i);
			String mp3Url = object2.getString("mp3Url");// 歌曲地址
			String musicName = object2.getString("name");// 歌曲名字
			String duration = object2.getString("duration");// 歌曲时长
			String alias = object2.getString("alias");// 原唱 后边解析
			String artists = object2.getString("artists");// 歌手 后边解析
			String album = object2.getString("album");// 专辑 后边解析

			// 过滤没有原唱的标签
			if (alias.equals("[]")) {
				aliasName = "";
			} else {
				JSONArray aliasArray = new JSONArray(alias);
				for (int j = 0; j < aliasArray.length(); j++) {
					aliasName = aliasArray.getString(j);
				}
			}

			JSONArray artistsArray = new JSONArray(artists);
			for (int k = 0; k < artistsArray.length(); k++) {
				JSONObject artistsObject = artistsArray.getJSONObject(k);
				artistName = artistsObject.getString("name");
			}

			JSONObject albumObject = new JSONObject(album);
			String albumName = albumObject.getString("name");
			String picUrl = albumObject.getString("picUrl");

			HotMusicInfo netNewMusicInfo = new HotMusicInfo(mp3Url, musicName,
					duration, aliasName, artistName, albumName, trackCount,
					picUrl);
			netNewMusicList.add(netNewMusicInfo);
		}
		Message message = handler.obtainMessage(
				Constant.WHAT_NET_NEWMUSIC_LIST, netNewMusicList);
		message.sendToTarget();
	}
	/**
	 * @Description:加载网络原创歌曲排行榜的url
	 * @param url
	 * @return void
	 * @author bai
	 */
	public void originalMusicList(String url) {

		httpUtils.send(HttpMethod.GET, Constant.API_NET_ORIGINALMUSIC_LIST,
				new RequestCallBack<String>() {

					@Override
					public void onFailure(HttpException arg0, String arg1) {

					}

					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						String originalMusicJson = arg0.result;
						try {
							parseOriginalMusicJson(originalMusicJson);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

	}
	
	/**
	 * @Description:解析原创歌曲榜数据，将数据通过handler返回
	 * @param jsonResult
	 * @throws JSONException
	 * @return void
	 * @author bai
	 */
	private void parseOriginalMusicJson(String jsonResult) throws JSONException {
		ArrayList<OriginalMusicInfo> originalMusicList = new ArrayList<OriginalMusicInfo>();
		JSONObject jsonObject = new JSONObject(jsonResult);
		String result = jsonObject.getString("result");
		JSONObject object = new JSONObject(result);
		String updateTime = object.getString("updateTime");// 更新时间
		String description = object.getString("description");// 描述
		String trackCount = object.getString("trackCount");// 歌曲总数
		String tracks = object.getString("tracks");// 歌曲
		// 保存起来
		SharedPrefUtil.setString(context, "originalmusic_updateTime", updateTime);
		SharedPrefUtil.setString(context, "originalmusic_description", description);
		SharedPrefUtil.setString(context, "originalmusic_trackCount", trackCount);

		JSONArray tracksArray = new JSONArray(tracks);
		for (int i = 0; i < tracksArray.length(); i++) {
			JSONObject object2 = (JSONObject) tracksArray.get(i);
			String mp3Url = object2.getString("mp3Url");// 歌曲地址
			String musicName = object2.getString("name");// 歌曲名字
			String duration = object2.getString("duration");// 歌曲时长
			String alias = object2.getString("alias");// 原唱 后边解析
			String artists = object2.getString("artists");// 歌手 后边解析
			String album = object2.getString("album");// 专辑 后边解析

			// 过滤没有原唱的标签
			if (alias.equals("[]")) {
				aliasName = "";
			} else {
				JSONArray aliasArray = new JSONArray(alias);
				for (int j = 0; j < aliasArray.length(); j++) {
					aliasName = aliasArray.getString(j);
				}
			}

			JSONArray artistsArray = new JSONArray(artists);
			for (int k = 0; k < artistsArray.length(); k++) {
				JSONObject artistsObject = artistsArray.getJSONObject(k);
				artistName = artistsObject.getString("name");
			}

			JSONObject albumObject = new JSONObject(album);
			String albumName = albumObject.getString("name");
			String picUrl = albumObject.getString("picUrl");

			OriginalMusicInfo originalMusicInfo = new OriginalMusicInfo(mp3Url, musicName,
					duration, aliasName, artistName, albumName, trackCount,
					picUrl);
			originalMusicList.add(originalMusicInfo);
		}
		Message message = handler.obtainMessage(
				Constant.WHAT_NET_ORIGINALMUSIC_LIST, originalMusicList);
		message.sendToTarget();
	}
	
	/**
	 * @Description:加载飙升歌曲排行榜的url
	 * @param url
	 * @return void
	 * @author bai
	 */
	public void riseMusicList(String url) {

		httpUtils.send(HttpMethod.GET, Constant.API_NET_RISEMUSIC_LIST,
				new RequestCallBack<String>() {

					@Override
					public void onFailure(HttpException arg0, String arg1) {

					}

					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						String riseMusicJson = arg0.result;
						try {
							parseRiseMusicJson(riseMusicJson);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

	}
	
	/**
	 * @Description:解析飙升歌曲榜数据，将数据通过handler返回
	 * @param jsonResult
	 * @throws JSONException
	 * @return void
	 * @author bai
	 */
	private void parseRiseMusicJson(String jsonResult) throws JSONException {
		ArrayList<RiseMusicInfo> riseMusicList = new ArrayList<RiseMusicInfo>();
		JSONObject jsonObject = new JSONObject(jsonResult);
		String result = jsonObject.getString("result");
		JSONObject object = new JSONObject(result);
		String updateTime = object.getString("updateTime");// 更新时间
		String description = object.getString("description");// 描述
		String trackCount = object.getString("trackCount");// 歌曲总数
		String tracks = object.getString("tracks");// 歌曲
		// 保存起来
		SharedPrefUtil.setString(context, "risemusic_updateTime", updateTime);
		SharedPrefUtil.setString(context, "risemusic_description", description);
		SharedPrefUtil.setString(context, "risemusic_trackCount", trackCount);

		JSONArray tracksArray = new JSONArray(tracks);
		for (int i = 0; i < tracksArray.length(); i++) {
			JSONObject object2 = (JSONObject) tracksArray.get(i);
			String mp3Url = object2.getString("mp3Url");// 歌曲地址
			String musicName = object2.getString("name");// 歌曲名字
			String duration = object2.getString("duration");// 歌曲时长
			String alias = object2.getString("alias");// 原唱 后边解析
			String artists = object2.getString("artists");// 歌手 后边解析
			String album = object2.getString("album");// 专辑 后边解析

			// 过滤没有原唱的标签
			if (alias.equals("[]")) {
				aliasName = "";
			} else {
				JSONArray aliasArray = new JSONArray(alias);
				for (int j = 0; j < aliasArray.length(); j++) {
					aliasName = aliasArray.getString(j);
				}
			}

			JSONArray artistsArray = new JSONArray(artists);
			for (int k = 0; k < artistsArray.length(); k++) {
				JSONObject artistsObject = artistsArray.getJSONObject(k);
				artistName = artistsObject.getString("name");
			}

			JSONObject albumObject = new JSONObject(album);
			String albumName = albumObject.getString("name");
			String picUrl = albumObject.getString("picUrl");

			RiseMusicInfo riseMusicInfo = new RiseMusicInfo(mp3Url, musicName,
					duration, aliasName, artistName, albumName, trackCount,
					picUrl);
			riseMusicList.add(riseMusicInfo);
		}
		Message message = handler.obtainMessage(
				Constant.WHAT_NET_RISEMUSIC_LIST, riseMusicList);
		message.sendToTarget();
	}


	/**
	 * @Description:通过网易云音乐搜索的接口搜索音乐 (输入歌名 或者演唱家)
	 * @return void
	 * @author bai
	 */
	public void searchMusicToAPI(final String url, List<NameValuePair> parmas) {
		httpUtils.configCurrentHttpCacheExpiry(1000 * 10);// 超时时间
		final RequestParams params = new RequestParams();
		params.addHeader("Cookie", COOKIE_APP_VERSION);
		params.addHeader("Referer", HTTP_REFERER);
		params.addQueryStringParameter(parmas);
		httpUtils.send(HttpMethod.POST, url, params,
				new RequestCallBack<String>() {

					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(context, "搜索音乐失败了", 0).show();
					}

					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						Log.e("成功的搜索了音乐", "成功的搜索了音乐");
						String searchResult = arg0.result;
						ParseSearchResultJson(searchResult);
					}
				});

	}

	/**
	 * @Description:解析搜索结果的api
	 * @param searchResult
	 * @return void
	 * @author bai
	 * @throws JSONException
	 */
	protected void ParseSearchResultJson(String searchResult) {
		String searchArtistName = null;
		ArrayList<SearchMusicInfo> searchMusicList = new ArrayList<SearchMusicInfo>();

		JSONObject object;
		try {
			object = new JSONObject(searchResult);
			String result = object.getString("result");
			JSONObject jsonObject = new JSONObject(result);
			String resultObject = jsonObject.getString("songs");
			JSONArray resultArray = new JSONArray(resultObject);
			for (int i = 0; i < resultArray.length(); i++) {
				musicInfo = new SearchMusicInfo();
				JSONObject musicObject = resultArray.getJSONObject(i);
				String musicID = musicObject.getString("id");
				String musicName = musicObject.getString("name");
				String artists = musicObject.getString("artists");

				JSONArray array = new JSONArray(artists);
				for (int j = 0; j < array.length(); j++) {
					JSONObject jsonObject1 = array.getJSONObject(j);
					searchArtistName = jsonObject1.getString("name");
				}
				musicInfo.setMusicArtist(searchArtistName);
				musicInfo.setMusicID(musicID);
				musicInfo.setMusicName(musicName);
				searchMusicList.add(musicInfo);
				Log.e("musicName", musicName);
				Log.e("成功的解析了音乐", "成功的解析了音乐");
			}

			Message message = handler.obtainMessage(
					Constant.WHAT_NET_SEARCH_LIST, searchMusicList);
			message.sendToTarget();

		} catch (JSONException e) {
			Message message = handler.obtainMessage(Constant.WHAT_EXECEPTION);
			message.sendToTarget();
			e.printStackTrace();
		}

	}

}
