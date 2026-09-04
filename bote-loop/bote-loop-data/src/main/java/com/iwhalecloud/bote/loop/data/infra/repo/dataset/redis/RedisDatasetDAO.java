package com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis;

import java.util.List;
import java.util.Map;

/**
 * 数据集Redis DAO接口
 * 迁移对应关系: Go语言DatasetDAO
 * - 功能: 提供数据集Redis缓存操作接口
 * - 方法定义: 各种数据集Redis缓存操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetDAO接口
 * - 使用Java接口定义，包含数据集Redis缓存操作方法
 * - 提供数据集项目数量缓存操作
 * <p>
 * 技术栈迁移:
 * - Go Redis客户端 -> Java StringRedisTemplate
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface RedisDatasetDAO {

  /**
   * 获取项目数量
   * 迁移对应关系: Go语言DatasetDAO.GetItemCount
   * - 功能: 获取数据集项目数量
   * - 参数: datasetID - 数据集ID
   * - 返回: 项目数量
   * - 用途: 从Redis获取数据集项目数量
   */
  Long getItemCount(Long datasetID);

  /**
   * 批量获取项目数量
   * 迁移对应关系: Go语言DatasetDAO.MGetItemCount
   * - 功能: 批量获取项目数量
   * - 参数: datasetIDs - 数据集ID列表
   * - 返回: 数据集ID到项目数量的映射
   * - 用途: 批量从Redis获取数据集项目数量
   */
  Map<Long, Long> mGetItemCount(List<Long> datasetIDs);

  /**
   * 增加项目数量
   * 迁移对应关系: Go语言DatasetDAO.IncrItemCount
   * - 功能: 增加项目数量
   * - 参数: datasetID - 数据集ID, n - 增加数量
   * - 返回: 新数量
   * - 用途: 增加数据集项目数量
   */
  Long incrItemCount(Long datasetID, Long n);

  /**
   * 设置项目数量
   * 迁移对应关系: Go语言DatasetDAO.SetItemCount
   * - 功能: 设置项目数量
   * - 参数: datasetID - 数据集ID, n - 数量
   * - 用途: 设置数据集项目数量
   */
  void setItemCount(Long datasetID, Long n);
}
