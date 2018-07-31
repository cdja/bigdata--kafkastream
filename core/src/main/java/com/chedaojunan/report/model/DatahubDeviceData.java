package com.chedaojunan.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class DatahubDeviceData extends FixedFrequencyIntegrationData {

    @JsonProperty("adCode")
    private String adCode; // adcode
    @JsonProperty("townCode")
    private String townCode; //

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public String getTownCode() {
        return townCode;
    }

    public void setTownCode(String townCode) {
        this.townCode = townCode;
    }

    public DatahubDeviceData(){}

    public DatahubDeviceData(FixedFrequencyIntegrationData fixedFrequency, String adCode, String townCode) {
        setDeviceId(fixedFrequency.getDeviceId());
        setDeviceImei(fixedFrequency.getDeviceImei());
        setLocalTime(fixedFrequency.getLocalTime());
        setServerTime(fixedFrequency.getServerTime());
        setTripId(fixedFrequency.getTripId());
        setLatitude(fixedFrequency.getLatitude());
        setLongitude(fixedFrequency.getLongitude());
        setAltitude(fixedFrequency.getAltitude());
        setGpsSpeed(fixedFrequency.getGpsSpeed());
        setDirection(fixedFrequency.getDirection());
        setYawRate(fixedFrequency.getYawRate());
        setAccelerateZ(fixedFrequency.getAccelerateZ());
        setRollRate(fixedFrequency.getRollRate());
        setAccelerateX(fixedFrequency.getAccelerateX());
        setPitchRate(fixedFrequency.getPitchRate());
        setAccelerateY(fixedFrequency.getAccelerateY());
        setSourceId(fixedFrequency.getSourceId());

        setRoadApiStatus(fixedFrequency.getRoadApiStatus());
        setCrosspoint(fixedFrequency.getCrosspoint());
        setRoadName(fixedFrequency.getRoadName());
        setRoadLevel(fixedFrequency.getRoadLevel());
        setMaxSpeed(fixedFrequency.getMaxSpeed());
        setIntersection(fixedFrequency.getIntersection());
        setIntersectionDistance(fixedFrequency.getIntersectionDistance());
        setTrafficRequestTimesamp(fixedFrequency.getTrafficRequestTimesamp());
        setTrafficRequestId(fixedFrequency.getTrafficRequestId());
        setTrafficApiStatus(fixedFrequency.getTrafficApiStatus());
        setCongestionInfo(fixedFrequency.getCongestionInfo());

        setAdCode(adCode);
        setTownCode(townCode);

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