package com.chedaojunan.report.utils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UrlUtils {

  private static final Logger LOG = LoggerFactory.getLogger(UrlUtils.class);

  private OkHttpClient httpClient;

  public UrlUtils(int readTimeout, int connectTimeout, int maxRetry, int maxIdleConnection, int keepAliveDuration) {
    ConnectionPool connectionPool = new ConnectionPool(maxIdleConnection, keepAliveDuration, TimeUnit.SECONDS);
    httpClient = new OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .readTimeout(readTimeout, TimeUnit.SECONDS)
        .connectTimeout(connectTimeout, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        //.cache(new Cache(cacheDir, cacheSize))
        .build();
    httpClient.interceptors().add(new Interceptor() {
      @Override
      public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Response response = doRequest(chain, request);
        int tryCount = 0;
        while (response == null && tryCount <= maxRetry) {
          tryCount++;
          // retry the request
          response = doRequest(chain, request);
        }
        if (response == null) {
          throw new IOException();
        }
        return response;
      }
    });
  }

  private Response doRequest(Interceptor.Chain chain, Request request) {
    Response response = null;
    try {
      response = chain.proceed(request);
    } catch (IOException e) {
      LOG.error("Exception {} for making the request {}", e, request);
    }
    return response;
  }

  public String getJsonFromUrl(Request request, String apiName) {
    //Request request = new Request.Builder().url(url).build();
    String url = request.url().toString();
    try (Response response = httpClient.newCall(request).execute()) {
      if (response == null || response.body() == null || !response.isSuccessful()
          || response.code() != 200 || response.code() != 206) {
        LOG.error("invalid response {}", url);
        return null;
      }
      return response.body().toString();
    } catch (SocketTimeoutException ste) {
      LOG.error("Socket Timeout Exception occurred while getting {} to API {} ", url, apiName);
      return null;
    } catch (IOException e) {
      LOG.error("Exception {} occurred while getting {} from API {}", e, url, apiName);
      return null;
    }
  }
}
