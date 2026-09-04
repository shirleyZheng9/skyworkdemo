package com.iwhalecloud.bote.service.a2a;

import com.iwhalecloud.bote.dto.a2a.PublishA2aAgentRequest;
import com.iwhalecloud.bote.dto.a2a.UnpublishA2aAgentRequest;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.a2a.spec.AgentCard;
import org.springframework.lang.Nullable;

/**
 * A2A 开放接口
 *
 * @author bianjp
 * @since 2025-09-09
 */
public interface IA2aOpenApiService {

  /**
   * 发布 A2A 智能体
   */
  ResultVO<Void> publishAgent(PublishA2aAgentRequest request, String apiKey);

  /**
   * 取消发布 A2A 智能体
   */
  ResultVO<Void> unpublishAgent(UnpublishA2aAgentRequest request, String apiKey);

  /**
   * 获取智能体卡片
   */
  @Nullable
  AgentCard getAgentCard(Long tenantId, Long agentId);

}
