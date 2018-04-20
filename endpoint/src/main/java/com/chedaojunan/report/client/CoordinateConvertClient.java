package com.chedaojunan.report.client;

import com.chedaojunan.report.common.Constants;
import com.chedaojunan.report.model.*;
import com.chedaojunan.report.utils.EndpointConstants;
import com.chedaojunan.report.utils.PrepareCoordinateConvertRequest;
import com.chedaojunan.report.utils.ResponseUtils;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CoordinateConvertClient extends Client<CoordinateConvertResponse> {

  private static final Logger LOG = LoggerFactory.getLogger(CoordinateConvertClient.class);
  private static final String API_NAME = "COORDINATE_CONVERT_API";

  private static CoordinateConvertClient instance = null;

  protected CoordinateConvertClient() {
    super();
  }

  public static synchronized CoordinateConvertClient getInstance() {
    LOG.info("Creating CoordinateConvertClient connection");
    return getInstance(instance, CoordinateConvertClient.class, API_NAME);
  }

  /* not needed */
  protected String composeUrl(CoordinateConvertRequestParam coordinateConvertRequestParam) {
    //TODO: add in validation for locations.
    String keyPara = String.join(EndpointConstants.EQUAL_SIGN_DELIMITER, CoordinateConvertRequest.KEY, coordinateConvertRequestParam.getKey());
    String locationsPara = String.join(EndpointConstants.EQUAL_SIGN_DELIMITER, CoordinateConvertRequest.LOCATIONS, PrepareCoordinateConvertRequest.convertLocationsToRequestString(coordinateConvertRequestParam.getLocations()));
    String coordsysPara = String.join(EndpointConstants.EQUAL_SIGN_DELIMITER, CoordinateConvertRequest.COORDSYS, coordinateConvertRequestParam.getCoordsys());
    String requestPara = String.join(EndpointConstants.AMPERSAND_DELIMITER, Arrays.asList(keyPara, locationsPara, coordsysPara));
    return String.join(EndpointConstants.QUESTION_MARK_DELIMITER, url, requestPara);
  }

  protected Request createRequest(CoordinateConvertRequest coordinateConvertRequest) {
    HttpUrl httpUrl = new HttpUrl.Builder()
        .scheme("http")
        .host(url)
        .addPathSegment(apiVersion)
        .addPathSegments(pathSegment)
        .addQueryParameter(CoordinateConvertRequestParam.KEY, coordinateConvertRequest.getKey())
        .addQueryParameter(CoordinateConvertRequestParam.LOCATIONS, coordinateConvertRequest.getLocations())
        .addQueryParameter(CoordinateConvertRequestParam.COORDSYS, coordinateConvertRequest.getCoordsys().toString())
        .build();

    Request request = new Request.Builder()
        .url(httpUrl)
        .build();
    return request;
  }

  public CoordinateConvertResponse getCoordinateConvertResponse(CoordinateConvertRequest coordinateConvertRequest) {
    String coordinateConvertResponseString = getClientResponseJson(createRequest(coordinateConvertRequest));
    return ResponseUtils.convertStringToCoordinateConvertResponse(coordinateConvertResponseString);
  }

  public List<FixedFrequencyAccessData> getCoordinateConvertFromResponse(List<FixedFrequencyAccessData> accessDataListNew, CoordinateConvertRequest coordinateConvertRequest) {

    List<FixedFrequencyAccessData> accessDataList = new ArrayList<>();
    CoordinateConvertResponse coordinateConvertResponse = getCoordinateConvertResponse(coordinateConvertRequest);

    List<String> coordinateConvertResponseGpsList = Arrays.asList(coordinateConvertResponse.getLocations().split(Constants.SEMICOLON));

    FixedFrequencyAccessData accessData;
    for (int i = 0; i < accessDataListNew.size(); i++) {
      accessData = accessDataListNew.get(i);
      if (!StringUtils.isEmpty(coordinateConvertResponseGpsList.get(i))) {
        ResponseUtils.enrichDataWithCoordinateConvertResponse(accessData, coordinateConvertResponseGpsList.get(i));
      }
      accessDataList.add(accessData);
    }

    return accessDataList;
  }

}
