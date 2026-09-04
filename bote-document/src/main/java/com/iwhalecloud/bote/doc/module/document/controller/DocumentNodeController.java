package com.iwhalecloud.bote.doc.module.document.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.doc.cache.DcDocumentNodeCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.constant.DocErrorCodeConsts;
import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRoleManager;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.control.service.DocumentNodeService;
import com.iwhalecloud.bote.doc.module.control.vo.DocFolderSubsVo;
import com.iwhalecloud.bote.doc.module.control.vo.NodeInfoTreeVo;
import com.iwhalecloud.bote.doc.module.control.vo.NodeInfoVo;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDetailDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentCreateRequestDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentNodeMoveLibraryOpRo;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentNodeMoveOpRo;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentNodeUpdateRO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentReleasedDTO;
import com.iwhalecloud.bote.doc.module.document.dto.NodeMoveLibraryDocumentDataOpRo;
import com.iwhalecloud.bote.doc.module.document.dto.NodePathDTO;
import com.iwhalecloud.bote.doc.module.document.dto.OnlineDocumentTypeInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WebCrawlerRequestDTO;
import com.iwhalecloud.bote.doc.module.document.dto.request.DocFolderSubsQueryParams;
import com.iwhalecloud.bote.doc.module.document.dto.request.QueryDocContributorsRequest;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocumentEntity;
import com.iwhalecloud.bote.doc.module.document.service.DocumentChangEventPublisher;
import com.iwhalecloud.bote.doc.module.document.service.DocumentPathEventPublisher;
import com.iwhalecloud.bote.doc.module.document.service.DocumentPreviewService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentContributorService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentPermissionService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContributeRequestDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContributorDTO;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryPermissionService;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryService;
import com.iwhalecloud.bote.doc.module.library.service.IDocumentLibraryInitService;
import com.iwhalecloud.bote.dto.base.FilePreviewDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Aiqing
 * @since 2025/8/18
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/document/node", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心：文档库文档")
public class DocumentNodeController {

  private final DocumentNodeService documentNodeService;
  private final IDocumentService documentService;
  private final ControlTemplate controlTemplate;
  private final DocumentLibraryService documentLibraryService;
  private final IDocumentLibraryInitService documentLibraryInitService;
  private final DocumentPreviewService documentPreviewService;
  private final DocumentPathEventPublisher documentPathEventPublisher;
  private final DocumentChangEventPublisher documentChangEventPublisher;
  private final DocumentLibraryPermissionService documentLibraryPermissionService;
  private final IDocumentPermissionService documentPermissionService;
  private final DcDocumentNodeCache dcDocumentNodeCache;
  private final IDocumentContributorService documentContributorService;

  @Value("${bote.dc.document.online-types.disabled:}")
  private List<String> disableOnlineTypes;

  @Operation(summary = "查询文档详情")
  @GetMapping("/detail/{documentId}")
  @IgnoreTenant
  public ResultVO<DcDocumentDetailDTO> detail(@PathVariable String documentId) {
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    DcDocumentDetailDTO documentDTO = documentService.findDetailByDocumentId(documentId, currentLoginUserId);
    if (documentDTO == null) {
      return ResultVO.fail(DocErrorCodeConsts.NODE_NOT_EXIST, "文档不存在", null, null);
    }
    documentChangEventPublisher.publishViewEvent(documentDTO.getLibraryId(), documentId, currentLoginUserId);
    return ResultVO.success(documentDTO);
  }

  @Operation(summary = "创建文档库文档")
  @PostMapping("/create")
  public ResultVO<NodeInfoVo> create(@RequestBody @Valid DocumentCreateRequestDTO documentCreateRequest) {
    Long userId = SessionUtil.getLoginInfo().getUserId();

    String parentId = documentCreateRequest.getParentId();
    String libraryId = documentCreateRequest.getLibraryId();
    if (StringUtils.isNotBlank(parentId)) {
      String parentLibraryId = documentService.getLibraryIdByDocument(parentId);
      Assert.isTrue(StringUtils.isNotBlank(parentLibraryId), "文档父节点不存在");
      Assert.isTrue(Objects.equals(libraryId, parentLibraryId), "非法参数, 父ID的所属文档库不一致");
    }
    ControlRole controlRole;
    if (StringUtils.isBlank(libraryId)) {
      if (!DocBaseConsts.AI_PORTAL.equals(documentCreateRequest.getPlatform())) {
        throw new BssException("文档库ID为空");
      }
      // 查询默认的个人文档库
      libraryId = documentLibraryService.findUserPrivateMyLibraryById(userId, documentCreateRequest.getSpaceId(), documentCreateRequest.getTenantId());
      Assert.isTrue(StringUtils.isNotBlank(libraryId), "文档库数据初始化异常");
      documentCreateRequest.setLibraryId(libraryId);
      controlRole = ControlRoleManager.parseNodeRole(DocRoleEnum.DOC_OWNER.getCode());
    }
    else {
      DocumentLibraryDTO documentLibrary = documentLibraryService.findByLibraryId(libraryId);
      if (documentLibrary == null) {
        throw new BssException("文档库不存在");
      }
      // 检查文档库的编辑权限
      controlRole = documentNodeService.checkParentNodeEditPermission(documentLibrary, userId, parentId, null);
    }
    String documentId = documentService.createOnlineDocumentNode(userId, documentCreateRequest);
    NodeInfoVo nodeInfo = documentNodeService.getNodeInfoByNodeId(documentId, controlRole);

    // 推送文档创建动态， 记录文档库动态
    documentChangEventPublisher.publishCreateEvent(libraryId, documentId, nodeInfo.getNodeName(), userId);
    return ResultVO.success(nodeInfo);
  }

  @Operation(summary = "爬取网页并创建在线文档")
  @PostMapping("/crawl")
  public ResultVO<NodeInfoVo> crawlWebPage(@RequestBody @Valid WebCrawlerRequestDTO request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long tenantId = request.getTenantId() != null ? request.getTenantId() : TenantContextHolder.getTenantId();
    Long spaceId = request.getSpaceId() != null ? request.getSpaceId() : TenantIdUtil.getSpaceId(tenantId);

    // 验证文档库权限
    String libraryId = request.getLibraryId();
    DocumentLibraryDTO documentLibrary = documentLibraryService.findByLibraryId(libraryId);
    if (documentLibrary == null) {
      throw new BssException("文档库不存在");
    }

    String parentId = request.getParentId();
    ControlRole controlRole = documentNodeService.checkParentNodeEditPermission(documentLibrary, userId, parentId, null);

    // 爬取网页并创建在线文档
    String documentId = documentService.crawlWebPageAndCreateDocument(
      request.getUrl(),
      libraryId,
      parentId,
      request.getDocumentName(),
      userId,
      tenantId,
      spaceId
    );

    NodeInfoVo nodeInfo = documentNodeService.getNodeInfoByNodeId(documentId, controlRole);

    // 推送文档创建动态，记录文档库动态
    documentChangEventPublisher.publishCreateEvent(libraryId, documentId, nodeInfo.getNodeName(), userId);
    return ResultVO.success(nodeInfo);
  }

  @Operation(summary = "更新文档节点信息")
  @PostMapping("/{documentId}/update")
  public ResultVO<Void> updateDocument(@RequestBody @Valid DocumentNodeUpdateRO updateRO, @PathVariable String documentId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      return ResultVO.fail("文档不存在");
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    controlTemplate.checkNodePermission(documentDTO.getLibraryId(), userId, documentId,
      NodePermission.RENAME_NODE, DocumentPermConsts.OPERATE_DENIED_CALLBACK);

    documentService.updateDocumentBasicInfo(documentDTO, userId, updateRO);
    if (!Objects.equals(documentDTO.getDocumentName(), updateRO.getDocumentName())) {
      // 异步事件
      documentPathEventPublisher.publishDocumentRenamedEvent(documentId, documentDTO.getLibraryId(), userId, documentDTO.getDocumentName(), updateRO.getSpaceId());
    }
    return ResultVO.success();
  }

  /**
   * 查询文档库文档树
   */
  @GetMapping("/tree")
  @Operation(summary = "查询文档库的文档节点树")
  @Parameters({
    @Parameter(name = "libraryId", description = "文档库ID", required = true,
      schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "lbryQkKp9XJEl"),
    @Parameter(name = "depth", in = ParameterIn.QUERY,
      description = "查询的树层级, 可以最大查询2级",
      schema = @Schema(type = "integer"), example = "2")
  })
  public ResultVO<NodeInfoTreeVo> getTree(
    @RequestParam(name = "libraryId") String libraryId,
    @RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(name = "envTenantId", required = false) Long envTenantId,
    @RequestParam(name = "spaceId") Long spaceId,
    @RequestParam(name = "depth", defaultValue = "10") @Valid @Min(0) @Max(10) Integer depth) {
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();

    DcDocumentEntity dcDocumentEntity = documentNodeService.getRootNodeIdDcDocumentEntityByLibraryId(libraryId, tenantId, spaceId);
    if (dcDocumentEntity == null) {
      return ResultVO.fail("查询失败，文档库初始化异常");
    }
    NodeInfoTreeVo tree = documentNodeService.getNodeTree(libraryId, dcDocumentEntity.getDocumentId(), currentLoginUserId, depth, tenantId, envTenantId, spaceId);
    return ResultVO.success(tree);
  }

  @PostMapping("/getFolderSubs")
  @Operation(summary = "查询文件夹子元素")
  public ResultVO<DocFolderSubsVo> getFolderSubs(@Valid @RequestBody DocFolderSubsQueryParams params) {
    if (params.getTenantId() == null) {
      return ResultVO.fail("租户ID不能为空");
    }
    DcDocumentDTO documentDTO = documentService.findByDocumentId(params.getDocumentId());
    if (documentDTO == null) {
      return ResultVO.fail("文件夹不存在");
    }
    if (!DocumentTypeEnum.FOLDER.getCode().equals(documentDTO.getDocumentType())) {
      return ResultVO.fail("不是文件夹");
    }
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    // 检查节点权限
    controlTemplate.checkNodePermission(documentDTO.getLibraryId(), currentLoginUserId, params.getDocumentId(),
      NodePermission.READ_NODE, DocumentPermConsts.ACCESS_DENIED_CALLBACK);
    return ResultVO.success(documentNodeService.getFolderSubs(params, documentDTO.getLibraryId()));
  }

  /**
   * 查询个人文档库的文档树
   */
  @GetMapping("/myTree")
  @Operation(summary = "查询文档库的文档节点树")
  @Parameters({
    @Parameter(name = "depth", in = ParameterIn.QUERY,
      description = "查询的树层级, 可以最大查询2级",
      schema = @Schema(type = "integer"), example = "2")
  })
  public ResultVO<NodeInfoTreeVo> getUserLibraryTree(
    @RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(name = "spaceId") Long spaceId,
    @RequestParam(name = "depth", defaultValue = "10") @Valid @Min(0) @Max(10) Integer depth) {
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    String userPrivateLibraryId = documentLibraryInitService.initializeUserDocumentLibrary(currentLoginUserId, tenantId, spaceId);
    //    String userPrivateLibraryId = documentLibraryService.findUserPrivateLibraryId(currentLoginUserId, spaceId);

    String rootNodeId = documentNodeService.getRootNodeIdByLibraryId(userPrivateLibraryId, tenantId, spaceId);
    if (StringUtils.isBlank(rootNodeId)) {
      return ResultVO.fail("查询失败，文档库初始化异常");
    }
    NodeInfoTreeVo tree = documentNodeService.getNodeTree(userPrivateLibraryId, rootNodeId, currentLoginUserId, depth, tenantId, tenantId, spaceId);
    return ResultVO.success(tree);
  }

  @GetMapping("/parents")
  @Operation(summary = "查询文档节点的上级节点路径")
  @Parameter(name = "documentId", description = "文档节点ID", required = true,
    schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "docRTGSy43DJ9")
  public ResultVO<List<NodePathDTO>> getParentNodes(
    @RequestParam(name = "documentId") String documentId) {
    String libraryId = documentService.getLibraryIdByDocument(documentId);
    if (StringUtils.isBlank(libraryId)) {
      return ResultVO.fail("文档不存在");
    }
    List<NodePathDTO> nodePaths = documentNodeService.getParentPathByNodeId(libraryId, documentId);
    return ResultVO.success(nodePaths);
  }

  /**
   * 查询下级节点
   */
  @GetMapping("/children")
  @Operation(summary = "获取文档节点的下级节点")
  @Parameters({
    @Parameter(name = "documentId", description = "node id", required = true,
      schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "nodRTGSy43DJ9")
  })
  public ResultVO<List<NodeInfoVo>> getNodeChildrenList(@RequestParam(name = "documentId") String documentId) {
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();

    String libraryId = documentService.getLibraryIdByDocument(documentId);
    if (StringUtils.isBlank(libraryId)) {
      return ResultVO.fail("文档不存在");
    }
    DocumentLibraryDTO documentLibraryDTO = documentLibraryService.findByLibraryId(libraryId);
    if (documentLibraryDTO == null) {
      return ResultVO.fail("文档库不存在");
    }
    List<NodeInfoVo> nodeInfos = documentNodeService.getChildNodesByNodeId(libraryId, currentLoginUserId, documentId, null);
    return ResultVO.success(nodeInfos);
  }

  /**
   * Position node.
   */
  @GetMapping("/position/{documentId}")
  @Operation(summary = "定位文档节点")
  @Parameter(name = "documentId", description = "文档节点ID", required = true,
    schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "nodRTGSy43DJ9")
  public ResultVO<NodeInfoTreeVo> position(@PathVariable("documentId") String documentId, @RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(name = "spaceId") Long spaceId, @RequestParam(name = "envTenantId", required = false) Long envTenantId) {
    String libraryId = documentService.getLibraryIdByDocument(documentId);
    if (StringUtils.isBlank(libraryId)) {
      return ResultVO.fail("文档不存在");
    }
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    // 检查节点权限
    controlTemplate.checkNodePermission(libraryId, currentLoginUserId, documentId,
      NodePermission.READ_NODE, DocumentPermConsts.ACCESS_DENIED_CALLBACK);
    NodeInfoTreeVo treeVo = documentNodeService.position(libraryId, currentLoginUserId, documentId, tenantId, envTenantId, spaceId);
    return ResultVO.success(treeVo);
  }

  @PostMapping("/delete/{documentId}")
  @Operation(summary = "删除文档节点")
  @Parameters({
    @Parameter(name = "documentId", description = "文档ID", required = true,
      schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "nodRTGSy43DJ9")
  })
  public ResultVO<Void> delete(@PathVariable("documentId") String documentId,
    @RequestBody @Valid TenantBaseRO tenantBaseRO) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      // 不再提示错误
      return ResultVO.success();
    }
    TenantContextHolder.setTenantId(tenantBaseRO.getTenantId());
    if (tenantBaseRO.getSpaceId() == null) {
      tenantBaseRO.setSpaceId(TenantIdUtil.getSpaceId(tenantBaseRO.getTenantId()));
      SpaceContextHolder.setSpaceId(tenantBaseRO.getSpaceId());
    }
    String libraryId = documentDTO.getLibraryId();
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();

    documentNodeService.deleteDocumentNode(libraryId, currentLoginUserId, documentId);

    // 文档动态消息
    documentChangEventPublisher.publishDeleteEvent(libraryId, documentId,
      documentDTO.getDocumentName(), currentLoginUserId, tenantBaseRO.getSpaceId());

    documentPathEventPublisher.publishDocumentDeletedEvent(documentId, libraryId, currentLoginUserId, tenantBaseRO.getSpaceId());
    return ResultVO.success();
  }

  /**
   * 拖动节点位置
   * 1、首先需要校验是否具有原文件的删除权限和新文档库的新增权限
   * 2、原文件需要处理原文档库对应的数据的上下级链表关系
   * 3、添加到新文档库的时候需要判断是放到某个文件夹下还是同级下，都放到最前面
   */
  @PostMapping("/moveLibrary")
  @Operation(summary = "移动文档节点", description = "文档ID和目标父ID必填，前置ID当移动到同级最前列时为空")
  public ResultVO<Void> moveLibrary(@RequestBody @Valid DocumentNodeMoveLibraryOpRo nodeOpRo) {
    NodeMoveLibraryDocumentDataOpRo nodeMoveLibraryDocumentDataOpRo = documentNodeService.moveLibrary(nodeOpRo);
    if (nodeMoveLibraryDocumentDataOpRo == null) {
      return ResultVO.success();
    }
    dcDocumentNodeCache.delete(nodeOpRo.getDocumentId());
    documentPathEventPublisher.publishDocumentMovedEvent(nodeOpRo.getDocumentId(), nodeOpRo.getLibraryId(), SessionUtil.getLoginInfo().getUserId(), nodeOpRo.getSpaceId());
    publishDocumentMovedEvent(nodeMoveLibraryDocumentDataOpRo);
    return ResultVO.success();
  }

  /**
   * 发布文档移动事件
   * @param nodeData 移动数据
   */
  private void publishDocumentMovedEvent(NodeMoveLibraryDocumentDataOpRo nodeData) {
    if (CollectionUtils.isEmpty(nodeData.getSubIds())) {
      nodeData.setSubIds(Collections.singletonList(nodeData.getDocumentId()));
    }
    else {
      if (!nodeData.getSubIds().contains(nodeData.getDocumentId())) {
        nodeData.getSubIds().addFirst(nodeData.getDocumentId());
      }
    }
    nodeData.getSubIds().forEach(nodeId -> {
      documentChangEventPublisher.publishDocumentMovedInEvent(nodeData.getTargetLibraryId(), nodeId, null,
        SessionUtil.getLoginInfo().getUserId(), nodeData.getLibraryId());
      documentChangEventPublisher.publishDocumentMovedOutEvent(nodeData.getLibraryId(), nodeId, null,
        SessionUtil.getLoginInfo().getUserId(), nodeData.getTargetLibraryId());

    });
  }


  /**
   * 拖动节点位置
   */
  @PostMapping("/move")
  @Operation(summary = "移动文档节点", description = "文档ID和父ID必填，前置ID当移动到同级最前列时为空")
  public ResultVO<List<NodeInfoVo>> move(@RequestBody @Valid DocumentNodeMoveOpRo nodeOpRo) {
    String libraryId = nodeOpRo.getLibraryId();
    String documentId = nodeOpRo.getDocumentId();

    DcDocumentDTO documentDTO = documentService.checkDocumentIfExist(libraryId, documentId);

    String moveTargetParentId = nodeOpRo.getParentId();
    DcDocumentDTO parentNode;
    if (StringUtils.isNotBlank(moveTargetParentId)) {
      parentNode = documentService.checkDocumentIfExist(libraryId, moveTargetParentId);
      // 校验一下是否有同名文件或者文件夹
      if (documentService.existsDocumentByName(libraryId, moveTargetParentId, documentId, documentDTO.getDocumentName(), documentDTO.getDocumentType(), documentDTO.getContentSource())) {
        throw new BssException("该文档库的同级文件夹下存在相同名字的文档或者文件夹！");
      }
    }
    else {
      // parentId 为空时，默认根路径
      moveTargetParentId = documentNodeService.getRootNodeIdByLibraryId(libraryId, nodeOpRo.getTenantId(), nodeOpRo.getSpaceId());
      parentNode = documentService.findByDocumentId(moveTargetParentId);
      nodeOpRo.setParentId(moveTargetParentId);
    }

    if (StringUtils.isNotBlank(nodeOpRo.getPreNodeId())) {
      DcDocumentDTO preDocumentNode = documentService.checkDocumentIfExist(libraryId, nodeOpRo.getPreNodeId());
      if (!Objects.equals(preDocumentNode.getParentId(), parentNode.getDocumentId())) {
        throw new BssException("400", "父ID与前置ID参数非法");
      }
    }
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();

    // 检查移动权限
    controlTemplate.checkNodePermission(libraryId, currentLoginUserId, documentId,
      NodePermission.MOVE_NODE, DocumentPermConsts.OPERATE_DENIED_CALLBACK);

    String parentId = documentDTO.getParentId();
    if (parentId.equals(moveTargetParentId)) {
      // 同级移动
      controlTemplate.checkNodePermission(libraryId, currentLoginUserId, moveTargetParentId,
        NodePermission.MOVE_NODE,
        DocumentPermConsts.OPERATE_DENIED_CALLBACK);
    }
    else {
      // 移动到其他文件夹，检查目标文件节点的权限
      controlTemplate.checkNodePermission(libraryId, currentLoginUserId, moveTargetParentId,
        NodePermission.CREATE_NODE,
        DocumentPermConsts.OPERATE_DENIED_CALLBACK);
    }
    List<String> nodeIds = documentNodeService.move(currentLoginUserId, nodeOpRo);

    documentPathEventPublisher.publishDocumentMovedEvent(documentId, libraryId, currentLoginUserId, nodeOpRo.getSpaceId());
    List<NodeInfoVo> nodes = documentNodeService.getNodeInfoByNodeIds(libraryId, currentLoginUserId, nodeIds);
    return ResultVO.success(nodes);
  }


  @Operation(summary = "搜索文档库文档")
  @GetMapping("searchNode")
  public ResultVO<List<NodeBaseInfoDTO>> searchNode(@RequestParam String libraryId, @RequestParam String keyword) {
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    boolean canAccess = documentLibraryPermissionService.checkLibraryAccessPermission(libraryId, currentLoginUserId);
    if (!canAccess) {
      return ResultVO.fail(DocErrorCodeConsts.LIBRARY_ACCESS_DENY, "无权限访问", null, null);
    }
    List<NodeBaseInfoDTO> nodeList = documentNodeService.searchDocument(libraryId, keyword);
    return ResultVO.success(nodeList);
  }

  @IgnoreTenant
  @Operation(summary = "查询文档是否具有待发布图标")
  @GetMapping("queryDocumentReleased")
  public ResultVO<DocumentReleasedDTO> queryDocumentReleased(@RequestParam String documentId) {
    return ResultVO.success(documentNodeService.queryDocumentReleased(documentId));
  }

  @Operation(summary = "初始化文档库根节点")
  @GetMapping("initLibraryRootNode")
  public ResultVO<String> initLibraryRootNode(@RequestParam String libraryId,
    @RequestParam String libraryName,
    @RequestParam Long tenantId,
    @RequestParam Long spaceId) {
    String nodeId = documentService.initLibraryRootDocumentNode(libraryId, libraryName, tenantId, spaceId);
    return ResultVO.success(nodeId);
  }

  @Operation(summary = "查询文件的预览地址")
  @GetMapping("/{documentId}/getPreviewUrl")
  public ResultVO<FilePreviewDTO> getPreviewUrl(@PathVariable String documentId, @RequestParam(value = "id", required = false) Long id) {
    return documentPreviewService.getFilePreviewInfo(documentId, id);
  }

  @Operation(summary = "查询文件转换状态")
  @GetMapping("/getFileConvertStatus")
  public ResultVO<String> getFileConvertStatus(@RequestParam String url) {
    String status = documentPreviewService.getFileConvertStatus(url);
    return ResultVO.success(status);
  }

  @Operation(summary = "恢复继承权限模式")
  @PostMapping("/{documentId}/restoreInheritPermission")
  public ResultVO<Void> restoreInheritPermission(@PathVariable String documentId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      return ResultVO.fail("文档不存在");
    }

    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    String libraryId = documentDTO.getLibraryId();

    // 检查文档管理权限
    controlTemplate.checkNodePermission(libraryId, currentLoginUserId, documentId,
      NodePermission.MANAGE_NODE, DocumentPermConsts.OPERATE_DENIED_CALLBACK);

    // 恢复继承权限模式
    documentPermissionService.restoreInheritPermissionMode(documentId);

    return ResultVO.success();
  }

  @Operation(summary = "查询支持的在线文档类型")
  @GetMapping("querySupportOnlineTypes")
  @IgnoreTenant
  public ResultVO<List<OnlineDocumentTypeInfoDTO>> querySupportOnlineTypes() {
    List<DocumentTypeEnum> onlineTypes = DocumentTypeEnum.ONLINE_TYPES;
    List<OnlineDocumentTypeInfoDTO> infoDTOList = onlineTypes.stream()
      .filter(item -> {
        if (CollectionUtils.isEmpty(disableOnlineTypes)) {
          return true;
        }
        return !disableOnlineTypes.contains(item.getCode());
      }).map(item -> {
        return new OnlineDocumentTypeInfoDTO(item.getDescription(), item.getCode());
      }).toList();
    return ResultVO.success(infoDTOList);
  }

  @Operation(summary = "批量新增文档贡献者")
  @PostMapping("batchAddContributors")
  public ResultVO<Void> batchAddContributors(@RequestBody DocumentContributeRequestDTO request) {
    Assert.hasText(request.getDocumentId(), "文档ID不能为空");
    return documentContributorService.batchAddContributors(request);
  }

  @Operation(summary = "删除文档贡献者")
  @GetMapping("deleteByDocumentId")
  public ResultVO<Void> deleteByDocumentId(@RequestParam("documentId") String documentId,
      @RequestParam("userId") Long userId) {
    Assert.hasText(documentId, "文档ID不能为空");
    Assert.notNull(userId, "用户ID不能为空");
    return documentContributorService.deleteByDocumentId(documentId, userId);
  }

  @Operation(summary = "查询文档贡献者")
  @PostMapping("queryDocumentContributorPage")
  public ResultVO<PageInfo<DocumentContributorDTO>> queryDocumentContributorPage(@RequestBody QueryDocContributorsRequest request) {
    return ResultVO.success(documentContributorService.queryDocumentContributorPage(request));
  }
}
