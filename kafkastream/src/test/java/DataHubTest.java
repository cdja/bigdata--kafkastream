import com.chedaojunan.report.model.FixedFrequencyAccessData;
import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
import com.chedaojunan.report.service.ExternalApiExecutorService;
import com.chedaojunan.report.utils.WriteDatahubUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Properties;

public class DataHubTest {

  private static final Logger logger = LoggerFactory.getLogger(DataHubTest.class);


  public void runDatahub(int i) {

    WriteDatahubUtil datahubUtil = WriteDatahubUtil.getInstance();
    long serverTime = System.currentTimeMillis();

    ArrayList<FixedFrequencyIntegrationData> list = new ArrayList<>();
    System.out.println("serverTime Start:="+serverTime);
    for (int j = 0; j < 1000; j++) {
      FixedFrequencyAccessData accessData;
      accessData = new FixedFrequencyAccessData();
      accessData.setDeviceId("test000000" + j +"");
      accessData.setDeviceImei("test000000");
      accessData.setTripId(i+"");
      accessData.setLocalTime("1521478861000");
      accessData.setServerTime(serverTime+"");
      accessData.setLatitude(39.990475+0.00001*j);
      accessData.setLongitude(116.481499+0.00001*j);
      accessData.setAltitude(30.98);
      accessData.setDirection(98.00);
      accessData.setGpsSpeed(98.00);
      accessData.setYawRate(20.3);
      accessData.setAccelerateZ(20.4);
      accessData.setRollRate(20.5);
      accessData.setAccelerateX(20.6);
      accessData.setPitchRate(20.7);
      accessData.setAccelerateY(20.8);
      accessData.setSourceId("001");

      list.add(new FixedFrequencyIntegrationData(accessData));
    }
    try {
      datahubUtil.putRecords(list);
      long tt = System.currentTimeMillis() - serverTime;
      System.out.println("第"+i+"批数据，用时"+tt+"毫秒");
    } catch (Exception ex) {
      ex.printStackTrace();//handle exception here
    }
  }

  public static void main(String[] args) {
    DataHubTest dataHubTest = new DataHubTest();
    try {
      int i = 0;
      while(true){
        i++;
        dataHubTest.runDatahub(i);
//        Thread.sleep(0);
      }
    } catch (Exception e) {
    }
  }
}
