package com.iwhalecloud.bote.dto.scene.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景流程图线条的端点
 *
 * @author bianjp
 * @since 2024-09-12
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SceneGraphEndpointDTO {
  /** 节点编码 */
  private String cell;
  /** 端口编码(一个节点连多条线时区分每条线的含义) */
  private String port;
}
