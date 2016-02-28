package com.esint.music.utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v23Frame;
import org.jaudiotagger.tag.id3.ID3v23Tag;

import com.esint.music.R;
import com.esint.music.model.DownImageInfo;
import com.esint.music.model.Mp3Info;
import com.esint.music.model.DownMucicInfo;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

public class MediaUtils {

	private static List<String> lstFile = new ArrayList<String>();
	// ��ȡר�������uri

	private static final Uri albumArtUri = Uri
			.parse("content://media/external/audio/albumart");

	/*
	 * @Description:���ݸ���id��ѯ��������Ϣ ��ѯ������ģʽ
	 * 
	 * @author bai
	 */
	public static Mp3Info getMp3Info(Context context, long _id) {

		Cursor cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
				MediaStore.Audio.Media._ID + "=" + _id, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

		Mp3Info mp3Info = null;

		if (cursor.moveToNext()) {
			mp3Info = new Mp3Info();
			long id = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media._ID));// ���ֵ�id
			String title = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.TITLE));// ���ֵı���
			String artist = cursor.getString((cursor
					.getColumnIndex(MediaStore.Audio.Media.ARTIST)));// ���ֵĸ���
			String album = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.ALBUM));// ���ֵ�ר��
			long albumId = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));// ר����ID
			long duration = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media.DURATION));// ��ȡ������ʱ��
			long size = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media.SIZE));// ��ȡ�����Ĵ�С
			String url = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.DATA));// ��ȡ������·��
			int isMusic = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));// �Ƿ�Ϊ����
			
			// ��������ӵ�������
			if (isMusic == 0) {
				mp3Info.setId(id);
				mp3Info.setTitle(title);
				mp3Info.setArtist(artist);
				mp3Info.setAlbum(album);
				mp3Info.setAlbumId(albumId);
				mp3Info.setDuration(duration);
				mp3Info.setSize(size);
				mp3Info.setUrl(url);
				mp3Info.setIsMusic(isMusic);
			}
		}
		cursor.close();
		return mp3Info;
	}

	/*
	 * 
	 * @Description: ��ѯ������id
	 * 
	 * @param context
	 * 
	 * @return
	 * 
	 * @author bai
	 */
	public static long[] getMp3InfoId(Context context) {

		Cursor cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media._ID },
				MediaStore.Audio.Media.DURATION + ">=180000", null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

		long[] ids = null;
		if (cursor != null) {

			ids = new long[cursor.getCount()];
			for (int i = 0; i < cursor.getCount(); i++) {

				cursor.moveToNext();
				ids[i] = cursor.getLong(0);
			}

		}
		cursor.close();
		return ids;
	}

	/*
	 * 
	 * @Description:��ѯ������Ϣ �����ڼ�����
	 * 
	 * @return
	 * 
	 * @author bai
	 */
	public static ArrayList<Mp3Info> getMp3Info(Context context) {

		Cursor cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
				MediaStore.Audio.Media.DURATION + ">=180000", null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

		ArrayList<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext();
			Mp3Info mp3Info = new Mp3Info();

			long id = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media._ID));// ���ֵ�id
			String title = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.TITLE));// ���ֵı���
			String artist = cursor.getString((cursor
					.getColumnIndex(MediaStore.Audio.Media.ARTIST)));// ���ֵĸ���
			String album = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.ALBUM));// ���ֵ�ר��
			long albumId = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));// ר����ID
			long duration = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media.DURATION));// ��ȡ������ʱ��
			long size = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media.SIZE));// ��ȡ�����Ĵ�С
			String url = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.DATA));// ��ȡ������·��
			int isMusic = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));// �Ƿ�Ϊ����

			if (artist.equals("<unknown>")) {
				artist = "";
			} else {
				// ��������ӵ�������
				if (isMusic == 0) {
					mp3Info.setId(id);
					mp3Info.setTitle(title);
					mp3Info.setArtist(artist);
					mp3Info.setAlbum(album);
					mp3Info.setAlbumId(albumId);
					mp3Info.setDuration(duration);
					mp3Info.setSize(size);
					mp3Info.setUrl(url);
					mp3Info.setIsMusic(isMusic);
					mp3Infos.add(mp3Info);
				}
			}
		}
		cursor.close();
		return mp3Infos;
	}

	/*
	 * @Description: ��list���������Map���ݶ���ÿһ��Map���������һ�����ֵ���������
	 * 
	 * @param mp3Info
	 * 
	 * @return
	 * 
	 * @author bai
	 */
	public static List<HashMap<String, String>> getMusicMap3(
			List<Mp3Info> mp3Infos) {

		List<HashMap<String, String>> mp3List = new ArrayList<HashMap<String, String>>();
		for (Iterator<Mp3Info> iterator = mp3Infos.iterator(); iterator
				.hasNext();) {
			Mp3Info mp3Info = iterator.next();
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("title", mp3Info.getTitle());
			map.put("Artist", mp3Info.getArtist());
			map.put("album", mp3Info.getAlbum());
			map.put("albumId", mp3Info.getAlbumId() + "");
			map.put("duration", formatTime(mp3Info.getDuration()));
			map.put("size", mp3Info.getSize() + "");
			map.put("url", mp3Info.getUrl());
		}
		return mp3List;
	}

	/*
	 * @Description:ת��ʱ��ĸ�ʽ ������ת���ɷֺ���
	 * 
	 * @param time
	 * 
	 * @return
	 * 
	 * @author bai
	 */
	public static String formatTime(long time) {
		String min = time / (1000 * 60) + "";
		String sec = time % (1000 * 60) + "";
		if (min.length() < 2) {
			min = "0" + time / (1000 * 60) + "";
		} else {
			min = time / (1000 * 60) + "";
		}
		if (sec.length() == 4) {
			sec = "0" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 3) {
			sec = "00" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 2) {
			sec = "000" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 1) {
			sec = "0000" + (time % (1000 * 60)) + "";
		}
		return min + ":" + sec.trim().substring(0, 2);
	}

	/**
	 * ��ȡĬ��ר��ͼƬ
	 * 
	 * @param context
	 * @return
	 */
	public static Bitmap getDefaultArtwork(Context context, boolean small) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		if (small) { // ����СͼƬ
			return BitmapFactory.decodeStream(context.getResources()
					.openRawResource(R.drawable.notification_default_cover),
					null, opts);
		}
		return BitmapFactory.decodeStream(context.getResources()
				.openRawResource(R.drawable.notification_default_cover), null,
				opts);
	}

	/**
	 * ���ļ����л�ȡר������λͼ
	 * 
	 * @param context
	 * @param songid
	 * @param albumid
	 * @return
	 */
	private static Bitmap getArtworkFromFile(Context context, long songid,
			long albumid) {
		Bitmap bm = null;
		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException(
					"Must specify an album or a song id");
		}
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			FileDescriptor fd = null;
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/"
						+ songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			} else {
				Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			}
			options.inSampleSize = 1;
			// ֻ���д�С�ж�
			options.inJustDecodeBounds = true;
			// ���ô˷����õ�options�õ�ͼƬ��С
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			// ���ǵ�Ŀ������800pixel�Ļ�������ʾ
			// ������Ҫ����computeSampleSize�õ�ͼƬ���ŵı���
			options.inSampleSize = 100;
			// ���ǵõ������ŵı��������ڿ�ʼ��ʽ����Bitmap����
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			// ����options��������������Ҫ���ڴ�
			bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bm;
	}

	/**
	 * ��ȡר������λͼ����
	 * 
	 * @param context
	 * @param song_id
	 * @param album_id
	 * @param allowdefalut
	 * @return
	 */
	public static Bitmap getArtwork(Context context, long song_id,
			long album_id, boolean allowdefalut, boolean small) {
		if (album_id < 0) {
			if (song_id < 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}
			if (allowdefalut) {
				return getDefaultArtwork(context, small);
			}
			return null;
		}
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				BitmapFactory.Options options = new BitmapFactory.Options();
				// ���ƶ�ԭʼ��С
				options.inSampleSize = 1;
				// ֻ���д�С�ж�
				options.inJustDecodeBounds = true;
				// ���ô˷����õ�options�õ�ͼƬ�Ĵ�С
				BitmapFactory.decodeStream(in, null, options);
				/** ���ǵ�Ŀ��������N pixel�Ļ�������ʾ�� ������Ҫ����computeSampleSize�õ�ͼƬ���ŵı��� **/
				/** �����targetΪ800�Ǹ���Ĭ��ר��ͼƬ��С�����ģ�800ֻ�ǲ������ֵ���������������Ľ�� **/
				if (small) {
					options.inSampleSize = computeSampleSize(options, 40);
				} else {
					options.inSampleSize = computeSampleSize(options, 600);
				}
				// ���ǵõ������ű��������ڿ�ʼ��ʽ����Bitmap����
				options.inJustDecodeBounds = false;
				options.inDither = false;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				in = res.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, options);
			} catch (FileNotFoundException e) {
				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefalut) {
							return getDefaultArtwork(context, small);
						}
					}
				} else if (allowdefalut) {
					bm = getDefaultArtwork(context, small);
				}
				return bm;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * ��ͼƬ���к��ʵ�����
	 * 
	 * @param options
	 * @param target
	 * @return
	 */
	public static int computeSampleSize(Options options, int target) {
		int w = options.outWidth;
		int h = options.outHeight;
		int candidateW = w / target;
		int candidateH = h / target;
		int candidate = Math.max(candidateW, candidateH);
		if (candidate == 0) {
			return 1;
		}
		if (candidate > 1) {
			if ((w > target) && (w / candidate) < target) {
				candidate -= 1;
			}
		}
		if (candidate > 1) {
			if ((h > target) && (h / candidate) < target) {
				candidate -= 1;
			}
		}
		return candidate;
	}

	public static ArrayList<DownMucicInfo> GetMusicFiles(String Path,
			String Extension, boolean IsIterative) // ����Ŀ¼����չ�����Ƿ�������ļ���
	{

		ArrayList<DownMucicInfo> netMusicList = new ArrayList<DownMucicInfo>();
		File[] files = new File(Path).listFiles();

		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isFile()) {
				if (f.getPath()
						.substring(f.getPath().length() - Extension.length())
						.equals(Extension)) // �ж���չ��
					lstFile.add(f.getPath());
				String wholeName = f.getName();// ����ļ�������
				String target = Environment.getExternalStorageDirectory() + "/"
						+ "/���صĸ���" + "/" + wholeName;
				String[] whole = wholeName.split("-");
				String artist = whole[0];
				String musicName = whole[1];
				musicName = musicName.substring(0, musicName.lastIndexOf('.'));
				DownMucicInfo netMucicInfo = new DownMucicInfo();

				File sourceFile = new File(target);
				try {
					MP3File mp3file = new MP3File(sourceFile);
					MP3AudioHeader header = mp3file.getMP3AudioHeader();
					if (header == null) {
						return null;
					}
					// ����ʱ��
					String durationStr = header.getTrackLengthAsString();
					netMucicInfo.setDownMusicDuration(durationStr);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TagException e) {
					e.printStackTrace();
				} catch (ReadOnlyFileException e) {
					e.printStackTrace();
				} catch (InvalidAudioFrameException e) {
					e.printStackTrace();
				}

				String fileSize = getFileSize(files[i].length());

				netMucicInfo.setDownMusicName(musicName);
				netMucicInfo.setDownMusicArtist(artist);
				netMucicInfo.setDownMusicSize(fileSize);
				netMucicInfo.setDownMusicUrl(target);
				netMusicList.add(netMucicInfo);

				if (!IsIterative)
					break;
			} else if (f.isDirectory() && f.getPath().indexOf("/.") == -1) // ���Ե��ļ��������ļ�/�ļ��У�
				GetMusicFiles(f.getPath(), Extension, IsIterative);
		}
		return netMusicList;
	}

	// �õ��ƶ�Ŀ¼�µ�ͼƬ
	public static ArrayList<DownImageInfo> GetImagFiles(String Path,
			String Extension, boolean IsIterative) // ����Ŀ¼����չ�����Ƿ�������ļ���
	{
		ArrayList<DownImageInfo> imageList = new ArrayList<DownImageInfo>();
		File[] files = new File(Path).listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isFile()) {
				if (f.getPath()
						.substring(f.getPath().length() - Extension.length())
						.equals(Extension)) // �ж���չ��
					lstFile.add(f.getPath());
				String wholeName = f.getName();// ����ļ�������
				String target = Environment.getExternalStorageDirectory() + "/"
						+ "/���ص�ͼƬ" + "/" + wholeName;
				DownImageInfo downImageInfo = new DownImageInfo();
				downImageInfo.setDownImagePath(target);
				imageList.add(downImageInfo);
			}
		}
		return imageList;
	}

	/**
	 * �����ļ��Ĵ�С��������ص�m�ַ���
	 * 
	 * @param fileS
	 * @return
	 */
	public static String getFileSize(long fileS) {// ת���ļ���С
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * ��ȡ��������
	 * 
	 * @param trackLengthAsString
	 * @return
	 */
	public static long getTrackLength(String trackLengthAsString) {

		if (trackLengthAsString.contains(":")) {
			String temp[] = trackLengthAsString.split(":");
			if (temp.length == 2) {
				int m = Integer.parseInt(temp[0]);// ��
				int s = Integer.parseInt(temp[1]);// ��
				int currTime = (m * 60 + s) * 1000;
				return currTime;
			}
		}
		return 0;
	}
}
