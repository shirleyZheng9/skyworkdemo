package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 运营贡献者/提问用户排名 DTO
 *
 * @author qian.sisheng
 * @since 2026/02/27
 */
@Getter
@Setter
@ToString
@Schema(description = "运营贡献者/提问用户排名DTO")
public class KnowledgeOpsContributorDTO {

  @Schema(description = "排名序号（全局，从1开始）")
  private Integer rankNo;
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "真实姓名")
  private String realName;
  @Schema(description = "上传文档数量")
  private Long uploadCount;
  @Schema(description = "提问数量")
  private Long questionCount;
  @Schema(description = "等级标签")
  private String levelLabel;
  @Schema(description = "上传文档占比")
  private BigDecimal uploadRate;
  @Schema(description = "提问占比")
  private BigDecimal questionRate;
}
