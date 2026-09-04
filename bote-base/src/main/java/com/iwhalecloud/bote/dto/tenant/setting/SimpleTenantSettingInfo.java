package com.iwhalecloud.bote.dto.tenant.setting;

import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * 租户设置信息
 *
 * @author chen.linfa
 * @since 2025-01-09
 */
@Getter
@Setter
public class SimpleTenantSettingInfo {
  /** 意图设置 */
  private IntentStrategyDTO intentStrategy;
  /** 知识库账号 */
  private String knowledgeUserName;
  /** 知识库账号密码 */
  private String knowledgePassword;
  /** 知识库账号API Key */
  private String knowledgeApiKey;
  /** 默认大模型ID */
  private Long largeModelId;
  /** 是否开启流程日志 */
  private Boolean flowLogEnabled;
  /** 联想术语匹配阈值 */
  private Double suggestionScore;
  /** 联想术语匹配条数 */
  private Integer suggestionLimit;
  /** 知识库信息 */
  private KnowledgeInfoDTO knowledgeInfoDTO;
  /** knowledgeGraph SSO 登录载荷（project / 对接用户） */
  private KnowledgeGraphLoginDTO knowledgeGraphLoginPayload;
  /** 插件市场信息 */
  private TenantPluginHubSettingDTO pluginHub;
  /** 水印设置 */
  private TenantWatermarkSettingDTO watermark;
  /** 安全围栏设置 */
  private TenantSecurityFlowSettingDTO securityFlow;
}
