package com.iwhalecloud.bote.dto.bot;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 应用智能体规划策略
 *
 * @author chen.linfa
 * @since 2025-05-26
 */
@Getter
@Setter
@ToString
public class SimpleAgentStrategyDTO {
  /** 策略类型 */
  private String type;

  /** 规划智能体 */
  private SimpleBotSceneDTO planAgent;

  /** 编排的智能体列表 */
  private List<SimpleBotSceneDTO> agents;
}
