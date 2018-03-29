package com.chedaojunan.report.utils;

import com.chedaojunan.report.model.AutoGraspRequestParam;
import com.chedaojunan.report.model.FixedFrequencyAccessData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SampledDataCleanAndRetTest {

    List<String> batchList;
    ArrayList<String> listSample = null;
    FixedFrequencyAccessData accessData;

    ArrayList<String> batchList02;
    ArrayList<String> listSample02 = null;
    FixedFrequencyAccessData accessData02;

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
    }

    @Test
    public void testSampleKafkaDataGpsSame() {
        listSample = sampledDataCleanAndRet.sampleKafkaData(batchList);
        Assert.assertEquals(3, listSample.size());
    }

    @Test
    public void testSampleKafkaDataGpsDiff() {
        listSample02 = sampledDataCleanAndRet.sampleKafkaData(batchList02);
        Assert.assertEquals(3, listSample02.size());
    }

    @Test
    public void testAutoGraspRequestParamRet() {
        AutoGraspRequestParam autoGraspRequestParam = sampledDataCleanAndRet.autoGraspRequestParamRet(sampledDataCleanAndRet.sampleKafkaData(batchList));
        Assert.assertNotNull(autoGraspRequestParam);
    }

//    @Test
//    public void testDataIntegration() throws IOException{
//        HashMap mapGaoDe = new HashMap();
//        listSample = sampledDataCleanAndRet.sampleKafkaData(batchList);
//        List integrationDataList = sampledDataCleanAndRet.dataIntegration(batchList, listSample, mapGaoDe);
//        Assert.assertNotNull(integrationDataList);
//        Assert.assertEquals(6, integrationDataList.size());
//    }

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