package com.chedaojunan.report.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chedaojunan.report.model.RectangleTrafficInfoRequest;
import com.chedaojunan.report.model.RectangleTrafficInfoResponse;
import com.chedaojunan.report.utils.EndpointConstants;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class RectangleTrafficInfoClient extends Client<RectangleTrafficInfoResponse> {

  private static final Logger LOG = LoggerFactory.getLogger(RectangleTrafficInfoClient.class);
  private static final String API_NAME = "RECTANGLE_TRAFFIC_INFO_API";

  private static RectangleTrafficInfoClient instance = null;

  protected RectangleTrafficInfoClient() {
    super();
  }

  public static synchronized RectangleTrafficInfoClient getInstance() {
    LOG.info("Creating RectangleTrafficInfoApiClient connection");
    return getInstance(instance, RectangleTrafficInfoClient.class, API_NAME);
  }

  protected String composeUrl(RectangleTrafficInfoRequest rectangleTrafficInfoRequest) {

    //TODO: add in validation for rectangle.
    String keyPara = String.join(EndpointConstants.EQUAL_SIGN_DELIMITER, RectangleTrafficInfoRequest.KEY, rectangleTrafficInfoRequest.getKey());
    String rectanglePara = String.join(EndpointConstants.EQUAL_SIGN_DELIMITER, RectangleTrafficInfoRequest.RECTANGLE, rectangleTrafficInfoRequest.getRectangle());
    String requestPara = String.join(EndpointConstants.AMPERSAND_DELIMITER, keyPara, rectanglePara);
    return String.join(EndpointConstants.QUESTION_MARK_DELIMITER, url, requestPara);
  }

  protected Request createRequest(RectangleTrafficInfoRequest rectangleTrafficInfoRequest) {
    HttpUrl httpUrl = new HttpUrl.Builder()
        .scheme("http")
        .host(url)
        .addPathSegment(apiVersion)
        .addPathSegments(pathSegment)
        .addQueryParameter(RectangleTrafficInfoRequest.KEY, rectangleTrafficInfoRequest.getKey())
        .addQueryParameter(RectangleTrafficInfoRequest.RECTANGLE, rectangleTrafficInfoRequest.getRectangle())
        .addQueryParameter(RectangleTrafficInfoRequest.EXTENSIONS, rectangleTrafficInfoRequest.getExtensions().toString())
        .build();

    Request request = new Request.Builder()
        .url(httpUrl)
        .build();
    return request;
  }

  public RectangleTrafficInfoResponse getTrafficInfoResponse(RectangleTrafficInfoRequest rectangleTrafficInfoRequest) {
    return getClientJsonPojo(createRequest(rectangleTrafficInfoRequest), RectangleTrafficInfoResponse.class);
  }
}
