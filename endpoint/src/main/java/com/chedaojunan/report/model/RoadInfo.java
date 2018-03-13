package com.chedaojunan.report.model;

public class RoadInfo {
  // 交叉点坐标
  private String crosspoint;
  // 道路名称
  private String roadname;
  // 道路经纬度坐标
  private String polyline;
  // 道路等级
  private int roadlevel;
  // 道路最高限速
  private int maxspeed;
  // 临近路口
  private String intersection;
  // 距离临近路口距离
  private double intersectiondistance;

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

  public String getIntersection() {
    return intersection;
  }

  public void setIntersection(String intersection) {
    this.intersection = intersection;
  }

  public double getIntersectiondistance() {
    return intersectiondistance;
  }

  public void setIntersectiondistance(double intersectiondistance) {
    this.intersectiondistance = intersectiondistance;
  }
}
