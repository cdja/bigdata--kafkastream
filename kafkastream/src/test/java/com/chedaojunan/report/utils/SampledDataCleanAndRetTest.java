package com.chedaojunan.report.utils;

import com.chedaojunan.report.model.AutoGraspRequestParam;
import com.chedaojunan.report.model.FixedFrequencyAccessData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SampledDataCleanAndRetTest {

    List<FixedFrequencyAccessData> batchList;
    List listSample = null;
    FixedFrequencyAccessData accessData;

    SampledDataCleanAndRet sampledDataCleanAndRet = new SampledDataCleanAndRet();

    @Before
    public void init() throws IOException {
        batchList = new ArrayList<>();
        // accessData数据设置
        for (int i = 0; i < 6; i++) {
            accessData = new FixedFrequencyAccessData();
            accessData.setDevice_id("70211191");
            accessData.setDevice_imei("64691168800");
            accessData.setTrip_id(i + 100 + "");
            accessData.setLocal_time("1521478861000");
            accessData.setServer_time(1521478866000L + i + "");
            accessData.setLatitude(39.00);
            accessData.setLongitude(129.01);
            accessData.setAltitude(30.98);
            accessData.setDirection(98.00);
            accessData.setGps_speed(98.00);
            accessData.setYawrate(20.3);
            accessData.setAccelerate_z(20.4);
            accessData.setRollrate(20.5);
            accessData.setAccelerate_x(20.6);
            accessData.setPitchrate(20.7);
            accessData.setAccelerate_y(20.8);
            accessData.setSource_id("source_id" + (i + 1));
            batchList.add(accessData);
        }
    }

    @Test
    public void testSampleKafkaData() throws IOException {
        listSample = sampledDataCleanAndRet.sampleKafkaData(batchList);
        Assert.assertEquals(3, listSample.size());
    }

    @Test
    public void testAutoGraspRequestParamRet() throws IOException{
        AutoGraspRequestParam autoGraspRequestParam = sampledDataCleanAndRet.autoGraspRequestParamRet(sampledDataCleanAndRet.sampleKafkaData(batchList));
        Assert.assertNotNull(autoGraspRequestParam);
    }

    @Test
    public void testDataIntegration() throws IOException{
        HashMap mapGaoDe = new HashMap();
        listSample = sampledDataCleanAndRet.sampleKafkaData(batchList);
        List integrationDataList = sampledDataCleanAndRet.dataIntegration(batchList, listSample, mapGaoDe);
        Assert.assertNotNull(integrationDataList);
        Assert.assertEquals(6, integrationDataList.size());
    }

}