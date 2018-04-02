import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.chedaojunan.report.serdes.ArrayListSerde;
import com.chedaojunan.report.utils.FixedFrequencyAccessDataTimestampExtractor;
import com.chedaojunan.report.utils.WriteDatahubUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.TimeWindows;

import com.chedaojunan.report.client.AutoGraspApiClient;
import com.chedaojunan.report.model.AutoGraspRequest;
import com.chedaojunan.report.model.FixedFrequencyAccessData;
import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
import com.chedaojunan.report.utils.SampledDataCleanAndRet;

public class KafkaStreamNewTest {

  static final Serde<String> stringSerde = Serdes.String();
  private static final String BOOTSTRAP_SERVERS = "localhost:9092";
  private static final int WINDOW_LENGTH_IN_SECONDS = 90;
  //private static final long TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS = 10000000000L;

  static Comparator<String> sortingByServerTime =
      (s1, s2)-> (int) (Long.parseLong(SampledDataCleanAndRet.convertToFixedAccessDataPojo(s1).getServerTime()) -
          Long.parseLong(SampledDataCleanAndRet.convertToFixedAccessDataPojo(s2).getServerTime()));

  static AutoGraspApiClient autoGraspApiClient = AutoGraspApiClient.getInstance();

  public static void main(String[] args) {

    String rawDataTopic = "hy-raw-data-test";

    final KafkaStreams sampledRawDataStream = buildDataStream(rawDataTopic);

    sampledRawDataStream.start();

    // mock producer
    String dataFile = "testdata";
    KafkaProducerTest producerTest = new KafkaProducerTest();
    producerTest.runProducer(dataFile, rawDataTopic);
    producerTest.close();

    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
    Runtime.getRuntime().addShutdownHook(new Thread(sampledRawDataStream::close));

  }

  static KafkaStreams buildDataStream(String inputTopic) {
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
    WriteDatahubUtil writeDatahubUtil = new WriteDatahubUtil();

    //final KStream<Windowed<String>, ArrayList<String>> orderedDataStream = kStream
    final KStream<String, FixedFrequencyIntegrationData> enrichedDataStream = kStream
        .map(
            (key, rawDataString) ->
                new KeyValue<>(SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString).getDeviceId(), rawDataString)
        )
        .groupByKey()
        .aggregate(
            // the initializer
            () -> {
              return new ArrayList<>();
            },
            // the "add" aggregator
            (windowedCarId, record, queue) -> {
              if (!queue.contains(record))
                queue.add(record);
              return queue;
            },
            TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOW_LENGTH_IN_SECONDS)),
            new ArrayListSerde<>(stringSerde)
        )
        .toStream()

    //orderedDataStream.print();

    //final KStream<String, FixedFrequencyIntegrationData> enrichedDataStream = orderedDataStream
        .map((windowedString, accessDataList) -> {
          long windowStartTime = windowedString.window().start();
          long windowEndTime = windowedString.window().end();
          accessDataList.sort(sortingByServerTime);
          ArrayList<FixedFrequencyAccessData> sampledDataList = SampledDataCleanAndRet.sampleKafkaData(accessDataList);
          AutoGraspRequest autoGraspRequest = SampledDataCleanAndRet.autoGraspRequestRet(sampledDataList);
          System.out.println("apiQuest: " + autoGraspRequest);
          String dataKey = String.join("-", String.valueOf(windowStartTime), String.valueOf(windowEndTime));
          List<FixedFrequencyIntegrationData> gaodeApiResponseList = new ArrayList<>();
          if (autoGraspRequest != null)
            gaodeApiResponseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequest);
          ArrayList<FixedFrequencyAccessData> rawDataList = accessDataList
              .stream()
              .map(rawDataString -> SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString))
              .collect(Collectors.toCollection(ArrayList::new));
          ArrayList<FixedFrequencyIntegrationData> enrichedData = SampledDataCleanAndRet.dataIntegration(rawDataList, sampledDataList, gaodeApiResponseList);
          // 整合数据入库datahub
          if(CollectionUtils.isNotEmpty(enrichedData)) {
            writeDatahubUtil.putRecords(enrichedData);
          }
          return new KeyValue<>(dataKey, enrichedData);
        })
        .flatMapValues(gaodeApiResponseList ->
            gaodeApiResponseList
                .stream()
                .collect(Collectors.toList()));

    enrichedDataStream.print();

    return new KafkaStreams(builder, streamsConfiguration);

  }

  /*static KafkaStreams buildDataStream(String inputTopic) {
    final Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
        BOOTSTRAP_SERVERS);
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, com.chedaojunan.report.utils.FixedFrequencyAccessDataTimestampExtractor.class);
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    //streamsConfiguration.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");
    //streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/Users/qianz/Documents/Misc/Work-beijing/state-store-test");

    KStreamBuilder builder = new KStreamBuilder();
    KStream<String, String> kStream = builder.stream(inputTopic);
    WriteDatahubUtil writeDatahubUtil = new WriteDatahubUtil();

    //final KStream<String, FixedFrequencyIntegrationData> enrichedDataStream = kStream
    final KStream<Windowed<String>, String> enrichedDataStream = kStream
        .map(
            (key, rawDataString) ->
                new KeyValue<>("haha", rawDataString)
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
            TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOW_LENGTH_IN_SECONDS)),
            new PriorityQueueSerde<>(stringComparator, stringSerde)
        )
        .toStream()
        .flatMapValues(accessDataPriorityQueue ->
          accessDataPriorityQueue
              .stream()
              .map(rawAccessDataString -> {
                FixedFrequencyAccessData rawAccessData = SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawAccessDataString);
                return new Pair<> (rawAccessData.getDeviceId(), rawAccessDataString);
              })
              .collect(Collectors.toCollection(PriorityQueue::new)));

       .map((windowedString, accessDataPriorityQueue) -> {
          long windowStartTime = windowedString.window().start();
          long windowEndTime = windowedString.window().end();
          ArrayList<FixedFrequencyAccessData> sampledDataList = SampledDataCleanAndRet.sampleKafkaData(new ArrayList<>(accessDataPriorityQueue));
          AutoGraspRequest autoGraspRequest = SampledDataCleanAndRet.autoGraspRequestRet(sampledDataList);
          //System.out.println("apiQuest: " + autoGraspRequest);
          String dataKey = String.join("-", String.valueOf(windowStartTime), String.valueOf(windowEndTime));
          List<FixedFrequencyIntegrationData> gaodeApiResponseList = new ArrayList<>();
          if (autoGraspRequest != null)
            gaodeApiResponseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequest);
          ArrayList<FixedFrequencyAccessData> rawDataList = accessDataPriorityQueue
              .stream()
              .map(rawDataString -> SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString))
              .collect(Collectors.toCollection(ArrayList::new));
          ArrayList<FixedFrequencyIntegrationData> enrichedData = SampledDataCleanAndRet.dataIntegration(rawDataList, sampledDataList, gaodeApiResponseList);
          // 整合数据入库datahub
          if (enrichedData != null && enrichedData.size()!=0) {
            writeDatahubUtil.putRecords(enrichedData);
          }
          return new KeyValue<>(dataKey, enrichedData);
        })
        .flatMapValues(gaodeApiResponseList ->
            gaodeApiResponseList
                .stream()
                .collect(Collectors.toList()));

    enrichedDataStream.print();

    return new KafkaStreams(builder, streamsConfiguration);

  }*/
}