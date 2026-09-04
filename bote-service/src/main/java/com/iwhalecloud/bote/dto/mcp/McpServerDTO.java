package com.iwhalecloud.bote.dto.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.BeyondConsts;
import com.iwhalecloud.bote.dto.beyond.BeyondMqMessageDTO.ResourceDTO;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import com.iwhalecloud.bote.entity.mcp.McpServerEntity;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * mcp DTO
 *
 * @author auto
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class McpServerDTO extends McpServerEntity {
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "创建人图标")
  private String creatorIcon;
  @Schema(description = "创建人编码")
  private String creatorCode;
  @Schema(description = "标签ID列表")
  private List<Long> labelIds;
  @Schema(description = "关联标签，后端保存数据库时使用，前端展示也需要")
  private List<LabelObjectRelDTO> labels;
  @Schema(description = "请求头")
  private List<HeaderItem> headers;
  @Schema(description = "ai门户智能体是否启用")
  private Boolean enable;
  @Schema(description = "BoteClaw空间ID")
  private Long spaceId;

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

  /**
   * 保存 JSON 配置属性
   */
  public void saveJsonConfig() {
    if (CollectionUtils.isNotEmpty(headers)) {
      this.headersJson = JsonUtil.toJsonString(headers);
      Assert.isTrue(this.headersJson.getBytes(StandardCharsets.UTF_8).length < 4000, "请求头内容长度超出限制");
    }
    else {
      this.headersJson = null;
    }
  }

  /**
   * 转换 MCP 对象
   */
  public static McpServerDTO from(ResourceDTO resource) {
    McpServerDTO dto = new McpServerDTO();
    dto.setServerId(resource.getResourceId());
    dto.setServerName(resource.getResourceName());
    dto.setServerDesc(resource.getResourceDesc());
    dto.setServerDetail(resource.getResourceDesc());
    dto.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
    dto.setServerEffect("1");
    dto.setRemark(BaseConsts.SYSTEM_TYPE_BEYOND);

    String serverType = McpConsts.TRANSPORT_SSE;
    String serverIcon = "./images/avatar/img-mcp-sse.png";
    if (BeyondConsts.MCP_TYPE_STREAMABLE.equals(resource.getMcpTransferType())) {
      serverType = McpConsts.TRANSPORT_STREAMABLE;
      serverIcon = "./images/avatar/img-mcp-http.png";
    }
    dto.setServerType(serverType);
    dto.setServerUrl(resource.getMcpServerUrl());
    dto.setServerIcon(serverIcon);

    if (MapUtils.isNotEmpty(resource.getMcpHeader())) {
      List<Map<String, String>> headers = new ArrayList<>();
      for (Entry<String, String> entry : resource.getMcpHeader().entrySet()) {
        headers.add(ImmutableMap.of("name", entry.getKey(), "value", entry.getValue()));
      }
      dto.setHeadersJson(JsonUtil.toJsonString(headers));
    }

    String statusCd = BaseConsts.STATUS_CD_VALID;
    if (Objects.equals(resource.getResourceStatus(), BeyondConsts.STATUS_INVALID)) {
      statusCd = BaseConsts.STATUS_CD_INVALID;
    }
    dto.setStatusCd(statusCd);
    dto.setCatalogItemId(2025051901L);
    return dto;
  }
}
