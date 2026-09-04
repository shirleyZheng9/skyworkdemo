package com.iwhalecloud.bote.dto.bot.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 已上架智能应用分页查询参数
 *
 * @author lizuyin
 * @since 2025-03-04
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PublishedAppQueryParams extends PagingQueryParams {
  @Schema(description = "模糊搜索条件（应用名称、应用描述）")
  private String keyword;
  @NotNull(message = "企业空间不能为空")
  @Schema(description = "企业空间ID")
  private Long spaceId;
  @NotNull(message = "外系统项目ID不能为空")
  @Schema(description = "外系统项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long extTenantId;
}
