package com.chedaojunan.report.client;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chedaojunan.report.model.AutoGraspRequestParam;
import com.chedaojunan.report.model.AutoGraspResponse;
import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
import com.chedaojunan.report.model.RectangleTrafficInfoRequest;
import com.chedaojunan.report.model.RectangleTrafficInfoResponse;
import com.chedaojunan.report.model.RoadInfo;
import com.chedaojunan.report.utils.EndpointConstants;
import com.chedaojunan.report.utils.Pair;
import com.chedaojunan.report.utils.PrepareAutoGraspRequest;

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

  public List<FixedFrequencyIntegrationData> getTrafficInfoFromAutoGraspResponse(AutoGraspRequestParam autoGraspRequestParam) {

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
    List<RoadInfo> roadInfoList = autoGraspResponse.getRoadInfoList();

    List<FixedFrequencyIntegrationData> integrationDataList =
      IntStream.range(1, Math.min(autoGraspRequestGpsList.size(), autoGraspResponse.getCount()))
        .mapToObj(index -> {
          String validGPS1 = getValidGPS(index - 1, autoGraspRequestGpsList, roadInfoList);
          String validGPS2 = getValidGPS(index, autoGraspRequestGpsList, roadInfoList);
          String trafficInfoRequestRectangle = String.join(SEMI_COLON, validGPS1, validGPS2);
          String requestTimestamp = Instant.now().toString();
          String requestId = UUID.randomUUID().toString();

          FixedFrequencyIntegrationData integrationData = new FixedFrequencyIntegrationData();
          enrichDataWithAutoGraspResponse(integrationData, index - 1, autoGraspRequestGpsList, roadInfoList, autoGraspResponse, requestTimestamp, requestId);
          //gaodeApiResponseMap.put(origGPS, integrationData);

          RectangleTrafficInfoRequest trafficInfoRequest = new RectangleTrafficInfoRequest(apiKey, trafficInfoRequestRectangle, requestId, requestTimestamp, null);
          RectangleTrafficInfoResponse trafficInfoResponse = rectangleTrafficInfoClient.getTrafficInfoResponse(trafficInfoRequest);

          enrichDataWithTrafficInfoResponse(integrationData, trafficInfoResponse.getStatus(), trafficInfoResponse.getTrafficInfo().toString());
          //gaodeApiResponseMap.put(origGPS, integrationData);

          return integrationData;
        }).collect(Collectors.toList());

    //replicate traffic info for the last GPS
    int requestGpsListSize = autoGraspRequestGpsList.size();

    //String secondToLastGPS = autoGraspRequestGpsList.get(requestGpsListSize - 2);
    //String lastGPS = autoGraspRequestGpsList.get(requestGpsListSize - 1);
    FixedFrequencyIntegrationData integrationData = new FixedFrequencyIntegrationData();
    String requestTimestamp = Instant.now().toString();
    String requestId = UUID.randomUUID().toString();

    FixedFrequencyIntegrationData integrationDataCopy = integrationDataList.get(integrationDataList.size() - 1);
    enrichDataWithAutoGraspResponse(integrationData, requestGpsListSize-1, autoGraspRequestGpsList, roadInfoList, autoGraspResponse, requestTimestamp, requestId);
    enrichDataWithTrafficInfoResponse(integrationData, integrationDataCopy.getTrafficApiStatus(), integrationDataCopy.getCongestionInfo());
    integrationDataList.add(integrationData);

    return integrationDataList;
  }

  public String getValidGPS(int index, List<String> autoGraspRequestGpsList, List<RoadInfo> autoGraspResponseRoadInfoList) {
    String crosspoint = autoGraspResponseRoadInfoList.get(index).getCrosspoint();
    if(crosspoint.equals(INVALID_CROSSPOINT))
      return autoGraspRequestGpsList.get(index);
    else
      return crosspoint;
  }

  public void enrichDataWithAutoGraspResponse(FixedFrequencyIntegrationData integrationData,
                                              int index, List<String> autoGraspRequestGpsList, List<RoadInfo> roadInfoList,
                                              AutoGraspResponse autoGraspResponse, String requestTimestamp, String requestId) {
    RoadInfo roadInfo = roadInfoList.get(index);
    String validGPS = getValidGPS(index, autoGraspRequestGpsList, roadInfoList);
    integrationData.setRoadApiStatus(autoGraspResponse.getStatus());
    integrationData.setCrosspoint(validGPS);
    integrationData.setRoadName(roadInfo.getRoadname());
    integrationData.setMaxSpeed(roadInfo.getMaxspeed());
    integrationData.setRoadLevel(roadInfo.getRoadlevel());
    integrationData.setIntersection(roadInfo.getIntersection().toString());
    integrationData.setIntersectionDistance(roadInfo.getIntersectiondistance());
    integrationData.setTrafficRequestId(requestId);
    integrationData.setTrafficRequestTimesamp(requestTimestamp);
  }

  public FixedFrequencyIntegrationData enrichDataWithTrafficInfoResponse (FixedFrequencyIntegrationData integrationData,
                                                                          int trafficInfoResponseStatus, String congestionInfo) {
    integrationData.setTrafficApiStatus(trafficInfoResponseStatus);
    integrationData.setCongestionInfo(congestionInfo);

    return integrationData;
  }

}
