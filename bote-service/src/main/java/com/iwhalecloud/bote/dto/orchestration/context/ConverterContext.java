package com.iwhalecloud.bote.dto.orchestration.context;

import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphEdgeDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.helper.SceneGraphValidator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

/**
 * 场景转换器上下文
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class ConverterContext {
  /** 是否是单节点调试 */
  private final boolean singleNodeDebug;
  /** 租户 ID */
  private final Long tenantId;
  /** 开始节点 */
  private SceneGraphNodeDTO startNode;
  /** 目标节点映射, key 为节点编码 */
  private final Map<SceneGraphNodeDTO, List<Pair<String, SceneGraphNodeDTO>>> targetEdgesMap = new HashMap<>();
  /** 有多个源节点的节点编码集合 */
  @Getter(AccessLevel.NONE)
  private final Set<String> multiSourceNodes = new HashSet<>();

  public ConverterContext(Long tenantId, SceneGraphDTO graph) {
    this(tenantId, graph, false);
  }

  public ConverterContext(Long tenantId, SceneGraphDTO graph, boolean singleNodeDebug) {
    Assert.notNull(graph, "graph 不能为空");
    this.tenantId = tenantId;
    this.startNode = SceneGraphValidator.validateBasic(graph);
    this.singleNodeDebug = singleNodeDebug;

    List<SceneGraphNodeDTO> nodes = graph.getNodes();
    List<SceneGraphEdgeDTO> edges = graph.getEdges();
    Map<String, SceneGraphNodeDTO> nodeMap = nodes.stream().collect(Collectors.toMap(SceneGraphNodeDTO::getNodeCode, Function.identity()));

    // 构造节点的目标线条映射
    for (SceneGraphNodeDTO node : nodes) {
      String nodeCode = node.getNodeCode();
      targetEdgesMap.put(node, edges.stream()
        .filter(e -> nodeCode.equals(e.getSource().getCell()))
        .map(e -> {
          SceneGraphNodeDTO targetNode = nodeMap.get(e.getTarget().getCell());
          Assert.notNull(node, () -> "节点不存在: " + e.getTarget().getCell());
          return Pair.of(e.getSource().getPort(), targetNode);
        })
        .collect(Collectors.toList()));
    }

    // 校验线条
    Assert.isTrue(edges.stream().noneMatch(e -> Objects.equals(e.getSource().getCell(), e.getTarget().getCell())),
      "节点的连线不允许指向自身");
    SceneGraphValidator.validateEdges(startNode, nodes, targetEdgesMap);
  }

  /**
   * 获取节点的目标线条列表，返回源节点端口和目标节点对象
   */
  public List<Pair<String, SceneGraphNodeDTO>> getTargetEdges(SceneGraphNodeDTO node) {
    return targetEdgesMap.get(node);
  }

}
