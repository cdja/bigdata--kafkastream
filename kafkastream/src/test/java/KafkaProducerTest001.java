import com.chedaojunan.report.model.FixedFrequencyAccessData;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KafkaProducerTest001 {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerTest001.class);
  //private static final String BOOTSTRAP_SERVERS = "47.95.10.165:9092,47.93.24.115:9092,39.106.170.188:9092";
  private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";

  private Producer producer;

  public void runProducer(String inputTopic, int i) {

    Properties configProperties = new Properties();
    configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        Serdes.String().serializer().getClass());
    configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        Serdes.String().serializer().getClass());


    producer = new KafkaProducer(configProperties);

    FixedFrequencyAccessData accessData;
    accessData = new FixedFrequencyAccessData();
    accessData.setDeviceId("70211191");
    accessData.setDeviceImei("64691168800");
    accessData.setTripId(0+i+"");
    accessData.setLocalTime("1521478861000");
    accessData.setServerTime(System.currentTimeMillis() + "");
    accessData.setLatitude(39.00);
    accessData.setLongitude(129.01);
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
    try {
      System.out.println(new ObjectMapper().writeValueAsString(accessData));
      producer.send(new ProducerRecord<String, String>(inputTopic, new ObjectMapper().writeValueAsString(accessData)));
    } catch (Exception ex) {
      ex.printStackTrace();//handle exception here
    }
  }

  public void close() {
    if (producer != null) {
      producer.close();
      LOG.info("Kafka producer is closed.");
    }
  }

  public static void main(String[] args) {
    KafkaProducerTest001 producerTest = new KafkaProducerTest001();
    String inputTopic = "data-test4";
    try {
      int i=0;
      while(true){
        i++;
        producerTest.runProducer(inputTopic,i);
        Thread.sleep(900);
      }
    } catch (InterruptedException e) {
    }
    producerTest.close();
  }
}
