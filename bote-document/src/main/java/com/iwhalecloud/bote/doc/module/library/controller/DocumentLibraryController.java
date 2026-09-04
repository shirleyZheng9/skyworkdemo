package com.iwhalecloud.bote.doc.module.library.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.doc.cache.DocumentLibraryCache;
import com.iwhalecloud.bote.doc.cache.PinnedDocumentCache;
import com.iwhalecloud.bote.doc.cache.PinnedLibraryCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.exception.DocumentLibraryAccessDenyException;
import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.consts.TargetTypeEnum;
import com.iwhalecloud.bote.doc.consts.VisibilityScopeEnum;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryDetailDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryListDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentPermissionService;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.dto.ExitShareRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.ExitShareResponseDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryBatchPermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryPermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibrarySettingsRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibrarySettingsResponseDTO;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryPermissionService;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.doc.module.person.service.IHomepageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/library", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心：文档库")
public class DocumentLibraryController {

  private final DocumentLibraryService documentLibraryService;
  private final DocumentLibraryPermissionService documentLibraryPermissionService;
  private final DocumentLibraryCache documentLibraryCache;
  private final IDocumentPermissionService documentPermissionService;
  private final IDcUserService dcUserService;
  private final IHomepageService homepageService;
  private final PinnedLibraryCache pinnedLibraryCache;
  private final PinnedDocumentCache pinnedDocumentCache;

  @PostMapping("/create")
  @Operation(summary = "创建文档库")
  public ResultVO<DocumentLibraryDTO> create(@RequestBody @Valid DocumentLibraryDTO request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (request.getTenantId() == null) {
      request.setTenantId(TenantContextHolder.getTenantId());
    }
    if (request.getSpaceId() == null) {
      request.setSpaceId(TenantIdUtil.getSpaceId(request.getTenantId()));
    }
    DocumentLibraryDTO dto = documentLibraryService.createLibrary(request, userId);
    return ResultVO.success(dto);
  }

  @PostMapping("/delete/{libraryId}")
  @Operation(summary = "删除文档库")
  public ResultVO<Void> delete(@Parameter(description = "文档库ID") @PathVariable("libraryId") String libraryId,
                               @RequestBody TenantBaseRO baseRO) {
    Assert.hasText(libraryId, "文档库ID不能为空");
    TenantContextHolder.setTenantId(baseRO.getTenantId());
    Long userId = SessionUtil.getLoginInfo().getUserId();
    DocumentLibraryDTO libraryDTO = documentLibraryService.findByLibraryIdAndSpaceId(libraryId, baseRO.getSpaceId(), baseRO.getTenantId());
    if (libraryDTO == null) {
      return ResultVO.fail("404", "文档库不存在", null, null);
    }
    boolean libraryManager = documentLibraryPermissionService.isLibraryManager(libraryId, userId);
    if (!libraryManager) {
      return ResultVO.fail("403", "无权限删除文档库", null, null);
    }
    ResultVO<Void> resultVO = documentLibraryService.deleteLibrary(libraryId, userId);
    // 清除缓存
    documentLibraryCache.deleteLibraryCache(libraryId);
    homepageService.unpinResourceByTenant(baseRO.getTenantId(), libraryId, TargetTypeEnum.LIBRARY.getCode(), baseRO.getSpaceId());
    pinnedLibraryCache.clearTenantCache(baseRO.getTenantId(), baseRO.getSpaceId());
    pinnedDocumentCache.clearTenantCache(baseRO.getTenantId(), baseRO.getSpaceId());
    Long spaceTenantId = TenantIdUtil.getSpaceTenantId(baseRO.getSpaceId());
    if (spaceTenantId != null) {
      pinnedLibraryCache.clearTenantCache(spaceTenantId, baseRO.getSpaceId());
      pinnedDocumentCache.clearTenantCache(spaceTenantId, baseRO.getSpaceId());
    }
    return resultVO;
  }

  @GetMapping("/page")
  @Operation(summary = "获取文档库分页")
  public ResultVO<PageInfo<LibraryListDTO>> page(
    @Parameter(description = "页码，默认1") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
    @Parameter(description = "每页大小，默认20") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
    @Parameter(description = "排序字段：created_time、updated_time；默认created_time")
    @RequestParam(value = "sortBy", defaultValue = DocBaseConsts.SORT_FIELD_CREATED_TIME) String sortBy,
    @Parameter(description = "排序顺序：desc、asc；默认desc") @RequestParam(value = "sortOrder", defaultValue = DocBaseConsts.SORT_ORDER_DESC)
    String sortOrder,
    @Parameter(description = "关键词（仅对library_name模糊匹配）") @RequestParam(value = "keyword", required = false) String keyword,
    @Parameter(description = "空间ID", required = false) @RequestParam Long spaceId,
    @Parameter(description = "查询模式", required = false) @RequestParam(required = false) String queryMode,
    @Parameter(description = "环境租户ID") @RequestParam(required = false) Long envTenantId,
    @Parameter(description = "租户ID") @RequestParam Long tenantId) {
    Assert.isTrue(pageNum > 0, "页码必须大于0");
    Assert.isTrue(pageSize > 0, "每页大小必须大于0");
    if (envTenantId == null) {
      envTenantId = tenantId;
    }
    if (DocBaseConsts.AI_PORTAL_QUERY_MODE.equalsIgnoreCase(queryMode)) {
      tenantId = null;
    }
    return ResultVO.success(documentLibraryService.getVisibleLibraryPage(pageNum, pageSize, sortBy, sortOrder, keyword, spaceId, tenantId, envTenantId));
  }

  @GetMapping("/detail/{libraryId}")
  @Operation(summary = "获取文档库详情")
  @IgnoreTenant
  public ResultVO<LibraryDetailDTO> detail(@Parameter(description = "文档库ID") @PathVariable("libraryId") String libraryId,
                                         @Parameter(description = "空间ID") @RequestParam Long spaceId, @Parameter(description = "租户ID") @RequestParam Long tenantId) {
    Assert.hasText(libraryId, "文档库ID不能为空");
    DocumentLibraryDTO documentLibraryDTO = documentLibraryService.findByLibraryIdAndSpaceId(libraryId, spaceId, null);
    if (documentLibraryDTO == null) {
      return ResultVO.fail("文档库不存在");
    }
    // 由于调用此接口可能未传递租户ID， 此处进行校验
    TenantContextHolder.setIgnore(false);
    TenantContextHolder.setTenantId(documentLibraryDTO.getTenantId());

    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    // 检查访问权限
    LibraryRoleEnum userLibraryRole = documentLibraryPermissionService.queryUserLibraryMaxRole(libraryId, currentLoginUserId);
    if (Objects.equals(userLibraryRole, LibraryRoleEnum.ANONYMOUS)) {
      // 如果无权限，判断是否有某个文档权限，有文档权限，只返回基本信息
      LibraryDetailDTO detailDTO = getLibraryDetailWithAnnoPermission(libraryId, currentLoginUserId, documentLibraryDTO);
      return ResultVO.success(detailDTO);
    }
    if ("T".equals(documentLibraryDTO.getIsBuiltin())) {
      tenantId = null;
    }
    LibraryDetailDTO detail = documentLibraryService.getLibraryDetail(libraryId, spaceId, tenantId);
    detail.setPermissions(userLibraryRole.getCode());
    return ResultVO.success(detail);
  }

  private LibraryDetailDTO getLibraryDetailWithAnnoPermission(String libraryId, Long currentLoginUserId, DocumentLibraryDTO documentLibraryDTO) {
    boolean hasNodePermission = documentPermissionService.existUserPermissionByLibraryId(libraryId, currentLoginUserId);
    if (!hasNodePermission) {
      throw new DocumentLibraryAccessDenyException();
    }
    LibraryDetailDTO detailDTO = new LibraryDetailDTO();
    detailDTO.setLibraryId(libraryId);
    detailDTO.setLibraryName(documentLibraryDTO.getLibraryName());
    detailDTO.setDescription(documentLibraryDTO.getDescription());
    detailDTO.setLibraryIcon(documentLibraryDTO.getLibraryIcon());
    detailDTO.setColor(documentLibraryDTO.getColor());
    detailDTO.setPermissions(LibraryRoleEnum.ANONYMOUS.getCode());
    detailDTO.setTenantId(documentLibraryDTO.getTenantId());
    detailDTO.setSpaceId(documentLibraryDTO.getSpaceId());

    if (Objects.equals(VisibilityScopeEnum.PRIVATE.getCode(), documentLibraryDTO.getVisibilityScope())) {
      // 如果是个人文档库， 文档库名称需要处理成 xxx文档库
      String ownerName = dcUserService.findUserNameById(documentLibraryDTO.getOwnerId());
      detailDTO.setLibraryName(String.format("%s的文档", ownerName));
    }
    return detailDTO;
  }

  @PostMapping("/settings/{libraryId}")
  @Operation(summary = "文档库设置")
  public ResultVO<LibrarySettingsResponseDTO> updateSettings(@Parameter(description = "文档库ID") @PathVariable("libraryId") String libraryId,
                                                             @RequestBody @Valid LibrarySettingsRequestDTO request) {
    Assert.hasText(libraryId, "文档库ID不能为空");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    LibrarySettingsResponseDTO response = documentLibraryService.updateLibrarySettings(libraryId, request, userId);
    documentLibraryCache.deleteLibraryCache(libraryId);
    pinnedLibraryCache.clearTenantCache(request.getTenantId(), request.getSpaceId());
    Long spaceTenantId = TenantIdUtil.getSpaceTenantId(request.getSpaceId());
    if (spaceTenantId != null) {
      pinnedLibraryCache.clearTenantCache(spaceTenantId, request.getSpaceId());
    }
    return ResultVO.success(response);
  }

  @PostMapping("/permissions/{libraryId}")
  @Operation(summary = "文档库权限设置")
  public ResultVO<Void> updatePermissions(@Parameter(description = "文档库ID") @PathVariable("libraryId") String libraryId,
                                          @RequestBody @Valid LibraryPermissionRequestDTO request) {
    Assert.hasText(libraryId, "文档库ID不能为空");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    return documentLibraryService.updateLibraryPermissions(libraryId, request, userId);
  }

  @PostMapping("/permissions/batch/{libraryId}")
  @Operation(summary = "文档库批量权限设置")
  public ResultVO<Void> batchPermissions(@Parameter(description = "文档库ID") @PathVariable("libraryId") String libraryId,
                                         @RequestBody @Valid LibraryBatchPermissionRequestDTO request) {
    Assert.hasText(libraryId, "文档库ID不能为空");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    return documentLibraryService.batchLibraryPermissions(libraryId, request, userId);
  }

  @PostMapping("/exitShare")
  @Operation(summary = "退出共享文档库")
  public ResultVO<ExitShareResponseDTO> exitShare(@RequestBody @Valid ExitShareRequestDTO request) {
    Assert.notNull(request, "退出共享请求不能为空");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    return documentLibraryService.exitShare(request, userId);
  }

  @GetMapping("/queryLibraryByDocumentId")
  @Operation(summary = "通过文档id查询文档库信息")
  public ResultVO<DocumentLibraryDTO> queryLibraryByDocumentId(@RequestParam("documentId") String documentId) {
    Assert.hasText(documentId, "文档ID不能为空");
    return ResultVO.success(documentLibraryService.queryLibraryByDocumentId(documentId));
  }
}


