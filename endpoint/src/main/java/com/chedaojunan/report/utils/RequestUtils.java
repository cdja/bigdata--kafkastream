package com.chedaojunan.report.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RequestUtils {

  public static <K, V> Map<K, V> putTwoListsIntoMap (List<K> keys, List<V> values) {
    return IntStream.range(0, keys.size()).boxed()
        .collect(Collectors.toMap(keys::get, values::get));
  }
}
