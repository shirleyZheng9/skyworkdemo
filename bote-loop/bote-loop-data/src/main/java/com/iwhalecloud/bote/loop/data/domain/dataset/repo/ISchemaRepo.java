package com.iwhalecloud.bote.loop.data.domain.dataset.repo;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import java.util.List;

/**
 * 模式仓库接口
 * 迁移对应关系: Go语言repo.ISchemaRepo
 * - 功能: 提供数据集模式数据访问接口
 * - 方法定义: 各种数据集模式数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的repo.ISchemaRepo接口
 * - 使用Java接口定义，包含数据集模式数据访问方法
 * - 提供数据集模式数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface ISchemaRepo {

  /**
   * 获取模式
   * 迁移对应关系: Go语言repo.ISchemaRepo.GetSchema
   * - 功能: 获取模式
   * - 参数: spaceID - 空间ID, id - 模式ID
   * - 返回: 数据集模式
   * - 用途: 获取单个模式
   */
  DatasetSchema getSchema(Long spaceID, Long id);

  /**
   * 批量获取模式
   * 迁移对应关系: Go语言repo.ISchemaRepo.MGetSchema
   * - 功能: 批量获取模式
   * - 参数: spaceID - 空间ID, ids - 模式ID列表
   * - 返回: 模式列表
   * - 用途: 批量获取模式
   */
  List<DatasetSchema> mGetSchema(Long spaceID, List<Long> ids);

  /**
   * 创建模式
   * 迁移对应关系: Go语言repo.ISchemaRepo.CreateSchema
   * - 功能: 创建模式
   * - 参数: schema - 数据集模式
   * - 用途: 创建模式
   */
  void createSchema(DatasetSchema schema);

  /**
   * 更新模式
   * 迁移对应关系: Go语言repo.ISchemaRepo.UpdateSchema
   * - 功能: 更新模式
   * - 参数: updateVersion - 更新版本, schema - 数据集模式
   * - 用途: 更新模式
   */
  void updateSchema(Long updateVersion, DatasetSchema schema);
}
