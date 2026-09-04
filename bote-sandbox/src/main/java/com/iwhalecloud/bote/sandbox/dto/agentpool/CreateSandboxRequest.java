package com.iwhalecloud.bote.sandbox.dto.agentpool;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 创建沙箱请求
 *
 * @author bianjp
 * @since 2026-05-08
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_EMPTY)
@JsonNaming(SnakeCaseStrategy.class)
public class CreateSandboxRequest {
  /** 镜像地址 */
  private String imageUri;
  /** 沙箱过期时间(秒) */
  private Long timeout;
  /** 资源限制 */
  private ResourceLimit resourceLimit;
  /** 环境变量 */
  private Map<String, String> env;
  /** 启动命令 */
  private List<String> entrypoint;

  /**
   * 资源限制
   */
  @Getter
  @Setter
  @ToString
  @JsonInclude(Include.NON_NULL)
  @JsonNaming(SnakeCaseStrategy.class)
  public static class ResourceLimit {
    /** CPU 限制(毫核，1核=1000毫核) */
    private Integer cpuMilli;
    /** 内存限制(MB) */
    private Integer memoryMb;
  }
}
