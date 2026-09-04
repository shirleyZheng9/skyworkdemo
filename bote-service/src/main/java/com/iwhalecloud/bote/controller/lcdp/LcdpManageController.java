package com.iwhalecloud.bote.controller.lcdp;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.lcdp.LcdpAppAggregateInfoDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpAppVersionDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpAttrSpecDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpPageInstDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpStandardServiceDTO;
import com.iwhalecloud.bote.dto.lcdp.query.LcdpQueryParams;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.service.lcdp.ILcdpManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 灵犀平台管理服务 controller
 *
 * @author qian.sisheng
 * @since 2025-06-05
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "lcdp/manage", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "灵犀：灵犀平台管理服务")
public class LcdpManageController {

  private final ILcdpManageService lcdpManageService;

  @Operation(summary = "查询编排服务列表")
  @PostMapping("queryStandardServicePage")
  public ResultVO<PageInfo<LcdpStandardServiceDTO>> queryStandardServicePage(@RequestBody LcdpQueryParams queryParams) {
    return ResultVO.success(lcdpManageService.queryStandardServicePage(queryParams));
  }

  @Operation(summary = "查询应用版本列表")
  @GetMapping("queryAppVersionList")
  public ResultVO<List<LcdpAppVersionDTO>> queryAppVersionList(@RequestParam(value = "tenantId", required = false) Long tenantId) {
    return ResultVO.success(lcdpManageService.queryAppVersionList(tenantId));
  }

  @Operation(summary = "查询应用聚合信息")
  @PostMapping("queryAppAggregateInfo")
  public ResultVO<LcdpAppAggregateInfoDTO> queryAppAggregateInfo(@RequestBody LcdpQueryParams queryParams) {
    return ResultVO.success(lcdpManageService.queryAppAggregateInfo(queryParams));
  }

  @Operation(summary = "查询页面实例列表")
  @PostMapping("queryPageInstPage")
  public ResultVO<PageInfo<LcdpPageInstDTO>> queryPageInstPage(@RequestBody LcdpQueryParams queryParams) {
    return ResultVO.success(lcdpManageService.queryPageInstPage(queryParams));
  }

  @Operation(summary = "查询属性规格列表")
  @PostMapping("queryAttrSpecList")
  public ResultVO<PageInfo<LcdpAttrSpecDTO>> queryAttrSpecPage(@RequestBody LcdpQueryParams queryParams) {
    return ResultVO.success(lcdpManageService.queryAttrSpecPage(queryParams));
  }

  @Operation(summary = "批量保存页面实例")
  @PostMapping("batchSavePageInst")
  public ResultVO<Void> batchSavePageInst(@RequestBody List<SkillPageDTO> skillPageList) {
    if (CollectionUtils.isEmpty(skillPageList)) {
      return ResultVO.success();
    }
    return lcdpManageService.batchSavePageInst(skillPageList);
  }

  @Operation(summary = "保存静态数据")
  @PostMapping("batchSaveAttrSpec")
  public ResultVO<Void> batchSaveAttrSpec(@RequestBody LcdpAttrSpecDTO attrSpec) {
    return lcdpManageService.batchSaveAttrSpec(attrSpec);
  }

  @Operation(summary = "保存编排服务")
  @PostMapping("batchSaveService")
  public ResultVO<Void> batchSaveService(@RequestBody SkillServiceDTO service) {
    return lcdpManageService.batchSaveService(service);
  }
}
