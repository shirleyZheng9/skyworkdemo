package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库运营概览 DTO
 *
 * @author qian.sisheng
 * @since 2026/02/27
 */
@Getter
@Setter
@ToString
public class KnowledgeOpsOverviewDTO {
  @Schema(description = "知识数量")
  private Integer knowledgeCount;
  @Schema(description = "文档数量")
  private Integer documentCount;
  @Schema(description = "用户问数量")
  private Integer userAskCount;
  @Schema(description = "文档贡献者数量")
  private Integer documentContributorCount;
}
