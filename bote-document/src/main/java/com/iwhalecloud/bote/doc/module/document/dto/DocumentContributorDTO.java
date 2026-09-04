package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档贡献者DTO
 *
 * @author Aiqing
 * @since 2025-09-18
 */
@Getter
@Setter
@ToString
public class DocumentContributorDTO {

  @Schema(description = "文档唯一编码")
  private String documentId;

  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "用户名称")
  private String userName;
  @Schema(description = "用户编码")
  private String userCode;

  @DiffField(name = "contributor_score")
  @Schema(description = "贡献者得分")
  private BigDecimal contributorScore;
}
