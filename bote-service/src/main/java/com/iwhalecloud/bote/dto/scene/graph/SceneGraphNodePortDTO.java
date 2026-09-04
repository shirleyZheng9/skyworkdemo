package com.iwhalecloud.bote.dto.scene.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * 场景流程图节点的端点
 *
 * @author bianjp
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SceneGraphNodePortDTO {
  /** ID, 与线条端点的 port 对应 */
  private String id;
  /** 分组 */
  private String group;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SceneGraphNodePortDTO that = (SceneGraphNodePortDTO) o;
    return Objects.equals(id, that.id) && Objects.equals(group, that.group);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, group);
  }
}
