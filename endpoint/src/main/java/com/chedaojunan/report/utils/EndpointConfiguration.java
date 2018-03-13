package com.chedaojunan.report.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chedaojunan.report.client.AutoGraspApiClient;

public class EndpointConfiguration {

  private final static Logger LOG = LoggerFactory.getLogger(EndpointConfiguration.class);

  private String baseUrl;
  private int readTimeout;
  private int connectTimeout;
  private int maxRetries;
  private int maxIdleConnection;
  private int keepAliveDuration;

  private EndpointConfiguration(String baseUrl, int readTimeout, int connectTimeout, int maxRetries, int maxIdleConnection, int keepAliveDuration) {
    this.baseUrl = baseUrl;
    this.readTimeout = readTimeout;
    this.connectTimeout = connectTimeout;
    this.maxRetries = maxRetries;
    this.maxIdleConnection = maxIdleConnection;
    this.keepAliveDuration = keepAliveDuration;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public int getMaxIdleConnection() {
    return maxIdleConnection;
  }

  public int getKeepAliveDuration() {
    return keepAliveDuration;
  }

  private static Map<Class, EndpointConfiguration> settingsMap = new HashMap<>();

  public static EndpointConfiguration getConfiguration(Class clazz) {
    if (MapUtils.isEmpty(settingsMap)) {
      settingsMap.put(AutoGraspApiClient.class, new EndpointConfiguration(EndpointUtils.getEndpointProperties().getProperty(EndpointConstants.GAODE_AUTOGRASP_API_URL),
          Integer.parseInt(EndpointUtils.getEndpointProperties().getProperty(EndpointConstants.GAODE_AUTOGRASP_API_READ_TIMEOUT)),
          Integer.parseInt(EndpointUtils.getEndpointProperties().getProperty(EndpointConstants.GAODE_AUTOGRASP_API_CONNECT_TIMEOUT)),
          Integer.parseInt(EndpointUtils.getEndpointProperties().getProperty(EndpointConstants.GAODE_AUTOGRASP_API_MAX_CONNECT_RETRY)),
          Integer.parseInt(EndpointUtils.getEndpointProperties().getProperty(EndpointConstants.GAODE_AUTOGRASP_API_POOL_MAX_IDLE_CONNECTIONS)),
          Integer.parseInt(EndpointUtils.getEndpointProperties().getProperty(EndpointConstants.GAODE_AUTOGRASP_API_POOL_KEEP_ALIVE_DURATION))));
    }

    if (settingsMap.containsKey(clazz))
      return settingsMap.get(clazz);

    return null;
  }
}


