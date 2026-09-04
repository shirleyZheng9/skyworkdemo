package com.iwhalecloud.bote.loop.data.application.convertor;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetCategoryDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetFeaturesDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetSchemaDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetSpecDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetStatusDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVisibilityDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldSchemaDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.SecurityLevelDTO;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetCategory;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetFeatures;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSpec;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVisibility;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SecurityLevel;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据集转换器
 * 迁移对应关系: Go语言backend/modules/data/application/convertor/dataset/dataset.go
 * - 功能: 数据集相关的DO和DTO转换
 * - 主要方法:
 * * datasetDO2DTO - 数据集DO转DTO
 * * schemaDO2DTO - 数据集模式DO转DTO
 * * datasetStatusDO2DTO - 数据集状态DO转DTO
 * * datasetCategoryDO2DTO - 数据集分类DO转DTO
 * * securityLevelDO2DTO - 安全级别DO转DTO
 * * datasetVisibilityDO2DTO - 数据集可见性DO转DTO
 * * datasetSpecDO2DTO - 数据集规格DO转DTO
 * * datasetFeaturesDO2DTO - 数据集特性DO转DTO
 * * convertCategoryDTO2DO - 分类DTO转DO
 * * securityLevelDTO2DO - 安全级别DTO转DO
 * * visibilityDTO2DO - 可见性DTO转DO
 * * specDTO2DO - 规格DTO转DO
 * * featuresDTO2DO - 特性DTO转DO
 * <p>
 * Java实现说明:
 * - 对应Go的dataset.go文件
 * - 使用静态方法进行转换
 * - 处理所有枚举类型转换
 * - 包含时间戳转换逻辑
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go switch语句 -> Java switch表达式
 * - Go error返回 -> Java异常处理
 * - Go指针操作 -> Java对象操作
 * - Go时间戳 -> Java时间戳
 */
public final class DatasetConvertor {

  private DatasetConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 数据集DO转DTO
   * 迁移对应关系: Go语言DatasetDO2DTO方法
   *
   * @param dataset 数据集DO
   * @param schema 数据集模式DO
   * @return 数据集DTO
   */
  public static DatasetDTO datasetDO2DTO(Dataset dataset, DatasetSchema schema) {
    if (dataset == null) {
      return null;
    }

    DatasetDTO dto = new DatasetDTO();
    dto.setId(dataset.getId());
    dto.setAppId(dataset.getAppId());
    dto.setSpaceId(dataset.getSpaceId());
    dto.setSchemaId(dataset.getSchemaId());
    dto.setName(dataset.getName());
    dto.setDescription(dataset.getDescription());
    dto.setStatus(datasetStatusDO2DTO(dataset.getStatus()));
    dto.setCategory(datasetCategoryDO2DTO(dataset.getCategory()));
    dto.setBizCategory(dataset.getBizCategory());
    dto.setSecurityLevel(securityLevelDO2DTO(dataset.getSecurityLevel()));
    dto.setVisibility(datasetVisibilityDO2DTO(dataset.getVisibility()));
    dto.setSpec(datasetSpecDO2DTO(dataset.getSpec()));
    dto.setFeatures(datasetFeaturesDO2DTO(dataset.getFeatures()));
    dto.setLatestVersion(dataset.getLatestVersion());
    dto.setNextVersionNum(dataset.getNextVersionNum());
    dto.setChangeUncommitted(dataset.isChangeUncommitted());
    dto.setItemCount(0L); // 需要额外从 redis 读取，此处不填充
    dto.setCreatedBy(dataset.getCreatedBy());
    dto.setCreatedAt(dataset.getCreatedAt().getTime());
    dto.setUpdatedBy(dataset.getUpdatedBy());
    dto.setUpdatedAt(dataset.getUpdatedAt().getTime());
    dto.setCatalogItemId(dataset.getCatalogItemId());
    if (dataset.getExpiredAt() != null) {
      dto.setExpiredAt(dataset.getExpiredAt().getTime());
    }

    if (schema != null) {
      dto.setSchema(schemaDO2DTO(schema));
    }

    return dto;
  }

  /**
   * 数据集模式DO转DTO
   * 迁移对应关系: Go语言SchemaDO2DTO方法
   *
   * @param schema 数据集模式DO
   * @return 数据集模式DTO
   */
  public static DatasetSchemaDTO schemaDO2DTO(DatasetSchema schema) {
    if (schema == null) {
      return null;
    }

    List<FieldSchemaDTO> fields = null;
    if (schema.getFields() != null) {
      fields = schema.getFields().stream()
        .map(DatasetSchemaConvertor::fieldSchemaDO2DTO)
        .collect(Collectors.toList());
    }

    DatasetSchemaDTO dto = new DatasetSchemaDTO();
    dto.setId(schema.getId());
    dto.setAppId(schema.getAppId());
    dto.setSpaceId(schema.getSpaceId());
    dto.setDatasetId(schema.getDatasetId());
    dto.setFields(fields);
    dto.setImmutable(schema.getImmutable());
    dto.setCreatedBy(schema.getCreatedBy());
    dto.setCreatedAt(schema.getCreatedAt().getTime());
    dto.setUpdatedBy(schema.getUpdatedBy());
    dto.setUpdatedAt(schema.getUpdatedAt().getTime());
    dto.setUpdateVersion(schema.getUpdateVersion());

    return dto;
  }

  /**
   * 数据集状态DO转DTO
   * 迁移对应关系: Go语言DatasetStatusDO2DTO方法
   *
   * @param status DO层数据集状态
   * @return DTO层数据集状态
   */
  public static DatasetStatusDTO datasetStatusDO2DTO(DatasetStatus status) {
    if (status == null) {
      return null;
    }

    return switch (status) {
      case AVAILABLE -> DatasetStatusDTO.AVAILABLE;
      case DELETED -> DatasetStatusDTO.DELETED;
      case EXPIRED -> DatasetStatusDTO.EXPIRED;
      case IMPORTING -> DatasetStatusDTO.IMPORTING;
      case EXPORTING -> DatasetStatusDTO.EXPORTING;
      case INDEXING -> DatasetStatusDTO.INDEXING;
      default -> null;
    };
  }

  /**
   * 数据集分类DO转DTO
   * 迁移对应关系: Go语言DatasetCategoryDO2DTO方法
   *
   * @param category DO层数据集分类
   * @return DTO层数据集分类
   */
  public static DatasetCategoryDTO datasetCategoryDO2DTO(DatasetCategory category) {
    if (category == null) {
      return null;
    }

    return switch (category) {
      case GENERAL -> DatasetCategoryDTO.GENERAL;
      case TRAINING -> DatasetCategoryDTO.TRAINING;
      case VALIDATION -> DatasetCategoryDTO.VALIDATION;
      case EVALUATION -> DatasetCategoryDTO.EVALUATION;
      default -> null;
    };
  }

  /**
   * 安全级别DO转DTO
   * 迁移对应关系: Go语言SecurityLevelDO2DTO方法
   *
   * @param securityLevel DO层安全级别
   * @return DTO层安全级别
   */
  public static SecurityLevelDTO securityLevelDO2DTO(SecurityLevel securityLevel) {
    if (securityLevel == null) {
      return null;
    }

    return switch (securityLevel) {
      case L1 -> SecurityLevelDTO.L1;
      case L2 -> SecurityLevelDTO.L2;
      case L3 -> SecurityLevelDTO.L3;
      case L4 -> SecurityLevelDTO.L4;
      default -> null;
    };
  }

  /**
   * 数据集可见性DO转DTO
   * 迁移对应关系: Go语言DatasetVisibilityDO2DTO方法
   *
   * @param visibility DO层数据集可见性
   * @return DTO层数据集可见性
   */
  public static DatasetVisibilityDTO datasetVisibilityDO2DTO(DatasetVisibility visibility) {
    if (visibility == null) {
      return null;
    }

    return switch (visibility) {
      case PUBLIC -> DatasetVisibilityDTO.PUBLIC;
      case SPACE -> DatasetVisibilityDTO.SPACE;
      case SYSTEM -> DatasetVisibilityDTO.SYSTEM;
      default -> null;
    };
  }

  /**
   * 数据集规格DO转DTO
   * 迁移对应关系: Go语言DatasetSpecDO2DTO方法
   *
   * @param spec 数据集规格DO
   * @return 数据集规格DTO
   */
  public static DatasetSpecDTO datasetSpecDO2DTO(DatasetSpec spec) {
    if (spec == null) {
      return null;
    }

    DatasetSpecDTO dto = new DatasetSpecDTO();
    dto.setMaxItemCount(spec.getMaxItemCount());
    dto.setMaxFieldCount(spec.getMaxFieldCount());
    dto.setMaxItemSize(spec.getMaxItemSize());
    return dto;
  }

  /**
   * 数据集特性DO转DTO
   * 迁移对应关系: Go语言DatasetFeaturesDO2DTO方法
   *
   * @param features 数据集特性DO
   * @return 数据集特性DTO
   */
  public static DatasetFeaturesDTO datasetFeaturesDO2DTO(DatasetFeatures features) {
    if (features == null) {
      return null;
    }

    DatasetFeaturesDTO dto = new DatasetFeaturesDTO();
    dto.setEditSchema(features.getEditSchema());
    dto.setRepeatedData(features.getRepeatedData());
    dto.setMultiModal(features.getMultiModal());
    return dto;
  }

  /**
   * 分类DTO转DO
   * 迁移对应关系: Go语言ConvertCategoryDTO2DO方法
   *
   * @param category DTO层数据集分类
   * @return DO层数据集分类
   */
  public static DatasetCategory convertCategoryDTO2DO(DatasetCategoryDTO category) {
    if (category == null) {
      return DatasetCategory.UNKNOWN;
    }

    return switch (category) {
      case GENERAL -> DatasetCategory.GENERAL;
      case TRAINING -> DatasetCategory.TRAINING;
      case VALIDATION -> DatasetCategory.VALIDATION;
      case EVALUATION -> DatasetCategory.EVALUATION;
      default -> DatasetCategory.UNKNOWN;
    };
  }

  /**
   * 安全级别DTO转DO
   * 迁移对应关系: Go语言SecurityLevelDTO2DO方法
   *
   * @param securityLevel DTO层安全级别
   * @return DO层安全级别
   */
  public static SecurityLevel securityLevelDTO2DO(SecurityLevelDTO securityLevel) {
    if (securityLevel == null) {
      return SecurityLevel.UNKNOWN;
    }

    return switch (securityLevel) {
      case L1 -> SecurityLevel.L1;
      case L2 -> SecurityLevel.L2;
      case L3 -> SecurityLevel.L3;
      case L4 -> SecurityLevel.L4;
      default -> SecurityLevel.UNKNOWN;
    };
  }

  /**
   * 可见性DTO转DO
   * 迁移对应关系: Go语言VisibilityDTO2DO方法
   *
   * @param visibility DTO层数据集可见性
   * @return DO层数据集可见性
   */
  public static DatasetVisibility visibilityDTO2DO(DatasetVisibilityDTO visibility) {
    if (visibility == null) {
      return DatasetVisibility.UNKNOWN;
    }

    return switch (visibility) {
      case PUBLIC -> DatasetVisibility.PUBLIC;
      case SPACE -> DatasetVisibility.SPACE;
      case SYSTEM -> DatasetVisibility.SYSTEM;
      default -> DatasetVisibility.UNKNOWN;
    };
  }

  /**
   * 规格DTO转DO
   * 迁移对应关系: Go语言SpecDTO2DO方法
   *
   * @param datasetSpec 数据集规格DTO
   * @return 数据集规格DO
   */
  public static DatasetSpec specDTO2DO(DatasetSpecDTO datasetSpec) {
    if (datasetSpec == null) {
      return null;
    }

    DatasetSpec spec = new DatasetSpec();
    spec.setMaxItemCount(datasetSpec.getMaxItemCount());
    spec.setMaxFieldCount(datasetSpec.getMaxFieldCount());
    spec.setMaxItemSize(datasetSpec.getMaxItemSize());
    return spec;
  }

  /**
   * 特性DTO转DO
   * 迁移对应关系: Go语言FeaturesDTO2DO方法
   *
   * @param dto 数据集特性DTO
   * @return 数据集特性DO
   */
  public static DatasetFeatures featuresDTO2DO(DatasetFeaturesDTO dto) {
    if (dto == null) {
      return null;
    }

    DatasetFeatures features = new DatasetFeatures();
    features.setEditSchema(dto.getEditSchema());
    features.setRepeatedData(dto.getRepeatedData());
    features.setMultiModal(dto.getMultiModal());
    return features;
  }
}
