import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import com.chedaojunan.report.model.HongyanRawData;
import com.chedaojunan.report.utils.ObjectMapperUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HongyanDataTimestampExtractor implements TimestampExtractor {
  private static final String TIME_FORMAT = "MM-dd-yy HH:mm:ss";
  @Override
  public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
    SimpleDateFormat df = new SimpleDateFormat(TIME_FORMAT);
    try {
      String rawDataString = record.value().toString();
      HongyanRawData rawData = KafkaStreamTest.convertToHongYanPojo(rawDataString);
      if(rawData != null) {
        Date d = df.parse(rawData.getGpsTime());
        long milliseconds = d.getTime();
        //System.out.println(rawData.getCarId() + ":" + rawData.getGpsTime() + "-" + milliseconds);
        return milliseconds;
      } else
        return -1L;
    } catch (ParseException e) {
      e.printStackTrace();
    } /*catch (IOException e){
      e.printStackTrace();
    }*/
    return -1L;
  }    //throw new IllegalArgumentException("HongyanDataTimestampExtractor cannot extract timestamp");
}
