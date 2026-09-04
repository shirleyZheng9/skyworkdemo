package com.iwhalecloud.bote.mapper.dashboard;

import com.iwhalecloud.bote.dto.dashboard.ModelUsageAggregationCheckpointDTO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageLogIncrementDTO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageStatusCandidateDTO;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 模型累计使用量内部汇总 Mapper。 */
public interface ModelUsageAggregationMapper {
  /** 查询并锁定任务检查点，锁持续到当前事务提交或回滚。 */
  ModelUsageAggregationCheckpointDTO selectCheckpointForUpdate(@Param("taskCode") String taskCode);

  /** 部署 DML 缺失时，以当前时间创建安全统计起点。 */
  int insertCheckpoint(@Param("taskCode") String taskCode, @Param("startTime") Date startTime);

  /**
   * 按复合游标读取下一批日志；只返回汇总需要的轻量字段。
   *
   * @param statisticsStartTime 功能累计统计起点
   * @param lastCreatedTime 最近成功处理的日志入库时间
   * @param lastLogId 同一入库时间内最近成功处理的日志 ID
   * @param cutoffTime 异步日志安全窗口截止时间
   * @param limit 单批最大读取数量
   */
  List<ModelUsageLogIncrementDTO> selectPendingLogs(
    @Param("statisticsStartTime") Date statisticsStartTime,
    @Param("lastCreatedTime") Date lastCreatedTime,
    @Param("lastLogId") Long lastLogId,
    @Param("cutoffTime") Date cutoffTime,
    @Param("limit") int limit
  );

  /** 给已有“租户 + 模型”汇总记录累加本批调用次数。 */
  int incrementTotal(
    @Param("tenantId") Long tenantId,
    @Param("modelId") Long modelId,
    @Param("modelName") String modelName,
    @Param("productType") String productType,
    @Param("increment") long increment
  );

  /** 为首次出现的“租户 + 模型”组合创建汇总记录。 */
  int insertTotal(
    @Param("tenantId") Long tenantId,
    @Param("modelId") Long modelId,
    @Param("modelName") String modelName,
    @Param("productType") String productType,
    @Param("increment") long increment
  );

  /** 在本批汇总完成后推进持久化游标。 */
  int updateCheckpoint(
    @Param("taskCode") String taskCode,
    @Param("lastCreatedTime") Date lastCreatedTime,
    @Param("lastLogId") Long lastLogId
  );

  /** 按最久未检查时间取得下一批模型状态校准项。 */
  List<ModelUsageStatusCandidateDTO> selectStatusCandidates(@Param("limit") int limit);

  /** 回写模型存续状态、来源及本次校准时间。 */
  int updateModelStatus(
    @Param("tenantId") Long tenantId,
    @Param("modelId") Long modelId,
    @Param("modelStatus") String modelStatus,
    @Param("modelSource") String modelSource,
    @Param("productType") String productType
  );
}
