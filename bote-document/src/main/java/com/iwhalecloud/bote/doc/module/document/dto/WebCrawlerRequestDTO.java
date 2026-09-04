package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网页爬虫请求参数
 *
 * @author Auto Generated
 * @since 2025-01-XX
 */
@Getter
@Setter
@ToString
public class WebCrawlerRequestDTO extends TenantBaseRO {

  @Schema(description = "网页URL", example = "https://www.example.com", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "网页URL不能为空")
  private String url;

  @Schema(description = "文档库ID", example = "lbr20", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "文档库ID不能为空")
  private String libraryId;

  @Schema(description = "父节点ID（可选，为空则在根节点下创建）", example = "fod10")
  private String parentId;

  @Schema(description = "文档名称（可选，为空则使用网页标题）", example = "示例文档")
  private String documentName;
}

