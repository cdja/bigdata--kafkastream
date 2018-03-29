package com.chedaojunan.report.model;

import java.io.IOException;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 固定频率接入数据实体类
 *
 */

public class FixedFrequencyAccessData {

  @JsonProperty(value = "device_id")
  public String deviceId			    ; // 车载adas设备编码/注册时绑定的一个唯一编码

  @JsonProperty(value = "device_imei")
  public String deviceImei			; // 车载adas设备imei号

  @JsonProperty(value = "local_time")
  public String localTime			; // 设备端数据采集的时间戳

  @JsonProperty(value = "server_time")
  public String serverTime			; // 服务端时间戳

  @JsonProperty(value = "trip_id")
  public String tripId				; // 行程ID

  @JsonProperty(value = "lat")
  public double latitude				; // 纬度

  @JsonProperty(value = "longi")
  public double longitude			    ; // 经度

  @JsonProperty(value = "alt")
  public double altitude				; // 海拔

  @JsonProperty(value = "gps_speed")
  public double gpsSpeed	    ; // GPS速度

  @JsonProperty(value = "direction")
  public double direction             ; // 方向角

  @JsonProperty(value = "yawrate")
  public double yawRate				; // 偏航角速度 （Z方向）

  @JsonProperty(value = "accelerate_z")
  public double accelerateZ		    ; // 线性加速度 （Z方向）

  @JsonProperty(value = "rollrate")
  public double rollRate			    ; // 角速度 （X方向）

  @JsonProperty(value = "accelerate_x")
  public double accelerateX		    ; // 线性加速度 （X方向）

  @JsonProperty(value = "pitchrate")
  public double pitchRate			    ; // 角速度 （Y方向）

  @JsonProperty(value = "accelerate_y")
  public double accelerateY		    ; // 线性加速度 （Y方向）

  @JsonProperty(value = "source_id")
  public String sourceId             ; // 数据来源id

  public FixedFrequencyAccessData(){}

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getDeviceImei() {
    return deviceImei;
  }

  public void setDeviceImei(String deviceImei) {
    this.deviceImei = deviceImei;
  }

  public String getLocalTime() {
    return localTime;
  }

  public void setLocalTime(String localTime) {
    this.localTime = localTime;
  }

  public String getServerTime() {
    return serverTime;
  }

  public void setServerTime(String serverTime) {
    this.serverTime = serverTime;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getAltitude() {
    return altitude;
  }

  public void setAltitude(double altitude) {
    this.altitude = altitude;
  }

  public double getGpsSpeed() {
    return gpsSpeed;
  }

  public void setGpsSpeed(double gpsSpeed) {
    this.gpsSpeed = gpsSpeed;
  }

  public double getDirection() {
    return direction;
  }

  public void setDirection(double direction) {
    this.direction = direction;
  }

  public double getYawRate() {
    return yawRate;
  }

  public void setYawRate(double yawRate) {
    this.yawRate = yawRate;
  }

  public double getAccelerateZ() {
    return accelerateZ;
  }

  public void setAccelerateZ(double accelerateZ) {
    this.accelerateZ = accelerateZ;
  }

  public double getRollRate() {
    return rollRate;
  }

  public void setRollRate(double rollRate) {
    this.rollRate = rollRate;
  }

  public double getAccelerateX() {
    return accelerateX;
  }

  public void setAccelerateX(double accelerateX) {
    this.accelerateX = accelerateX;
  }

  public double getPitchRate() {
    return pitchRate;
  }

  public void setPitchRate(double pitchRate) {
    this.pitchRate = pitchRate;
  }

  public double getAccelerateY() {
    return accelerateY;
  }

  public void setAccelerateY(double accelerateY) {
    this.accelerateY = accelerateY;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

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
