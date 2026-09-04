package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.entity.base.ApiAuthEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * API 鉴权 DTO
 *
 * @author auto
 * @since 2024-09-19
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ApiAuthDTO extends ApiAuthEntity {
  @Schema(description = "创建人")
  private String creatorName;
}
