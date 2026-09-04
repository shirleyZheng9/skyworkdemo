package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据源测试参数
 *
 * @author qian.sisheng
 * @since 2024/11/14
 */
@Getter
@Setter
@ToString
public class DataSourceTestParams {
  @Schema(description = "地址")
  private String url;
  @Schema(description = "用户名")
  private String username;
  @Schema(description = "密码")
  private String password;
}
