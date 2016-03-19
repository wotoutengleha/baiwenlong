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
* 类名称：MySQLite   
* 类描述： 我的数据库操作类 用来存储喜欢的音乐 
* 创建人：bai   
* 创建时间：2016-3-18 上午9:47:40         
*/
public class MySQLite extends SQLiteOpenHelper {

	// 创建表 BLOB字段是用来存储图片的

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
		Log.e("数据库创建成功", "数据库创建成功");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	// 将当前的图片转换成字节数组
	public static byte[] img(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	// 将字节数组转换成bitmap
	public static Bitmap getBmp(byte[] in) {
		Bitmap bmpout = BitmapFactory.decodeByteArray(in, 0, in.length);
		return bmpout;
	}
}
