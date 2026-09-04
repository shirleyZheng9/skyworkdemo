package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.ThreadContextRunner;
import com.iwhalecloud.bote.doc.common.utils.DcIdUtils;
import com.iwhalecloud.bote.doc.consts.ContentSourceEnum;
import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.consts.DocStatusEnum;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.consts.VisibilityScopeEnum;
import com.iwhalecloud.bote.doc.enums.DocSequences;
import com.iwhalecloud.bote.doc.integration.PortalOrgIntegration;
import com.iwhalecloud.bote.doc.module.control.base.ControlRoleDict;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentCreateRequestDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPathDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionDTO;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentPermissionMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.knowledge.dto.AddDocumentResp;
import com.iwhalecloud.bote.doc.module.knowledge.dto.CatalogStructureDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocDeleteDocumentsDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.ProcessTenantResultDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentAddParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.KnowledgeBaseManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.service.IBtDcQaRecordManageService;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentManageService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.BtDcKbPermissionHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainConfigHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainDocumentHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocumentHelper;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.entity.DocumentLibraryEntity;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryService;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.query.CatalogQueryParams;
import com.iwhalecloud.bote.dto.base.query.FileInfoQueryParams;
import com.iwhalecloud.bote.dto.knowledge.SimpleDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryDocmentResponse;
import com.iwhalecloud.bote.dto.portal.SimpleTenantDTO;
import com.iwhalecloud.bote.mapper.base.CatalogManageMapper;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.mapper.portal.TenantQueryMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static java.util.stream.Collectors.toList;

/**
 * 文档管理服务实现
 *
 * @author auto
 * @since 2024-09-20
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DocumentManageServiceImpl implements IDocumentManageService {
  // @formatter:off
  private final Logger logger = LoggerFactory.getLogger(DocumentManageServiceImpl.class);
  private final DocumentManageMapper documentManageMapper;
  private final KnowledgeBaseManageMapper knowledgeBaseManageMapper;
  private final DocChainConfigHelper docChainConfigHelper;
  private final BtDcKbPermissionHelper btDcKbPermissionHelper;
  private final DocChainDocumentHelper docChainDocumentHelper;
  private final JdbcTemplate jdbcTemplate;
  private final IDocumentService documentService;
  private final ControlTemplate controlTemplate;
  private final ICatalogManageService catalogManageService;
  private final PortalOrgIntegration portalOrgIntegration;
  private final IRefreshCacheService refreshCacheService;
  private final FileInfoManageMapper fileInfoManageMapper;
  private final DocumentLibraryService documentLibraryService;
  private final DocumentPermissionMapper documentPermissionMapper;
  private final IBtDcQaRecordManageService btDcQaRecordManageService;
  private final DocumentHelper documentHelper;
  private final TenantQueryMapper tenantQueryMapper;
  private final CatalogManageMapper catalogManageMapper;

  // @formatter:on

  private static List<DocumentDTO> getDocuments(List<QueryDocmentResponse.DocmentInfo> list) {
    List<DocumentDTO> documents = new ArrayList<>();
    for (QueryDocmentResponse.DocmentInfo doc : CollectionUtils.emptyIfNull(list)) {
      DocumentDTO document = new DocumentDTO();
      document.setDocumentId(doc.getId());
      document.setDocStatus(doc.getDocStatus());
      document.setCreatedTime(doc.getCreateDate());
      document.setParseStartedTime(doc.getCreateDate());
      document.setDocName(doc.getPath());
      document.setDcDocumentType(DcIdUtils.determineDocumentType(document.getDocName()));
      document.setFileSize(doc.getFileSize());
      document.setDocumentStatus(DocBaseConsts.STATUS_CD_VALID);
      documents.add(document);
    }
    return documents;
  }

  @Override
  public DocumentDTO findDocument(Long tenantId, Long documentId) {
    DocumentDTO document = documentManageMapper.getDocument(tenantId, documentId);
    List<DocumentDTO> list = Arrays.asList(document);
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (!grantSuperAdminEdit(list, userId)) {
      List<DocumentDTO> pendingDocuments = grantLibraryPermissions(list, userId);
      if (!CollectionUtils.isEmpty(pendingDocuments)) {
        grantDocumentLevelPermissions(pendingDocuments, document.getSpaceId(), userId);
      }

    }
    return document;
  }

  @Override
  @Transactional
  public ResultVO<Object> addDocument(DocumentAddParams params) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.getKnowledgeBase(params.getTenantId(), params.getKnowledgeId(), userId);
    Assert.notNull(knowledge, () -> "知识库不存在, knowledgeId=" + params.getKnowledgeId());
    List<Long> fileInfoIds = new ArrayList<>();
    fileInfoIds.addAll(CollectionUtils.emptyIfNull(params.getFileInfoIds()));
    fileInfoIds.addAll(CollectionUtils.emptyIfNull(params.getStructFileInfoIds()));
    FileInfoQueryParams fileInfoQueryParams = new FileInfoQueryParams();
    fileInfoQueryParams.setTenantId(params.getTenantId());
    fileInfoQueryParams.setFileInfoIds(fileInfoIds);
    List<FileInfoDTO> fileInfos = fileInfoManageMapper.selectFileInfoList(fileInfoQueryParams);
    Assert.notEmpty(fileInfos, "查询不到合法的文件，fileInfoIds=" + fileInfoIds);
    // 检查文件名是否重复
    if (existsDuplicateFileNames(fileInfos, params.getTenantId(), params.getKnowledgeId())) {
      return ResultVO.fail("检测到文件名重复，同一知识库中文件名必须唯一");
    }
    List<DocumentDTO> documents = new ArrayList<>(fileInfos.size());
    for (FileInfoDTO fileInfo : fileInfos) {
      DocumentDTO document = new DocumentDTO();
      document.setDocumentId(DocSequences.DOCUMENT_DOCUMENT_ID.next());
      document.setTenantId(params.getTenantId());
      document.setKnowledgeId(params.getKnowledgeId());
      document.setDataFrom(KnowledgeConsts.DOCUMENT_FORM_UPLOAD);
      document.setDocName(fileInfo.getFileName());
      document.setDocStatus(KnowledgeConsts.DOCUMENT_STATUS_UNTREATED);
      document.setFileInfoId(fileInfo.getFileInfoId());
      document.setFileId(fileInfo.getFileId());
      document.setFileSize(fileInfo.getFileSize());
      document.setSpaceId(params.getSpaceId());
      if (CollectionUtils.emptyIfNull(params.getStructFileInfoIds()).contains(fileInfo.getFileInfoId())) {
        document.setDocumentType(KnowledgeConsts.DOCUMENT_TYPE_STRUCT_DATA);
        // 结构化的附件，自动生成可编辑的在线文档
//        SpringUtil.getBean(IDocumentContentManageService.class)
//          .importDocumentContent(params.getTenantId(), document.getDocumentId(), null, document.getFileId(), KnowledgeConsts.IMPORT_TYPE_OVERWRITE);
      }
      else {
        document.setDocumentType(KnowledgeConsts.DOCUMENT_TYPE_UN_STRUCT_DATA);
      }
      document.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
      document.setCreatorId(userId);
      document.setUpdatorId(userId);
      documents.add(document);
      //documentResourceCompute(document);
    }

    if (knowledge.isDocChainType()) {
      // 对接 docchain 上传文档
      docChainConfigHelper.saveDocument(params.getTenantId(), knowledge.getTopicId(), documents, null);
    }

    documentManageMapper.batchInsertDocument(documents);
    knowledge.setFileCounts(knowledge.getFileCounts() + documents.size());
    knowledgeBaseManageMapper.updateKnowledgeBase(knowledge);
    ResourceElementFactory.get(OperClassEnum.KNOWLEDGE.name()).submit(knowledge.getTenantId(), knowledge.getKnowledgeId());
    return ResultVO.success(documents);
  }

  /**
   * 检查文件名称是否重复
   */
  private boolean existsDuplicateFileNames(List<FileInfoDTO> fileInfos, Long tenantId, Long knowledgeId) {
    Set<String> fileNameSet = new HashSet<>();
    // 校验新增的文件列表名称
    for (FileInfoDTO fileInfo : fileInfos) {
      if (!fileNameSet.add(fileInfo.getFileName())) {
        return true;
      }
    }
    // 校验已存在的文件列表名称
    List<SimpleDocumentDTO> simpleDocuments = documentManageMapper.selectSimpleDocumentList(tenantId, knowledgeId);
    for (SimpleDocumentDTO simpleDocument : CollectionUtils.emptyIfNull(simpleDocuments)) {
      if (fileNameSet.contains(simpleDocument.getDocName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * 1、需要判断是否具有知识库管理权限或者编辑权限 2、需要判断是否具有文档的查看权限
   */
  @Override
  @Transactional
  public ResultVO<Object> addDocumentNew(DocumentAddParams params) {
    KnowledgeBaseDTO knowledge = validateKnowledgePermission(params);

    List<DcDocumentDTO> validDocuments = filterAndValidateDocuments(params);

    AddDocumentResp processResult = processDocuments(validDocuments, knowledge, params);

    saveDocuments(processResult.getDocuments(), knowledge);

    return ResultVO.success(processResult);
  }

  /**
   * 验证知识库权限
   */
  private KnowledgeBaseDTO validateKnowledgePermission(DocumentAddParams params) {
    KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.getKnowledgeBase(params.getTenantId(),
      params.getKnowledgeId(), SessionUtil.getLoginInfo().getUserId());
    Assert.notNull(knowledge, () -> "知识库不存在, knowledgeId=" + params.getKnowledgeId());
    String kbPermissionType = btDcKbPermissionHelper.queryKnowledgeBasePermissionType(knowledge.getKnowledgeId(), knowledge.getOwnerId());
    Assert.isTrue(LibraryRoleEnum.MANAGE.getCode().equals(kbPermissionType) || LibraryRoleEnum.EDIT.getCode().equals(kbPermissionType), () -> "没有知识库的编辑权限");
    return knowledge;
  }

  /**
   * 过滤和验证文档
   */
  private List<DcDocumentDTO> filterAndValidateDocuments(DocumentAddParams params) {

    List<DcDocumentDTO> batchByDocuments = documentService.findBatchByDocumentId(params.getDcDocumentIds());
    Assert.notEmpty(batchByDocuments, "查询不到合法的文件，dcDocumentIds=" + params.getDcDocumentIds());

    // 过滤掉文件夹类型的文档
    batchByDocuments = batchByDocuments.stream()
      .filter(i -> !DocumentTypeEnum.FOLDER.getCode().equals(i.getDocumentType())
        && !DocumentTypeEnum.ROOT.getCode().equals(i.getDocumentType()))
      .collect(toList());
    Assert.notEmpty(batchByDocuments, "查询不到合法的文件，都是文件夹，dcDocumentIds=" + params.getDcDocumentIds());

    return filterDocumentsByPermission(batchByDocuments, params.getDcDocumentIds());
  }

  /**
   * 根据权限过滤文档
   */
  private List<DcDocumentDTO> filterDocumentsByPermission(List<DcDocumentDTO> documents, List<String> fileInfoIds) {
    // 按文档库分组，批量查询权限
    // 过滤掉 libraryId 为 null 的文档，这些文档无法查询权限
    Map<String, List<DcDocumentDTO>> libraryGroupMap = documents.stream()
      .filter(doc -> StringUtils.isNotBlank(doc.getLibraryId()))
      .collect(Collectors.groupingBy(DcDocumentDTO::getLibraryId));

    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    List<DcDocumentDTO> validDocuments = new ArrayList<>();

    for (Map.Entry<String, List<DcDocumentDTO>> entry : libraryGroupMap.entrySet()) {
      String libraryId = entry.getKey();
      List<DcDocumentDTO> libraryDocuments = entry.getValue();

      List<String> nodeIds = libraryDocuments.stream().map(DcDocumentDTO::getDocumentId).collect(toList());

      ControlRoleDict roleDict = controlTemplate.fetchNodeRole(libraryId, currentLoginUserId, nodeIds);

      for (DcDocumentDTO document : libraryDocuments) {
        ControlRole controlRole = roleDict.get(document.getDocumentId());
        if (controlRole != null && controlRole.hasPermission(NodePermission.READ_NODE)) {
          validDocuments.add(document);
        }
      }
    }

    Assert.notEmpty(validDocuments, "查询不到用户具有查看权限的文件，dcDocumentIds=" + fileInfoIds);
    return validDocuments;
  }

  /**
   * 处理文档并创建文档对象
   */
  private AddDocumentResp processDocuments(List<DcDocumentDTO> validDocuments, KnowledgeBaseDTO knowledge,
    DocumentAddParams params) {
    List<String> fails = new ArrayList<>();
    List<String> success = new ArrayList<>();
    List<DocumentDTO> documents = new ArrayList<>(validDocuments.size());
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Set<String> setDocNames = new HashSet<>();
    for (DcDocumentDTO fileInfo : validDocuments) {
      if (setDocNames.contains(fileInfo.getDocumentName()) || documentManageMapper
        .checkDocument(fileInfo.getDocumentId(), knowledge.getKnowledgeId(), fileInfo.getDocumentName(), knowledge.getTenantId())) {
        fails.add(fileInfo.getDocumentName() + "-添加失败原因：相同名字的文档已存在或者同一个文档重复添加");
        continue;
      }
      setDocNames.add(fileInfo.getDocumentName());
      success.add(fileInfo.getDocumentName());
      DocumentDTO document = createDocument(fileInfo, params, userId);
      documents.add(document);
    }
    AddDocumentResp resp = new AddDocumentResp();
    resp.setDocuments(documents);
    resp.setSuccess(success);
    resp.setFails(fails);
    return resp;
  }

  /**
   * 创建文档对象
   */
  private DocumentDTO createDocument(DcDocumentDTO fileInfo, DocumentAddParams params,
    Long userId) {
    DocumentDTO document = new DocumentDTO();
    document.setDocumentId(DocSequences.DOCUMENT_DOCUMENT_ID.next());
    document.setTenantId(params.getTenantId());
    document.setSpaceId(params.getSpaceId());
    document.setKnowledgeId(params.getKnowledgeId());
    document.setDataFrom(
      ContentSourceEnum.UPLOAD.getCode().equals(fileInfo.getContentSource()) ? KnowledgeConsts.DOCUMENT_FORM_UPLOAD
        : KnowledgeConsts.DOCUMENT_FROM_CRAWLING);
    document.setDocName(fileInfo.getDocumentName());
    document.setDocStatus(KnowledgeConsts.DOCUMENT_STATUS_UNTREATED);
    document.setFileInfoId(fileInfo.getFileInfoId());
    document.setDcDocumentId(fileInfo.getDocumentId());
    document.setFileId(fileInfo.getFileId()); // 它的值会变动存储意义不大

    // 设置文档类型
    if (DocumentTypeEnum.EXCEL.getCode().contains(fileInfo.getDocumentType()) || DocumentTypeEnum.EXCEL_ONLINE.getCode().equals(fileInfo.getDocumentType())) {
      document.setDocumentType(KnowledgeConsts.DOCUMENT_TYPE_STRUCT_DATA);
//      if (DocumentTypeEnum.EXCEL.getCode().contains(fileInfo.getDocumentType())) {
//        // 结构化的附件，自动生成可编辑的在线文档
//        SpringUtil.getBean(IDocumentContentManageService.class).importDocumentContent(params.getTenantId(),
//          document.getDocumentId(), null, document.getFileId(), KnowledgeConsts.IMPORT_TYPE_OVERWRITE);
//      }
    }
    else {
      document.setDocumentType(KnowledgeConsts.DOCUMENT_TYPE_UN_STRUCT_DATA);
    }

    document.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    document.setCreatorId(userId);
    document.setUpdatorId(userId);

    return document;
  }

  /**
   * 保存文档
   */
  private void saveDocuments(List<DocumentDTO> documents, KnowledgeBaseDTO knowledge) {
    if (documents.isEmpty()) {
      return;
    }

    synchronousDocChain(knowledge, documents);
    documentManageMapper.batchInsertDocument(documents);
    //for (DocumentDTO document : documents) {
    //  documentResourceCompute(document);
    //}
    ResourceElementFactory.get(OperClassEnum.KNOWLEDGE.name()).submit(knowledge.getTenantId(), knowledge.getKnowledgeId());
  }


  @Override
  @Transactional
  public ResultVO<Long> deleteDocument(Long tenantId, Long documentId) {
    DocumentDTO document = documentManageMapper.getDocument(tenantId, documentId);
    Assert.notNull(document, () -> "文档不存在, documentId=" + documentId);
    KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.getKnowledgeBase(tenantId, document.getKnowledgeId(), SessionUtil.getLoginInfo().getUserId());
    Assert.notNull(knowledge, () -> "知识库不存在, knowledgeId=" + document.getKnowledgeId());
    String kbPermissionType = btDcKbPermissionHelper.queryKnowledgeBasePermissionType(knowledge.getKnowledgeId(), knowledge.getOwnerId());
    Assert.isTrue(LibraryRoleEnum.MANAGE.getCode().equals(kbPermissionType) || LibraryRoleEnum.EDIT.getCode().equals(kbPermissionType), () -> "没有知识库的编辑权限");

    documentManageMapper.deleteDocument(document.getTenantId(), documentId, SessionUtil.getLoginInfo().getUserId());

    if (knowledge.getTopicId() != null && document.isDocChainType() && docChainConfigHelper.existsTopic(tenantId, knowledge.getTopicId())) {
      if (document.getExtSystemId() != null && docChainConfigHelper.existsTopicDocId(document.getTenantId(), knowledge.getTopicId(), document.getExtSystemId())) {
        // 先查询是否存在然后在进行删除，如果不存在那么直接删除博特的相关知识库文档
        docChainConfigHelper.deleteDocument(document.getTenantId(), document.getExtSystemId());
      }
    }
    btDcQaRecordManageService.deleteBtDcQaRecordInfoByknowledgeId(document.getKnowledgeId(), document.getExtSystemId(), tenantId);
    knowledge.setFileCounts(knowledge.getFileCounts() > 0 ? knowledge.getFileCounts() - 1 : 0);
    knowledgeBaseManageMapper.updateKnowledgeBase(knowledge);
    ResourceElementFactory.get(OperClassEnum.KNOWLEDGE.name()).submit(knowledge.getTenantId(), knowledge.getKnowledgeId());
    //removeDocumentResource(document);
    return ResultVO.success(document.getKnowledgeId());
  }

  @Override
  public List<DocumentDTO> queryDocumentList(DocumentQueryParams queryParams) {
    KnowledgeBaseDTO knowledge = queryKonwledgeBase(queryParams.getTenantId(), queryParams.getKnowledgeId());
    if (knowledge.isRelated()) {
      List<QueryDocmentResponse.DocmentInfo> documentInfos = docChainConfigHelper.queryDocument(knowledge.getTenantId(), knowledge.getTopicId());
      return getDocuments(documentInfos);
    }
    return documentManageMapper.selectDocumentList(queryParams);
  }

  @Override
  @Transactional
  @SuppressFBWarnings("SECSQLISPRJDBC")
  public ResultVO<PageInfo<DocumentDTO>> queryDocumentPage(DocumentQueryParams queryParams) {
    KnowledgeBaseDTO knowledge = queryKonwledgeBase(queryParams.getTenantId(), queryParams.getKnowledgeId());

    // 权限检查
    checkKnowledgeBasePermission(knowledge);

    // 关联知识库走特殊逻辑
    if (knowledge.isRelated()) {
      return ResultVO.success(getDocumentPageInfo(queryParams, knowledge));
    }

    // 准备查询参数
    prepareQueryParams(queryParams, knowledge);

    // 查询文档分页
    PageInfo<DocumentDTO> pageInfo = documentManageMapper.selectDocumentPage(queryParams, queryParams.buildRowBounds())
      .toPageInfo();

    // 处理查询结果
    processDocumentPageResult(pageInfo, knowledge);

    return ResultVO.success(pageInfo);
  }

  /**
   * 检查知识库访问权限
   */
  private void checkKnowledgeBasePermission(KnowledgeBaseDTO knowledge) {
    String permissionType = btDcKbPermissionHelper.queryKnowledgeBasePermissionType(
      knowledge.getKnowledgeId(), knowledge.getOwnerId());
    boolean isMembersOnly = VisibilityScopeEnum.MEMBERS.getCode().equals(knowledge.getVisibilityScope());
    boolean hasNoPermission = StringUtils.isEmpty(permissionType);
    Assert.isTrue(!(isMembersOnly && hasNoPermission), () -> "没有权限访问该知识库！");
  }

  /**
   * 准备查询参数
   */
  private void prepareQueryParams(DocumentQueryParams queryParams, KnowledgeBaseDTO knowledge) {

    // 查询子目录ID列表
    queryParams.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(knowledge.getTenantId(),
      knowledge.getSpaceId(), queryParams.getCatalogItemId(), DocBaseConsts.CATALOG_TYPE_KNOWLEDGE));

    // 处理文档状态参数：支持逗号分隔的多个状态
    if (StringUtils.isNotBlank(queryParams.getDocStatus())) {
      if (!"refresh".equalsIgnoreCase(queryParams.getDocStatus())) {
        queryParams.setDocStatuss(Arrays.asList(queryParams.getDocStatus().split(",")));
        queryParams.setDocStatus(null);
      }
      else {
        queryParams.setHasUpdate("1");
        queryParams.setDocStatus(DocStatusEnum.COMPLETED.getCode());
      }
    }
    if (DocumentTypeEnum.WORD.getCode().equals(queryParams.getDcDocumentType())) {
      queryParams.setDcDocumentTypes(Arrays.asList(DocumentTypeEnum.WORD.getCode(), DocumentTypeEnum.WORD_ONLINE.getCode()));
      queryParams.setDcDocumentType(null);
    }
    if (DocumentTypeEnum.EXCEL.getCode().equals(queryParams.getDcDocumentType())) {
      queryParams.setDcDocumentTypes(Arrays.asList(DocumentTypeEnum.EXCEL.getCode(), DocumentTypeEnum.EXCEL_ONLINE.getCode()));
      queryParams.setDcDocumentType(null);
    }
  }

  /**
   * 处理文档分页查询结果
   */
  private void processDocumentPageResult(PageInfo<DocumentDTO> pageInfo, KnowledgeBaseDTO knowledge) {
    if (CollectionUtils.isEmpty(pageInfo.getList())) {
      return;
    }

    // 设置文档路径
    setDocumentPaths(pageInfo.getList(), knowledge.getSpaceId());

    // 同步 DocChain 文档状态
    syncDocChainDocumentStatus(pageInfo.getList(), knowledge);
  }

  /**
   * 设置文档路径
   */
  private void setDocumentPaths(List<DocumentDTO> documents, Long spaceId) {
    populateDocumentPaths(documents);
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (grantSuperAdminEdit(documents, userId)) {
      return;
    }
    List<DocumentDTO> pendingDocuments = grantLibraryPermissions(documents, userId);
    if (CollectionUtils.isEmpty(pendingDocuments)) {
      return;
    }
    grantDocumentLevelPermissions(pendingDocuments, spaceId, userId);
  }

  private void populateDocumentPaths(List<DocumentDTO> documents) {
    List<String> documentIds = documents.stream()
      .map(DocumentDTO::getDcDocumentId)
      .filter(StringUtils::isNotBlank)
      .collect(toList());
    Map<String, DocumentPathDTO> pathMap = documentService.getDocumentPathBatch(documentIds);

    documents.forEach(item -> {
      DocumentPathDTO pathDTO = pathMap.get(item.getDcDocumentId());
      if (pathDTO != null && StringUtils.isNotBlank(pathDTO.getDocumentPath())) {
        item.setResourcePath(pathDTO.getDocumentPath());
      }
      if (StringUtils.isEmpty(item.getDcDocumentType())) {
        item.setDcDocumentType(DcIdUtils.determineDocumentType(item.getDocName()));
      }
    });
  }

  private boolean grantSuperAdminEdit(List<DocumentDTO> documents, Long userId) {
    if (!SessionUtil.isSuperAdmin(userId)) {
      return false;
    }
    documents.forEach(item -> {
      item.setDocumentEdit(DocBaseConsts.TRUE);
      item.setDocumentDownload(DocBaseConsts.TRUE);
    });
    return true;
  }

  private List<DocumentDTO> grantLibraryPermissions(List<DocumentDTO> documents, Long userId) {
    // 分离出 libraryId 为 null 的文档，这些文档无法查询权限，直接加入待处理列表
    List<DocumentDTO> documentsWithLibraryId = new ArrayList<>();
    List<DocumentDTO> pendingDocuments = new ArrayList<>();
    for (DocumentDTO document : documents) {
      if (StringUtils.isBlank(document.getLibraryId())) {
        pendingDocuments.add(document);
      }
      else {
        documentsWithLibraryId.add(document);
      }
    }

    // 对有效的文档按 libraryId 分组
    Map<String, List<DocumentDTO>> libraryGroupMap = documentsWithLibraryId.stream()
      .collect(Collectors.groupingBy(DocumentDTO::getLibraryId));

    for (Map.Entry<String, List<DocumentDTO>> entry : libraryGroupMap.entrySet()) {
      String libraryId = entry.getKey();
      List<DocumentDTO> libraryDocuments = entry.getValue();
      List<String> nodeIds = libraryDocuments.stream().map(DocumentDTO::getDcDocumentId).collect(toList());
      ControlRoleDict roleDict = controlTemplate.fetchNodeRole(libraryId, userId, nodeIds);
      for (DocumentDTO document : libraryDocuments) {
        ControlRole controlRole = roleDict.get(document.getDcDocumentId());
        if (controlRole != null && controlRole.hasPermission(NodePermission.EDIT_NODE)) {
          document.setDocumentEdit(DocBaseConsts.TRUE);
          document.setDocumentDownload(DocBaseConsts.TRUE);
        }
        else {
          pendingDocuments.add(document);
        }
      }
    }
    return pendingDocuments;
  }

  private void grantDocumentLevelPermissions(List<DocumentDTO> documents, Long spaceId, Long userId) {
    List<Long> orgs = resolveUserOrgIds(spaceId, userId);
    List<String> documentIds = documents.stream().map(DocumentDTO::getDcDocumentId).toList();
    List<DocumentPermissionDTO> documentPermissionDTOS =
      documentPermissionMapper.selectUserPermissionByDocumentIds(documentIds, userId, orgs);
    if (CollectionUtils.isEmpty(documentPermissionDTOS)) {
      return;
    }
    Map<String, List<String>> documentPermissionMap = documentPermissionDTOS.stream()
      .collect(Collectors.groupingBy(DocumentPermissionDTO::getDocumentId,
        Collectors.mapping(DocumentPermissionDTO::getPermissionType, toList())));
    for (DocumentDTO document : documents) {
      List<String> permissions = documentPermissionMap.get(document.getDcDocumentId());
      if (hasDocumentEditPermission(permissions)) {
        document.setDocumentEdit(DocBaseConsts.TRUE);
      }
      if (hasDocumentDownloadPermission(permissions)) {
        document.setDocumentDownload(DocBaseConsts.TRUE);
      }
    }
  }

  private List<Long> resolveUserOrgIds(Long spaceId, Long userId) {
    List<OrgDTO> orgList = portalOrgIntegration.queryUserOrgList(spaceId, userId);
    if (CollectionUtils.isEmpty(orgList)) {
      return null;
    }
    return orgList.stream().map(OrgDTO::getOrgId).collect(toList());
  }

  private boolean hasDocumentEditPermission(List<String> permissions) {
    if (CollectionUtils.isEmpty(permissions)) {
      return false;
    }
    return permissions.contains(DocRoleEnum.DOC_MANAGE.getCode()) || permissions.contains(DocRoleEnum.DOC_EDIT.getCode());
  }

  private boolean hasDocumentDownloadPermission(List<String> permissions) {
    if (CollectionUtils.isEmpty(permissions)) {
      return false;
    }
    return permissions.contains(DocRoleEnum.DOC_MANAGE.getCode()) || permissions.contains(DocRoleEnum.DOC_EDIT.getCode()) || permissions.contains(DocRoleEnum.DOWNLOAD.getCode());
  }

  /**
   * 同步 DocChain 中的文档状态
   */
  private void syncDocChainDocumentStatus(List<DocumentDTO> documents, KnowledgeBaseDTO knowledge) {
    if (!knowledge.isDocChainType()) {
      return;
    }

    // 筛选出状态为解析中的文档
    List<DocumentDTO> analyzingDocuments = documents.stream()
      .filter(d -> KnowledgeConsts.DOCUMENT_STATUS_ANALYZING.equals(d.getDocStatus()))
      .collect(toList());

    if (!analyzingDocuments.isEmpty()) {
      batchUpdateDocStatus(knowledge.getTenantId(), knowledge.getTopicId(), analyzingDocuments);
    }
  }

  @Override
  @Transactional
  @SuppressFBWarnings("SECSQLISPRJDBC")
  public void updateDocChainDocStatus() {
    // 只处理近期更新过的文档。定时任务的执行频率很高，如果长期未更新，大概率是出现了异常情况，重复检查也没用
    Date minUpdatedTime = DateUtils.addHours(new Date(), -1);
    // 数据量应该不大，全部查出来
    List<DocumentDTO> documents = documentManageMapper.selectAnalyzingDocumentsOfDocChain(minUpdatedTime);
    if (CollectionUtils.isEmpty(documents)) {
      return;
    }
    Long spaceId = documents.get(0).getSpaceId();
    // 按 (tenantId, topicId) 分组，分批处理。一个主题下的文档数量应该不会很多
    Map<Pair<Long, Long>, List<DocumentDTO>> group = documents.stream()
      .collect(Collectors.groupingBy(p -> Pair.of(p.getTenantId(), p.getTopicId())));
    int total = documents.size();
    int updatedCount = 0;
    RuntimeException lastException = null;
    for (Entry<Pair<Long, Long>, List<DocumentDTO>> entry : group.entrySet()) {
      Long tenantId = entry.getKey().getLeft();
      Long topicId = entry.getKey().getRight();
      // 不使用事务，采用默认的自动提交方式
      // 忽略异常，避免处理一个主题失败影响其它主题
      try {
        updatedCount += batchUpdateDocStatus(tenantId, spaceId, entry.getValue());
      }
      catch (BssException e) {
        lastException = e;
        logger.error("Failed to update docChain doc status: tenantId={}, topicId={}, error={}", tenantId, topicId,
          e.getMessage());
      }
      catch (RuntimeException e) {
        lastException = e;
        logger.error("Failed to update docChain doc status: tenantId={}, topicId={}", tenantId, topicId, e);
      }
    }
    logger.debug("Sync docChain document status finished: total={}, updated={}", total, updatedCount);
    // 确保定时任务日志能正确记录失败
    if (lastException != null) {
      throw lastException;
    }
  }

  @Override
  public ResultVO<Void> buildDocument(Long tenantId, Long documentId, Boolean newDoc, String isExist) {
    if (DocBaseConsts.TRUE.equals(isExist)) {
      docChainConfigHelper.redoDocument(tenantId, documentId);
      return ResultVO.success();
    }
    DocumentDTO document = documentManageMapper.getDocument(tenantId, documentId);
    Assert.notNull(document, () -> "文档不存在, documentId=" + documentId);
    String docStatus = document.getDocStatus();
    if (KnowledgeConsts.DOCUMENT_STATUS_ANALYZING.equals(docStatus)) {
      throw new BssException("文档正在处理中，请稍后再试");
    }

    if (document.isDocChainType()) {
      // 知识复制、环境迁移情况下，对应的知识可能还未同步，需要提前校验
      if (!docChainConfigHelper.existsTopic(tenantId, document.getTopicId())) {
        throw new BssException("系统检测到对应的知识库还未同步到DocChain，请点击学习更新知识");
      }

      if (KnowledgeConsts.DOCUMENT_STATUS_FEATURE_EXTRACTION.equals(docStatus)) {
        docChainConfigHelper.summaryDocument(tenantId, document.getExtSystemId());
        docStatus = KnowledgeConsts.DOCUMENT_STATUS_FEATURE_EXTRACTION;
      }
      else {
        if (Boolean.TRUE.equals(newDoc) && StringUtils.isNotEmpty(document.getDcDocumentId())) {
          String kbPermissionType = btDcKbPermissionHelper.queryKnowledgeBasePermissionType(document.getKnowledgeId(), document.getOwnerId());
          Assert.isTrue(LibraryRoleEnum.MANAGE.getCode().equals(kbPermissionType) || LibraryRoleEnum.EDIT.getCode().equals(kbPermissionType), () -> "没有知识库的编辑权限");
          docStatus = KnowledgeConsts.DOCUMENT_FROM_CRAWLING;
        }
        else {
          docChainConfigHelper.redoDocument(tenantId, document.getExtSystemId());
          docStatus = KnowledgeConsts.DOCUMENT_FROM_CRAWLING;
        }
      }
      Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
      document.setDocStatus(docStatus);
      document.setUpdatorId(currentLoginUserId);
      document.setParseStartedTime(new Date());

      updateBuildDocument(document, currentLoginUserId);
    }
    return ResultVO.success();
  }

  /**
   * 修改知识库文档信息
   *
   * @param document
   * @param currentLoginUserId
   */
  private void updateBuildDocument(DocumentDTO document, Long currentLoginUserId) {
    document.setErrorMessage(null);
    if ("1".equals(document.getHasUpdate()) && CommonConsts.STATUS_CD_VALID.equals(document.getDocumentStatus())) {
      if (StringUtils.isNotEmpty(document.getDocumentName()) && !document.getDocumentName().equals(document.getDocName())) {
        // 先删除然后再删除docchain相关数据，需要检查docchian是否存在，存在再进行删除
        if (docChainConfigHelper.existsTopicDocId(document.getTenantId(), document.getTopicId(), document.getExtSystemId())) {
          docChainDocumentHelper.deleteDocument(document.getTenantId(), document.getExtSystemId());
        }
        document.setDocName(document.getDocumentName());
      }
      docChainConfigHelper.saveDocumentNew(document.getTenantId(), document.getTopicId(), Collections.singletonList(document), null);
    }
    else {
      if (docChainConfigHelper.existsTopicDocId(document.getTenantId(), document.getTopicId(),
        document.getExtSystemId())) {
        docChainConfigHelper.redoDocument(document.getTenantId(), document.getExtSystemId());
      }
      else {
        document.setDocStatus(KnowledgeConsts.DOCUMENT_STATUS_FAILED);
        document.setErrorMessage("知识库文档并没有在docchain中有记录，构建失败，请更新学习知识！");
      }
    }

    document.setUpdatorId(currentLoginUserId);
    document.setHasUpdate("0");
    // 避免把 HTTP 请求放在事务中
    TransactionUtil.execute(() -> {
      documentManageMapper.updateDocument(document);
    });
  }

  @Override
  public boolean isFinishedBuildDocument(Long tenantId, Long knowledgeId) {
    KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.selectSimpleKnowledgeById(tenantId, knowledgeId);
    Assert.notNull(knowledge, "知识库不存在，knowledgeId=" + knowledgeId);
    List<QueryDocmentResponse.DocmentInfo> docs = docChainConfigHelper.queryDocument(tenantId, knowledge.getTopicId());
    return docs.stream().allMatch(QueryDocmentResponse.DocmentInfo::isSuccess);
  }

  @Override
  @SuppressWarnings("BusyWait")
  public boolean waitBuildDocumentFinish(Long tenantId, Long knowledgeId, int maxWaitTime) {
    // 轮询间隔（毫秒）
    int pollInterval = 3000;
    // 超时时间（毫秒）
    if (maxWaitTime <= 0) {
      maxWaitTime = 60000;
    }
    long startTime = System.currentTimeMillis();
    try {
      KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.selectSimpleKnowledgeById(tenantId, knowledgeId);
      Assert.notNull(knowledge, "知识库不存在: knowledgeId=" + knowledgeId);
      do {
        List<QueryDocmentResponse.DocmentInfo> docs = docChainConfigHelper.queryDocument(tenantId,
          knowledge.getTopicId());
        // 构建失败
        if (docs.stream().anyMatch(QueryDocmentResponse.DocmentInfo::isFailed)) {
          logger.warn("Document build failed: tenantId={}, knowledgeId={}", tenantId, knowledgeId);
          break;
        }
        // 构建成功
        if (docs.stream().allMatch(QueryDocmentResponse.DocmentInfo::isSuccess)) {
          return true;
        }
        Thread.sleep(pollInterval);
      }
      while (System.currentTimeMillis() - startTime < maxWaitTime);
    }
    catch (InterruptedException e) {
      logger.error("Failed to poll query document build state, interrupted: knowledgeId={}", knowledgeId, e);
      Thread.currentThread().interrupt();
    }
    catch (BssException | IllegalStateException | IllegalArgumentException e) {
      logger.error("Failed to poll query document build state: interrupted={}, error={}", knowledgeId, e.getMessage());
    }
    catch (RuntimeException e) {
      logger.error("Failed to poll query document build state: knowledgeId={}, error={}", knowledgeId, e.getMessage(),
        e);
    }
    return false;
  }

  @Override
  public void rebuildDocuments(Long tenantId, Long fileInfoId, Long fileId, String fileName) {
    List<DocumentDTO> documents = documentManageMapper.selectDocumentsByFileInfoId(tenantId, fileInfoId);
    if (documents.isEmpty()) {
      return;
    }
    for (DocumentDTO document : documents) {
      // 如果文件名称发生变化，需要删除原有的关联文档
      if (!Objects.equals(document.getDocName(), fileName)) {
        docChainDocumentHelper.deleteDocument(document.getTenantId(), document.getExtSystemId());
      }
      Map<Long, Long> mapping = docChainDocumentHelper.uploadDocument(document.getTenantId(), document.getTopicId(),
        Collections.singletonList(fileId));
      document.setDocName(fileName);
      document.setExtSystemId(mapping.get(fileId));
      documentManageMapper.updateDocument(document);
      refreshCacheService.refresh(DocCacheConsts.CACHE_NAME_KNOWLEDGE, tenantId + ":" + document.getKnowledgeId());
    }
  }

  /**
   * 1、文件上传到对端是不需要给docId，通过主题和文件名进行映射；--因此在文档变更后只要在队列表中对应的文档ext_system_id设置为空即可，并且将状态设置为待处理；
   * @param dcDocId 文档库文档id
   * @param tenantId 租户id
   * @return
   */
  @Transactional
  @Override
  public void rebuildBtDocumentByDcDocDocId(String dcDocId, Long tenantId, Long currentLoginUserId,
    String onLineEditing) {
    List<DocumentDTO> documents = documentManageMapper.selectDocumentListByDcDcumentId(dcDocId, tenantId);
    if (CollectionUtils.isEmpty(documents)) {
      return;
    }
    boolean onLineEdited = CommonConsts.TRUE.equals(onLineEditing);
    if (onLineEdited) {
      // 对文档打标有一个待发布的标识，说明有引用知识库文档
      documentService.updateDocumentReleased(dcDocId, DocBaseConsts.TRUE);
    }
    // 异步上传文档
    for (DocumentDTO document : documents) {
      if (document.isDocChainType()) {
        documentManageMapper.updateDocumentHasUpdate(document.getDocumentId(), tenantId, currentLoginUserId, document.getDocName());
        if (!onLineEdited) {
          // 将事务分小一点
          boolean reName = updateDocumentNameIfChanged(document);
          processRebuildQueue(document, tenantId, reName);
        }
      }
    }
  }

  /**
   * 更新文档名称（如果发生变化）
   * @return 是否发生了重命名
   */
  private boolean updateDocumentNameIfChanged(DocumentDTO document) {
    if (StringUtils.isNotEmpty(document.getDocumentName()) && !document.getDocumentName().equals(document.getDocName())) {
      document.setDocName(document.getDocumentName());
      return true;
    }
    return false;
  }

  /**
   * 处理重建队列：更新现有队列或创建新队列
   */
  private void processRebuildQueue(DocumentDTO document, Long tenantId, boolean reName) {
    ThreadContextRunner.runWithTenant(tenantId, () -> {
      if (reName) {
        // 先删除然后再删除docchain相关数据，需要检查docchian是否存在，存在再进行删除
        if (docChainConfigHelper.existsTopicDocId(document.getTenantId(), document.getTopicId(),
          document.getExtSystemId())) {
          docChainDocumentHelper.deleteDocument(tenantId, document.getExtSystemId());
        }
      }
      docChainConfigHelper.saveDocumentNew(document.getTenantId(), document.getTopicId(),
        Collections.singletonList(document), null);

      documentManageMapper.updateDocument(document);
      return null;
    });
  }

  private PageInfo<DocumentDTO> getDocumentPageInfo(DocumentQueryParams queryParams, KnowledgeBaseDTO knowledgeBase) {
    QueryDocmentResponse.DocmentPageInfo docmentInfos = docChainConfigHelper.queryDocumentPage(knowledgeBase.getTenantId(), knowledgeBase.getTopicId(),
      queryParams.getSearchContent(), queryParams.getPageNum(), queryParams.getPageSize());
    PageInfo<DocumentDTO> pageInfo = new PageInfo<>();
    if (docmentInfos == null) {
      return pageInfo;
    }
    List<DocumentDTO> documents = getDocuments(docmentInfos.getList());
    pageInfo.setPageNum(queryParams.getPageNum());
    pageInfo.setPageSize(queryParams.getPageSize());
    pageInfo.setList(filterDcDocumentType(documents, queryParams.getDcDocumentType()));
    pageInfo.setTotal(docmentInfos.getTotal());
    return pageInfo;
  }

  private List<DocumentDTO> filterDcDocumentType(List<DocumentDTO> documents, String dcDocumentType) {
    if (CollectionUtils.isEmpty(documents) || StringUtils.isBlank(dcDocumentType)) {
      return documents;
    }
    return documents.stream().filter(i -> dcDocumentType.equals(i.getDocumentType())).toList();
  }

  /**
   * 批量更新文档状态
   */
  private int batchUpdateDocStatus(Long tenantId, Long topicId, List<DocumentDTO> documents) {
    // 查询 DocChain 主题下的所有文档状态
    List<QueryDocmentResponse.DocmentInfo> docChainDocuments = docChainConfigHelper.queryDocument(tenantId, topicId);
    List<Object[]> updateArgs = new ArrayList<>();
    for (DocumentDTO document : documents) {
      QueryDocmentResponse.DocmentInfo doc = IterableUtils.find(docChainDocuments,
        d -> Objects.equals(document.getExtSystemId(), d.getId()));
      // 文档不存在时当作失败
      String newStatus = doc != null ? doc.getDocStatus() : KnowledgeConsts.DOCUMENT_STATUS_FAILED;
      if (!newStatus.equals(document.getDocStatus())) {
        // queryDocumentPage 接口需要给前端返回最新的文档状态
        document.setDocStatus(newStatus);
        updateArgs.add(new Object[]{
          newStatus, tenantId, document.getDocumentId()
        });
      }
    }
    if (!updateArgs.isEmpty()) {
      String updateSql = "UPDATE bt_document set doc_status = ? where tenant_id = ? AND document_id = ?";
      jdbcTemplate.batchUpdate(updateSql, updateArgs);
    }
    return updateArgs.size();
  }

  private void synchronousDocChain(KnowledgeBaseDTO knowledge, List<DocumentDTO> documents) {
    if (knowledge.isDocChainType()) { // 改成进入队列中
      // 对接 docchain 上传文档
       docChainConfigHelper.saveDocumentNew(knowledge.getTenantId(), knowledge.getTopicId(), documents, null);
    }
    knowledge.setFileCounts(knowledge.getFileCounts() + documents.size());
    knowledgeBaseManageMapper.updateKnowledgeBase(knowledge);
  }

  private KnowledgeBaseDTO queryKonwledgeBase(Long tenantId, Long knowledgeId) {
    KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.getKnowledgeBase(tenantId, knowledgeId, SessionUtil.getLoginInfo().getUserId());
    Assert.notNull(knowledge, "知识库不存在，knowledgeId=" + knowledgeId);
    return knowledge;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public ResultVO<Void> createDocumentNodesFromFileInfo() {
    // 查询需要创建文档节点的fileInfo信息
    //noinspection resource
    Page<FileInfoDTO> baseJobData = documentManageMapper.selectFileInfoForDocumentNodeCreation(new RowBounds(0, 200)); //NOPMD - suppressed CloseResource - 不需要关闭
    List<FileInfoDTO> fileInfoList = baseJobData.getResult();

    if (CollectionUtils.isEmpty(fileInfoList)) {
      logger.info("没有找到需要创建文档节点的文件信息");
      return ResultVO.success();
    }

    logger.info("开始创建文档节点，共{}条数据需要处理", fileInfoList.size());

    // 按租户ID分组
    Map<Long, List<FileInfoDTO>> tenantFileInfoMap = fileInfoList.stream().collect(Collectors.groupingBy(FileInfoDTO::getTenantId));

    // 初始化租户空间ID映射
    Map<Long, Long> tenantIdSpaceIdMap = buildTenantIdSpaceIdMap(tenantFileInfoMap.keySet());

    // 处理每个租户的文件信息
    int successCount = 0;
    List<String> processedFileInfoIds = new ArrayList<>();
    Map<Long, String> tenantLibraryIdMap = new HashMap<>();

    for (Map.Entry<Long, List<FileInfoDTO>> entry : tenantFileInfoMap.entrySet()) {
      ProcessTenantResultDTO result = processTenantFileInfos(entry, tenantIdSpaceIdMap, tenantLibraryIdMap);
      successCount += result.getSuccessCount();
      processedFileInfoIds.addAll(result.getProcessedFileInfoIds());
    }

    // 针对bt_document表的数据处理：通过file_info_id关联bt_dc_document和bt_document，在bt_document表的dc_document_id字段中填充对应的bt_dc_document的document_id
    updateDocumentDcDocumentId(processedFileInfoIds);

    logger.debug("文档节点创建完成，成功: {}, 总数: {}", successCount, fileInfoList.size());
    return ResultVO.success();
  }

  /**
   * 构建租户ID到空间ID的映射
   */
  private Map<Long, Long> buildTenantIdSpaceIdMap(Set<Long> tenantIds) {
    Map<Long, Long> tenantIdSpaceIdMap = new HashMap<>();
    if (CollectionUtils.isEmpty(tenantIds)) {
      return tenantIdSpaceIdMap;
    }
    List<SimpleTenantDTO> simpleTenants = tenantQueryMapper.getSimpleTenants(new ArrayList<>(tenantIds));
    if (CollectionUtils.isNotEmpty(simpleTenants)) {
      for (SimpleTenantDTO simpleTenant : simpleTenants) {
        if (simpleTenant.getTenantId() != null && simpleTenant.getSpaceId() != null) {
          tenantIdSpaceIdMap.put(simpleTenant.getTenantId(), simpleTenant.getSpaceId());
        }
      }
    }
    return tenantIdSpaceIdMap;
  }

  /**
   * 处理单个租户的文件信息
   */
  private ProcessTenantResultDTO processTenantFileInfos(Map.Entry<Long, List<FileInfoDTO>> entry,
                                                      Map<Long, Long> tenantIdSpaceIdMap,
                                                      Map<Long, String> tenantLibraryIdMap) {
    Long tenantId = entry.getKey();
    List<FileInfoDTO> fileInfos = entry.getValue();

    // 构建目录映射
    CatalogStructureDTO catalogStructure = buildCatalogStructure(tenantId);

    // 获取或创建文档库ID
    String libraryId = tenantLibraryIdMap.computeIfAbsent(tenantId,
      id -> getOrCreatePublicLibrary(id, 1L, tenantIdSpaceIdMap.get(tenantId)));

    // 查询所有全员文档库下的文档文件夹
    Map<String, String> documentServerPathIdMap = documentService.findFoldersByLibraryId(libraryId, tenantId);

    // 处理每个文件信息
    int successCount = 0;
    List<String> processedFileInfoIds = new ArrayList<>();
    for (FileInfoDTO fileInfo : fileInfos) {
      try {
        String documentId = processSingleFileInfo(fileInfo, libraryId, tenantIdSpaceIdMap,
          catalogStructure.getCatalogIdMap(), catalogStructure.getCatalogMap(), documentServerPathIdMap);
        successCount++;
        processedFileInfoIds.add(fileInfo.getFileInfoId() + "," + fileInfo.getTenantId() + "," + documentId);
      }
      catch (Exception e) {
        logger.error("创建文档节点失败，fileInfoId: {}, error: {}", fileInfo.getFileInfoId(), e.getMessage(), e);
      }
    }

    return new ProcessTenantResultDTO(successCount, processedFileInfoIds);
  }

  /**
   * 构建目录结构（包括目录映射和目录ID映射）
   */
  private CatalogStructureDTO buildCatalogStructure(Long tenantId) {
    CatalogQueryParams params = new CatalogQueryParams();
    params.setTenantId(tenantId);
    params.setCatalogType(CatalogConsts.TYPE_KNOWLEDGE);
    List<CatalogDTO> catalogs = catalogManageMapper.selectCatalogList(params);

    Map<Long, Map<String, String>> catalogIdMap = new HashMap<>();
    Map<Long, CatalogDTO> catalogMap = catalogs.stream()
      .collect(Collectors.toMap(CatalogDTO::getCatalogId, catalog -> catalog, (existing, replacement) -> existing));

    if (CollectionUtils.isNotEmpty(catalogs)) {
      for (CatalogDTO item : catalogs) {
        Map<String, String> catalogNameMap = buildCatalogNameMap(item, catalogMap);
        catalogIdMap.put(item.getCatalogId(), catalogNameMap);
      }
    }

    return new CatalogStructureDTO(catalogIdMap, catalogMap);
  }

  /**
   * 构建单个目录的名称映射
   */
  private Map<String, String> buildCatalogNameMap(CatalogDTO catalog, Map<Long, CatalogDTO> catalogMap) {
    List<CatalogDTO> children = new ArrayList<>();
    findAndAddParents(catalog, catalogMap, children);

    Map<String, String> catalogNameMap = new HashMap<>();
    for (int i = 0; i < children.size(); i++) {
      String catalogName = children.get(i).getCatalogName();
      String path = children.subList(i, children.size()).stream()
        .map(CatalogDTO::getCatalogName)
        .collect(Collectors.joining("/"));
      catalogNameMap.put(catalogName, path);
    }
    return catalogNameMap;
  }

  private String processSingleFileInfo(FileInfoDTO fileInfo, String libraryId, Map<Long, Long> tenantIdSpaceIdMap, Map<Long, Map<String, String>> catalogIdMap, Map<Long, CatalogDTO> catalogMap, Map<String, String> documentServerPathIdMap) {
    Long tenantId = fileInfo.getTenantId();
    TenantContextHolder.setTenantId(tenantId);
    Long spaceId = tenantIdSpaceIdMap.get(tenantId);
    if (spaceId == null) {
      spaceId = tenantId;
    }
    Long userId = Optional.ofNullable(fileInfo.getCreatorId()).orElse(-1L);
    SpaceContextHolder.setSpaceId(spaceId);

    String parentId = resolveCatalogParent(fileInfo, libraryId, catalogIdMap, tenantId, userId, spaceId, catalogMap, documentServerPathIdMap);

    DocumentCreateRequestDTO createRequest = buildDocumentCreateRequest(fileInfo, libraryId, parentId, spaceId);

    String documentId = documentService.createUploadDocumentNode(userId, createRequest);
    logger.debug("成功创建文档节点，fileInfoId: {}, documentId: {}", fileInfo.getFileInfoId(), documentId);
    return documentId;
  }

  private DocumentCreateRequestDTO buildDocumentCreateRequest(FileInfoDTO fileInfo, String libraryId, String parentId, Long spaceId) {
    DocumentCreateRequestDTO createRequest = new DocumentCreateRequestDTO();
    createRequest.setTenantId(fileInfo.getTenantId());
    createRequest.setSpaceId(spaceId);
    createRequest.setDocumentName(fileInfo.getFileName());
    createRequest.setFileInfoId(fileInfo.getFileInfoId());
    createRequest.setDocumentType(determineDocumentType(fileInfo.getFileName()));
    createRequest.setIsConvert(DocBaseConsts.FALSE);
    createRequest.setLibraryId(libraryId);
    createRequest.setParentId(parentId);
    return createRequest;
  }

  private String resolveCatalogParent(FileInfoDTO fileInfo, String libraryId, Map<Long, Map<String, String>> catalogIdMap, Long tenantId, Long userId, Long spaceId, Map<Long, CatalogDTO> catalogMap, Map<String, String> documentServerPathIdMap) {
    if (fileInfo.getCatalogItemId() == null) {
      return null;
    }

    Map<String, String> stringStringMap = catalogIdMap.get(fileInfo.getCatalogItemId());
    if (stringStringMap == null) {
      return null;
    }

    if (stringStringMap.size() == 1) {
      CatalogDTO catalogDTO = catalogMap.get(fileInfo.getCatalogItemId());
      if (catalogDTO != null) {
        return resolveSingleCatalogParent(catalogDTO.getCatalogName(), libraryId, tenantId, userId, spaceId, documentServerPathIdMap);
      }
      else {
        return null;
      }
    }
    return quseryFolders(stringStringMap, libraryId, catalogMap.get(fileInfo.getCatalogItemId()), tenantId, userId, spaceId, documentServerPathIdMap);
  }

  private String resolveSingleCatalogParent(String catalogName, String libraryId, Long tenantId, Long userId, Long spaceId, Map<String, String> documentServerPathIdMap) {
    if (StringUtils.isNotEmpty(documentServerPathIdMap.get(catalogName))) {
      return documentServerPathIdMap.get(catalogName);
    }
    String parentId = createFolder(catalogName, libraryId, null, tenantId, userId, spaceId);
    documentServerPathIdMap.put(catalogName, parentId);
    return parentId;
  }

  private void updateDocumentDcDocumentId(List<String> processedFileInfoIds) {
    if (processedFileInfoIds.isEmpty()) {
      return;
    }
    int totalUpdatedCount = 0;
    for (String item : processedFileInfoIds) {
      try {
        String[] split = item.split(",");
        Long fileInfoId = Long.valueOf(split[0]);
        Long tenantId = Long.valueOf(split[1]);
        String dcDocumentId = split[2];
        int updatedCount = documentHelper.updateDocumentDcDocumentId(fileInfoId, tenantId, dcDocumentId);
        if (updatedCount > 0) {
          totalUpdatedCount += updatedCount;
          logger.debug("成功更新bt_document表的dc_document_id字段，fileInfoId: {}, 更新记录数: {}", fileInfoId, updatedCount);
        }
      }
      catch (Exception e) {
        logger.error("更新bt_document表的dc_document_id字段失败，fileInfoIdAndTenantId: {}, error: {}", item, e.getMessage(), e);
      }
    }
    logger.info("bt_document表dc_document_id字段更新完成，共处理{}个fileInfoId，累计更新{}条记录",
      processedFileInfoIds.size(), totalUpdatedCount);
  }

  private String quseryFolders(Map<String, String> catalogNameMap, String libraryId, CatalogDTO catalogDTO,
    Long tenantId, Long userId, Long spaceId, Map<String, String> documentServerPathIdMap) {
    String tempParentId = null;
    if (catalogNameMap != null) {
      String catalogName = catalogNameMap.get(catalogDTO.getCatalogName());

      String[] catalogNames = catalogName.split("/");
      for (int i = catalogNames.length - 1; i >= 0; i--) {
        String name = catalogNames[i];
        if (StringUtils.isNotEmpty(documentServerPathIdMap.get(catalogNameMap.get(name)))) {
          tempParentId = documentServerPathIdMap.get(catalogNameMap.get(name));
        }
        else {
          tempParentId = createFolder(name, libraryId, tempParentId, tenantId, userId, spaceId);
          documentServerPathIdMap.put(catalogNameMap.get(name), tempParentId);
        }
      }
    }
    return tempParentId;
  }

  /**
   * 创建文档库文件夹
   */
  private String createFolder(String catalogName, String libraryId, String tempParentId, Long tenantId, Long userId, Long spaceId) {
    // 构建DocumentCreateRequestDTO
    DocumentCreateRequestDTO createRequest = new DocumentCreateRequestDTO();
    createRequest.setTenantId(tenantId);
    createRequest.setSpaceId(spaceId);
    createRequest.setDocumentName(catalogName);
    createRequest.setFileInfoId(null);
    createRequest.setDocumentType(DocumentTypeEnum.FOLDER.getCode());
    createRequest.setIsConvert(DocBaseConsts.FALSE);
    createRequest.setLibraryId(libraryId);
    createRequest.setParentId(tempParentId);
    if (userId == null) {
      userId = -1L;
    }
    return documentService.createUploadDocumentNode(userId, createRequest);
  }

  /**
   * 递归查找父级目录并添加到队列（从根到子节点的顺序）
   *
   * @param catalogDTO 节点
   * @param catalogMap 目录ID到目录对象的映射
   * @param children 目录队列（从根到子节点）
   */
  private void findAndAddParents(CatalogDTO catalogDTO, Map<Long, CatalogDTO> catalogMap, List<CatalogDTO> children) {
    children.add(catalogDTO);
    if (catalogDTO.getParCatalogId() == null || catalogDTO.getParCatalogId() == -1L) {
      return;
    }
    CatalogDTO parent = catalogMap.get(catalogDTO.getParCatalogId());
    if (parent != null) {
      findAndAddParents(parent, catalogMap, children);
    }
  }

  /**
   * 根据文件名确定文档类型
   */
  private String determineDocumentType(String fileName) {
    String extension = StringUtils.substringAfterLast(fileName, ".").toLowerCase();
    return switch (extension) {
      case "doc", "docx" -> DocumentTypeEnum.WORD.getCode();
      case "xls", "xlsx" -> DocumentTypeEnum.EXCEL.getCode();
      case "ppt", "pptx" -> DocumentTypeEnum.PPT.getCode();
      case "pdf" -> DocumentTypeEnum.PDF.getCode();
      case "txt" -> DocumentTypeEnum.TXT.getCode();
      case "md", "mdx" -> DocumentTypeEnum.MD.getCode();
      case "png", "jpg", "jpeg" -> DocumentTypeEnum.IMAGE.getCode();
      default -> DocumentTypeEnum.FILE.getCode();
    };
  }

  @Override
  public ResultVO<Map<String, Object>> queryDocumentInfo(Long tenantId, String knowledgeIdExpr) {
    List<KnowledgeBaseDTO> knowledges = new ArrayList<>();
    List<Long> knowledgeIds = new ArrayList<>();
    if (!knowledgeIdExpr.startsWith("$")) {
      knowledgeIds.addAll(resolveIdsByLiteral(knowledgeIdExpr));
      knowledges.addAll(knowledgeBaseManageMapper.selectSimpleKnowledgeByIds(tenantId, knowledgeIds));
    }
    if (CollectionUtils.isNotEmpty(knowledges)) {
      Map<Long, List<DocumentDTO>> group = CollectionUtils.emptyIfNull(
          documentManageMapper.selectSimpleDocumentByKnowledgeIds(tenantId, knowledgeIds)).stream()
        .collect(Collectors.groupingBy(DocumentDTO::getKnowledgeId));
      for (KnowledgeBaseDTO knowledge : knowledges) {
        knowledge.setDocuments(group.get(knowledge.getKnowledgeId()));
      }
    }
    Map<String, Object> info = new HashMap<>();
    info.put("knowledges", knowledges);
    return ResultVO.success(info);
  }

  /**
   * 取保文档是否存在 更新文档的发布标识或者叫同步标识
   *
   * @param documentId
   */
  @Override
  @Transactional
  public void documentReleased(String documentId) {
    // 延时等待，确保文档发布状态变更后的数据同步完成
    try {
      TimeUnit.SECONDS.sleep(KnowledgeConsts.DOCUMENT_RELEASED_DELAY_SECONDS);
    }
    catch (InterruptedException e) {
      // 恢复中断状态，遵循最佳实践
      Thread.currentThread().interrupt();
      logger.warn("文档发布延时等待被中断: documentId={}", documentId);
      throw new BssException("文档发布处理被中断", e);
    }
    DcDocumentDTO dcDocumentDTO = documentService.findByDocumentId(documentId);
    if (dcDocumentDTO == null) {
      throw new BssException("文档不存在");
    }
    documentService.updateDocumentReleased(documentId, DocBaseConsts.FALSE);
    rebuildBtDocumentByDcDocDocId(documentId, dcDocumentDTO.getTenantId(), SessionUtil.getLoginInfo().getUserId(),
      null);
  }

  @Override
  @Transactional
  public ResultVO<Long> deleteDocuments(DocDeleteDocumentsDTO deleteDocumentsDTO) {
    checkDeleteDocuments(deleteDocumentsDTO);
    if (CollectionUtils.isEmpty(deleteDocumentsDTO.getDocumentDTOS())) {
      return ResultVO.success(deleteDocumentsDTO.getKnowledgeId());
    }
    KnowledgeBaseDTO knowledge = deleteDocumentsDTO.getKnowledge();
    documentManageMapper.deleteDocuments(deleteDocumentsDTO.getTenantId(), deleteDocumentsDTO.getDocumentDTOS().stream().map(DocumentDTO::getDocumentId).toList(), SessionUtil.getLoginInfo().getUserId());
    boolean existsTopic = false;
    if (knowledge.isDocChainType() && knowledge.getTopicId() != null) {
      existsTopic = docChainConfigHelper.existsTopic(deleteDocumentsDTO.getTenantId(), knowledge.getTopicId());
    }

    for (DocumentDTO document : deleteDocumentsDTO.getDocumentDTOS()) {
      if (existsTopic && document.getExtSystemId() != null && docChainConfigHelper.existsTopicDocId(document.getTenantId(), knowledge.getTopicId(), document.getExtSystemId())) {
        // 先查询是否存在然后在进行删除，如果不存在那么直接删除博特的相关知识库文档
        docChainConfigHelper.deleteDocument(document.getTenantId(), document.getExtSystemId());
      }
      btDcQaRecordManageService.deleteBtDcQaRecordInfoByknowledgeId(document.getKnowledgeId(),
        document.getExtSystemId(), deleteDocumentsDTO.getTenantId());
    }

    knowledge.setFileCounts(knowledge.getFileCounts() > 0 ? knowledge.getFileCounts() - deleteDocumentsDTO.getDocumentDTOS().size() : 0);
    knowledgeBaseManageMapper.updateKnowledgeBase(knowledge);
    ResourceElementFactory.get(OperClassEnum.KNOWLEDGE.name()).submit(knowledge.getTenantId(), knowledge.getKnowledgeId());
    // removeDocumentResource(document);
    return ResultVO.success(deleteDocumentsDTO.getKnowledgeId());
  }

  /**
   * @param deleteDocumentsDTO 批量删除知识库文档的参数
   */
  private void checkDeleteDocuments(DocDeleteDocumentsDTO deleteDocumentsDTO) {
    Assert.notNull(deleteDocumentsDTO.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(deleteDocumentsDTO.getKnowledgeId(), "知识库 ID 不能为空");
    if (!DocBaseConsts.TRUE.equals(deleteDocumentsDTO.getClrearAll())) {
      Assert.isTrue(CollectionUtils.isNotEmpty(deleteDocumentsDTO.getDocumentIds()), "批量删除知识库文档文档列表 ID 不能为空");
    }
    KnowledgeBaseDTO knowledge = knowledgeBaseManageMapper.getKnowledgeBase(deleteDocumentsDTO.getTenantId(), deleteDocumentsDTO.getKnowledgeId(), SessionUtil.getLoginInfo().getUserId());
    Assert.notNull(knowledge, () -> "知识库不存在, knowledgeId=" + deleteDocumentsDTO.getKnowledgeId());
    String kbPermissionType = btDcKbPermissionHelper.queryKnowledgeBasePermissionType(knowledge.getKnowledgeId(), knowledge.getOwnerId());
    Assert.isTrue(LibraryRoleEnum.MANAGE.getCode().equals(kbPermissionType) || LibraryRoleEnum.EDIT.getCode().equals(kbPermissionType), () -> "没有知识库的编辑权限");
    deleteDocumentsDTO.setKnowledge(knowledge);

    DocumentQueryParams queryParams = new DocumentQueryParams();
    queryParams.setTenantId(deleteDocumentsDTO.getTenantId());
    queryParams.setKnowledgeId(deleteDocumentsDTO.getKnowledgeId());
    if (!DocBaseConsts.TRUE.equals(deleteDocumentsDTO.getClrearAll())) {
      queryParams.setDocumentIds(deleteDocumentsDTO.getDocumentIds());
    }
    List<DocumentDTO> documentDTOS = documentManageMapper.selectDocumentList(queryParams);
    deleteDocumentsDTO.setDocumentDTOS(documentDTOS);
  }

  /**
   * 解析 ID 列表常量值，字符串必须是单个 ID 或逗号分隔的多个 ID
   */
  private static List<Long> resolveIdsByLiteral(String expr) {
    if (!expr.contains(",")) {
      Assert.isTrue(StringUtils.isNumeric(expr), () -> "ID 不合法: " + expr);
      return Collections.singletonList(Long.parseLong(expr));
    }
    String[] pieces = expr.split("\\s*,\\s*");
    return Arrays.stream(pieces).map(piece -> {
      Assert.isTrue(StringUtils.isNumeric(piece), () -> "ID 不合法: " + expr);
      return Long.parseLong(piece);
    }).distinct().collect(toList());
  }

  /**
   * 获取或创建全员文档库
   *
   * @param tenantId 租户ID
   * @return 文档库ID
   */
  private String getOrCreatePublicLibrary(Long tenantId, Long userId, Long spaceId) {
    // 查询是否已存在全员文档库
    DocumentLibraryEntity existingLibrary = documentManageMapper.selectPublicLibraryByTenantId(tenantId, "全员文档库",
      VisibilityScopeEnum.PUBLIC.getCode());

    if (existingLibrary != null) {
      logger.debug("租户{}已存在全员文档库，libraryId: {}", tenantId, existingLibrary.getLibraryId());
      return existingLibrary.getLibraryId();
    }

    // 创建新的全员文档库
    DocumentLibraryDTO libraryDTO = new DocumentLibraryDTO();
    libraryDTO.setLibraryName("全员文档库");
    libraryDTO.setVisibilityScope(VisibilityScopeEnum.PUBLIC.getCode());
    libraryDTO.setIsBuiltin(DocBaseConsts.TRUE);
    libraryDTO.setDescription("存量文档的全员可见文档库");
    libraryDTO.setTenantId(tenantId);
    libraryDTO.setLibraryId(DcIdUtils.createLibraryId());
    libraryDTO.setSortOrder(0);
    libraryDTO.setCreatorId(userId);
    libraryDTO.setUpdatorId(userId);
    libraryDTO.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    libraryDTO.setIsBuiltin(DocBaseConsts.TRUE);
    libraryDTO.setLibraryIcon(DocBaseConsts.DEFAULT_LIBRARY_ICON);
    libraryDTO.setColor(DocBaseConsts.DEFAULT_LIBRARY_COLOR);
    libraryDTO.setSpaceId(spaceId);
    DocumentLibraryDTO createdLibrary = documentLibraryService.createLibrary(libraryDTO, userId);
    logger.debug("为租户{}创建全员文档库成功，libraryId: {}", tenantId, createdLibrary.getLibraryId());
    return createdLibrary.getLibraryId();
  }
}
