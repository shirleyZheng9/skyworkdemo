package com.iwhalecloud.bote.mapper.loop.data.dataset;

import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetSchemaEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 数据集Schema MyBatis Mapper接口
 * 迁移对应关系: Go语言ISchemaDAO
 * - 功能: 提供数据集Schema数据访问接口
 * - 方法定义: 各种数据集Schema数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的ISchemaDAO接口
 * - 使用MyBatis注解和XML映射
 * - 提供数据集Schema数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java MyBatis接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */

public interface SchemaMapper {

  /**
   * 创建Schema
   * 迁移对应关系: Go语言ISchemaDAO.CreateSchema
   * - 功能: 创建数据集Schema
   * - 参数: schema - Schema对象
   * - 用途: 创建新Schema
   */
  void createSchema(DatasetSchemaEntity schema);

  /**
   * 更新Schema
   * 迁移对应关系: Go语言ISchemaDAO.UpdateSchema
   * - 功能: 更新数据集Schema
   * - 参数: updateVersion - 更新版本号, schema - Schema对象
   * - 用途: 更新Schema
   */
  void updateSchema(@Param("updateVersion") Long updateVersion, @Param("schema") DatasetSchemaEntity schema);

  /**
   * 获取Schema
   * 迁移对应关系: Go语言ISchemaDAO.GetSchema
   * - 功能: 获取数据集Schema
   * - 参数: spaceId - 空间ID, id - Schema ID
   * - 返回: Schema对象
   * - 用途: 获取单个Schema
   */
  DatasetSchemaEntity getSchema(@Param("spaceId") Long spaceId, @Param("id") Long id);

  /**
   * 批量获取Schema
   * 迁移对应关系: Go语言ISchemaDAO.MGetSchema
   * - 功能: 批量获取数据集Schema
   * - 参数: spaceId - 空间ID, ids - Schema ID列表
   * - 返回: Schema列表
   * - 用途: 批量获取Schema
   */
  List<DatasetSchemaEntity> mGetSchema(@Param("spaceId") Long spaceId, @Param("ids") List<Long> ids);
}
