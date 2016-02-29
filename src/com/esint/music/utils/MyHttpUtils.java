package com.esint.music.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.esint.music.model.HotMusicInfo;
import com.esint.music.model.NewMusicInfo;
import com.esint.music.model.SearchMusicInfo;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.http.client.entity.BodyParamsEntity;

/**
 * �����ƣ�HttpUtils �������� ����json���� ����list �����ˣ�bai ����ʱ�䣺2016-2-19 ����4:56:42
 */
public class MyHttpUtils {

	private Context context;
	private com.lidroid.xutils.HttpUtils httpUtils;
	public static Handler handler;
	private String aliasName;// ���а��½������ݵ�ԭ������
	private String artistName;// ���а������½������ݵ������ݳ���

	public static final String COOKIE_APP_VERSION = "appver=2.6.1";
	public static final String HTTP_REFERER = "http://music.163.com";

	public MyHttpUtils(Context context) {
		this.context = context;
		httpUtils = new com.lidroid.xutils.HttpUtils();
		handler = new Handler();
	}

	/**
	 * @Description:���������ֵ������ַ
	 * @param url
	 * @return void
	 * @author bai
	 */
	public void netNewMusicList(String url) {
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				Toast.makeText(context, "����ʧ����" + arg0, 0).show();
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

	// �����¸���json����
	private void parseNewMusicJson(String jsonResult) throws JSONException {
		ArrayList<NewMusicInfo> netNewMusicList = new ArrayList<NewMusicInfo>();
		JSONObject jsonObject = new JSONObject(jsonResult);
		String result = jsonObject.getString("result");
		JSONObject object = new JSONObject(result);
		String updateTime = object.getString("updateTime");// ����ʱ��
		String description = object.getString("description");// ����
		String trackCount = object.getString("trackCount");// ��������
		String tracks = object.getString("tracks");// ����
		// ��������
		SharedPrefUtil.setString(context, "updateTime", updateTime);
		SharedPrefUtil.setString(context, "description", description);
		SharedPrefUtil.setString(context, "trackCount", trackCount);

		JSONArray tracksArray = new JSONArray(tracks);
		for (int i = 0; i < tracksArray.length(); i++) {
			JSONObject object2 = (JSONObject) tracksArray.get(i);
			String mp3Url = object2.getString("mp3Url");// ������ַ
			String musicName = object2.getString("name");// ��������
			String duration = object2.getString("duration");// ����ʱ��
			String alias = object2.getString("alias");// ԭ�� ��߽���
			String artists = object2.getString("artists");// ���� ��߽���
			String album = object2.getString("album");// ר�� ��߽���

			// ����û��ԭ���ı�ǩ
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
	 * @Description:���������ȸ����а��url
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
						Log.e("hotMusicJson", hotMusicJson);
						try {
							parseHotMusicJson(hotMusicJson);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

	}

	/**
	 * @Description:�������ݣ�������ͨ��handler����
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
		String updateTime = object.getString("updateTime");// ����ʱ��
		String description = object.getString("description");// ����
		String trackCount = object.getString("trackCount");// ��������
		String tracks = object.getString("tracks");// ����
		// ��������
		SharedPrefUtil.setString(context, "hotmusic_updateTime", updateTime);
		SharedPrefUtil.setString(context, "hotmusic_description", description);
		SharedPrefUtil.setString(context, "hotmusic_trackCount", trackCount);

		JSONArray tracksArray = new JSONArray(tracks);
		for (int i = 0; i < tracksArray.length(); i++) {
			JSONObject object2 = (JSONObject) tracksArray.get(i);
			String mp3Url = object2.getString("mp3Url");// ������ַ
			String musicName = object2.getString("name");// ��������
			String duration = object2.getString("duration");// ����ʱ��
			String alias = object2.getString("alias");// ԭ�� ��߽���
			String artists = object2.getString("artists");// ���� ��߽���
			String album = object2.getString("album");// ר�� ��߽���

			// ����û��ԭ���ı�ǩ
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
	 * @Description:ͨ�����������������Ľӿ��������� (������� �����ݳ���)
	 * @return void
	 * @author bai
	 */
	public void searchMusicToAPI(final String url, List<NameValuePair> parmas) {
		httpUtils.configCurrentHttpCacheExpiry(1000 * 10);// ��ʱʱ��
		final RequestParams params = new RequestParams();
		params.addHeader("Cookie", COOKIE_APP_VERSION);
		params.addHeader("Referer", HTTP_REFERER);
		params.addQueryStringParameter(parmas);
		httpUtils.send(HttpMethod.POST, url, params,
				new RequestCallBack<String>() {

					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(context, "��������ʧ����", 0).show();
					}

					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						String searchResult = arg0.result;
						try {
							ParseSearchResultJson(searchResult);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

	}

	/**
	 * @Description:�������������api
	 * @param searchResult
	 * @return void
	 * @author bai
	 * @throws JSONException
	 */
	protected void ParseSearchResultJson(String searchResult)
			throws JSONException {
		String searchArtistName = null;
		ArrayList<SearchMusicInfo> searchMusicList = new ArrayList<SearchMusicInfo>();

		JSONObject object = new JSONObject(searchResult);
		String result = object.getString("result");
		JSONObject jsonObject = new JSONObject(result);
		String resultObject = jsonObject.getString("songs");
		JSONArray resultArray = new JSONArray(resultObject);
		for (int i = 0; i < resultArray.length(); i++) {
			SearchMusicInfo musicInfo = new SearchMusicInfo();
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
		}

		Message message = handler.obtainMessage(
				Constant.WHAT_NET_HOTMUSIC_LIST, searchMusicList);
		message.sendToTarget();
	}

}
