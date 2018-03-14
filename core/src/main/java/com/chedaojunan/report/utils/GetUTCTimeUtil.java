package com.chedaojunan.report.utils;

import java.util.Calendar;

public class GetUTCTimeUtil {

  // 取得本地时间：
  private Calendar cal = Calendar.getInstance();
  // 取得时间偏移量：
  private int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
  // 取得夏令时差：
  private int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);

  // 10位UTC时间
  public long getUTCTimeStr(long locatTime) {
    cal.setTimeInMillis(locatTime);
//        System.out.println("local millis = " + cal.getTimeInMillis()); // 等效System.currentTimeMillis() , 统一值，不分时区
    // 从本地时间里扣除这些差量，即可以取得UTC时间：
    cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
    long mills = cal.getTimeInMillis();

    System.out.println("UTC = " + mills/1000L);
    return mills;
  }

  public long getUTCTimeFromLocal(long millis) {
    cal.setTimeInMillis(millis);

    // 从本地时间里扣除这些差量，即可以取得UTC时间：
    cal.add(java.util.Calendar.MILLISECOND, (zoneOffset + dstOffset));
    long mills = cal.getTimeInMillis();
    System.out.println("Local time = " + mills);
    return mills;
  }
}
