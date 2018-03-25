package com.chedaojunan.report.utils;

import com.chedaojunan.report.model.AutoGraspRequestParam;
import com.chedaojunan.report.model.FixedFrequencyAccessData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SampledDataCleanAndRetTest {

    List<FixedFrequencyAccessData> list;
    FixedFrequencyAccessData fixedFrequencyAccessData;

    SampledDataCleanAndRet sampledDataCleanAndRet = new SampledDataCleanAndRet();

    @Before
    public void init() throws IOException {
        list = new ArrayList<>();

        // TODO fixedFrequencyAccessData数据设置
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "001", "1521478861000", "1521478861001",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.11, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "002", "1521478861000", "1521478862002",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.12, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "003", "1521478861000", "1521478863003",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.13, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "004", "1521478861000", "1521478864004",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.14, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "005", "1521478861000", "1521478865005",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.15, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "006", "1521478861000", "1521478866006",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.16, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
    }

    @Test
    public void testSampleKafkaData() throws IOException, CloneNotSupportedException {
        List listResult = sampledDataCleanAndRet.sampleKafkaData(list);
        Assert.assertEquals(3, listResult.size());
    }

    @Test
    public void testAutoGraspRequestParamRet() throws IOException, CloneNotSupportedException{
        AutoGraspRequestParam autoGraspRequestParam = sampledDataCleanAndRet.autoGraspRequestParamRet(sampledDataCleanAndRet.sampleKafkaData(list));
        Assert.assertNotNull(autoGraspRequestParam);
    }

}