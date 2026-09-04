package com.iwhalecloud.bote.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 批量保存用户常用应用 DTO
 *
 * @author wang.tingyun
 * @since 2025-09-12
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BatchSaveUserFavAppDTO {

  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "工作空间ID")
  private Long spaceId;
  @Schema(description = "用户应用列表")
  private List<UserFavAppDTO> appList;

}
