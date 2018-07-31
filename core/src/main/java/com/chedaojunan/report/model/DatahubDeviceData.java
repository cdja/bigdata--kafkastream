package com.chedao.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class DatahubDeviceData extends DeviceDataPush1007 {

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

    public DatahubDeviceData(DeviceDataPush1007 deviceData, String adCode, String townCode) {
        setDevice_vin(deviceData.getDevice_vin());
        setGpstime(deviceData.getGpstime());
        setLat(deviceData.getLat());
        setLng(deviceData.getLng());
        setDirection(deviceData.getDirection());
        setGps_speed(deviceData.getGps_speed());
        setCanSpeed(deviceData.getCanSpeed());
        setAcc(deviceData.getAcc());
        setRealTimeFuel(deviceData.getRealTimeFuel());
        setLicensePlate(deviceData.getLicensePlate());
        setEcuMileage(deviceData.getEcuMileage());
        setTotalFuelConsumption(deviceData.getTotalFuelConsumption());
        setIntegralGasConsumption(deviceData.getIntegralGasConsumption());
        setAvgGasConsumption(deviceData.getAvgGasConsumption());
        setTotalGasConsumption(deviceData.getTotalGasConsumption());
        setRotations(deviceData.getRotations());
        setLng1Surplus(deviceData.getLng1Surplus());
        setLng2Surplus(deviceData.getLng2Surplus());
        setLng3Surplus(deviceData.getLng3Surplus());
        setLng4Surplus(deviceData.getLng4Surplus());
        setActualEnginePercentTor(deviceData.getActualEnginePercentTor());
        setEngineTorMode(deviceData.getEngineTorMode());
        setDriverEnginePercentTor(deviceData.getDriverEnginePercentTor());
        setAccPedalPos(deviceData.getAccPedalPos());
        setCoolingWaterTem(deviceData.getCoolingWaterTem());
        setOilPressure(deviceData.getOilPressure());
        setTimestamp(deviceData.getTimestamp());

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