package mq_kafka_test;

import com.alibaba.fastjson.JSON;
import com.chedaojunan.report.model.FixedFrequencyAccessData;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaProducerDemo {

    public static void main(String args[]) {
        if (System.getProperty("java.security.auth.login.config") == null) {
            System.setProperty("java.security.auth.login.config",  KafkaProducerDemo.class.getClassLoader().getResource("kafka_client_jaas.conf").getPath());
        }

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-ons-internet.aliyun.com:8080");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, KafkaProducerDemo.class.getClassLoader().getResource("kafka.client.truststore.jks").getPath());
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "KafkaOnsClient");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "ONS");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

        String message = "This is a message from kafka a";
        FixedFrequencyAccessData fixedFrequencyData = new FixedFrequencyAccessData();
    	fixedFrequencyData.setDevice_id("10000");
        fixedFrequencyData.setDevice_imei("2");
        fixedFrequencyData.setTrip_id("3");
        fixedFrequencyData.setLocal_time("4");
        fixedFrequencyData.setServer_time("5");
        fixedFrequencyData.setEvent_id_list("6");
        fixedFrequencyData.setFile_id_list("7");
        fixedFrequencyData.setLat(8.00);
        fixedFrequencyData.setLongi(9.00);
        fixedFrequencyData.setAlt(10.00);
        fixedFrequencyData.setGps_speed(11.00);
        fixedFrequencyData.setSpeed(12.00);
        fixedFrequencyData.setYawrate(13.00);
        fixedFrequencyData.setAccelerate_z(14.00);
        fixedFrequencyData.setRollrate(15.00);
        fixedFrequencyData.setAccelerate_x(16.00);
        fixedFrequencyData.setPitchrate(17.00);
        fixedFrequencyData.setAccelerate_y(18.00);
        fixedFrequencyData.setTarget_distance(19.00);
        fixedFrequencyData.setTarget_speed(20.00);
        fixedFrequencyData.setTarget_id("21");
        fixedFrequencyData.setTarget_type("22");
        fixedFrequencyData.setCollision_time(23.00);
        fixedFrequencyData.setMonitoring_car_num(24);
        fixedFrequencyData.setMonitoring_lane_num(25);
        fixedFrequencyData.setDeviation_distance(26.00);
        fixedFrequencyData.setDeviation_speed(27.00);
        fixedFrequencyData.setTarget_lane_info("28");
        fixedFrequencyData.setSource_id("29");
        fixedFrequencyData.setDirection(30.33);

        String  topic = System.getProperty("kafka.ons.TOPIC", "test_gw_kafka001");

        Future<RecordMetadata> metadataFuture = producer.send(new ProducerRecord<String, String>(
            topic,
            null,
            System.currentTimeMillis(),
            //String.valueOf(message.hashCode()),
            "test1",
            JSON.toJSONString(fixedFrequencyData)));
        try {
            RecordMetadata recordMetadata = metadataFuture.get();
            System.out.println("produce ok:" + recordMetadata.toString());
        } catch (Exception e) {
            System.out.println("error occurred");
            e.printStackTrace();
        }
        producer.flush();

    }
}
