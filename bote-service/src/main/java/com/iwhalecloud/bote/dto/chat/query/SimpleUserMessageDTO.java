package com.iwhalecloud.bote.dto.chat.query;

import com.iwhalecloud.bote.dto.planning.SimplePlanDTO.SimplePlanStepDTO;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author chen.linfa
 * @since 2025-06-25
 */
@Getter
@Setter
@ToString
public class SimpleUserMessageDTO {
  /** 消息内容 */
  private String content;

  /** 用户入参，可传递到智能体 */
  private Map<String, Object> params;

  /** 选择性执行自定义规划策略 */
  private Boolean planable;

  /** 盒子业务信息 */
  private List<SimplePlanStepDTO> busiInfos;
}
