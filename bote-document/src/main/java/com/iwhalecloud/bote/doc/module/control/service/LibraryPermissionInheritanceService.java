package com.iwhalecloud.bote.doc.module.control.service;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.cache.DocumentLibraryCache;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.consts.SubjectTypeEnum;
import com.iwhalecloud.bote.doc.consts.VisibilityScopeEnum;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryPermissionDTO;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder.ControlSubject;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.control.base.role.LibraryInheritedRoleFactory;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryPermissionService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 文档库权限继承服务
 *
 * @author Aiqing
 * @since 2025-08-21
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class LibraryPermissionInheritanceService {

  private static final Logger logger = LoggerFactory.getLogger(LibraryPermissionInheritanceService.class);

  private final DocumentLibraryPermissionService documentLibraryPermissionService;
  private final IDocumentService documentService;
  private final DocumentLibraryCache documentLibraryCache;

  /**
   * 批量获取节点的文档库继承权限
   *
   * @param nodeIds 节点ID列表
   * @param controlSubjects 控制主体列表
   * @return 节点ID -> 继承权限角色的映射
   */
  public Map<String, ControlRole> batchGetLibraryInheritedRoles(List<String> nodeIds,
                                                                List<ControlSubject> controlSubjects) {
    if (CollectionUtils.isEmpty(nodeIds) || CollectionUtils.isEmpty(controlSubjects)) {
      return Collections.emptyMap();
    }

    try {
      // 获取节点对应的文档库ID
      Map<String, String> nodeToLibraryMap = documentService.findDocumentLibraryMapping(nodeIds);
      if (nodeToLibraryMap.isEmpty()) {
        logger.warn("未找到节点对应的文档库映射，节点数量: {}", nodeIds.size());
        return Collections.emptyMap();
      }

      // 按文档库分组节点
      Map<String, List<String>> libraryToNodesMap = nodeToLibraryMap.entrySet().stream()
        .collect(Collectors.groupingBy(Map.Entry::getValue,
          Collectors.mapping(Map.Entry::getKey, Collectors.toList())
        ));

      Map<String, ControlRole> result = new HashMap<>();

      // 为每个文档库批量查询权限
      for (Map.Entry<String, List<String>> entry : libraryToNodesMap.entrySet()) {
        String libraryId = entry.getKey();
        List<String> nodesInLibrary = entry.getValue();

        // 获取用户在该文档库的最高权限
        String highestRole = getHighestLibraryRole(libraryId, controlSubjects);
        if (highestRole != null) {
          ControlRole inheritedRole = LibraryInheritedRoleFactory.createInheritedRole(highestRole);
          logger.debug("文档库 {} 的 {} 个节点继承权限: {}", libraryId, nodesInLibrary.size(), highestRole);
          for (String nodeId : nodesInLibrary) {
            result.put(nodeId, inheritedRole);
          }
        }
      }
      return result;
    }
    catch (Exception e) {
      logger.error("批量获取文档库继承权限失败", e);
      return Collections.emptyMap();
    }
  }

  /**
   * 获取用户在文档库中的最高权限
   */
  private String getHighestLibraryRole(String libraryId, List<ControlSubject> controlSubjects) {
    try {
      DocumentLibraryDTO documentLibraryDTO = documentLibraryCache.getByLibraryId(libraryId);
      if (documentLibraryDTO == null) {
        logger.warn("文档库查询不存在, libraryId:{}", libraryId);
        return null;
      }

      // 检查特殊权限（所有者、超管）
      String specialRole = checkSpecialPermissions(documentLibraryDTO, controlSubjects);
      if (specialRole != null) {
        return specialRole;
      }

      // 查询用户权限并获取最高权限
      String highestRole = findHighestPermissionFromUserRoles(libraryId, controlSubjects);

      // 处理公共文档库默认权限
      return handlePublicLibraryDefaultPermission(highestRole, documentLibraryDTO.getVisibilityScope());
    }
    catch (Exception e) {
      logger.error("获取文档库最高权限失败，libraryId: {}", libraryId, e);
      return null;
    }
  }

  /**
   * 检查特殊权限（文档库所有者、平台超管）
   */
  private String checkSpecialPermissions(DocumentLibraryDTO documentLibraryDTO, List<ControlSubject> controlSubjects) {
    Long ownerId = documentLibraryDTO.getOwnerId();

    // 检查是否为文档库所有者
    if (isLibraryOwner(controlSubjects, ownerId)) {
      return LibraryRoleEnum.OWNER.getCode();
    }

    // 检查是否为平台超管
    if (isSuperAdmin(controlSubjects)) {
      return LibraryRoleEnum.MANAGE.getCode();
    }

    return null;
  }

  /**
   * 检查是否为文档库所有者
   */
  private boolean isLibraryOwner(List<ControlSubject> controlSubjects, Long ownerId) {
    return controlSubjects.stream().anyMatch(item ->
      Objects.equals(item.getSubjectType(), SubjectTypeEnum.USER) &&
        Objects.equals(item.getSubjectId(), ownerId)
    );
  }

  /**
   * 检查是否为平台超管
   */
  private boolean isSuperAdmin(List<ControlSubject> controlSubjects) {
    return controlSubjects.stream().anyMatch(item ->
      Objects.equals(item.getSubjectType(), SubjectTypeEnum.USER) && SessionUtil.isSuperAdmin(item.getSubjectId())
    );
  }

  /**
   * 从用户角色中查找最高权限
   */
  private String findHighestPermissionFromUserRoles(String libraryId, List<ControlSubject> controlSubjects) {
    String highestRole = null;
    int highestLevel = Integer.MAX_VALUE;

    // 按主体类型分组查询权限
    Map<String, List<Long>> subjectTypeMap = groupSubjectsByType(controlSubjects);

    for (Map.Entry<String, List<Long>> entry : subjectTypeMap.entrySet()) {
      String subjectType = entry.getKey();
      List<Long> subjectIds = entry.getValue();

      List<LibraryPermissionDTO> permissions = documentLibraryPermissionService
        .querySubjectLibraryPermissionBatch(libraryId, subjectIds, subjectType);

      // 更新最高权限
      for (LibraryPermissionDTO permission : permissions) {
        String roleCode = permission.getPermissionType();
        int level = getRoleLevel(roleCode);
        if (level < highestLevel) {
          highestLevel = level;
          highestRole = roleCode;
        }
      }
    }

    return highestRole;
  }

  /**
   * 按主体类型分组
   */
  private Map<String, List<Long>> groupSubjectsByType(List<ControlSubject> controlSubjects) {
    return controlSubjects.stream()
      .collect(Collectors.groupingBy(
        item -> item.getSubjectType().getCode(),
        Collectors.mapping(ControlSubject::getSubjectId, Collectors.toList())
      ));
  }

  /**
   * 处理公共文档库默认权限
   */
  private String handlePublicLibraryDefaultPermission(String highestRole, String visibilityScope) {
    if (StringUtils.isBlank(highestRole) && VisibilityScopeEnum.PUBLIC.getCode().equalsIgnoreCase(visibilityScope)) {
      // 公共文档库统一赋予默认权限
      return DocumentPermConsts.PUBLIC_LIBRARY_DEFAULT_ROLE.getCode();
    }
    return highestRole;
  }

  /**
   * 获取权限级别（数字越小权限越高）
   */
  private int getRoleLevel(String roleCode) {
    for (LibraryRoleEnum role : LibraryRoleEnum.values()) {
      if (role.getCode().equals(roleCode)) {
        return role.getLevel();
      }
    }
    return 99; // 未知权限
  }
}
