package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 服务节点差异树
 *
 * @author qian.sisheng
 * @since 2024-10-31
 */
@Getter
@Setter
@ToString
@Schema(description = "服务节点差异树")
public class ServiceNodeDiffTreeDTO {
  @Schema(description = "差异类型", allowableValues = {"added", "deleted", "modified", "equal"})
  private String type;
  @Schema(description = "左边节点（旧版本）")
  private Object left;
  @Schema(description = "右边节点（新版本）")
  private Object right;
  @Schema(description = "子节点差异树列表")
  private List<ServiceNodeDiffTreeDTO> children;

  /**
   * 构造新增节点差异
   */
  public static ServiceNodeDiffTreeDTO ofAdded(Object node) {
    ServiceNodeDiffTreeDTO view = new ServiceNodeDiffTreeDTO();
    view.type = "added";
    view.right = node;
    return view;
  }

  /**
   * 构造删除节点差异
   */
  public static ServiceNodeDiffTreeDTO ofDeleted(Object node) {
    ServiceNodeDiffTreeDTO view = new ServiceNodeDiffTreeDTO();
    view.type = "deleted";
    view.left = node;
    return view;
  }

  /**
   * 构造相等或修改节点差异
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static ServiceNodeDiffTreeDTO of(Object left, Object right, boolean equal) {
    ServiceNodeDiffTreeDTO view = new ServiceNodeDiffTreeDTO();
    view.type = equal ? "equal" : "modified";
    view.left = left;
    view.right = right;
    return view;
  }
}
