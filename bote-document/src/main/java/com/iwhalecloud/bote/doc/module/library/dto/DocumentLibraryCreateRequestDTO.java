package com.iwhalecloud.bote.doc.module.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DocumentLibraryCreateRequestDTO {

  @NotBlank
  @Schema(description = "文档库名称")
  private String libraryName;

  @Schema(description = "描述")
  private String description;

  @Schema(description = "可见范围：PUBLIC/MEMBERS/PRIVATE/OTHER")
  private String visibilityScope;

  @Schema(description = "文档库图标URL")
  private String libraryIcon;
}


