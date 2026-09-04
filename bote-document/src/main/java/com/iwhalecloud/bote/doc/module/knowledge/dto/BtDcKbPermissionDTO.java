package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.iwhalecloud.bote.doc.module.knowledge.entity.BtDcKbPermissionEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库权限表 DTO
 *
 * @author linmengfan
 * @since 2025-08-23
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BtDcKbPermissionDTO extends BtDcKbPermissionEntity {

  @Schema(description = "是否为所有者")
  private Boolean isOwner;

  @Schema(description = "主体名称（用户名、部门名）")
  private String subjectName;

  @Schema(description = "赋权人")
  private String grantedByName;

}
