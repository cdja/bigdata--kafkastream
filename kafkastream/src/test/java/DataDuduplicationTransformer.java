import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;

public class DataDuduplicationTransformer<K, V, E> implements Transformer<K, V, KeyValue<K, V>> {

  private static final String storeName = "testStateStore";

  private ProcessorContext context;

  /**
   * Key: event unique identifier - deviceId+local time
   * Value: timestamp (event-time) of the corresponding event when the event ID was seen for the
   * first time
   */
  private WindowStore<E, Long> eventIdStore;

  private final long leftDurationMs;
  private final long rightDurationMs;

  private final KeyValueMapper<K, V, E> idExtractor;

  /**
   * @param maintainDurationPerEventInMs how long to "remember" a known event (or rather, an event
   *                                     ID), during the time of which any incoming duplicates of
   *                                     the event will be dropped, thereby de-duplicating the
   *                                     input.
   * @param idExtractor extracts a unique identifier from a record by which we de-duplicate input
   *                    records; if it returns null, the record will not be considered for
   *                    de-duping but forwarded as-is.
   */
  DataDuduplicationTransformer(long maintainDurationPerEventInMs, KeyValueMapper<K, V, E> idExtractor) {
    if (maintainDurationPerEventInMs < 1) {
      throw new IllegalArgumentException("maintain duration per event must be >= 1");
    }
    leftDurationMs = maintainDurationPerEventInMs / 2;
    rightDurationMs = maintainDurationPerEventInMs - leftDurationMs;
    this.idExtractor = idExtractor;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void init(final ProcessorContext context) {
    this.context = context;
    eventIdStore = (WindowStore<E, Long>) context.getStateStore(storeName);
  }

  @Override
  public KeyValue<K,V> punctuate(long timestamp) {
    // Not needed
    return null;
  }

  @Override
  public void close() {
    // Note: The store should NOT be closed manually here via `eventIdStore.close()`!
    // The Kafka Streams API will automatically close stores when necessary.
  }

  public KeyValue<K, V> transform(final K key, final V value) {
    E eventId = idExtractor.apply(key, value);
    if (eventId == null) {
      return KeyValue.pair(key, value);
    } else {
      KeyValue<K, V> output;
      if (isDuplicate(eventId)) {
        output = null;
        updateTimestampOfExistingEventToPreventExpiry(eventId, context.timestamp());
      } else {
        output = KeyValue.pair(key, value);
        rememberNewEvent(eventId, context.timestamp());
      }
      return output;
    }
  }

  // store closed exception?
  private boolean isDuplicate(final E eventId) {
    long eventTime = context.timestamp();
    WindowStoreIterator<Long> timeIterator = eventIdStore.fetch(
        eventId,
        eventTime - leftDurationMs,
        eventTime + rightDurationMs);
    boolean isDuplicate = timeIterator.hasNext();
    timeIterator.close();
    return isDuplicate;
  }

  private void updateTimestampOfExistingEventToPreventExpiry(final E eventId, long newTimestamp) {
    eventIdStore.put(eventId, newTimestamp, newTimestamp);
  }

  private void rememberNewEvent(final E eventId, long timestamp) {
    eventIdStore.put(eventId, timestamp, timestamp);
  }

}
