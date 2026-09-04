package com.iwhalecloud.bote.dto.datasync.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 复制数据入参
 *
 * @author chen.linfa
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class CopyDataParams {
  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "重置的租户 ID")
  private Long resetTenantId;

  @Schema(description = "是否全量")
  private Boolean syncAll;

  @Schema(description = "自定义方式，是否复制关联数据")
  private Boolean relatable;

  @Schema(description = "自定义方式，模块主表的主键值串")
  private Map<String, String> codeAndIds;

  @Schema(description = "是否重置主键 ID")
  private Boolean resetPrimaryKey;

  @Schema(description = "空间 ID")
  private Long spaceId;
}
