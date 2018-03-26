package com.chedaojunan.report.utils;

import com.chedaojunan.report.model.FixedFrequencyAccessData;
import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
import com.chedaojunan.report.model.GaoDeFusionReturn;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WriteDatahubUtilTest {

    private List integrationDataList = null;
    FixedFrequencyAccessData accessData;
    GaoDeFusionReturn gaoDeFusionReturn;

    @Before
    public void init() throws IOException {
        FixedFrequencyIntegrationData integrationData;
        accessData = new FixedFrequencyAccessData();
        accessData.setDevice_id("70211191");
        accessData.setDevice_imei("64691168800");
        accessData.setTrip_id("100");
        accessData.setLocal_time("1521478861000");
        accessData.setServer_time("1521478866000");
        accessData.setEvent_id_list("event_id_list");
        accessData.setFile_id_list("file_id_list");
        accessData.setLatitude(39.00);
        accessData.setLongitude(129.01);
        accessData.setAltitude(30.98);
        accessData.setDirection(98.00);
        accessData.setGps_speed(98.00);
        accessData.setSpeed(98.16);
        accessData.setYawrate(20.3);
        accessData.setAccelerate_z(20.4);
        accessData.setRollrate(20.5);
        accessData.setAccelerate_x(20.6);
        accessData.setPitchrate(20.7);
        accessData.setAccelerate_y(20.8);
        accessData.setTarget_distance(20.9);
        accessData.setTarget_speed(80.01);
        accessData.setTarget_id("target_id");
        accessData.setTarget_type("target_type");
        accessData.setCollision_time(1234.01);
        accessData.setMonitoring_car_num(3);
        accessData.setMonitoring_lane_num(3);
        accessData.setDeviation_distance(18.92);
        accessData.setDeviation_speed(32.32);
        accessData.setTarget_lane_info("target_lane_info");
        accessData.setSource_id("source_id");

        gaoDeFusionReturn = new GaoDeFusionReturn();
        gaoDeFusionReturn.setRoad_api_status(1);
        gaoDeFusionReturn.setCrosspoint("crosspoint");
        gaoDeFusionReturn.setRoadname("roadname");
        gaoDeFusionReturn.setRoadlevel(1);
        gaoDeFusionReturn.setMaxspeed(120);
        gaoDeFusionReturn.setIntersection("intersection");
        gaoDeFusionReturn.setIntersectiondistance("intersectiondistance");
        gaoDeFusionReturn.setTraffic_request_time("1521266461000");
        gaoDeFusionReturn.setTraffic_request_id("traffic_request_id");
        gaoDeFusionReturn.setTraffic_api_status(1);
        // json格式
        String congestion_info = "{\"description\":\"北三环路：从安华桥到苏州桥严重拥堵，蓟门桥附近自西向东行驶缓慢；北四环路：学院桥附近自东向西严重拥堵，安慧桥附近自东向西行驶缓慢；京藏高速：北沙滩桥附近出京方向行驶缓慢。\",\"evaluation\":{\"expedite\":\"44.44%\",\"congested\":\"44.44%\",\"blocked\":\"11.11%\",\"unknown\":\"0.01%\",\"status\":\"3\",\"description\":\"中度拥堵\"}}";
        gaoDeFusionReturn.setCongestion_info(congestion_info);

        integrationData = new FixedFrequencyIntegrationData(accessData, gaoDeFusionReturn);

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
