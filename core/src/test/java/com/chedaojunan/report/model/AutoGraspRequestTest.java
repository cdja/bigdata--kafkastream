package com.chedaojunan.report.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.chedaojunan.report.utils.Pair;

public class AutoGraspRequestTest {

  @Test
  public void testConvertLocationsToRequestString(){
    String carKey = "testKey";
    String carId = "abcd123456";
    long time1 = 1434077500;
    long time2 = 1434077501;
    long time3 = 1434077510;
    List<Long> time = new ArrayList<>();
    time.add(time1);
    time.add(time2);
    time.add(time3);
    int direction1 = 1;
    int direction2 = 1;
    int direction3 = 2;
    List<Integer> directions = new ArrayList<>();
    directions.add(direction1);
    directions.add(direction2);
    directions.add(direction3);
    int speed1 = 1;
    int speed2 = 1;
    int speed3 = 2;
    List<Integer> speed = new ArrayList<>();
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
    AutoGraspRequestParam autoGraspRequestParam = new AutoGraspRequestParam(carKey, carId, locations, time, directions, speed);
    String locationString = autoGraspRequestParam.toString();
    System.out.println(locationString);
    //Assert.assertEquals("116.496167,39.917066|116.496149,39.917205|116.496149,39.917326", locationString);

  }

}