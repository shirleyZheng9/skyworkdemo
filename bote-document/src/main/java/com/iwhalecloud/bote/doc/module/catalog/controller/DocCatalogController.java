package com.iwhalecloud.bote.doc.module.catalog.controller;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.catalog.helper.DocCatalogHelper;
import com.iwhalecloud.bote.dto.base.query.CatalogQueryParams;
import com.iwhalecloud.bote.dto.base.query.DeleteCatalogDTO;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 目录管理 controller
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/catalog", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：目录管理")
public class DocCatalogController {
  private final ICatalogManageService catalogManageService;
  private final DocCatalogHelper docCatalogHelper;

  @Operation(summary = "保存目录")
  @PostMapping("saveCatalog")
  public ResultVO<CatalogDTO> saveCatalog(@RequestBody @Valid CatalogDTO catalog) {
    return catalogManageService.saveCatalog(catalog);
  }

  @Operation(summary = "删除目录")
  @PostMapping("deleteCatalog")
  public ResultVO<Void> deleteCatalog(@RequestBody DeleteCatalogDTO params) {
    Assert.notNull(params.getCatalogId(), "目录 ID 不能为空");
    return catalogManageService.deleteCatalog(params.getTenantId(), params.getCatalogId());
  }

  @Operation(summary = "按照分类查询目录树")
  @PostMapping("queryCatalogTree")
  public ResultVO<List<CatalogDTO>> queryCatalogTree(@RequestBody CatalogQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    Assert.hasText(params.getCatalogType(), "目录类型不能为空");
    return ResultVO.success(docCatalogHelper.queryCatalogTree(params));
  }
}
