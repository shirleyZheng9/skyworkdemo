package com.iwhalecloud.bote.doc.module.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentActivityEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档动态记录
 *
 * @author Aiqing
 * @since 2025-08-21
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "文档动态记录")
public class DocumentActivityDTO extends DocumentActivityEntity {

  @Schema(description = "用户名称")
  private String userName;
  @Schema(description = "用户头像")
  private String userIcon;

  @Schema(description = "文档名称")
  private String documentName;
  @Schema(description = "文档类型")
  private String documentType;

  @Schema(description = "文档库名称")
  private String libraryName;

  @Schema(description = "目标文档库名称")
  private String targetLibraryName;

  @Schema(description = "操作类型名称")
  private String actionTypeName;

  @Schema(description = "文档是否已删除")
  private Boolean documentDeleted;

  @Schema(description = "文档库是否已删除")
  private Boolean targetLibraryDeleted;
}
