package com.iwhalecloud.bote.dto.mcp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * MCP 服务简单信息
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SimpleMcpServiceDTO {
  /** 主键 */
  private Long serverId;
  /** 服务名称 */
  private String serverName;
  /** 服务类型 */
  private String serverType;
  /** 服务命令(stdio) */
  private String serverCommand;
  /** 服务参数(stdio) */
  private String serverArgs;
  /** 服务环境变量(stdio) */
  private String serverEnv;
  /** 服务地址(sse/streamable) */
  private String serverUrl;
  /** 鉴权令牌 */
  private String serverToken;
  /** 租户ID */
  private Long tenantId;
  /** 请求头 */
  private List<HeaderItem> headers;
  /** 请求头(JSON), 查询数据库使用 */
  @JsonIgnore
  private String headersJson;
  /** SSE 端点 */
  private String serverEndpoint;

  /**
   * 解析 JSON 配置属性
   */
  public void parseJsonConfig() {
    if (StringUtils.isNotEmpty(headersJson)) {
      this.headers = JsonUtil.parseJsonRequired(headersJson, new TypeReference<>() {
      });
    }
    this.headersJson = null;
  }

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("tenantId", tenantId);
    map.put("serverId", serverId);
    map.put("serverName", serverName);
    map.put("serverArgs", serverArgs);
    map.put("serverType", serverType);
    return map;
  }
}
