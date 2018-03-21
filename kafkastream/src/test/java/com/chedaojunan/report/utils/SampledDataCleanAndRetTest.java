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
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "001", "1521478861000", "1521478861000",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.19, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "001", "1521478861000", "1521478861000",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.19, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "001", "1521478861000", "1521478861000",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.19, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "001", "1521478861000", "1521478861000",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.19, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "001", "1521478861000", "1521478861000",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.19, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
        fixedFrequencyAccessData = new FixedFrequencyAccessData("70211191", "64691168800", "001", "1521478861000", "1521478861000",
                "event_id_list", "file_id_list", 39.00, 129.01, 30.98, 98.00, 98.00, 98.19, 20.3, 20.4, 20.5, 20.6, 20.7, 20.8, 20.9,
                80.01, "target_id", "target_type", 1234.01,3, 3, 18.92,
                32.32, "target_lane_info" , "source_id");
        list.add(fixedFrequencyAccessData);
    }

    @Test
    public void testSampleKafkaData() throws IOException {
        List listResult = sampledDataCleanAndRet.sampleKafkaData(list);
        Assert.assertEquals(3, listResult.size());
    }

    @Test
    public void testAutoGraspRequestParamRet() throws IOException{
        AutoGraspRequestParam autoGraspRequestParam = sampledDataCleanAndRet.autoGraspRequestParamRet(sampledDataCleanAndRet.sampleKafkaData(list));
        Assert.assertNotNull(autoGraspRequestParam);
    }

}