package com.iwhalecloud.bote.doc.module.library.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.cache.PinnedLibraryCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.consts.PermissionActionEnum;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.consts.SettingsActionEnum;
import com.iwhalecloud.bote.doc.consts.SubjectTypeEnum;
import com.iwhalecloud.bote.doc.consts.TargetTypeEnum;
import com.iwhalecloud.bote.doc.consts.VisibilityScopeEnum;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedLibraryDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UserInfo;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryDetailDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryDetailDTO.MemberInfo;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryListDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryPageQueryParam;
import com.iwhalecloud.bote.doc.module.person.mapper.FavoriteMapper;
import com.iwhalecloud.bote.doc.module.control.service.DocumentNodeService;
import com.iwhalecloud.bote.doc.module.document.entity.LibraryPermissionEntity;
import com.iwhalecloud.bote.doc.module.document.service.DocumentPathEventPublisher;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocumentLibraryInitServiceHelper;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.dto.ExitShareRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.ExitShareResponseDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryBatchPermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryBatchPermissionRequestDTO.PermissionMemberDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryPermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryPermissionRequestDTO.LibraryPermissionDataDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibrarySettingsRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibrarySettingsRequestDTO.LibrarySettingsDataDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibrarySettingsResponseDTO;
import com.iwhalecloud.bote.doc.module.library.entity.DocumentLibraryEntity;
import com.iwhalecloud.bote.doc.module.library.mapper.DocumentLibraryMapper;
import com.iwhalecloud.bote.doc.module.library.mapper.LibraryPermissionMapper;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryService;
import com.iwhalecloud.bote.doc.module.user.service.IDcOrgService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.doc.common.utils.DcIdUtils;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.BeanUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 文档库服务实现 负责文档库的创建、查询、分页等业务逻辑，并补充用户/组织等扩展信息。
 *
 * @author auto
 * @since 2025-08-13
 */
@Service
@RequiredArgsConstructor
public class DocumentLibraryServiceImpl implements DocumentLibraryService {

  private final DocumentLibraryMapper documentLibraryMapper;

  private final IDcUserService dcUserService;

  private final IDcOrgService dcOrgService;

  private final LibraryPermissionMapper libraryPermissionMapper;

  private final FavoriteMapper favoriteMapper;

  private final PinnedLibraryCache pinnedLibraryCache;

  /**
   * 根据文档库ID查询名称
   *
   * @param libraryId 文档库 ID
   * @return 文档库名称
   */
  @Override
  public String getNameById(String libraryId) {
    return documentLibraryMapper.selectNameById(libraryId);
  }

  /**
   * 根据文档库ID查询基本信息
   *
   * @param libraryId 文档库 ID
   * @return 文档库信息
   */
  @Override
  public DocumentLibraryDTO findByLibraryId(String libraryId) {
    DocumentLibraryEntity documentLibraryEntity = documentLibraryMapper.selectByLibrary(libraryId);
    if (documentLibraryEntity == null) {
      return null;
    }
    return BeanUtil.copy(documentLibraryEntity, DocumentLibraryDTO.class);
  }

  @Override
  public DocumentLibraryDTO findByLibraryIdAndSpaceId(String libraryId, Long spaceId, Long tenantId) {
    DocumentLibraryEntity documentLibraryEntity = documentLibraryMapper.selectByLibraryAndSpaceId(libraryId, spaceId, tenantId);
    if (documentLibraryEntity == null) {
      return null;
    }
    return BeanUtil.copy(documentLibraryEntity, DocumentLibraryDTO.class);
  }

  @Override
  public DocumentLibraryDTO findByLibraryIdAndTenantId(String libraryId, Long tenantId, String platform) {
    DocumentLibraryEntity documentLibraryEntity = documentLibraryMapper.selectByLibraryAndTenantId(libraryId, tenantId, platform);
    if (documentLibraryEntity == null) {
      return null;
    }
    return BeanUtil.copy(documentLibraryEntity, DocumentLibraryDTO.class);
  }

  /**
   * 创建文档库
   *
   * @param request 创建请求
   * @param creatorId 创建人用户ID
   * @return 新建的文档库
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public DocumentLibraryDTO createLibrary(DocumentLibraryDTO request, Long creatorId) {
    Assert.isTrue(!Objects.equals(request.getVisibilityScope(), VisibilityScopeEnum.PRIVATE.getCode()),
      "不能创建此文档库");
    if (documentLibraryMapper.existsLibraryName(request)) {
      throw new BssException(BaseErrorConstant.CHECK_NAME.toResult(request.getLibraryName()).getResultMsg());
    }
    request.setId(IDUtils.nextId());
    request.setLibraryId(DcIdUtils.createLibraryId());
    request.setLibraryName(request.getLibraryName());
    request.setDescription(StringUtils.trimToNull(request.getDescription()));
    request.setVisibilityScope(
      StringUtils.defaultIfBlank(request.getVisibilityScope(), VisibilityScopeEnum.PUBLIC.getCode()));
    request.setLibraryIcon(StringUtils.trimToNull(request.getLibraryIcon()));
    request.setOwnerId(creatorId);
    request.setSortOrder(0);
    request.setAccessCount(0);
    request.setLastAccessTime(new Date());
    request.setCreatorId(creatorId);
    request.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    // 如果未设置spaceId，从上下文获取
    if (request.getSpaceId() == null) {
      request.setSpaceId(SpaceContextHolder.getSpaceId());
    }

    documentLibraryMapper.insert(request);

    // 初始化文档库根节点
    IDocumentService documentService = SpringUtil.getBean(IDocumentService.class);
    documentService.initLibraryRootDocumentNode(request.getLibraryId(), request.getLibraryName(),
      request.getTenantId(), request.getSpaceId());
    return request;
  }

  /**
   * 获取"我可见"的文档库分页列表
   *
   * @param pageNum 页码
   * @param pageSize 每页大小
   * @param sortBy 排序字段
   * @param sortOrder 排序顺序
   * @param keyword 关键词
   * @param spaceId 空间ID
   * @return 文档库分页列表
   */
  @Override
  public PageInfo<LibraryListDTO> getVisibleLibraryPage(Integer pageNum, Integer pageSize, String sortBy,
    String sortOrder, String keyword, Long spaceId, Long tenantId, Long envTenantId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    List<Long> orgIds = loadUserOrgIds(userId);

    String statusCd = DocBaseConsts.STATUS_CD_VALID;
    String safeSortBy = normalizeSortBy(sortBy);
    String safeSortOrder = normalizeSortOrder(sortOrder);
    RowBounds rowBounds = buildRowBounds(pageNum, pageSize);

    Page<LibraryListDTO> page = executePageQuery(userId, orgIds, statusCd, safeSortBy, safeSortOrder, keyword,
      SessionUtil.isSuperAdmin(userId), spaceId, rowBounds, tenantId);

    List<LibraryListDTO> list = new ArrayList<>(page);
    Map<Long, PortalUserDTO> userMap = fetchUserMap(collectRelatedUserIds(list));
    enrichUserInfos(list, userMap);
    // 查询并设置收藏状态和置顶状态
    enrichFavoriteAndPinStatus(list, userId, spaceId, envTenantId);

    return buildPageInfo(page, list);
  }

  /**
   * 获取文档库详情
   *
   * @param libraryId 文档库 ID
   * @param spaceId 空间ID
   * @return 文档库详情
   */
  @Override
  public LibraryDetailDTO getLibraryDetail(String libraryId, Long spaceId, Long tenantId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    String statusCd = DocBaseConsts.STATUS_CD_VALID;

    LibraryDetailDTO detail = loadLibraryDetailBase(libraryId, userId, statusCd, spaceId, tenantId);
    if (detail == null) {
      return null;
    }

    enrichCreatorInfo(detail);

    List<MemberInfo> members = loadLibraryMembers(libraryId, statusCd, detail.getCreatorUserId(), spaceId);

    // 创建创建者的成员信息并添加到列表首位
    if (detail.getCreatorUserId() != null) {
      MemberInfo creatorMember = new LibraryDetailDTO.MemberInfo();
      creatorMember.setSubjectType(SubjectTypeEnum.USER.getCode());
      creatorMember.setSubjectId(detail.getCreatorUserId());
      creatorMember.setPermissionType(LibraryRoleEnum.MANAGE.getCode());
      creatorMember.setIsOwner(true);
      // 将创建者添加到成员列表的首位
      List<MemberInfo> enhancedMembers = new ArrayList<>();
      enhancedMembers.add(creatorMember);
      if (CollectionUtils.isNotEmpty(members)) {
        enhancedMembers.addAll(members);
      }
      members = enhancedMembers;
    }

    if (CollectionUtils.isNotEmpty(members)) {
      members = enrichMemberNames(members);
      // 设置成员的所有者标识
      setMemberOwnerFlag(members, detail.getCreatorUserId());
    }
    detail.setMembers(members);

    return detail;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public LibrarySettingsResponseDTO updateLibrarySettings(String libraryId, LibrarySettingsRequestDTO request,
    Long userId) {
    // 验证请求参数和权限
    DocumentLibraryEntity library = validateLibrarySettings(libraryId, request, userId);

    // 执行设置更新
    return executeLibrarySettingsUpdate(libraryId, library, request, userId);
  }

  /**
   * 验证文档库设置更新请求
   */
  private DocumentLibraryEntity validateLibrarySettings(String libraryId, LibrarySettingsRequestDTO request, Long userId) {
    // 验证文档库是否存在
    DocumentLibraryEntity library = documentLibraryMapper.selectByLibrary(libraryId);
    if (library == null) {
      throw new BssException("文档库不存在: " + libraryId);
    }

    // 检查是否为"我的文档"库，如果是则禁止修改设置
    if (DocBaseConsts.TRUE.equals(library.getIsBuiltin()) && "我的文档".equals(library.getLibraryName())) {
      throw new BssException("我的文档库无法修改设置");
    }
    checkLibrarySettingPermission(library, request, userId);

    return library;
  }

  /**
   * 判断设置文档库信息的时候校验权限以及名称是否重复
   * @param library 文档库信息
   * @param request 请求信息
   * @param userId 登录id
   */
  private void checkLibrarySettingPermission(DocumentLibraryEntity library, LibrarySettingsRequestDTO request, Long userId) {
    // 验证操作类型
    SettingsActionEnum.fromCode(request.getAction());
    SettingsActionEnum action = SettingsActionEnum.fromCode(request.getAction());
    if (action == SettingsActionEnum.UPDATE_INFO) {
      LibrarySettingsDataDTO data = request.getData();
      if (data != null && StringUtils.isNotEmpty(data.getLibraryName())
        && !library.getLibraryName().equals(data.getLibraryName())) {
        DocumentLibraryDTO libraryDTO = new DocumentLibraryDTO();
        libraryDTO.setLibraryId(library.getLibraryId());
        libraryDTO.setLibraryName(data.getLibraryName());
        libraryDTO.setTenantId(request.getTenantId());
        if (documentLibraryMapper.existsLibraryName(libraryDTO)) {
          throw new BssException(BaseErrorConstant.CHECK_NAME.toResult(data.getLibraryName()).getResultMsg());
        }
      }
    }

    // 验证操作权限（超级管理员或所有者可修改设置，管理员也可修改）
    if (!SessionUtil.isSuperAdmin(userId) && !library.getOwnerId().equals(userId)) {
      // 检查是否是管理者（查询 bt_dc_library_permission 表）
      LibraryPermissionEntity userPermission = libraryPermissionMapper.selectByLibraryAndSubject(library.getLibraryId(),
        SubjectTypeEnum.USER.getCode(), userId);

      if (userPermission == null || !LibraryRoleEnum.MANAGE.getCode().equals(userPermission.getPermissionType())) {
        throw new BssException("权限不足，只有文档库所有者和管理员可以修改设置");
      }
    }
  }

  /**
   * 执行文档库设置更新
   */
  private LibrarySettingsResponseDTO executeLibrarySettingsUpdate(String libraryId, DocumentLibraryEntity library,
    LibrarySettingsRequestDTO request, Long userId) {
    SettingsActionEnum action = SettingsActionEnum.fromCode(request.getAction());

    LibrarySettingsResponseDTO response = new LibrarySettingsResponseDTO();
    response.setLibraryId(libraryId);
    response.setAction(request.getAction());

    if (action == SettingsActionEnum.UPDATE_INFO) {
      // 更新基础信息
      LibrarySettingsDataDTO data = request.getData();
      updateLibraryInfo(libraryId, data, userId, request.getSpaceId());
      response.setMessage("基础信息更新成功");
      if (!Objects.equals(library.getLibraryName(), data.getLibraryName())) {
        // 更新根节点名称
        IDocumentService documentService = SpringUtil.getBean(IDocumentService.class);
        documentService.updateLibraryRootNodeName(libraryId, data.getLibraryName());

        DocumentPathEventPublisher documentPathEventPublisher = SpringUtil.getBean(DocumentPathEventPublisher.class);
        documentPathEventPublisher.publishLibraryNameChangedEvent(libraryId, data.getLibraryName(), userId,
          request.getSpaceId());
      }
    }
    else if (action == SettingsActionEnum.UPDATE_VISIBILITY) {
      // 更新可见范围
      updateLibraryVisibility(libraryId, request.getData(), userId, request.getSpaceId());
      response.setMessage("可见范围更新成功");
    }

    return response;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVO<Void> updateLibraryPermissions(String libraryId, LibraryPermissionRequestDTO request, Long userId) {
    validateLibraryAndPermissions(libraryId, userId);
    validatePermissionType(request);
    executePermissionAction(libraryId, request, userId);
    return ResultVO.success();
  }

  @Override
  public String findUserPrivateLibraryId(Long userId, Long spaceId) {
    List<String> userPrivateLibraryId = documentLibraryMapper.findUserPrivateLibraryId(userId, spaceId);
    return CollectionUtils.isEmpty(userPrivateLibraryId) ? null : userPrivateLibraryId.getFirst();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVO<Void> batchLibraryPermissions(String libraryId, LibraryBatchPermissionRequestDTO request,
    Long userId) {
    // 验证文档库存在性和用户权限
    validateLibraryAndPermissions(libraryId, userId);

    // 验证批量操作类型
    validateBatchActionType(request);

    // 验证所有成员的权限设置合法性
    validateBatchMembers(request.getMembers());

    try {
      PermissionActionEnum action = PermissionActionEnum.valueOf(request.getAction());
      executeBatchPermissionAction(libraryId, request.getMembers(), userId, action);
      return ResultVO.success();
    }
    catch (Exception e) {
      throw new BssException("批量权限设置失败，请稍后重试: " + e.getMessage(), e);
    }
  }

  /**
   * 删除文档库（逻辑删除）
   *
   * @param libraryId 文档库ID
   * @param userId 操作用户ID
   * @return 删除结果
   */
  @Override
  @Transactional
  public ResultVO<Void> deleteLibrary(String libraryId, Long userId) {
    Assert.hasText(libraryId, "文档库ID不能为空");

    // 检查文档库是否存在
    DocumentLibraryDTO library = findByLibraryId(libraryId);
    if (library == null) {
      return ResultVO.fail("文档库不存在");
    }

    // 检查是否为系统内置文档库
    if (DocBaseConsts.TRUE.equals(library.getIsBuiltin())) {
      return ResultVO.fail("系统内置文档库无法删除");
    }
    // 删除文档库中文档及相关数据
    DocumentNodeService documentNodeService = SpringUtil.getBean(DocumentNodeService.class);
    List<String> documentNodeIds = documentNodeService.selectWithoutRootByLibraryId(libraryId);
    if (!documentNodeIds.isEmpty()) {
      documentNodeService.deleteDocumentNode(libraryId, userId, documentNodeIds.toArray(new String[0]));
    }
    // 删除收藏数据
    favoriteMapper.deleteTenantFavoriteByTarget(TenantContextHolder.getRequiredTenantId(), libraryId,
      TargetTypeEnum.LIBRARY.getCode(), SpaceContextHolder.getRequiredSpaceId());
    // 执行逻辑删除
    int result = documentLibraryMapper.rubbishByLibraryId(libraryId, userId, library.getSpaceId());
    DocumentPathEventPublisher documentPathEventPublisher = SpringUtil.getBean(DocumentPathEventPublisher.class);
    documentPathEventPublisher.publishLibraryDeletedEvent(libraryId, userId, library.getSpaceId(), documentNodeIds);
    if (result <= 0) {
      return ResultVO.fail("删除文档库失败");
    }
    return ResultVO.success();
  }

  @Override
  public String findUserPrivateMyLibraryById(Long userId, Long spaceId, Long tenantId) {
    List<String> userPrivateLibrary = documentLibraryMapper.findUserPrivateMyLibraryById(userId, spaceId);
    if (userPrivateLibrary == null || userPrivateLibrary.isEmpty()) {
      //直接创建
      return DocumentLibraryInitServiceHelper.initializeUserDocumentLibrary(userId, tenantId, spaceId);
    }
    return userPrivateLibrary.getFirst();
  }

  @Override
  public ResultVO<ExitShareResponseDTO> exitShare(ExitShareRequestDTO request, Long userId) {
    Assert.notNull(request, "退出共享请求不能为空");
    Assert.hasText(request.getLibraryId(), "文档库ID不能为空");

    String libraryId = request.getLibraryId();

    // 查找当前用户在该文档库的直接权限记录
    LibraryPermissionEntity userPermission = libraryPermissionMapper.selectByLibraryAndSubject(libraryId,
      SubjectTypeEnum.USER.getCode(), userId);

    if (userPermission == null) {
      return ResultVO.fail("您没有该文档库的权限，无法退出共享");
    }

    // 检查权限是否为直接授予的（非继承权限）
    // 如果权限是由组织继承而来，则不能直接退出
    if (!Objects.equals(userPermission.getGrantedBy(), userId)) {
      return ResultVO.fail("该权限由组织管理员授予，无法直接退出共享，请联系组织管理员");
    }

    // 执行逻辑删除权限记录
    int result = libraryPermissionMapper.deleteByPrimaryKey(userPermission.getPermissionId());
    if (result <= 0) {
      return ResultVO.fail("退出共享失败，请稍后重试");
    }

    // 构建响应
    ExitShareResponseDTO response = new ExitShareResponseDTO();
    response.setLibraryId(libraryId);

    return ResultVO.success(response);
  }

  @Override
  public DocumentLibraryDTO queryLibraryByDocumentId(String documentId) {
    return documentLibraryMapper.queryLibraryByDocumentId(documentId);
  }

  private LibraryDetailDTO loadLibraryDetailBase(String libraryId, Long userId, String statusCd, Long spaceId, Long tenantId) {
    return documentLibraryMapper.selectLibraryBase(libraryId, statusCd, userId, spaceId, tenantId);
  }

  private void enrichCreatorInfo(LibraryDetailDTO detail) {
    if (detail.getCreatorUserId() == null) {
      return;
    }
    PortalUserDTO creatorUser = dcUserService.findUserById(detail.getCreatorUserId());
    if (creatorUser == null) {
      return;
    }
    UserInfo creator = new UserInfo();
    creator.setUserId(creatorUser.getUserId());
    creator.setUsername(creatorUser.getUserName());
    detail.setCreator(creator);
  }

  private List<LibraryDetailDTO.MemberInfo> loadLibraryMembers(String libraryId, String statusCd, Long creatorId, Long spaceId) {
    return documentLibraryMapper.selectLibraryMembers(libraryId, statusCd, creatorId, spaceId);
  }

  /**
   * 填充成员名称信息，并过滤掉已删除的用户/组织
   *
   * @param members 成员列表
   * @return 过滤后的成员列表（已删除的用户/组织将被移除）
   */
  private List<MemberInfo> enrichMemberNames(List<MemberInfo> members) {
    Map<Long, PortalUserDTO> userMap = fetchAllUserNames(members);
    Map<Long, OrgDTO> orgMap = fetchAllOrgNames(members);
    return enrichAllMemberNames(members, userMap, orgMap);
  }

  /**
   * 获取所有用户名信息
   *
   * @param members 成员列表
   * @return 用户ID到用户信息的映射
   */
  private Map<Long, PortalUserDTO> fetchAllUserNames(List<MemberInfo> members) {
    List<Long> subjectUserIds = members.stream().filter(m -> "USER".equalsIgnoreCase(m.getSubjectType()))
      .map(LibraryDetailDTO.MemberInfo::getSubjectId).filter(Objects::nonNull).distinct().toList();
    Set<Long> allUserIds = new HashSet<>(subjectUserIds);
    List<Long> grantedByUserIds = members.stream().map(LibraryDetailDTO.MemberInfo::getGrantedBy)
      .filter(Objects::nonNull).distinct().toList();
    allUserIds.addAll(grantedByUserIds);
    Map<Long, PortalUserDTO> userMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(allUserIds)) {
      userMap = dcUserService.findUserMapBatchByIds(new ArrayList<>(allUserIds));
    }

    return userMap;
  }

  /**
   * 获取所有组织名称信息
   *
   * @param members 成员列表
   * @return 组织ID到组织信息的映射
   */
  private Map<Long, OrgDTO> fetchAllOrgNames(List<MemberInfo> members) {
    List<Long> orgIds = members.stream().filter(m -> "ORG".equalsIgnoreCase(m.getSubjectType()))
      .map(LibraryDetailDTO.MemberInfo::getSubjectId).filter(Objects::nonNull).distinct().toList();
    Map<Long, OrgDTO> orgMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(orgIds)) {
      List<OrgDTO> orgs = dcOrgService.findBatchById(orgIds);
      if (CollectionUtils.isNotEmpty(orgs)) {
        orgMap = orgs.stream().collect(Collectors.toMap(OrgDTO::getOrgId, org -> org, (a, b) -> a));
      }
    }
    return orgMap;
  }

  /**
   * 填充所有成员的用户名信息，并过滤掉已删除的用户/组织
   *
   * @param members 成员列表
   * @param userMap 用户ID到用户信息的映射
   * @param orgMap 组织ID到组织信息的映射
   * @return 过滤后的成员列表（已删除的用户/组织将被移除）
   */
  private List<MemberInfo> enrichAllMemberNames(List<MemberInfo> members, Map<Long, PortalUserDTO> userMap,
    Map<Long, OrgDTO> orgMap) {
    List<MemberInfo> validMembers = new ArrayList<>();
    for (LibraryDetailDTO.MemberInfo m : members) {
      boolean isValid = false;
      // 设置 subjectName
      if (SubjectTypeEnum.USER.getCode().equalsIgnoreCase(m.getSubjectType())) {
        PortalUserDTO u = userMap.get(m.getSubjectId());
        if (u != null) {
          m.setSubjectName(u.getUserName());
          isValid = true;
        }
      }
      else if (SubjectTypeEnum.ORG.getCode().equalsIgnoreCase(m.getSubjectType())) {
        if (m.getSubjectId() != null) {
          OrgDTO org = orgMap.get(m.getSubjectId());
          if (org != null) {
            m.setSubjectName(org.getOrgName());
            isValid = true;
          }
        }
      }
      // 设置授权人名称
      if (m.getGrantedBy() != null) {
        PortalUserDTO grantedByUser = userMap.get(m.getGrantedBy());
        if (grantedByUser != null) {
          m.setGrantedByName(grantedByUser.getUserName());
        }
      }
      // 只有用户/组织存在时才添加到结果列表
      if (isValid) {
        validMembers.add(m);
      }
    }
    return validMembers;
  }

  private void setMemberOwnerFlag(List<MemberInfo> members, Long creatorUserId) {
    for (MemberInfo member : members) {
      // 如果成员是用户类型且subjectId等于创建者ID，则标记为所有者
      member.setIsOwner(SubjectTypeEnum.USER.getCode().equalsIgnoreCase(member.getSubjectType()) && Objects.equals(
        member.getSubjectId(), creatorUserId));
    }
  }

  private List<Long> loadUserOrgIds(Long userId) {
    List<OrgDTO> orgList = dcOrgService.queryUserOrgList(userId);
    return CollectionUtils.emptyIfNull(orgList).stream().map(OrgDTO::getOrgId).toList();
  }

  private String normalizeSortBy(String sortBy) {
    if (sortBy.equalsIgnoreCase(DocBaseConsts.SORT_FIELD_UPDATED_TIME)) {
      return DocBaseConsts.SORT_FIELD_UPDATED_TIME;
    }
    return DocBaseConsts.SORT_FIELD_CREATED_TIME;
  }

  private String normalizeSortOrder(String sortOrder) {
    return DocBaseConsts.SORT_ORDER_DESC.equalsIgnoreCase(sortOrder)
      ? DocBaseConsts.SORT_ORDER_DESC
      : DocBaseConsts.SORT_ORDER_ASC;
  }

  private RowBounds buildRowBounds(Integer pageNum, Integer pageSize) {
    int num = pageNum == null ? 1 : pageNum;
    int size = pageSize == null ? 20 : pageSize;
    return new RowBounds((num - 1) * size, size);
  }

  private Set<Long> collectRelatedUserIds(List<LibraryListDTO> list) {
    Set<Long> userIdSet = new HashSet<>();
    for (LibraryListDTO dto : list) {
      if (dto.getCreatorUserId() != null) {
        userIdSet.add(dto.getCreatorUserId());
      }
      if (dto.getOwnerUserId() != null) {
        userIdSet.add(dto.getOwnerUserId());
      }
      if (dto.getSharedByUserId() != null) {
        userIdSet.add(dto.getSharedByUserId());
      }
    }
    return userIdSet;
  }

  private Map<Long, PortalUserDTO> fetchUserMap(Set<Long> userIdSet) {
    Map<Long, PortalUserDTO> userMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(userIdSet)) {
      List<PortalUserDTO> users = dcUserService.findUserBatchByIds(new ArrayList<>(userIdSet));
      if (CollectionUtils.isNotEmpty(users)) {
        userMap = users.stream().collect(Collectors.toMap(PortalUserDTO::getUserId, u -> u, (a, b) -> a));
      }
    }
    return userMap;
  }

  private void enrichUserInfos(List<LibraryListDTO> list, Map<Long, PortalUserDTO> userMap) {
    for (LibraryListDTO dto : list) {
      if (dto.getCreatorUserId() != null) {
        PortalUserDTO user = userMap.get(dto.getCreatorUserId());
        if (user != null) {
          UserInfo creator = new UserInfo();
          creator.setUserId(user.getUserId());
          creator.setUsername(user.getUserName());
          dto.setCreator(creator);
        }
      }
      if (dto.getOwnerUserId() != null) {
        PortalUserDTO user = userMap.get(dto.getOwnerUserId());
        if (user != null) {
          UserInfo owner = new UserInfo();
          owner.setUserId(user.getUserId());
          owner.setUsername(user.getUserName());
          dto.setOwner(owner);
        }
      }
      if (dto.getSharedByUserId() != null) {
        PortalUserDTO user = userMap.get(dto.getSharedByUserId());
        if (user != null) {
          UserInfo sharedBy = new UserInfo();
          sharedBy.setUserId(user.getUserId());
          sharedBy.setUsername(user.getUserName());
          dto.setSharedBy(sharedBy);
        }
      }
    }
  }

  private PageInfo<LibraryListDTO> buildPageInfo(Page<LibraryListDTO> page, List<LibraryListDTO> list) {
    PageInfo<LibraryListDTO> pageInfo = new PageInfo<>(list);
    pageInfo.setPageNum(page.getPageNum());
    pageInfo.setPageSize(page.getPageSize());
    pageInfo.setTotal(page.getTotal());
    pageInfo.setPages(page.getPages());
    pageInfo.setList(list);
    return pageInfo;
  }

  /**
   * 更新文档库基础信息
   */
  private void updateLibraryInfo(String libraryId, LibrarySettingsRequestDTO.LibrarySettingsDataDTO data, Long userId, Long spaceId) {
    // 验证必填字段
    if (StringUtils.isBlank(data.getLibraryName())) {
      throw new BssException("文档库名称不能为空");
    }
    // 验证名称长度
    if (data.getLibraryName().length() > 100) {
      throw new BssException("文档库名称长度不能超过100个字符");
    }
    // 验证描述长度
    if (StringUtils.isNotBlank(data.getDescription()) && data.getDescription().length() > 255) {
      throw new BssException("描述长度不能超过255个字符");
    }
    int result = documentLibraryMapper.updateLibraryInfo(libraryId, data.getLibraryName(), data.getDescription(),
      data.getLibraryIcon(), data.getColor(), userId, LocalDateTime.now(), spaceId);
    if (result <= 0) {
      throw new BssException("更新文档库基础信息失败");
    }
  }

  private Page<LibraryListDTO> executePageQuery(Long userId, List<Long> orgIds, String statusCd, String safeSortBy,
    String safeSortOrder, String keyword, Boolean isSuperAdmin, Long spaceId, RowBounds rowBounds, Long tenantId) {
    LibraryPageQueryParam param = new LibraryPageQueryParam();
    param.setUserId(userId);
    param.setOrgIds(orgIds);
    param.setStatusCd(statusCd);
    param.setSortBy(safeSortBy);
    param.setSortOrder(safeSortOrder);
    param.setKeywordLike(StringUtils.isBlank(keyword) ? null : keyword);
    param.setSpaceId(spaceId);
    param.setTenantId(tenantId);
    if (Boolean.TRUE.equals(isSuperAdmin)) {
      return documentLibraryMapper.selectAllLibraries(param, rowBounds);
    }
    else {
      return documentLibraryMapper.selectVisibleLibraries(param, rowBounds);
    }
  }

  /**
   * 更新文档库可见范围
   */
  private void updateLibraryVisibility(String libraryId, LibrarySettingsRequestDTO.LibrarySettingsDataDTO data,
    Long userId, Long spaceId) {
    // 验证可见范围
    if (StringUtils.isBlank(data.getVisibilityScope())) {
      throw new BssException("可见范围不能为空");
    }
    // 验证可见范围是否合法
    boolean isValidScope = false;
    for (VisibilityScopeEnum scope : VisibilityScopeEnum.values()) {
      if (scope.getCode().equals(data.getVisibilityScope())) {
        isValidScope = true;
        break;
      }
    }
    if (!isValidScope) {
      throw new BssException("无效的可见范围: " + data.getVisibilityScope());
    }
    int result = documentLibraryMapper.updateLibraryVisibility(libraryId, data.getVisibilityScope(), userId,
      LocalDateTime.now(), spaceId);
    if (result <= 0) {
      throw new BssException("更新文档库可见范围失败");
    }
  }

  /**
   * 验证文档库存在性和用户权限
   */
  private void validateLibraryAndPermissions(String libraryId, Long userId) {
    DocumentLibraryEntity library = documentLibraryMapper.selectByLibrary(libraryId);
    if (library == null) {
      throw new BssException("文档库不存在: " + libraryId);
    }

    // 检查是否为"我的文档"库，如果是则禁止修改权限
    if (DocBaseConsts.TRUE.equals(library.getIsBuiltin()) && "我的文档".equals(library.getLibraryName())) {
      throw new BssException("我的文档库无法修改设置");
    }

    // 验证操作权限（超级管理员或所有者可修改权限设置，管理员也可修改）
    if (!SessionUtil.isSuperAdmin(userId) && !library.getOwnerId().equals(userId)) {
      LibraryPermissionEntity userPermission = libraryPermissionMapper.selectByLibraryAndSubject(libraryId,
        SubjectTypeEnum.USER.getCode(), userId);

      if (userPermission == null || !LibraryRoleEnum.MANAGE.getCode().equals(userPermission.getPermissionType())) {
        throw new BssException("权限不足，只有文档库所有者和管理员可以修改权限设置");
      }
    }
  }

  /**
   * 验证权限类型
   */
  private void validatePermissionType(LibraryPermissionRequestDTO request) {
    PermissionActionEnum action = PermissionActionEnum.valueOf(request.getAction());
    if (action != PermissionActionEnum.REMOVE_MEMBER) {
      if (StringUtils.isBlank(request.getData().getPermissionType())) {
        throw new BssException("权限类型不能为空");
      }
      boolean isValidPermission = false;
      for (LibraryRoleEnum role : LibraryRoleEnum.values()) {
        if (role.getCode().equals(request.getData().getPermissionType())) {
          isValidPermission = true;
          break;
        }
      }
      if (!isValidPermission) {
        throw new BssException("无效的权限类型: " + request.getData().getPermissionType());
      }
    }
  }

  /**
   * 处理具体的权限操作
   */
  private void processPermissionAction(String libraryId, LibraryPermissionRequestDTO request, Long userId,
    PermissionActionEnum action) {
    switch (action) {
      case ADD_MEMBER:
        addLibraryMember(libraryId, request.getData(), userId);
        break;
      case UPDATE_MEMBER:
        updateLibraryMember(libraryId, request.getData(), userId);
        break;
      case REMOVE_MEMBER:
        removeLibraryMember(libraryId, request.getData());
        break;
      default:
        throw new BssException("不支持的操作类型: " + action.getCode());
    }
  }

  /**
   * 添加文档库成员
   */
  private void addLibraryMember(String libraryId, LibraryPermissionDataDTO data, Long userId) {
    // 检查权限主体是否已存在
    validateMemberNotExists(libraryId, data);

    // 创建并保存权限记录
    LibraryPermissionEntity permission = createPermissionEntity(libraryId, data, userId);
    savePermissionEntity(permission, "添加成员失败");
  }

  /**
   * 更新文档库成员权限
   */
  private void updateLibraryMember(String libraryId, LibraryPermissionDataDTO data, Long userId) {
    // 检查权限主体是否存在
    LibraryPermissionEntity existingPermission = validateMemberExists(libraryId, data);

    // 更新权限类型
    updatePermissionEntity(existingPermission, data, userId);
  }

  /**
   * 移除文档库成员
   */
  private void removeLibraryMember(String libraryId, LibraryPermissionDataDTO data) {
    // 检查权限主体是否存在
    LibraryPermissionEntity existingPermission = validateMemberExists(libraryId, data);

    // 删除权限记录
    deletePermissionEntity(existingPermission);
  }

  /**
   * 验证成员不存在
   */
  private void validateMemberNotExists(String libraryId, LibraryPermissionDataDTO data) {
    LibraryPermissionEntity existingPermission = libraryPermissionMapper.selectByLibraryAndSubject(libraryId,
      data.getSubjectType(), data.getSubjectId());

    if (existingPermission != null) {
      throw new BssException("权限主体已存在，无法重复添加");
    }
  }

  /**
   * 验证成员存在
   */
  private LibraryPermissionEntity validateMemberExists(String libraryId, LibraryPermissionDataDTO data) {
    LibraryPermissionEntity existingPermission = libraryPermissionMapper.selectByLibraryAndSubject(libraryId,
      data.getSubjectType(), data.getSubjectId());

    if (existingPermission == null) {
      throw new BssException("权限主体不存在，无法操作");
    }

    return existingPermission;
  }

  /**
   * 创建权限实体
   */
  private LibraryPermissionEntity createPermissionEntity(String libraryId, LibraryPermissionDataDTO data, Long userId) {
    LibraryPermissionEntity permission = new LibraryPermissionEntity();
    permission.setPermissionId(IDUtils.nextId());
    permission.setLibraryId(libraryId);
    permission.setSubjectType(data.getSubjectType());
    permission.setSubjectId(data.getSubjectId());
    permission.setPermissionType(data.getPermissionType());
    permission.setGrantedBy(userId);
    permission.setCreatorId(userId);
    permission.setUpdatorId(userId);
    permission.setStatusCd("00A");

    return permission;
  }

  /**
   * 保存权限实体
   */
  private void savePermissionEntity(LibraryPermissionEntity permission, String errorMessage) {
    int result = libraryPermissionMapper.insert(permission);
    if (result <= 0) {
      throw new BssException(errorMessage);
    }
  }

  /**
   * 更新权限实体
   */
  private void updatePermissionEntity(LibraryPermissionEntity permission, LibraryPermissionDataDTO data, Long userId) {
    permission.setPermissionType(data.getPermissionType());
    permission.setUpdatorId(userId);

    int result = libraryPermissionMapper.updateByPrimaryKey(permission);
    if (result <= 0) {
      throw new BssException("更新成员权限失败");
    }
  }

  /**
   * 删除权限实体
   */
  private void deletePermissionEntity(LibraryPermissionEntity permission) {
    int result = libraryPermissionMapper.deleteByPrimaryKey(permission.getPermissionId());
    if (result <= 0) {
      throw new BssException("移除成员失败");
    }
  }

  /**
   * 执行权限操作
   */
  private void executePermissionAction(String libraryId, LibraryPermissionRequestDTO request, Long userId) {
    try {
      PermissionActionEnum action = PermissionActionEnum.valueOf(request.getAction());
      processPermissionAction(libraryId, request, userId, action);
    }
    catch (Exception e) {
      throw new BssException("权限设置失败，请稍后重试: " + e.getMessage(), e);
    }
  }

  /**
   * 验证批量操作类型
   */
  private void validateBatchActionType(LibraryBatchPermissionRequestDTO request) {
    try {
      PermissionActionEnum action = PermissionActionEnum.valueOf(request.getAction());
      if (action != PermissionActionEnum.BATCH_ADD && action != PermissionActionEnum.BATCH_UPDATE
        && action != PermissionActionEnum.BATCH_REMOVE) {
        throw new BssException("不支持的批量操作类型: " + request.getAction());
      }
    }
    catch (IllegalArgumentException e) {
      throw new BssException("无效的批量操作类型: " + request.getAction(), e);
    }
  }

  /**
   * 验证批量成员权限设置合法性
   */
  private void validateBatchMembers(List<PermissionMemberDTO> members) {
    for (PermissionMemberDTO member : members) {
      // 验证主体类型
      if (!isValidSubjectType(member.getSubjectType())) {
        throw new BssException("无效的主体类型: " + member.getSubjectType());
      }

      // 验证主体ID
      if (member.getSubjectId() == null) {
        throw new BssException("主体ID不能为空");
      }

      // 验证权限类型（对于添加和更新操作）
      if (member.getPermissionType() != null && !isValidPermissionType(member.getPermissionType())) {
        throw new BssException("无效的权限类型: " + member.getPermissionType());
      }
    }
  }

  /**
   * 验证主体类型是否合法
   */
  private boolean isValidSubjectType(String subjectType) {
    for (SubjectTypeEnum subjectTypeEnum : SubjectTypeEnum.values()) {
      if (subjectTypeEnum.getCode().equals(subjectType)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 验证权限类型是否合法
   */
  private boolean isValidPermissionType(String permissionType) {
    for (LibraryRoleEnum role : LibraryRoleEnum.values()) {
      if (role.getCode().equals(permissionType)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 执行批量权限操作
   */
  private void executeBatchPermissionAction(String libraryId, List<PermissionMemberDTO> members, Long userId,
    PermissionActionEnum action) {
    switch (action) {
      case BATCH_ADD:
        batchAddPermissions(libraryId, members, userId);
        break;
      case BATCH_UPDATE:
        batchUpdatePermissions(libraryId, members, userId);
        break;
      case BATCH_REMOVE:
        batchRemovePermissions(libraryId, members);
        break;
      default:
        throw new BssException("不支持的操作类型: " + action.getCode());
    }
  }

  /**
   * 批量添加权限
   */
  private void batchAddPermissions(String libraryId, List<PermissionMemberDTO> members, Long userId) {
    List<LibraryPermissionEntity> permissionsToAdd = new ArrayList<>();

    for (PermissionMemberDTO member : members) {
      // 检查权限主体是否已存在
      LibraryPermissionEntity existingPermission = libraryPermissionMapper.selectByLibraryAndSubject(libraryId,
        member.getSubjectType(), member.getSubjectId());

      if (existingPermission != null) {
        throw new BssException("权限主体已存在，无法重复添加: " + member.getSubjectType() + "-" + member.getSubjectId());
      }

      // 创建权限实体
      LibraryPermissionEntity permission = createPermissionEntity(libraryId, member, userId);
      permissionsToAdd.add(permission);
    }

    // 批量插入权限
    if (!permissionsToAdd.isEmpty()) {
      int result = libraryPermissionMapper.batchInsertPermissions(permissionsToAdd);
      if (result <= 0) {
        throw new BssException("批量添加权限失败");
      }
    }
  }

  /**
   * 批量更新权限
   */
  private void batchUpdatePermissions(String libraryId, List<PermissionMemberDTO> members, Long userId) {
    for (PermissionMemberDTO member : members) {
      // 检查权限主体是否存在
      LibraryPermissionEntity existingPermission = libraryPermissionMapper.selectByLibraryAndSubject(libraryId,
        member.getSubjectType(), member.getSubjectId());

      if (existingPermission == null) {
        throw new BssException("权限主体不存在，无法更新: " + member.getSubjectType() + "-" + member.getSubjectId());
      }

      // 更新权限类型
      existingPermission.setPermissionType(member.getPermissionType());
      existingPermission.setUpdatorId(userId);

      int result = libraryPermissionMapper.updateByPrimaryKey(existingPermission);
      if (result <= 0) {
        throw new BssException("更新权限失败: " + member.getSubjectType() + "-" + member.getSubjectId());
      }
    }
  }

  /**
   * 批量移除权限
   */
  private void batchRemovePermissions(String libraryId, List<PermissionMemberDTO> members) {
    List<Long> permissionIdsToDelete = new ArrayList<>();

    for (PermissionMemberDTO member : members) {
      // 检查权限主体是否存在
      LibraryPermissionEntity existingPermission = libraryPermissionMapper.selectByLibraryAndSubject(libraryId,
        member.getSubjectType(), member.getSubjectId());

      if (existingPermission == null) {
        throw new BssException("权限主体不存在，无法移除: " + member.getSubjectType() + "-" + member.getSubjectId());
      }

      permissionIdsToDelete.add(existingPermission.getPermissionId());
    }

    // 批量删除权限
    if (!permissionIdsToDelete.isEmpty()) {
      int result = libraryPermissionMapper.batchDeletePermissions(permissionIdsToDelete);
      if (result <= 0) {
        throw new BssException("批量删除权限失败");
      }
    }
  }

  /**
   * 创建权限实体（重载方法）
   */
  private LibraryPermissionEntity createPermissionEntity(String libraryId, PermissionMemberDTO member, Long userId) {
    LibraryPermissionEntity permission = new LibraryPermissionEntity();
    permission.setPermissionId(IDUtils.nextId());
    permission.setLibraryId(libraryId);
    permission.setSubjectType(member.getSubjectType());
    permission.setSubjectId(member.getSubjectId());
    permission.setPermissionType(member.getPermissionType());
    permission.setGrantedBy(userId);
    permission.setCreatorId(userId);
    permission.setUpdatorId(userId);
    permission.setStatusCd("00A");
    permission.setTenantId(TenantContextHolder.getTenantId());

    return permission;
  }

  /**
   * 填充收藏状态和置顶状态
   *
   * @param list 文档库列表
   * @param userId 当前用户ID
   * @param spaceId 空间ID
   */
  private void enrichFavoriteAndPinStatus(List<LibraryListDTO> list, Long userId, Long spaceId, Long tenantId) {
    if (CollectionUtils.isEmpty(list)) {
      return;
    }

    // 获取用户的置顶文档库列表
    List<PinnedLibraryDTO> pinnedLibraries = pinnedLibraryCache.getPinnedLibraries(userId, spaceId);
    Set<String> pinnedLibraryIds = CollectionUtils.emptyIfNull(pinnedLibraries).stream()
      .map(PinnedLibraryDTO::getLibraryId).collect(Collectors.toSet());

    // 批量查询收藏状态
    List<String> libraryIds = list.stream().map(LibraryListDTO::getLibraryId).distinct().collect(Collectors.toList());

    Map<String, Boolean> favoriteStatusMap = queryFavoriteStatus(userId, libraryIds, spaceId, tenantId);

    // 设置收藏和置顶状态
    for (LibraryListDTO dto : list) {
      // 设置收藏状态
      dto.setIsFavorite(favoriteStatusMap.getOrDefault(dto.getLibraryId(), false));
      // 设置置顶状态
      dto.setIsPin(pinnedLibraryIds.contains(dto.getLibraryId()));
    }
  }

  /**
   * 批量查询用户的收藏状态
   *
   * @param userId 用户ID
   * @param libraryIds 文档库ID列表
   * @return 收藏状态映射
   */
  private Map<String, Boolean> queryFavoriteStatus(Long userId, List<String> libraryIds, Long spaceId, Long tenantId) {
    Map<String, Boolean> favoriteStatusMap = new HashMap<>();

    if (CollectionUtils.isEmpty(libraryIds)) {
      return favoriteStatusMap;
    }

    // 为每个文档库查询收藏状态
    for (String libraryId : libraryIds) {
      boolean isFavorite = favoriteMapper.existsFavorite(userId, spaceId, libraryId, "LIBRARY", tenantId);
      favoriteStatusMap.put(libraryId, isFavorite);
    }

    return favoriteStatusMap;
  }
}


