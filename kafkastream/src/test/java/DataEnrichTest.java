import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chedaojunan.report.client.AutoGraspApiClient;
import com.chedaojunan.report.client.RegeoClient;
import com.chedaojunan.report.model.AutoGraspRequest;
import com.chedaojunan.report.model.DatahubDeviceData;
import com.chedaojunan.report.model.FixedFrequencyAccessData;
import com.chedaojunan.report.model.FixedFrequencyAccessGpsData;
import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
import com.chedaojunan.report.serdes.ArrayListSerde;
import com.chedaojunan.report.serdes.SerdeFactory;
import com.chedaojunan.report.utils.FixedFrequencyAccessDataTimestampExtractor;
import com.chedaojunan.report.utils.KafkaConstants;
import com.chedaojunan.report.utils.ReadProperties;
import com.chedaojunan.report.utils.SampledDataCleanAndRet;
import com.chedaojunan.report.utils.WriteDatahubUtil;

public class DataEnrichTest {

  private static final Logger logger = LoggerFactory.getLogger(DataEnrichTest.class);
  private static final long TIMEOUT_PER_GAODE_API_REQUEST_IN_NANO_SECONDS = 10000000000L;
  private static final String EVENT_ID_DELIMITER = "-";

  private static final String dedupStoreName = "testStateStore";

  private static Properties kafkaProperties = null;

  private static final int kafkaWindowLengthInSeconds;

  private static final Serde<String> stringSerde;

  private static final Serde<FixedFrequencyAccessData> fixedFrequencyAccessDataSerde;

  private static final Serde<FixedFrequencyIntegrationData> fixedFrequencyIntegrationDataSerde;


  private static final ArrayListSerde<String> arrayListStringSerde;

  private static final ArrayListSerde<FixedFrequencyAccessData> arrayListFixedFrequencyDataSerde;

  private static final ArrayListSerde<FixedFrequencyIntegrationData> arrayListFixedFrequencyIntegrationDataSerde;


  private static RegeoClient regeoClient;

  private static AutoGraspApiClient autoGraspApiClient;

  static {
    kafkaProperties = ReadProperties.getProperties(KafkaConstants.PROPERTIES_FILE_NAME);
    stringSerde = Serdes.String();
    Map<String, Object> serdeProp = new HashMap<>();
    fixedFrequencyAccessDataSerde = SerdeFactory.createSerde(FixedFrequencyAccessData.class, serdeProp);
    fixedFrequencyIntegrationDataSerde = SerdeFactory.createSerde(FixedFrequencyIntegrationData.class, serdeProp);
    arrayListStringSerde = new ArrayListSerde<>(stringSerde);
    arrayListFixedFrequencyDataSerde = new ArrayListSerde<>(fixedFrequencyAccessDataSerde);
    arrayListFixedFrequencyIntegrationDataSerde = new ArrayListSerde<>(fixedFrequencyIntegrationDataSerde);
    kafkaWindowLengthInSeconds = Integer.parseInt(kafkaProperties.getProperty(KafkaConstants.KAFKA_WINDOW_DURATION));
    autoGraspApiClient = AutoGraspApiClient.getInstance();
    regeoClient = RegeoClient.getInstance();
  }

  public static void main(String[] args) {
    String rawDataTopic = kafkaProperties.getProperty(KafkaConstants.KAFKA_RAW_DATA_TOPIC);
    String outputTopic = kafkaProperties.getProperty(KafkaConstants.KAFKA_OUTPUT_TOPIC);

    /*
     * 1. sample data within the window
     * 2. make gaode API calls
     * 3. enrich raw data within the window and output to the output topic
    */

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
    streamsConfiguration.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 100000);
    // Specify default (de)serializers for record keys and for record values.
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, FixedFrequencyAccessDataTimestampExtractor.class);
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getProperty(KafkaConstants.AUTO_OFFSET_RESET_CONFIG));
    // The commit interval for flushing records to state stores and downstream must be lower than
    // this integration test's timeout (30 secs) to ensure we observe the expected processing results.
    streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, TimeUnit.SECONDS.toMillis(30));
    // Use a temporary directory for storing state, which will be automatically removed after the test.
    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/Users/qianz/Documents/Misc/Work-beijing/");

    return streamsConfiguration;
  }

  static KafkaStreams buildDataStream(String inputTopic) {
    long maintainDurationPerEventInMs = TimeUnit.MINUTES.toMillis(2);
    // The number of segments has no impact on "correctness".
    // Using more segments implies larger overhead but allows for more fined grained record expiration
    // Note: the specified retention time is a _minimum_ time span and no strict upper time bound
    int numberOfSegments = 3; //???

    // retention period must be at least window size -- for this use case, we don't need a longer retention period
    // and thus just use the window size as retention time
    long retentionPeriod = maintainDurationPerEventInMs;


    final Properties streamsConfiguration = getStreamConfig();

    StreamsBuilder builder = new StreamsBuilder();

    WriteDatahubUtil writeDatahubUtil = new WriteDatahubUtil();
    /*StoreBuilder<KeyValueStore<String, ArrayList<FixedFrequencyAccessData>>> rawDataStore = Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(dedupStoreName),
        Serdes.String(),
        new ArrayListSerde(fixedFrequencyAccessDataSerde));
        //.withCachingEnabled();*/


    StoreBuilder<WindowStore<String, Long>> dedupStoreBuilder = Stores.windowStoreBuilder(
        Stores.persistentWindowStore(dedupStoreName,
            retentionPeriod,
            numberOfSegments,
            maintainDurationPerEventInMs,
            false
        ),
        Serdes.String(),
        Serdes.Long());

    builder.addStateStore(dedupStoreBuilder);

    KStream<String, String> inputStream = builder.stream(inputTopic);

    KStream<String, String> windowedRawData = inputStream
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
          String windowIdString = String.join(KafkaConstants.HYPHEN, String.valueOf(windowStartTime), String.valueOf(windowEndTime));
          return new KeyValue<>(windowIdString, accessDataList);
        })
        .flatMapValues(
            accessDataList ->
                accessDataList
                    .stream()
                    .collect(Collectors.toList())
        );
        //.map((windowedString, rawDataString) -> new KeyValue<>(String.join(KafkaConstants.HYPHEN, windowedString, SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString).getDeviceId()), rawDataString));

    //windowedRawData.print(Printed.toSysOut());

    //windowedRawDataDedup: windowStart-windowEnd, rawDataString
    KStream<String, String> windowedRawDataDedup = windowedRawData
        .transform(
            () -> new DataDuduplicationTransformer<>(
                maintainDurationPerEventInMs,
                (key, value) -> eventIdForDedup(value)),
            dedupStoreName
        );

    //windowedRawDataDedup.print(Printed.toSysOut());

    KStream<String, FixedFrequencyIntegrationData> test1 = windowedRawDataDedup
    //KStream<String, String> test1 = windowedRawDataDedup
        .groupByKey()
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
        .map((windowId, rawDataList) -> {
            List<FixedFrequencyAccessData> accessDataList = rawDataList
                .stream()
                .map(rawDataString -> SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString))
                .collect(Collectors.toList());
            // 坐标转化接口调用
            List<FixedFrequencyAccessGpsData> coordinateConvertResponseList;
            coordinateConvertResponseList = SampledDataCleanAndRet.getCoordinateConvertResponseList(accessDataList);
            ArrayList<FixedFrequencyAccessGpsData> sampledDataList = SampledDataCleanAndRet.sampleKafkaData(new ArrayList<>(coordinateConvertResponseList));
            AutoGraspRequest autoGraspRequest = SampledDataCleanAndRet.autoGraspRequestRet(sampledDataList);
            List<FixedFrequencyIntegrationData> gaodeApiResponseList = new ArrayList<>();
            if (autoGraspRequest != null)
              gaodeApiResponseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequest);
            ArrayList<FixedFrequencyIntegrationData> enrichedDataList = SampledDataCleanAndRet.dataIntegration(coordinateConvertResponseList, sampledDataList, gaodeApiResponseList);
          ArrayList<DatahubDeviceData> enrichedDataOver = null;
          if (enrichedDataList != null) {
            enrichedDataOver = regeoClient.getRegeoFromResponse(enrichedDataList);
          }
          if (enrichedDataOver != null) {
            try {
              // 整合数据入库datahub
              if (CollectionUtils.isNotEmpty(enrichedDataOver)) {
                System.out.println("write to DataHub: " + Instant.now().toString() + "enrichedDataOver.size(): " + enrichedDataOver.size());
                //logger.info("write to DataHub: " + Instant.now().toString() + "enrichedDataOver.size(): " + enrichedDataOver.size());
                writeDatahubUtil.putRecords(enrichedDataOver);
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
            return new KeyValue<>(windowId, enrichedDataList);
        })
        .flatMapValues(
            enrichedDataList ->
                enrichedDataList
                  .stream()
                  //.map(enrichedData -> enrichedData.toString())
                  .collect(Collectors.toList())
        );

    test1.print(Printed.toSysOut());

    return new KafkaStreams(builder.build(), streamsConfiguration);
  }

  private static String eventIdForDedup(String rawDataString){
    return StringUtils.join(EVENT_ID_DELIMITER,
        SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString).getDeviceId(),
        SampledDataCleanAndRet.convertToFixedAccessDataPojo(rawDataString).getLocalTime()
    );
  }
}
