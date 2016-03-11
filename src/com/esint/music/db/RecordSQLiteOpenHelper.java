package com.esint.music.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**   
* 类名称：RecordSQLiteOpenHelper   
* 类描述： 搜索记录里的数据库  
* 创建人：bai   
* 创建时间：2016-3-11 下午2:49:59         
*/
public class RecordSQLiteOpenHelper extends SQLiteOpenHelper {

	  private static String name = "temp.db";
	  private static Integer version = 1;

	  public RecordSQLiteOpenHelper(Context context) {
	    super(context, name, null, version);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase db) {
	    db.execSQL("create table records(id integer primary key autoincrement,name varchar(200))");
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	  }

	}
