package com.chedaojunan.report.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadInfo {

  @JsonProperty("crosspoint")
  private String crosspoint; // 交叉点坐标

  @JsonProperty("roadname")
  private String roadname; // 道路名称

  @JsonProperty("ployline")
  private String polyline; // 道路经纬度坐标

  @JsonProperty("roadlevel")
  private int roadlevel; // 道路等级

  @JsonProperty("maxspeed")
  private int maxspeed; // 道路最高限速

  @JsonProperty("intersection")
  private String[] intersection; // 临近路口

  @JsonProperty("intersectiondistance")
  private double intersectiondistance; // 距离临近路口距离

  public String getCrosspoint() {
    return crosspoint;
  }

  public void setCrosspoint(String crosspoint) {
    this.crosspoint = crosspoint;
  }

  public String getRoadname() {
    return roadname;
  }

  public void setRoadname(String roadname) {
    this.roadname = roadname;
  }

  public String getPolyline() {
    return polyline;
  }

  public void setPolyline(String polyline) {
    this.polyline = polyline;
  }

  public int getRoadlevel() {
    return roadlevel;
  }

  public void setRoadlevel(int roadlevel) {
    this.roadlevel = roadlevel;
  }

  public int getMaxspeed() {
    return maxspeed;
  }

  public void setMaxspeed(int maxspeed) {
    this.maxspeed = maxspeed;
  }

  public String[] getIntersection() {
    return intersection;
  }

  public void setIntersection(String[] intersection) {
    this.intersection = intersection;
  }

  public double getIntersectiondistance() {
    return intersectiondistance;
  }

  public void setIntersectiondistance(double intersectiondistance) {
    this.intersectiondistance = intersectiondistance;
  }
}
