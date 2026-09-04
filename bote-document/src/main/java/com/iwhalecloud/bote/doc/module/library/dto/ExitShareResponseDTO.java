package com.iwhalecloud.bote.doc.module.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "退出共享文档库响应")
public class ExitShareResponseDTO {

  @Schema(description = "文档库ID")
  private String libraryId;
}
