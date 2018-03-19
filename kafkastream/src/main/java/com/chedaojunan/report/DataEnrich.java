package com.chedaojunan.report;

import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chedaojunan.report.service.ExternalApiExecutorService;
import com.chedaojunan.report.utils.KafkaConstants;
import com.chedaojunan.report.utils.ReadProperties;

public class DataEnrich {

  private static final Logger LOG = LoggerFactory.getLogger(DataEnrich.class);

  private static Properties properties = new Properties();

  static {
    properties = ReadProperties.getProperties(KafkaConstants.PROPERTIES_FILE_NAME);
  }

  public static void main(String[] args) throws Exception {

    KStreamBuilder builder = new KStreamBuilder();
    KStream<String, String> rawData = builder.stream(properties.getProperty(KafkaConstants.KAFKA_RAW_DATA_TOPIC));

    //opertations

    KafkaStreams streams = new KafkaStreams(builder, getStreamConfig());

    streams.cleanUp();
    streams.start();

    Runtime.getRuntime().addShutdownHook(new Thread( () -> {
      streams.close();
      ExternalApiExecutorService.closeExecutorService();
    }));
  }

  private static Properties getStreamConfig() {
    final Properties streamsConfiguration = new Properties();

    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, KafkaConstants.KAFKA_STREAM_APPLICATION_NAME);
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
        KafkaConstants.KAFKA_BOOTSTRAP_SERVERS);

    String uuid = UUID.randomUUID().toString();
    String stateDir = "/tmp/" + uuid;
    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);

    // Specify default (de)serializers for record keys and for record values.
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

    // Set the commit interval to 500ms so that any changes are flushed frequently. The low latency
    // would be important for anomaly detection.
    //streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);

    //what to do when there is no offset data in Kafka brokers
    //streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    return streamsConfiguration;
  }


}
