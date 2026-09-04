package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理端单技能导入请求（ZIP 包）
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString
@Schema(description = "管理端单技能导入请求（ZIP 包上传）")
public class AdminImportRequest {

  @NotNull(message = "请上传 zip 文件")
  @Schema(description = "技能 ZIP 包", requiredMode = Schema.RequiredMode.REQUIRED)
  private MultipartFile packageFile;

  @Schema(description = "技能类型: platform, community", defaultValue = "community")
  private String skillTypeInSquare = "community";

  @Schema(description = "技能编码（可选，若不传则从 SKILL.md / meta 解析）")
  private String skillCode;
}
