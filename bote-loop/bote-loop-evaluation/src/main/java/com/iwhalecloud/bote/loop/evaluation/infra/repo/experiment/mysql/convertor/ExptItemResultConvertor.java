package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptItemResultEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptItemResultRunLogEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunState;
import java.util.Date;

/**
 * 实验数据项结果转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/convert/expt_result.go
 * - 功能: 实验数据项结果DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: 指针类型转换、字符串字节转换
 * <p>
 * Java实现说明:
 * - 对应Go的ExptItemResultConvertor结构体
 * - 处理指针类型转换
 * - 支持运行日志转换
 * <p>
 * 技术栈迁移:
 * - Go func PO2RunLogPO -> Java convertToRunLogPO
 * - Go func PO2DO -> Java convertToDO
 * - Go func DO2PO -> Java convertToPO
 * - Go gptr.Of -> Java 包装类型赋值
 * - Go gptr.Indirect -> Java 包装类型取值
 * - Go conv.UnsafeStringToBytes -> Java String.getBytes()
 * - Go conv.UnsafeBytesToString -> Java new String(bytes)
 */
public final class ExptItemResultConvertor {

  private ExptItemResultConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 将PO转换为运行日志PO
   * 迁移对应关系: Go语言PO2RunLogPO
   *
   * @param id 日志ID
   * @param result 数据项结果PO
   * @return 运行日志PO
   */
  public static ExptItemResultRunLogEntity convertToRunLogPO(Long id, ExptItemResultEntity result) {
    if (result == null) {
      return null;
    }

    return ExptItemResultRunLogEntity.builder()
      .id(id)                                    // ID: id
      .spaceId(result.getSpaceId())              // SpaceID: result.SpaceID
      .exptId(result.getExptId())                // ExptID: result.ExptID
      .exptRunId(result.getExptRunId())          // ExptRunID: result.ExptRunID
      .itemId(result.getItemId())                // ItemID: result.ItemID
      .status(result.getStatus())                // Status: result.Status
      .errMsg(result.getErrMsg())                // ErrMsg: result.ErrMsg
      .logId(result.getLogId())                  // LogID: result.LogID
      .build();
  }

  /**
   * 将PO转换为DO
   * 迁移对应关系: Go语言PO2DO
   *
   * @param rlEntity 持久化对象
   * @return 领域对象
   */
  public static ExptItemResult convertToDO(ExptItemResultEntity rlEntity) {
    if (rlEntity == null) {
      return null;
    }

    return ExptItemResult.builder()
      .id(rlEntity.getId())                      // ID: rl.ID
      .spaceId(rlEntity.getSpaceId())            // SpaceID: rl.SpaceID
      .exptId(rlEntity.getExptId())              // ExptID: rl.ExptID
      .exptRunId(rlEntity.getExptRunId())        // ExptRunID: rl.ExptRunID
      .itemId(rlEntity.getItemId())              // ItemID: rl.ItemID
      .itemIdx(rlEntity.getItemIdx())            // ItemIdx: gptr.Indirect(rl.ItemIdx)
      .status(rlEntity.getStatus() != null ? ItemRunState.fromValue(rlEntity.getStatus()) : null) // Status: entity.ItemRunState(rl.Status)
      .errMsg(rlEntity.getErrMsg()) // ErrMsg: conv.UnsafeBytesToString(gptr.Indirect(rl.ErrMsg))
      .logId(rlEntity.getLogId())                // LogID: rl.LogID
      .build();
  }

  /**
   * 将DO转换为PO
   * 迁移对应关系: Go语言DO2PO
   *
   * @param result 领域对象
   * @return 持久化对象
   */
  public static ExptItemResultEntity convertToPO(ExptItemResult result) {
    if (result == null) {
      return null;
    }

    return ExptItemResultEntity.builder()
      .id(result.getId())                        // ID: result.ID
      .spaceId(result.getSpaceId())              // SpaceID: result.SpaceID
      .exptId(result.getExptId())                // ExptID: result.ExptID
      .exptRunId(result.getExptRunId())          // ExptRunID: result.ExptRunID
      .itemId(result.getItemId())                // ItemID: result.ItemID
      .itemIdx(result.getItemIdx())              // ItemIdx: gptr.Of(result.ItemIdx)
      .status(result.getStatus() != null ? result.getStatus().getValue() : null) // Status: int32(result.Status)
      .errMsg(result.getErrMsg()) // ErrMsg: gptr.Of(conv.UnsafeStringToBytes(result.ErrMsg))
      .logId(result.getLogId())                  // LogID: result.LogID
      .deletedAt(0L)
      .createdAt(new Date())
      .build();
  }
}
