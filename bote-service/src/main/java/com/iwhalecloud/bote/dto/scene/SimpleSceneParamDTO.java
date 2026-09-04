package com.iwhalecloud.bote.dto.scene;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景变量
 *
 * @author chen.linfa
 * @since 2024-10-08
 */
@Getter
@Setter
@ToString
public class SimpleSceneParamDTO {
  /** 场景 ID **/
  private Long sceneId;
  /** 场景变量 */
  private String variableJson;
  /** 租户 ID */
  private Long tenantId;
}
