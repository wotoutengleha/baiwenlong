package com.esint.music.utils;

import android.content.Context;

/**   
* 类名称：CacheUtils   
* 类描述： 缓存工具类   缓存的内容是json数据
* 创建人：bai   
* 创建时间：2016-3-15 下午2:35:54         
*/
public class CacheUtils {

	/**
	* @Description:存取缓存  用URL地址作为key Json数据作为值 
	* @param key  URL
	* @param json  
	* @return void 
	* @author bai
	*/
	public static void setCache(Context context,String key,String value){
		SharedPrefUtil.setString(context, key, value);
	}
	
	//获取缓存
	public static String getCache(String key,Context context){
		return SharedPrefUtil.getString(context, key, null);
	}
}
