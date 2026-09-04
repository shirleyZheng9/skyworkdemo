package com.iwhalecloud.bote.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * 发布步骤基本定义
 *
 * @author chen.linfa
 * @since 2024-10-21
 */
@Getter
@RequiredArgsConstructor
public enum PublishStepType {

  /** 导出：初始化 */
  INITIALIZE_EXPORT(10, "初始化", true),
  /** 导出：收集配置数据 */
  COllECT_DATA(20, "收集配置数据", true),
  /** 导出：封装结果 */
  WRAP_EXPORT(40, "封装结果", true),

  /** 导入：解析数据包 */
  PARSE_FILE(110, "解析数据包", true),
  /** 导入：出厂脚本修改并执行 */
  PARSE_SCRIPT(120, "出厂脚本修改并执行", true),
  /** 环境实例确认并修改 */
  CHANGE_ENV_INST(125, "环境实例确认并修改", false),
  /** 导入：确认导入 */
  CONFIRM_IMPORT(130, "确认导入", true),

  /** 复制：初始化 */
  INITIALIZE_FOR_COPY(410, "初始化", true),
  /** 复制：收集配置数据 */
  COllECT_DATA_FOR_COPY(420, "收集配置数据", true),
  /** 复制：保存配置数据 */
  SAVE_DATA_FOR_COPY(430, "保存配置数据", true),

  /** 模型微调：初始化 */
  INITIALIZE_FINETUNE(210, "初始化", true),
  /** 模型微调：执行 */
  CALL_FINETUNE(220, "执行", true),
  /** 模型微调：更新状态 */
  UPDATE_FINETUNE_STATUS(230, "更新状态", false),

  /** 模型评测：执行 */
  CALL_EVAL(310, "执行", true),
  /** 模型评测：更新状态 */
  UPDATE_EVAL_STATUS(320, "更新状态", false),

  /** 在线发布：初始化 */
  INITIALIZE_ONLINE(510, "初始化", true),
  /** 在线发布：收集配置数据 */
  COLLECT_DATA_ONLINE(520, "收集配置数据", true),
  /** 在线发布：确认发布 */
  CONFIRM_PUBLISH_ONLINE(550, "确认发布", true);

  /** 类型值 */
  private final int value;

  /** 名称 */
  private final String name;

  /** 是否自动环节 */
  private final Boolean auto;

  /**
   * 根据类型值获取类型枚举值
   *
   * @param value 类型值
   * @return 类型枚举值
   */
  @Nullable
  public static PublishStepType findByValue(@Nullable Integer value) {
    if (value == null) {
      return null;
    }
    for (PublishStepType type : values()) {
      if (value.equals(type.getValue())) {
        return type;
      }
    }
    return null;
  }
}
