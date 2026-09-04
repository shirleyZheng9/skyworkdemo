package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetSchemaEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Date;
import java.util.List;

/**
 * Schema转换器
 * 迁移对应关系: Go语言convertor包中的Schema转换器
 * - 功能: 提供Schema DO和PO之间的转换
 * - 方法定义: 各种Schema转换方法
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的Schema转换器
 * - 使用Java静态方法提供转换功能
 * - 提供Schema DO和PO之间的转换
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go JSON序列化 -> Jackson序列化
 * - Go错误处理 -> Java异常处理
 */
public final class SchemaConvertor {

  private SchemaConvertor() {
  }
  /**
   * 将DatasetSchema DO转换为PO
   * 迁移对应关系: Go语言SchemaDO2PO
   * - 功能: 将Schema DO转换为PO
   * - 参数: datasetSchema - Schema DO对象
   * - 返回: Schema PO对象
   * - 用途: 数据持久化前的转换
   */
  public static DatasetSchemaEntity schemaDO2PO(DatasetSchema datasetSchema) {
    if (datasetSchema == null) {
      return null;
    }

    DatasetSchemaEntity.DatasetSchemaEntityBuilder builder = DatasetSchemaEntity.builder()
      .id(datasetSchema.getId())
      .appId(datasetSchema.getAppId())
      .spaceId(datasetSchema.getSpaceId())
      .datasetId(datasetSchema.getDatasetId())
      .immutable(datasetSchema.getImmutable() != null && datasetSchema.getImmutable() ? "T" : "F")
      .createdBy(datasetSchema.getCreatedBy())
      .createdAt(datasetSchema.getCreatedAt() != null ? datasetSchema.getCreatedAt() : new Date())
      .updatedBy(datasetSchema.getUpdatedBy())
      .updatedAt(datasetSchema.getUpdatedAt() != null ? datasetSchema.getUpdatedAt() : new Date())
      .updateVersion(datasetSchema.getUpdateVersion() != null ? datasetSchema.getUpdateVersion() : 1L);

    // 处理字段
    if (datasetSchema.getFields() != null && !datasetSchema.getFields().isEmpty()) {
      try {
        String fieldsJson = JsonUtil.toJsonString(datasetSchema.getFields());
        builder.fields(fieldsJson);
      }
      catch (Exception e) {
        throw new BssException("marshal schema.fields failed, data=" + datasetSchema.getFields(), e);
      }
    }

    return builder.build();
  }

  /**
   * 将DatasetSchema PO转换为DO
   * 迁移对应关系: Go语言ConvertSchemaPO2DO
   * - 功能: 将Schema PO转换为DO
   * - 参数: po - Schema PO对象
   * - 返回: Schema DO对象
   * - 用途: 数据查询后的转换
   */
  public static DatasetSchema schemaPO2DO(DatasetSchemaEntity po) {
    if (po == null) {
      return null;
    }

    DatasetSchema.DatasetSchemaBuilder builder = DatasetSchema.builder()
      .id(po.getId())
      .appId(po.getAppId())
      .spaceId(po.getSpaceId())
      .datasetId(po.getDatasetId())
      .immutable(po.getImmutable() != null && ("T".equals(po.getImmutable()) || "t".equals(po.getImmutable())))
      .createdBy(po.getCreatedBy())
      .createdAt(po.getCreatedAt())
      .updatedBy(po.getUpdatedBy())
      .updatedAt(po.getUpdatedAt())
      .updateVersion(po.getUpdateVersion());

    // 处理字段
    if (po.getFields() != null && !po.getFields().isEmpty()) {
      try {
        List<FieldSchema> fields = JsonUtil.parseJson(po.getFields(), new TypeReference<List<FieldSchema>>() {
        });
        builder.fields(fields);
      }
      catch (Exception e) {
        throw new BssException("unmarshal schema.fields failed, data=" + po.getFields(), e);
      }
    }

    return builder.build();
  }
}
