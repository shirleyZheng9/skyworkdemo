package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Python 包
 *
 * @author wangtingyun
 * @since 2026-03-04
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Python 包")
public class PythonPackage {
  @Schema(description = "包名")
  private String name;
  @Schema(description = "版本")
  private String version;
}
