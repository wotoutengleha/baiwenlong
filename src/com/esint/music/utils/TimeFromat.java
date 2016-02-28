package com.esint.music.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * �����ƣ�TimeFromat �������� ʱ��ת������ �����ˣ�bai ����ʱ�䣺2016-2-19 ����9:38:59
 */
public class TimeFromat {

	// ����ת��Ϊ����
	public static void main(String[] args) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		long now = System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(now);
		System.out.println(now + " = " + formatter.format(calendar.getTime()));
	}
}
