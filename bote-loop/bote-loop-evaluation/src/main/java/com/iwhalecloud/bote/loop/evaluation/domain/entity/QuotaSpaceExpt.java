package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 空间实验配额领域对象
 * 迁移对应关系: Go语言entity.QuotaSpaceExpt
 * - 功能: 空间实验配额的数据模型
 * - 字段定义:
 * * exptId2RunTime: Map<Long, Long> - 实验ID到运行时间的映射
 * <p>
 * Java实现说明:
 * - 对应Go的QuotaSpaceExpt结构体
 * - 使用Lombok注解简化代码
 * - 支持JSON序列化/反序列化
 * - 提供序列化方法
 * <p>
 * 技术栈迁移:
 * - Go map[int64]int64 -> Java Map<Long, Long>
 * - Go json.Marshal -> Java JSON序列化
 * - Go error返回 -> Java异常处理
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotaSpaceExpt {

  /**
   * 实验ID到运行时间的映射
   * 迁移对应关系: Go语言QuotaSpaceExpt.ExptID2RunTime
   * - 功能: 记录每个实验的运行时间（Unix时间戳）
   * - 类型: Map<Long, Long> - 实验ID -> Unix时间戳
   */
  private Map<Long, Long> exptId2RunTime;
}
