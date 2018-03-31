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
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.Stores;

import com.chedaojunan.report.client.AutoGraspApiClient;
import com.chedaojunan.report.model.AutoGraspRequest;
import com.chedaojunan.report.model.FixedFrequencyAccessData;
import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
import com.chedaojunan.report.service.ExternalApiExecutorService;
import com.chedaojunan.report.utils.SampledDataCleanAndRet;

public class KafkaStreamTest {

  static final Serde<String> stringSerde = Serdes.String();
  private static final String BOOTSTRAP_SERVERS = "localhost:9092";
  private static final int WINDOW_LENGTH_IN_SECONDS = 90;
  private static final long TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS = 10000000000L;
  static Comparator<String> stringComparator =
      (s1, s2) -> (int) (Long.parseLong(SampledDataCleanAndRet.convertToFixedAccessDataPojo(s1).getServerTime()) -
          Long.parseLong(SampledDataCleanAndRet.convertToFixedAccessDataPojo(s2).getServerTime()));
  //static final Serde<AutoGraspRequestParam> autoGraspRequestParamSerde =
  //final Serde<Long> longSerde = Serdes.Long();
  //final Serde<Windowed<String>> windowedStringSerde = new WindowedSerde<>(stringSerde);

  public static void main(String[] args) {
    String rawDataTopic = "hy-raw-data-test";
    String apiReuqestTopic = "schedule";
    String apiResponseTopic = "twitter_json";

    /*TopologyBuilder topologyBuilder = new TopologyBuilder();
    topologyBuilder.*/

    //final KafkaStreams sampledRawDataStream = buildSampleDataStream(rawDataTopic, apiReuqestTopic);

    //sampledRawDataStream.start();

    // mock producer
    /*String dataFile = "testdata";
    KafkaProducerTest producerTest = new KafkaProducerTest();
    producerTest.runProducer(dataFile, rawDataTopic);
    producerTest.close();*/

    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
    //Runtime.getRuntime().addShutdownHook(new Thread(sampledRawDataStream::close));

    final KafkaStreams apiResponseStream = buildApiResponseStream(apiReuqestTopic, apiResponseTopic);
    apiResponseStream.start();

    /*ReadOnlyKeyValueStore<String, String> keyValueStore =
        apiResponseStream.store("CountsKeyValueStore", QueryableStoreTypes.keyValueStore());
    System.out.println("count for hello:" + keyValueStore.get("1489213890000-1489213980000"));*/

    Runtime.getRuntime().addShutdownHook(new Thread(apiResponseStream::close));

    final KafkaStreams enrichDataStream = buildApiEnrichedDataStream(apiResponseTopic, rawDataTopic);
    enrichDataStream.start();

    Runtime.getRuntime().addShutdownHook(new Thread(enrichDataStream::close));


  }

  /*static void buildGlobalTable(){
    KStreamBuilder globalBuilder = new KStreamBuilder();
    StateStoreSupplier<KeyValueStore<String, String>> storeSupplier = Stores
        .create("config-table")
        .withKeys(stringSerde)
        .withValues(stringSerde)
        .persistent()
        .disableLogging()
        .build();

    // a Processor that updates the store
    ProcessorSupplier<String, String> procSupplier = () -> new ConfigWorker();
    globalBuilder.addGlobalStore(
        storeSupplier.get(),
        "test-table-source",
        new StringDeserializer(),
        new StringDeserializer(),
        apiResponseTopic,
        "config-worker",
        procSupplier)
        .buildGlobalStateTopology();
  }*/

  static KafkaStreams buildApiEnrichedDataStream(String apiResponseTopic, String rawDataTopic) {

    final Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
        BOOTSTRAP_SERVERS);
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    //streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/Users/qianz/Documents/Misc/Work-beijing/state-store-test");
    streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, FixedFrequencyIntegrationDataTimestampExtractor.class);

    KStreamBuilder builder = new KStreamBuilder();
    KStream<String, String> apiResponseStringStream = builder.stream(apiResponseTopic);
    KStream<String, ArrayList<String>> apiResponseStringListStream = apiResponseStringStream
        .map((key, responseString) -> new KeyValue<>("haha", responseString))
        .groupByKey()
        .aggregate(
            // the initializer
            () -> {
              return new ArrayList<>();
            },
            // the "add" aggregator
            (windowTime, record, list) -> {
              list.add(record);
              return list;
            },
            new ArrayListSerde<>(stringSerde)
        ).toStream();

    apiResponseStringListStream.print();


    KStream<String, String> rawDataStream = builder.stream(rawDataTopic);

    rawDataStream.print();


    // stream-to-table join using carId and window-ts
    /*final KStream<String, ArrayList<String>> orderedRawData = rawDataStream
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
            TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOW_LENGTH_IN_SECONDS)),
            new PriorityQueueSerde<>(stringComparator, stringSerde)
        )
        .toStream()
        .map(
            (windowedString, accessDataPriorityQueue) -> {
              long windowStartTime = windowedString.window().start();
              long windowEndTime = windowedString.window().end();
              ArrayList<String> rawDataList = new ArrayList<>(accessDataPriorityQueue);
              //return new KeyValue<>(String.join("-", String.valueOf(windowStartTime), String.valueOf(windowEndTime)), rawDataList);
              return new KeyValue<>("haha", rawDataList);
            });

    orderedRawData.print();*/



    /*final KStream<String, String> apiResponseListStream = builder.stream(apiResponseTopic);
    //apiResponseListStream.print();

    final KStream<String, String> test = orderedRawData
        .join(apiResponseListStream, (rawDataStringList, apiResposneString) -> {
          //ArrayList<String> sampleStringList = SampledDataCleanAndRet.sampleKafkaData(rawDataStringList);
          //return sampleStringList;
          return "haha";
            }, JoinWindows.of(TimeUnit.SECONDS.toMillis(WINDOW_LENGTH_IN_SECONDS))
        );
    test.print();*/

    /*KStream<String, String> test = orderedRawData
        .join(apiResponseListTable, (rawDataStringList, apiResposneString) -> {
              //ArrayList<String> sampleStringList = SampledDataCleanAndRet.sampleKafkaData(rawDataStringList);
              //return sampleStringList;
              return "haha";
            }
        );
    test.print();*/


    /*final KStream<String, ArrayList<String>> enrichedDataStream = orderedRawData
        .leftJoin(gaodeApiResponseTable, (rawDataStringList, apiResposneStringList) -> {

          ArrayList<FixedFrequencyAccessData> rawDataList = rawDataStringList
              .stream()
              .map(rawDataString ->
                  SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString))
              .collect(Collectors.toCollection(ArrayList::new));

          ArrayList<String> sampleStringList = SampledDataCleanAndRet.sampleKafkaData(rawDataStringList);
          ArrayList<FixedFrequencyAccessData> sampleList = sampleStringList
              .stream()
              .map(sampleDataString ->
                  SampledDataCleanAndRet.convertToFixedAccessDataPojo(sampleDataString))
              .collect(Collectors.toCollection(ArrayList::new));
          String deviceId = sampleList.get(0).getDeviceId();
          ArrayList<FixedFrequencyIntegrationData> apiResponseList =
              filterAndConvertApiResponseListBasedOnDeviceId(apiResposneStringList, deviceId);
          return SampledDataCleanAndRet.dataIntegration(rawDataList, sampleList, apiResponseList);
        })
        .mapValues(enrichedList ->
            enrichedList
                .stream()
                .map(data -> data.toString())
                .collect(Collectors.toCollection(ArrayList::new)));

    enrichedDataStream.print();*/

    return new KafkaStreams(builder, streamsConfiguration);
  }

  static KafkaStreams buildApiResponseStream(String apiRequestTopic, String apiResponseTopic) {

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
    //streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/Users/qianz/Documents/Misc/Work-beijing/state-store-test");

    KStreamBuilder builder = new KStreamBuilder();
    KStream<String, String> apiRequestStream = builder.stream(apiRequestTopic);

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
                    AutoGraspRequest autoGraspRequest = SampledDataCleanAndRet.convertToAutoGraspRequest(apiRequest);
                    List<FixedFrequencyIntegrationData> gaodeApiResponseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequest);
                    gaodeApiResponseList
                        .stream()
                        .forEach(gaodeApiResponse -> results.add(gaodeApiResponse.toString()));
                  })
              ).collect(Collectors.toList());
          ExternalApiExecutorService.getFuturesWithTimeout(futures, TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS, "calling Gaode API");
          return results;
        });

    KStream<String, String> gaodeApiResponseStream = gaodeApiResponseTable
        .toStream()
        .flatMapValues(
            apiResponseList -> apiResponseList.stream().collect(Collectors.toList())
        );

    gaodeApiResponseStream.to(stringSerde, stringSerde, apiResponseTopic);
    //gaodeApiResponseStream.print();


    return new KafkaStreams(builder, streamsConfiguration);

  }

  /*static KafkaStreams buildSampleDataStream(String inputTopic, String apiReuqestTopic) {
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
            TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOW_LENGTH_IN_SECONDS)),
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
          AutoGraspRequest autoGraspRequest = SampledDataCleanAndRet.autoGraspRequestRet(sampledDataList);
          String valueString;
          if (autoGraspRequest == null)
            valueString = null;
          else
            valueString = autoGraspRequest.toString();
          return new KeyValue<>(String.join("-", String.valueOf(windowStartTime), String.valueOf(windowEndTime)), valueString);
        })
        .filter((key, autoGraspRequestParamString) -> StringUtils.isNotEmpty(autoGraspRequestParamString))
        .to(stringSerde, stringSerde, apiReuqestTopic);

    return new KafkaStreams(builder, streamsConfiguration);

  }*/

  static ArrayList<FixedFrequencyIntegrationData> filterAndConvertApiResponseListBasedOnDeviceId(ArrayList<String> apiResposneStringList, String deviceId) {
    ArrayList<FixedFrequencyIntegrationData> apiResponseList = apiResposneStringList
        .stream()
        .map(apiResposneString ->
            SampledDataCleanAndRet.convertToFixedFrequencyIntegrationDataPojo(apiResposneString)
        )
        .filter(integrationData -> StringUtils.endsWithIgnoreCase(integrationData.getDeviceId(), deviceId))
        .collect(Collectors.toCollection(ArrayList::new));
    return apiResponseList;
  }

}
