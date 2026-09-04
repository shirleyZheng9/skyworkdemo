package com.iwhalecloud.bote.doc.module.person.service.impl;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.support.tree.DefaultTreeBuildFactory;
import com.iwhalecloud.bote.doc.common.support.tree.NodeSortHelper;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.module.person.dto.MyDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UserInfo;
import com.iwhalecloud.bote.doc.module.person.dto.MyDocumentQueryParams;
import com.iwhalecloud.bote.doc.module.person.mapper.MyDocumentMapper;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.doc.module.library.service.IDocumentLibraryInitService;
import com.iwhalecloud.bote.doc.module.person.service.IMyDocumentService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 我的文档服务
 *
 * @author yangran
 * @since 2025-08-18
 */
@Service
@RequiredArgsConstructor
public class MyDocumentServiceImpl implements IMyDocumentService {

  private static final Logger logger = LoggerFactory.getLogger(MyDocumentServiceImpl.class);
  private final MyDocumentMapper myDocumentMapper;
  private final IDocumentLibraryInitService documentLibraryInitService;
  private final IDcUserService dcUserService;

  /**
   * 需要过滤如果是非-99的那么就需要筛选只有当前租户的数据
   * @param queryParams 查询条件
   * @return
   */
  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public MyDocumentDTO queryMyDocumentTree(MyDocumentQueryParams queryParams) {
    // 自动初始化用户个人文档库和内置文件夹
    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 确保用户个人文档库和内置文件夹存在
    String libraryId;
    try {
      libraryId = documentLibraryInitService.initializeUserDocumentLibrary(userId, queryParams.getTenantId(),
        queryParams.getSpaceId());
    }
    catch (Exception e) {
      // 如果初始化失败（可能是连接关闭等问题），尝试查询已存在的文档库
      logger.warn("初始化用户文档库失败，尝试查询已存在的文档库: userId={}, tenantId={}, spaceId={}, error={}",
        userId, queryParams.getTenantId(), queryParams.getSpaceId(), e.getMessage(), e);
      libraryId = myDocumentMapper.existsUserDocumentLibrary(userId, queryParams.getSpaceId());
      if (StringUtils.isEmpty(libraryId)) {
        // 如果查询不到已存在的文档库，重新抛出异常
        throw new BssException("文档库初始化失败且未找到已存在的文档库", e);
      }
    }
    queryParams.setLibraryId(libraryId);
    List<MyDocumentDTO> dtoList = myDocumentMapper.selectMyDocumentList(queryParams);

    // 丰富创建人信息
    if (CollectionUtils.isNotEmpty(dtoList)) {
      if (queryParams.getTenantId() != null) {
        // 筛选条件：documentType 不为 FOLDER 和 ROOT 类型的，或者 tenantId 等于入参的 tenantId
        dtoList = dtoList.stream().filter(item ->
          // 条件1：documentType 是 FOLDER 或者是 ROOT
          Objects.equals(DocumentTypeEnum.FOLDER.getCode(), item.getDocumentType())
            || Objects.equals(DocumentTypeEnum.ROOT.getCode(), item.getDocumentType())
            // 或者 条件2：tenantId 等于入参的 tenantId
            || Objects.equals(queryParams.getTenantId(), item.getTenantId())).toList();
      }
      enrichCreatorInfo(dtoList);
    }

    Optional<MyDocumentDTO> first = dtoList.stream()
      .filter(item -> Objects.equals(DocumentTypeEnum.ROOT.getCode(), item.getDocumentType()))
      .findFirst();
    if (!first.isPresent()) {
      // 此处考虑矫正异常数据
      throw new BssException("文档库初始化异常");
    }
    MyDocumentDTO rootNode = first.get();
    // 构建树结构
    List<MyDocumentDTO> nodeTrees = new DefaultTreeBuildFactory<MyDocumentDTO>().doTreeBuild(dtoList);
    // 同级排序
    NodeSortHelper.sortNodeAtSameLevel(nodeTrees);
    // 其他节点应该是root节点的子节点
    return rootNode;
  }

  /**
   * 丰富创建人信息
   * <p>参考 HomepageServiceImpl#enrichRecentFiles 方法的实现，批量查询用户信息避免 N+1 查询问题</p>
   *
   * @param documents 文档列表
   */
  private void enrichCreatorInfo(List<MyDocumentDTO> documents) {
    if (CollectionUtils.isEmpty(documents)) {
      return;
    }
    List<Long> userIds = documents.stream().map(BaseEntity::getCreatorId).toList();
    // 批量查询用户信息
    Map<Long, PortalUserDTO> userMap =  dcUserService.findUserMapBatchByIds(userIds);
    // 填充创建人信息
    for (MyDocumentDTO document : documents) {
      document.setMyDoc(DocBaseConsts.TRUE);
      PortalUserDTO creator = userMap.get(document.getCreatorId());
      if (creator != null) {
        UserInfo creatorInfo = new UserInfo();
        creatorInfo.setUserId(creator.getUserId());
        creatorInfo.setUsername(creator.getUserName());
        document.setCreator(creatorInfo);
      }
    }
  }
}
