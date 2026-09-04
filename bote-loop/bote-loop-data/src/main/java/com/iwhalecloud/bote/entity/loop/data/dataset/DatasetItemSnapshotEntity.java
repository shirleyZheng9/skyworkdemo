package com.iwhalecloud.bote.entity.loop.data.dataset;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集条目快照表持久化对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bt_dataset_item_snapshot")
public class DatasetItemSnapshotEntity {

  /**
   * 主键id
   */
  @Id
  private Long id;

  /**
   * 应用ID
   */
  private Integer appId;

  /**
   * 空间ID
   */
  private Long spaceId;

  /**
   * 数据集ID
   */
  private Long datasetId;

  /**
   * Schema ID
   */
  private Long schemaId;

  /**
   * Version ID
   */
  private Long versionId;

  /**
   * 条目主键ID
   */
  private Long itemPrimaryId;

  /**
   * 条目ID
   */
  private Long itemId;

  /**
   * 条目幂等key
   */
  private String itemKey;

  /**
   * 数据内容
   */
  private String data;

  /**
   * 多轮数据内容
   */
  private String repeatedData;

  /**
   * 内容属性
   */
  private String dataProperties;

  /**
   * 添加版本号
   */
  private Long addVn;

  /**
   * 删除版本号
   */
  private Long delVn;

  /**
   * snapshot创建时间
   */
  private Date createdAt;

  /**
   * item创建人
   */
  private String itemCreatedBy;

  /**
   * item创建时间
   */
  private Date itemCreatedAt;

  /**
   * item修改人
   */
  private String itemUpdatedBy;

  /**
   * item修改时间
   */
  private Date itemUpdatedAt;
}
