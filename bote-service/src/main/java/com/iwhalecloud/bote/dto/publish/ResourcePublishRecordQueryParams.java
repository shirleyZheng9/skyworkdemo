package com.iwhalecloud.bote.dto.publish;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 资源发布记录分页查询参数
 *
 * @author lizuyin
 * @since 2025-08-04
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ResourcePublishRecordQueryParams extends PagingQueryParams {
  @Schema(description = "资源类型")
  private String resourceType;
  @Schema(description = "发布渠道")
  private String publishChannel;
  @Schema(description = "发布状态")
  private String publishStatus;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "资源名称（模糊搜索）")
  private String resourceName;
}
