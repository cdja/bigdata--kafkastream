
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
import com.chedaojunan.report.utils.SampledDataCleanAndRet;

public class FixedFrequencyIntegrationDataTimestampExtractor implements TimestampExtractor {

  @Override
  public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {
    String rawDataString = record.value().toString();
    FixedFrequencyIntegrationData rawData = SampledDataCleanAndRet.convertToFixedFrequencyIntegrationDataPojo(rawDataString);
    if (rawData != null && StringUtils.isNotEmpty(rawData.getServerTime())) {
        long milliseconds = Long.parseLong(rawData.getServerTime());
        //System.out.println(rawData.getDeviceId() + ":" + rawData.getServerTime() + "-" + milliseconds);
        return milliseconds;
      } else
        return -1L;
  }    //throw new IllegalArgumentException("HongyanDataTimestampExtractor cannot extract timestamp");
}
