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

        FixedFrequencyIntegrationData integrationData = null;
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
            entry.setDouble(7, integrationData.getLat());
            entry.setDouble(8, integrationData.getLongi());
            entry.setDouble(9, integrationData.getAlt());
            entry.setDouble(10, integrationData.getGps_speed());
            entry.setDouble(11, integrationData.getSpeed());
            entry.setDouble(12, integrationData.getYawrate());
            entry.setDouble(13, integrationData.getAccelerate_z());
            entry.setDouble(14, integrationData.getRollrate());
            entry.setDouble(15, integrationData.getAccelerate_x());
            entry.setDouble(16, integrationData.getPitchrate());
            entry.setDouble(17, integrationData.getAccelerate_y());
            entry.setString(18, integrationData.getRoad_api_status());
            entry.setString(19, integrationData.getPolyline());
            entry.setString(20, integrationData.getRoadname());
            entry.setString(21, integrationData.getRoadlevel());
            entry.setString(22, integrationData.getMaxspeed());
            entry.setString(23, integrationData.getIntersection());
            entry.setString(24, integrationData.getIntersectiondistance());
            entry.setString(25, integrationData.getTraffic_request_time());
            entry.setString(26, integrationData.getTraffic_request_id());
            entry.setString(27, integrationData.getTraffic_api_status());
            entry.setString(28, integrationData.getCongestion_info());
            entry.setDouble(29, integrationData.getTarget_distance());
            entry.setDouble(30, integrationData.getTarget_speed());
            entry.setString(31, integrationData.getTarget_id());
            entry.setString(32, integrationData.getTarget_type());
            entry.setDouble(33, integrationData.getCollision_time());
            entry.setBigint(34, (long)integrationData.getMonitoring_car_num());
            entry.setBigint(35, (long)integrationData.getMonitoring_lane_num());
            entry.setDouble(36, integrationData.getDeviation_distance());
            entry.setDouble(37, integrationData.getDeviation_speed());
            entry.setString(38, integrationData.getTarget_lane_info());

            // 使用自定义分区方式
            entry.setString(39, integrationData.getSource_id());
            // https://help.aliyun.com/document_detail/47453.html?spm=5176.product53345.6.555.MpixiB
            // 可根据系统时间
            dateUtils = new DateUtils();
            entry.setString(40, dateUtils.getYMD());
            entry.setString(41, dateUtils.getHM());

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
