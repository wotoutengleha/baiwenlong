package com.esint.music.utils;



import android.content.Context;
import android.content.SharedPreferences;

/*   
 *    
 * 类名称：ShredPrefUtil   
 * 类描述：保存状态的工具类   
 * 创建人：bai 
 * 创建时间：2016-1-12 上午10:48:09   
 *        
 */
public class SharedPrefUtil {

	private Context context;

	public SharedPrefUtil(Context context) {
		this.context = context;
	}
	//保存歌曲
	public static String getString(Context context, String key, String value) {
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		return sp.getString(key, value);
	}

	public static void setString(Context context, String key, String value) {
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().putString(key, value).commit();

	}
	
	

	public static boolean getBoolean(Context context, String key,
			boolean defaultVaule) {
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		return sp.getBoolean(key, defaultVaule);
	}

	public static void setBoolean(Context context, String key, boolean value) {

		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().putBoolean(key, value).commit();

	}

	
	public static int getInt(Context context, String key, int value) {
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		return sp.getInt(key, value);
	}

	public static void setInt(Context context, String key, int value) {
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().putInt(key, value).commit();

	}
	public static void clearSP(Context context){
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().clear().commit();
	}
	// 保存背景图片
	public void saveImgPath(String path) {
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().putString(Constant.BACK_IMG, path).commit();
	}
	public String getImgPath(){
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		return sp.getString(Constant.BACK_IMG, "");
	}
	//保存设置里摇一摇的状态
	public void saveShake (Context context,boolean shake){
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().putBoolean(Constant.SP_SHAKE_CHANGE_SONG, shake).commit();
	}
	//获取设置里摇一摇的状态
	public boolean getShake(Context context,boolean shake){
		SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME,
				Context.MODE_PRIVATE);
		return sp.getBoolean(Constant.SP_SHAKE_CHANGE_SONG, false);
	}
	
}
