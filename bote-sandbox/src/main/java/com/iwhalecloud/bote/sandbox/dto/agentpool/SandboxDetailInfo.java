package com.iwhalecloud.bote.sandbox.dto.agentpool;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 沙箱详情
 *
 * <p>未包含一些用不到的属性</p>
 *
 * @author bianjp
 * @since 2026-05-08
 */
@Getter
@Setter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
public class SandboxDetailInfo {
  /** 沙箱 ID */
  private Integer id;
  /** 会话 ID */
  private String chatId;
  /** 沙箱名称 */
  private String name;
  /** 沙箱状态 */
  private String status;
  /** 容器 ID */
  private String containerId;
  /** execd 接口地址 */
  private String endpointUrl;
  /** 过期时间 */
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private Date expiresAt;
  /** 创建时间 */
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private Date createdAt;

  /**
   * 检查沙箱状态是否健康
   */
  @JsonIgnore
  public boolean isHealthy() {
    return "running".equals(status) || "active".equals(status);
  }
}
