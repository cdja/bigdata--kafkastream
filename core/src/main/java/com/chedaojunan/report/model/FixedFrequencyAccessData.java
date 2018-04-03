package com.chedaojunan.report.model;

import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 固定频率接入数据实体类
 *
 */

public class FixedFrequencyAccessData {

  @JsonProperty(value = "device_id")
  private String deviceId			    ; // 车载adas设备编码/注册时绑定的一个唯一编码

  @JsonProperty(value = "device_imei")
  private String deviceImei			; // 车载adas设备imei号

  @JsonProperty(value = "local_time")
  private String localTime			; // 设备端数据采集的时间戳

  @JsonProperty(value = "server_time")
  private String serverTime			; // 服务端时间戳

  @JsonProperty(value = "trip_id")
  private String tripId				; // 行程ID

  @JsonProperty(value = "lat")
  private double latitude				; // 纬度

  @JsonProperty(value = "longi")
  private double longitude			    ; // 经度

  @JsonProperty(value = "alt")
  private double altitude				; // 海拔

  @JsonProperty(value = "gps_speed")
  private double gpsSpeed	    ; // GPS速度

  @JsonProperty(value = "direction")
  private double direction             ; // 方向角

  @JsonProperty(value = "yawrate")
  private double yawRate				; // 偏航角速度 （Z方向）

  @JsonProperty(value = "accelerate_z")
  private double accelerateZ		    ; // 线性加速度 （Z方向）

  @JsonProperty(value = "rollrate")
  private double rollRate			    ; // 角速度 （X方向）

  @JsonProperty(value = "accelerate_x")
  private double accelerateX		    ; // 线性加速度 （X方向）

  @JsonProperty(value = "pitchrate")
  private double pitchRate			    ; // 角速度 （Y方向）

  @JsonProperty(value = "accelerate_y")
  private double accelerateY		    ; // 线性加速度 （Y方向）

  @JsonProperty(value = "source_id")
  private String sourceId             ; // 数据来源id

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

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(deviceId)
        .append(deviceImei)
        .append(localTime)
        .append(serverTime)
        .append(tripId)
        .append(latitude)
        .append(longitude)
        .append(altitude)
        .append(gpsSpeed)
        .append(direction)
        .append(yawRate)
        .append(accelerateX)
        .append(accelerateY)
        .append(accelerateZ)
        .append(rollRate)
        .append(sourceId)
        .append(pitchRate)
        .toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof FixedFrequencyAccessData) == false) {
      return false;
    }
    FixedFrequencyAccessData rhs = ((FixedFrequencyAccessData) other);
    return new EqualsBuilder()
        .append(deviceId, rhs.deviceId)
        .append(deviceImei, rhs.deviceImei)
        .append(localTime, rhs.localTime)
        .append(serverTime, rhs.serverTime)
        .append(latitude, rhs.latitude)
        .append(longitude, rhs.longitude)
        .append(tripId, rhs.tripId)
        .append(altitude, rhs.altitude)
        .append(rollRate, rhs.rollRate)
        .append(yawRate, rhs.yawRate)
        .append(pitchRate, rhs.pitchRate)
        .append(sourceId, rhs.sourceId)
        .append(accelerateX, rhs.accelerateX)
        .append(accelerateY, rhs.accelerateY)
        .append(accelerateZ, rhs.accelerateZ)
        .append(gpsSpeed, rhs.gpsSpeed)
        .append(direction, rhs.direction)
        .isEquals();
  }
}
