package com.iwhalecloud.bote.loop.data.domain.dataset.repo;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOperation;
import java.util.List;
import java.util.Map;

/**
 * 操作仓库接口
 * 迁移对应关系: Go语言repo.IOperationRepo
 * - 功能: 提供数据集操作数据访问接口
 * - 方法定义: 各种数据集操作数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的repo.IOperationRepo接口
 * - 使用Java接口定义，包含数据集操作数据访问方法
 * - 提供数据集操作数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IOperationRepo {

  /**
   * 添加数据集操作
   * 迁移对应关系: Go语言repo.IOperationRepo.AddDatasetOperation
   * - 功能: 添加数据集操作
   * - 参数: datasetID - 数据集ID, op - 数据集操作
   * - 用途: 添加数据集操作
   */
  void addDatasetOperation(Long datasetID, DatasetOperation op);

  /**
   * 删除数据集操作
   * 迁移对应关系: Go语言repo.IOperationRepo.DelDatasetOperation
   * - 功能: 删除数据集操作
   * - 参数: datasetID - 数据集ID, opType - 操作类型, id - 操作ID
   * - 用途: 删除数据集操作
   */
  void delDatasetOperation(Long datasetID, DatasetOpType opType, String id);

  /**
   * 批量获取数据集操作
   * 迁移对应关系: Go语言repo.IOperationRepo.MGetDatasetOperations
   * - 功能: 批量获取数据集操作
   * - 参数: datasetID - 数据集ID, opTypes - 操作类型列表
   * - 返回: 操作类型到操作列表的映射
   * - 用途: 批量获取数据集操作
   */
  Map<DatasetOpType, List<DatasetOperation>> mGetDatasetOperations(Long datasetID, List<DatasetOpType> opTypes);
}
