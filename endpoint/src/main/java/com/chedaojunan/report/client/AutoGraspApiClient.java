package com.chedaojunan.report.client;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chedaojunan.report.model.AutoGraspRequestParam;
import com.chedaojunan.report.model.AutoGraspResponse;
import com.chedaojunan.report.utils.EndpointConstants;
import com.chedaojunan.report.utils.PrepareAutoGraspRequest;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

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

  protected String composeUrl(AutoGraspRequestParam autoGraspRequestParam) {
    //TODO: add in validation for locations.
    String carIdPara = StringUtils.join(AutoGraspRequestParam.CAR_ID, autoGraspRequestParam.getCarId(), EndpointConstants.EQUAL_SIGN_DELIMITER);
    String keyPara = StringUtils.join(AutoGraspRequestParam.KEY, autoGraspRequestParam.getKey(), EndpointConstants.EQUAL_SIGN_DELIMITER);
    String locationsPara = StringUtils.join(AutoGraspRequestParam.LOCATIONS, PrepareAutoGraspRequest.convertLocationsToRequestString(autoGraspRequestParam.getLocations()), EndpointConstants.EQUAL_SIGN_DELIMITER);
    String timePara = StringUtils.join(AutoGraspRequestParam.TIME, PrepareAutoGraspRequest.convertTimeToRequstString(autoGraspRequestParam.getTime()), EndpointConstants.EQUAL_SIGN_DELIMITER);
    String directionPara = StringUtils.join(AutoGraspRequestParam.DIRECTION, PrepareAutoGraspRequest.convertDirectionToRequestString(autoGraspRequestParam.getDirection()), EndpointConstants.EQUAL_SIGN_DELIMITER);
    String speedPara = StringUtils.join(AutoGraspRequestParam.SPEED, PrepareAutoGraspRequest.convertSpeedToRequestString(autoGraspRequestParam.getSpeed()), EndpointConstants.EQUAL_SIGN_DELIMITER);
    String requestPara = StringUtils.join(keyPara, carIdPara, locationsPara, timePara, directionPara, speedPara, EndpointConstants.AMPERSAND_DELIMITER);
    return String.join(EndpointConstants.QUESTION_MARK_DELIMITER, url, requestPara);
  }

  protected Request createRequest(AutoGraspRequestParam autoGraspRequestParam) {
    RequestBody requestBody = new FormBody.Builder()
        .add(AutoGraspRequestParam.CAR_ID, autoGraspRequestParam.getCarId())
        .build();
    Request request = new Request.Builder()
        .url(EndpointConstants.GAODE_AUTOGRASP_API_URL)
        .post(requestBody)
        .build();
    return request;
  }

  public AutoGraspResponse getAutoGraspResponse(AutoGraspRequestParam autoGraspRequestParam){
    //return getClientJsonPojo(composeUrl(autoGraspRequestParam), AutoGraspResponse.class);
    return getClientJsonPojo(createRequest(autoGraspRequestParam), AutoGraspResponse.class);
  }

}
