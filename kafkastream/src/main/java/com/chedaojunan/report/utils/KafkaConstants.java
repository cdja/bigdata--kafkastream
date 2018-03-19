package com.chedaojunan.report.utils;

public class KafkaConstants {

  public static final String PROPERTIES_FILE_NAME = "local/application.properties";

  // Kafka brokers
  public static final String KAFKA_STREAM_APPLICATION_NAME = "data.enrich.with.traffic.info";
  public static final String KAFKA_RAW_DATA_TOPIC = "kafka.raw.data.topic";
  public static final String KAFKA_BOOTSTRAP_SERVERS = "bootstrap.servers";
  public static final String KAFKA_WINDOW_DURATION = "kafka.window.duration"; //milliseconds

}
