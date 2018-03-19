package com.chedaojunan.report.utils;

import com.chedaojunan.report.common.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

	// 取得本地时间：
	private Calendar cal = Calendar.getInstance();
	// 取得时间偏移量：
	private int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
	// 取得夏令时差：
	private int dstOffset = cal.get(Calendar.DST_OFFSET);

	// 获取年月日
	public String getYMD() {
		String ymd = new SimpleDateFormat(Constants.YMD_PATTERN).format(Calendar.getInstance().getTime());
		return ymd;
	}

	// 获取时分
	public String getHM() {
		String hm = new SimpleDateFormat(Constants.HM_PATTERN).format(Calendar.getInstance().getTime());
		return hm;
	}

	// 从本地时间转化为UTC时间（10位）
	public long getUTCTimeFromLocal(long localTime) {

		cal.setTimeInMillis(localTime);
		// 从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		long utcMills = cal.getTimeInMillis()/1000L;

		return utcMills;
	}

	// 从UTC时间转化为本地时间
	public long getLocalTimeFromUTC(long utcTime) {

		cal.setTimeInMillis(utcTime);
		// 从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(Calendar.MILLISECOND, (zoneOffset + dstOffset));
		long localMills = cal.getTimeInMillis();

		return localMills;
	}

}
