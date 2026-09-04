package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.LabelDTO;
import com.iwhalecloud.bote.dto.base.query.LabelQueryParams;
import com.iwhalecloud.bote.service.base.ILabelManageService;
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

import jakarta.validation.Valid;

/**
 * 标签管理 controller
 *
 * @author auto
 * @since 2024-09-13
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/label", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：标签管理")
public class LabelManageController {
  private final ILabelManageService labelManageService;

  @Operation(summary = "保存标签")
  @PostMapping("saveLabel")
  public ResultVO<LabelDTO> saveLabel(@RequestBody @Valid LabelDTO label) {
    return labelManageService.saveLabel(label);
  }

  @Operation(summary = "删除标签")
  @GetMapping("deleteLabel")
  public ResultVO<Void> deleteLabel(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("labelId") Long labelId) {
    Assert.notNull(labelId, "标签 ID 不能为空");
    return labelManageService.deleteLabel(tenantId, labelId);
  }

  @Operation(summary = "按照分类查询标签列表")
  @PostMapping("queryLabelList")
  public ResultVO<List<LabelDTO>> queryLabelList(@RequestBody LabelQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    Assert.hasText(params.getLabelType(), "标签类型不能为空");
    return ResultVO.success(labelManageService.queryLabelList(params));
  }
}
