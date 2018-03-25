package com.chedaojunan.report.utils;

import com.chedaojunan.report.client.AutoGraspApiClient;
import com.chedaojunan.report.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SampledDataCleanAndRet {

    private static AutoGraspApiClient autoGraspApiClient;
    AutoGraspRequestParam autoGraspRequestParam;
    CalculateUtils calculateUtils = new CalculateUtils();
    private static final Logger log = LoggerFactory.getLogger(SampledDataCleanAndRet.class);

    HashMap gpsMap = new HashMap();

    // 60s数据采样返回
    public List<FixedFrequencyAccessData> sampleKafkaData(List batchList) throws IOException{

        int listSize = batchList.size();
        List sampleOver = new ArrayList(); // 用list存取样后数据
        CopyProperties copyProperties = new CopyProperties();
        int numRange = 50; // 数值取值范围[0,50)
        double decimalDigits = 0.000001;

        // 采集步长
        int stepLength = listSize / 3;
        // 60s内数据少于3条处理
        if (listSize >= 3) {
            FixedFrequencyAccessData accessData1;
            FixedFrequencyAccessData accessData2;
            FixedFrequencyAccessData accessData3;
            FixedFrequencyAccessData accessData4;
            for (int i = 0; i < listSize; i += stepLength) {
                if (i == 0) {
                    accessData4 = (FixedFrequencyAccessData) batchList.get(i);
                    gpsMap.put(accessData4.getLongitude() + "," + accessData4.getLatitude(), accessData4.getLongitude() + "," + accessData4.getLatitude());
                    sampleOver.add(accessData4);
                } else {
                    accessData1 = (FixedFrequencyAccessData) batchList.get(i - stepLength);
                    accessData2 = (FixedFrequencyAccessData) batchList.get(i);
                    accessData3 = new FixedFrequencyAccessData();
                    // TODO 根据经纬度判断数据是否有效
                    if (accessData1.getLatitude() == accessData2.getLatitude()
                            && accessData1.getLongitude() == accessData2.getLongitude()) {
                        accessData3 = copyProperties.clone(accessData2);
                        double longitude = calculateUtils.add(
                                calculateUtils.randomReturn(numRange, decimalDigits), accessData2.getLongitude());
                        double latitude = calculateUtils.add(
                                calculateUtils.randomReturn(numRange, decimalDigits), accessData2.getLatitude());
                        accessData3.setLongitude(longitude);
                        accessData3.setLatitude(latitude);
                        gpsMap.put(longitude + "," + latitude, accessData2.getLongitude() + "," + accessData2.getLatitude());
                    }
                    gpsMap.put(accessData2.getLongitude() + "," + accessData2.getLatitude(), accessData2.getLongitude() + "," + accessData2.getLatitude());
                    sampleOver.add(accessData3);
                }
            }
        // 车停止数据量不足3条，不做数据融合
        } else {
            for (int i = 0; i < listSize; i++) {
                sampleOver.add(batchList.get(i));
            }
        }

        return sampleOver;
    }


    // 返回抓路服务请求参数
    public AutoGraspRequestParam autoGraspRequestParamRet(List<FixedFrequencyAccessData> listSample) throws IOException {
        FixedFrequencyAccessData accessData1;
        FixedFrequencyAccessData accessData2;
        List<Long> times = new ArrayList<>();
        List<Double> directions = new ArrayList<>();
        Double direction;
        AzimuthFromLogLatUtil azimuthFromLogLatUtil;
        AzimuthFromLogLatUtil A;
        AzimuthFromLogLatUtil B;
        List<Double> speeds = new ArrayList<>();
        String apiKey = "";
        String carId = "";
        Pair<Double, Double> location;
        List<Pair<Double, Double>> locations = new ArrayList<>();
        DateUtils dateUtils = new DateUtils();
        int listSampleCount = listSample.size();
        for (int i = 0; i < listSampleCount; i++) {
            if (i==listSampleCount-1) {
                accessData1 = listSample.get(i-1);
                accessData2 = listSample.get(i);

                // TODO 需确认数据端收集的数据格式，并转化为UTC格式
                times.add(accessData2.getServer_time()==""?0L:dateUtils.getUTCTimeFromLocal(Long.valueOf(accessData2.getServer_time())));
                speeds.add(accessData2.getSpeed());
                location = new Pair<>(accessData2.getLongitude(), accessData2.getLatitude());
                locations.add(location);
            } else {
                accessData1 = listSample.get(i);
                accessData2 = listSample.get(i+1);

                // TODO 需确认数据端收集的数据格式，并转化为UTC格式
                times.add(accessData1.getServer_time()==""?0L:dateUtils.getUTCTimeFromLocal(Long.valueOf(accessData1.getServer_time())));
                speeds.add(accessData1.getSpeed());
                location = new Pair<>(accessData1.getLongitude(), accessData1.getLatitude());
                locations.add(location);
            }

            if (i==0) {
                apiKey = EndpointUtils.getEndpointProperties().getProperty(EndpointConstants.GAODE_API_KEY);
                carId = accessData1.getDevice_id();
            }

            // 根据经纬度计算得出
            A = new AzimuthFromLogLatUtil(accessData1.getLongitude(), accessData1.getLatitude());
            B = new AzimuthFromLogLatUtil(accessData2.getLongitude(), accessData2.getLatitude());
            azimuthFromLogLatUtil = new AzimuthFromLogLatUtil();

            direction = azimuthFromLogLatUtil.getAzimuth(A, B);
            if (!Double.isNaN(direction)) {
                directions.add(direction);
            } else {
                directions.add(0.0);
            }
        }

        autoGraspApiClient = AutoGraspApiClient.getInstance();
        autoGraspRequestParam = new AutoGraspRequestParam(apiKey, carId, locations, times, directions, speeds, ExtensionParamEnum.BASE);

        return autoGraspRequestParam;
    }

    // 数据整合
    public List dataIntegration(List<FixedFrequencyAccessData> listSample, HashMap gaoDeMap) throws IOException {
        int listSampleSize = listSample.size();
        int listGaoDeSize = gaoDeMap.size();

        List<FixedFrequencyIntegrationData> integrationDatas = new ArrayList<>();

        if (listSampleSize >= 3) {
            if (listGaoDeSize > 0) {
                // 整合步长
                int stepLength = listSampleSize / 3;
                for (int i = 0; i < listGaoDeSize; i++) {
                    // TODO 获取高德数据整合后实体类
                    gaoDeMap.get(i);
                    FixedFrequencyIntegrationData integrationData;
                    for (int j = i * stepLength; j < (i + 1) * stepLength; j++) {
                        listSample.get(j); // TODO 整合高德数据
                    }
                }
            } else {
                // TODO 高德地图不整合，返回(结构化数据和高德字段设置空)
            }
        } else {
            // TODO 高德地图不整合，返回(结构化数据和高德字段设置空)
        }
        return integrationDatas;
    }

    public static void main(String[] args) throws Exception{

        List<FixedFrequencyAccessData> list = new ArrayList();

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
        // TODO 以下为高德整合返回数据接受对象
        HashMap gaoDeMap = new HashMap();

        // 5.数据整合
        List integrationDataList = sampledData.dataIntegration(listSample, gaoDeMap);

        // 6.入库datahub
        WriteDatahubUtil writeDatahubUtil = new WriteDatahubUtil();
        int failNum = writeDatahubUtil.putRecords(integrationDataList);
        if (failNum > 0) {
            log.info("整合数据入库datahub失败!");
        }
    }

}