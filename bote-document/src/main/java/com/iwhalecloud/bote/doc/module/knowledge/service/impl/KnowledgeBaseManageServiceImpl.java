package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeEnabledTypeDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraKnowledgeBaseRespDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp.KnowledgeGraphResponse;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.KnowledgeGraphClientHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.WeKnoraKnowledgeClientHelper;
import com.iwhalecloud.bote.doc.module.knowledge.cache.KnowledgeCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.FreemarkerUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.utils.KnowledgeClientUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.consts.FavoriteConsts;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.consts.TargetTypeEnum;
import com.iwhalecloud.bote.doc.consts.VisibilityScopeEnum;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocChainConfigParamsDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocChainTopicParamsDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseSimpleDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.RebuildDocumentGroupDTO;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainTopicHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.KnowledgeRebuildHelper;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinResourceRequestDTO;
import com.iwhalecloud.bote.doc.module.knowledge.entity.KnowledgeQueryRecordEntity;
import com.iwhalecloud.bote.doc.enums.DocSequences;
import com.iwhalecloud.bote.doc.integration.PortalOrgIntegration;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.KnowledgeQueryRecordMapper;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeComboboxDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.SimpleKnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.UpdateKnowlegeVisibilityScopeDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocKnowledgeBaseQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentAddParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.KnowledgeComboboxQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.BtDcKbPermissionManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.KnowledgeBaseManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentManageService;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgeBaseManageService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.BtDcKbPermissionHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainConfigHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocchainExtraConfigHelper;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.doc.module.person.service.IFavoriteService;
import com.iwhalecloud.bote.doc.module.person.service.IHomepageService;
import com.iwhalecloud.bote.doc.common.utils.DcIdUtils;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.SimpleKnowledgeDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainExtraCfgDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainExtraDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryDocmentResponse;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 知识库管理服务实现
 *
 * @author auto
 * @since 2024-09-20
 */
@Service
@RequiredArgsConstructor
public class KnowledgeBaseManageServiceImpl implements IKnowledgeBaseManageService {
  private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseManageServiceImpl.class);

  // @formatter:off
  private final KnowledgeBaseManageMapper knowledgeBaseManageMapper;
  private final KnowledgeQueryRecordMapper queryRecordMapper;
  private final DocumentManageMapper documentManageMapper;
  private final BtDcKbPermissionManageMapper btDcKbPermissionManageMapper;
  private final BtDcKbPermissionHelper btDcKbPermissionHelper;
  private final IDocumentManageService documentManageService;
  private final DocChainConfigHelper docChainConfigHelper;
  private final IRefreshCacheService refreshCacheService;
  private final DocchainExtraConfigHelper docchainExtraConfigHelper;
  private final KnowledgeRebuildHelper knowledgeRebuildHelper;
  private final IHomepageService homepageService;
  private final IFavoriteService favoriteService;
  private final IFileStoreService fileStoreService;
  private final IResourceElementService resourceElementService;
  private final ICatalogManageService catalogManageService;
  private final IDcUserService dcUserService;
  private final FileInfoManageMapper fileInfoManageMapper;
  private final KnowledgeCache knowledgeCache;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final PortalOrgIntegration portalOrgIntegration;
  private final DocChainTopicHelper docChainTopicHelper;
  private final ObjectProvider<WeKnoraKnowledgeClientHelper> weKnoraKnowledgeClientHelper;
  private final ObjectProvider<KnowledgeGraphClientHelper> knowledgeGraphClientHelper;
  // @formatter:on

  @Override
  @SuppressWarnings("unchecked")
  public KnowledgeBaseDTO findKnowledgeBase(Long tenantId, Long knowledgeId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.getKnowledgeBase(tenantId, knowledgeId, userId);
    Assert.notNull(knowledge, () -> "知识库不存在: id=" + knowledgeId);
    String permissionType = btDcKbPermissionHelper.queryKnowledgeBasePermissionType(knowledge.getKnowledgeId(), knowledge.getOwnerId());
    Assert.isTrue(!(VisibilityScopeEnum.MEMBERS.getCode().equals(knowledge.getVisibilityScope()) && StringUtils.isEmpty(permissionType)),
      () -> "没有权限访问该知识库！");
    knowledge.setPermissionType((VisibilityScopeEnum.PUBLIC.getCode().equals(knowledge.getVisibilityScope()) && StringUtils.isEmpty(permissionType))
      ? LibraryRoleEnum.READ.getCode()
      : permissionType);
    PortalUserDTO userInfo = dcUserService.findUserById(knowledge.getUpdatorId());
    if (userInfo != null) {
      knowledge.setUpdatorName(userInfo.getUserName());
    }
    CatalogDTO catalog = catalogManageService.getCatalog(tenantId, knowledge.getCatalogItemId());
    if (catalog != null) {
      knowledge.setCatalogName(catalog.getCatalogName());
    }
    if (StringUtils.isNotEmpty(knowledge.getKnowledgeStrategy())) {
      knowledge.setStrategy(JsonUtil.parseJson(knowledge.getKnowledgeStrategy(), new TypeReference<>() {
      }));
    }
    else {
      String template = BaseSystemParameter.DOCCHAIN_TOPIC_API_REQUEST.getValueFromDb();
      Map<String, Object> params = new HashMap<>(4);
      params.put("topicId", knowledge.getKnowledgeId());
      params.put("topicName", knowledge.getKnowledgeName());
      params.put("comment", knowledge.getKnowledgeDesc());
      params.put("operation", "modify");
      String request = FreemarkerUtil.process(template, params);
      Map<String, Object> content = JsonUtil.parseJsonRequired(request, new TypeReference<>() {
      });
      knowledge.setStrategy((Map<String, Object>) content.get("extra"));
    }
    String type = MapUtils.getString(knowledge.getStrategy(), "chat_with_neo4j");
    knowledge.setIsCommonTopic(StringUtils.isEmpty(type) || KnowledgeConsts.TOPIC_TYPE_COMMON.equalsIgnoreCase(type));
    return knowledge;
  }

  @Override
  @Transactional
  public ResultVO<KnowledgeBaseDTO> saveKnowledgeBase(KnowledgeBaseDTO knowledge) {
    // 校验名称唯一性
    if (knowledgeBaseManageMapper.existsKnowledgeBaseName(knowledge)) {
      return BaseErrorConstant.CHECK_NAME.toResult(knowledge.getKnowledgeName());
    }
    if (knowledge.getSpaceId() == null) {
      knowledge.setSpaceId(TenantIdUtil.getSpaceId(knowledge.getTenantId()));
    }
    knowledge.setStatusCd(DocBaseConsts.STATUS_CD_VALID);

    KnowledgeBaseDTO old = knowledge.getKnowledgeId() == null ? null : findKnowledgeBase(knowledge.getTenantId(), knowledge.getKnowledgeId());
    dataProcessing(knowledge, old);
    setFileCount(old, knowledge);
    DataDifference<KnowledgeBaseDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, knowledge, false, knowledge.getTenantId(),
      OperClassEnum.KNOWLEDGE);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    // 处理新增场景，同时带有文档
    addDocument(knowledge);
    // 修改场景，按需重新构建所有文档
    docchainExtraConfigHelper.processRedoDocument(knowledge, old);
    KnowledgeBaseDTO toSaveData = difference.getToSaveData();
    if (knowledge.getIsTopPinned() != null && knowledge.getIsTopPinned()) {
      PinResourceRequestDTO dto = new PinResourceRequestDTO();
      dto.setTargetId(toSaveData.getKbCode());
      dto.setTargetType(FavoriteConsts.TARGET_TYPE_KNOWLEDGE);
      dto.setTenantId(knowledge.getTenantId());
      dto.setSpaceId(knowledge.getSpaceId());
      homepageService.pinResource(dto, SessionUtil.getLoginInfo().getUserId());
    }
    return ResultVO.success(toSaveData);
  }

  private void dataProcessing(KnowledgeBaseDTO knowledge, KnowledgeBaseDTO old) {
    if (old == null) {
      knowledge.setKbCode(DcIdUtils.createKbId());
      knowledge.setOwnerId(SessionUtil.getLoginInfo().getUserId());
      if (StringUtils.isEmpty(knowledge.getVisibilityScope())) {
        knowledge.setVisibilityScope(VisibilityScopeEnum.PUBLIC.getCode());
      }
    }
    if (knowledge.isDocChainType()) {
      if (knowledge.getStrategy() != null) {
        knowledge.setKnowledgeStrategy(JsonUtil.toJsonString(knowledge.getStrategy()));
      }
      if (knowledge.isRelated()) {
        // 需要校验一把主题是否存在
        Assert.notNull(knowledge.getTopicId(), "主题ID不能为空");
        existsTopicId(knowledge.getTenantId(), knowledge.getTopicId());
      }
      else {
        docChainConfigHelper.saveKnowledge(knowledge, old);
      }
      knowledge.setKnowledgeStatus(KnowledgeConsts.KNOWLEDGE_STATUS_PUBLISHED);
    }
  }

  @Override
  @Transactional
  @SuppressWarnings("PMD.GuardLogStatement")
  public ResultVO<KnowledgeBaseDTO> deleteKnowledgeBase(Long tenantId, Long knowledgeId) {
    KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.selectSimpleKnowledgeById(tenantId, knowledgeId);
    Assert.notNull(knowledge, () -> "知识库不存在， knowledgeId=" + knowledgeId);
    String kbPermissionType = btDcKbPermissionHelper.queryKnowledgeBasePermissionType(knowledge.getKnowledgeId(), knowledge.getOwnerId());
    Assert.isTrue(LibraryRoleEnum.MANAGE.getCode().equals(kbPermissionType), () -> "没有知识库的管理权限");

    if (resourceElementService.existsRelatedResource(tenantId, knowledgeId, DataSyncCodeEnum.KNOWLEDGE.getCode())) {
      return ResultVO.fail("数据已存在关联配置数据，不允许删除");
    }
    DocumentQueryParams params = new DocumentQueryParams();
    params.setKnowledgeId(knowledgeId);
    params.setTenantId(tenantId);
    List<DocumentDTO> documents = documentManageMapper.selectDocumentList(params);
    if (CollectionUtils.isNotEmpty(documents)) {
      return ResultVO.fail("存在知识库文档信息，不可删除");
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    /*
     * 知识库相关的权限表也应当设置失效
     */
    homepageService.unpinResourceByTenant(tenantId, knowledge.getKbCode(), TargetTypeEnum.KNOWLEDGE.getCode(),
      SpaceContextHolder.getRequiredSpaceId());
    favoriteService.removeTenantFavorite(tenantId, knowledge.getKbCode(), TargetTypeEnum.KNOWLEDGE.getCode(),
      SpaceContextHolder.getRequiredSpaceId());
    btDcKbPermissionManageMapper.deleteBtDcKbPermissionByKnowledgeId(knowledgeId, userId);
    knowledgeBaseManageMapper.deleteKnowledgeBase(tenantId, knowledgeId, userId);
    if (knowledge.isDocChainType() && knowledge.getTopicId() != null && !knowledge.isRelated()) {
      // 对接 docchain 且非关联方式，才需要删除
      try {
        docChainConfigHelper.deleteKnowledgeBase(tenantId, knowledge.getTopicId());
      }
      catch (Exception e) {
        // 调用接口失败时仍然允许删除
        if (logger.isWarnEnabled()) {
          logger.warn("Failed to delete DocChain knowledge base: tenantId={}, topicId={}, error={}", tenantId,
            knowledge.getTopicId(), e.getMessage());
        }
      }
    }
    ResourceElementFactory.get(OperClassEnum.KNOWLEDGE.name()).clear(tenantId, knowledgeId);
    return ResultVO.success();
  }

  @Transactional
  @Override
  public ResultVO<String> rebuild(Long tenantId, Long knowledgeId, Boolean newDoc) {
    KnowledgeBaseDTO dto = findKnowledgeBase(tenantId, knowledgeId);
    validateRebuildPermission(dto);

    Long topicId = rebuildKnowledgeBase(dto);

    if (shouldProcessDocuments(dto)) {
      return ResultVO.success(processDocumentRebuild(dto, topicId, newDoc));
    }
    else {
      return ResultVO.success("知识更新学习中！");
    }
  }

  /**
   * 校验主题id是否存在
   *
   * @param tenantId 租户id
   * @param topicId 主题id
   */
  public void existsTopicId(Long tenantId, Long topicId) {
    docChainConfigHelper.existsTopicId(tenantId, topicId);
  }
  /**
   * 验证重构权限
   */
  private void validateRebuildPermission(KnowledgeBaseDTO dto) {
    String kbPermissionType = btDcKbPermissionHelper.queryKnowledgeBasePermissionType(dto.getKnowledgeId(), dto.getOwnerId());
    Assert.isTrue(LibraryRoleEnum.MANAGE.getCode().equals(kbPermissionType) || LibraryRoleEnum.EDIT.getCode().equals(kbPermissionType),
      () -> "没有知识库的编辑权限");
  }

  /**
   * 重构知识库
   */
  private Long rebuildKnowledgeBase(KnowledgeBaseDTO dto) {
    Long topicId = docChainConfigHelper.rebuildKnowledge(dto);
    dto.setTopicId(topicId);
    dto.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    knowledgeBaseManageMapper.updateKnowledgeForRebuild(dto);
    return topicId;
  }

  /**
   * 判断是否需要处理文档
   */
  private boolean shouldProcessDocuments(KnowledgeBaseDTO dto) {
    return !dto.isRelated() && dto.isDocChainType();
  }

  /**
   * 处理文档重构
   */
  private String processDocumentRebuild(KnowledgeBaseDTO dto, Long topicId, Boolean newDoc) {
    List<DocumentDTO> documents = getDocumentsForRebuild(dto);
    if (CollectionUtils.isEmpty(documents)) {
      return "请先添加知识";
    }

    RebuildDocumentGroupDTO groups = categorizeDocuments(documents);
    String resultStr = "";
    if (Boolean.TRUE.equals(newDoc)) {
      resultStr = knowledgeRebuildHelper.processNewDocRebuild(dto, topicId, groups);
    }
    else {
      resultStr = knowledgeRebuildHelper.processOldDocRebuild(dto, topicId, documents);
    }
    return resultStr;
  }

  /**
   * 获取需要重构的文档列表
   */
  private List<DocumentDTO> getDocumentsForRebuild(KnowledgeBaseDTO dto) {
    DocumentQueryParams params = new DocumentQueryParams();
    params.setTenantId(dto.getTenantId());
    params.setKnowledgeId(dto.getKnowledgeId());
    return documentManageMapper.selectDocumentList(params);
  }

  /**
   * 对文档进行分类
   */
  private RebuildDocumentGroupDTO categorizeDocuments(List<DocumentDTO> documents) {
    RebuildDocumentGroupDTO groups = new RebuildDocumentGroupDTO();
    groups.setNewDocuments(new ArrayList<>());
    groups.setOldDocuments(new ArrayList<>());
    documents.forEach(item -> {
      if (StringUtils.isNotEmpty(item.getDcDocumentId())) {
        groups.getNewDocuments().add(item);
      }
      else {
        groups.getOldDocuments().add(item);
      }
    });
    return groups;
  }

  @Override
  public List<SimpleKnowledgeBaseDTO> queryKnowledgeBaseList(DocKnowledgeBaseQueryParams queryParams) {
    // 在这里设置登陆人和部门
    setParamDocKnowledgeBaseQueryParamsUserInfo(queryParams, false);
    queryParams.setSuperAdmin(SessionUtil.isSuperAdmin(queryParams.getUserId()));
    return knowledgeBaseManageMapper.selectKnowledgeBaseList(queryParams);
  }

  /**
   * 在ai门户：传递的租户id和目录特殊处理：-99和-99组合是企业级知识库查询：-88和-99是项目级知识库查询；
   *
   * @param queryParams 查询条件
   */
  @Override
  public PageInfo<KnowledgeBaseDTO> queryKnowledgeBasePage(DocKnowledgeBaseQueryParams queryParams) {
    // 设置用户信息
    setParamDocKnowledgeBaseQueryParamsUserInfo(queryParams, false);
    queryParams.setPinTenantId(queryParams.getTenantId());
    // 处理租户和目录参数
    processTenantAndCatalogParams(queryParams);

    // 设置目录列表和超级管理员标识
    setupCatalogListAndSuperAdmin(queryParams);

    // 查询分页数据
    RowBounds rowBounds = queryParams.buildRowBounds();
    PageInfo<KnowledgeBaseDTO> pageInfo = knowledgeBaseManageMapper.selectKnowledgeBasePage(queryParams, rowBounds).toPageInfo();

    // 填充附加信息
    enrichPageInfo(pageInfo, queryParams);

    return pageInfo;
  }

  @Override
  public PageInfo<KnowledgeBaseSimpleDTO> queryKnowledgeBasePageByType(DocKnowledgeBaseQueryParams queryParams) {
    String knowledgeType = queryParams.getKnowledgeType();
    if (StringUtils.isEmpty(knowledgeType) || "docChain".equals(knowledgeType)) {
      return convertToSimplePageInfo(queryKnowledgeBasePage(queryParams));
    }
    if ("weknora".equals(knowledgeType)) {
      return queryWeKnoraKnowledgeBasePage(queryParams);
    }
    if (KnowledgeConsts.KNOWLEDGE_TYPE_KNOWLEDGE_GRAPH.equals(knowledgeType)) {
      return queryKnowledgeGraphKnowledgeBasePage(queryParams);
    }
    return new PageInfo<>();
  }

  /**
   * 将知识库分页信息转换为简单知识库分页信息
   */
  private PageInfo<KnowledgeBaseSimpleDTO> convertToSimplePageInfo(PageInfo<KnowledgeBaseDTO> knowledgeBasePageInfo) {
    PageInfo<KnowledgeBaseSimpleDTO> pageInfo = new PageInfo<>();
    if (knowledgeBasePageInfo == null || CollectionUtils.isEmpty(knowledgeBasePageInfo.getList())) {
      pageInfo.setList(Collections.emptyList());
      return pageInfo;
    }
    // 转换为简单知识库DTO列表
    List<KnowledgeBaseSimpleDTO> simpleList = knowledgeBasePageInfo.getList().stream()
      .map(this::convertToSimpleDTO)
      .collect(Collectors.toList());
    pageInfo.setList(simpleList);
    pageInfo.setTotal(knowledgeBasePageInfo.getTotal());
    pageInfo.setPageNum(knowledgeBasePageInfo.getPageNum());
    pageInfo.setPageSize(knowledgeBasePageInfo.getPageSize());
    return pageInfo;
  }

  /**
   * 将知识库转换为简单知识库DTO
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private KnowledgeBaseSimpleDTO convertToSimpleDTO(KnowledgeBaseDTO knowledgeBase) {
    KnowledgeBaseSimpleDTO simpleDTO = new KnowledgeBaseSimpleDTO();
    simpleDTO.setKnowledgeId(String.valueOf(knowledgeBase.getKnowledgeId()));
    simpleDTO.setKnowledgeName(knowledgeBase.getKnowledgeName());
    simpleDTO.setKnowledgeType(knowledgeBase.getKnowledgeType());
    simpleDTO.setKnowledgeIcon(knowledgeBase.getKnowledgeIcon());
    simpleDTO.setKnowledgeStatus(knowledgeBase.getKnowledgeStatus());
    simpleDTO.setFileCounts(knowledgeBase.getFileCounts());
    simpleDTO.setKnowledgeDesc(knowledgeBase.getKnowledgeDesc());
    return simpleDTO;
  }

  /**
   * 查询WeKnora知识库分页信息
   */
  private PageInfo<KnowledgeBaseSimpleDTO> queryWeKnoraKnowledgeBasePage(DocKnowledgeBaseQueryParams queryParams) {
    WeKnoraKnowledgeClientHelper helper = weKnoraKnowledgeClientHelper.getIfAvailable();
    if (helper == null) {
      throw new BssException("WeKnora 未启用，请联系管理员");
    }
    List<WeKnoraKnowledgeBaseRespDTO> weKnoraKnowledgeBases = helper.listKnowledgeBases(queryParams.getTenantId());
    List<KnowledgeBaseSimpleDTO> knowledgeBases = weKnoraKnowledgeBases.stream()
      .map(this::convertWeKnoraToSimpleDTO)
      .collect(Collectors.toList());
    return buildPageInfo(knowledgeBases, queryParams.getPageNum(), queryParams.getPageSize());
  }

  /**
   * 查询 knowledgeGraph 知识库分页信息
   */
  private PageInfo<KnowledgeBaseSimpleDTO> queryKnowledgeGraphKnowledgeBasePage(DocKnowledgeBaseQueryParams queryParams) {
    KnowledgeGraphClientHelper helper = knowledgeGraphClientHelper.getIfAvailable();
    if (helper == null) {
      throw new BssException("knowledgeGraph 未启用，请联系管理员");
    }
    List<KnowledgeGraphResponse> tasks = helper.queryKnowledgeList(queryParams.getTenantId(), -1, queryParams.getSearchContent());
    List<KnowledgeBaseSimpleDTO> knowledgeBases = tasks.stream()
      .map(this::convertKnowledgeGraphToSimpleDTO)
      .toList();
    return buildPageInfo(knowledgeBases, queryParams.getPageNum(), queryParams.getPageSize());
  }

  /**
   * 将 knowledgeGraph 任务对象转换为统一知识库简版 DTO
   */
  private KnowledgeBaseSimpleDTO convertKnowledgeGraphToSimpleDTO(KnowledgeGraphResponse task) {
    KnowledgeBaseSimpleDTO simpleDTO = new KnowledgeBaseSimpleDTO();
    simpleDTO.setKnowledgeId(StringUtils.defaultString(task.getDatabase()));
    simpleDTO.setKnowledgeName(StringUtils.defaultString(task.getDatabase()));
    simpleDTO.setKnowledgeType(KnowledgeConsts.KNOWLEDGE_TYPE_KNOWLEDGE_GRAPH);
    return simpleDTO;
  }

  /**
   * 将WeKnora知识库转换为简单知识库DTO
   */
  private KnowledgeBaseSimpleDTO convertWeKnoraToSimpleDTO(WeKnoraKnowledgeBaseRespDTO weKnoraKnowledgeBase) {
    KnowledgeBaseSimpleDTO simpleDTO = new KnowledgeBaseSimpleDTO();
    simpleDTO.setKnowledgeId(weKnoraKnowledgeBase.getId());
    simpleDTO.setKnowledgeName(weKnoraKnowledgeBase.getName());
    simpleDTO.setKnowledgeType("weKnora");
    simpleDTO.setFileCounts(weKnoraKnowledgeBase.getKnowledgeCount());
    simpleDTO.setKnowledgeDesc(weKnoraKnowledgeBase.getDescription());
    return simpleDTO;
  }

  /**
   * 构建分页信息
   */
  private PageInfo<KnowledgeBaseSimpleDTO> buildPageInfo(List<KnowledgeBaseSimpleDTO> allData, int pageNum, int pageSize) {
    PageInfo<KnowledgeBaseSimpleDTO> pageInfo = new PageInfo<>();
    int total = allData.size();
    int fromIndex = Math.max(0, (pageNum - 1) * pageSize);
    int toIndex = Math.min(fromIndex + pageSize, total);
    if (fromIndex >= total) {
      pageInfo.setList(Collections.emptyList());
    } else {
      pageInfo.setList(allData.subList(fromIndex, toIndex));
    }
    pageInfo.setTotal(total);
    pageInfo.setPageNum(pageNum);
    pageInfo.setPageSize(pageSize);
    return pageInfo;
  }

  /**
   * 处理租户和目录参数
   */
  private void processTenantAndCatalogParams(DocKnowledgeBaseQueryParams queryParams) {
    if (StringUtils.isEmpty(queryParams.getEnterprise())) {
      return;
    }
    Long spaceTenantId = TenantIdUtil.getSpaceTenantId(queryParams.getSpaceId());
    if (queryParams.getTenantId() != null && spaceTenantId.equals(queryParams.getTenantId())) {
      if (CommonConsts.TRUE.equals(queryParams.getEnterprise())) {
        queryParams.setCatalogItemId(DocBaseConsts.BUSINESS_PAR_CATALOG_ID);
      }
      else {
        queryParams.setCatalogItemId(DocBaseConsts.BUSINESS_PAR_CATALOG_ID_PROJECT);
      }
      handleBusinessTenantParams(queryParams, spaceTenantId);
    }
    else {
      handleProjectTenantParams(queryParams);
    }
  }

  /**
   * 处理企业租户参数
   */
  private void handleBusinessTenantParams(DocKnowledgeBaseQueryParams queryParams, Long spaceTenantId) {
    queryParams.setTenantId(null);
    if (DocBaseConsts.BUSINESS_PAR_CATALOG_ID.equals(queryParams.getCatalogItemId())) {
      // 说明是企业级的
      queryParams.setTenantId(spaceTenantId);
      queryParams.setCatalogItemId(null);
    }
    if (DocBaseConsts.BUSINESS_PAR_CATALOG_ID_PROJECT.equals(queryParams.getCatalogItemId())) {
      // 说明是项目级的
      queryParams.setUnTenantId(spaceTenantId);
      queryParams.setCatalogItemId(null);
    }
  }

  /**
   * 处理项目租户参数
   */
  private void handleProjectTenantParams(DocKnowledgeBaseQueryParams queryParams) {
    if (queryParams.getCatalogItemId() != null && queryParams.getCatalogItemId() < 0) {
      // 某个项目的
      queryParams.setCatalogItemId(null);
    }
  }

  /**
   * 设置目录列表和超级管理员标识
   */
  private void setupCatalogListAndSuperAdmin(DocKnowledgeBaseQueryParams params) {
    params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getSpaceId(), params.getCatalogItemId(),
      DocBaseConsts.CATALOG_TYPE_KNOWLEDGE));
    params.setSuperAdmin(SessionUtil.isSuperAdmin(params.getUserId()));
  }

  /**
   * 填充分页信息的附加数据（用户信息和权限信息）
   */
  private void enrichPageInfo(PageInfo<KnowledgeBaseDTO> pageInfo, DocKnowledgeBaseQueryParams queryParams) {
    if (pageInfo == null || pageInfo.getList().isEmpty()) {
      return;
    }

    // 批量查询用户信息
    List<Long> ownerIds = pageInfo.getList().stream().map(KnowledgeBaseDTO::getOwnerId).distinct().collect(Collectors.toList());
    Map<Long, PortalUserDTO> userMap = dcUserService.findUserMapBatchByIds(ownerIds);

    // 获取权限信息
    boolean isSuperAdmin = Boolean.TRUE.equals(queryParams.getSuperAdmin());
    Map<Long, String> permissionMap = getPermissionMap(pageInfo, queryParams, isSuperAdmin);

    // 设置用户信息和权限信息
    setOwnerAndPermissionInfo(pageInfo.getList(), userMap, permissionMap, isSuperAdmin);
  }

  /**
   * 获取权限映射
   */
  private Map<Long, String> getPermissionMap(PageInfo<KnowledgeBaseDTO> pageInfo, DocKnowledgeBaseQueryParams queryParams, boolean isSuperAdmin) {
    if (isSuperAdmin) {
      return null;
    }
    return btDcKbPermissionHelper.batchQueryKnowledgeBasePermissionType(pageInfo.getList(), queryParams.getUserId(), queryParams.getDeptIds());
  }

  /**
   * 设置所有者和权限信息
   */
  private void setOwnerAndPermissionInfo(List<KnowledgeBaseDTO> knowledgeList, Map<Long, PortalUserDTO> userMap, Map<Long, String> permissionMap,
    boolean isSuperAdmin) {
    for (KnowledgeBaseDTO item : knowledgeList) {
      // 设置用户信息
      PortalUserDTO userInfo = userMap.get(item.getOwnerId());
      if (userInfo != null) {
        item.setOwnerName(userInfo.getUserName());
      }
      // 设置权限信息
      String permission = isSuperAdmin ? LibraryRoleEnum.MANAGE.getCode() : permissionMap.get(item.getKnowledgeId());
      item.setPermissionType(permission);
    }
  }

  @Override
  public PageInfo<SimpleKnowledgeBaseDTO> querySimpleKnowledgeBasePage(DocKnowledgeBaseQueryParams params) {
    // 在这里设置登陆人和部门
    setParamDocKnowledgeBaseQueryParamsUserInfo(params, false);
    params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getSpaceId(), params.getCatalogItemId(),
      DocBaseConsts.CATALOG_TYPE_KNOWLEDGE));
    // noinspection resource
    return knowledgeBaseManageMapper.selectSimpleKnowledgeBasePage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public PageInfo<KnowledgeComboboxDTO> queryKnowledgeComboboxPage(KnowledgeComboboxQueryParams queryParams) {
    // 在这里设置登陆人和部门
    queryParams.setUserId(SessionUtil.getLoginInfo().getUserId());

    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    List<OrgDTO> orgList = portalOrgIntegration.queryUserOrgList(spaceId, queryParams.getUserId());
    if (CollectionUtils.isNotEmpty(orgList)) {
      queryParams.setDeptIds(orgList.stream().map(OrgDTO::getOrgId).collect(Collectors.toList()));
    }
    queryParams.setSuperAdmin(SessionUtil.isSuperAdmin(queryParams.getUserId()));
    // noinspection resource
    return knowledgeBaseManageMapper.selectKnowledgeComboboxPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  public List<String> queryLatestQueryRecords(Long tenantId, Long knowledgeId, String querySource) {
    // 查询最近 10 条不重复的问题
    // content 字段使用了 text/clob 类型，Oracle 不支持对 clob 做 group by/distinct, 只能在应用层处理
    int total = queryRecordMapper.countQueryRecords(tenantId, knowledgeId, querySource);
    if (total == 0) {
      return Collections.emptyList();
    }
    int expectedCount = 10;
    int pageSize = 50;
    // 问题集合，去重
    Set<String> questions = new LinkedHashSet<>();
    ISelect select = () -> queryRecordMapper.selectQueryContentList(tenantId, knowledgeId, querySource);
    for (int offset = 0; offset < total; offset += pageSize) {
      try (Page<Object> page = PageHelper.offsetPage(offset, pageSize, false)) {
        List<String> queryContentList = page.doSelectPage(select);
        for (String item : queryContentList) {
          if (questions.add(item) && questions.size() >= expectedCount) {
            break;
          }
        }
      }
    }
    return new ArrayList<>(questions);
  }

  @Override
  public KnowledgeRecallResponse testRecall(KnowledgeRecallParamDTO params) {
    SimpleKnowledgeDTO knowledge = knowledgeCache.get(params.getTenantId(), params.getKnowledgeId());
    Assert.notNull(knowledge, "知识库不存在");
    // 记录查询记录
    addKnowledgeQueryRecord(params.getTenantId(), params.getKnowledgeId(), KnowledgeConsts.QUERY_SOURCE_RECALL_TEST, params.getQuery());
    params.setKnowledgeList(Collections.singletonList(knowledge));
    Pair<List<String>, List<String>> topicAndDocIds = btDcKbPermissionHelper.getTopicAndDocIds(params.getKnowledgeList(), params.getDocumentIds(), params.getTenantId());
    params.setTopicIds(topicAndDocIds.getLeft());
    params.setDocIds(topicAndDocIds.getRight());
    KnowledgeClient knowledgeClient = KnowledgeClientUtil.getClient(knowledge.getKnowledgeType());
    return knowledgeClient.recall(params);
  }

  @Override
  public KnowledgeRecallResponse docChainBatchRecall(KnowledgeRecallParamDTO params) {
    // todo 补充批量recall逻辑
    return null;
  }

  @Override
  public void addKnowledgeQueryRecord(Long tenantId, Long knowledgeId, String source, String query) {
    KnowledgeQueryRecordEntity queryRecord = new KnowledgeQueryRecordEntity();
    queryRecord.setId(DocSequences.KNOWLEDGE_QUERY_RECORD_ID.next());
    queryRecord.setKnowledgeId(knowledgeId);
    queryRecord.setQuerySource(source);
    queryRecord.setContent(query);
    queryRecord.setTenantId(tenantId);
    queryRecord.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    TransactionUtil.execute(() -> queryRecordMapper.insertQueryRecord(queryRecord));
  }

  @Override
  public ResultVO<Pair<Long, Boolean>> buildKnowledgeByFileId(Long tenantId, Long fileId, int maxWaitTime) {
    ResultVO<Long> result = addKnowledgeByFileId(tenantId, fileId);
    if (!result.isSuccess()) {
      return ResultVO.fail(result.getResultMsg());
    }
    Long knowledgeId = result.getResultObject();
    boolean isFinished = documentManageService.waitBuildDocumentFinish(tenantId, knowledgeId, maxWaitTime);
    return ResultVO.success(Pair.of(knowledgeId, isFinished));
  }

  @Override
  public void clearTemporaryKnowledge() {
    // 删除 20 分钟前创建的临时知识库
    Date maxCreatedTime = DateUtils.addMinutes(new Date(), -20);
    List<DocumentDTO> documents = documentManageMapper.selectDocumentForClear(maxCreatedTime);
    if (documents.isEmpty()) {
      return;
    }
    // 按知识库 ID 分组
    Map<Long, List<DocumentDTO>> documentsGroup = documents.stream().collect(Collectors.groupingBy(DocumentDTO::getKnowledgeId));
    RuntimeException lastException = null;
    for (Entry<Long, List<DocumentDTO>> entry : documentsGroup.entrySet()) {
      Long knowledgeId = entry.getKey();
      Long tenantId = entry.getValue().get(0).getTenantId();
      Long topicId = entry.getValue().get(0).getTopicId();
      // 不使用事务，采用默认的自动提交方式
      // 忽略异常，避免处理一个知识库失败影响其它知识库
      try {
        for (DocumentDTO document : entry.getValue()) {
          // 删除 DocChain 中的文档
          docChainConfigHelper.deleteDocument(tenantId, document.getExtSystemId());
          documentManageMapper.deleteDocument(tenantId, document.getDocumentId(), 1L);
          // 清理关联的文件信息
          if (document.getFileInfoId() != null) {
            fileInfoManageMapper.deleteFileInfo(tenantId, document.getFileInfoId(), 1L);
          }
          if (document.getFileId() != null) {
            fileStoreService.deleteFileQuietly(document.getFileId());
          }
        }
        // 删除 DocChain 中的主题
        docChainConfigHelper.deleteKnowledgeBase(tenantId, topicId);
        knowledgeBaseManageMapper.deleteKnowledgeBase(tenantId, knowledgeId, 1L);
      }
      catch (RuntimeException e) {
        lastException = e;
        if (logger.isErrorEnabled()) {
          logger.error("Failed to clear temporary knowledge: tenantId={}, knowledgeId={}, topicId={}", tenantId,
            knowledgeId, topicId, e);
        }
      }
    }
    // 确保定时任务日志能正确记录失败
    if (lastException != null) {
      throw lastException;
    }
  }

  @Override
  public List<DocChainExtraDTO> getDocChainExtraConfig() {
    return docchainExtraConfigHelper.getDocChainExtraConfig();
  }

  @Override
  public ResultVO<KnowledgeInfoDTO> getTenantKnowledgeConfig(Long tenantId) {
    return ResultVO.success(tenantSettingInfoCache.getKnowledgeInfo(tenantId));
  }

  @Override
  public ResultVO<KnowledgeEnabledTypeDTO> getKnowledgeEnabledType() {
    KnowledgeEnabledTypeDTO knowledgeEnabledType = new KnowledgeEnabledTypeDTO();
    knowledgeEnabledType.setDocChainEnabled(Boolean.parseBoolean(BaseSystemParameter.ENABLE_DOCCHAIN.getValueFromEnv()));
    knowledgeEnabledType.setWeKnoraEnabled(Boolean.parseBoolean(BaseSystemParameter.ENABLE_WEKNORA.getValueFromEnv()));
    knowledgeEnabledType.setKnowledgeGraphEnabled(Boolean.parseBoolean(BaseSystemParameter.ENABLE_KNOWLEDGE_GRAPH.getValueFromEnv()));
    return ResultVO.success(knowledgeEnabledType);
  }

  @Transactional
  @Override
  public ResultVO<Void> updateKonwledgeVisibilityScope(UpdateKnowlegeVisibilityScopeDTO params) {
    KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.selectSimpleKnowledgeById(params.getTenantId(), params.getKnowledgeId());
    Assert.notNull(knowledge, () -> "知识库不存在， knowledgeId=" + params.getKnowledgeId());
    params.setUserId(SessionUtil.getLoginInfo().getUserId());
    String kbPermissionType = btDcKbPermissionHelper.queryKnowledgeBasePermissionType(knowledge.getKnowledgeId(), knowledge.getOwnerId());
    Assert.isTrue(LibraryRoleEnum.MANAGE.getCode().equals(kbPermissionType), () -> "没有知识库的管理权限");
    knowledgeBaseManageMapper.updateKnowledgeBaseVisibilityScope(params);
    return ResultVO.success();
  }

  /**
   * 查询知识飞轮的知识库
   */
  @Override
  public List<SimpleKnowledgeBaseDTO> querySimpleKnowledgeBaseList(DocKnowledgeBaseQueryParams queryParams) {
    // 在这里设置登陆人和部门和是否为ai门户进来的
    setParamDocKnowledgeBaseQueryParamsUserInfo(queryParams, true);
    queryParams.setSuperAdmin(SessionUtil.isSuperAdmin(queryParams.getUserId()));
    List<SimpleKnowledgeBaseDTO> simpleKnowledgeBaseDTOS = knowledgeBaseManageMapper.selectKnowledgeBaseList(queryParams);
    if (CollectionUtils.isNotEmpty(simpleKnowledgeBaseDTOS)) {
      // 统一设置用户信息和权限信息
      boolean isSuperAdmin = Boolean.TRUE.equals(queryParams.getSuperAdmin());
      // 批量查询权限信息（非超级管理员）
      if (!isSuperAdmin) {
        Map<Long, String> permissionMap = btDcKbPermissionHelper.batchQuerySimpleKnowledgeBasePermissionType(simpleKnowledgeBaseDTOS,
          queryParams.getUserId(), queryParams.getDeptIds());
        simpleKnowledgeBaseDTOS = simpleKnowledgeBaseDTOS.stream()
          .filter(i -> LibraryRoleEnum.MANAGE.getCode().equals(permissionMap.get(i.getKnowledgeId()))).toList();
      }
    }
    return simpleKnowledgeBaseDTOS;
  }

  /**
   * 重新计算知识下的文档数量
   */
  private void setFileCount(KnowledgeBaseDTO old, KnowledgeBaseDTO knowledge) {
    long fileCounts = ObjectUtils.getIfNull(knowledge.getFileCounts(), 0L);
    if (knowledge.isDocChainType()) {
      if (knowledge.isRelated()) {
        // 关联类型，实时调用 docchain 接口获取文档数量
        QueryDocmentResponse.DocmentPageInfo documents = docChainConfigHelper.queryDocumentPage(knowledge.getTenantId(), knowledge.getTopicId(), null, 1, 10);
        if (documents != null) {
          fileCounts = documents.getTotal();
        }
      }
      else if (old != null) {
        fileCounts = documentManageMapper.countDocuments(knowledge.getTenantId(), knowledge.getKnowledgeId());
      }
    }
    knowledge.setFileCounts(fileCounts);
  }

  private void addDocument(KnowledgeBaseDTO knowledge) {
    if (!knowledge.isDocChainType() || knowledge.isRelated() || (CollectionUtils.isEmpty(knowledge.getFileInfoIds()) && CollectionUtils.isEmpty(
      knowledge.getStructFileInfoIds()))) {
      return;
    }
    DocumentAddParams addParams = new DocumentAddParams();
    addParams.setTenantId(knowledge.getTenantId());
    addParams.setKnowledgeId(knowledge.getKnowledgeId());
    addParams.setFileInfoIds(knowledge.getFileInfoIds());
    addParams.setStructFileInfoIds(knowledge.getStructFileInfoIds());
    addParams.setSpaceId(knowledge.getSpaceId());
    documentManageService.addDocument(addParams);
  }

  /**
   * 设置查询参数中的用户信息
   */
  private void setParamDocKnowledgeBaseQueryParamsUserInfo(DocKnowledgeBaseQueryParams queryParams, boolean ignore) {
    // 在这里设置登陆人和部门
    queryParams.setUserId(SessionUtil.getLoginInfo().getUserId());
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    List<OrgDTO> orgList = portalOrgIntegration.queryUserOrgList(spaceId, queryParams.getUserId());
    queryParams.setDeptIds(CollectionUtils.emptyIfNull(orgList).stream().map(OrgDTO::getOrgId).collect(Collectors.toList()));
    if (ignore && DocBaseConsts.AI_PORTAL.equals(queryParams.getPlatform())) {
      queryParams.setTenantId(null);
    }
  }

  private ResultVO<Long> addKnowledgeByFileId(Long tenantId, Long fileId) {
    FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
    Assert.notNull(fileInfo, "查询不到有效的文件信息");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 构造关联文件信息
    FileInfoDTO file = new FileInfoDTO();
    file.setFileInfoId(DocSequences.FILE_INFO_ID.next());
    file.setFileId(fileId);
    file.setFileName(fileInfo.getFileName());
    file.setTenantId(tenantId);
    file.setBusiType(DocBaseConsts.FILE_BUSI_TYPE_DOCUMENT);
    file.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    file.setCreatedTime(new Date());
    file.setCatalogItemId(DocBaseConsts.DEFAULT_CATALOG_ITEM_PARENT_ID);
    file.setCreatorId(userId);
    file.setUpdatorId(userId);

    // 构造知识库信息
    KnowledgeBaseDTO knowledge = new KnowledgeBaseDTO();
    knowledge.setTenantId(tenantId);
    knowledge.setCatalogItemId(DocBaseConsts.DEFAULT_CATALOG_ITEM_PARENT_ID);
    knowledge.setKnowledgeName(DateUtil.format(DateUtil.DATETIME_COMPACT) + "-" + fileId);
    knowledge.setKnowledgeIcon("./images/avatar/img-Knowledge-avatar-1.png");
    knowledge.setIsExist(DocBaseConsts.FALSE);
    knowledge.setIsTemporary(DocBaseConsts.TRUE);
    knowledge.setKnowledgeType(KnowledgeConsts.KNOWLEDGE_TYPE_DOC_CHAIN);
    String fileType = FilenameUtils.getExtension(file.getFileName());
    if (isFileTypeAllowed(fileType, BaseSystemParameter.CONVERT_TO_ONLINE_EXCEL_TYPE.getValueFromDb())) {
      knowledge.setKnowledgeStrategy(BaseSystemParameter.DOCCHAIN_CHATEXCEL_STRATEGY.getValueFromDb());
    }
    knowledge.setFileInfoIds(Collections.singletonList(file.getFileInfoId()));

    // 创建 DocChain 账号
    if (docChainConfigHelper.createDocChainAccount(tenantId)) {
      refreshCacheService.refresh(DocCacheConsts.CACHE_NAME_TENANT_SETTING, tenantId.toString());
    }

    // 独立提交事务，避免后边的查询文档构建进度出现超时
    ResultVO<Long> result = ResultVO.success();
    TransactionUtil.executeNew(() -> {
      fileInfoManageMapper.insertFileInfo(file);
      ResultVO<KnowledgeBaseDTO> re = saveKnowledgeBase(knowledge);
      if (re.isSuccess()) {
        result.setResultObject(re.getResultObject().getKnowledgeId());
      }
      else {
        result.setResultCode("-1");
        result.setResultMsg(re.getResultMsg());
      }
    });
    return result;
  }

  private boolean isFileTypeAllowed(String fileType, String allowTypesFromDb) {
    String[] allowFileTypes = allowTypesFromDb.split(",");
    return ArrayUtils.contains(allowFileTypes, fileType);
  }

  @Override
  public Map<String, Object> queryDynamicConfigParams(Long tenantId) {
    // 获取docchain主题配置参数
    DocChainTopicParamsDTO topicConfigParams = docChainTopicHelper.getDocchainTopicConfigParams(tenantId);
    if (topicConfigParams == null) {
      return Collections.emptyMap();
    }
    List<DocChainExtraCfgDTO> docChainExtraCfgs = knowledgeBaseManageMapper.selectDynamicDocChainExtraCfg();
    if (CollectionUtils.isEmpty(docChainExtraCfgs)) {
      return Collections.emptyMap();
    }
    // 获取全部配置项
    Map<String, Object> result = new HashMap<>();
    List<DocChainConfigParamsDTO> allConfigs = topicConfigParams.getAllConfig();
    // 遍历docchain全部配置项，获取对应的动态配置项的值
    for (DocChainConfigParamsDTO config : allConfigs) {
      DocChainExtraCfgDTO docChainExtraCfg = IterableUtils.find(docChainExtraCfgs,
        p -> p.getCode().equals(config.getKey()));
      if (docChainExtraCfg != null) {
        result.put(config.getKey(), config.getValues() != null ? config.getValues() : null);
      }
    }
    return result;
  }
}
