package com.esint.music.utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicAPI {

	public static final String SEARCH_GET_URL = "http://music.163.com/api/search/get";
	public static final String PLAYLIST_DETAIL_URL = "http://music.163.com/api/playlist/detail";
	public static final String SONG_DETAIL_URL = "http://music.163.com/api/song/detail";
	public static final String COOKIE_APP_VERSION = "appver=2.6.1";
	public static final String HTTP_REFERER = "http://music.163.com";
	public static final String[] audio_qualities = { "hMusic", "mMusic",
			"lMusic", "bMusic" };
	private static final String dlurl_format = "http://m1.music.126.net/%s/%s.mp3";
	private static final String picurl_format = "http://p%s.music.126.net/%s/%s.jpg";

	public static JSONObject searchSongs(final String name) {
		final JSONObject[] result = new JSONObject[1];
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost(SEARCH_GET_URL);
					httpPost.addHeader("Cookie", COOKIE_APP_VERSION);
					httpPost.addHeader("Referer", HTTP_REFERER);

					List<NameValuePair> querys = new ArrayList<NameValuePair>();
					querys.add(new BasicNameValuePair("s", name));
					querys.add(new BasicNameValuePair("type", "1")); // type 1
																		// means
																		// searching
																		// songs
					querys.add(new BasicNameValuePair("offset", "0"));
					querys.add(new BasicNameValuePair("sub", "false"));
					querys.add(new BasicNameValuePair("limit", "20"));
					httpPost.setEntity(new UrlEncodedFormEntity(querys,
							HTTP.UTF_8));

					HttpResponse httpResponse = httpClient.execute(httpPost);
					JSONObject response = new JSONObject(EntityUtils
							.toString(httpResponse.getEntity()));
					result[0] = response.getJSONObject("result");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		try {
			thread.join();
			return result[0];
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject searchArtist(final String name) {
		final JSONObject[] result = new JSONObject[1];
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost(SEARCH_GET_URL);
					httpPost.addHeader("Cookie", COOKIE_APP_VERSION);
					httpPost.addHeader("Referer", HTTP_REFERER);

					List<NameValuePair> querys = new ArrayList<NameValuePair>();
					querys.add(new BasicNameValuePair("s", name));
					querys.add(new BasicNameValuePair("type", "100")); // type 1
																		// means
																		// searching
																		// songs
					querys.add(new BasicNameValuePair("offset", "0"));
					querys.add(new BasicNameValuePair("sub", "false"));
					querys.add(new BasicNameValuePair("limit", "20"));
					httpPost.setEntity(new UrlEncodedFormEntity(querys,
							HTTP.UTF_8));

					HttpResponse httpResponse = httpClient.execute(httpPost);
					JSONObject response = new JSONObject(EntityUtils
							.toString(httpResponse.getEntity()));
					result[0] = response.getJSONObject("result");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		try {
			thread.join();
			return result[0];
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject searchAlbum(final String name) {
		final JSONObject[] result = new JSONObject[1];
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost(SEARCH_GET_URL);
					httpPost.addHeader("Cookie", COOKIE_APP_VERSION);
					httpPost.addHeader("Referer", HTTP_REFERER);

					List<NameValuePair> querys = new ArrayList<NameValuePair>();
					querys.add(new BasicNameValuePair("s", name));
					querys.add(new BasicNameValuePair("type", "10")); // type 1
																		// means
																		// searching
																		// songs
					querys.add(new BasicNameValuePair("offset", "0"));
					querys.add(new BasicNameValuePair("sub", "false"));
					querys.add(new BasicNameValuePair("limit", "20"));
					httpPost.setEntity(new UrlEncodedFormEntity(querys,
							HTTP.UTF_8));

					HttpResponse httpResponse = httpClient.execute(httpPost);
					JSONObject response = new JSONObject(EntityUtils
							.toString(httpResponse.getEntity()));
					result[0] = response.getJSONObject("result");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		try {
			thread.join();
			return result[0];
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* for example: ID 60198 Billboard Charts */
	public static JSONObject getPlaylistDetails(final String id) {
		final JSONObject[] result = new JSONObject[1];
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpClient = new DefaultHttpClient();
					HttpGet httpGet = new HttpGet(PLAYLIST_DETAIL_URL + "?id="
							+ id);

					HttpResponse httpResponse = httpClient.execute(httpGet);
					JSONObject response = new JSONObject(
							EntityUtils.toString(httpResponse.getEntity()));
					result[0] = response.getJSONObject("result");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		try {
			thread.join();
			return result[0];
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getSongUrl(final String id) {
		final String[] dfsid = { null };
		final String[] result = new String[1];
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpClient = new DefaultHttpClient();

					List<NameValuePair> querys = new ArrayList<NameValuePair>();
					querys.add(new BasicNameValuePair("ids", "[" + id + "]"));
					HttpGet httpGet = new HttpGet(SONG_DETAIL_URL + "?"
							+ URLEncodedUtils.format(querys, "utf-8"));
					httpGet.addHeader("Cookie", COOKIE_APP_VERSION);
					httpGet.addHeader("Referer", HTTP_REFERER);

					HttpResponse httpResponse = httpClient.execute(httpGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						JSONObject jsonObject = new JSONObject(EntityUtils
								.toString(httpResponse.getEntity()));
						/*
						 * try "hMusic" than "mMusic" than "lMusic" than
						 * "bMusic"
						 */
						for (String quality : audio_qualities) {
							/*
							 * pick the best quality we found and save it in a
							 * new temporary array
							 */
							JSONObject jsonTmp = new JSONObject(jsonObject
									.getJSONArray("songs").getJSONObject(0)
									.getString(quality));
							if (jsonTmp != null) {
								/* Search for dfsId */
								final String dfsid = jsonTmp.getString("dfsId");
								/* Decrypt dfsId */
								final String encrypted_dfsid = encrypt_dfsId(dfsid);
								/* Build the final URL */
								result[0] = String.format(dlurl_format,
										encrypted_dfsid, dfsid);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		try {
			thread.join();
			return result[0];
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getPictureUrl(final String id) {
		final String[] result = new String[1];
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				final String encrypted_dfsid = encrypt_dfsId(id);
				result[0] = String.format(picurl_format, getRandome(),
						encrypted_dfsid, id);
			}
		});
		thread.start();
		try {
			thread.join();
			return result[0];
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Thx http://moonlib.com/606.html ,
	 * https://github.com/sk1418/zhuaxia/blob/master/zhuaxia/netease.py and
	 * https://github.com/PeterDing/iScript/blob/master/music.163.com.py
	 * Additional thanks to user "cryzed" :) Thx jdownloader for their java
	 * based plugin
	 * 
	 * Usage: hMusic -> dfsId = 2900511674786860 encrypt_dfsId(2900511674786860)
	 * Result: 1A-XOhu8BlTz9Yeso5t3vg== MP3 File:
	 * 1A-XOhu8BlTz9Yeso5t3vg==/2900511674786860.mp3
	 */
	public static String encrypt_dfsId(String dfsid) {
		String result = "";
		byte[] byte1 = "3go8&$8*3*3h0k(2)2".getBytes();
		byte[] byte2 = dfsid.getBytes();
		final int byte1_len = byte1.length;
		for (int i = 0; i < byte2.length; i++) {
			byte2[i] = (byte) (byte2[i] ^ byte1[i % byte1_len]);
		}
		byte[] md5bytes = new byte[0];
		try {
			md5bytes = MessageDigest.getInstance("MD5").digest(byte2);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		final String b64 = Base64.encodeToString(md5bytes, Base64.DEFAULT);
		/* cleanup */
		result = b64.replace("/", "_");
		result = result.replace("+", "-");
		/* remove linebreak */
		result = result.replace("\n", "").replace("\r", "");
		return result;
	}

	public static String getRandome() {
		Random rand = new Random();
		int min = 1;
		int max = 4;
		String num = String.valueOf(rand.nextInt(max - min + 1) + min);
		return num;
	}

}