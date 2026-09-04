package com.iwhalecloud.bote.dto.portal.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户查询参数
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "租户查询参数")
public class TenantQueryParams extends PagingQueryParams {
  @Schema(description = "名称/编码")
  private String searchContent;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "机器人 ID")
  private Long botId;
  @Schema(description = "用户 ID")
  private Long userId;
  @Schema(description = "是否机器人广场")
  private String isBotSquare;
  @Schema(description = "功能类型")
  private List<String> funcTypes;
  @Schema(description = "工作空间 ID")
  private Long spaceId;
}
