package com.iwhalecloud.bote.dto.lcdp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 灵犀平台页面实例 DTO
 *
 * @author qian.sisheng
 * @since 2025-06-05
 */
@Getter
@Setter
@ToString
@Schema(description = "灵犀平台页面实例")
public class LcdpPageInstDTO {
  @Schema(description = "页面实例ID")
  private Long pageId;
  @Schema(description = "页面实例名称")
  private String pageName;
  @Schema(description = "页面实例编码")
  private String pageCode;
  @Schema(description = "页面容器类型-弹窗：Modal；单独页面：Page; 推拉门:Drawer")
  private String pageContainerType;
  @Schema(description = "应用ID")
  private Long appId;
  @Schema(description = "页面路径")
  private String pagePath;
  @Schema(description = "页面布局")
  private String pageLayout;
  @Schema(description = "终端类型")
  private String terminalType;
  @Schema(description = "默认版本ID")
  private String defaultVersionId;
  @Schema(description = "页面参数")
  private List<PageParams> params;

  /**
   * 页面参数
   */
  @Getter
  @Setter
  @ToString
  public static class PageParams {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "编码")
    private String code;
  }
}
