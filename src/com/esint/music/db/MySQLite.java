package com.esint.music.db;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**   
* �����ƣ�MySQLite   
* �������� �ҵ����ݿ������ �����洢ϲ�������� 
* �����ˣ�bai   
* ����ʱ�䣺2016-3-18 ����9:47:40         
*/
public class MySQLite extends SQLiteOpenHelper {

	// ������ BLOB�ֶ��������洢ͼƬ��

	public static final String CREATE_MUSIC = "create table Music ("
			+ "id integer primary key autoincrement, " + "MusicTitle text, "
			+ "MusicArtist text, " + "MusicTime text, " + "MusicUrl text, "
			+ "MusicImg BLOB)";

	public MySQLite(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_MUSIC);
		Log.e("���ݿⴴ���ɹ�", "���ݿⴴ���ɹ�");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	// ����ǰ��ͼƬת�����ֽ�����
	public static byte[] img(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	// ���ֽ�����ת����bitmap
	public static Bitmap getBmp(byte[] in) {
		Bitmap bmpout = BitmapFactory.decodeByteArray(in, 0, in.length);
		return bmpout;
	}
}
