package com.chedaojunan.report.model;

public class FixedFrequencyAccessGpsData extends FixedFrequencyAccessData {

  private double correctedLatitude				; // 修正后纬度
  private double correctedLongitude			    ; // 修正后经度

  public FixedFrequencyAccessGpsData(){}

  public FixedFrequencyAccessGpsData(FixedFrequencyAccessData accessData, double correctedLatitude, double correctedLongitude) {
    setDeviceId(accessData.getDeviceId());
    setDeviceImei(accessData.getDeviceImei());
    setLocalTime(accessData.getLocalTime());
    setServerTime(accessData.getServerTime());
    setTripId(accessData.getTripId());
    setLatitude(accessData.getLatitude());
    setLongitude(accessData.getLongitude());
    setAltitude(accessData.getAltitude());
    setGpsSpeed(accessData.getGpsSpeed());
    setDirection(accessData.getDirection());
    setYawRate(accessData.getYawRate());
    setAccelerateZ(accessData.getAccelerateZ());
    setRollRate(accessData.getRollRate());
    setAccelerateX(accessData.getAccelerateX());
    setPitchRate(accessData.getPitchRate());
    setAccelerateY(accessData.getAccelerateY());
    setSourceId(accessData.getSourceId());

    setCorrectedLatitude(correctedLatitude);
    setCorrectedLongitude(correctedLongitude);

  }

  public FixedFrequencyAccessGpsData(FixedFrequencyAccessData accessData) {
    setDeviceId(accessData.getDeviceId());
    setDeviceImei(accessData.getDeviceImei());
    setLocalTime(accessData.getLocalTime());
    setServerTime(accessData.getServerTime());
    setTripId(accessData.getTripId());
    setLatitude(accessData.getLatitude());
    setLongitude(accessData.getLongitude());
    setAltitude(accessData.getAltitude());
    setGpsSpeed(accessData.getGpsSpeed());
    setDirection(accessData.getDirection());
    setYawRate(accessData.getYawRate());
    setAccelerateZ(accessData.getAccelerateZ());
    setRollRate(accessData.getRollRate());
    setAccelerateX(accessData.getAccelerateX());
    setPitchRate(accessData.getPitchRate());
    setAccelerateY(accessData.getAccelerateY());
    setSourceId(accessData.getSourceId());
  }

  public double getCorrectedLatitude() {
    return correctedLatitude;
  }

  public void setCorrectedLatitude(double correctedLatitude) {
    this.correctedLatitude = correctedLatitude;
  }

  public double getCorrectedLongitude() {
    return correctedLongitude;
  }

  public void setCorrectedLongitude(double correctedLongitude) {
    this.correctedLongitude = correctedLongitude;
  }

}
