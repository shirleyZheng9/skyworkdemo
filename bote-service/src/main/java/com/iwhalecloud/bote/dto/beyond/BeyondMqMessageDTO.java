package com.iwhalecloud.bote.dto.beyond;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应 MQ 消息对象
 *
 * @author chen.linfa
 * @since 2025-12-04
 */
@Getter
@Setter
@ToString
public class BeyondMqMessageDTO {
  /** 资源 */
  private PayloadDTO payload;

  /** 元数据 */
  private Map<String, Object> metadata;

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PayloadDTO {
    private ResourceDTO resource;
  }

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ResourceDTO {
    /** 资源 ID */
    private Long resourceId;
    /** 资源名称 */
    private String resourceName;
    /** 资源编码 */
    private String resourceCode;
    /** 资源描述 */
    private String resourceDesc;
    /** 资源类型 */
    private String resourceBizType;
    /** 资源状态 */
    private Integer resourceStatus;
    /** MCP 服务链接 */
    private String mcpServerUrl;
    /** MCP 服务类型 */
    private String mcpTransferType;
    /** MCP 头部参数 */
    private Map<String, String> mcpHeader;
    /** 工具集头部参数 */
    private Map<String, String> headers;
    /** 工具集数据 */
    private List<PluginMachineInfo> pluginMachineInfo;
  }

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PluginMachineInfo {
    private PluginMachine pluginMachine;

    private OpenAPI pluginMachineOpenAPI;
  }

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PluginMachine {
    /** 工具 ID */
    private Long pluginMachineId;
    /** 工具名称 */
    private String machineName;
    /** 工具编码 */
    private String machineCode;
  }
}
