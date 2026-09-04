package com.iwhalecloud.bote.dto.dashboard;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 智能体模式数量查询结果。
 *
 * @author zhengxueli
 * @since 2026-08-28
 */
@Getter
@Setter
@ToString
public class AgentModeCountDTO {

  /** 智能体模式编码，对应 bt_bot_scene.scene_type。 */
  private String code;

  /** 当前模式下的有效智能体数量。 */
  private Long count;
}
