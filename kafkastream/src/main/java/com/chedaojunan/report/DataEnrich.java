package com.chedaojunan.report;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.chedaojunan.report.client.RegeoClient;
import com.chedaojunan.report.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chedaojunan.report.client.AutoGraspApiClient;
import com.chedaojunan.report.serdes.ArrayListSerde;
import com.chedaojunan.report.serdes.SerdeFactory;
import com.chedaojunan.report.service.ExternalApiExecutorService;
import com.chedaojunan.report.transformer.AccessDataTransformerSupplier;
import com.chedaojunan.report.utils.FixedFrequencyAccessDataTimestampExtractor;
import com.chedaojunan.report.utils.KafkaConstants;
import com.chedaojunan.report.utils.ReadProperties;
import com.chedaojunan.report.utils.SampledDataCleanAndRet;
import com.chedaojunan.report.utils.WriteDatahubUtil;

public class DataEnrich {

  private static final Logger logger = LoggerFactory.getLogger(DataEnrich.class);
  private static final long TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS = 10000000000L;

  private static Properties kafkaProperties = null;

  private static final int kafkaWindowLengthInSeconds;

  private static final Serde<String> stringSerde;

  private static final Serde<FixedFrequencyAccessData> fixedFrequencyAccessDataSerde;

  private static final ArrayListSerde<String> arrayListStringSerde;

  private static RegeoClient regeoClient;

  private static AutoGraspApiClient autoGraspApiClient;

  static {
    kafkaProperties = ReadProperties.getProperties(KafkaConstants.PROPERTIES_FILE_NAME);
    stringSerde = Serdes.String();
    Map<String, Object> serdeProp = new HashMap<>();
    fixedFrequencyAccessDataSerde = SerdeFactory.createSerde(FixedFrequencyAccessData.class, serdeProp);
    arrayListStringSerde = new ArrayListSerde<>(stringSerde);
    kafkaWindowLengthInSeconds = Integer.parseInt(kafkaProperties.getProperty(KafkaConstants.KAFKA_WINDOW_DURATION));
    autoGraspApiClient = AutoGraspApiClient.getInstance();
    regeoClient = RegeoClient.getInstance();
  }

  public static void main(String[] args) {
    String rawDataTopic = kafkaProperties.getProperty(KafkaConstants.KAFKA_RAW_DATA_TOPIC);

    final KafkaStreams sampledRawDataStream = buildDataStream(rawDataTopic);

    sampledRawDataStream.start();

    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
    Runtime.getRuntime().addShutdownHook(new Thread(sampledRawDataStream::close));
  }

  private static Properties getStreamConfig() {
    final Properties streamsConfiguration = new Properties();
    String kafkaApplicationName = kafkaProperties.getProperty(KafkaConstants.KAFKA_STREAM_APPLICATION_NAME);
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaApplicationName);
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
        kafkaProperties.getProperty(KafkaConstants.KAFKA_BOOTSTRAP_SERVERS));
    streamsConfiguration.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000);
    // Specify default (de)serializers for record keys and for record values.
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, FixedFrequencyAccessDataTimestampExtractor.class);
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getProperty(KafkaConstants.AUTO_OFFSET_RESET_CONFIG));

    return streamsConfiguration;
  }

  static KafkaStreams buildDataStream(String inputTopic) {
    final Properties streamsConfiguration = getStreamConfig();

    StreamsBuilder builder = new StreamsBuilder();
    StoreBuilder<KeyValueStore<String, ArrayList<FixedFrequencyAccessData>>> rawDataStore = Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore("rawDataStore"),
        Serdes.String(),
        new ArrayListSerde(fixedFrequencyAccessDataSerde))
        .withCachingEnabled();


    WriteDatahubUtil writeDatahubUtil = new WriteDatahubUtil();

    builder.addStateStore(rawDataStore);

    KStream<String, String> kStream = builder.stream(inputTopic);

    final KStream<String, String> orderedDataStream = kStream
        .map(
            (key, rawDataString) ->
                new KeyValue<>(SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString).getDeviceId(), rawDataString)
        )
        .groupByKey()
        .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(kafkaWindowLengthInSeconds)).until(TimeUnit.SECONDS.toMillis(kafkaWindowLengthInSeconds)))
        .aggregate(
            () -> new ArrayList<>(),
            (windowedCarId, record, list) -> {
              if (!list.contains(record))
                list.add(record);
              return list;
            },
            Materialized.with(stringSerde, arrayListStringSerde)
        )
        .toStream()
        .map((windowedString, accessDataList) -> {
          long windowStartTime = windowedString.window().start();
          long windowEndTime = windowedString.window().end();
          String dataKey = String.join("-", String.valueOf(windowStartTime), String.valueOf(windowEndTime));
          return new KeyValue<>(dataKey, accessDataList);
        })
        .flatMapValues(accessDataList -> accessDataList.stream().collect(Collectors.toList()));

    //orderedDataStream.print();

    KStream<String, ArrayList<ArrayList<FixedFrequencyAccessData>>> dedupOrderedDataStream =
        orderedDataStream.transform(new AccessDataTransformerSupplier(rawDataStore.name()), rawDataStore.name());

//    dedupOrderedDataStream
//        .flatMapValues(eventLists ->
//            eventLists
//                .stream()
//                .map(
//                    eventList ->
//                        eventList.stream()
//                            .map(data -> data.getTripId())
//                            .collect(Collectors.toList())
//                ).collect(Collectors.toList())
//        )
//        .print();

    dedupOrderedDataStream
        .flatMapValues(accessDataLists -> {
          ArrayList<DatahubDeviceData> enrichedDataList = new ArrayList<>();
          List<Future<?>> futures = accessDataLists
              .stream()
              .map(
                  accessDataList -> ExternalApiExecutorService.getExecutorService().submit(() -> {
                    // 坐标转化接口调用
                    List<FixedFrequencyAccessGpsData> coordinateConvertResponseList;
                    coordinateConvertResponseList = SampledDataCleanAndRet.getCoordinateConvertResponseList(accessDataList);
//                    System.out.println("coordinateConvertResponseList: " + coordinateConvertResponseList.size());
//                    coordinateConvertResponseList.sort(SampledDataCleanAndRet.sortingByServerTime);
                    ArrayList<FixedFrequencyAccessGpsData> sampledDataList = SampledDataCleanAndRet.sampleKafkaData(new ArrayList<>(coordinateConvertResponseList));
                    AutoGraspRequest autoGraspRequest = SampledDataCleanAndRet.autoGraspRequestRet(sampledDataList);
//                    System.out.println("apiQuest: " + autoGraspRequest);
                    List<FixedFrequencyIntegrationData> gaodeApiResponseList = new ArrayList<>();
                    if (autoGraspRequest != null)
                      gaodeApiResponseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequest);
                    ArrayList<FixedFrequencyIntegrationData> enrichedData = SampledDataCleanAndRet.dataIntegration(coordinateConvertResponseList, sampledDataList, gaodeApiResponseList);

                    ArrayList<DatahubDeviceData> enrichedDataOver = null;
                    if (enrichedData != null) {
                        enrichedDataOver = regeoClient.getRegeoFromResponse(enrichedData);
                    }
                    if (enrichedDataOver != null) {
                        try {
                            // 整合数据入库datahub
                            if (CollectionUtils.isNotEmpty(enrichedDataOver)) {
//                                System.out.println("write to DataHub: " + Instant.now().toString() + "enrichedDataOver.size(): " + enrichedDataOver.size());
                                logger.info("write to DataHub: " + Instant.now().toString() + "enrichedDataOver.size(): " + enrichedDataOver.size());
                                writeDatahubUtil.putRecords(enrichedDataOver);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//                        enrichedDataOver
//                                .stream()
//                                .forEach(data -> enrichedDataList.add(data));
                    }
                  })
              ).collect(Collectors.toList());
          ExternalApiExecutorService.getFuturesWithTimeout(futures, TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS, "calling Gaode API");

          return enrichedDataList;
        });

    return new KafkaStreams(builder.build(), streamsConfiguration);

  }

  /*static KafkaStreams buildDataStreamNew (String inputTopic) {
    final Properties streamsConfiguration = getStreamConfig();

    KStreamBuilder builder = new KStreamBuilder();
    KStream<String, String> kStream = builder.stream(inputTopic);
    WriteDatahubUtil writeDatahubUtil = new WriteDatahubUtil();

    final KStream<Windowed<String>, FixedFrequencyIntegrationData> enrichedDataStream = kStream
        //final KStream<Windowed<String>, String> enrichedDataStream = kStream
        .map(
            (key, rawDataString) ->
                new KeyValue<>(ALL_KEY, rawDataString)
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
            TimeWindows.of(TimeUnit.SECONDS.toMillis(kafkaWindowLengthInSeconds)),
            new ArrayListSerde<>(stringSerde)
        )
        .toStream()
        .mapValues(accessDataStringList ->
            accessDataStringList
                .stream()
                .map(rawAccessDataString ->
                    SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawAccessDataString))
                .collect(Collectors.toCollection(ArrayList::new))
        )
        //KStream<Windowed<String>, ArrayList<FixedFrequencyAccessData>> -> KStream<Windowed<String>, ArrayList<ArrayList<FixedFrequencyAccessData>>>
        .mapValues(accessDataList -> {
          ArrayList<ArrayList<FixedFrequencyAccessData>> accessDataByDeviceIdList = accessDataList
              .stream()
              .collect(Collectors.groupingBy(FixedFrequencyAccessData::getDeviceId))
              .values()
              .stream()
              .map(list -> list.stream()
                  .reduce(new ArrayList<FixedFrequencyAccessData>(),
                      (list1, data) -> {
                        list1.add(data);
                        return list1;
                      },
                      (list1, list2) -> {
                        list1.addAll(list2);
                        return list1;
                      })
              ).collect(Collectors.toCollection(ArrayList::new));
          return accessDataByDeviceIdList;
        })
        //KStream<Windowed<String>, ArrayList<ArrayList<FixedFrequencyAccessData>>>
        .flatMapValues(accessDataByDeviceIdList -> {
          ArrayList<FixedFrequencyIntegrationData> enrichedDatList = new ArrayList<>();
          List<Future<?>> futures = accessDataByDeviceIdList
              .stream()
              .map(
                  accessDataList -> ExternalApiExecutorService.getExecutorService().submit(() -> {
                    accessDataList.sort(sortingByServerTime);
                    ArrayList<FixedFrequencyAccessData> sampledDataList = SampledDataCleanAndRet.sampleKafkaData(new ArrayList<>(accessDataList));
                    AutoGraspRequest autoGraspRequest = SampledDataCleanAndRet.autoGraspRequestRet(sampledDataList);
                    System.out.println("apiQuest: " + autoGraspRequest);
                    List<FixedFrequencyIntegrationData> gaodeApiResponseList = new ArrayList<>();
                    if (autoGraspRequest != null)
                      gaodeApiResponseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequest);
                    ArrayList<FixedFrequencyIntegrationData> enrichedData = SampledDataCleanAndRet.dataIntegration(accessDataList, sampledDataList, gaodeApiResponseList);
                    enrichedData
                        .stream()
                        .forEach(data -> enrichedDatList.add(data));
                  })
              ).collect(Collectors.toList());
          ExternalApiExecutorService.getFuturesWithTimeout(futures, TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS, "calling Gaode API");
          // 整合数据入库datahub
          if (CollectionUtils.isNotEmpty(enrichedDatList)) {
            writeDatahubUtil.putRecords(enrichedDatList);
          }
          return enrichedDatList;
        });

//    enrichedDataStream.print();

    return new KafkaStreams(builder, streamsConfiguration);

  }*/
}
