package com.iwhalecloud.bote.loop.data.infra.rediskey;

import org.springframework.stereotype.Component;

/**
 * Redis键格式化工具类
 * 迁移对应关系: Go语言rediskey包
 * - 功能: 提供Redis键格式化方法
 * - 方法定义: 各种Redis键格式化方法
 * <p>
 * Java实现说明:
 * - 对应Go的rediskey包
 * - 使用Java类定义，包含Redis键格式化方法
 * - 提供Redis键格式化功能
 * <p>
 * 技术栈迁移:
 * - Go包 -> Java类
 * - Go函数 -> Java方法
 * - Go字符串格式化 -> Java字符串格式化
 */
@Component
public class RedisKeyFormatter {

  // 对应Go代码中的常量定义
  private static final String DATASET_ITEM_COUNT_KEY = "dataset:%d:item_count";         // dataset:dataset_id:item_count, int, 数据集item 数量
  private static final String DATASET_VERSION_ITEM_COUNT_KEY = "dataset_version:%d:item_count"; // dataset_version:dataset_version_id:item_count, int, 数据集版本 item 数量
  private static final String DATASET_OPERATION_KEY = "dataset:%d:op:%s";              // dataset:dataset_id:op:operation_type, set[op_id, op_entity], 数据集操作

  /**
   * 格式化数据集项目数量键
   * 迁移对应关系: Go语言FormatDatasetItemCountKey
   * - 功能: 格式化数据集项目数量键
   * - 参数: datasetID - 数据集ID
   * - 返回: Redis键
   * - 用途: 生成数据集项目数量Redis键
   * - 格式: dataset:dataset_id:item_count
   */
  public String formatDatasetItemCountKey(Long datasetID) {
    return String.format(DATASET_ITEM_COUNT_KEY, datasetID);
  }

  /**
   * 格式化数据集版本项目数量键
   * 迁移对应关系: Go语言FormatDatasetVersionItemCountKey
   * - 功能: 格式化数据集版本项目数量键
   * - 参数: versionID - 版本ID
   * - 返回: Redis键
   * - 用途: 生成数据集版本项目数量Redis键
   * - 格式: dataset_version:dataset_version_id:item_count
   */
  public String formatDatasetVersionItemCountKey(Long versionID) {
    return String.format(DATASET_VERSION_ITEM_COUNT_KEY, versionID);
  }

  /**
   * 格式化数据集操作键
   * 迁移对应关系: Go语言FormatDatasetOperationKey
   * - 功能: 格式化数据集操作键
   * - 参数: datasetID - 数据集ID, opType - 操作类型
   * - 返回: Redis键
   * - 用途: 生成数据集操作Redis键
   * - 格式: dataset:dataset_id:op:operation_type
   */
  public String formatDatasetOperationKey(Long datasetID, String opType) {
    return String.format(DATASET_OPERATION_KEY, datasetID, opType);
  }
}
