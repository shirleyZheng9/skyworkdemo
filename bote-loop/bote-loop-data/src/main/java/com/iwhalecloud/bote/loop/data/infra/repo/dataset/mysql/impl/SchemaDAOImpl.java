package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.impl;

import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetSchemaEntity;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.SchemaDAO;
import com.iwhalecloud.bote.mapper.loop.data.dataset.SchemaMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * 数据集Schema DAO实现类
 * 迁移对应关系: Go语言SchemaDAOImpl
 * - 功能: 实现数据集Schema数据访问
 * - 方法实现: 各种数据集Schema数据操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的SchemaDAOImpl结构体
 * - 使用MyBatis实现数据库操作
 * - 提供数据集Schema数据CRUD操作实现
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Repository
@RequiredArgsConstructor
public class SchemaDAOImpl implements SchemaDAO {
  private final SchemaMapper schemaMapper;

  /**
   * 创建Schema
   * 迁移对应关系: Go语言SchemaDAOImpl.CreateSchema
   * - 功能: 创建数据集Schema
   * - 参数: schema - Schema对象
   * - 用途: 创建新Schema
   */
  @Override
  public void createSchema(DatasetSchemaEntity schema) {
    if (schema == null) {
      throw new BssException("schema is required");
    }

    try {
      schemaMapper.createSchema(schema);
    }
    catch (Exception e) {
      throw new BssException("create schema failed", e);
    }
  }

  /**
   * 更新Schema
   * 迁移对应关系: Go语言SchemaDAOImpl.UpdateSchema
   * - 功能: 更新数据集Schema
   * - 参数: updateVersion - 更新版本号, schema - Schema对象
   * - 用途: 更新Schema
   */
  @Override
  public void updateSchema(Long updateVersion, DatasetSchemaEntity schema) {
    if (updateVersion == null || updateVersion <= 0) {
      throw new BssException("updateVersion is required");
    }
    if (schema == null) {
      throw new BssException("schema is required");
    }

    try {
      // 如果schema的updateVersion为0，则设置为updateVersion + 1
      if (schema.getUpdateVersion() == null || schema.getUpdateVersion() == 0) {
        schema.setUpdateVersion(updateVersion + 1);
      }
      schemaMapper.updateSchema(updateVersion, schema);
    }
    catch (Exception e) {
      throw new BssException("update schema failed", e);
    }
  }

  /**
   * 获取Schema
   * 迁移对应关系: Go语言SchemaDAOImpl.GetSchema
   * - 功能: 获取数据集Schema
   * - 参数: spaceId - 空间ID, id - Schema ID
   * - 返回: Schema对象
   * - 用途: 获取单个Schema
   */
  @Override
  public DatasetSchemaEntity getSchema(Long spaceId, Long id) {
    if (spaceId == null || id == null) {
      throw new BssException("spaceId and id are required");
    }

    try {
      return schemaMapper.getSchema(spaceId, id);
    }
    catch (Exception e) {
      throw new BssException("get dataset schema failed", e);
    }
  }

  /**
   * 批量获取Schema
   * 迁移对应关系: Go语言SchemaDAOImpl.MGetSchema
   * - 功能: 批量获取数据集Schema
   * - 参数: spaceId - 空间ID, ids - Schema ID列表
   * - 返回: Schema列表
   * - 用途: 批量获取Schema
   */
  @Override
  public List<DatasetSchemaEntity> mGetSchema(Long spaceId, List<Long> ids) {
    if (spaceId == null) {
      throw new BssException("spaceId is required");
    }
    if (CollectionUtils.isEmpty(ids)) {
      return new java.util.ArrayList<>();
    }

    try {
      return schemaMapper.mGetSchema(spaceId, ids);
    }
    catch (Exception e) {
      throw new BssException("mget schemas failed", e);
    }
  }
}
