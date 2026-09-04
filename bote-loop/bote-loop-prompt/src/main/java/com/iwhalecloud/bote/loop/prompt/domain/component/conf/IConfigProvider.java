package com.iwhalecloud.bote.loop.prompt.domain.component.conf;

import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDetailDTO;

/**
 * 配置提供者接口
 * 迁移对应关系: Go语言modules/prompt/domain/component/conf.IConfigProvider
 * - 功能: 定义配置提供者的接口规范
 * - 主要方法:
 * * getPromptHubMaxQPSBySpace - 根据空间ID获取最大QPS
 * * getPromptDefaultConfig - 获取Prompt默认配置
 * <p>
 * Java实现说明:
 * - 对应Go的conf.IConfigProvider接口
 * - 使用Java接口定义配置提供规范
 * - 支持配置查询和获取
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go context.Context -> Java方法参数（可选）
 * - Go error返回 -> Java异常处理
 * - Go int64 -> Java Long
 */
public interface IConfigProvider {

  /**
   * 根据空间ID获取最大QPS
   * 迁移对应关系: Go语言IConfigProvider.GetPromptHubMaxQPSBySpace
   * - 功能: 根据空间ID获取Prompt Hub的最大QPS限制
   * - 参数: 空间ID
   * - 返回: 最大QPS值
   * - 异常: 配置加载失败时抛出异常
   */
  int getPromptHubMaxQPSBySpace(Long spaceId);

  /**
   * 获取Prompt默认配置
   * 迁移对应关系: Go语言IConfigProvider.GetPromptDefaultConfig
   * - 功能: 获取Prompt的默认配置
   * - 返回: Prompt默认配置
   * - 异常: 配置加载失败时抛出异常
   */
  PromptDetailDTO getPromptDefaultConfig(Long spaceId);
}
