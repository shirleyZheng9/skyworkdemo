package com.iwhalecloud.bote.loop.data.application.convertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVersionDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.SnapshotStatusDTO;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SnapshotStatus;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

/**
 * 数据集版本转换器
 * 迁移对应关系: Go语言backend/modules/data/application/convertor/dataset/version.go
 * - 功能: 数据集版本相关的DO和DTO转换
 * - 主要方法:
 * * versionDO2DTO - 数据集版本DO转DTO
 * * snapshotStatusDO2DTO - 快照状态DO转DTO
 * <p>
 * Java实现说明:
 * - 对应Go的version.go文件
 * - 使用静态方法进行转换
 * - 处理JSON序列化逻辑
 * - 包含时间戳转换逻辑
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go switch语句 -> Java switch表达式
 * - Go error返回 -> Java异常处理
 * - Go指针操作 -> Java对象操作
 * - Go时间戳 -> Java时间戳
 * - Go JSON序列化 -> Java Jackson序列化
 */
public final class DatasetVersionConvertor {

  private DatasetVersionConvertor() {
    // 工具类，禁止实例化
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 数据集版本DO转DTO
   * 迁移对应关系: Go语言VersionDO2DTO方法
   *
   * @param version 数据集版本DO
   * @return 数据集版本DTO
   */
  public static DatasetVersionDTO versionDO2DTO(DatasetVersion version) {
    if (version == null) {
      return null;
    }

    DatasetVersionDTO dto = new DatasetVersionDTO();
    dto.setId(version.getId());
    dto.setAppId(version.getAppId());
    dto.setSpaceId(version.getSpaceId());
    dto.setDatasetId(version.getDatasetId());
    dto.setSchemaId(version.getSchemaId());
    dto.setVersion(version.getVersion());
    dto.setVersionNum(version.getVersionNum());
    dto.setDescription(version.getDescription());
    dto.setItemCount(version.getItemCount());
    dto.setSnapshotStatus(snapshotStatusDO2DTO(version.getSnapshotStatus()));
    dto.setCreatedBy(version.getCreatedBy());
    dto.setCreatedAt(version.getCreatedAt().getTime());

    if (version.getDisabledAt() != null) {
      dto.setDisabledAt(version.getDisabledAt().getTime());
    }

    if (version.getDatasetBrief() != null) {
      try {
        String datasetBriefStr = objectMapper.writeValueAsString(version.getDatasetBrief());
        dto.setDatasetBrief(datasetBriefStr);
      }
      catch (JsonProcessingException e) {
        throw new BssException("marshal dataset_version.dataset_brief failed, data=" + version.getDatasetBrief() + ", error=" + e.getMessage(), e);
      }
    }

    return dto;
  }

  /**
   * 快照状态DO转DTO
   * 迁移对应关系: Go语言SnapshotStatusDO2DTO方法
   *
   * @param status DO层快照状态
   * @return DTO层快照状态
   */
  public static SnapshotStatusDTO snapshotStatusDO2DTO(SnapshotStatus status) {
    if (status == null) {
      return null;
    }
    // 直接映射，避免字符串转换问题
    return switch (status) {
      case UNSTARTED -> SnapshotStatusDTO.UNSTARTED;
      case IN_PROGRESS -> SnapshotStatusDTO.IN_PROGRESS;
      case COMPLETED -> SnapshotStatusDTO.COMPLETED;
      case FAILED -> SnapshotStatusDTO.FAILED;
      case UNKNOWN -> null; // UNKNOWN状态返回null
    };
  }
}
