package com.chedaojunan.report.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import com.chedaojunan.report.model.FixedFrequencyAccessData;
import com.chedaojunan.report.model.HongyanRawData;
import com.chedaojunan.report.utils.SampledDataCleanAndRet;

public class FixedFrequencyAccessDataTimestampExtractor implements TimestampExtractor {

  @Override
  public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
      String rawDataString = record.value().toString();
      FixedFrequencyAccessData rawData = SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString);
      if (rawData != null) {
        long milliseconds = Long.parseLong(rawData.getServerTime());
        //System.out.println(rawData.getDeviceId() + ":" + rawData.getServerTime() + "-" + milliseconds);
        return milliseconds;
      } else
        return -1L;
  }    //throw new IllegalArgumentException("HongyanDataTimestampExtractor cannot extract timestamp");
}
