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
        List<RecordEntry> recordEntries = new ArrayList<RecordEntry>();
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
            entry.setString(2, integrationData.getTrip_id());
            entry.setString(3, integrationData.getLocal_time());
            entry.setString(4, integrationData.getServer_time());
            entry.setString(5, integrationData.getEvent_id_list());
            entry.setString(6, integrationData.getFile_id_list());
            entry.setDouble(7, integrationData.getLatitude());
            entry.setDouble(8, integrationData.getLongitude());
            entry.setDouble(9, integrationData.getAltitude());
            entry.setDouble(10, integrationData.getDirection());
            entry.setDouble(11, integrationData.getGps_speed());
            entry.setDouble(12, integrationData.getSpeed());
            entry.setDouble(13, integrationData.getYawrate());
            entry.setDouble(14, integrationData.getAccelerate_z());
            entry.setDouble(15, integrationData.getRollrate());
            entry.setDouble(16, integrationData.getAccelerate_x());
            entry.setDouble(17, integrationData.getPitchrate());
            entry.setDouble(18, integrationData.getAccelerate_y());
            entry.setBigint(19, (long)integrationData.getRoad_api_status());
            entry.setString(20, integrationData.getPolyline());
            entry.setString(21, integrationData.getRoadname());
            entry.setBigint(22, (long)integrationData.getRoadlevel());
            entry.setBigint(23, (long)integrationData.getMaxspeed());
            entry.setString(24, integrationData.getIntersection());
            entry.setString(25, integrationData.getIntersectiondistance());
            entry.setString(26, integrationData.getTraffic_request_time());
            entry.setString(27, integrationData.getTraffic_request_id());
            entry.setBigint(28, (long)integrationData.getTraffic_api_status());
            entry.setString(29, integrationData.getCongestion_info());
            entry.setDouble(30, integrationData.getTarget_distance());
            entry.setDouble(31, integrationData.getTarget_speed());
            entry.setString(32, integrationData.getTarget_id());
            entry.setString(33, integrationData.getTarget_type());
            entry.setDouble(34, integrationData.getCollision_time());
            entry.setBigint(35, (long)integrationData.getMonitoring_car_num());
            entry.setBigint(36, (long)integrationData.getMonitoring_lane_num());
            entry.setDouble(37, integrationData.getDeviation_distance());
            entry.setDouble(38, integrationData.getDeviation_speed());
            entry.setString(39, integrationData.getTarget_lane_info());

            // 使用自定义分区方式
            entry.setString(40, integrationData.getSource_id());

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

            entry.setString(41, ymd);
            entry.setString(42, hm);

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
