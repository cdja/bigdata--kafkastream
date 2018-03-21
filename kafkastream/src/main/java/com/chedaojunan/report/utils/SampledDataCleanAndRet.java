package com.chedaojunan.report.utils;

import com.chedaojunan.report.client.AutoGraspApiClient;
import com.chedaojunan.report.model.AutoGraspRequestParam;
import com.chedaojunan.report.model.AutoGraspResponse;
import com.chedaojunan.report.model.ExtensionParamEnum;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SampledDataCleanAndRet {

    private static AutoGraspApiClient autoGraspApiClient;
    AutoGraspRequestParam autoGraspRequestParam;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SampledDataCleanAndRet.class);

    // 获取60s内，分组排序后的数据，判断采样清洗
    public List<FixedFrequencyAccessData> sampleKafkaData(List list) throws IOException{

        int listSize = list.size();
        List sampleOver = new ArrayList(); // 用list存取样后数据

        // 采集步长
        int stepLength = listSize / 3;
        // 60s内数据少于3条处理
        if (listSize >= 3) {
            for (int i = 0; i < listSize; i += stepLength) {
                sampleOver.add(list.get(i));
            }
            // TODO 根据device_id，time，locations判断是否有效；数据相同，调用无效，数据相同做微调整
//            for (int i = 0; i < sampleOver.size() - 1; i++) {
//                FixedFrequencyAccessData t = (FixedFrequencyAccessData) sampleOver.get(i);
//                FixedFrequencyAccessData t1 = (FixedFrequencyAccessData) sampleOver.get(i + 1);
//                if (t.getServer_time() == t1.getServer_time() || (t.getLat() == t1.getLat() && t.getLongi() == t1.getLongi())) {
//
//                }
//            }
        } // 车停止数据量不足3条，不做数据融合
        else {
            for (int i = 0; i < listSize; i++) {
                sampleOver.add(list.get(i));
            }
        }

        return sampleOver;
    }

    public static void main(String[] args) throws Exception{

        List<FixedFrequencyAccessData> list = new ArrayList<FixedFrequencyAccessData>();

        SampledDataCleanAndRet sampledData = new SampledDataCleanAndRet();
        autoGraspApiClient = AutoGraspApiClient.getInstance();

        // 1.60s数据采样返回
        List<FixedFrequencyAccessData> listSample = sampledData.sampleKafkaData(list);

        if (listSample.size() >= 3) {
            // 2.高德抓路服务参数返回
            AutoGraspRequestParam autoGraspRequestParam = sampledData.autoGraspRequestParamRet(listSample);
            // 3.调用抓路服务
            AutoGraspResponse response = autoGraspApiClient.getAutoGraspResponse(autoGraspRequestParam);
            // 4. TODO 调用交通态势服务参数和服务

        }
        // 5.数据整合

        // 6.入库datahub
//        WriteDatahubUtil writeDatahubUtil = new WriteDatahubUtil();
//        int failNum = writeDatahubUtil.putRecords(integrationDataList);
//        if (failNum > 0) {
//            log.info("整合数据入库datahub失败!");
//        }
    }


    // 返回抓路服务请求参数
    public AutoGraspRequestParam autoGraspRequestParamRet(List<FixedFrequencyAccessData> listSample) throws IOException {
        FixedFrequencyAccessData accessData1;
        FixedFrequencyAccessData accessData2;
        List<Long> times = new ArrayList<>();
        List<Double> directions = new ArrayList<>();
        AzimuthFromLogLatUtil azimuthFromLogLatUtil;
        AzimuthFromLogLatUtil A;
        AzimuthFromLogLatUtil B;
        List<Double> speeds = new ArrayList<>();
        String apiKey = "";
        String carId = "";
        Pair<Double, Double> location;
        List<Pair<Double, Double>> locations = new ArrayList<>();
        int listSampleCount = listSample.size();
        for (int i = 0; i < listSampleCount; i++) {
            if (i==listSampleCount-1) {
                accessData1 = listSample.get(i);
                accessData2 = listSample.get(i);
            } else {
                accessData1 = listSample.get(i);
                accessData2 = listSample.get(i+1);
            }

            if (i==0) {
                apiKey = EndpointUtils.getEndpointProperties().getProperty(EndpointConstants.GAODE_API_KEY);
                carId = accessData1.getDevice_id();
            }

            // TODO 需确认数据端收集的数据格式，并转化为UTC格式
            times.add(accessData1.getServer_time()==""?0L:Long.valueOf(accessData1.getServer_time()));

            // 根据经纬度计算得出
            A = new AzimuthFromLogLatUtil(accessData1.getLongi(), accessData1.getLat());
            B = new AzimuthFromLogLatUtil(accessData2.getLongi(), accessData2.getLat());
            azimuthFromLogLatUtil = new AzimuthFromLogLatUtil();
            directions.add(azimuthFromLogLatUtil.getAngle(A, B));

            speeds.add(accessData1.getSpeed());

            location = new Pair<>(accessData1.getLongi(), accessData1.getLat());
            locations.add(location);
        }

        autoGraspApiClient = AutoGraspApiClient.getInstance();
        autoGraspRequestParam = new AutoGraspRequestParam(apiKey, carId, locations, times, directions, speeds, ExtensionParamEnum.BASE);

        return autoGraspRequestParam;
    }

}