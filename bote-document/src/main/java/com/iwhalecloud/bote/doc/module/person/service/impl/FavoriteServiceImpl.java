package com.iwhalecloud.bote.doc.module.person.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.consts.FavoriteConsts;
import com.iwhalecloud.bote.doc.consts.TargetTypeEnum;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.AddFavoriteRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.AddFavoriteResponseDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.FavoriteListDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.FavoriteOperationResponseDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.FavoritePinnedDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.PinFavoriteRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.RemoveFavoriteRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.UnpinFavoriteRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UserInfo;
import com.iwhalecloud.bote.doc.module.person.entity.UserFavoriteEntity;
import com.iwhalecloud.bote.doc.module.person.mapper.FavoriteMapper;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPathDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.doc.module.person.service.IFavoriteService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 收藏模块 Service 实现
 *
 * @author lizuyin
 * @since 2025-08-21
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements IFavoriteService {

  private final FavoriteMapper favoriteMapper;

  private final IDocumentService documentService;

  private final DocumentLibraryService documentLibraryService;

  private final IDcUserService dcUserService;

  /**
   * 将source列表的所有元素添加到target列表中
   *
   * @param target 目标列表
   * @param source 源列表
   */
  private static void addAll(List<FavoritePinnedDTO> target, List<FavoritePinnedDTO> source) {
    if (CollectionUtils.isNotEmpty(source)) {
      target.addAll(source);
    }
  }

  @Override
  public List<FavoritePinnedDTO> getPinnedFavorites(Long tenantId, Long spaceId, @Nullable String targetType) {
    Long userId = SessionUtil.getLoginInfo().getUserId();

    List<FavoritePinnedDTO> result = new ArrayList<>();
    if (StringUtils.isBlank(targetType)) {
      result = favoriteMapper.selectPinnedAllFavorites(tenantId, userId, spaceId);
    }
    else {
      if (FavoriteConsts.TARGET_TYPE_DOCUMENT.equals(targetType) || FavoriteConsts.TARGET_TYPE_FOLDER.equals(
        targetType)) {
        addAll(result, favoriteMapper.selectPinnedDocumentOrFolderFavorites(tenantId, userId, spaceId, targetType));
      }
      else if (FavoriteConsts.TARGET_TYPE_LIBRARY.equals(targetType)) {
        addAll(result,
          favoriteMapper.selectPinnedLibraryFavorites(tenantId, userId, spaceId, FavoriteConsts.TARGET_TYPE_LIBRARY));
      }
      else if (FavoriteConsts.TARGET_TYPE_KNOWLEDGE.equals(targetType)) {
        addAll(result, favoriteMapper.selectPinnedKnowledgeFavorites(tenantId, userId, spaceId,
          FavoriteConsts.TARGET_TYPE_KNOWLEDGE));
      }
      else {
        return Collections.emptyList();
      }
    }

    if (CollectionUtils.isNotEmpty(result)) {
      enrichFavorites(result);
      // 统一排序：置顶排序升序，置顶时间倒序
    }
    return result;
  }

  @Override
  public PageInfo<FavoriteListDTO> getFavoriteList(Long tenantId, Long spaceId, Integer pageNum, Integer pageSize,
    String targetType) {
    if (StringUtils.isBlank(targetType)) {
      return getAllFavorites(tenantId, spaceId, pageNum, pageSize);
    }
    else {
      return getFilteredFavorites(tenantId, spaceId, pageNum, pageSize, targetType);
    }
  }

  @Override
  @Transactional
  public AddFavoriteResponseDTO addFavorite(AddFavoriteRequestDTO request) {
    // 参数校验
    validateAddFavoriteRequest(request);
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long tenantId = TenantContextHolder.getTenantId();
    validateTargetResource(request.getTargetId(), request.getTargetType(), request.getPlatform());
    UserFavoriteEntity existingFavorite = favoriteMapper.selectByUserIdAndTarget(userId, request.getSpaceId(),
      request.getTargetId(), request.getTargetType(), tenantId);
    Long favoriteId;
    if (existingFavorite != null) {
      if (DocBaseConsts.STATUS_CD_VALID.equals(existingFavorite.getStatusCd())) {
        favoriteId = existingFavorite.getFavoriteId();
      }
      else if (DocBaseConsts.STATUS_CD_INVALID.equals(existingFavorite.getStatusCd())) {
        favoriteMapper.restoreFavorite(existingFavorite.getFavoriteId(), userId);
        favoriteId = existingFavorite.getFavoriteId();
      }
      else {
        throw new BssException("收藏记录状态异常：" + existingFavorite.getStatusCd());
      }
    }
    else {
      favoriteId = createFavoriteRecord(userId, tenantId, request.getSpaceId(), request);
    }
    AddFavoriteResponseDTO response = new AddFavoriteResponseDTO();
    response.setFavoriteId(favoriteId);
    return response;
  }

  @Override
  @Transactional
  public ResultVO<Void> removeFavorite(RemoveFavoriteRequestDTO request) {
    // 参数校验
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getTargetId(), "目标资源ID不能为空");
    Assert.hasText(request.getTargetType(), "目标类型不能为空");

    // 校验目标类型是否支持
    boolean isValidType =
      FavoriteConsts.TARGET_TYPE_DOCUMENT.equals(request.getTargetType()) || FavoriteConsts.TARGET_TYPE_FOLDER.equals(
        request.getTargetType()) || FavoriteConsts.TARGET_TYPE_LIBRARY.equals(request.getTargetType())
        || FavoriteConsts.TARGET_TYPE_KNOWLEDGE.equals(request.getTargetType());
    Assert.isTrue(isValidType, "不支持的目标类型：" + request.getTargetType());

    // 获取当前用户信息
    Long userId = SessionUtil.getLoginInfo().getUserId();

    // 检查是否已经收藏过
    if (!isAlreadyFavorited(userId, request.getSpaceId(), request.getTenantId(), request.getTargetId(), request.getTargetType())) {
      return ResultVO.fail("该资源未被收藏，无法取消收藏");
    }

    // 根据目标资源直接删除收藏记录（软删除）
    try {
      favoriteMapper.deleteFavoriteByTarget(userId, request.getSpaceId(), request.getTenantId(), request.getTargetId(),
        request.getTargetType());
    }
    catch (Exception e) {
      throw new BssException("取消收藏数据库操作失败, " + e.getMessage(), e);
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> removeTenantFavorite(Long tenantId, String targetId, String targetType, Long spaceId) {
    // 参数校验
    Assert.notNull(tenantId, "租户ID不能为空");
    Assert.notNull(targetId, "目标资源ID不能为空");
    Assert.hasText(targetType, "目标类型不能为空");
    Assert.notNull(spaceId, "空间ID不能为空");

    // 校验目标类型是否支持
    boolean isValidType =
      FavoriteConsts.TARGET_TYPE_DOCUMENT.equals(targetType) || FavoriteConsts.TARGET_TYPE_FOLDER.equals(targetType)
        || FavoriteConsts.TARGET_TYPE_LIBRARY.equals(targetType) || FavoriteConsts.TARGET_TYPE_KNOWLEDGE.equals(
        targetType);
    Assert.isTrue(isValidType, "不支持的目标类型：" + targetType);

    try {
      favoriteMapper.deleteTenantFavoriteByTarget(tenantId, targetId, targetType, spaceId);
    }
    catch (Exception e) {
      throw new BssException("删除租户维度收藏数据库操作失败, " + e.getMessage(), e);
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public FavoriteOperationResponseDTO pinFavorite(PinFavoriteRequestDTO request) {
    // 参数校验
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getFavoriteId(), "收藏ID不能为空");

    // 获取当前用户信息
    Long userId = SessionUtil.getLoginInfo().getUserId();

    // 验证收藏记录是否存在且属于当前用户
    UserFavoriteEntity favorite = favoriteMapper.selectByFavoriteIdAndUserId(request.getFavoriteId(), userId,
      request.getSpaceId(), request.getTenantId());
    Assert.notNull(favorite, "收藏记录不存在或无权限操作");

    // 检查是否已经置顶
    if (FavoriteConsts.IS_PINNED_TRUE.equals(favorite.getIsFavoritePinned())) {
      // 已经置顶，直接返回
      FavoriteOperationResponseDTO response = new FavoriteOperationResponseDTO();
      response.setFavoriteId(request.getFavoriteId());
      return response;
    }

    // 获取下一个置顶排序号
    Integer nextPinOrder = getNextPinOrder(userId, request.getSpaceId());

    // 更新为置顶状态
    favoriteMapper.updatePinStatus(request.getFavoriteId(), userId, request.getSpaceId(), FavoriteConsts.IS_PINNED_TRUE,
      nextPinOrder, request.getTenantId());

    // 构建响应
    FavoriteOperationResponseDTO response = new FavoriteOperationResponseDTO();
    response.setFavoriteId(request.getFavoriteId());
    return response;
  }

  @Override
  @Transactional
  public FavoriteOperationResponseDTO unpinFavorite(UnpinFavoriteRequestDTO request) {
    // 参数校验
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getFavoriteId(), "收藏ID不能为空");

    // 获取当前用户信息
    Long userId = SessionUtil.getLoginInfo().getUserId();

    // 验证收藏记录是否存在且属于当前用户
    UserFavoriteEntity favorite = favoriteMapper.selectByFavoriteIdAndUserId(request.getFavoriteId(), userId,
      request.getSpaceId(), request.getTenantId());
    Assert.notNull(favorite, "收藏记录不存在或无权限操作");

    // 检查是否已经取消置顶
    if (!FavoriteConsts.IS_PINNED_TRUE.equals(favorite.getIsFavoritePinned())) {
      // 已经取消置顶，直接返回
      FavoriteOperationResponseDTO response = new FavoriteOperationResponseDTO();
      response.setFavoriteId(request.getFavoriteId());
      return response;
    }

    // 更新为非置顶状态
    favoriteMapper.updatePinStatus(request.getFavoriteId(), userId, request.getSpaceId(), "F", 0, request.getTenantId());

    // 构建响应
    FavoriteOperationResponseDTO response = new FavoriteOperationResponseDTO();
    response.setFavoriteId(request.getFavoriteId());
    return response;
  }

  /**
   * 查询所有类型的收藏列表
   */
  private PageInfo<FavoriteListDTO> getAllFavorites(Long tenantId, Long spaceId, Integer pageNum, Integer pageSize) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    RowBounds rowBounds = buildRowBounds(pageNum, pageSize);
    //noinspection resource
    Page<FavoriteListDTO> page = favoriteMapper.selectFavoriteAllList(tenantId, userId, spaceId, rowBounds); //NOPMD - suppressed CloseResource - 不需要关闭
    List<FavoriteListDTO> resultList = new ArrayList<>();
    for (FavoriteListDTO item : page) {
      enrichFavoriteListItem(item);
      resultList.add(item);
    }
    PageInfo<FavoriteListDTO> pageInfo = new PageInfo<>(resultList);
    pageInfo.setPageNum(page.getPageNum());
    pageInfo.setPageSize(page.getPageSize());
    pageInfo.setTotal(page.getTotal());
    pageInfo.setPages(page.getPages());
    pageInfo.setList(resultList);
    return pageInfo;
  }

  /**
   * 根据指定的目标类型查询收藏列表
   */
  private PageInfo<FavoriteListDTO> getFilteredFavorites(Long tenantId, Long spaceId, Integer pageNum, Integer pageSize,
    String targetType) {
    Long userId = SessionUtil.getLoginInfo().getUserId();

    RowBounds rowBounds = buildRowBounds(pageNum, pageSize);
    Page<FavoriteListDTO> page; //NOPMD - suppressed CloseResource - 不需要关闭
    if (FavoriteConsts.TARGET_TYPE_DOCUMENT.equals(targetType) || FavoriteConsts.TARGET_TYPE_FOLDER.equals(
      targetType)) {
      //noinspection resource
      page = favoriteMapper.selectFavoriteDocumentOrFolderList(tenantId, userId, spaceId, targetType, rowBounds);
    }
    else if (FavoriteConsts.TARGET_TYPE_LIBRARY.equals(targetType)) {
      //noinspection resource
      page = favoriteMapper.selectFavoriteLibraryList(tenantId, userId, spaceId, FavoriteConsts.TARGET_TYPE_LIBRARY,
        rowBounds);
    }
    else if (FavoriteConsts.TARGET_TYPE_KNOWLEDGE.equals(targetType)) {
      page = favoriteMapper.selectFavoriteKnowledgeList(tenantId, userId, spaceId, FavoriteConsts.TARGET_TYPE_KNOWLEDGE,
        rowBounds);
    }
    else {
      PageInfo<FavoriteListDTO> emptyPageInfo = new PageInfo<>(Collections.emptyList());
      emptyPageInfo.setPageNum(pageNum != null ? pageNum : DocBaseConsts.DEFAULT_PAGE_NUM);
      emptyPageInfo.setPageSize(pageSize != null ? pageSize : DocBaseConsts.DEFAULT_PAGE_SIZE);
      emptyPageInfo.setTotal(0);
      emptyPageInfo.setPages(0);
      return emptyPageInfo;
    }
    List<FavoriteListDTO> resultList = new ArrayList<>();
    for (FavoriteListDTO item : page) {
      enrichFavoriteListItem(item);
      resultList.add(item);
    }
    PageInfo<FavoriteListDTO> pageInfo = new PageInfo<>(resultList);
    pageInfo.setPageNum(page.getPageNum());
    pageInfo.setPageSize(page.getPageSize());
    pageInfo.setTotal(page.getTotal());
    pageInfo.setPages(page.getPages());
    pageInfo.setList(resultList);
    return pageInfo;
  }

  /**
   * 校验添加收藏请求参数
   */
  private void validateAddFavoriteRequest(AddFavoriteRequestDTO request) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getTargetId(), "目标资源ID不能为空");
    Assert.hasText(request.getTargetType(), "目标类型不能为空");

    // 校验目标类型是否支持
    boolean isValidType =
      FavoriteConsts.TARGET_TYPE_DOCUMENT.equals(request.getTargetType()) || FavoriteConsts.TARGET_TYPE_FOLDER.equals(
        request.getTargetType()) || FavoriteConsts.TARGET_TYPE_LIBRARY.equals(request.getTargetType())
        || FavoriteConsts.TARGET_TYPE_KNOWLEDGE.equals(request.getTargetType());
    Assert.isTrue(isValidType, "不支持的目标类型：" + request.getTargetType());
  }

  /**
   * 校验目标资源是否存在
   */
  private void validateTargetResource(String targetId, String targetType, String platform) {
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    Long tenantId = TenantContextHolder.getRequiredTenantId();
    if (FavoriteConsts.TARGET_TYPE_DOCUMENT.equals(targetType) || FavoriteConsts.TARGET_TYPE_FOLDER.equals(
      targetType)) {
      validateDocumentOrFolder(targetId, tenantId, platform);
    }
    else if (FavoriteConsts.TARGET_TYPE_LIBRARY.equals(targetType)) {
      validateLibrary(targetId, tenantId, platform);
    }
    else if (FavoriteConsts.TARGET_TYPE_KNOWLEDGE.equals(targetType)) {
      validateKnowledge(targetId, tenantId, spaceId, platform);
    }
    else {
      throw new BssException("不支持的目标类型：" + targetType);
    }
  }

  /**
   * 校验文档或文件夹是否存在
   */
  private void validateDocumentOrFolder(String targetId, Long tenantId, String platform) {
    Assert.hasText(targetId, "文档或文件夹ID不能为空");
    Assert.notNull(tenantId, "租户ID不能为空");
    DcDocumentDTO document = documentService.findByDocumentIdAndTenantId(targetId, tenantId, platform);
    Assert.notNull(document, () -> "文档或文件夹不存在: " + targetId);
  }

  /**
   * 校验文档库是否存在
   */
  private void validateLibrary(String targetId, Long tenantId, String platform) {
    Assert.hasText(targetId, "文档库ID不能为空");
    Assert.notNull(tenantId, "租户ID不能为空");
    DocumentLibraryDTO library = documentLibraryService.findByLibraryIdAndTenantId(targetId, tenantId, platform);
    Assert.notNull(library, () -> "文档库不存在: " + targetId);
  }

  /**
   * 校验知识库是否存在
   */
  private void validateKnowledge(String targetId, Long tenantId, Long spaceId, String platform) {
    Assert.hasText(targetId, "知识库ID不能为空");
    Assert.notNull(spaceId, "企业空间ID不能为空");
    boolean exists = favoriteMapper.existsKnowledgeBaseByKbCode(tenantId, targetId, spaceId, platform);
    Assert.isTrue(exists, () -> "知识库不存在: " + targetId);

  }

  /**
   * 检查是否已经收藏过（有效状态）
   */
  private boolean isAlreadyFavorited(Long userId, Long spaceId, Long tenantId, String targetId, String targetType) {
    return favoriteMapper.existsFavorite(userId, spaceId, targetId, targetType, tenantId);
  }

  /**
   * 创建收藏记录
   */
  private Long createFavoriteRecord(Long userId, Long tenantId, Long spaceId, AddFavoriteRequestDTO request) {
    // 创建收藏实体
    UserFavoriteEntity favorite = new UserFavoriteEntity();
    favorite.setFavoriteId(IDUtils.nextId());
    favorite.setUserId(userId);
    favorite.setTargetId(request.getTargetId());
    favorite.setTargetType(request.getTargetType());
    favorite.setIsFavoritePinned("F"); // 默认不置顶
    favorite.setFavoritePinOrder(0); // 默认排序为0
    favorite.setTenantId(tenantId);
    favorite.setSpaceId(spaceId);
    favorite.setCreatorId(userId);
    favorite.setUpdatorId(userId);
    favorite.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    favorite.setCreatedTime(new Date());
    favorite.setUpdatedTime(new Date());

    // 插入数据库
    favoriteMapper.insert(favorite);

    return favorite.getFavoriteId();
  }

  /**
   * 获取下一个置顶排序号
   */
  private Integer getNextPinOrder(Long userId, Long spaceId) {
    Integer maxPinOrder = favoriteMapper.getMaxPinOrder(userId, spaceId);
    return maxPinOrder != null ? maxPinOrder + 1 : 1;
  }

  /**
   * 构建分页参数
   */
  private RowBounds buildRowBounds(Integer pageNum, Integer pageSize) {
    int num = pageNum != null ? pageNum : DocBaseConsts.DEFAULT_PAGE_NUM;
    int size = pageSize != null ? pageSize : DocBaseConsts.DEFAULT_PAGE_SIZE;
    return new RowBounds((num - 1) * size, size);
  }

  /**
   * 补充收藏列表项的详细信息
   */
  private void enrichFavoriteListItem(FavoriteListDTO item) {
    // 补充创建人信息
    UserInfo creator = new UserInfo();
    creator.setUserId(item.getCreatorUserId());
    if (item.getCreatorUserId() != null) {
      PortalUserDTO userById = dcUserService.findUserById(item.getCreatorUserId());
      if (userById != null) {
        creator.setUsername(userById.getUserName());
      }
    }
    item.setCreator(creator);

    // 设置是否置顶
    item.setIsTopPinned(FavoriteConsts.IS_PINNED_TRUE.equals(item.getIsFavoritePinned()));

    // 根据目标类型补充特定信息
    if (TargetTypeEnum.DOCUMENT.getCode().equals(item.getTargetType())) {
      // 文档类型固定权限为EDIT，并获取文档路径
      item.setPermissions(FavoriteConsts.PERMISSION_EDIT);
      DocumentPathDTO pathDTO = documentService.getDocumentPath(item.getTargetId());
      if (pathDTO != null) {
        item.setResourcePath(pathDTO.getDocumentPath());
        item.setResourcePathCode(pathDTO.getDocumentPathCode());
      }
    }
  }

  /**
   * 补充创建人信息、文档路径、权限等
   */
  private void enrichFavorites(List<FavoritePinnedDTO> favorites) {
    for (FavoritePinnedDTO dto : favorites) {
      UserInfo creator = new UserInfo();
      creator.setUserId(dto.getCreatorUserId());
      if (dto.getCreatorUserId() != null) {
        PortalUserDTO userById = dcUserService.findUserById(dto.getCreatorUserId());
        if (userById != null) {
          creator.setUsername(userById.getUserName());
        }
      }
      dto.setCreator(creator);

      if (TargetTypeEnum.DOCUMENT.getCode().equals(dto.getTargetType())) {
        dto.setPermissions(FavoriteConsts.PERMISSION_EDIT);
        DocumentPathDTO pathDTO = documentService.getDocumentPath(dto.getTargetId());
        if (pathDTO != null) {
          dto.setResourcePath(pathDTO.getDocumentPath());
          dto.setResourcePathCode(pathDTO.getDocumentPathCode());
        }
      }
    }
  }
}


