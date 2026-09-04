package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.AttrSpecValueImport;
import com.iwhalecloud.bote.dto.skill.SkillAttrSpecDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.importer.AttrImporter;
import com.iwhalecloud.bote.service.skill.ISkillAttrManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.transform.imports.Importers;
import com.iwhalecloud.bss.litchi.transform.imports.descriptor.ImportDescriptor;
import com.iwhalecloud.bss.litchi.transform.imports.importers.dto.ImportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

/**
 * 技能：属性管理 controller
 *
 * @author auto
 * @since 2024-09-15
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skill/attr", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "技能：属性管理")
public class SkillAttrManageController {
  private final ISkillAttrManageService attrManageService;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "查询单个属性")
  @GetMapping("findAttrSpec")
  public ResultVO<SkillAttrSpecDTO> findAttrSpec(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(name = "attrId") Long attrId) {
    Assert.notNull(attrId, "属性 ID 不能为空");
    return ResultVO.success(attrManageService.findAttrSpec(tenantId, attrId));
  }

  @Operation(summary = "保存属性")
  @PostMapping("saveAttrSpec")
  public ResultVO<SkillAttrSpecDTO> saveAttrSpec(@RequestBody @Valid SkillAttrSpecDTO attrSpec) {
    ResultVO<SkillAttrSpecDTO> result = attrManageService.saveAttrSpec(attrSpec);
    if (result.isSuccess()) {
      String key = attrSpec.getTenantId() + CacheConsts.COLON + attrSpec.getAttrNbr();
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_ATTR_SPEC, key);
    }
    return result;
  }

  @Operation(summary = "删除属性")
  @GetMapping("deleteAttrSpec")
  public ResultVO<Void> deleteAttrSpec(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(name = "attrId") Long attrId) {
    Assert.notNull(attrId, "属性 ID 不能为空");
    ResultVO<Void> result = attrManageService.deleteAttrSpec(tenantId, attrId);
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + attrId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_ATTR_SPEC, key);
    }
    return result;
  }

  @Operation(summary = "查询属性列表")
  @PostMapping("queryAttrSpecList")
  public ResultVO<List<SkillAttrSpecDTO>> queryAttrSpecList(@RequestBody SkillQueryParams queryParams) {
    return ResultVO.success(attrManageService.queryAttrSpecList(queryParams));
  }

  @Operation(summary = "分页查询属性")
  @PostMapping("queryAttrSpecPage")
  public ResultVO<PageInfo<SkillAttrSpecDTO>> queryAttrSpecPage(@RequestBody SkillQueryParams queryParams) {
    return ResultVO.success(attrManageService.queryAttrSpecPage(queryParams));
  }

  @Operation(summary = "导入静态数据")
  @PostMapping("importAttrSpecValue")
  public ResultVO<List<ResultVO<ImportResult>>> importAttrSpecValue(@RequestParam("tenantId") Long tenantId,
    @RequestParam("catalogItemId") Long catalogItemId, @RequestParam("file") MultipartFile file) {
    // 导入格式描述
    // @formatter:off
    ImportDescriptor<AttrSpecValueImport> descriptor = ImportDescriptor.<AttrSpecValueImport>builder()
      .fitToShowDetailPop(false)
      .failMaxSizeIfDetailPop(0)
      .showDetailPop(true)
      .startColumnIndex(0)
      .endColumnIndex(9)
      .batch(false)
      .checkRecordLimitIf(true)
      .sheetNum(0)
      .build();
    // @formatter:on
    // 处理上传的文件
    return Importers.imports(file, descriptor, new AttrImporter(tenantId, catalogItemId));
  }
}
