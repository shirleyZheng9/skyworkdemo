package com.iwhalecloud.bote.loop.prompt.domain.component.conf;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.dto.tenant.setting.SimpleTenantSettingInfo;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.MessageDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ModelConfigDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDetailDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptTemplateDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.RoleDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.TemplateTypeDTO;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import lombok.RequiredArgsConstructor;

/**
 * Prompt配置提供者 (Spring Boot版本)
 * 迁移对应关系: Go语言modules/prompt/infra/conf.PromptConfigProvider
 * - 功能: 提供Prompt模块的配置管理
 * - 主要方法:
 * * getPromptHubMaxQPSBySpace - 根据空间ID获取最大QPS
 * * getPromptDefaultConfig - 获取Prompt默认配置
 * <p>
 * Java实现说明:
 * - 对应Go的conf.PromptConfigProvider结构体
 * - 使用Spring Boot的配置管理功能
 * - 实现IConfigProvider接口
 * - 支持配置自动注入和热更新
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Spring Boot @Component
 * - Go接口 -> Java接口
 * - Go map -> Java Map
 * - Go配置文件 -> Spring Boot application.yml
 * - Go error返回 -> Java异常处理
 */
@RequiredArgsConstructor
public class PromptConfigProvider implements IConfigProvider {
  private final PromptHubRateLimitConfig rateLimitConfig;
  private final TenantSettingInfoCache tenantSettingInfoCache;

  /**
   * 初始化方法
   * 迁移对应关系: Go语言init函数
   * - 功能: 在Bean创建后执行初始化逻辑
   * - 实现: Spring Boot的@PostConstruct注解
   */
  @PostConstruct
  public void init() {
    // 可以在这里添加初始化逻辑
    // 例如：验证配置、设置默认值等
    if (rateLimitConfig.getDefaultMaxQps() == null) {
      rateLimitConfig.setDefaultMaxQps(500);
    }
  }

  /**
   * 根据空间ID获取最大QPS
   * 迁移对应关系: Go语言PromptConfigProvider.GetPromptHubMaxQPSBySpace
   * - 功能: 根据空间ID获取Prompt Hub的最大QPS限制
   * - 参数: 空间ID
   * - 返回: 最大QPS值
   */
  @Override
  public int getPromptHubMaxQPSBySpace(Long spaceId) {
    if (spaceId == null) {
      throw new IllegalArgumentException("Space ID cannot be null");
    }

    return rateLimitConfig.getMaxQPSForSpace(spaceId);
  }

  /**
   * 获取Prompt默认配置
   * 迁移对应关系: Go语言PromptConfigProvider.GetPromptDefaultConfig
   * - 功能: 获取Prompt的默认配置
   * - 返回: Prompt默认配置
   */
  @Override
  public PromptDetailDTO getPromptDefaultConfig(Long tenantId) {
    SimpleTenantSettingInfo simpleTenantSettingInfo = tenantSettingInfoCache.get(tenantId);
    PromptDetailDTO promptDetailDTO = new PromptDetailDTO();
    ModelConfigDTO config = new ModelConfigDTO();
    if (simpleTenantSettingInfo != null) {
      config.setModelId(simpleTenantSettingInfo.getLargeModelId());
    }
    config.setTemperature(0.8);
    config.setTopP((double) 0);
    config.setMaxTokens(4096);
    promptDetailDTO.setModelConfig(config);
    PromptTemplateDTO promptTemplateDTO = new PromptTemplateDTO();
    promptTemplateDTO.setMessages(Collections.singletonList(new MessageDTO("", RoleDTO.SYSTEM)));
    promptTemplateDTO.setTemplateType(TemplateTypeDTO.NORMAL);
    promptDetailDTO.setPromptTemplate(promptTemplateDTO);
    return promptDetailDTO;
  }
}
