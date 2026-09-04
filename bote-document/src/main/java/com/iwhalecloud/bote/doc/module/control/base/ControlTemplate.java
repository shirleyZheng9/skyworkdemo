package com.iwhalecloud.bote.doc.module.control.base;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.module.control.base.ControlIdBuilder.ControlId;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder.ControlSubject;
import com.iwhalecloud.bote.doc.module.control.base.exception.UnknownControlTypeException;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.request.ControlRequest;
import com.iwhalecloud.bote.doc.module.control.base.request.ControlRequestFactory;
import com.iwhalecloud.bote.doc.module.control.base.request.NodeControlRequest;
import com.iwhalecloud.bote.doc.module.control.base.request.NodeControlRequestFactory;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRoleManager;
import com.iwhalecloud.bote.doc.module.control.base.role.NodeManagerRole;
import com.iwhalecloud.bote.doc.module.control.service.UserSubjectService;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryPermissionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/**
 * 文档节点权限控制
 *
 * @author Aiqing
 * @since 2025-08-19
 *
 */
@Component
public class ControlTemplate {

  private final UserSubjectService userSubjectService;
  private final DocumentLibraryPermissionService documentLibraryPermissionService;
  private final List<ControlRequestFactory> factories = new ArrayList<>();

  public ControlTemplate(UserSubjectService userSubjectService,
                         DocumentLibraryPermissionService permissionService) {
    this.userSubjectService = userSubjectService;
    this.documentLibraryPermissionService = permissionService;
    this.factories.add(new NodeControlRequestFactory());
  }


  /**
   * 查询某个用户单个文档节点的权限集
   *
   * @param userId 用户ID
   * @param nodeId 节点ID
   * @return control role
   */
  public ControlRole fetchNodeRole(String libraryId, Long userId, String nodeId) {
    ControlRoleDict controlRoleDict = execute(SubjectBuilder.userId(userId), ControlIdBuilder.nodeId(nodeId, libraryId));
    if (controlRoleDict.isEmpty()) {
      return ControlRoleManager.parseNodeRole(DocRoleEnum.ANONYMOUS.getCode());
    }
    return controlRoleDict.get(nodeId);
  }

  public ControlRoleDict fetchNodeRole(String libraryId, Long userId, List<String> nodeIds) {
    return execute(SubjectBuilder.userId(userId), ControlIdBuilder.nodeIds(nodeIds, libraryId));
  }

  public ControlRoleDict fetchNodeTreeNode(String libraryId, Long userId, List<String> nodeIds) {
    return execute(SubjectBuilder.userId(userId), ControlIdBuilder.nodeIds(nodeIds, libraryId),
      ControlRequestOption.create().setNodeTreeRoleBuilding(true));
  }

  /**
   * 查询分享的文档节点权限集
   *
   * @param userId member id
   * @param nodeIds node id list
   * @return control role dict
   */
  public ControlRoleDict fetchShareNodeTree(String libraryId, Long userId, List<String> nodeIds) {
    return execute(SubjectBuilder.userId(userId), ControlIdBuilder.nodeIds(nodeIds, libraryId),
      ControlRequestOption.create().setNodeTreeRoleBuilding(true)
        .setShareNodeTreeRoleBuilding(true));
  }

  public ControlRoleDict fetchInternalNodeRole(String libraryId, Long userId, List<String> nodeIds) {
    return execute(SubjectBuilder.userId(userId), ControlIdBuilder.nodeIds(nodeIds, libraryId),
      ControlRequestOption.create().setInternalBuilding(true));
  }

  public void checkNodePermission(String libraryId,
                                  Long userId,
                                  String nodeId,
                                  NodePermission permission,
                                  Consumer<Boolean> resultCallback) {
    ControlRole controlRole = fetchNodeRole(libraryId, userId, nodeId);
    resultCallback.accept(controlRole.hasPermission(permission));
  }

  /**
   * 判断用户是否拥有节点的某个权限
   *
   * @param userId 用户ID
   * @param nodeId 文档节点ID
   * @param permission 节点权限
   * @return true | false
   */
  public boolean hasNodePermission(String libraryId, Long userId, String nodeId, NodePermission permission) {
    ControlRoleDict controlRoleDict = execute(SubjectBuilder.userId(userId), ControlIdBuilder.nodeId(nodeId, libraryId));
    if (controlRoleDict.isEmpty()) {
      return false;
    }
    ControlRole controlRole = controlRoleDict.get(nodeId);
    if (controlRole == null) {
      return false;
    }
    return controlRole.hasPermission(permission);
  }

  protected ControlRoleDict execute(ControlSubject subject, ControlId controlId) {
    return execute(subject, controlId, ControlRequestOption.create().setNodeTreeRoleBuilding(false));
  }

  protected ControlRoleDict doExecute(ControlSubject subject, ControlId controlId,
                                      ControlRequestWrapper requestWrapper) {
    // 如果校验对象是文档库的管理者
    if (isDocumentLibraryAdmin(subject.getSubjectId(), controlId)) {
      ControlRoleDict controlRoleDict = ControlRoleDict.create();
      ControlRole topRole = this.getTopRole(controlId.getControlType());
      controlId.toRealIdList().forEach(id -> controlRoleDict.put(id, topRole));
      return controlRoleDict;
    }
    // 查询用户关联的所有实体
    List<ControlSubject> fromUnitIds = userSubjectService.queryUserAllRelUnitId(subject.getSubjectId());
    return doExecute(fromUnitIds, controlId, requestWrapper);
  }

  protected ControlRoleDict execute(ControlSubject subject, ControlId controlId,
                                    ControlRequestOption requestOption) {
    return doExecute(subject, controlId, new DefaultControlRequestWrapper(requestOption));
  }

  protected ControlRoleDict doExecute(List<ControlSubject> controlSubjects, ControlId controlId,
                                      ControlRequestWrapper requestWrapper) {
    ControlRequest request = createRequest(controlSubjects, controlId);
    if (requestWrapper != null) {
      requestWrapper.doWrapper(request);
    }
    return request.execute();
  }

  /**
   * 查询是否是文档库的管理者
   *
   * @param userId 用户ID
   * @param controlId 权限控制对象封装
   */
  private boolean isDocumentLibraryAdmin(Long userId, ControlId controlId) {
    return documentLibraryPermissionService.isLibraryManager(controlId.getLibraryId(), userId);
  }

  private ControlRole getTopRole(ControlType controlType) {
    if (Objects.requireNonNull(controlType) == ControlType.NODE) {
      return new NodeManagerRole(true, true);
    }
    return null;
  }

  private ControlRequest createRequest(List<ControlSubject> subjects, ControlId controlId) {
    for (ControlRequestFactory factory : this.factories) {
      if (factory.getControlType().equals(controlId.getControlType())) {
        return factory.create(subjects, controlId.getControlIds());
      }
    }
    // Unknown control type
    throw new UnknownControlTypeException(controlId.getControlType());
  }

  private record DefaultControlRequestWrapper(ControlRequestOption requestOption) implements ControlRequestWrapper {

    @Override
      public void doWrapper(ControlRequest request) {
        if (this.requestOption != null) {
          if (request instanceof NodeControlRequest) {
            ((NodeControlRequest) request).setShareTreeBuilding(
              this.requestOption.shareNodeTreeRoleBuilding);
          }
        }
      }
    }
}
