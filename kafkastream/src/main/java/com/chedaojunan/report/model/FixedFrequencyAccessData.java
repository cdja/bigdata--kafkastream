package com.chedaojunan.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 固定频率接入数据实体类
 *
 */
public class FixedFrequencyAccessData {

    @JsonProperty(value = "device_id")
    public String device_id			    ; // 车载adas设备编码/注册时绑定的一个唯一编码
    @JsonProperty(value = "device_imei")
    public String device_imei			; // 车载adas设备imei号
    @JsonProperty(value = "local_time")
    public String local_time			; // 设备端数据采集的时间戳
    @JsonProperty(value = "server_time")
    public String server_time			; // 服务端时间戳
    @JsonProperty(value = "trip_id")
    public String trip_id				; // 行程ID
    @JsonProperty(value = "lat")
    public double latitude				; // 纬度
    @JsonProperty(value = "longi")
    public double longitude			    ; // 经度
    @JsonProperty(value = "alt")
    public double altitude				; // 海拔
    @JsonProperty(value = "gps_speed")
    public double gps_speed			    ; // GPS速度
    @JsonProperty(value = "direction")
    public double direction             ; // 方向角
    @JsonProperty(value = "yawrate")
    public double yawrate				; // 偏航角速度 （Z方向）
    @JsonProperty(value = "accelerate_z")
    public double accelerate_z		    ; // 线性加速度 （Z方向）
    @JsonProperty(value = "rollrate")
    public double rollrate			    ; // 角速度 （X方向）
    @JsonProperty(value = "accelerate_x")
    public double accelerate_x		    ; // 线性加速度 （X方向）
    @JsonProperty(value = "pitchrate")
    public double pitchrate			    ; // 角速度 （Y方向）
    @JsonProperty(value = "accelerate_y")
    public double accelerate_y		    ; // 线性加速度 （Y方向）
    @JsonProperty(value = "source_id")
    public String source_id             ; // 数据来源id

    public FixedFrequencyAccessData(){}

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getDevice_imei() {
        return device_imei;
    }

    public void setDevice_imei(String device_imei) {
        this.device_imei = device_imei;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getLocal_time() {
        return local_time;
    }

    public void setLocal_time(String local_time) {
        this.local_time = local_time;
    }

    public String getServer_time() {
        return server_time;
    }

    public void setServer_time(String server_time) {
        this.server_time = server_time;
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

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public double getGps_speed() {
        return gps_speed;
    }

    public void setGps_speed(double gps_speed) {
        this.gps_speed = gps_speed;
    }

    public double getYawrate() {
        return yawrate;
    }

    public void setYawrate(double yawrate) {
        this.yawrate = yawrate;
    }

    public double getAccelerate_z() {
        return accelerate_z;
    }

    public void setAccelerate_z(double accelerate_z) {
        this.accelerate_z = accelerate_z;
    }

    public double getRollrate() {
        return rollrate;
    }

    public void setRollrate(double rollrate) {
        this.rollrate = rollrate;
    }

    public double getAccelerate_x() {
        return accelerate_x;
    }

    public void setAccelerate_x(double accelerate_x) {
        this.accelerate_x = accelerate_x;
    }

    public double getPitchrate() {
        return pitchrate;
    }

    public void setPitchrate(double pitchrate) {
        this.pitchrate = pitchrate;
    }

    public double getAccelerate_y() {
        return accelerate_y;
    }

    public void setAccelerate_y(double accelerate_y) {
        this.accelerate_y = accelerate_y;
    }

    public String getSource_id() {
        return source_id;
    }

    public void setSource_id(String source_id) {
        this.source_id = source_id;
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