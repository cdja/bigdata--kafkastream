import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import com.chedaojunan.report.model.HongyanRawData;
import com.chedaojunan.report.utils.ObjectMapperUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaStreamTest {

  private static final String BOOTSTRAP_SERVERS = "localhost:9092";
  private static final String TIME_PATTERN = "MM-dd-yy HH:mm:ss";
  private static final int SAMPLE_THRESHOLD = 2;


  public static void main(String[] args) {
    String inputTopic = "hy-raw-data-test";
    //String outputTopic = "word-count-output";

    String dataFile = "testdata";
    KafkaProducerTest producerTest = new KafkaProducerTest();
    producerTest.runProducer(dataFile, inputTopic);
    producerTest.close();

    final Serde<String> stringSerde = Serdes.String();
    final Serde<Long> longSerde = Serdes.Long();

    final Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
        BOOTSTRAP_SERVERS);
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/Users/qianz/Documents/Misc/Work-beijing/state-store-test");
    streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, HongyanDataTimestampExtractor.class);

    Map<String, Object> serdeProps = new HashMap<>();
    Serde<HongyanRawData> hongyanRawDataSerde = SerdeFactory.createSerde(HongyanRawData.class, serdeProps);

    final Serde<Windowed<String>> windowedStringSerde = new WindowedSerde<>(stringSerde);

    KStreamBuilder builder = new KStreamBuilder();
    KStream<String, String> kStream = builder.stream(inputTopic);

    final Comparator<HongyanRawData> comparator =
        (o1, o2) -> (int) (KafkaStreamTest.convertTimeStringToEpochSecond(o2.getGpsTime()) -
            KafkaStreamTest.convertTimeStringToEpochSecond(o1.getGpsTime()));


    KTable<Windowed<String>, PriorityQueue<HongyanRawData>> rawDataWindow = kStream
        .map((key, rawDataString) -> new KeyValue<>(convertToHongYanPojo(rawDataString).getCarId(), rawDataString))
        .groupByKey()
        .aggregate(
            // the initializer
            () -> {
              return new PriorityQueue<>(comparator);
            },
            // the "add" aggregator
            (windowedIndustry, record, queue) -> {
              queue.add(convertToHongYanPojo(record));
              return queue;
            },
            TimeWindows.of(TimeUnit.SECONDS.toMillis(90)),
            new PriorityQueueSerde<>(comparator, hongyanRawDataSerde)
        );

    rawDataWindow
        .toStream()
        .map((windowedString, hongyanRawDataPriorityQueue) -> new KeyValue<>(windowedString, hongyanRawDataPriorityQueue.size()))
        .filter((windowedString, hongyanRawDataCount) -> hongyanRawDataCount >= SAMPLE_THRESHOLD)
        .print();

    KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
    streams.start();

    KafkaConsumerTest.runConsumer(inputTopic);
    streams.close();
  }

  static HongyanRawData convertToHongYanPojo(String rawDataString) {
    if (StringUtils.isEmpty(rawDataString))
      return null;
    ObjectMapper objectMapper = ObjectMapperUtils.getObjectMapper();
    try {
      HongyanRawData rawData = objectMapper.readValue(rawDataString, HongyanRawData.class);
      //LOG.info("Processing CAR_ID={} at GPS_TIME={}", rawData.getCarId(), rawData.getGpsTime());
      return rawData;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static long convertTimeStringToEpochSecond(String timeString) {
    ZonedDateTime dateTime = ZonedDateTime.parse(timeString, DateTimeFormatter
        .ofPattern(TIME_PATTERN).withZone(ZoneId.of("UTC")));
    return dateTime.toEpochSecond();
  }
}
