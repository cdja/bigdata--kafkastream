package com.chedaojunan.report.utils;

import com.aliyun.datahub.DatahubClient;
import com.aliyun.datahub.DatahubConfiguration;
import com.aliyun.datahub.auth.AliyunAccount;
import com.aliyun.datahub.common.data.RecordSchema;
import com.aliyun.datahub.model.PutRecordsResult;
import com.aliyun.datahub.model.RecordEntry;
import com.aliyun.datahub.model.ShardEntry;
import com.aliyun.datahub.wrapper.Topic;
import com.chedaojunan.report.model.DatahubDeviceData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WriteDatahubUtil {

    private static final Logger logger = LoggerFactory.getLogger(WriteDatahubUtil.class);

    private static Properties datahubProperties = null;

    static {
        datahubProperties = ReadProperties.getProperties(DatahubConstants.PROPERTIES_FILE_NAME);
    }

    private static String accessId = datahubProperties.getProperty(DatahubConstants.ACCESS_ID);
    private static String accessKey = datahubProperties.getProperty(DatahubConstants.ACCESS_KEY);
    private static String endpoint = datahubProperties.getProperty(DatahubConstants.ENDPOINT);
    private static String projectName = datahubProperties.getProperty(DatahubConstants.PROJECT_NAME);
    private static String topicName = datahubProperties.getProperty(DatahubConstants.TOPIC_NAME);
    private static String topicShardNum = datahubProperties.getProperty(DatahubConstants.TOPIC_SHARDNUM);

    private DatahubClient client;
    private DatahubConfiguration conf;
    DateUtils dateUtils = null;

    private static WriteDatahubUtil single = null;

    public WriteDatahubUtil() {
        conf = new DatahubConfiguration(new AliyunAccount(accessId, accessKey), endpoint);
        client = new DatahubClient(conf);
    }

    //静态工厂方法
    public static synchronized  WriteDatahubUtil getInstance() {
        if (single == null) {
            single = new WriteDatahubUtil();
        }
        return single;
    }

    // 存数据到datahub的固定频率采集数据表中
    public int putRecords(ArrayList<DatahubDeviceData> list) {
        if (list == null || list.size()==0) {
            return -1;
        }
        Topic topic = Topic.Builder.build(projectName, topicName, client);
        List<ShardEntry> shards = topic.listShard();
        RecordSchema schema = topic.getRecordSchema();
        List<RecordEntry> recordEntries = new ArrayList();
        String ymd;
        String hm;
        long time;

        DatahubDeviceData integrationData;
        for (int i = 0; i < list.size(); i++) {
            integrationData = list.get(i);
            // RecordData
            RecordEntry entry = new RecordEntry(schema);
            if (integrationData != null) {
                entry.setString(0, integrationData.getDeviceId());
                entry.setString(1, integrationData.getDeviceImei());
                entry.setString(2, integrationData.getLocalTime());
                entry.setString(3, integrationData.getTripId());
                entry.setString(4, integrationData.getServerTime());
                entry.setDouble(5, integrationData.getLatitude());
                entry.setDouble(6, integrationData.getLongitude());
                entry.setDouble(7, integrationData.getAltitude());
                entry.setDouble(8, integrationData.getDirection());
                entry.setDouble(9, integrationData.getGpsSpeed());
                entry.setDouble(10, integrationData.getYawRate());
                entry.setDouble(11, integrationData.getAccelerateZ());
                entry.setDouble(12, integrationData.getRollRate());
                entry.setDouble(13, integrationData.getAccelerateX());
                entry.setDouble(14, integrationData.getPitchRate());
                entry.setDouble(15, integrationData.getAccelerateY());
                entry.setBigint(16, (long) integrationData.getRoadApiStatus());
                entry.setString(17, integrationData.getCrosspoint());
                entry.setString(18, integrationData.getRoadName());
                entry.setBigint(19, (long) integrationData.getRoadLevel());
                entry.setBigint(20, (long) integrationData.getMaxSpeed());
                entry.setString(21, integrationData.getIntersection());
                entry.setString(22, integrationData.getIntersectionDistance());
                entry.setString(23, integrationData.getTrafficRequestTimesamp());
                entry.setString(24, integrationData.getTrafficRequestId());
                entry.setBigint(25, (long) integrationData.getTrafficApiStatus());
                entry.setString(26, integrationData.getCongestionInfo());

                // 使用自定义分区方式
                entry.setString(27, integrationData.getSourceId());

                // 根据server_time设置，为空则根据系统当前时间
                dateUtils = new DateUtils();
                if (StringUtils.isNotEmpty(integrationData.getServerTime())) {
                    // time增加300000毫秒，分区时间后延
                    time = Long.valueOf(integrationData.getServerTime()) + 300000L;
                    ymd = dateUtils.getYMDFromTime(time);
    //                hm = dateUtils.getHMFromTime(time);
                    int hm_temp = Integer.parseInt(dateUtils.getMinuteFromTime(time)) / 5 * 5;
                    if (hm_temp<60) {
                        hm = dateUtils.getHourFromTime(time) + "_" +
                                String.format("%02d", hm_temp);
                    } else {
                        hm = dateUtils.getHourFromTime(time) + "_" +
                                String.format("%02d", 00);
                    }

                } else {
                    int hm_temp = Integer.parseInt(dateUtils.getMinute_After5M()) / 5 * 5;
                    ymd = dateUtils.getYMD_After5M();
    //                hm = dateUtils.getHM();
                    if (hm_temp<60) {
                        hm = dateUtils.getHour_After5M() + "_" + String.format("%02d", hm_temp);
                    } else {
                        hm = dateUtils.getHour_After5M() + "_" + String.format("%02d", 00);
                    }
                }

                entry.setString(28, ymd);
                entry.setString(29, hm);

                // 增加adcode和towncode
                entry.setString(30, integrationData.getAdCode());
                entry.setString(31, integrationData.getTownCode());

                // 写记录到不同的分片
                String shardId = shards.get((int) (Math.random() * Integer.parseInt(topicShardNum)) % Integer.parseInt(topicShardNum)).getShardId();
                entry.setShardId(shardId);
                recordEntries.add(entry);
            }
        }

        // 尝试次数
        int retryCount = 3;
        PutRecordsResult result = topic.putRecords(recordEntries, retryCount);
        int failNum = result.getFailedRecordCount();
        if (failNum > 0) {
            // 操作失败的记录
            logger.info("failed records:");
            for (RecordEntry record : result.getFailedRecords()) {
                logger.info(record.toJsonNode().toString());
            }
            return failNum;
        } else {
            // 所有的记录处理成功
            logger.info("successfully write all records");
            return 0;
        }
    }

}