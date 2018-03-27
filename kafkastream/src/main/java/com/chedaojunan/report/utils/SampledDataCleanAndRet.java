package com.chedaojunan.report.utils;

import com.chedaojunan.report.client.AutoGraspApiClient;
import com.chedaojunan.report.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class SampledDataCleanAndRet {

    private static AutoGraspApiClient autoGraspApiClient;
    AutoGraspRequestParam autoGraspRequestParam;
    CalculateUtils calculateUtils = new CalculateUtils();
    private static final Logger log = LoggerFactory.getLogger(SampledDataCleanAndRet.class);

    HashMap gpsMap = new HashMap();

    // 60s数据采样返回
    public List<FixedFrequencyAccessData> sampleKafkaData(List batchList) throws IOException{

        int batchListSize = batchList.size();
        List sampleOver = new ArrayList(); // 用list存取样后数据
        CopyProperties copyProperties = new CopyProperties();
        int numRange = 50; // 数值取值范围[0,50)
        double decimalDigits = 0.000001;

        // 采集步长
        int stepLength = batchListSize / 3;
        // 60s内数据少于3条处理
        if (batchListSize >= 3) {
            FixedFrequencyAccessData accessData1;
            FixedFrequencyAccessData accessData2;
            FixedFrequencyAccessData accessData3;
            FixedFrequencyAccessData accessData4;
            for (int i = 0; i < batchListSize; i += stepLength) {
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
            for (int i = 0; i < batchListSize; i++) {
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
                speeds.add(accessData2.getGps_speed());
                location = new Pair<>(accessData2.getLongitude(), accessData2.getLatitude());
                locations.add(location);
            } else {
                accessData1 = listSample.get(i);
                accessData2 = listSample.get(i+1);

                // TODO 需确认数据端收集的数据格式，并转化为UTC格式
                times.add(accessData1.getServer_time()==""?0L:dateUtils.getUTCTimeFromLocal(Long.valueOf(accessData1.getServer_time())));
                speeds.add(accessData1.getGps_speed());
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
    public List dataIntegration(List<FixedFrequencyAccessData> batchList, List<FixedFrequencyAccessData> listSample, Map mapGaoDe) throws IOException {
        int batchListSize = batchList.size();
        int listSampleSize = listSample.size();
        int mapGaoDeSize = mapGaoDe.size();
        // 整合步长
        int stepLength = batchListSize / 3;

        List<FixedFrequencyIntegrationData> integrationDatas = new ArrayList<>();
        FixedFrequencyIntegrationData integrationData;
        FixedFrequencyAccessData accessData;
        GaoDeFusionReturn gaoDeFusionReturn;

        // 采样数据和高德融合数据大于等于3条，并且两种数据条数相同时
        if (listSampleSize >= 3 && mapGaoDeSize >= 3 && listSampleSize==mapGaoDeSize) {
            for (int i = 0; i < mapGaoDeSize; i++) {
                // TODO 获取高德数据整合后实体类
                gaoDeFusionReturn = (GaoDeFusionReturn)mapGaoDe.get(listSample.get(i * stepLength).getLongitude() + ","
                        + listSample.get(i * stepLength).getLatitude());
                for (int j = i * stepLength; j < (i + 1) * stepLength; j++) {
                    if (batchListSize > j) {
                        // TODO 整合高德数据
                        integrationData = new FixedFrequencyIntegrationData(batchList.get(j), gaoDeFusionReturn);
                        integrationDatas.add(integrationData);
                    }
                }
            }
        } else {
            // TODO 高德地图不整合，返回(结构化数据和高德字段设置空)
            for (int i = 0; i < batchListSize; i++) {
                accessData = batchList.get(i);
                integrationData = new FixedFrequencyIntegrationData(accessData);
                integrationDatas.add(integrationData);
            }
        }
        return integrationDatas;
    }

    public static void main(String[] args) throws Exception{

        List<FixedFrequencyAccessData> batchList = new ArrayList();

        SampledDataCleanAndRet sampledData = new SampledDataCleanAndRet();
        autoGraspApiClient = AutoGraspApiClient.getInstance();

        // 1.60s数据采样返回
        List<FixedFrequencyAccessData> listSample = sampledData.sampleKafkaData(batchList);

        if (listSample.size() >= 3) {
            // 2.高德抓路服务参数返回
            AutoGraspRequestParam autoGraspRequestParam = sampledData.autoGraspRequestParamRet(listSample);
            // 3.调用抓路服务
            AutoGraspResponse response = autoGraspApiClient.getAutoGraspResponse(autoGraspRequestParam);
            // 4. TODO 调用交通态势服务参数和服务

        }
        // TODO 以下为高德整合返回数据接受对象
        Map gaoDeMap = new HashMap();

        // 5.数据整合
        List integrationDataList = sampledData.dataIntegration(batchList, listSample, gaoDeMap);

        // 6.入库datahub
        WriteDatahubUtil writeDatahubUtil = new WriteDatahubUtil();
        if (integrationDataList.size() > 0) {
            int failNum = writeDatahubUtil.putRecords(integrationDataList);
            if (failNum > 0) {
                log.info("整合数据入库datahub失败!");
            }
        }
    }

}