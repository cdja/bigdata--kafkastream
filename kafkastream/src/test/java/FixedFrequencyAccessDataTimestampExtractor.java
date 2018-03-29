import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import com.chedaojunan.report.model.FixedFrequencyAccessData;
import com.chedaojunan.report.model.HongyanRawData;
import com.chedaojunan.report.utils.SampledDataCleanAndRet;

public class FixedFrequencyAccessDataTimestampExtractor implements TimestampExtractor {

  private static final String TIME_FORMAT = "MM-dd-yy HH:mm:ss";

  @Override
  public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
    SimpleDateFormat df = new SimpleDateFormat(TIME_FORMAT);
    try {
      String rawDataString = record.value().toString();
      FixedFrequencyAccessData rawData = SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString);
      if (rawData != null) {
        Date d = df.parse(rawData.getServerTime());
        long milliseconds = d.getTime();
        //System.out.println(rawData.getDeviceId() + ":" + rawData.getServerTime() + "-" + milliseconds);
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
