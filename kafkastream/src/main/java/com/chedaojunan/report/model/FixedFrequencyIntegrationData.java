package com.chedaojunan.report.model;

import javax.validation.constraints.NotNull;

/**
 * 固定采集频率整合数据实体类
 *
 */
public class FixedFrequencyIntegrationData extends FixedFrequencyAccessData{

    @NotNull
    private int road_api_status		    ; // 抓路接口返回结果状态：0表示请求失败；1表示请求成功',

    @NotNull
    private String polyline				; // 通过抓路修正的经纬度',

    @NotNull
    private String roadname				; // 道路名称',

    @NotNull
    private int roadlevel			    ; // 道路等级',

    @NotNull
    private int maxspeed				; // 道路最高限速',

    @NotNull
    private String intersection			; // 临近路口',

    @NotNull
    private String intersectiondistance	; // 距离临近路口距离',

    @NotNull
    private String traffic_request_time	; // 调用交通态势接口的时间戳',

    @NotNull
    private String traffic_request_id	; // 每次调用输入变量id （在调用接口中赋一个唯一值）',

    @NotNull
    private int traffic_api_status	    ; // 交通态势接口返回结果状态：0表示请求失败；1表示请求成功',

    @NotNull
    private String congestion_info		; // 交通态势，以key-avalue对的方式存储',

    public FixedFrequencyIntegrationData(){}

    public FixedFrequencyIntegrationData(String device_id, String device_imei, String trip_id, String local_time,
                                         String server_time, String event_id_list, String file_id_list, double latitude,
                                         double longitude, double altitude, double direction, double gps_speed, double speed,
                                         double yawrate, double accelerate_z, double rollrate, double accelerate_x,
                                         double pitchrate, double accelerate_y, int road_api_status, String polyline,
                                         String roadname, int roadlevel, int maxspeed, String intersection,
                                         String intersectiondistance, String traffic_request_time,
                                         String traffic_request_id, int traffic_api_status, String congestion_info,
                                         double target_distance, double target_speed, String target_id, String target_type,
                                         double collision_time, int monitoring_car_num, int monitoring_lane_num,
                                         double deviation_distance, double deviation_speed, String target_lane_info, String source_id) {

        super(device_id, device_imei, trip_id, local_time, server_time, event_id_list, file_id_list, latitude, longitude,
                altitude, direction, gps_speed, speed, yawrate, accelerate_z, rollrate, accelerate_x, pitchrate, accelerate_y,
                target_distance, target_speed, target_id, target_type, collision_time, monitoring_car_num, monitoring_lane_num,
                deviation_distance, deviation_speed, target_lane_info, source_id);

        this.road_api_status = road_api_status;
        this.polyline = polyline;
        this.roadname = roadname;
        this.roadlevel = roadlevel;
        this.maxspeed = maxspeed;
        this.intersection = intersection;
        this.intersectiondistance = intersectiondistance;
        this.traffic_request_time = traffic_request_time;
        this.traffic_request_id = traffic_request_id;
        this.traffic_api_status = traffic_api_status;
        this.congestion_info = congestion_info;
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

    public int getRoad_api_status() {
        return road_api_status;
    }

    public void setRoad_api_status(int road_api_status) {
        this.road_api_status = road_api_status;
    }

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    public String getRoadname() {
        return roadname;
    }

    public void setRoadname(String roadname) {
        this.roadname = roadname;
    }

    public int getRoadlevel() {
        return roadlevel;
    }

    public void setRoadlevel(int roadlevel) {
        this.roadlevel = roadlevel;
    }

    public int getMaxspeed() {
        return maxspeed;
    }

    public void setMaxspeed(int maxspeed) {
        this.maxspeed = maxspeed;
    }

    public String getIntersection() {
        return intersection;
    }

    public void setIntersection(String intersection) {
        this.intersection = intersection;
    }

    public String getIntersectiondistance() {
        return intersectiondistance;
    }

    public void setIntersectiondistance(String intersectiondistance) {
        this.intersectiondistance = intersectiondistance;
    }

    public String getTraffic_request_time() {
        return traffic_request_time;
    }

    public void setTraffic_request_time(String traffic_request_time) {
        this.traffic_request_time = traffic_request_time;
    }

    public String getTraffic_request_id() {
        return traffic_request_id;
    }

    public void setTraffic_request_id(String traffic_request_id) {
        this.traffic_request_id = traffic_request_id;
    }

    public int getTraffic_api_status() {
        return traffic_api_status;
    }

    public void setTraffic_api_status(int traffic_api_status) {
        this.traffic_api_status = traffic_api_status;
    }

    public String getCongestion_info() {
        return congestion_info;
    }

    public void setCongestion_info(String congestion_info) {
        this.congestion_info = congestion_info;
    }

    @Override
    public String toString() {
        return "FixedFrequencyIntegrationData{" +
                "road_api_status=" + road_api_status +
                ", polyline='" + polyline + '\'' +
                ", roadname='" + roadname + '\'' +
                ", roadlevel=" + roadlevel +
                ", maxspeed=" + maxspeed +
                ", intersection='" + intersection + '\'' +
                ", intersectiondistance='" + intersectiondistance + '\'' +
                ", traffic_request_time='" + traffic_request_time + '\'' +
                ", traffic_request_id='" + traffic_request_id + '\'' +
                ", traffic_api_status=" + traffic_api_status +
                ", congestion_info='" + congestion_info + '\'' +
                '}';
    }


}