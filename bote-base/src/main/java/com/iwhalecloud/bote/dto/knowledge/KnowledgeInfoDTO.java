package com.iwhalecloud.bote.dto.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识库设置信息
 *
 * @author system
 * @since 2024-12-19
 */
@Data
@Schema(description = "知识库设置信息")
public class KnowledgeInfoDTO {

  @Schema(description = "知识库类型")
  private String knowledgeType;

  @Schema(description = "知识库地址")
  private String serviceUrl;

  @Schema(description = "知识库页面")
  private String webUrl;

  @Schema(description = "AppKey")
  private String appKey;

  @Schema(description = "密钥")
  private String secretKey;

  @Schema(description = "页面条件")
  private PageCondition pageCondition;

  @Schema(description = "是否启用")
  private Boolean enabled;

  /**
   * 页面条件内部类
   */
  @Data
  @Schema(description = "页面条件")
  public static class PageCondition {

    @Schema(description = "是否显示目录")
    private Boolean showCatalog;

    @Schema(description = "是否显示文件")
    private Boolean showFile;
  }
}
