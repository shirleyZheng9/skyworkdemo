package com.iwhalecloud.bote.entity.base;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件信息 Entity
 *
 * @author auto
 * @since 2024-09-24
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_file_info")
public class FileInfoEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long fileInfoId;
  @DiffField(name = "FILE_ID")
  @Schema(description = "文件ID")
  private Long fileId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "FILE_NAME")
  @Schema(description = "文件名称")
  private String fileName;
  @DiffField(name = "STATUS_TIME")
  @Schema(description = "状态时间")
  private Date statusTime;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @DiffField(name = "BUSI_TYPE")
  @Schema(description = "分类")
  private String busiType;
  @DiffField(name = "BUSI_SUB_TYPE")
  @Schema(description = "文件子类型")
  private String busiSubType;
  @DiffField(name = "REQ_JSON")
  @Schema(description = "组件入参")
  private String reqJson;
}
