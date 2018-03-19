package com.chedaojunan.report.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.chedaojunan.report.model.AutoGraspRequestParam;
import com.chedaojunan.report.model.AutoGraspResponse;
import com.chedaojunan.report.model.ExtensionParamEnum;
import com.chedaojunan.report.model.RectangleTrafficInfoResponse;
import com.chedaojunan.report.utils.EndpointConstants;
import com.chedaojunan.report.utils.EndpointUtils;
import com.chedaojunan.report.utils.Pair;
import com.chedaojunan.report.utils.UrlUtils;

public class AutoGraspApiClientTest {

  private AutoGraspApiClient autoGraspApiClient;
  private AutoGraspRequestParam autoGraspRequestParam;

  private UrlUtils urlUtils;

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
    Double direction1 = 1.0;
    Double direction2 = 1.0;
    Double direction3 = 2.0;
    List<Double> directions = new ArrayList<>();
    directions.add(direction1);
    directions.add(direction2);
    directions.add(direction3);
    Double speed1 = 1.0;
    Double speed2 = 1.0;
    Double speed3 = 2.0;
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
    autoGraspRequestParam = new AutoGraspRequestParam(apiKey, carId, locations, time, directions, speed, ExtensionParamEnum.BASE);
  }

  @Test
  public void testGetAutoGraspResponse() throws Exception {
    AutoGraspResponse response = autoGraspApiClient.getAutoGraspResponse(autoGraspRequestParam);
    Assert.assertNotNull(response);
    Assert.assertEquals(3, response.getCount());
  }

  @Test
  public void testGetTrafficInfoFromAutoGraspResponse() {
    List<RectangleTrafficInfoResponse> responseList = autoGraspApiClient.getTrafficInfoFromAutoGraspResponse(autoGraspRequestParam);
    Assert.assertEquals(2, responseList.size());
  }
}