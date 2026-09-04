package com.iwhalecloud.bote.service.dashboard;

/**
 * 模型使用量汇总服务。
 * 调度任务可以连续调用以追赶积压，同时避免单个数据库事务过大。
 */
public interface IModelUsageAggregationService {
  /**
   * 汇总下一批日志。
   *
   * @return 本批读取的日志数；0 表示当前没有待处理数据
   */
  int aggregateNextBatch();

  /**
   * 分批校准汇总表中的模型删除状态和来源。
   *
   * @return 本批完成状态校准的模型数
   */
  int refreshNextModelStatusBatch();
}
