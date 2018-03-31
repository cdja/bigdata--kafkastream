package com.chedaojunan.report.utils;

import com.chedaojunan.report.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SampledDataCleanAndRetTest {

    List<String> batchList;
    ArrayList<String> listSample = null;
    FixedFrequencyAccessData accessData;

    ArrayList<String> batchList02;
    ArrayList<String> listSample02 = null;
    FixedFrequencyAccessData accessData02;

    ArrayList<FixedFrequencyAccessData> batchList03;
    ArrayList<FixedFrequencyAccessData> listSample03 = null;
    FixedFrequencyAccessData accessData03;
    List<FixedFrequencyIntegrationData> gaodeApiResponseList = null;

    SampledDataCleanAndRet sampledDataCleanAndRet = new SampledDataCleanAndRet();

    @Before
    public void init() throws IOException {
        batchList = new ArrayList<>();
        // accessData数据设置
        for (int i = 0; i < 6; i++) {
            accessData = new FixedFrequencyAccessData();
            accessData.setDeviceId("70211191");
            accessData.setDeviceImei("64691168800");
            accessData.setTripId(i + 100 + "");
            accessData.setLocalTime("1521478861000");
            accessData.setServerTime(1521478866000L + i + "");
            accessData.setLatitude(39.00);
            accessData.setLongitude(129.01);
            accessData.setAltitude(30.98);
            accessData.setDirection(98.00);
            accessData.setGpsSpeed(98.00);
            accessData.setYawRate(20.3);
            accessData.setAccelerateZ(20.4);
            accessData.setRollRate(20.5);
            accessData.setAccelerateX(20.6);
            accessData.setPitchRate(20.7);
            accessData.setAccelerateY(20.8);
            accessData.setSourceId("source_id_" + (i + 1));
            batchList.add(convertFromFixedAccessDataPojoToStr(accessData));
        }

        batchList02 = new ArrayList<>();
        // accessData02数据设置
        for (int i = 0; i < 6; i++) {
            accessData02 = new FixedFrequencyAccessData();
            accessData02.setDeviceId("70211191");
            accessData02.setDeviceImei("64691168800");
            accessData02.setTripId(i + 100 + "");
            accessData02.setLocalTime("1521478861000");
            accessData02.setServerTime(1521478866000L + i + "");
            accessData02.setLatitude(39.00 + i);
            accessData02.setLongitude(129.01 + i);
            accessData02.setAltitude(30.98);
            accessData02.setDirection(98.00);
            accessData02.setGpsSpeed(98.00);
            accessData02.setYawRate(20.3);
            accessData02.setAccelerateZ(20.4);
            accessData02.setRollRate(20.5);
            accessData02.setAccelerateX(20.6);
            accessData02.setPitchRate(20.7);
            accessData02.setAccelerateY(20.8);
            accessData02.setSourceId("source_id_" + (i + 1));
            batchList02.add(convertFromFixedAccessDataPojoToStr(accessData02));
        }

        batchList03 = new ArrayList<>();
        listSample03 = new ArrayList<>();

        GaoDeFusionReturn gaoDeFusionReturn;
        FixedFrequencyIntegrationData integrationData;
        gaodeApiResponseList = new ArrayList<>();
        // accessData03数据设置
        for (int i = 0; i < 6; i++) {
            accessData03 = new FixedFrequencyAccessData();
            accessData03.setDeviceId("70211191");
            accessData03.setDeviceImei("64691168800");
            accessData03.setTripId(i + 100 + "");
            accessData03.setLocalTime("1521478861000");
            accessData03.setServerTime(1521478866000L + i + "");
            accessData03.setLatitude(39.917066 + i);
            accessData03.setLongitude(116.496167 + i);
            accessData03.setAltitude(30.98);
            accessData03.setDirection(98.00);
            accessData03.setGpsSpeed(98.00);
            accessData03.setYawRate(20.3);
            accessData03.setAccelerateZ(20.4);
            accessData03.setRollRate(20.5);
            accessData03.setAccelerateX(20.6);
            accessData03.setPitchRate(20.7);
            accessData03.setAccelerateY(20.8);
            accessData03.setSourceId("source_id_" + (i + 1));
            if ( i == 0 || i == 2 || i == 4) {
                listSample03.add(accessData03);

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


                integrationData = new FixedFrequencyIntegrationData(accessData03, gaoDeFusionReturn);
                gaodeApiResponseList.add(integrationData);
            }
            batchList03.add(accessData03);
        }
    }

    /*@Test
    public void testSampleKafkaDataGpsSame() {
        listSample = sampledDataCleanAndRet.sampleKafkaData(batchList);
        Assert.assertEquals(3, listSample.size());
    }

    @Test
    public void testSampleKafkaDataGpsDiff() {
        listSample02 = sampledDataCleanAndRet.sampleKafkaData(batchList02);
        Assert.assertEquals(3, listSample02.size());
    }*/

    @Test
    public void testAutoGraspRequestRet() {
        AutoGraspRequest autoGraspRequest = sampledDataCleanAndRet.autoGraspRequestRet(sampledDataCleanAndRet.sampleKafkaData(batchList));
        Assert.assertNotNull(autoGraspRequest);
    }

    @Test
    public void testDataIntegrationGaoDeNoResponseData() throws IOException{
        // TODO test
        List<FixedFrequencyIntegrationData> gaodeApiResponseList = new ArrayList<>();
//        listSample = sampledDataCleanAndRet.sampleKafkaData(batchList);
        List integrationDataList = sampledDataCleanAndRet.dataIntegration(batchList03, listSample03, gaodeApiResponseList);
        Assert.assertNotNull(integrationDataList);
        Assert.assertEquals(6, integrationDataList.size());
    }

    @Test
    public void testDataIntegrationGaoDeWithResponseData() throws IOException{
        // TODO test
//        List<FixedFrequencyIntegrationData> gaodeApiResponseList = new ArrayList<>();
//        listSample = sampledDataCleanAndRet.sampleKafkaData(batchList);
        List integrationDataList = sampledDataCleanAndRet.dataIntegration(batchList03, listSample03, gaodeApiResponseList);
        Assert.assertNotNull(integrationDataList);
        Assert.assertEquals(6, integrationDataList.size());
    }

    public static String convertFromFixedAccessDataPojoToStr(FixedFrequencyAccessData accessData) {

        if (!ObjectUtils.allNotNull(accessData))
            return null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(accessData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}