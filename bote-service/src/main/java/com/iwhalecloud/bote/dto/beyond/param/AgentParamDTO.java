package com.iwhalecloud.bote.dto.beyond.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 智能体参数DTO
 *
 * @author lizuyin
 * @since 2025-07-21
 */
@Getter
@Setter
@ToString
public class AgentParamDTO {
  /** 智能体类型 */
  private String agentType;
  /** 数字员工配置 */
  private String prologue;
  /** 智能体开发类型 */
  private String agentDevType;
  /** 百应调用头部参数 */
  private String agentSseHead;
  /** agent的对话地址 */
  private String agentSseUrlOri;
  /** 页面对接地址 */
  private String agentWebUrlOri;
  /** 管理页面地址 */
  private String agentAdminUrlOri;
  /** 数字员工引用的外系统资源id */
  private List<Long> relIds;
}
