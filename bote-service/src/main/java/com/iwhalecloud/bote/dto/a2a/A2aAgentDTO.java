package com.iwhalecloud.bote.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.a2a.A2aAgentEntity;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.a2a.spec.AgentCard;
import io.swagger.v3.oas.annotations.media.Schema;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * A2A 服务传输对象
 *
 * @author bianjp
 * @since 2025-09-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@Schema(description = "A2A 服务传输对象")
public class A2aAgentDTO extends A2aAgentEntity {
  @Schema(description = "智能体卡片")
  private AgentCard agentCard;
  @Schema(description = "智能体图标")
  private String agentIcon;
  @Schema(description = "鉴权配置")
  private A2aAuthConfig authConfig;

  /**
   * 解析 JSON 配置属性
   */
  public void parseJsonConfig() {
    if (StringUtils.isNotEmpty(agentCardJson)) {
      this.agentCard = JsonUtil.parseJsonRequired(agentCardJson, AgentCard.class);
    }
    if (StringUtils.isNotEmpty(authConfigJson)) {
      this.authConfig = JsonUtil.parseJsonRequired(authConfigJson, A2aAuthConfig.class);
    }
    this.agentCardJson = null;
    this.authConfigJson = null;
  }

  /**
   * 保存 JSON 配置属性
   */
  public void saveJsonConfig() {
    this.agentCardJson = JsonUtil.toJsonString(agentCard);
    if (authConfig != null) {
      authConfig.validate();
      this.authConfigJson = JsonUtil.toJsonString(authConfig);
      Assert.isTrue(this.authConfigJson.getBytes(StandardCharsets.UTF_8).length < 4000, "鉴权请求头内容长度超出限制");
    }
    else {
      this.authConfigJson = null;
    }
  }
}
