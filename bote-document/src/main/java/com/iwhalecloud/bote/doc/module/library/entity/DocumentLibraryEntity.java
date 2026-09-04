package com.iwhalecloud.bote.doc.module.library.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档库表
 *
 * @author yangran
 * @since 2025-08-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_document_library")
@Schema(hidden = true)
public class DocumentLibraryEntity extends BaseEntity {
  @Id
  @DiffId
  private Long id;
  @DiffField(name = "library_id")
  @Schema(description = "文档库ID")
  private String libraryId;
  @DiffField(name = "library_name")
  @Schema(description = "文档库名称")
  @Size(max = 20, message = "文档库名称超过限定长度20")
  private String libraryName;
  @DiffField(name = "library_icon")
  @Schema(description = "文档库图标URL")
  private String libraryIcon;
  @DiffField(name = "description")
  @Schema(description = "描述")
  @Size(max = 100, message = "文档库描述超过限定长度100")
  private String description;
  @DiffField(name = "visibility_scope")
  @Schema(description = "可见范围：PUBLIC-全员可见，MEMBERS-成员可见，PRIVATE-私有，OTHER-其他")
  private String visibilityScope;
  @DiffField(name = "user_id")
  @Schema(description = "私有文档库对应的用户ID")
  private Long ownerId;
  @DiffField(name = "is_builtin")
  @Schema(description = "是否系统内置：T-是（不可删除/重命名），F-否")
  private String isBuiltin;
  @DiffField(name = "sort_order")
  @Schema(description = "排序序号")
  private Integer sortOrder;
  @DiffField(name = "access_count")
  @Schema(description = "访问次数统计")
  private Integer accessCount;
  @DiffField(name = "last_access_time")
  @Schema(description = "最后访问时间")
  private Date lastAccessTime;
  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "space_id")
  @Schema(description = "空间ID")
  private Long spaceId;
  @DiffField(name = "COLOR")
  @Schema(description = "颜色")
  private String color;
}


