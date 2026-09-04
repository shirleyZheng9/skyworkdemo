package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.skill.SkillObjectDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.skill.ISkillObjectManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
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

/**
 * 技能：对象 controller
 *
 * @author auto
 * @since 2024-09-16
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skill/object", name = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "技能：对象管理")
public class SkillObjectManageController {
  private final ISkillObjectManageService objectManageService;

  @Operation(summary = "保存对象")
  @PostMapping("saveSkillObject")
  public ResultVO<SkillObjectDTO> saveSkillObject(@RequestBody SkillObjectDTO object) {
    return objectManageService.saveSkillObject(object);
  }

  @Operation(summary = "查找单个对象")
  @GetMapping("findSkillObject")
  public ResultVO<SkillObjectDTO> findSkillObject(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("busiObjectId") Long busiObjectId) {
    Assert.notNull(busiObjectId, "对象 ID 不能为空");
    return ResultVO.success(objectManageService.findSkillObject(tenantId, busiObjectId));
  }

  @Operation(summary = "查询业务对象列表")
  @PostMapping("querySkillObjectList")
  public ResultVO<List<SkillObjectDTO>> querySkillObjectList(@RequestBody SkillQueryParams params) {
    return ResultVO.success(objectManageService.querySkillObjectList(params));
  }

  @Operation(summary = "分页查询对象")
  @PostMapping("querySkillObjectPage")
  public ResultVO<PageInfo<SkillObjectDTO>> querySkillObjectPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(objectManageService.querySkillObjectPage(params));
  }

  @Operation(summary = "删除对象")
  @GetMapping("deleteSkillObject")
  public ResultVO<Void> deleteSkillObject(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("busiObjectId") Long busiObjectId) {
    Assert.notNull(busiObjectId, "对象 ID 不能为空");
    return objectManageService.deleteSkillObject(tenantId, busiObjectId);
  }
}
