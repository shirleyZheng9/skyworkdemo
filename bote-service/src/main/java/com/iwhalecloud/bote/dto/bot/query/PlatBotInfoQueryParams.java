package com.iwhalecloud.bote.dto.bot.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 语料基本信息查询参数
 *
 * @author auto
 * @since 2025-05-26
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "语料基本信息查询参数")
public class PlatBotInfoQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "空间ID")
  private Long spaceId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "组织ID列表", hidden = true)
  private List<Long> orgIdList;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "来源")
  private String source;
  @Schema(description = "是否超级管理员")
  private Boolean isAdmin;
  @Schema(description = "数据来源")
  private String dataFrom;
}
