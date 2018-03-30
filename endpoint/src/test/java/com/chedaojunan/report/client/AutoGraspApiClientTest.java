package com.chedaojunan.report.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.chedaojunan.report.model.AutoGraspRequest;
import com.chedaojunan.report.model.AutoGraspRequestParam;
import com.chedaojunan.report.model.AutoGraspResponse;
import com.chedaojunan.report.model.ExtensionParamEnum;
import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
import com.chedaojunan.report.model.RectangleTrafficInfoResponse;
import com.chedaojunan.report.utils.EndpointConstants;
import com.chedaojunan.report.utils.EndpointUtils;
import com.chedaojunan.report.utils.ObjectMapperUtils;
import com.chedaojunan.report.utils.Pair;
import com.chedaojunan.report.utils.PrepareAutoGraspRequest;
import com.chedaojunan.report.utils.UrlUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.*;

public class AutoGraspApiClientTest {

  private AutoGraspApiClient autoGraspApiClient;
  private AutoGraspRequestParam autoGraspRequestParam;
  private AutoGraspRequest autoGraspRequest;


  @Before
  public void init() throws IOException {
    autoGraspApiClient = AutoGraspApiClient.getInstance();
    String apiKey = EndpointUtils.getEndpointProperties().getProperty(EndpointConstants.GAODE_API_KEY);
    String carId = "abcd123456";
    long time1 = 1434077500;
    long time2 = 1434077501;
    long time3 = 1434077510;
    List<Long> time = new ArrayList<>();
    time.add(time1);
    time.add(time2);
    time.add(time3);
    double direction1 = 1.0;
    double direction2 = 1.0;
    double direction3 = 2.0;
    List<Double> directions = new ArrayList<>();
    directions.add(direction1);
    directions.add(direction2);
    directions.add(direction3);
    double speed1 = 1.0;
    double speed2 = 1.0;
    double speed3 = 2.0;
    List<Double> speed = new ArrayList<>();
    speed.add(speed1);
    speed.add(speed2);
    speed.add(speed3);
    Pair<Double, Double> location1 = new Pair<>(116.496167,39.917066);
    Pair<Double, Double> location2 = new Pair<>(116.496149,39.917205);
    Pair<Double, Double> location3 = new Pair<>(116.496149,39.917326);
    List<Pair<Double, Double>> locations = new ArrayList<>();
    locations.add(location1);
    locations.add(location2);
    locations.add(location3);
    String locationString = PrepareAutoGraspRequest.convertLocationsToRequestString(locations);
    String timeString = PrepareAutoGraspRequest.convertTimeToRequstString(time);
    String speedString  = PrepareAutoGraspRequest.convertSpeedToRequestString(speed);
    String directionString = PrepareAutoGraspRequest.convertDirectionToRequestString(directions);
    autoGraspRequest = new AutoGraspRequest(apiKey, carId, locationString, timeString, directionString, speedString);
  }

  @Test
  public void testGetAutoGraspResponse() throws Exception {
    AutoGraspResponse response = autoGraspApiClient.getAutoGraspResponse(autoGraspRequest);
    Assert.assertNotNull(response);
    Assert.assertEquals(3, response.getCount());
  }

  @Test
  public void testGetTrafficInfoFromAutoGraspResponse() {
    String apiKey = EndpointUtils.getEndpointProperties().getProperty(EndpointConstants.GAODE_API_KEY);
    String carId = "70211192";
    String locationString = "107.997534,26.605624|107.989107,26.596756|107.985385,26.595086";
    String timeString = "1514414,1514414,1514414";
    String directionString = "220.4,243.4,243.4";
    String speedString = "0.0,1.0,2.0";
    autoGraspRequest = new AutoGraspRequest(apiKey, carId, locationString, timeString, directionString, speedString);
    List<FixedFrequencyIntegrationData> gaodeApiResponseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequest);
    Assert.assertEquals(3, gaodeApiResponseList.size());
  }
}