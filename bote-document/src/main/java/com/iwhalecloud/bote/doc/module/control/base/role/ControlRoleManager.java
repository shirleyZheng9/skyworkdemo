package com.iwhalecloud.bote.doc.module.control.base.role;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

/**
 * 角色管理
 */
public final class ControlRoleManager {
  private ControlRoleManager() {
  }

  private static final Map<String, ControlRole> NODE_CONTROL_ROLE_MAP;

  static {
    // 文档节点的权限集合
    NODE_CONTROL_ROLE_MAP = new HashMap<>(16);
    registerNodeRole(DocRoleEnum.ANONYMOUS.getCode(), new NodeAnonymousRole());
    registerNodeRole(DocRoleEnum.DOC_READ.getCode(), new NodeReaderRole());
    registerNodeRole(DocRoleEnum.DOWNLOAD.getCode(), new NodeDownloaderRole());
    registerNodeRole(DocRoleEnum.DOC_CORRECTION.getCode(), new NodeCorrectionRole());
    registerNodeRole(DocRoleEnum.DOC_EDIT.getCode(), new NodeEditorRole());
    registerNodeRole(DocRoleEnum.DOC_MANAGE.getCode(), new NodeManagerRole());
    registerNodeRole(DocRoleEnum.DOC_OWNER.getCode(), new NodeOwnerRole());
  }

  private static void registerNodeRole(String name, ControlRole role) {
    if (name != null) {
      NODE_CONTROL_ROLE_MAP.put(name, role);
    }
  }

  public static ControlRole parseNodeRole(String roleCode) {
    Assert.isTrue(NODE_CONTROL_ROLE_MAP.containsKey(roleCode), "node role is not exist");
    return NODE_CONTROL_ROLE_MAP.get(roleCode);
  }

  /**
   * parse and sort node role.
   *
   * @param roleCodes role codes
   * @return sorted list
   */
  public static List<ControlRole> parseAndSortNodeRole(Collection<String> roleCodes) {
    List<ControlRole> sortedList = roleCodes.stream().reduce(new ArrayList<>(),
      (controlRoles, item) -> {
        controlRoles.add(parseNodeRole(item));
        return controlRoles;
      },
      (controlRoles, childRoles) -> {
        controlRoles.addAll(childRoles);
        return controlRoles;
      });
    Collections.sort(sortedList);
    return sortedList;
  }

  public static ControlRole getTopNodeRole(Collection<String> roleCodes) {
    List<ControlRole> roles = parseAndSortNodeRole(roleCodes);
    return roles.get(roles.size() - 1);
  }
}
