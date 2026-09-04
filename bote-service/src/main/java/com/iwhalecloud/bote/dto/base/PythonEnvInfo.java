package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Python 3 环境信息
 *
 * @author wangtingyun
 * @since 2026-03-04
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Python 3 环境信息")
public class PythonEnvInfo {
  @Schema(description = "Python 版本")
  private String version;
  @Schema(description = "可用的包列表")
  private List<PythonPackage> packages;
}
