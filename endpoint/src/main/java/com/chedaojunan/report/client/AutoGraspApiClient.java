package com.chedaojunan.report.client;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chedaojunan.report.model.AutoGraspRequestParam;
import com.chedaojunan.report.model.AutoGraspResponse;
import com.chedaojunan.report.utils.EndpointConstants;
import com.chedaojunan.report.utils.PrepareAutoGraspRequest;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class AutoGraspApiClient extends Client<AutoGraspResponse> {

  private static final Logger LOG = LoggerFactory.getLogger(AutoGraspApiClient.class);
  private static final String API_NAME = "AUTO_GRASP_API";

  private static AutoGraspApiClient instance = null;

  protected AutoGraspApiClient() {
    super();
  }

  public static synchronized AutoGraspApiClient getInstance() {
    LOG.info("Creating AutoGraspApiClient connection");
    return getInstance(instance, AutoGraspApiClient.class, API_NAME);
  }

  /* not needed */
  protected String composeUrl(AutoGraspRequestParam autoGraspRequestParam) {
    //TODO: add in validation for locations.
    String carIdPara = String.join(EndpointConstants.EQUAL_SIGN_DELIMITER, AutoGraspRequestParam.CAR_ID, autoGraspRequestParam.getCarId());
    String keyPara = String.join(EndpointConstants.EQUAL_SIGN_DELIMITER, AutoGraspRequestParam.KEY, autoGraspRequestParam.getKey());
    String locationsPara = String.join(EndpointConstants.EQUAL_SIGN_DELIMITER, AutoGraspRequestParam.LOCATIONS, PrepareAutoGraspRequest.convertLocationsToRequestString(autoGraspRequestParam.getLocations()));
    String timePara = String.join(EndpointConstants.EQUAL_SIGN_DELIMITER, AutoGraspRequestParam.TIME, PrepareAutoGraspRequest.convertTimeToRequstString(autoGraspRequestParam.getTime()));
    String directionPara = String.join(EndpointConstants.EQUAL_SIGN_DELIMITER, AutoGraspRequestParam.DIRECTION, PrepareAutoGraspRequest.convertDirectionToRequestString(autoGraspRequestParam.getDirection()));
    String speedPara = String.join(EndpointConstants.EQUAL_SIGN_DELIMITER, AutoGraspRequestParam.SPEED, PrepareAutoGraspRequest.convertSpeedToRequestString(autoGraspRequestParam.getSpeed()));
    String requestPara = String.join(EndpointConstants.AMPERSAND_DELIMITER, Arrays.asList(keyPara, carIdPara, locationsPara, timePara, directionPara, speedPara));
    return String.join(EndpointConstants.QUESTION_MARK_DELIMITER, url, requestPara);
  }

  protected Request createRequest(AutoGraspRequestParam autoGraspRequestParam) {
    HttpUrl httpUrl = new HttpUrl.Builder()
        .scheme("http")
        .host(url)
        .addPathSegment(apiVersion)
        .addPathSegment(pathSegment)
        .addQueryParameter(AutoGraspRequestParam.KEY, autoGraspRequestParam.getKey())
        .addQueryParameter(AutoGraspRequestParam.CAR_ID, autoGraspRequestParam.getCarId())
        .addQueryParameter(AutoGraspRequestParam.LOCATIONS, PrepareAutoGraspRequest.convertLocationsToRequestString(autoGraspRequestParam.getLocations()))
        .addQueryParameter(AutoGraspRequestParam.TIME, PrepareAutoGraspRequest.convertTimeToRequstString(autoGraspRequestParam.getTime()))
        .addQueryParameter(AutoGraspRequestParam.DIRECTION, PrepareAutoGraspRequest.convertDirectionToRequestString(autoGraspRequestParam.getDirection()))
        .addQueryParameter(AutoGraspRequestParam.SPEED, PrepareAutoGraspRequest.convertSpeedToRequestString(autoGraspRequestParam.getSpeed()))
        .addQueryParameter(AutoGraspRequestParam.EXTENSIONS, autoGraspRequestParam.getExtensionParamEnum().toString())
        .build();

    Request request = new Request.Builder()
        .url(httpUrl)
        .build();
    return request;
  }

  public AutoGraspResponse getAutoGraspResponse(AutoGraspRequestParam autoGraspRequestParam) {
    return getClientJsonPojo(createRequest(autoGraspRequestParam), AutoGraspResponse.class);
  }

}
