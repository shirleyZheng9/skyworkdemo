package com.iwhalecloud.bote.doc.module.person.controller;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.person.dto.MyDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.MyDocumentQueryParams;
import com.iwhalecloud.bote.doc.module.person.service.IMyDocumentService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 我的文档管理
 *
 * @author yangran
 * @since 2025-08-18
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/myDocuments", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心：我的文档")
public class MyDocumentController {
  private final IMyDocumentService myDocumentService;

  @GetMapping
  @Operation(summary = "获取我的文档")
  public ResultVO<MyDocumentDTO> getMyDocuments(
    @RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(name = "spaceId") Long spaceId,
    @Parameter(description = "排序方式：name-按名称，updateTime-按更新时间，默认updateTime") @RequestParam(value = "sortBy", defaultValue = "updateTime")
    String sortBy,
    @Parameter(description = "排序顺序：desc-倒序，asc-正序，默认desc") @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder,
    @Parameter(description = "搜索关键词，支持文档名称模糊搜索") @RequestParam(value = "keyword", required = false) String keyword) {
    MyDocumentQueryParams queryParams = new MyDocumentQueryParams();
    queryParams.setSortBy(sortBy);
    queryParams.setSortOrder(sortOrder);
    queryParams.setKeyword(keyword);
    queryParams.setTenantId(tenantId);
    queryParams.setSpaceId(spaceId);
    return ResultVO.success(myDocumentService.queryMyDocumentTree(queryParams));
  }

}
