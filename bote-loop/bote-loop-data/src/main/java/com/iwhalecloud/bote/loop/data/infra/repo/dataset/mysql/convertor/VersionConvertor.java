package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetVersionEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SnapshotProgress;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SnapshotStatus;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

/**
 * 版本转换器
 * 迁移对应关系: Go语言convertor包中的Version转换器
 * - 功能: 提供版本DO和PO之间的转换
 * - 方法定义: 各种版本转换方法
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的Version转换器
 * - 使用Java静态方法提供转换功能
 * - 提供版本DO和PO之间的转换
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go JSON序列化 -> Jackson序列化
 * - Go错误处理 -> Java异常处理
 */
public final class VersionConvertor {

  private VersionConvertor() {
    // 工具类，禁止实例化
  }

  private static final ObjectMapper objectMapper = JsonMapper.builder().build();

  /**
   * 将DatasetVersion DO转换为PO
   * 迁移对应关系: Go语言VersionDO2PO
   * - 功能: 将版本DO转换为PO
   * - 参数: datasetVersion - 版本DO对象
   * - 返回: 版本PO对象
   * - 用途: 数据持久化前的转换
   */
  public static DatasetVersionEntity versionDO2PO(DatasetVersion datasetVersion) {
    if (datasetVersion == null) {
      return null;
    }

    DatasetVersionEntity.DatasetVersionEntityBuilder builder = DatasetVersionEntity.builder()
      .id(datasetVersion.getId())
      .appId(datasetVersion.getAppId())
      .spaceId(datasetVersion.getSpaceId())
      .datasetId(datasetVersion.getDatasetId())
      .schemaId(datasetVersion.getSchemaId())
      .version(datasetVersion.getVersion())
      .versionNum(datasetVersion.getVersionNum())
      .description(datasetVersion.getDescription())
      .itemCount(datasetVersion.getItemCount())
      .snapshotStatus(datasetVersion.getSnapshotStatus() != null ? datasetVersion.getSnapshotStatus().getValue() : null)
      .updateVersion(datasetVersion.getUpdateVersion())
      .createdBy(datasetVersion.getCreatedBy())
      .createdAt(datasetVersion.getCreatedAt())
      .disabledAt(datasetVersion.getDisabledAt());

    // 处理数据集简介
    if (datasetVersion.getDatasetBrief() != null) {
      try {
        String datasetBriefJson = objectMapper.writeValueAsString(datasetVersion.getDatasetBrief());
        builder.datasetBrief(datasetBriefJson);
      }
      catch (Exception e) {
        throw new BssException("marshal version.DatasetBrief failed, data=" + datasetVersion.getDatasetBrief(), e);
      }
    }

    // 处理快照进度
    if (datasetVersion.getSnapshotProgress() != null) {
      try {
        String snapshotProgressJson = objectMapper.writeValueAsString(datasetVersion.getSnapshotProgress());
        builder.snapshotProgress(snapshotProgressJson);
      }
      catch (Exception e) {
        throw new BssException("marshal version.SnapshotProgress failed, data=" + datasetVersion.getSnapshotProgress(), e);
      }
    }

    return builder.build();
  }

  /**
   * 将DatasetVersion PO转换为DO
   * 迁移对应关系: Go语言ConvertVersionPOToDO
   * - 功能: 将版本PO转换为DO
   * - 参数: po - 版本PO对象
   * - 返回: 版本DO对象
   * - 用途: 数据查询后的转换
   */
  public static DatasetVersion versionPO2DO(DatasetVersionEntity po) {
    if (po == null) {
      return null;
    }

    DatasetVersion.DatasetVersionBuilder builder = DatasetVersion.builder()
      .id(po.getId())
      .appId(po.getAppId())
      .spaceId(po.getSpaceId())
      .datasetId(po.getDatasetId())
      .schemaId(po.getSchemaId())
      .version(po.getVersion())
      .versionNum(po.getVersionNum())
      .description(po.getDescription())
      .itemCount(po.getItemCount())
      .snapshotStatus(po.getSnapshotStatus() != null ? SnapshotStatus.fromValue(po.getSnapshotStatus()) : null)
      .updateVersion(po.getUpdateVersion())
      .createdBy(po.getCreatedBy())
      .createdAt(po.getCreatedAt())
      .disabledAt(po.getDisabledAt());

    // 处理数据集简介
    if (po.getDatasetBrief() != null && !po.getDatasetBrief().isEmpty()) {
      try {
        Dataset datasetBrief = objectMapper.readValue(po.getDatasetBrief(), Dataset.class);
        builder.datasetBrief(datasetBrief);
      }
      catch (Exception e) {
        throw new BssException("unmarshal version.DatasetBrief failed, data=" + po.getDatasetBrief(), e);
      }
    }

    // 处理快照进度
    if (po.getSnapshotProgress() != null && !po.getSnapshotProgress().isEmpty()) {
      try {
        SnapshotProgress snapshotProgress = objectMapper.readValue(po.getSnapshotProgress(), SnapshotProgress.class);
        builder.snapshotProgress(snapshotProgress);
      }
      catch (Exception e) {
        throw new BssException("unmarshal version.SnapshotProgress failed, data=" + po.getSnapshotProgress(), e);
      }
    }

    return builder.build();
  }
}
