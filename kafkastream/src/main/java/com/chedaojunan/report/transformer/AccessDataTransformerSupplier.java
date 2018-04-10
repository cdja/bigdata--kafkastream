package com.chedaojunan.report.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import com.chedaojunan.report.model.FixedFrequencyAccessData;
import com.chedaojunan.report.utils.KafkaConstants;
import com.chedaojunan.report.utils.ReadProperties;
import com.chedaojunan.report.utils.SampledDataCleanAndRet;

public class AccessDataTransformerSupplier
    implements TransformerSupplier<String, String, KeyValue<String, Map<String, ArrayList<FixedFrequencyAccessData>>>> {

  private static int schedulePunctuateInMilliSeconds;

  static {
    schedulePunctuateInMilliSeconds = Integer.parseInt(
        ReadProperties.getProperties(KafkaConstants.PROPERTIES_FILE_NAME, KafkaConstants.KAFKA_WINDOW_DURATION)
    ) * 1000;
  }

  final private String stateStoreName;

  public AccessDataTransformerSupplier(String stateStoreName) {
    this.stateStoreName = stateStoreName;
  }

  @Override
  public Transformer<String, String, KeyValue<String, Map<String, ArrayList<FixedFrequencyAccessData>>>> get() {
    return new Transformer<String, String, KeyValue<String, Map<String, ArrayList<FixedFrequencyAccessData>>>>() {

      private KeyValueStore<String, ArrayList<FixedFrequencyAccessData>> stateStore;

      private ProcessorContext context;

      @SuppressWarnings("unchecked")
      @Override
      public void init(ProcessorContext context) {
        stateStore = (KeyValueStore<String, ArrayList<FixedFrequencyAccessData>>) context.getStateStore(stateStoreName);

        this.context = context;

        this.context.schedule(schedulePunctuateInMilliSeconds, PunctuationType.WALL_CLOCK_TIME, (timstamp) -> {
          /*LocalDateTime dateTime =
              Instant.ofEpochMilli(timstamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
          System.out.println("timestamp: " + dateTime.toString());*/
          KeyValueIterator<String, ArrayList<FixedFrequencyAccessData>> iter = this.stateStore.all();
          Map<String, ArrayList<FixedFrequencyAccessData>> allAccessDataMap = new HashMap<>();
          while (iter.hasNext()) {
            KeyValue<String, ArrayList<FixedFrequencyAccessData>> entry = iter.next();
            ArrayList<FixedFrequencyAccessData> accessDataList = entry.value;
            accessDataList.sort(SampledDataCleanAndRet.sortingByServerTime);
            allAccessDataMap.put(entry.key, accessDataList);
            stateStore.delete(entry.key);
          }
          iter.close();
          if (allAccessDataMap.size() > 0)
            context.forward(timstamp, allAccessDataMap);

          context.commit();
        });
      }

      @Override
      public KeyValue<String, Map<String, ArrayList<FixedFrequencyAccessData>>> transform(String key, String value) {
        //Optional<ArrayList<String>> eventList = Optional.ofNullable(stateStore.get(value));
        FixedFrequencyAccessData accessData = SampledDataCleanAndRet.convertToFixedAccessDataPojo(value);
        String dataKey = String.join(KafkaConstants.HYPHEN, key, accessData.getDeviceId());
        ArrayList<FixedFrequencyAccessData> eventList = stateStore.get(dataKey);
        if (CollectionUtils.isNotEmpty(eventList)) {
          if (!eventList.contains(accessData)) {
            eventList.add(accessData);
            stateStore.put(dataKey, eventList);
          }
        } else
          stateStore.put(dataKey, new ArrayList<>(Arrays.asList(accessData)));
        return null;
      }

      @Override
      public KeyValue<String, Map<String, ArrayList<FixedFrequencyAccessData>>> punctuate(long timestamp) {
        // Not needed
        return null;
      }

      @Override
      public void close() {
        // Note: The store should NOT be closed manually here via `stateStore.close()`!
        // The Kafka Streams API will automatically close stores when necessary.
        stateStore.close();
      }
    };
  }
}
