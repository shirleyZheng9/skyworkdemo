package com.iwhalecloud.bote.doc.module.control.service;

import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.consts.SubjectTypeEnum;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder.ControlSubject;
import com.iwhalecloud.bote.doc.module.control.mapper.DocumentNodeMapper;
import com.iwhalecloud.bote.doc.module.control.model.ControlRoleInfo;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.control.model.SimpleNodeInfo;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentPermissionService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * 文档节点的角色权限service
 *
 * @author Aiqing
 * @since 2025/8/21
 */
@Service
@RequiredArgsConstructor
public class NodeRoleService {

  private final DocumentNodeMapper documentNodeMapper;
  private final DocumentNodeService documentNodeService;
  private final IDocumentPermissionService documentPermissionService;

  private static @NotNull Map<ControlSubject, String> buildSubjectRoleMap(Long userId, List<ControlRoleInfo> controlRoleInfos) {
    Map<ControlSubject, String> unitRoleMap = new HashMap<>(controlRoleInfos.size() + 1);
    for (ControlRoleInfo controlRoleInfo : controlRoleInfos) {
      Long subjectId = controlRoleInfo.getSubjectId();
      String subjectType = controlRoleInfo.getSubjectType();
      // 跳过操作者原有指定权限和原所有者
      if ((subjectId.equals(userId) && Objects.equals(subjectType, SubjectTypeEnum.USER.getCode()))
        || controlRoleInfo.getRole().equals(DocRoleEnum.DOC_OWNER.getCode())) {
        continue;
      }
      unitRoleMap.put(SubjectBuilder.userId(controlRoleInfo.getSubjectId()), controlRoleInfo.getRole());
    }
    // 设置操作者为所有者角色
    unitRoleMap.put(SubjectBuilder.userId(userId), DocRoleEnum.DOC_OWNER.getCode());
    return unitRoleMap;
  }

  public List<SimpleNodeInfo> getNodeInfoWithPermissionStatus(List<String> nodeIds) {
    List<NodeBaseInfoDTO> parentNodes = documentNodeService.getParentPathNodes(nodeIds, true);
    if (CollectionUtils.isEmpty(parentNodes)) {
      return new ArrayList<>();
    }
    return parentNodes.stream()
      .map(item -> {
        SimpleNodeInfo nodeInfo = new SimpleNodeInfo();
        nodeInfo.setNodeId(item.getNodeId());
        nodeInfo.setParentId(item.getParentId());
        nodeInfo.setNodeType(item.getNodeType());
        nodeInfo.setCreatorId(item.getCreatorId());
        nodeInfo.setPermissionMode(item.getPermissionMode());
        return nodeInfo;
      })
      .toList();
  }

  /**
   * 复制继承的权限， 转为独立权限配置，防止删除后再恢复，权限继承链断裂
   *
   * @param userId 对象用户ID
   * @param documentIdList 文档ID集合
   */
  public void copyExtendNodeRoleIfExtend(Long userId, List<String> documentIdList) {
    for (String nodeId : documentIdList) {
      SimpleNodeInfo simpleNodeInfo = documentNodeMapper.selectNodeInfoWithPermissionStatus(nodeId);
      if (!simpleNodeInfo.isExtend()) {
        // 非继承权限跳过
        continue;
      }
      // 从继承节点拷贝非owner的授权
      String extendNodeId = this.getNodeExtendNodeId(nodeId);
      if (extendNodeId == null) {
        continue;
      }
      List<ControlRoleInfo> controlRoleInfos =
        documentPermissionService.selectControlDocumentRoleInfoByControlIds(Collections.singletonList(extendNodeId));
      Map<ControlSubject, String> subjectRoleMap = buildSubjectRoleMap(userId, controlRoleInfos);
      // 创建新的授权
      documentPermissionService.addDocumentControlPermission(userId, nodeId, subjectRoleMap);
    }
  }

  public String getNodeExtendNodeId(String nodeId) {
    List<String> nodeIds = new ArrayList<>();
    nodeIds.add(nodeId);
    String parentId = nodeId;
    while (true) {
      SimpleNodeInfo node = documentNodeMapper.selectNodeInfoWithPermissionStatus(parentId);
      if (node == null || Objects.equals(node.getNodeType(), DocumentTypeEnum.ROOT.getCode())) {
        return null;
      }
      if (!node.isExtend()) {
        return node.getNodeId();
      }
      parentId = node.getParentId();
      // 防止死循环
      if (nodeIds.contains(parentId)) {
        throw new BssException("数据异常");
      }
      nodeIds.add(parentId);
    }
  }
}
