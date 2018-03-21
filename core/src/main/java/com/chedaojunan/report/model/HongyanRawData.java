package com.chedaojunan.report.model;

import java.time.ZonedDateTime;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HongyanRawData {

  private static final Logger LOG = LoggerFactory.getLogger(HongyanRawData.class);
  private static final String CAR_ID = "car_id";
  private static final String TERMINAL_ID = "terminal_id";
  private static final String GPS_TIME = "gps_time";
  private static final String LATITUDE = "lat";
  private static final String LONGITUDE = "long";

  @NotNull
  @JsonProperty(CAR_ID)
  private String carId;

  @NotNull
  @JsonProperty(TERMINAL_ID)
  private String terminalId;

  @NotNull
  @JsonProperty(GPS_TIME)
  private String gpsTime;

  @NotNull
  @JsonProperty(LATITUDE)
  private double latitude;

  @NotNull
  @JsonProperty(LONGITUDE)
  private double longitude;

  public String getCarId() {
    return carId;
  }

  public void setCarId(String carId) {
    this.carId = carId;
  }

  public String getTerminalId() {
    return terminalId;
  }

  public void setTerminalId(String terminalId) {
    this.terminalId = terminalId;
  }

  public String getGpsTime() {
    return gpsTime;
  }

  public void setGpsTime(String gpsTime) {
    this.gpsTime = gpsTime;
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

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 31)
        .append(terminalId)
        .append(carId)
        .append(latitude)
        .append(longitude)
        .append(gpsTime).toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof HongyanRawData) == false) {
      return false;
    }
    HongyanRawData rhs = ((HongyanRawData) other);
    return new EqualsBuilder()
        .append(carId, rhs.carId)
        .append(terminalId, rhs.terminalId)
        .append(latitude, rhs.latitude)
        .append(longitude, rhs.longitude)
        .append(gpsTime, rhs.gpsTime).isEquals();
  }
}
