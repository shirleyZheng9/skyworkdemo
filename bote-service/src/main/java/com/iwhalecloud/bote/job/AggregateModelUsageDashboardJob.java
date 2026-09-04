package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.iwhalecloud.bote.service.dashboard.IModelUsageAggregationService;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 数据概览看板模型累计使用量汇总任务。
 * <p>每次执行包含两个阶段：</p>
 * <ol>
 *   <li>根据数据库检查点分批消费当前日志表和历史日志表，累计模型调用次数；</li>
 *   <li>分批核对汇总表里的模型是否仍然有效，更新 ACTIVE/DELETED 状态。</li>
 * </ol>
 *
 * <p>该任务不提供外部调用接口。多节点同时触发时，由检查点行锁串行化日志消费，
 * 防止同一批日志被重复统计。</p>
 */
public class AggregateModelUsageDashboardJob extends AbstractSimpleJob {
  /**
   * 单次调度最多处理 100 批，每批 2000 条，即最多追赶 20 万条日志。
   * 设置上限可避免积压期间一个任务长期占用线程，剩余数据由后续周期继续处理。
   */
  private static final int MAX_BATCH_COUNT = 100;

  /** 延迟获取 Spring Bean，保持与项目现有 ElasticJob 任务创建方式一致。 */
  private IModelUsageAggregationService aggregationService;

  public AggregateModelUsageDashboardJob() {
    super("汇总看板模型使用量");
  }

  @Override
  protected void doExecute(ShardingContext shardingContext) {
    // Job 实例不走常规 Spring 构造注入，因此首次执行时从容器取得业务服务。
    if (aggregationService == null) {
      aggregationService = SpringUtil.getBean(IModelUsageAggregationService.class);
    }

    // 连续读取增量批次；返回 0 代表安全时间窗口内已经没有待汇总日志。
    int total = 0;
    for (int i = 0; i < MAX_BATCH_COUNT; i++) {
      int count = aggregationService.aggregateNextBatch();
      total += count;
      if (count == 0) {
        break;
      }
    }

    // 即使本轮没有新调用，也要校准历史模型是否已被删除、停用或重新启用。
    int refreshedStatuses = aggregationService.refreshNextModelStatusBatch();
    logger.debug("模型调用分布汇总 logs: rows={}, refreshedStatuses={}",
      total, refreshedStatuses);
  }
}
