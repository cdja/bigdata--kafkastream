import java.util.ArrayList;
import java.util.Comparator;
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
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import com.chedaojunan.report.client.AutoGraspApiClient;
import com.chedaojunan.report.model.AutoGraspRequestParam;
import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
import com.chedaojunan.report.service.ExternalApiExecutorService;
import com.chedaojunan.report.utils.SampledDataCleanAndRet;

public class KafkaStreamTest {

  private static final String BOOTSTRAP_SERVERS = "localhost:9092";
  private static final int SAMPLE_THRESHOLD = 2;
  private static final long TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS = 10000000000L;


  static Comparator<String> stringComparator =
      (s1, s2) -> (int) (SampledDataCleanAndRet.convertTimeStringToEpochSecond(SampledDataCleanAndRet.convertToFixedAccessDataPojo(s1).getServerTime()) -
          SampledDataCleanAndRet.convertTimeStringToEpochSecond(SampledDataCleanAndRet.convertToFixedAccessDataPojo(s2).getServerTime()));

  static final Serde<String> stringSerde = Serdes.String();
  //static final Serde<AutoGraspRequestParam> autoGraspRequestParamSerde =
  //final Serde<Long> longSerde = Serdes.Long();
  //final Serde<Windowed<String>> windowedStringSerde = new WindowedSerde<>(stringSerde);


  public static void main(String[] args) {
    String inputTopic = "hy-raw-data-test";
    String outputTopic = "schedule";

    /*TopologyBuilder topologyBuilder = new TopologyBuilder();
    topologyBuilder.*/

    final KafkaStreams sampledRawDataStream = buildSampleDataStream(inputTopic, outputTopic);

    sampledRawDataStream.start();

    // mock producer
    String dataFile = "testdata";
    KafkaProducerTest producerTest = new KafkaProducerTest();
    producerTest.runProducer(dataFile, inputTopic);
    producerTest.close();

    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
    Runtime.getRuntime().addShutdownHook(new Thread(sampledRawDataStream::close));

    final KafkaStreams apiRequestStream = buildApiEnrichedDataStream(outputTopic, inputTopic);
    apiRequestStream.start();

    Runtime.getRuntime().addShutdownHook(new Thread(apiRequestStream::close));

  }

  static KafkaStreams buildApiEnrichedDataStream(String apiRequestTopic, String rawDataTopic) {
    AutoGraspApiClient autoGraspApiClient = AutoGraspApiClient.getInstance();

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
    final KTable<String, ArrayList<String>> gaodeApiResponseTable = apiRequestStream
        .groupByKey()
        .aggregate(
            // the initializer
            () -> {
              return new ArrayList<>();
            },
            // the "add" aggregator
            (windowTime, record, list) -> {
              if (!list.contains(record))
                list.add(record);
              return list;
            },
            new ArrayListSerde<>(stringSerde)
        )
        .mapValues(apiRquestList -> {
          ArrayList<String> results = new ArrayList<>();
          List<Future<?>> futures = apiRquestList
              .stream()
              .map(
                  apiRequest -> ExternalApiExecutorService.getExecutorService().submit(() -> {
                    System.out.println("apiRequest: " + apiRequest);
                    AutoGraspRequestParam autoGraspRequestParam = SampledDataCleanAndRet.convertToAutoGraspRequestParam(apiRequest);
                    List<FixedFrequencyIntegrationData> gaodeApiResponseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequestParam);
                    gaodeApiResponseList
                        .stream()
                        .forEach(gaodeApiResponse -> results.add(gaodeApiResponse.toString()));
                  })
              ).collect(Collectors.toList());
          ExternalApiExecutorService.getFuturesWithTimeout(futures, TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS, "calling Gaode API");
          return results;
        });

    /*KStream<Long, AdClickAndViewEvent> leftJoin = viewStream.leftJoin(clickStream, (view, click) ->  new AdClickAndViewEvent(view, click),
        Serdes.Long(), AdSerdes.AD_VIEW_SERDE);
    leftJoin.print();*/

    gaodeApiResponseTable.toStream().print();

    // stream-to-table join using carId, ts and GPS

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
    streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, FixedFrequencyAccessDataTimestampExtractor.class);
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    //streamsConfiguration.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");
    //streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/Users/qianz/Documents/Misc/Work-beijing/state-store-test");

    KStreamBuilder builder = new KStreamBuilder();
    KStream<String, String> kStream = builder.stream(inputTopic);


    final KStream<Windowed<String>, PriorityQueue<String>> orderedRawDataWindow = kStream
        .map(
            (key, rawDataString) ->
                new KeyValue<>(SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString).getDeviceId(), rawDataString)
        )
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

    //orderedRawDataWindow.print();

    // write ordered sample data back to a kafka topic
    orderedRawDataWindow
        .map((windowedString, accessDataPriorityQueue) -> {
          long windowStartTime = windowedString.window().start();
          long windowEndTime = windowedString.window().end();
          ArrayList<String> sampledDataList = SampledDataCleanAndRet.sampleKafkaData(new ArrayList<>(accessDataPriorityQueue));
          AutoGraspRequestParam autoGraspRequestParam = SampledDataCleanAndRet.autoGraspRequestParamRet(sampledDataList);
          String valueString;
          if (autoGraspRequestParam == null)
            valueString = null;
          else
            valueString = autoGraspRequestParam.toString();
          return new KeyValue<>(String.join("-", String.valueOf(windowStartTime), String.valueOf(windowEndTime)), valueString);
        })
        .filter((key, autoGraspRequestParamString) -> StringUtils.isNotEmpty(autoGraspRequestParamString))
        .to(stringSerde, stringSerde, outputTopic);

    return new KafkaStreams(builder, streamsConfiguration);

  }

  /*static HongyanRawData convertToHongYanPojo(String rawDataString) {
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
  }*/

}
