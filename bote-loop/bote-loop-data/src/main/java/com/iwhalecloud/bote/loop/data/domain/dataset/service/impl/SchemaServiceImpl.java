package com.iwhalecloud.bote.loop.data.domain.dataset.service.impl;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.SchemaService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.util.SchemaUtils;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据集模式服务实现类
 * 迁移对应关系: Go语言DatasetServiceImpl模式相关方法
 * - 功能: 实现数据集模式相关的业务逻辑
 * - 方法实现: 各种数据集模式操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetServiceImpl模式相关方法
 * - 使用Repository层实现数据访问
 * - 提供数据集模式管理功能
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Service
@RequiredArgsConstructor
public class SchemaServiceImpl implements SchemaService {
  private static final Logger logger = LoggerFactory.getLogger(SchemaServiceImpl.class);

  private final IDatasetAPI repo;

  /**
   * 更新模式
   * 迁移对应关系: Go语言DatasetServiceImpl.UpdateSchema
   * - 功能: 更新数据集模式
   * - 参数: ds - 数据集, fields - 字段列表, updatedBy - 更新者
   * - 用途: 更新数据集模式
   */
  @Transactional
  @SuppressWarnings("PMD.GuardLogStatement")
  public void updateSchema(Dataset ds, List<FieldSchema> fields, String updatedBy) {
    // 拼装 + 校验
    DatasetSchema preSchema = repo.getSchema(ds.getSpaceId(), ds.getSchemaId());
    if (preSchema == null) {
      throw new BssException("schema not found");
    }

    boolean compatible = SchemaUtils.schemaCompatible(preSchema.getFields(), fields);
    if (!compatible) {
      ensureEmptyDataset(ds);
    }

    List<FieldSchema> allFields = SchemaUtils.mergeSchema(ds, preSchema, fields);

    Runnable postCheck = () -> {
      if (compatible) {
        return;
      }
      ensureEmptyDataset(ds); // 仅空数据集允许不兼容变更 :)
    };

    // 原地更新
    if (!preSchema.getImmutable()) {
      preSchema.setFields(allFields);
      preSchema.setUpdatedBy(updatedBy);
      updateSchema(preSchema, postCheck);
    }
    else {
      // 使用新的 schema
      logger.info("rotate schema to new id, dataset_id={}, pre_schema_id={}", ds.getId(), preSchema.getId());
      rotateSchema(ds, allFields, updatedBy, postCheck);
    }
  }

  /**
   * 确保数据集为空
   * 迁移对应关系: Go语言DatasetServiceImpl.ensureEmptyDataset
   * - 功能: 确保数据集为空
   * - 参数: ds - 数据集
   * - 用途: 检查数据集是否为空
   */
  private void ensureEmptyDataset(Dataset ds) {
    Long itemCount = repo.getItemCount(ds.getId());
    if (itemCount > 0) {
      throw new BssException("incompatible schema change on non-empty dataset");
    }
  }

  /**
   * 轮换模式
   * 迁移对应关系: Go语言DatasetServiceImpl.rotateSchema
   * - 功能: 轮换到新模式
   * - 参数: ds - 数据集, fields - 字段列表, updatedBy - 更新者, postCheck - 后检查函数
   * - 用途: 轮换模式
   */
  @Transactional
  public void rotateSchema(Dataset ds, List<FieldSchema> fields, String updatedBy, Runnable postCheck) {
    DatasetSchema schema = SchemaUtils.newSchemaOfDataset(ds, fields);
    schema.setUpdatedBy(updatedBy);
    schema.setId(0L);

    repo.createSchema(schema);

    Dataset patch = Dataset.builder()
      .schemaId(schema.getId())
      .lastOperation(DatasetOpType.UPDATE_SCHEMA)
      .updatedBy(updatedBy)
      .build();

    repo.patchDataset(patch, Dataset.builder()
      .spaceId(ds.getSpaceId())
      .id(ds.getId())
      .build());

    postCheck.run();
  }

  /**
   * 更新模式
   * 迁移对应关系: Go语言DatasetServiceImpl.updateSchema
   * - 功能: 更新模式
   * - 参数: schema - 模式对象, postCheck - 后检查函数
   * - 用途: 更新模式
   */
  @Transactional
  public void updateSchema(DatasetSchema schema, Runnable postCheck) {
    Long v = schema.getUpdateVersion();
    schema.setUpdateVersion(v + 1);

    repo.updateSchema(v, schema);

    Dataset patch = Dataset.builder()
      .lastOperation(DatasetOpType.UPDATE_SCHEMA)
      .updatedBy(schema.getUpdatedBy())
      .build();

    repo.patchDataset(patch, Dataset.builder()
      .spaceId(schema.getSpaceId())
      .id(schema.getDatasetId())
      .build());

    postCheck.run();
  }
}
