package com.esint.music.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * �����ƣ�TimeFromat �������� ʱ��ת������ �����ˣ�bai ����ʱ�䣺2016-2-19 ����9:38:59
 */
public class TimeFromat {

	// ����ת��Ϊ����
	public static String timeFormat(String time) {
		DateFormat formatter = new SimpleDateFormat("MM"+"��"+"dd"+"��");
		long now = System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(now);
		System.out.println(now + " = " + formatter.format(calendar.getTime()));
		return formatter.format(calendar.getTime());
	}
}
