package com.chedaojunan.report.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.validation.constraints.NotNull;

/**
 * 固定频率接入数据实体类
 *
 */
public class FixedFrequencyAccessData {

    @NotNull
    public String device_id			    ; // 车载adas设备编码/注册时绑定的一个唯一编码

    @NotNull
    public String device_imei			; // 车载adas设备imei号

    @NotNull
    public String trip_id				; // 行程ID

    @NotNull
    public String local_time			; // 设备端数据采集的时间戳

    @NotNull
    public String server_time			; // 服务端时间戳

    @NotNull
    public String event_id_list		    ; // 事故/预警事件编码列表

    @NotNull
    public String file_id_list		    ; // 事件对应的图片/视频文档唯一编码列表

    @NotNull
    public double latitude				; // 经度

    @NotNull
    public double longitude			    ; // 纬度

    @NotNull
    public double altitude				; // 海拔

    @NotNull
    public	double direction            ; // 方向角

    @NotNull
    public double gps_speed			    ; // GPS速度

    @NotNull
    public double speed				    ; // 本车速度，跟IMU融合之后的速度

    @NotNull
    public double yawrate				; // 偏航角速度 （Z方向）

    @NotNull
    public double accelerate_z		    ; // 线性加速度 （Z方向）

    @NotNull
    public double rollrate			    ; // 角速度 （X方向）

    @NotNull
    public double accelerate_x		    ; // 线性加速度 （X方向）

    @NotNull
    public double pitchrate			    ; // 角速度 （Y方向）

    @NotNull
    public double accelerate_y		    ; // 线性加速度 （Y方向）

    @NotNull
    public double target_distance		; // 前车距离

    @NotNull
    public double target_speed			; // 前车车速

    @NotNull
    public String target_id			    ; // 关键目标ID

    @NotNull
    public String target_type			; // 关键目标类型编码

    @NotNull
    public double collision_time		; // 与前车碰撞时间

    @NotNull
    public int monitoring_car_num		; // 视野内被识别的车辆数

    @NotNull
    public int monitoring_lane_num		; // 视野内被识别的车道数

    @NotNull
    public double deviation_distance	; // 车道偏离距离

    @NotNull
    public double deviation_speed		; // 车道偏离速度

    @NotNull
    public String target_lane_info		; // 目标车道信息编码（虚实、黄白、单双）

    @NotNull
    public String source_id             ; // 数据来源id

    public FixedFrequencyAccessData(){}

    public FixedFrequencyAccessData(String device_id, String device_imei, String trip_id, String local_time, String server_time,
                                    String event_id_list, String file_id_list, double latitude, double longitude, double altitude, double direction,
                                    double gps_speed, double speed, double yawrate, double accelerate_z, double rollrate,
                                    double accelerate_x, double pitchrate, double accelerate_y, double target_distance,
                                    double target_speed, String target_id, String target_type, double collision_time,
                                    int monitoring_car_num, int monitoring_lane_num, double deviation_distance,
                                    double deviation_speed, String target_lane_info, String source_id) {
        this.device_id = device_id;
        this.device_imei = device_imei;
        this.trip_id = trip_id;
        this.local_time = local_time;
        this.server_time = server_time;
        this.event_id_list = event_id_list;
        this.file_id_list = file_id_list;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.direction = direction;
        this.gps_speed = gps_speed;
        this.speed = speed;
        this.yawrate = yawrate;
        this.accelerate_z = accelerate_z;
        this.rollrate = rollrate;
        this.accelerate_x = accelerate_x;
        this.pitchrate = pitchrate;
        this.accelerate_y = accelerate_y;
        this.target_distance = target_distance;
        this.target_speed = target_speed;
        this.target_id = target_id;
        this.target_type = target_type;
        this.collision_time = collision_time;
        this.monitoring_car_num = monitoring_car_num;
        this.monitoring_lane_num = monitoring_lane_num;
        this.deviation_distance = deviation_distance;
        this.deviation_speed = deviation_speed;
        this.target_lane_info = target_lane_info;
        this.source_id = source_id;
    }

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

    public String getEvent_id_list() {
        return event_id_list;
    }

    public void setEvent_id_list(String event_id_list) {
        this.event_id_list = event_id_list;
    }

    public String getFile_id_list() {
        return file_id_list;
    }

    public void setFile_id_list(String file_id_list) {
        this.file_id_list = file_id_list;
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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
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

    public double getTarget_distance() {
        return target_distance;
    }

    public void setTarget_distance(double target_distance) {
        this.target_distance = target_distance;
    }

    public double getTarget_speed() {
        return target_speed;
    }

    public void setTarget_speed(double target_speed) {
        this.target_speed = target_speed;
    }

    public String getTarget_id() {
        return target_id;
    }

    public void setTarget_id(String target_id) {
        this.target_id = target_id;
    }

    public String getTarget_type() {
        return target_type;
    }

    public void setTarget_type(String target_type) {
        this.target_type = target_type;
    }

    public double getCollision_time() {
        return collision_time;
    }

    public void setCollision_time(double collision_time) {
        this.collision_time = collision_time;
    }

    public int getMonitoring_car_num() {
        return monitoring_car_num;
    }

    public void setMonitoring_car_num(int monitoring_car_num) {
        this.monitoring_car_num = monitoring_car_num;
    }

    public int getMonitoring_lane_num() {
        return monitoring_lane_num;
    }

    public void setMonitoring_lane_num(int monitoring_lane_num) {
        this.monitoring_lane_num = monitoring_lane_num;
    }

    public double getDeviation_distance() {
        return deviation_distance;
    }

    public void setDeviation_distance(double deviation_distance) {
        this.deviation_distance = deviation_distance;
    }

    public double getDeviation_speed() {
        return deviation_speed;
    }

    public void setDeviation_speed(double deviation_speed) {
        this.deviation_speed = deviation_speed;
    }

    public String getTarget_lane_info() {
        return target_lane_info;
    }

    public void setTarget_lane_info(String target_lane_info) {
        this.target_lane_info = target_lane_info;
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