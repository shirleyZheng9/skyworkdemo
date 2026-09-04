package com.iwhalecloud.bote.dto.scene.graph;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景流程图
 *
 * @author bianjp
 * @since 2024-09-12
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SceneGraphDTO {
  /** 节点列表 */
  private List<SceneGraphNodeDTO> nodes;
  /** 线条列表 */
  private List<SceneGraphEdgeDTO> edges;
}
