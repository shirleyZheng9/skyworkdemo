package com.iwhalecloud.bote.doc.module.document.service.impl;

import static com.iwhalecloud.bote.common.enums.BaseSystemParameter.DOCUMENT_HIS_LOG_ENABLED;

import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.doc.cache.DocumentLibraryCache;
import com.iwhalecloud.bote.doc.cache.DocumentPathCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.exception.DocumentNodeAccessDenyException;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.common.utils.DcIdUtils;
import com.iwhalecloud.bote.doc.common.utils.DocumentNameUtils;
import com.iwhalecloud.bote.doc.consts.ContentSourceEnum;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.consts.IdRulePrefixEnum;
import com.iwhalecloud.bote.doc.consts.VisibilityScopeEnum;
import com.iwhalecloud.bote.doc.module.base.helper.ImportServiceHelper;
import com.iwhalecloud.bote.doc.module.base.service.impl.FileUploadHelper;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.control.mapper.DocumentNodeMapper;
import com.iwhalecloud.bote.doc.module.control.vo.NodePermissionView;
import com.iwhalecloud.bote.doc.module.crawl.CrawlStepFactory;
import com.iwhalecloud.bote.doc.module.crawl.dto.CrawlResult;
import com.iwhalecloud.bote.doc.module.crawl.step.transformer.ImageTransformer;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDetailDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentContributorDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentCreateRequestDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentNodeUpdateRO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPathDTO;
import com.iwhalecloud.bote.doc.module.document.dto.NodePathDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocumentEntity;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentContributorEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocContentService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentContributorService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentFileLogService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.dtable.service.IDocumentDimTableRelaService;
import com.iwhalecloud.bote.doc.module.collaboration.doc.service.NodeJsService;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.BeanUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 文档服务类
 *
 * @author Aiqing
 * @since 2025/8/15
 */
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements IDocumentService {
  private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

  private static final String DOC_NAME_DEFAULT_WORD = "无标题文档";
  private static final String DOC_NAME_DEFAULT_EXCEL = "无标题表格";
  private static final String DOC_NAME_DEFAULT_FOLD = "新建文件夹";
  private static final String DOC_NAME_DEFAULT_DIM_TABLE = "无标题多维表格";

  /** 文件夹批量上传时跳过单次创建节点触发的库元素重算，由流程结束时统一重算一次，避免死锁。**/
  private static final ThreadLocal<Boolean> SKIP_LIBRARY_COMPUTE = ThreadLocal.withInitial(() -> false);

  /** 文档类型的默认映射名称 **/
  private static final Map<DocumentTypeEnum, String> DOCUMENT_TYPE_DEFAULT_NAME_MAP = new EnumMap<>(DocumentTypeEnum.class);

  private final DocumentMapper documentMapper;
  private final DocumentNodeMapper documentNodeMapper;
  private final DocumentLibraryCache documentLibraryCache;
  private final DocumentLibraryService documentLibraryService;
  private final ControlTemplate controlTemplate;
  private final IDocContentService docContentService;
  private final IDocumentContributorService documentContributorService;
  private final IDcUserService dcUserService;
  private final FileUploadHelper fileUploadHelper;
  private final ImportServiceHelper importServiceHelper;
  private final NodeJsService nodeJsService;
  private final IDocumentAttachmentService documentAttachmentService;
  private final ImageTransformer imageTransformer;

  static {
    DOCUMENT_TYPE_DEFAULT_NAME_MAP.put(DocumentTypeEnum.WORD_ONLINE, DOC_NAME_DEFAULT_WORD);
    DOCUMENT_TYPE_DEFAULT_NAME_MAP.put(DocumentTypeEnum.EXCEL_ONLINE, DOC_NAME_DEFAULT_EXCEL);
    DOCUMENT_TYPE_DEFAULT_NAME_MAP.put(DocumentTypeEnum.FOLDER, DOC_NAME_DEFAULT_FOLD);
    DOCUMENT_TYPE_DEFAULT_NAME_MAP.put(DocumentTypeEnum.DIM_TABLE, DOC_NAME_DEFAULT_DIM_TABLE);
  }

  private final IDocumentDimTableRelaService documentDimTableRelaService;

  private final IDocumentFileLogService documentFileLogService;

  private static @NotNull DocumentPathDTO convertDocumentPathDTO(List<NodePathDTO> nodePathDTOS) {
    DocumentPathDTO documentPathDTO = new DocumentPathDTO();
    List<String> pathCodeList = new ArrayList<>();
    List<String> patchNameList = new ArrayList<>();
    nodePathDTOS.forEach(item -> {
      pathCodeList.add(item.getNodeId());
      patchNameList.add(item.getNodeName());
    });

    documentPathDTO.setDocumentPath(String.join("/", patchNameList));
    documentPathDTO.setDocumentPathCode(String.join("/", pathCodeList));
    documentPathDTO.setPath(nodePathDTOS);
    return documentPathDTO;
  }

  private static @NotNull DcDocumentEntity buildDocumentEntity(Long userId,
    String libraryId,
    String documentName,
    DocumentTypeEnum documentType,
    boolean isBuiltin,
    String parentId,
    Long tenantId,
    Long spaceId) {
    String documentId = DcIdUtils.createDocumentId(documentType);
    // 创建文档实体
    DcDocumentEntity documentEntity = new DcDocumentEntity();
    documentEntity.setId(IDUtils.nextId());
    documentEntity.setLibraryId(libraryId);
    documentEntity.setDocumentId(documentId);
    documentEntity.setDocumentName(documentName);
    documentEntity.setParentId(parentId);
    documentEntity.setDocumentType(documentType.getCode());
    documentEntity.setRevision(DocBaseConsts.DOCUMENT_INIT_REVISION);
    documentEntity.setWordCount(0);
    documentEntity.setViewCount(0);
    documentEntity.setIsConvert(DocBaseConsts.FALSE);
    documentEntity.setIsBuiltin(isBuiltin ? DocBaseConsts.TRUE : DocBaseConsts.FALSE);
    documentEntity.setCreatorId(userId);
    documentEntity.setUpdatorId(userId);
    documentEntity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    documentEntity.setTenantId(tenantId);
    documentEntity.setSpaceId(spaceId);
    documentEntity.setContentSource(ContentSourceEnum.ONLINE.getCode());
    return documentEntity;
  }

  @Override
  public String initLibraryRootDocumentNode(String libraryId, String libraryName, Long tenantId, Long spaceId) {
    String existRootNode = documentNodeMapper.selectRootNodeIdByLibraryId(libraryId, tenantId, spaceId);
    if (StringUtils.isNotBlank(existRootNode)) {
      return existRootNode;
    }
    DcDocumentEntity documentEntity = initDocumentAndSave(0L, libraryId, libraryName, DocumentTypeEnum.ROOT, true, "0", tenantId, spaceId, null);
    return documentEntity.getDocumentId();
  }

  @Override
  public DocumentPathDTO getDocumentPath(String documentId) {
    // 避免循环注入
    DocumentPathCache documentPathCache = SpringUtil.getBean(DocumentPathCache.class);
    List<NodePathDTO> nodePathDTOS = documentPathCache.getPath(documentId);
    if (CollectionUtils.isEmpty(nodePathDTOS)) {
      return new DocumentPathDTO();
    }
    // 当存在 nodeName 为空的情况时，直接返回空对象，不进行路径转换
    boolean hasEmptyNodeName = nodePathDTOS.stream()
      .anyMatch(item -> item == null || StringUtils.isBlank(item.getNodeName()));
    if (hasEmptyNodeName) {
      return new DocumentPathDTO();
    }
    return convertDocumentPathDTO(nodePathDTOS);
  }

  @Override
  public Map<String, DocumentPathDTO> getDocumentPathBatch(List<String> documentIdList) {
    if (CollectionUtils.isEmpty(documentIdList)) {
      return Collections.emptyMap();
    }
    DocumentPathCache documentPathCache = SpringUtil.getBean(DocumentPathCache.class);
    Map<String, List<NodePathDTO>> multiPaths = documentPathCache.mget(documentIdList);

    Map<String, DocumentPathDTO> pathMap = new LinkedHashMap<>();
    for (String documentId : documentIdList) {
      List<NodePathDTO> pathList = multiPaths.get(documentId);
      if (CollectionUtils.isEmpty(pathList)) {
        // 回退单条查询，确保缓存缺失时可以加载
        DocumentPathDTO singlePath = getDocumentPath(documentId);
        pathMap.put(documentId, singlePath);
        continue;
      }
      boolean hasEmptyNodeName = pathList.stream()
        .anyMatch(item -> item == null || StringUtils.isBlank(item.getNodeName()));
      if (hasEmptyNodeName) {
        pathMap.put(documentId, new DocumentPathDTO());
        continue;
      }
      pathMap.put(documentId, convertDocumentPathDTO(pathList));
    }
    return pathMap;
  }

  @Override
  public String getLibraryIdByDocument(String documentId) {
    return documentMapper.selectLibraryIdByDocumentId(documentId);
  }

  @Override
  public DcDocumentDTO findByDocumentId(String documentId) {
    DcDocumentEntity documentEntity = documentMapper.selectByDocumentId(documentId);
    if (documentEntity == null) {
      return null;
    }
    // 此处不能使用缓存，需要直接查库，可能存在忽略租户的直接查询
    String libraryName = documentLibraryService.getNameById(documentEntity.getLibraryId());
    DcDocumentDTO documentDTO = BeanUtil.copy(documentEntity, DcDocumentDTO.class);
    documentDTO.setLibraryName(libraryName);
    return documentDTO;
  }

  @Override
  public DcDocumentDTO findByDocumentIdAndTenantId(String documentId, Long tenantId, String platform) {
    DcDocumentEntity documentEntity = documentMapper.selectByDocumentIdAndTenantId(documentId, tenantId, platform);
    if (documentEntity == null) {
      return null;
    }
    String libraryName = documentLibraryCache.getLibraryName(documentEntity.getLibraryId());
    DcDocumentDTO documentDTO = BeanUtil.copy(documentEntity, DcDocumentDTO.class);
    documentDTO.setLibraryName(libraryName);
    return documentDTO;
  }

  private static List<DocumentContributorDTO> convertContributorDTO(String documentId,
    List<DocumentContributorEntity> contributorEntities,
    Map<Long, PortalUserDTO> userMap) {
    if (CollectionUtils.isEmpty(contributorEntities)) {
      return Collections.emptyList();
    }
    return contributorEntities.stream().map(item -> {
      DocumentContributorDTO contributorDTO = new DocumentContributorDTO();
      contributorDTO.setDocumentId(documentId);
      contributorDTO.setContributorScore(item.getContributorScore());
      contributorDTO.setUserId(item.getUserId());
      PortalUserDTO portalUserDTO = userMap.get(item.getUserId());
      if (portalUserDTO != null) {
        contributorDTO.setUserName(portalUserDTO.getUserName());
        contributorDTO.setUserCode(portalUserDTO.getUserCode());
      }
      return contributorDTO;
    }).toList();
  }

  @Override
  public DcDocumentDetailDTO findDetailByDocumentId(String documentId, Long userId) {
    DcDocumentEntity documentEntity = documentMapper.selectByDocumentId(documentId);
    if (documentEntity == null) {
      return null;
    }
    TenantContextHolder.setTenantId(documentEntity.getTenantId());
    TenantContextHolder.setIgnore(false);

    ControlRole controlRole = controlTemplate.fetchNodeRole(documentEntity.getLibraryId(), userId, documentId);
    boolean hasPermission = controlRole.hasPermission(NodePermission.READ_NODE);
    if (!hasPermission) {
      throw new DocumentNodeAccessDenyException();
    }
    DocumentLibraryDTO documentLibraryDTO = documentLibraryCache.getByLibraryId(documentEntity.getLibraryId());
    Assert.notNull(documentLibraryDTO, "文档库不存在");

    DcDocumentDetailDTO documentDTO = BeanUtil.copy(documentEntity, DcDocumentDetailDTO.class);
    documentDTO.setLibraryName(documentLibraryDTO.getLibraryName());
    DocumentPathDTO documentPath = this.getDocumentPath(documentId);
    documentDTO.setDocumentPath(documentPath);

    documentDTO.setPermission(controlRole.getRoleTag());
    documentDTO.setPermissionView(controlRole.permissionToBean(NodePermissionView.class));
    List<DocumentContributorEntity> contributorEntities = documentContributorService.findByDocumentId(documentId);

    List<Long> allUsers = new ArrayList<>();
    allUsers.add(documentEntity.getCreatorId());
    allUsers.add(documentEntity.getUpdatorId());

    List<Long> contributorUsers = Optional.ofNullable(contributorEntities).orElse(new ArrayList<>())
      .stream().map(DocumentContributorEntity::getUserId).toList();
    allUsers.addAll(contributorUsers);
    Map<Long, PortalUserDTO> userMap = dcUserService.findUserMapBatchByIds(allUsers);

    List<DocumentContributorDTO> contributorDTOList = convertContributorDTO(documentId, contributorEntities, userMap);
    documentDTO.setContributors(contributorDTOList);

    documentDTO.setCreator(userMap.get(documentEntity.getCreatorId()));
    documentDTO.setUpdator(userMap.get(documentEntity.getUpdatorId()));

    // 特殊处理文档库名称，和文档路径
    String visibilityScope = documentLibraryDTO.getVisibilityScope();
    if (VisibilityScopeEnum.PRIVATE.getCode().equals(visibilityScope)
      && !Objects.equals(userId, documentLibraryDTO.getOwnerId())) {
      PortalUserDTO portalUserDTO = dcUserService.findUserById(documentLibraryDTO.getOwnerId());
      if (portalUserDTO != null) {
        String libraryName = String.format("%s的文档", portalUserDTO.getUserName());
        documentDTO.setLibraryName(libraryName);
        documentPath.getPath().get(0).setNodeName(libraryName);
        String newPath = documentPath.getPath()
          .stream().map(NodePathDTO::getNodeName).collect(Collectors.joining("/"));
        documentPath.setDocumentPath(newPath);
      }
    }

    return documentDTO;
  }

  @Override
  @Transactional
  public String createOnlineDocumentNode(Long userId, DocumentCreateRequestDTO documentCreateRequest) {
    String parentId = documentCreateRequest.getParentId();
    if (StringUtils.isNotBlank(parentId)) {
      // 检查上级节点是否存在, 且是否是下级节点
      DcDocumentEntity documentEntity = documentMapper.selectByDocumentId(parentId);
      if (documentEntity == null) {
        throw new BssException("400", "父节点不存在");
      }
    }
    else {
      // 默认在根节点下
      parentId = documentNodeMapper.selectRootNodeIdByLibraryId(documentCreateRequest.getLibraryId(), documentCreateRequest.getTenantId(), documentCreateRequest.getSpaceId());
    }
    // 根据文件类型，生成默认文件名
    DocumentTypeEnum documentType = DocumentTypeEnum.valueOf(documentCreateRequest.getDocumentType());
    String documentName = generateDefaultDocumentName(documentType, documentCreateRequest.getLibraryId());
    Long spaceId = SpaceContextHolder.getSpaceId();
    DcDocumentEntity documentEntity = initDocumentAndSave(userId, documentCreateRequest.getLibraryId(), documentName,
      documentType, false, parentId, documentCreateRequest.getTenantId(), spaceId, null);

    // 根据不同的文档类型，初始化扩展表数据
    initDocumentMetaData(documentEntity, userId, documentType);
    return documentEntity.getDocumentId();
  }

  /**
   * 创建内置文件夹
   */
  @Override
  @Transactional
  public void createBuiltinFolder(Long userId, Long spaceId, Long tenantId, String builtinType,
    String folderName, String libraryId, String rootNodeId) {
    DcDocumentEntity folder = new DcDocumentEntity();
    folder.setId(IDUtils.nextId());
    folder.setDocumentId(DcIdUtils.createDocumentId(DocumentTypeEnum.FOLDER));
    folder.setDocumentName(folderName);
    folder.setLibraryId(libraryId);
    folder.setParentId(rootNodeId);
    folder.setDocumentType(DocumentTypeEnum.FOLDER.getCode());
    folder.setContentSource(ContentSourceEnum.ONLINE.getCode());
    folder.setIsBuiltin(DocBaseConsts.TRUE);
    folder.setBuiltinType(builtinType);
    folder.setCreatorId(userId);
    folder.setSpaceId(spaceId);
    folder.setTenantId(tenantId);
    folder.setUpdatorId(userId);
    folder.setRevision(DocBaseConsts.DOCUMENT_INIT_REVISION);
    folder.setIsConvert(DocBaseConsts.FALSE);
    folder.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    folder.setContentSource(ContentSourceEnum.ONLINE.getCode());
    documentMapper.insert(folder);

    // 新建的文档默认放在最前方
    String firstSiblingDocumentId = getFirstSiblingDocumentId(rootNodeId);
    this.updateSiblingPreDocumentId(firstSiblingDocumentId, folder.getDocumentId());
  }

  @Override
  @Transactional
  public void updateDocumentBasicInfo(DcDocumentDTO existDocument, Long userId, DocumentNodeUpdateRO updateRO) {
    String documentId = existDocument.getDocumentId();
    // 处理文档后缀
    String contentSource = existDocument.getContentSource();
    if (Objects.equals(contentSource, ContentSourceEnum.UPLOAD.getCode())) {
      // 拼接旧的后缀，只修改名称
      String fileNameWithoutExt = FilenameUtils.removeExtension(updateRO.getDocumentName());
      String extension = FilenameUtils.getExtension(existDocument.getDocumentName());
      String newFileName = fileNameWithoutExt + DocBaseConsts.DOT + extension;

      updateRO.setDocumentName(newFileName);
    }
    if (!updateRO.getDocumentName().equals(existDocument.getDocumentName())
      && existsDocumentByName(existDocument.getLibraryId(), existDocument.getParentId(), documentId, updateRO.getDocumentName(), existDocument.getDocumentType(), existDocument.getContentSource())) {
      throw new BssException("该文档库的同级文件夹下存在相同名字的文档或者文件夹！");
    }

    if (Objects.equals(contentSource, ContentSourceEnum.UPLOAD.getCode())) {
      // 更新 file_info 文档名称
      fileUploadHelper.updateFileInfoName(existDocument.getFileInfoId(), updateRO.getDocumentName(), existDocument.getTenantId());
    }

    documentMapper.updateDocumentBasicInfo(documentId, userId, updateRO.getDocumentName());
  }

  @Override
  @Transactional
  public void updateUploadDocumentNameWhenReUp(String documentId, String documentName, Long userId, String documentType) {
    DcDocumentDTO documentDTO = this.findByDocumentId(documentId);
    Assert.notNull(documentDTO, "文档不存在");
    documentMapper.updateUploadDocumentName(documentId, userId, documentName, documentType);
  }

  @Override
  public DcDocumentDTO checkDocumentIfExist(String libraryId, String documentId) {
    DcDocumentDTO documentDTO = this.findByDocumentId(documentId);
    Assert.notNull(documentDTO, "文档不存在");
    Assert.isTrue(Objects.equals(libraryId, documentDTO.getLibraryId()), "参数异常，文档库ID不一致");
    return documentDTO;
  }

  @Override
  public List<DcDocumentDTO> findBatchByDocumentId(List<String> documentIds) {
    return documentMapper.findBatchByDocumentId(documentIds);
  }

  @Override
  public Map<String, String> findDocumentLibraryMapping(List<String> documentIds) {
    if (CollectionUtils.isEmpty(documentIds)) {
      return new HashMap<>();
    }
    List<DcDocumentDTO> documentDTOList = documentMapper.findLibraryBatchByDocumentId(documentIds);
    return documentDTOList.stream()
      .collect(Collectors.toMap(DcDocumentDTO::getDocumentId, DcDocumentDTO::getLibraryId));
  }

  @Override
  @Transactional
  public String createUploadDocumentNode(Long userId, DocumentCreateRequestDTO documentCreateRequest) {
    // 设置租户ID到请求对象
    documentCreateRequest.setTenantId(TenantContextHolder.getTenantId());

    // 处理父节点ID
    String parentId = documentCreateRequest.getParentId();
    String libraryId = documentCreateRequest.getLibraryId();

    // 校验父节点权限
    if (StringUtils.isNotBlank(parentId)) {
      // 检查上级节点是否存在, 且是否是下级节点
      DcDocumentEntity parentEntity = documentMapper.selectByDocumentId(parentId);
      if (parentEntity == null) {
        throw new BssException("400", "父节点不存在");
      }

      // 检查是否属于同一文档库
      if (!Objects.equals(libraryId, parentEntity.getLibraryId())) {
        throw new BssException("400", "父节点不属于指定的文档库");
      }

      // 校验父节点租户
      validateParentTenant(parentEntity, documentCreateRequest.getTenantId());

      String parentDocumentType = parentEntity.getDocumentType();
      if (!Objects.equals(parentDocumentType, DocumentTypeEnum.ROOT.getCode()) && !Objects.equals(parentDocumentType,
        DocumentTypeEnum.FOLDER.getCode())) {
        throw new BssException("400", "只能在文件夹下新建节点");
      }
    }
    else {
      // 默认在根节点下
      parentId = documentNodeMapper.selectRootNodeIdByLibraryId(libraryId, documentCreateRequest.getTenantId(), documentCreateRequest.getSpaceId());
      documentCreateRequest.setParentId(parentId);
    }
    // 创建上传文档实体
    DcDocumentEntity documentEntity = createUploadDocumentEntity(userId, documentCreateRequest);
    if (shouldComputeLibraryForCurrentRequest()) {
      libraryCompute(libraryId);
    }
    return documentEntity.getDocumentId();
  }

  /**
   * 校验父节点是否属于指定的租户
   *
   * @param parentEntity 父节点实体
   * @param requestTenantId 请求的租户ID
   */
  private void validateParentTenant(DcDocumentEntity parentEntity, Long requestTenantId) {
    // 检查是否属于同一租户（如果是"我的文档"则跳过此校验）
    if (!Objects.equals(requestTenantId, parentEntity.getTenantId())) {
      throw new BssException("400", "父节点不属于指定的租户");
    }
  }

  @Override
  public DcDocumentDTO findByNameAndParent(String documentName, String parentId, String libraryId, Long tenantId) {
    return documentMapper.selectByNameAndParent(documentName, parentId, libraryId, tenantId);
  }

  @Override
  public void updateLibraryRootNodeName(String libraryId, String libraryName) {
    documentMapper.updateLibraryRootNodeName(libraryId, libraryId);
  }

  @Override
  public void updateLastModify(String documentId, Long updatorId) {
    documentMapper.updateLastModify(documentId, updatorId);
  }

  @Override
  public boolean existsDocumentByName(String libraryId, String parentId, String documentId, String fileName, String documentType,
    String contentSource) {
    return documentMapper.existsDocumentByName(libraryId, parentId, documentId, fileName, documentType, contentSource);
  }

  /**
   * 创建上传文档实体
   */
  private DcDocumentEntity createUploadDocumentEntity(Long userId, DocumentCreateRequestDTO documentCreateRequest) {
    DocumentTypeEnum documentType = DocumentTypeEnum.valueOf(documentCreateRequest.getDocumentType());
    // 处理文档名称：优先使用传入的名称，否则生成默认名称
    String documentName = StringUtils.isNotBlank(documentCreateRequest.getDocumentName())
      ? documentCreateRequest.getDocumentName()
      : generateDefaultDocumentName(documentType, documentCreateRequest.getLibraryId());

    if (documentCreateRequest.getSpaceId() == null) {
      documentCreateRequest.setSpaceId(SpaceContextHolder.getSpaceId());
    }

    // 根据是否指定前置文档ID选择不同的创建方法
    DcDocumentEntity documentEntity;
    if (StringUtils.isNotBlank(documentCreateRequest.getPrevDocumentId())) {
      // 指定了前置文档，使用自定义顺序
      documentEntity = initDocumentAndSaveWithOrder(userId, documentCreateRequest.getLibraryId(), documentName, documentType, false,
        documentCreateRequest.getParentId(), documentCreateRequest.getPrevDocumentId(), documentCreateRequest.getTenantId(), documentCreateRequest.getSpaceId(), documentCreateRequest.getDataUrl());
    }
    else {
      // 没有指定前置文档，使用默认顺序（放在最前方）
      documentEntity = initDocumentAndSave(userId, documentCreateRequest.getLibraryId(), documentName, documentType, false,
        documentCreateRequest.getParentId(), documentCreateRequest.getTenantId(), documentCreateRequest.getSpaceId(), documentCreateRequest.getDataUrl());
    }

    // 处理上传文档特有的字段
    if (documentCreateRequest.getFileInfoId() != null) {
      documentEntity.setFileInfoId(documentCreateRequest.getFileInfoId());
      documentEntity.setContentSource(ContentSourceEnum.UPLOAD.getCode());
      // 设置上传文档的版本号
      documentEntity.setRevision(DocBaseConsts.DOCUMENT_INIT_REVISION);
      // 设置转换标识
      if (StringUtils.isNotBlank(documentCreateRequest.getIsConvert())) {
        documentEntity.setIsConvert(documentCreateRequest.getIsConvert());
      }
      else {
        documentEntity.setIsConvert(DocBaseConsts.FALSE);
      }
      // 更新文档实体
      documentMapper.updateByDocumentId(documentEntity);
      if (DOCUMENT_HIS_LOG_ENABLED.getBooleanValueFromDb()) {
        documentFileLogService.appendByFileInfoId(documentEntity.getDocumentId(), documentCreateRequest.getFileInfoId(),
          documentEntity.getRevision(), userId, documentCreateRequest.getTenantId());
      }
    }
    if (shouldComputeLibraryForCurrentRequest()) {
      libraryCompute(documentCreateRequest.getLibraryId());
    }
    return documentEntity;
  }

  @Override
  @Transactional
  public void updateDocumentPermissionMode(String documentId, Integer permissionMode, Long updatorId) {
    documentMapper.updateDocumentPermissionMode(documentId, permissionMode, updatorId);
  }

  @Override
  @Transactional
  public void convertToOnlineIfNeeded(String aTrue, DcDocumentDTO documentDTO, Long userId) {
    importServiceHelper.convertToOnlineIfNeeded(aTrue, documentDTO, userId);
  }

  @Override
  @Transactional
  public void updateDocumentReleased(String documentId, String released) {
    DcDocumentEntity dcDocumentEntity = documentMapper.selectByDocumentId(documentId);
    if (dcDocumentEntity != null && !released.equals(dcDocumentEntity.getReleased())) {
      documentMapper.updateReleasedType(documentId, released);
    }
  }

  @Override
  public Map<String, String> findFoldersByLibraryId(String libraryId, Long tenantId) {
    Map<String, String> objectObjectHashMap = new HashMap<>();
    List<DcDocumentEntity> dtoList = documentMapper.findFoldersByLibraryId(libraryId, tenantId);
    if (CollectionUtils.isEmpty(dtoList)) {
      return objectObjectHashMap;
    }
    // 构建目录ID到目录对象的映射，便于快速查找
    Map<String, DcDocumentEntity> dcDocumentMap = dtoList.stream().collect(Collectors.toMap(DcDocumentEntity::getDocumentId, catalog -> catalog, (existing, replacement) -> existing));

    for (DcDocumentEntity dto : dtoList) {
      List<DcDocumentEntity> tempObj = new ArrayList<>();
      subNodes(tempObj, dto, dcDocumentMap);
      if (CollectionUtils.isNotEmpty(tempObj)) {
        // 获取当前节点的 documentId 作为 key
        String documentId = dto.getDocumentId();
        // 拼接 tempObj 中所有元素的 documentName（从当前节点开始）
        StringBuilder pathBuilder = new StringBuilder();
        for (DcDocumentEntity entity : tempObj) {
          if (StringUtils.isNotEmpty(entity.getDocumentName())) {
            if (!pathBuilder.isEmpty()) {
              pathBuilder.append("/");
            }
            pathBuilder.append(entity.getDocumentName());
          }
        }
        objectObjectHashMap.put(pathBuilder.toString(), documentId);
      }
    }
    return objectObjectHashMap;
  }

  private void initDocumentMetaData(DcDocumentEntity documentEntity, Long userId, DocumentTypeEnum documentType) {
    String documentId = documentEntity.getDocumentId();
    if (Objects.equals(DocumentTypeEnum.WORD_ONLINE, documentType)) {
      // 文档扩展表记录初始化
      docContentService.initDocContentEntity(documentId);
    }
    if (Objects.equals(DocumentTypeEnum.DIM_TABLE, documentType)) {
      // 多维表格创建, 远程调用
      documentDimTableRelaService.createDimTable(documentId, userId);
    }

    // 创建人加到贡献者列表
    documentContributorService.addOrUpdateContributor(documentId, userId, BigDecimal.ZERO, userId);

    // 此方法为预留接口，用于未来扩展不同文档类型的元数据初始化
  }

  private void subNodes(List<DcDocumentEntity> tempObj, DcDocumentEntity dto, Map<String, DcDocumentEntity> dcDocumentMap) {
    if (dto == null) {
      return;
    }
    tempObj.add(dto);
    if (StringUtils.isNotEmpty(dto.getParentId()) && dto.getParentId().startsWith(IdRulePrefixEnum.FOLD.getPrefix())) {
      DcDocumentEntity dcDocumentEntity = dcDocumentMap.get(dto.getParentId());
      subNodes(tempObj, dcDocumentEntity, dcDocumentMap);
    }
  }

  /**
   * 创建文档实体并保存（支持自定义顺序）
   */
  private DcDocumentEntity initDocumentAndSaveWithOrder(Long userId,
    String libraryId,
    String documentName,
    DocumentTypeEnum documentType,
    boolean isBuiltin,
    String parentId,
    String prevDocumentId,
    Long tenantId,
    Long spaceId,
    String dataUrl) {
    DcDocumentEntity documentEntity = buildDocumentEntity(userId, libraryId, documentName,
      documentType, isBuiltin, parentId, tenantId, spaceId);
    documentEntity.setDataUrl(dataUrl);

    // 处理文档顺序关系
    if (StringUtils.isNotBlank(prevDocumentId)) {
      // 指定了前置文档ID，将新文档插入到指定位置
      this.updateSiblingPreDocumentId(prevDocumentId, documentEntity.getDocumentId());
    }
    else {
      // 没有指定前置文档，将新文档放在最前方
      String firstSiblingDocumentId = getFirstSiblingDocumentId(parentId);
      this.updateSiblingPreDocumentId(firstSiblingDocumentId, documentEntity.getDocumentId());
    }

    // 插入数据库
    documentMapper.insert(documentEntity);
    return documentEntity;
  }

  /**
   * 生成默认文档名称，包含智能序号管理
   *
   * @param documentType 文档类型
   * @param libraryId 文档库ID
   * @return 生成的文档名称
   */
  private String generateDefaultDocumentName(DocumentTypeEnum documentType, String libraryId) {
    // todo 缓存优化
    // 根据文档类型确定基础名称
    String baseName = getBaseDocumentName(documentType);

    // 查询整个文档库下的所有文档名称
    List<String> existingNames = documentMapper.selectDocumentNamesByLibraryId(libraryId);

    // 使用工具类生成默认文档名称
    return DocumentNameUtils.generateDefaultDocumentName(baseName, existingNames);
  }

  /**
   * 根据文档类型获取基础文档名称
   *
   * @param documentType 文档类型
   * @return 基础文档名称
   */
  private String getBaseDocumentName(DocumentTypeEnum documentType) {
    return DOCUMENT_TYPE_DEFAULT_NAME_MAP.getOrDefault(documentType, DOC_NAME_DEFAULT_WORD);
  }

  /**
   * 获取同级目录下的第一个文档ID
   *
   * @param parentId 父目录ID
   * @return 第一个文档ID，如果没有则返回null
   */
  private String getFirstSiblingDocumentId(String parentId) {
    return documentMapper.selectFirstSiblingDocumentId(parentId);
  }

  /**
   * 更新文档的前置节点
   *
   * @param documentId 文档ID
   * @param preDocumentId 前置文档ID
   */
  private void updateSiblingPreDocumentId(String documentId, String preDocumentId) {
    documentMapper.updateSiblingPreDocumentId(documentId, preDocumentId, LocalDateTime.now());
  }

  private DcDocumentEntity initDocumentAndSave(Long userId,
    String libraryId,
    String documentName,
    DocumentTypeEnum documentType,
    boolean isBuiltin,
    String parentId,
    Long tenantId,
    Long spaceId,
    String dataUrl) {
    DcDocumentEntity documentEntity = buildDocumentEntity(userId, libraryId, documentName, documentType, isBuiltin, parentId, tenantId, spaceId);
    documentEntity.setDataUrl(dataUrl);

    // 新建的文档默认放在最前方
    String firstSiblingDocumentId = getFirstSiblingDocumentId(parentId);
    this.updateSiblingPreDocumentId(firstSiblingDocumentId, documentEntity.getDocumentId());
    // 插入数据库
    documentMapper.insert(documentEntity);
    return documentEntity;
  }

  @Override
  @Transactional
  public List<String> batchCreateUploadDocumentNodes(Long userId, List<DocumentCreateRequestDTO> documentCreateRequests) {
    if (CollectionUtils.isEmpty(documentCreateRequests)) {
      return new ArrayList<>();
    }

    Long tenantId = TenantContextHolder.getTenantId();
    Long spaceId = SpaceContextHolder.getSpaceId();

    List<DcDocumentEntity> documentEntities = buildBatchDocumentEntities(userId, documentCreateRequests, tenantId, spaceId);
    List<String> documentIds = documentEntities.stream().map(DcDocumentEntity::getDocumentId).toList();
    // 直接在插入前设置同父目录下新文档的 prevDocumentId 链，避免额外的更新操作
    linkPrevForNewGroups(documentCreateRequests, documentEntities);
    documentMapper.batchInsert(documentEntities);
    if (BaseSystemParameter.DOCUMENT_HIS_LOG_ENABLED.getBooleanValueFromDb()) {
      for (DcDocumentEntity documentEntity : documentEntities) {
        if (documentEntity.getFileInfoId() != null
          && ContentSourceEnum.UPLOAD.getCode().equals(documentEntity.getContentSource())
          && documentEntity.getRevision() != null) {
          documentFileLogService.appendByFileInfoId(documentEntity.getDocumentId(), documentEntity.getFileInfoId(),
            documentEntity.getRevision(), userId, tenantId);
        }
      }
    }
    batchAddContributors(documentEntities, userId);
    if (shouldComputeLibraryForCurrentRequest()) {
      libraryCompute(documentCreateRequests.getFirst().getLibraryId());
    }
    return documentIds;
  }

  /**
   * 构建批量文档实体列表
   */
  private List<DcDocumentEntity> buildBatchDocumentEntities(Long userId,
    List<DocumentCreateRequestDTO> documentCreateRequests,
    Long tenantId,
    Long spaceId) {
    List<DcDocumentEntity> documentEntities = new ArrayList<>();
    for (DocumentCreateRequestDTO request : documentCreateRequests) {
      request.setTenantId(tenantId);
      String parentId = resolveParentId(request, tenantId, spaceId);
      DcDocumentEntity documentEntity = buildBatchDocumentEntity(userId, request, parentId, tenantId, spaceId);
      processUploadDocumentFields(documentEntity, request);
      documentEntities.add(documentEntity);
    }
    return documentEntities;
  }

  /**
   * 为同一父目录分组内的新文档按顺序设置 prevDocumentId（new[i].prev = new[i-1]）。
   */
  private void linkPrevForNewGroups(List<DocumentCreateRequestDTO> documentCreateRequests,
    List<DcDocumentEntity> documentEntities) {
    Map<String, List<Integer>> indicesByParent = new LinkedHashMap<>();
    for (int i = 0; i < documentCreateRequests.size(); i++) {
      String parentId = documentCreateRequests.get(i).getParentId();
      indicesByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(i);
    }
    for (Map.Entry<String, List<Integer>> entry : indicesByParent.entrySet()) {
      List<Integer> idxList = entry.getValue();
      for (int j = 0; j < idxList.size(); j++) {
        DcDocumentEntity curr = documentEntities.get(idxList.get(j));
        if (j == 0) {
          curr.setPrevDocumentId(null);
        }
        else {
          DcDocumentEntity prev = documentEntities.get(idxList.get(j - 1));
          curr.setPrevDocumentId(prev.getDocumentId());
        }
      }
    }
  }

  /**
   * 解析父节点ID
   */
  private String resolveParentId(DocumentCreateRequestDTO request, Long tenantId, Long spaceId) {
    String parentId = request.getParentId();
    if (StringUtils.isBlank(parentId)) {
      parentId = documentNodeMapper.selectRootNodeIdByLibraryId(request.getLibraryId(), tenantId, spaceId);
      request.setParentId(parentId);
    }
    return parentId;
  }

  /**
   * 构建单个批量文档实体
   */
  private DcDocumentEntity buildBatchDocumentEntity(Long userId,
    DocumentCreateRequestDTO request,
    String parentId,
    Long tenantId,
    Long spaceId) {
    DocumentTypeEnum documentType = DocumentTypeEnum.valueOf(request.getDocumentType());
    String documentName = StringUtils.isNotBlank(request.getDocumentName())
      ? request.getDocumentName()
      : generateDefaultDocumentName(documentType, request.getLibraryId());
    return buildDocumentEntity(userId, request.getLibraryId(), documentName, documentType, false, parentId, tenantId, spaceId);
  }

  /**
   * 处理上传文档特有字段
   */
  private void processUploadDocumentFields(DcDocumentEntity documentEntity, DocumentCreateRequestDTO request) {
    if (request.getFileInfoId() != null) {
      documentEntity.setFileInfoId(request.getFileInfoId());
      documentEntity.setContentSource(ContentSourceEnum.UPLOAD.getCode());
      documentEntity.setRevision(DocBaseConsts.DOCUMENT_INIT_REVISION);
      documentEntity.setIsConvert(StringUtils.isNotBlank(request.getIsConvert())
        ? request.getIsConvert()
        : DocBaseConsts.FALSE);
    }
  }

  /**
   * 批量添加贡献者
   */
  private void batchAddContributors(List<DcDocumentEntity> documentEntities, Long userId) {
    List<String> documentIdList = documentEntities.stream().map(DcDocumentEntity::getDocumentId).toList();
    if (!CollectionUtils.isEmpty(documentIdList)) {
      documentContributorService.batchAddOrUpdateContributors(documentIdList, userId, BigDecimal.ZERO, userId);
    }
  }

  private void libraryCompute(String libraryId) {
    DocumentLibraryDTO documentLibraryDTO = documentLibraryService.findByLibraryId(libraryId);
    ResourceElementFactory.get(OperClassEnum.LIBRARY.name()).submit(documentLibraryDTO.getTenantId(), documentLibraryDTO.getId());
  }

  /** 当前请求是否应执行库元素重算（文件夹批量上传时跳过，由流程结束时统一重算） */
  private boolean shouldComputeLibraryForCurrentRequest() {
    return !Boolean.TRUE.equals(SKIP_LIBRARY_COMPUTE.get());
  }

  @Override
  public void setSkipLibraryComputeForFolderUpload(boolean skip) {
    if (skip) {
      SKIP_LIBRARY_COMPUTE.set(true);
    }
    else {
      SKIP_LIBRARY_COMPUTE.remove();
    }
  }

  @Override
  public void recomputeLibraryElements(String libraryId) {
    libraryCompute(libraryId);
  }

  @Override
  public String crawlWebPageAndCreateDocument(String url, String libraryId, String parentId, String documentName,
    Long userId, Long tenantId, Long spaceId) {
    // 1. 获取文档名称（如果未指定，则从网页中提取标题）

    // 2. 处理父节点ID
    String actualParentId = parentId;
    if (StringUtils.isBlank(actualParentId)) {
      // 默认在根节点下
      actualParentId = documentNodeMapper.selectRootNodeIdByLibraryId(libraryId, tenantId, spaceId);
    }

    // 3. 检查是否已存在同名文档
    DcDocumentDTO existingDocument = findByNameAndParent(documentName, actualParentId, libraryId, tenantId);
    if (existingDocument != null) {
      throw new BssException("已存在同名文档: " + documentName);
    }

    // 4. 爬取网页内容（清理后的HTML）
    ResultVO<CrawlResult> crawlResultResultVO = CrawlStepFactory.fetchMarkdownContent(url);
    CrawlResult resultObject = crawlResultResultVO.getResultObject();
    if (resultObject == null) {
      throw new BssException("对端系统做了反爬策略，获取内容失败!");
    }
    String htmlContent = resultObject.getMarkdown();
    if (StringUtils.isBlank(htmlContent)) {
      throw new BssException("对端系统做了反爬策略或者页面为空，获取内容失败!");
    }
    // 5. 创建在线文档
    DocumentCreateRequestDTO createRequest = new DocumentCreateRequestDTO();
    createRequest.setLibraryId(libraryId);
    createRequest.setParentId(actualParentId);
    createRequest.setDocumentType(DocumentTypeEnum.WORD_ONLINE.getCode());
    createRequest.setDocumentName(documentName);
    createRequest.setTenantId(tenantId);
    createRequest.setSpaceId(spaceId);
    createRequest.setDataUrl(url);
    createRequest.setDataUrl(url);

    String documentId = createUploadDocumentNode(userId, createRequest);
    logger.info("在线文档创建成功: documentId={}, documentName={}", documentId, documentName);

    // 6. 下载并上传图片，替换图片URL
    htmlContent = downloadAndUploadImages(htmlContent, documentId, userId, resultObject.getFileInfos());

    // 7. 导入HTML内容到在线文档
    nodeJsService.importDocumentContent(documentId, "markdown", htmlContent, userId.toString());
    logger.info("网页内容导入在线文档成功: documentId={}, url={}", documentId, url);

    return documentId;
  }

  /**
   * 下载并上传图片，替换HTML中的图片URL
   *
   * @param htmlContent HTML内容
   * @param documentId 文档ID
   * @param userId 用户ID
   * @return 替换图片URL后的HTML内容
   */
  private String downloadAndUploadImages(String htmlContent, String documentId, Long userId,
    List<FileInfoVO> fileInfos) {
    if (StringUtils.isBlank(htmlContent) || StringUtils.isBlank(documentId) || CollectionUtils.isEmpty(fileInfos)) {
      return htmlContent;
    }

    List<DocumentAttachmentDTO> attachmentDTOS = documentAttachmentService.save(fileInfos, documentId, userId);
    if (CollectionUtils.isEmpty(attachmentDTOS)) {
      return htmlContent;
    }

    // 构建URL替换映射：file://fileId -> url
    Map<String, String> urlReplacementMap = attachmentDTOS.stream()
      .collect(Collectors.toMap(
        dto -> "file://" + dto.getFileId(),
        DocumentAttachmentDTO::getUrl,
        (existing, replacement) -> existing // 如果key重复，保留第一个
      ));
    htmlContent = imageTransformer.replaceImageUrlsInMarkdown(htmlContent, urlReplacementMap);

    return htmlContent;
  }
}
