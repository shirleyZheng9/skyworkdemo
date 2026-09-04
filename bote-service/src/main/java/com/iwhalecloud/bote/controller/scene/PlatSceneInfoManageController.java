package com.iwhalecloud.bote.controller.scene;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.bot.PlatSceneInfoDTO;
import com.iwhalecloud.bote.dto.bot.query.PlatSceneInfoQueryParams;
import com.iwhalecloud.bote.dto.scene.SimpleSceneDTO;
import com.iwhalecloud.bote.service.bot.IPlatSceneInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模板智能体管理 controller
 *
 * @author auto
 * @since 2025-06-21
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/platSceneInfo", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "模板智能体管理")
public class PlatSceneInfoManageController {

  private final IPlatSceneInfoManageService platSceneInfoManageService;

  @Operation(summary = "查询单个模板智能体")
  @GetMapping("findPlatSceneInfo")
  public ResultVO<PlatSceneInfoDTO> findPlatSceneInfo(
    @Parameter(description = "模板ID", required = true) @RequestParam("platSceneId") Long platSceneId) {
    Assert.notNull(platSceneId, "模板ID不能为空");
    return ResultVO.success(platSceneInfoManageService.findPlatSceneInfo(platSceneId));
  }

  @Operation(summary = "保存模板智能体")
  @PostMapping("savePlatSceneInfo")
  public ResultVO<PlatSceneInfoDTO> savePlatSceneInfo(@RequestBody PlatSceneInfoDTO platSceneInfo) {
    Assert.notNull(platSceneInfo.getSceneId(), "智能体 ID 不能为空");
    Assert.notNull(platSceneInfo.getCatalogItemId(), "模板分类不能为空");
    // 移除排序值验证，保存时不需要传入排序值，将在服务层设置默认值
    return platSceneInfoManageService.savePlatSceneInfo(platSceneInfo);
  }

  @Operation(summary = "删除模板智能体")
  @GetMapping("deletePlatSceneInfo")
  public ResultVO<Void> deletePlatSceneInfo(@Parameter(description = "模板ID", required = true) @RequestParam("platSceneId") Long platSceneId) {
    Assert.notNull(platSceneId, "模板ID不能为空");
    return platSceneInfoManageService.deletePlatSceneInfo(platSceneId);
  }

  @Operation(summary = "置顶模板智能体")
  @GetMapping("topPlatSceneInfo")
  public ResultVO<Void> topPlatSceneInfo(@Parameter(description = "模板ID", required = true) @RequestParam("platSceneId") Long platSceneId) {
    Assert.notNull(platSceneId, "模板ID不能为空");
    return platSceneInfoManageService.topPlatSceneInfo(platSceneId);
  }

  @Operation(summary = "查询模板智能体列表")
  @PostMapping("queryPlatSceneInfoList")
  public ResultVO<List<PlatSceneInfoDTO>> queryPlatSceneInfoList(@RequestBody PlatSceneInfoQueryParams queryParams) {
    return ResultVO.success(platSceneInfoManageService.queryPlatSceneInfoList(queryParams));
  }

  @Operation(summary = "查询模板智能体列表（分页）")
  @PostMapping("queryPlatSceneInfoPage")
  public ResultVO<PageInfo<PlatSceneInfoDTO>> queryPlatSceneInfoPage(@RequestBody PlatSceneInfoQueryParams queryParams) {
    return ResultVO.success(platSceneInfoManageService.queryPlatSceneInfoPage(queryParams));
  }

  @Operation(summary = "获取可选择的智能体")
  @GetMapping("queryAvailableScenes")
  public ResultVO<List<PlatSceneInfoDTO>> queryAvailableScenes(
    @Parameter(description = "搜索关键词") @RequestParam(value = "searchContent", required = false) String searchContent) {

    return ResultVO.success(platSceneInfoManageService.queryAvailableScenes(searchContent));
  }

  @Operation(summary = "查询智能体列表（分页），用于自主规划技智能体技能选择")
  @PostMapping("querySimpleScenePage")
  public ResultVO<PageInfo<SimpleSceneDTO>> querySimpleScenePage(@RequestBody PlatSceneInfoQueryParams queryParams) {
    return ResultVO.success(platSceneInfoManageService.querySimpleScenePage(queryParams));
  }
}
