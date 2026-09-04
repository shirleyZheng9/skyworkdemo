package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单服务模拟用例
 *
 * @author bianjp
 * @since 2024-12-24
 */
@Getter
@Setter
@ToString
public class SimpleServiceMockDTO {
  /** 用例名称 */
  private String mockName;
  /** 条件 */
  private IfCondition condition;
  /** 响应报文 */
  private Object response;
}
