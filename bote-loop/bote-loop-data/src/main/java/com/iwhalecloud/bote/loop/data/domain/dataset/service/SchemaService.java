package com.iwhalecloud.bote.loop.data.domain.dataset.service;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import java.util.List;

/**
 * 模式服务接口
 * 迁移对应关系: Go语言service.ISchemaService
 * - 功能: 提供模式相关的业务逻辑服务
 * - 方法定义: 各种模式操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的service.ISchemaService接口
 * - 使用Java接口定义，包含模式服务方法
 * - 提供模式CRUD和业务逻辑操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface SchemaService {

  /**
   * 更新模式
   * 迁移对应关系: Go语言service.ISchemaService.UpdateSchema
   * - 功能: 更新模式
   * - 参数: dataset - 数据集, fields - 字段列表, updatedBy - 更新者
   * - 用途: 更新数据集模式
   */
  void updateSchema(Dataset dataset, List<FieldSchema> fields, String updatedBy);
}
