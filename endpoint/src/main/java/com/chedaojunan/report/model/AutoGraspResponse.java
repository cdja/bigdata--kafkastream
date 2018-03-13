package com.chedaojunan.report.model;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class AutoGraspResponse {
  private String status;// 结果状态0,表示失败,1:表示成功
  private int count;// 返回结果的数目
  private String info;// 返回状态说明
  private String infoCode; // 返回信息码
  private List<RoadInfo> roadInfoList;// 抓路服务列表

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getInfoCode() {
    return infoCode;
  }

  public void setInfoCode(String infoCode) {
    this.infoCode = infoCode;
  }

  public List<RoadInfo> getRoadInfoList() {
    return roadInfoList;
  }

  public void setRoadInfoList(List<RoadInfo> roadInfoList) {
    this.roadInfoList = roadInfoList;
  }

  /*@Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof AutoGraspResponse) == false) {
      return false;
    }
    AutoGraspResponse rhs = ((AutoGraspResponse) other);
    return new EqualsBuilder().append(data, rhs.data).append(additionalProperties, rhs.additionalProperties).isEquals();
  }*/
}
