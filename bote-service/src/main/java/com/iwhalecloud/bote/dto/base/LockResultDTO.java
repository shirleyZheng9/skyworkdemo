package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 编辑所返回结构
 *
 * @author qian.sisheng
 * @since 2025-05-09
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LockResultDTO {
  @Schema(description = "返回类型: success续锁成功；fail.occupied:已被占用；fail.nothold:未持有页面资源")
  private String type;

  @Schema(description = "返回信息")
  private String message;

  @Schema(description = "用户名称")
  private String realName;
}
