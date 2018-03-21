import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;

import com.chedaojunan.report.model.HongyanRawData;
import com.chedaojunan.report.utils.ObjectMapperUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaConsumerTest {

  private static final String TOPIC = "hy-raw-data-test";
  /*private static final String BOOTSTRAP_SERVERS = "kafka1-footprint-personal-stage.footprint-trulia.com:9092," +
          "kafka2-footprint-personal-stage.footprint-trulia.com:9092," +
          "kafka3-footprint-personal-stage.footprint-trulia.com:9092/kafka";*/
  private static final String BOOTSTRAP_SERVERS = "localhost:9092";

  private static ObjectMapper objectMapper = ObjectMapperUtils.getObjectMapper();

  static void runConsumer() throws InterruptedException {
    final Consumer<String, String> consumer = createConsumer();
    final int giveUp = 100;   int noRecordsCount = 0;
    while (true) {
      final ConsumerRecords<String, String> consumerRecords =
          consumer.poll(10);
      if (consumerRecords.count()==0) {
        noRecordsCount++;
        if (noRecordsCount > giveUp) break;
        else continue;
      }
      consumerRecords.forEach(record -> {
        System.out.printf("Consumer Record:(%s, %s, %d, %d)\n",
            record.key(), record.value(),
            record.partition(), record.offset());
        try {
          HongyanRawData data = objectMapper.readValue(record.value(), HongyanRawData.class);
          System.out.println(data.getCarId());
        }catch(IOException e){
          e.printStackTrace();
        }
      });
      consumer.commitAsync();
    }
    consumer.close();
    System.out.println("DONE");
  }

  private static Consumer<String, String> createConsumer() {
    final Properties props = new Properties();
    props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
        BOOTSTRAP_SERVERS);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        Serdes.String().deserializer().getClass());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        Serdes.String().deserializer().getClass());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    // Create the consumer using props.
    final Consumer<String, String> consumer =
        new KafkaConsumer<>(props);
    // Subscribe to the topic.
    consumer.subscribe(Collections.singletonList(TOPIC));
    return consumer;
  }

  public static void main(String... args) throws Exception {
    runConsumer();
  }
}
