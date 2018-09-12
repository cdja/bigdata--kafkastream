//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.UUID;
//import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.Serde;
//import org.apache.kafka.common.serialization.Serdes;
//import org.apache.kafka.streams.KafkaStreams;
//import org.apache.kafka.streams.KeyValue;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.apache.kafka.streams.StreamsConfig;
//import org.apache.kafka.streams.kstream.KStream;
//import org.apache.kafka.streams.kstream.Materialized;
//import org.apache.kafka.streams.kstream.TimeWindows;
//import org.apache.kafka.streams.state.KeyValueStore;
//import org.apache.kafka.streams.state.StoreBuilder;
//import org.apache.kafka.streams.state.Stores;
//
//import com.chedaojunan.report.client.AutoGraspApiClient;
//import com.chedaojunan.report.model.AutoGraspRequest;
//import com.chedaojunan.report.model.FixedFrequencyAccessData;
//import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
//import com.chedaojunan.report.serdes.ArrayListSerde;
//import com.chedaojunan.report.serdes.SerdeFactory;
//import com.chedaojunan.report.service.ExternalApiExecutorService;
//import com.chedaojunan.report.transformer.AccessDataTransformerSupplier;
//import com.chedaojunan.report.utils.FixedFrequencyAccessDataTimestampExtractor;
//import com.chedaojunan.report.utils.SampledDataCleanAndRet;
//import com.chedaojunan.report.utils.WriteDatahubUtil;
//
//public class KafkaStreamNewTest {
//
//  static final Serde<String> stringSerde = Serdes.String();
//  private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";
//  private static final int WINDOW_LENGTH_IN_SECONDS = 60;
//  private static final long TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS = 10000000000L;
//
//  static AutoGraspApiClient autoGraspApiClient = AutoGraspApiClient.getInstance();
//
//  static Map<String, Object> serdeProp = new HashMap<>();
//
//  private static final Serde<FixedFrequencyAccessData> fixedFrequencyAccessDataSerde = SerdeFactory.createSerde(FixedFrequencyAccessData.class, serdeProp);
//
//  //private static final Serde<FixedFrequencyIntegrationData> fixedFrequencyIntegrationDataSerde = SerdeFactory.createSerde(FixedFrequencyIntegrationData.class, serdeProp);
//
//  //private static final ArrayListSerde<FixedFrequencyAccessData> arrayListAccessDataSerde = new ArrayListSerde<>(fixedFrequencyAccessDataSerde);
//
//  private static final ArrayListSerde<String> arrayListStringSerde = new ArrayListSerde<>(stringSerde);
//
//  public static void main(String[] args) {
//
//    //String rawDataTopic = "hy-raw-data-test";
//    String rawDataTopic = "data-test4";
//
//    final KafkaStreams sampledRawDataStream = buildDataStream(rawDataTopic);
//
//    sampledRawDataStream.start();
//
//    // mock producer
//    /*String dataFile = "testdata1";
//    KafkaProducerTest producerTest = new KafkaProducerTest();
//    producerTest.runProducer(dataFile, rawDataTopic);
//    producerTest.close();*/
//
//    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
//    Runtime.getRuntime().addShutdownHook(new Thread(sampledRawDataStream::close));
//
//  }
//
//  static KafkaStreams buildDataStream(String inputTopic) {
//    final Properties streamsConfiguration = new Properties();
//    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
//    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
//        BOOTSTRAP_SERVERS);
//    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
//        Serdes.String().getClass().getName());
//    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
//        Serdes.String().getClass().getName());
//    streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, FixedFrequencyAccessDataTimestampExtractor.class);
//    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//    //streamsConfiguration.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
//    //streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/state-store-test");
//
//    StreamsBuilder builder = new StreamsBuilder();
//    StoreBuilder<KeyValueStore<String, ArrayList<FixedFrequencyAccessData>>> rawDataStore = Stores.keyValueStoreBuilder(
//        Stores.persistentKeyValueStore("rawDataStore"),
//        Serdes.String(),
//        new ArrayListSerde(fixedFrequencyAccessDataSerde))
//        .withCachingEnabled();
//
//
//    WriteDatahubUtil writeDatahubUtil = new WriteDatahubUtil();
//
//    builder.addStateStore(rawDataStore);
//
//    KStream<String, String> kStream = builder.stream(inputTopic);
//
//    final KStream<String, String> orderedDataStream = kStream
//        .map(
//            (key, rawDataString) ->
//                new KeyValue<>(SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString).getDeviceId(), rawDataString)
//        )
//        .groupByKey()
//        .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOW_LENGTH_IN_SECONDS)).until(TimeUnit.SECONDS.toMillis(WINDOW_LENGTH_IN_SECONDS)))
//        .aggregate(
//            () -> new ArrayList<>(),
//            (windowedCarId, record, list) -> {
//              if (!list.contains(record))
//                list.add(record);
//              return list;
//            },
//            Materialized.with(stringSerde, arrayListStringSerde)
//        )
//        .toStream()
//        .map((windowedString, accessDataList) -> {
//          long windowStartTime = windowedString.window().start();
//          long windowEndTime = windowedString.window().end();
//          String dataKey = String.join("-", String.valueOf(windowStartTime), String.valueOf(windowEndTime));
//          return new KeyValue<>(dataKey, accessDataList);
//        })
//        .flatMapValues(accessDataList -> accessDataList.stream().collect(Collectors.toList()));
//
//    //orderedDataStream.print();
//
//    KStream<String, ArrayList<ArrayList<FixedFrequencyAccessData>>> dedupOrderedDataStream =
//        orderedDataStream.transform(new AccessDataTransformerSupplier(rawDataStore.name()), rawDataStore.name());
//
//    dedupOrderedDataStream
//        .flatMapValues(eventLists ->
//          eventLists
//              .stream()
//              .map(
//                  eventList ->
//                    eventList.stream()
//                        .map(data -> data.getTripId())
//                        .collect(Collectors.toList())
//              ).collect(Collectors.toList())
//        )
//        .print();
//
//    dedupOrderedDataStream
//        .flatMapValues(accessDataLists -> {
//          ArrayList<FixedFrequencyIntegrationData> enrichedDatList = new ArrayList<>();
//          List<Future<?>> futures = accessDataLists
//              .stream()
//              .map(
//                  accessDataList -> ExternalApiExecutorService.getExecutorService().submit(() -> {
//                    accessDataList.sort(SampledDataCleanAndRet.sortingByServerTime);
//                    ArrayList<FixedFrequencyAccessData> sampledDataList = SampledDataCleanAndRet.sampleKafkaData(new ArrayList<>(accessDataList));
//                    AutoGraspRequest autoGraspRequest = SampledDataCleanAndRet.autoGraspRequestRet(sampledDataList);
//                    System.out.println("apiQuest: " + autoGraspRequest);
//                    List<FixedFrequencyIntegrationData> gaodeApiResponseList = new ArrayList<>();
//                    if (autoGraspRequest != null)
//                      gaodeApiResponseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequest);
//                    ArrayList<FixedFrequencyIntegrationData> enrichedData = SampledDataCleanAndRet.dataIntegration(accessDataList, sampledDataList, gaodeApiResponseList);
//                    enrichedData
//                        .stream()
//                        .forEach(data -> enrichedDatList.add(data));
//                  })
//              ).collect(Collectors.toList());
//          ExternalApiExecutorService.getFuturesWithTimeout(futures, TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS, "calling Gaode API");
//          // 整合数据入库datahub
//          if (CollectionUtils.isNotEmpty(enrichedDatList)) {
//            System.out.println("write to DataHub: " + Instant.now().toString());
//            //enrichedDatList.stream().forEach(System.out::println);
//            //writeDatahubUtil.putRecords(enrichedDatList);
//          }
//          return enrichedDatList;
//        });
//
//    /*dedupOrderedDataStream
//        .map((dataKey, accessDataList) -> {
//          ArrayList<FixedFrequencyAccessData> sampledDataList = SampledDataCleanAndRet.sampleKafkaData(accessDataList);
//          AutoGraspRequest autoGraspRequest = SampledDataCleanAndRet.autoGraspRequestRet(sampledDataList);
//          System.out.println("apiQuest: " + autoGraspRequest);
//          List<FixedFrequencyIntegrationData> gaodeApiResponseList = new ArrayList<>();
//          if (autoGraspRequest != null)
//            gaodeApiResponseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequest);
//          ArrayList<FixedFrequencyAccessData> rawDataList = accessDataList
//              .stream()
//              .collect(Collectors.toCollection(ArrayList::new));
//          ArrayList<FixedFrequencyIntegrationData> enrichedData = SampledDataCleanAndRet.dataIntegration(rawDataList, sampledDataList, gaodeApiResponseList);
//          // 整合数据入库datahub
//          if (CollectionUtils.isNotEmpty(enrichedData)) {
//            System.out.println("write to DataHub: " + Instant.now().toString());
//            enrichedData.stream().forEach(System.out::println);
//            writeDatahubUtil.putRecords(enrichedData);
//          }
//          return new KeyValue<>(dataKey, enrichedData);
//        })
//        .flatMapValues(gaodeApiResponseList ->
//            gaodeApiResponseList
//                .stream()
//                .collect(Collectors.toList()));*/
//
//
//    //enrichedDataStream.print();
//
//
//    return new KafkaStreams(builder.build(), streamsConfiguration);
//
//  }
//
//  /*static KafkaStreams buildDataStreamNew (String inputTopic) {
//    final Properties streamsConfiguration = new Properties();
//    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
//    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
//        BOOTSTRAP_SERVERS);
//    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
//        Serdes.String().getClass().getName());
//    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
//        Serdes.String().getClass().getName());
//    streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, com.chedaojunan.report.utils.FixedFrequencyAccessDataTimestampExtractor.class);
//    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//    //streamsConfiguration.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
//    //streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/Users/qianz/Documents/Misc/Work-beijing/state-store-test");
//
//    StreamsBuilder builder = new StreamsBuilder();
//
//    StoreBuilder<KeyValueStore<String, ArrayList<FixedFrequencyAccessData>>> rawDataStore = Stores.keyValueStoreBuilder(
//        Stores.persistentKeyValueStore("rawDataStore"),
//        Serdes.String(),
//        new ArrayListSerde(fixedFrequencyAccessDataSerde))
//        .withCachingEnabled();
//
//
//    WriteDatahubUtil writeDatahubUtil = new WriteDatahubUtil();
//
//    builder.addStateStore(rawDataStore);
//
//    KStream<String, String> kStream = builder.stream(inputTopic);
//
//    final KStream<String, String> orderedDataStream = kStream
//        //final KStream<Windowed<String>, String> enrichedDataStream = kStream
//        .map(
//            (key, rawDataString) ->
//                new KeyValue<>("haha", rawDataString)
//        )
//        .groupByKey()
//        .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOW_LENGTH_IN_SECONDS)).until(TimeUnit.SECONDS.toMillis(WINDOW_LENGTH_IN_SECONDS)))
//        .aggregate(
//            () -> new ArrayList<>(),
//            (windowedCarId, record, list) -> {
//              if (!list.contains(record))
//                list.add(record);
//              return list;
//            },
//            Materialized.with(stringSerde, arrayListStringSerde)
//        )
//        .toStream()
//        .flatMapValues(accessDataList -> accessDataList.stream().collect(Collectors.toList()));
//
//        .mapValues(accessDataStringList ->
//            accessDataStringList
//                .stream()
//                .map(rawAccessDataString ->
//                    SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawAccessDataString))
//                .collect(Collectors.toCollection(ArrayList::new))
//        )
//        //KStream<Windowed<String>, ArrayList<FixedFrequencyAccessData>> -> KStream<Windowed<String>, ArrayList<ArrayList<FixedFrequencyAccessData>>>
//        .mapValues(accessDataList -> {
//          ArrayList<ArrayList<FixedFrequencyAccessData>> accessDataByDeviceIdList = accessDataList
//              .stream()
//              .collect(Collectors.groupingBy(FixedFrequencyAccessData::getDeviceId))
//              .values()
//              .stream()
//              .map(list -> list.stream()
//                  .reduce(new ArrayList<FixedFrequencyAccessData>(),
//                      (list1, data) -> {
//                        list1.add(data);
//                        return list1;
//                      },
//                      (list1, list2) -> {
//                        list1.addAll(list2);
//                        return list1;
//                      })
//              ).collect(Collectors.toCollection(ArrayList::new));
//          return accessDataByDeviceIdList;
//        })
//        //KStream<Windowed<String>, ArrayList<ArrayList<FixedFrequencyAccessData>>>
//        .flatMapValues(accessDataByDeviceIdList -> {
//          ArrayList<FixedFrequencyIntegrationData> enrichedDatList = new ArrayList<>();
//          List<Future<?>> futures = accessDataByDeviceIdList
//              .stream()
//              .map(
//                  accessDataList -> ExternalApiExecutorService.getExecutorService().submit(() -> {
//                    accessDataList.sort(sortingByServerTime);
//                    ArrayList<FixedFrequencyAccessData> sampledDataList = SampledDataCleanAndRet.sampleKafkaData(new ArrayList<>(accessDataList));
//                    AutoGraspRequest autoGraspRequest = SampledDataCleanAndRet.autoGraspRequestRet(sampledDataList);
//                    System.out.println("apiQuest: " + autoGraspRequest);
//                    List<FixedFrequencyIntegrationData> gaodeApiResponseList = new ArrayList<>();
//                    if (autoGraspRequest != null)
//                      gaodeApiResponseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequest);
//                    ArrayList<FixedFrequencyIntegrationData> enrichedData = SampledDataCleanAndRet.dataIntegration(accessDataList, sampledDataList, gaodeApiResponseList);
//                    enrichedData
//                        .stream()
//                        .forEach(data -> enrichedDatList.add(data));
//                  })
//              ).collect(Collectors.toList());
//          ExternalApiExecutorService.getFuturesWithTimeout(futures, TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS, "calling Gaode API");
//          // 整合数据入库datahub
//          if (CollectionUtils.isNotEmpty(enrichedDatList)) {
//            enrichedDatList.forEach(data -> System.out.println(Instant.now().toString() + data));
//            writeDatahubUtil.putRecords(enrichedDatList);
//          }
//          return enrichedDatList;
//        });
//
//    enrichedDataStream.print();
//
//    return new KafkaStreams(builder.build(), streamsConfiguration);
//
//  }*/
//
//}
//
//        /*@SuppressWarnings("unchecked")
//        @Override
//        public void init(ProcessorContext context) {
//          stateStore = (KeyValueStore<String, ArrayList<FixedFrequencyAccessData>>) context.getStateStore(stateStoreName);
//
//          this.context = context;
//
//          this.context.schedule(60000, PunctuationType.WALL_CLOCK_TIME, (timstamp) -> {
//            LocalDateTime dateTime =
//                Instant.ofEpochMilli(timstamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
//            System.out.println("timestamp: " + dateTime.toString());
//            KeyValueIterator<String, ArrayList<FixedFrequencyAccessData>> iter = this.stateStore.all();
//            while (iter.hasNext()) {
//              KeyValue<String, ArrayList<FixedFrequencyAccessData>> entry = iter.next();
//              ArrayList<FixedFrequencyAccessData> accessDataList = entry.value;
//              accessDataList.sort(sortingByServerTime);
//              context.forward(entry.key, accessDataList);
//              stateStore.delete(entry.key);
//            }
//            iter.close();
//
//            context.commit();
//          });
//        }*/