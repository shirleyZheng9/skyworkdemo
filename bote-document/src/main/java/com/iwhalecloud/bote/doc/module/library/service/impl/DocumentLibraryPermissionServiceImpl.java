package com.iwhalecloud.bote.doc.module.library.service.impl;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.consts.SubjectTypeEnum;
import com.iwhalecloud.bote.doc.consts.VisibilityScopeEnum;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryPermissionDTO;
import com.iwhalecloud.bote.doc.module.library.entity.DocumentLibraryEntity;
import com.iwhalecloud.bote.doc.module.library.mapper.DocumentLibraryMapper;
import com.iwhalecloud.bote.doc.module.library.mapper.LibraryPermissionMapper;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryPermissionService;
import com.iwhalecloud.bote.doc.module.user.service.IDcOrgService;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * 文档库权限service
 *
 * @author Aiqing
 * @since 2025/8/19
 */
@Service
@RequiredArgsConstructor
public class DocumentLibraryPermissionServiceImpl implements DocumentLibraryPermissionService {

  private final LibraryPermissionMapper libraryPermissionMapper;
  private final DocumentLibraryMapper documentLibraryMapper;
  private final IDcOrgService dcOrgService;

  @Override
  public boolean checkLibraryEditPermission(String libraryId, Long userId) {
    LibraryRoleEnum libraryRoleEnum = queryUserLibraryMaxRole(libraryId, userId);
    return libraryRoleEnum.getLevel() <= LibraryRoleEnum.EDIT.getLevel();
  }

  @Override
  public boolean checkLibraryAccessPermission(String libraryId, Long userId) {
    LibraryRoleEnum libraryRoleEnum = queryUserLibraryMaxRole(libraryId, userId);
    return libraryRoleEnum.getLevel() < LibraryRoleEnum.ANONYMOUS.getLevel();
  }

  @Override
  public boolean isLibraryManager(String libraryId, Long userId) {
    LibraryRoleEnum libraryRoleEnum = queryUserLibraryMaxRole(libraryId, userId);
    return libraryRoleEnum.getLevel() <= LibraryRoleEnum.MANAGE.getLevel();
  }

  @Override
  public List<LibraryPermissionDTO> selectByLibraryId(String libraryId) {
    return libraryPermissionMapper.selectByLibraryId(libraryId);
  }

  @Override
  public LibraryRoleEnum queryUserLibraryMaxRole(String libraryId, Long userId) {
    DocumentLibraryEntity libraryEntity = documentLibraryMapper.selectByLibrary(libraryId);
    if (libraryEntity == null) {
      return LibraryRoleEnum.ANONYMOUS;
    }
    Long ownerId = libraryEntity.getOwnerId();
    if (ownerId != null && Objects.equals(ownerId, userId)) {
      return LibraryRoleEnum.OWNER;
    }
    // 超管权限处理
    boolean superAdmin = SessionUtil.isSuperAdmin(userId);
    if (superAdmin) {
      return LibraryRoleEnum.MANAGE;
    }

    List<String> userPermRoles = libraryPermissionMapper.querySubjectLibraryPermissionTypesBatch(libraryId,
      Collections.singletonList(userId), SubjectTypeEnum.USER.getCode());

    if (userPermRoles.contains(LibraryRoleEnum.MANAGE.getCode())) {
      return LibraryRoleEnum.MANAGE;
    }
    Set<String> allRoles = new HashSet<>(userPermRoles);
    List<Long> orgIdList = dcOrgService.queryUserOrgIdList(userId);
    if (CollectionUtils.isNotEmpty(orgIdList)) {
      List<String> orgPermRoles = libraryPermissionMapper.querySubjectLibraryPermissionTypesBatch(libraryId,
        orgIdList, SubjectTypeEnum.ORG.getCode());
      allRoles.addAll(orgPermRoles);
    }
    // 填充公共文档库的权限
    if (VisibilityScopeEnum.PUBLIC.getCode().equalsIgnoreCase(libraryEntity.getVisibilityScope())) {
      allRoles.add(DocumentPermConsts.PUBLIC_LIBRARY_DEFAULT_ROLE.getCode());
    }

    if (CollectionUtils.isEmpty(allRoles)) {
      return LibraryRoleEnum.ANONYMOUS;
    }
    return allRoles.stream()
      .map(LibraryRoleEnum::getByCode)
      .min(Comparator.comparingInt(LibraryRoleEnum::getLevel))
      .orElse(LibraryRoleEnum.ANONYMOUS);
  }

  @Override
  public List<LibraryPermissionDTO> querySubjectLibraryPermissionBatch(String libraryId, List<Long> subjectIdList, String subjectType) {
    if (CollectionUtils.isEmpty(subjectIdList)) {
      return Collections.emptyList();
    }
    return libraryPermissionMapper.querySubjectLibraryPermissionBatch(libraryId, subjectIdList, subjectType);
  }
}
