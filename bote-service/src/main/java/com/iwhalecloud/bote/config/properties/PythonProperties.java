package com.iwhalecloud.bote.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Python 配置
 *
 * @author bianjp
 * @since 2024-08-02
 */
@Component
@ConfigurationProperties("bote.python")
@Getter
@Setter
@ToString
public class PythonProperties {
  /** python 命令。默认为 python3，从系统的 PATH 中寻找命令；也可以配置命令的完整路径。 */
  private String executable = "python3";
  /** pip 命令。默认为 pip3，从系统的 PATH 中寻找命令；也可以配置命令的完整路径。 */
  private String pipExecutable = "pip3";
  /** uv 命令。默认为 uv，从系统的 PATH 中寻找命令；也可以配置命令的完整路径。 */
  private String uvExecutable = "uv";
  /** uv 缓存大小限制（单位Byte）。默认为 500 MiB */
  private Long uvCacheSize = 524288000L;
}
