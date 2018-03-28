import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.TopologyBuilder;

import com.chedaojunan.report.model.FixedFrequencyAccessData;
import com.chedaojunan.report.model.HongyanRawData;
import com.chedaojunan.report.service.ExternalApiExecutorService;
import com.chedaojunan.report.utils.ObjectMapperUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaStreamTest {

  private static final String BOOTSTRAP_SERVERS = "localhost:9092";
  private static final String TIME_PATTERN = "MM-dd-yy HH:mm:ss";
  private static final int SAMPLE_THRESHOLD = 2;
  private static final long TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS = 10000000000L;


  static Comparator<String> stringComparator =
      (s1, s2) -> (int) (KafkaStreamTest.convertTimeStringToEpochSecond(convertToHongYanPojo(s1).getGpsTime()) -
          KafkaStreamTest.convertTimeStringToEpochSecond(convertToHongYanPojo(s2).getGpsTime()));

  static final Serde<String> stringSerde = Serdes.String();
  //final Serde<Long> longSerde = Serdes.Long();
  //final Serde<Windowed<String>> windowedStringSerde = new WindowedSerde<>(stringSerde);


  public static void main(String[] args) {
    String inputTopic = "hy-raw-data-test";
    String outputTopic = "schedule";

    /*TopologyBuilder topologyBuilder = new TopologyBuilder();
    topologyBuilder.*/

    final KafkaStreams sampledRawDataStream = buildSampleDataStream(inputTopic, outputTopic);

    sampledRawDataStream.start();

    String dataFile = "testdata";
    KafkaProducerTest producerTest = new KafkaProducerTest();
    producerTest.runProducer(dataFile, inputTopic);
    producerTest.close();


    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
    Runtime.getRuntime().addShutdownHook(new Thread(sampledRawDataStream::close));
    //sampledRawDataStream.close();

    final KafkaStreams apiRequestStream = buildApiEnrichedDataStream(outputTopic, inputTopic);
    apiRequestStream.start();

    Runtime.getRuntime().addShutdownHook(new Thread(apiRequestStream::close));



  }

  static KafkaStreams buildApiEnrichedDataStream(String apiRequestTopic, String rawDataTopic) {
    final Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
        BOOTSTRAP_SERVERS);
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    //streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, HongyanDataTimestampExtractor.class);

    KStreamBuilder builder = new KStreamBuilder();
    KStream<String, String> apiRequestStream = builder.stream(apiRequestTopic);
    KStream<String, String> rawDataStream = builder.stream(rawDataTopic);

    // get Gaode API response -- final KStream<String, ArrayList<String>>
    apiRequestStream
        .groupByKey()
        .aggregate(
            // the initializer
            () -> {
              return new ArrayList<>();
            },
            // the "add" aggregator
            (windowTime, record, queue) -> {
              if (!queue.contains(record))
                queue.add(record);
              return queue;
            },
            new ArrayListSerde<>(stringSerde)
        )
        .mapValues(apiRquestList -> {
          ArrayList<String> results = new ArrayList<>();
          List<Future<?>> futures = apiRquestList
              .stream()
              .map(
                  apiRequest -> ExternalApiExecutorService.getExecutorService().submit(() -> {
                    System.out.println(apiRequest);
                    results.add(apiRequest);
                  })
              )
              .collect(Collectors.toList());
          ExternalApiExecutorService.getFuturesWithTimeout(futures, TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS, "calling Gaode API");
          return results;
        });

    //apiRequestStream.print();

    // stream-to-stream join using carId, ts and GPS

    return new KafkaStreams(builder, streamsConfiguration);
  }
  
  static KafkaStreams buildSampleDataStream(String inputTopic, String outputTopic) {
    final Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
        BOOTSTRAP_SERVERS);
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    //streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/Users/qianz/Documents/Misc/Work-beijing/state-store-test");
    streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, HongyanDataTimestampExtractor.class);
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    //streamsConfiguration.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");


    KStreamBuilder builder = new KStreamBuilder();
    KStream<String, String> kStream = builder.stream(inputTopic);

    final KStream<Windowed<String>, PriorityQueue<String>> orderedRawDataWindow = kStream
        .map((key, rawDataString) -> new KeyValue<>(convertToHongYanPojo(rawDataString).getCarId(), rawDataString))
        .groupByKey()
        .aggregate(
            // the initializer
            () -> {
              return new PriorityQueue<>(stringComparator);
            },
            // the "add" aggregator
            (windowedCarId, record, queue) -> {
              if (!queue.contains(record))
                queue.add(record);
              return queue;
            },
            TimeWindows.of(TimeUnit.SECONDS.toMillis(90)),
            new PriorityQueueSerde<>(stringComparator, stringSerde)
        )
        .toStream();

    orderedRawDataWindow.print();

    // write ordered sample data back to a kafka topic
    orderedRawDataWindow
        .map((windowedString, hongyanRawDataPriorityQueue) -> {
          long windowStartTime = windowedString.window().start();
          long windowEndTime = windowedString.window().end();
          return new KeyValue<>(String.join("-", String.valueOf(windowStartTime), String.valueOf(windowEndTime)), hongyanRawDataPriorityQueue);
        })
        .flatMapValues(hongyanRawDataPriorityQueue -> getSamples(hongyanRawDataPriorityQueue).stream().collect(Collectors.toList()))
        .to(stringSerde, stringSerde, outputTopic);

    return new KafkaStreams(builder, streamsConfiguration);

  }

  static List<String> getSamples(PriorityQueue<String> rawDataQueue) {
    List<String> accessDataList = new LinkedList<>();
    int size = rawDataQueue.size();
    for (int i = 0; i < size; i++) {
      FixedFrequencyAccessData accessData = new FixedFrequencyAccessData();
      HongyanRawData rawData = convertToHongYanPojo(rawDataQueue.poll());
      accessData.setDevice_id(rawData.getCarId());
      accessData.setServer_time(rawData.getGpsTime());
      accessData.setLatitude(rawData.getLatitude());
      accessData.setLongitude(rawData.getLongitude());
      accessData.setDevice_imei(Instant.now().toString());
      accessDataList.add(accessData.toString());
    }
    return accessDataList;
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

  static FixedFrequencyAccessData convertToFixedAccessDataPojo(String accessDataString) {
    if (StringUtils.isEmpty(accessDataString))
      return null;
    ObjectMapper objectMapper = ObjectMapperUtils.getObjectMapper();
    try {
      FixedFrequencyAccessData accessData = objectMapper.readValue(accessDataString, FixedFrequencyAccessData.class);
      return accessData;
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
