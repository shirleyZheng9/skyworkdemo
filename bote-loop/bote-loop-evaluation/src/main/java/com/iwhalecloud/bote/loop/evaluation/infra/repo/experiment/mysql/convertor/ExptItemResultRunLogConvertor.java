package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptItemResultRunLogEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResultRunLog;
import java.util.Date;

/**
 * 实验数据项结果运行日志转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/convert/expt_item_result_run_log.go
 * - 功能: 实验数据项结果运行日志DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: 指针类型转换、条件赋值
 * <p>
 * Java实现说明:
 * - 对应Go的ExptItemResultRunLogConverter结构体
 * - 处理指针类型转换
 * - 支持条件赋值
 * <p>
 * 技术栈迁移:
 * - Go func PO2DO -> Java convertToDO
 * - Go func DO2PO -> Java convertToPO
 * - Go gptr.Of -> Java 包装类型赋值
 * - Go gptr.Indirect -> Java 包装类型取值
 * - Go *[]byte -> Java byte[]
 * - Go *time.Time -> Java LocalDateTime
 */
public final class ExptItemResultRunLogConvertor {

  private ExptItemResultRunLogConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 将PO转换为DO
   * 迁移对应关系: Go语言PO2DO
   *
   * @param rlEntity 持久化对象
   * @return 领域对象
   */
  public static ExptItemResultRunLog convertToDO(ExptItemResultRunLogEntity rlEntity) {
    if (rlEntity == null) {
      return null;
    }

    return ExptItemResultRunLog.builder()
      .id(rlEntity.getId())                                    // ID: rl.ID
      .spaceId(rlEntity.getSpaceId())                          // SpaceID: rl.SpaceID
      .exptId(rlEntity.getExptId())                            // ExptID: rl.ExptID
      .exptRunId(rlEntity.getExptRunId())                      // ExptRunID: rl.ExptRunID
      .itemId(rlEntity.getItemId())                            // ItemID: rl.ItemID
      .status(rlEntity.getStatus())                            // Status: rl.Status
      .errMsg(rlEntity.getErrMsg())                            // ErrMsg: gptr.Indirect(rl.ErrMsg)
      .logId(rlEntity.getLogId())                              // LogID: rl.LogID
      .updatedAt(rlEntity.getUpdatedAt())                      // UpdatedAt: gptr.Of(rl.UpdatedAt)
      .build();
  }

  /**
   * 将DO转换为PO
   * 迁移对应关系: Go语言DO2PO
   *
   * @param doEntity 领域对象
   * @return 持久化对象
   */
  public static ExptItemResultRunLogEntity convertToPO(ExptItemResultRunLog doEntity) {
    if (doEntity == null) {
      return null;
    }

    ExptItemResultRunLogEntity.ExptItemResultRunLogEntityBuilder builder = ExptItemResultRunLogEntity.builder()
      .id(doEntity.getId())                                    // ID: do.ID
      .spaceId(doEntity.getSpaceId())                          // SpaceID: do.SpaceID
      .exptId(doEntity.getExptId())                            // ExptID: do.ExptID
      .exptRunId(doEntity.getExptRunId())                      // ExptRunID: do.ExptRunID
      .itemId(doEntity.getItemId())                            // ItemID: do.ItemID
      .status(doEntity.getStatus())                            // Status: do.Status
      .errMsg(doEntity.getErrMsg())                            // ErrMsg: gptr.Of(do.ErrMsg)
      .logId(doEntity.getLogId())
      .deletedAt(0L)
      .createdAt(new Date());                             // LogID: do.LogID

    // UpdatedAt: 条件赋值
    if (doEntity.getUpdatedAt() != null) {
      builder.updatedAt(doEntity.getUpdatedAt());
    }

    return builder.build();
  }
}
