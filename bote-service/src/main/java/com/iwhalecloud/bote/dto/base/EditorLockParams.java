package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 编辑锁参数
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */

@Setter
@Getter
@Schema(description = "编辑锁参数")
public class EditorLockParams {
  @Schema(description = "实体ID")
  private Long id;
  @Schema(description = "实体类型")
  private String type;
}
