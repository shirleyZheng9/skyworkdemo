package com.iwhalecloud.bote.dto.datasync.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.BooleanUtils;

/**
 * 数据同步参数
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
public class DataSyncParams {
  /** 压缩工作目录 */
  private String compressDir;
  /** 解压工作目录 */
  private String decompressDir;
  /** 租户 ID */
  private Long tenantId;
  /** 重置的租户 ID */
  private Long resetTenantId;
  /** 是否重置主键 ID */
  private Boolean resetPrimaryKey;
  /** 是否全量 */
  private Boolean syncAll;
  /** 自定义方式，是否导出关联数据 */
  private Boolean relatable;
  /** 自定义方式，是否备份 */
  private Boolean backUp;
  /** 自定义方式，模块主表的主键值串 */
  private Map<String, String> codeAndIds;
  /** 数据同步节点信息 */
  @JsonIgnore
  private Map<String, List<DataSyncTableDefinition>> nodes;
  /** 表定义 */
  private List<DataSyncTableDefinition> definitions;
  /** 文件 ID 新旧值映射 */
  private Map<Long, Long> fileIdMap;
  /** 主键 ID 新旧值映射，用于复制重置主键场景 */
  private Map<String, Map<Long, Long>> primaryIdMappings;
  /** 文件 ID，用于导入场景 */
  private Long fileId;
  /** 工作空间 ID */
  private Long spaceId;
  /** 文档中心数据收集，用于特殊处理 */
  @JsonIgnore
  private List<DataSyncTableDefinition> documentCenterDefinitions = new ArrayList<>();
  /** 导入时 环境实例确认并修改 环节是否自动流转 */
  private Boolean autoConfirm;
  /** 导出文件名称 不带后缀 */
  private String exportFileName;

  /**
   * 是否复制
   */
  @JsonIgnore
  public boolean isCopy() {
    return resetTenantId != null;
  }

  /**
   * 是否重置主键
   */
  @JsonIgnore
  public boolean isResetPrimary() {
    return BooleanUtils.isTrue(resetPrimaryKey);
  }
}
