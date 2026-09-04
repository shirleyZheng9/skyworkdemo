package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcKbPermissionDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseBatchPermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBasePermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcKbPermissionQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.service.IBtDcKbPermissionManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库权限表管理 controller
 *
 * @author linmengfan
 * @since 2025-08-23
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "manager/dckbpermission", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "知识库：权限表管理")
public class BtDcKbPermissionManageController {

  private final IBtDcKbPermissionManageService btDcKbPermissionManageService;


  @PostMapping("/permissions/{knowledgeId}")
  @Operation(summary = "知识库权限设置")
  public ResultVO<Void> updatePermissions(
    @Parameter(description = "文档库ID") @PathVariable("knowledgeId") Long knowledgeId,
    @RequestBody @Valid KnowledgeBasePermissionRequestDTO request) {
    Assert.notNull(knowledgeId, "知识库ID不能为空");
    Assert.notNull(request.getTenantId(), "租户ID不能为空");
    Assert.notNull(request.getData().getPermissionType(), "权限类型不能为空");
    Assert.notNull(request.getData().getSubjectType(), "主体类型不能为空");
    Assert.notNull(request.getData().getSubjectId(), "主体ID不能为空");
    return btDcKbPermissionManageService.updateKbPermissions(knowledgeId, request);
  }

  @PostMapping("/permissions/batch/{knowledgeId}")
  @Operation(summary = "知识库批量权限设置")
  public ResultVO<Void> batchPermissions(
    @Parameter(description = "文档库ID") @PathVariable("knowledgeId") Long knowledgeId,
    @RequestBody @Valid KnowledgeBaseBatchPermissionRequestDTO request) {
    Assert.notNull(knowledgeId, "知识库ID不能为空");
    Assert.notNull(request.getTenantId(), "租户ID不能为空");
    return btDcKbPermissionManageService.batchKbPermissions(knowledgeId, request);
  }


  @Operation(summary = "查询知识库权限表列表")
  @PostMapping("queryBtDcKbPermissionList")
  public ResultVO<List<BtDcKbPermissionDTO>> queryBtDcKbPermissionList(@RequestBody
                                                                       BtDcKbPermissionQueryParams queryParams) {
    return ResultVO.success(btDcKbPermissionManageService.queryBtDcKbPermissionList(queryParams));
  }

  @Operation(summary = "分页查询知识库权限表")
  @PostMapping("queryBtDcKbPermissionPage")
  public ResultVO<PageInfo<BtDcKbPermissionDTO>> queryBtDcKbPermissionPage(@RequestBody BtDcKbPermissionQueryParams queryParams) {
    return ResultVO.success(btDcKbPermissionManageService.queryBtDcKbPermissionPage(queryParams));
  }
}
