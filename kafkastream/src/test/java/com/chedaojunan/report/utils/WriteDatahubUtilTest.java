package com.chedaojunan.report.utils;

import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WriteDatahubUtilTest {

    private List integrationDataList = null;

    @Before
    public void init() throws IOException {
        FixedFrequencyIntegrationData integrationData;
        String device_id = "70267534";
        String device_imei = "device_imei";
        String trip_id = "trip_id";
        String local_time = "1521266461000";
        String server_time = "1521266461000";
        String event_id_list = "event_id_list";
        String file_id_list = "file_id_list";
        double latitude = 27.662769;
        double longitude = 106.909639;
        double altitude = 80.34;
        double direction = 80.3;
        double gps_speed = 34.8;
        double speed = 38.4;
        double yawrate = 1.0;
        double accelerate_z = 2.0;
        double rollrate = 3.0;
        double accelerate_x = 4.0;
        double pitchrate = 5.0;
        double accelerate_y = 6.0;
        int road_api_status = 1;
        String crosspoint = "crosspoint";
        String roadname = "roadname";
        int roadlevel = 1;
        int maxspeed = 120;
        String intersection = "intersection";
        String intersectiondistance = "intersectiondistance";
        String traffic_request_time = "1521266461000";
        String traffic_request_id = "traffic_request_id";
        int traffic_api_status = 1;

        // json格式
        String congestion_info = "{\"description\":\"北三环路：从安华桥到苏州桥严重拥堵，蓟门桥附近自西向东行驶缓慢；北四环路：学院桥附近自东向西严重拥堵，安慧桥附近自东向西行驶缓慢；京藏高速：北沙滩桥附近出京方向行驶缓慢。\",\"evaluation\":{\"expedite\":\"44.44%\",\"congested\":\"44.44%\",\"blocked\":\"11.11%\",\"unknown\":\"0.01%\",\"status\":\"3\",\"description\":\"中度拥堵\"}}";

        double target_distance = 7.0;
        double target_speed = 8.0;
        String target_id = "target_id";
        String target_type = "target_type";
        double collision_time = 9.0;
        int monitoring_car_num = 1;
        int monitoring_lane_num = 2;
        double deviation_distance = 10.0;
        double deviation_speed = 11.0;
        String target_lane_info = "target_lane_info";
        String source_id = "source_id";

        integrationData = new FixedFrequencyIntegrationData(
                device_id, device_imei, trip_id, local_time,
                server_time, event_id_list, file_id_list, latitude,
                longitude, altitude, direction, gps_speed, speed, yawrate,
                accelerate_z, rollrate, accelerate_x, pitchrate,
                accelerate_y, road_api_status, crosspoint, roadname,
                roadlevel, maxspeed, intersection, intersectiondistance,
                traffic_request_time, traffic_request_id, traffic_api_status,
                congestion_info, target_distance, target_speed, target_id,
                target_type, collision_time, monitoring_car_num, monitoring_lane_num,
                deviation_distance, deviation_speed, target_lane_info, source_id);

        integrationDataList = new ArrayList();
        integrationDataList.add(integrationData);
    }

    @Test
    public void testPutRecords() throws Exception {
        WriteDatahubUtil writeDatahubUtil = new WriteDatahubUtil();
        int failNum = writeDatahubUtil.putRecords(integrationDataList);
        Assert.assertEquals(0, failNum);
    }

}
