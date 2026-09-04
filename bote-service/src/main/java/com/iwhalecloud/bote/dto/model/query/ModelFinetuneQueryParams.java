package com.iwhalecloud.bote.dto.model.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模型微调查询参数
 *
 * @author auto
 * @since 2025-03-03
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "模型微调查询参数")
public class ModelFinetuneQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;

  @Schema(description = "用途")
  private String useType;

  @Schema(description = "状态")
  private String status;

  @Schema(description = "租户 ID")
  private Long tenantId;
}
