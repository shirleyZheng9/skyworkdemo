package com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOperation;
import java.util.List;
import java.util.Map;

/**
 * 数据集操作Redis DAO接口
 * 迁移对应关系: Go语言OperationDAO
 * - 功能: 提供数据集操作Redis缓存操作接口
 * - 方法定义: 各种数据集操作Redis缓存操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的OperationDAO接口
 * - 使用Java接口定义，包含数据集操作Redis缓存操作方法
 * - 提供数据集操作缓存操作
 * <p>
 * 技术栈迁移:
 * - Go Redis客户端 -> Java StringRedisTemplate
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface RedisOperationDAO {

  /**
   * 添加数据集操作
   * 迁移对应关系: Go语言OperationDAO.AddDatasetOperation
   * - 功能: 添加数据集操作
   * - 参数: datasetID - 数据集ID, op - 操作对象
   * - 用途: 添加数据集操作到Redis
   */
  void addDatasetOperation(Long datasetID, DatasetOperation op);

  /**
   * 删除数据集操作
   * 迁移对应关系: Go语言OperationDAO.DelDatasetOperation
   * - 功能: 删除数据集操作
   * - 参数: datasetID - 数据集ID, opType - 操作类型, id - 操作ID
   * - 用途: 从Redis删除数据集操作
   */
  void delDatasetOperation(Long datasetID, DatasetOpType opType, String id);

  /**
   * 批量获取数据集操作
   * 迁移对应关系: Go语言OperationDAO.MGetDatasetOperations
   * - 功能: 批量获取数据集操作
   * - 参数: datasetID - 数据集ID, opTypes - 操作类型列表
   * - 返回: 操作类型到操作列表的映射
   * - 用途: 批量从Redis获取数据集操作
   */
  Map<DatasetOpType, List<DatasetOperation>> mGetDatasetOperations(Long datasetID, List<DatasetOpType> opTypes);
}
