package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.entity.base.AppPublishEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 智能体发布 DTO
 *
 * @author auto
 * @since 2025-02-12
 */
@Getter
@Setter
@ToString(callSuper = true)
public class AppPublishDTO extends AppPublishEntity {
  @Schema(description = "请求地址前缀")
  private String urlPrefix;
  @Schema(description = "应用名称")
  private String botName;
}
