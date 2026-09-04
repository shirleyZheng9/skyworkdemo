package com.iwhalecloud.bote.dto.lcdp.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Lcdp查询参数
 *
 * @author qian.sisheng
 * @since 2025-06-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "Lcdp查询参数")
public class LcdpQueryParams extends PagingQueryParams {
  @Schema(description = "应用ID")
  private Long appId;
  @Schema(description = "模糊搜索内容")
  private String searchContent;
  @Schema(description = "页面终端类型, pc: PC, H5: APP")
  private String terminalType;
  @Schema(description = "页面容器类型: 页面 Page")
  private String pageContainerType;
  @Schema(description = "服务类型: orchestration: 编排服务")
  private String serviceType;
  @Schema(description = "SQL服务ID列表")
  private List<Long> sqlServiceIds;
  @Schema(description = "编排服务ID列表")
  private List<Long> standardServiceIds;
  @Schema(description = "平台服务ID列表")
  private List<Long> platformServiceIds;
  @Schema(description = "页面实例版本ID列表")
  private List<Long> pageVersionIds;
  @Schema(description = "属性规格ID列表")
  private List<Long> attrSpecAttrIds;
}
