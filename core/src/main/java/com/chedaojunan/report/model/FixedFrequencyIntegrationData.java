package com.chedaojunan.report.model;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FixedFrequencyIntegrationData extends FixedFrequencyAccessData {

  @JsonProperty(value = "road_api_status")
  private int road_api_status		    ; // 抓路接口返回结果状态：0表示请求失败；1表示请求成功',
  @JsonProperty(value = "crosspoint")
  private String crosspoint			; // 通过抓路修正的经纬度',
  @JsonProperty(value = "roadname")
  private String roadname				; // 道路名称',
  @JsonProperty(value = "roadlevel")
  private int roadlevel			    ; // 道路等级',
  @JsonProperty(value = "maxspeed")
  private int maxspeed				; // 道路最高限速',
  @JsonProperty(value = "intersection")
  private String intersection			; // 临近路口',
  @JsonProperty(value = "intersectiondistance")
  private String intersectiondistance	; // 距离临近路口距离',
  @JsonProperty(value = "traffic_request_time")
  private String traffic_request_time	; // 调用交通态势接口的时间戳',
  @JsonProperty(value = "traffic_request_id")
  private String traffic_request_id	; // 每次调用输入变量id （在调用接口中赋一个唯一值）',
  @JsonProperty(value = "traffic_api_status")
  private int traffic_api_status	    ; // 交通态势接口返回结果状态：0表示请求失败；1表示请求成功',
  @JsonProperty(value = "congestion_info")
  private String congestion_info		; // 交通态势，以json串的方式存储',

  public FixedFrequencyIntegrationData(){}

  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(this);
    } catch (IOException e) {
      return null;
    }
  }
}
