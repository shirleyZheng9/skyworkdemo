package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetCategory;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetFeatures;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSpec;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVisibility;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SecurityLevel;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * 数据集转换器
 * 迁移对应关系: Go语言convertor包中的Dataset转换器
 * - 功能: 提供数据集DO和PO之间的转换
 * - 方法定义: 各种数据集转换方法
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的Dataset转换器
 * - 使用Java静态方法提供转换功能
 * - 提供数据集DO和PO之间的转换
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go JSON序列化 -> Jackson序列化
 * - Go错误处理 -> Java异常处理
 */
public final class DatasetConvertor {

  private static final ObjectMapper objectMapper = JsonMapper.builder().build();

  private DatasetConvertor() {
  }

  /**
   * 将Dataset DO转换为DatasetEntity PO
   * 迁移对应关系: Go语言DatasetDO2PO
   * - 功能: 将数据集DO转换为PO
   * - 参数: dataset - 数据集DO对象
   * - 返回: 数据集PO对象
   * - 用途: 数据持久化前的转换
   */
  public static DatasetEntity datasetDO2PO(Dataset dataset, boolean setDefault) {
    if (dataset == null) {
      return null;
    }

    DatasetEntity.DatasetEntityBuilder builder = buildBasicDatasetEntity(dataset, setDefault);
    serializeFeatures(dataset, builder);
    serializeSpec(dataset, builder);

    return builder.build();
  }

  public static DatasetEntity datasetDO2PO(Dataset dataset) {
    return datasetDO2PO(dataset, true);
  }

  private static DatasetEntity.DatasetEntityBuilder buildBasicDatasetEntity(Dataset dataset, boolean setDefault) {
    Date now = new Date();
    String description = dataset.getDescription();
    Long nextVersionNum = dataset.getNextVersionNum();
    String status = dataset.getStatus() != null ? dataset.getStatus().getValue() : null;
    Long deletedAt = null;
    Date createdAt = dataset.getCreatedAt();
    Date updatedAt = dataset.getUpdatedAt();
    if (setDefault) {
      if (dataset.getNextVersionNum() == null) {
        nextVersionNum = 1L;
      }
      if (status == null) {
        status = "available";
      }
      deletedAt = 0L;
      if (createdAt == null) {
        createdAt = now;
      }
      if (updatedAt == null) {
        updatedAt = now;
      }
    }

    return DatasetEntity.builder()
      .id(dataset.getId())
      .appId(dataset.getAppId())
      .spaceId(dataset.getSpaceId())
      .schemaId(dataset.getSchemaId())
      .name(dataset.getName())
      .description(StringUtils.isEmpty(description) ? null : description)
      .category(dataset.getCategory() != null ? dataset.getCategory().getValue() : null)
      .bizCategory(dataset.getBizCategory())
      .status(status)
      .securityLevel(dataset.getSecurityLevel() != null ? dataset.getSecurityLevel().getValue() : null)
      .visibility(getVisibility(dataset))
      .latestVersion(dataset.getLatestVersion())
      .nextVersionNum(nextVersionNum)
      .lastOperation(getLastOperation(dataset))
      .createdBy(dataset.getCreatedBy())
      .createdAt(createdAt)
      .updatedBy(dataset.getUpdatedBy())
      .updatedAt(updatedAt)
      .deletedAt(deletedAt)
      .catalogItemId(dataset.getCatalogItemId())
      .expiredAt(dataset.getExpiredAt());
  }

  private static String getVisibility(Dataset dataset) {
    return dataset.getVisibility() != null ? dataset.getVisibility().getValue() : null;
  }

  private static String getLastOperation(Dataset dataset) {
    return dataset.getLastOperation() != null ? dataset.getLastOperation().getValue() : null;
  }

  private static void serializeFeatures(Dataset dataset, DatasetEntity.DatasetEntityBuilder builder) {
    if (dataset.getFeatures() != null) {
      try {
        String featuresJson = objectMapper.writeValueAsString(dataset.getFeatures());
        builder.features(featuresJson);
      }
      catch (Exception e) {
        throw new BssException("marshal dataset.features failed, data=" + dataset.getFeatures(), e);
      }
    }
  }

  private static void serializeSpec(Dataset dataset, DatasetEntity.DatasetEntityBuilder builder) {
    if (dataset.getSpec() != null) {
      try {
        String specJson = objectMapper.writeValueAsString(dataset.getSpec());
        builder.spec(specJson);
      }
      catch (Exception e) {
        throw new BssException("marshal dataset.spec failed, data=" + dataset.getSpec(), e);
      }
    }
  }

  /**
   * 将DatasetEntity PO转换为Dataset DO
   * 迁移对应关系: Go语言DatasetPO2DO
   * - 功能: 将数据集PO转换为DO
   * - 参数: po - 数据集PO对象
   * - 返回: 数据集DO对象
   * - 用途: 数据查询后的转换
   */
  public static Dataset datasetPO2DO(DatasetEntity po) {
    if (po == null) {
      return null;
    }

    Dataset.DatasetBuilder builder = buildBasicDataset(po);
    deserializeSpec(po, builder);
    deserializeFeatures(po, builder);

    return builder.build();
  }

  private static Dataset.DatasetBuilder buildBasicDataset(DatasetEntity po) {
    return Dataset.builder()
      .id(po.getId())
      .appId(po.getAppId())
      .spaceId(po.getSpaceId())
      .schemaId(po.getSchemaId())
      .name(po.getName())
      .description(po.getDescription())
      .category(po.getCategory() != null ? DatasetCategory.fromValue(po.getCategory()) : null)
      .bizCategory(po.getBizCategory())
      .status(po.getStatus() != null ? DatasetStatus.fromValue(po.getStatus()) : null)
      .securityLevel(po.getSecurityLevel() != null ? SecurityLevel.fromValue(po.getSecurityLevel()) : null)
      .visibility(po.getVisibility() != null ? DatasetVisibility.fromValue(po.getVisibility()) : null)
      .latestVersion(po.getLatestVersion())
      .nextVersionNum(po.getNextVersionNum())
      .lastOperation(po.getLastOperation() != null ? DatasetOpType.fromValue(po.getLastOperation()) : null)
      .createdBy(po.getCreatedBy())
      .createdAt(po.getCreatedAt())
      .updatedBy(po.getUpdatedBy())
      .updatedAt(po.getUpdatedAt())
      .expiredAt(po.getExpiredAt())
      .catalogItemId(po.getCatalogItemId());
  }

  private static void deserializeSpec(DatasetEntity po, Dataset.DatasetBuilder builder) {
    if (po.getSpec() != null && !po.getSpec().isEmpty()) {
      try {
        DatasetSpec spec = objectMapper.readValue(po.getSpec(), DatasetSpec.class);
        builder.spec(spec);
      }
      catch (Exception e) {
        throw new BssException("unmarshal dataset.spec failed, data=" + po.getSpec(), e);
      }
    }
  }

  private static void deserializeFeatures(DatasetEntity po, Dataset.DatasetBuilder builder) {
    if (po.getFeatures() != null && !po.getFeatures().isEmpty()) {
      try {
        DatasetFeatures features = objectMapper.readValue(po.getFeatures(), DatasetFeatures.class);
        builder.features(features);
      }
      catch (Exception e) {
        throw new BssException("unmarshal dataset.features failed, data=" + po.getFeatures(), e);
      }
    }
  }
}
