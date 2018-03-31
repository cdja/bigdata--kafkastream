import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;

import com.chedaojunan.report.model.HongyanRawData;
import com.chedaojunan.report.utils.ObjectMapperUtils;
import com.chedaojunan.report.utils.SampledDataCleanAndRet;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaConsumerTest {

  private static final String BOOTSTRAP_SERVERS = "localhost:9092";
  private static final String TIME_PATTERN = "MM-dd-yy HH:mm:ss";

  private static ObjectMapper objectMapper = ObjectMapperUtils.getObjectMapper();

  public static List<String> runConsumer(String inputTopic) {
    List<String> data = new LinkedList<>();
    final Consumer<String, String> consumer = createConsumer(inputTopic);
    final int giveUp = 100;
    int noRecordsCount = 0;
    while (true) {
      final ConsumerRecords<String, String> consumerRecords =
          consumer.poll(10);
      if (consumerRecords.count() == 0) {
        noRecordsCount++;
        if (noRecordsCount > giveUp) break;
        else continue;
      }
      consumerRecords.forEach(record -> {
        System.out.printf("Consumer Record:(%d, %s, %s, %d, %d)\n",
            record.timestamp(),
            record.key(), record.value(),
            record.partition(), record.offset());
        //System.out.println(SampledDataCleanAndRet.convertToFixedFrequencyIntegrationDataPojo(record.value()).getDeviceId());
        data.add(record.value());
      });
      consumer.commitAsync();
    }
    Runtime.getRuntime().addShutdownHook(new Thread(consumer::close));
    System.out.println("DONE");
    return data;
  }

  private static Consumer<String, String> createConsumer(String inputTopic) {
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
    consumer.subscribe(Collections.singletonList(inputTopic));
    return consumer;
  }

  public static void main(String... args) throws Exception {
    //String inputTopic = "hy-raw-data-test";
    String inputTopic = "twitter_json";
    //String inputTopic = "schedule";
    runConsumer(inputTopic);

  }

  public static ZonedDateTime convertTimeString(String timeString) {
    ZonedDateTime dateTime = ZonedDateTime.parse(timeString, DateTimeFormatter
        .ofPattern(TIME_PATTERN).withZone(ZoneId.of("UTC")));
    return dateTime;
  }

  static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
    SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
        new Comparator<Map.Entry<K, V>>() {
          @Override
          public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
            int res = e1.getValue().compareTo(e2.getValue());
            return res != 0 ? res : 1; // Special fix to preserve items with equal values
          }
        }
    );
    sortedEntries.addAll(map.entrySet());
    return sortedEntries;
  }

}
