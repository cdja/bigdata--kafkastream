package com.chedaojunan.report.utils;

import com.aliyun.datahub.DatahubClient;
import com.aliyun.datahub.DatahubConfiguration;
import com.aliyun.datahub.auth.AliyunAccount;
import com.aliyun.datahub.common.data.RecordSchema;
import com.aliyun.datahub.model.PutRecordsResult;
import com.aliyun.datahub.model.RecordEntry;
import com.aliyun.datahub.model.ShardEntry;
import com.aliyun.datahub.wrapper.Topic;
import com.chedaojunan.report.model.FixedFrequencyIntegrationData;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class WriteDatahubUtil {

    private static final Logger logger = Logger.getLogger(WriteDatahubUtil.class);

    private static String accessId = DatahubConstants.ACCESS_ID;
    private static String accessKey = DatahubConstants.ACCESS_KEY;
    private static String endpoint = DatahubConstants.ENDPOINT;
    private static String projectName = DatahubConstants.PROJECT_NAME;
    private static String topicName = DatahubConstants.TOPIC_NAME;
    private static String topicShardNum = DatahubConstants.TOPIC_SHARDNUM;

    private DatahubClient client;
    private DatahubConfiguration conf;
    DateUtils dateUtils = null;

    public WriteDatahubUtil() {
        conf = new DatahubConfiguration(new AliyunAccount(accessId, accessKey), endpoint);
        client = new DatahubClient(conf);
    }

    // 存数据到datahub的固定频率采集数据表中
    public int putRecords(List<Object> list) {
        Topic topic = Topic.Builder.build(projectName, topicName, client);
        List<ShardEntry> shards = topic.listShard();
        RecordSchema schema = topic.getRecordSchema();
        List<RecordEntry> recordEntries = new ArrayList();
        String ymd;
        String hm;
        long time;

        FixedFrequencyIntegrationData integrationData;
        for (int i = 0; i < list.size(); i++) {
            integrationData = (FixedFrequencyIntegrationData)list.get(i);
            // RecordData
            RecordEntry entry = new RecordEntry(schema);
            entry.setString(0, integrationData.getDevice_id());
            entry.setString(1, integrationData.getDevice_imei());
            entry.setString(2, integrationData.getLocal_time());
            entry.setString(3, integrationData.getTrip_id());
            entry.setString(4, integrationData.getServer_time());
            entry.setDouble(5, integrationData.getLatitude());
            entry.setDouble(6, integrationData.getLongitude());
            entry.setDouble(7, integrationData.getAltitude());
            entry.setDouble(8, integrationData.getDirection());
            entry.setDouble(9, integrationData.getGps_speed());
            entry.setDouble(10, integrationData.getYawrate());
            entry.setDouble(11, integrationData.getAccelerate_z());
            entry.setDouble(12, integrationData.getRollrate());
            entry.setDouble(13, integrationData.getAccelerate_x());
            entry.setDouble(14, integrationData.getPitchrate());
            entry.setDouble(15, integrationData.getAccelerate_y());
            entry.setBigint(16, (long)integrationData.getRoad_api_status());
            entry.setString(17, integrationData.getCrosspoint());
            entry.setString(18, integrationData.getRoadname());
            entry.setBigint(19, (long)integrationData.getRoadlevel());
            entry.setBigint(20, (long)integrationData.getMaxspeed());
            entry.setString(21, integrationData.getIntersection());
            entry.setString(22, integrationData.getIntersectiondistance());
            entry.setString(23, integrationData.getTraffic_request_time());
            entry.setString(24, integrationData.getTraffic_request_id());
            entry.setBigint(25, (long)integrationData.getTraffic_api_status());
            entry.setString(26, integrationData.getCongestion_info());

            // 使用自定义分区方式
            entry.setString(27, integrationData.getSource_id());

            // 根据server_time设置，为空则根据系统当前时间
            dateUtils = new DateUtils();
            if (StringUtils.isNotEmpty(integrationData.getServer_time())) {
                time = Long.valueOf(integrationData.getServer_time());
                ymd = dateUtils.getYMDFromTime(time);
                hm = dateUtils.getHMFromTime(time);
            } else {
                ymd = dateUtils.getYMD();
                hm = dateUtils.getHM();
            }

            entry.setString(28, ymd);
            entry.setString(29, hm);

            // 写记录到不同的分片
            String shardId = shards.get(i % Integer.parseInt(topicShardNum)).getShardId();
            entry.setShardId(shardId);
            recordEntries.add(entry);
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
