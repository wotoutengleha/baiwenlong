package com.esint.music.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 类名称：TimeFromat 类描述： 时间转换工具 创建人：bai 创建时间：2016-2-19 下午9:38:59
 */
public class TimeFromat {

	// 毫秒转换为日期
	public static void main(String[] args) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		long now = System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(now);
		System.out.println(now + " = " + formatter.format(calendar.getTime()));
	}
}
