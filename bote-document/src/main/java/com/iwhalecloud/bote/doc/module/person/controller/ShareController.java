package com.iwhalecloud.bote.doc.module.person.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.person.dto.share.MyShareDTO;
import com.iwhalecloud.bote.doc.module.person.dto.share.SharedWithMeDTO;
import com.iwhalecloud.bote.doc.module.person.service.IShareService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 共享模块接口
 * <p>提供“与我共享”和“我共享的”两个查询接口。</p>
 *
 * @author lizuyin
 * @since 2025-08-20
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/share", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心：共享管理")
public class ShareController {
  private final IShareService shareService;

  /**
   * 获取与我共享的文档列表。
   *
   * @param pageNum 页码，默认1
   * @param pageSize 每页大小，默认20
   * @param fileType 文件类型过滤：WORD、EXCEL、PDF、PPT、TXT、IMAGE
   * @param sortBy 排序方式：shareTime、modifyTime，默认shareTime
   * @param sortOrder 排序顺序：desc、asc，默认desc
   * @return 分页数据
   */
  @GetMapping("sharedWithMe")
  @Operation(summary = "获取与我共享")
  public ResultVO<PageInfo<SharedWithMeDTO>> sharedWithMe(
    @Parameter(description = "页码，默认1") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
    @Parameter(description = "每页大小，默认20") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
    @Parameter(description = "文件类型过滤") @RequestParam(value = "fileType", required = false) String fileType,
    @Parameter(description = "排序方式") @RequestParam(value = "sortBy", defaultValue = "shareTime") String sortBy,
    @Parameter(description = "排序顺序") @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder,
    @RequestParam(name = "platform", required = false) String platform,
    @RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(name = "spaceId") Long spaceId) {

    Assert.isTrue(pageNum > 0, "页码必须大于0");
    Assert.isTrue(pageSize > 0, "每页大小必须大于0");

    return ResultVO.success(shareService.getSharedWithMe(pageNum, pageSize, fileType, sortBy, sortOrder, tenantId, spaceId, platform));
  }

  /**
   * 获取我共享的文档列表。
   * <p>permissions 字段为我对该文档的所有分享记录的“最高权限”。</p>
   *
   * @param pageNum 页码，默认1
   * @param pageSize 每页大小，默认20
   * @param fileType 文件类型过滤：WORD、EXCEL、PDF、PPT、TXT、IMAGE
   * @param sortBy 排序方式：shareTime、modifyTime，默认shareTime
   * @param sortOrder 排序顺序：desc、asc，默认desc
   * @return 分页数据
   */
  @GetMapping("myShares")
  @Operation(summary = "获取我共享的")
  public ResultVO<PageInfo<MyShareDTO>> myShares(
    @Parameter(description = "页码，默认1") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
    @Parameter(description = "每页大小，默认20") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
    @Parameter(description = "文件类型过滤") @RequestParam(value = "fileType", required = false) String fileType,
    @Parameter(description = "排序方式") @RequestParam(value = "sortBy", defaultValue = "shareTime") String sortBy,
    @Parameter(description = "排序顺序") @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder,
    @RequestParam(name = "platform", required = false) String platform,
    @Parameter(description = "租户ID") @RequestParam("tenantId") Long tenantId, @RequestParam(name = "spaceId") Long spaceId) {

    Assert.isTrue(pageNum > 0, "页码必须大于0");
    Assert.isTrue(pageSize > 0, "每页大小必须大于0");

    return ResultVO.success(shareService.getMyShares(pageNum, pageSize, fileType, sortBy, sortOrder, tenantId, spaceId, platform));
  }
}


