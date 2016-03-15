package com.esint.music.utils;

import android.content.Context;

/**   
* �����ƣ�CacheUtils   
* �������� ���湤����   �����������json����
* �����ˣ�bai   
* ����ʱ�䣺2016-3-15 ����2:35:54         
*/
public class CacheUtils {

	/**
	* @Description:��ȡ����  ��URL��ַ��Ϊkey Json������Ϊֵ 
	* @param key  URL
	* @param json  
	* @return void 
	* @author bai
	*/
	public static void setCache(Context context,String key,String value){
		SharedPrefUtil.setString(context, key, value);
	}
	
	//��ȡ����
	public static String getCache(String key,Context context){
		return SharedPrefUtil.getString(context, key, null);
	}
}
