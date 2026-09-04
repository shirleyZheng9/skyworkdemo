package com.iwhalecloud.bote.doc.module.library.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "退出共享文档库请求")
public class ExitShareRequestDTO extends TenantBaseRO {

  @NotBlank(message = "文档库ID不能为空")
  @Schema(description = "文档库ID")
  private String libraryId;
}
