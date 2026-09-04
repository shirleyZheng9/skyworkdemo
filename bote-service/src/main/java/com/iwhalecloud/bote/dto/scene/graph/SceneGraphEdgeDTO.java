package com.iwhalecloud.bote.dto.scene.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景流程图线条
 *
 * @author bianjp
 * @since 2024-09-12
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SceneGraphEdgeDTO {
  /** 源端点 */
  private SceneGraphEndpointDTO source;
  /** 目标端点 */
  private SceneGraphEndpointDTO target;
}
