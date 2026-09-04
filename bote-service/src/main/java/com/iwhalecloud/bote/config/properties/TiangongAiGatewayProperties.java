package com.iwhalecloud.bote.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 天工 AI 网关 / 模型广场对接配置
 *
 * @author jiangm
 * @since 2026-08-06
 */
@Component
@ConfigurationProperties("bote.tg.ai-gateway")
@Getter
@Setter
@ToString(exclude = "token")
public class TiangongAiGatewayProperties {
  /** aimarket 模型列表 API 根地址，如 https://ai-sit.artifex-cmcc.com.cn */
  private String baseUrl = "";
  /**
   * 额度/AI Key 内部接口根地址，如 https://inner-api-ft.artifex-cmcc.com.cn。
   * 与 aimarket 可能不是同一域名；为空时回退到 {@link #baseUrl}。
   */
  private String quotaBaseUrl = "";
  /** 共享认证 Token（原样写入 Authorization，不要加 Bearer 前缀） */
  private String token = "";
  /** 模型推理访问地址（写入 bt_library_large_model.access_url） */
  private String accessUrl = "";
  /** 分页拉取 aimarket 模型时的每页大小 */
  private int pageSize = 100;
  /**
   * 自动同步节流间隔（秒）。同一 tenantId 在间隔内最多同步一次。
   * queryLargeModelPage / queryLargeModels 触发自动同步时生效。
   */
  private int syncIntervalSeconds = 300;
}
