package com.iwhalecloud.bote.dto.scene.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.StepType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景流程图节点
 *
 * @author bianjp
 * @since 2024-09-12
 */
@Getter
@Setter
@ToString
public class SceneGraphNodeDTO {
  /** 节点名称 */
  private String nodeName;
  /** 节点编码 */
  private String nodeCode;
  /** 节点类型 */
  private String nodeType;
  /** X 坐标 */
  private Integer x;
  /** Y 坐标 */
  private Integer y;
  /** 子画布宽度（目前仅用于循环节点） */
  private Integer width;
  /** 子画布高度（目前仅用于循环节点） */
  private Integer height;
  /** 子节点编码列表（目前仅用于循环节点） */
  private List<String> childrenCodes;
  /** 节点数据 */
  private Map<String, Object> nodeData;
  /** 端点列表 */
  private List<SceneGraphNodePortDTO> portsItems;
  /** 子节点列表 */
  private List<SceneGraphNodeDTO> childrenNodes;

  /**
   * 是否时对话型专属的节点
   */
  @JsonIgnore
  public boolean isChatOnly() {
    return StepType.REPLY.equals(nodeType) || StepType.PAGE.equals(nodeType) || StepType.PAGE_FUNC.equals(nodeType);
  }

  // 转换流程图时会将节点用作集合的 key, 需要重写 equals, hashCode 方法

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    return Objects.equals(nodeCode, ((SceneGraphNodeDTO) other).nodeCode);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(nodeCode);
  }
}
