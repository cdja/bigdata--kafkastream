package com.chedaojunan.report.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chedaojunan.report.model.AutoGraspRequestParam;
import com.chedaojunan.report.model.AutoGraspResponse;
import com.chedaojunan.report.model.RectangleTrafficInfoRequest;
import com.chedaojunan.report.model.RectangleTrafficInfoResponse;
import com.chedaojunan.report.model.RoadInfo;
import com.chedaojunan.report.utils.EndpointConstants;
import com.chedaojunan.report.utils.Pair;
import com.chedaojunan.report.utils.PrepareAutoGraspRequest;
import com.chedaojunan.report.utils.RequestUtils;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class AutoGraspApiClient extends Client<AutoGraspResponse> {

  private static final Logger LOG = LoggerFactory.getLogger(AutoGraspApiClient.class);
  private static final String API_NAME = "AUTO_GRASP_API";

  private static final String INVALID_CROSSPOINT = "0,0";
  private static final String SEMI_COLON = ";";

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
        .addPathSegments(pathSegment)
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

  public List<RectangleTrafficInfoResponse> getTrafficInfoFromAutoGraspResponse(AutoGraspRequestParam autoGraspRequestParam) {
    AutoGraspResponse autoGraspResponse = getClientJsonPojo(createRequest(autoGraspRequestParam), AutoGraspResponse.class);
    List<String> autoGraspRequestGpsList = autoGraspRequestParam.getLocations()
        .stream()
        .map(Pair::toString)
        .collect(Collectors.toList());
    int dataCount = autoGraspResponse.getCount();
    if (CollectionUtils.isEmpty(autoGraspRequestGpsList) || CollectionUtils.isEmpty(autoGraspResponse.getRoadInfoList()) ||
        (autoGraspRequestGpsList.size() != autoGraspResponse.getRoadInfoList().size()) ||
        autoGraspRequestGpsList.size() != dataCount ||
        autoGraspResponse.getRoadInfoList().size() != dataCount) {
      throw new IllegalArgumentException("autoGrasp locations cannot be matched with roads in response");
    }

    String apiKey = autoGraspRequestParam.getKey();
    RectangleTrafficInfoClient rectangleTrafficInfoClient = RectangleTrafficInfoClient.getInstance();
    List<RectangleTrafficInfoResponse> trafficInfoResponseList =
        IntStream.range(1, Math.min(autoGraspRequestGpsList.size(), autoGraspResponse.getCount()))
        .mapToObj(index -> {
          String validGPS1 = getValidGPS(index-1, autoGraspRequestGpsList, autoGraspResponse.getRoadInfoList());
          String validGPS2 = getValidGPS(index, autoGraspRequestGpsList, autoGraspResponse.getRoadInfoList());
          String trafficInfoRequestRectangle = String.join(SEMI_COLON, validGPS1, validGPS2);
          RectangleTrafficInfoRequest trafficInfoRequest = new RectangleTrafficInfoRequest(apiKey, trafficInfoRequestRectangle, null);
          return rectangleTrafficInfoClient.getTrafficInfoResponse(trafficInfoRequest);
        }).collect(Collectors.toList());

    return trafficInfoResponseList;
  }

  public String getValidGPS(int index, List<String> autoGraspRequestGpsList, List<RoadInfo> autoGraspResponseRoadInfoList) {
    String crosspoint = autoGraspResponseRoadInfoList.get(index).getCrosspoint();
    if(crosspoint.equals(INVALID_CROSSPOINT))
      return autoGraspRequestGpsList.get(index);
    else
      return crosspoint;
  }

}
