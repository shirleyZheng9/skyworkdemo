package com.iwhalecloud.bote.dto.portal.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 门户目录参数
 *
 * @author chen.linfa
 * @since 2025-10-15
 */
@Getter
@Setter
@ToString
@Schema(description = "门户目录参数")
public class PortalDirParams {
  @Schema(description = "目录 ID")
  private Long dirId;

  @Schema(description = "菜单 ID 集合")
  private List<Long> menuIds;
}
