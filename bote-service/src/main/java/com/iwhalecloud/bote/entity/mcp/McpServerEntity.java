package com.iwhalecloud.bote.entity.mcp;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * mcp Entity
 *
 * @author auto
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_MCP_SERVER")
public class McpServerEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long serverId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "SERVER_DESC")
  @Schema(description = "服务描述")
  private String serverDesc;
  @DiffField(name = "SERVER_EFFECT")
  @Schema(description = "服务有效标识: 1 有效, 0 失效")
  private String serverEffect;
  @DiffField(name = "SERVER_TYPE")
  @Schema(description = "服务类型: SSE、STDIO、streamableHttp")
  private String serverType;
  @DiffField(name = "SERVER_COMMAND")
  @Schema(description = "服务命令")
  private String serverCommand;
  @DiffField(name = "SERVER_ARGS")
  @Schema(description = "服务参数")
  private String serverArgs;
  @DiffField(name = "SERVER_ENV")
  @Schema(description = "服务环境变量")
  private String serverEnv;
  @DiffField(name = "SERVER_URL")
  @Schema(description = "服务URL")
  private String serverUrl;
  @DiffField(name = "SERVER_TOKEN")
  @Schema(description = "认证token")
  private String serverToken;
  @DiffField(name = "SERVER_NAME")
  @Schema(description = "服务名称")
  private String serverName;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @DiffField(name = "SERVER_ICON")
  @Schema(description = "服务图标")
  private String serverIcon;
  @DiffField(name = "SERVER_DETAIL")
  @Schema(description = "服务详情")
  private String serverDetail;
  @DiffField(name = "HEADERS_JSON")
  @Schema(description = "请求头(JSON)")
  protected String headersJson;
  @DiffField(name = "SERVER_ENDPOINT")
  @Schema(description = "SSE 端点")
  private String serverEndpoint;
  @DiffField(name = "DATA_FROM")
  @Schema(description = "来源：空为租户来源，10A为通用智能体来源，此时租户id存储的是空间id")
  private String dataFrom;
}
