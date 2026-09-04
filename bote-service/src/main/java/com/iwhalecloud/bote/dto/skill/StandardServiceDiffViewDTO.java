package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 服务差异视图
 *
 * <p>用于表示两个编排服务之间的差异。</p>
 *
 * @author qian.sisheng
 * @since 2024-10-31
 */
@Getter
@Setter
@ToString
@Schema(description = "编排服务差异视图")
public class StandardServiceDiffViewDTO {
  @Schema(description = "流程图")
  private SceneGraphDTO graph;
  @Schema(description = "旧版本服务(差异视图左边)")
  private Object oldNodes;
  @Schema(description = "新版本服务(差异视图右边)")
  private Object nodes;
  @Schema(description = "节点差异树列表")
  private List<ServiceNodeDiffTreeDTO> nodeDifferences;
  @Schema(description = "旧版本游离节点")
  private List<SceneGraphNodeDTO> oldUnUseNodes;
  @Schema(description = "新版本游离节点")
  private List<SceneGraphNodeDTO> unUseNodes;
}
