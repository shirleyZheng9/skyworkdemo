package com.iwhalecloud.bote.controller.organization;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.organization.OrganizationFieldConfigDTO;
import com.iwhalecloud.bote.service.organization.IOrganizationFieldConfigService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
 * 组织字段配置管理控制器
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/organization/field-config", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "组织字段配置管理")
public class OrganizationFieldConfigController {
  private final IOrganizationFieldConfigService fieldConfigService;

  @Operation(summary = "保存字段配置")
  @PostMapping("saveFieldConfig")
  public ResultVO<OrganizationFieldConfigDTO> saveFieldConfig(@RequestBody @Valid OrganizationFieldConfigDTO fieldConfig) {
    return fieldConfigService.saveFieldConfig(fieldConfig);
  }

  @Operation(summary = "查询单个字段配置")
  @GetMapping("findFieldConfig")
  public ResultVO<OrganizationFieldConfigDTO> findFieldConfig(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "配置ID") @RequestParam("configId") Long configId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(configId, "配置ID不能为空");
    return ResultVO.success(fieldConfigService.findFieldConfig(spaceId, configId));
  }

  @Operation(summary = "根据字段键名查询字段配置")
  @GetMapping("findFieldConfigByKey")
  public ResultVO<OrganizationFieldConfigDTO> findFieldConfigByKey(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "字段键名") @RequestParam("fieldKey") String fieldKey) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.hasText(fieldKey, "字段键名不能为空");
    return ResultVO.success(fieldConfigService.findFieldConfigByKey(spaceId, fieldKey));
  }

  @Operation(summary = "查询企业的所有字段配置")
  @GetMapping("queryFieldConfigsBySpace")
  public ResultVO<List<OrganizationFieldConfigDTO>> queryFieldConfigsBySpace(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "是否启用") @RequestParam(value = "isEnabled", defaultValue = "true", required = false) Boolean isEnabled,
    @Parameter(description = "查询关键字") @RequestParam(value = "searchContent", required = false) String searchContent) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    return ResultVO.success(fieldConfigService.queryFieldConfigsBySpace(spaceId, isEnabled, searchContent));
  }

  @Operation(summary = "删除字段配置")
  @PostMapping("deleteFieldConfig")
  public ResultVO<Void> deleteFieldConfig(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "配置ID") @RequestParam("configId") Long configId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.notNull(configId, "配置ID不能为空");
    return fieldConfigService.deleteFieldConfig(spaceId, configId);
  }

  @Operation(summary = "检查字段键名是否存在")
  @GetMapping("checkFieldKeyExists")
  public ResultVO<Boolean> checkFieldKeyExists(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "字段键名") @RequestParam("fieldKey") String fieldKey,
    @Parameter(description = "排除的配置ID") @RequestParam(value = "excludeConfigId", required = false) Long excludeConfigId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.hasText(fieldKey, "字段键名不能为空");
    boolean exists = fieldConfigService.checkFieldKeyExists(spaceId, fieldKey, excludeConfigId);
    return ResultVO.success(exists);
  }

  @Operation(summary = "验证扩展字段数据")
  @PostMapping("validateExtFields")
  public ResultVO<Void> validateExtFields(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "扩展字段数据") @RequestBody Map<String, Object> extFields) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    return fieldConfigService.validateExtFields(spaceId, extFields);
  }

  @Operation(summary = "获取字段配置的映射")
  @GetMapping("getFieldConfigMap")
  public ResultVO<Map<String, OrganizationFieldConfigDTO>> getFieldConfigMap(
    @Parameter(description = "企业空间ID") @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "是否启用") @RequestParam(value = "isEnabled", defaultValue = "true") Boolean isEnabled) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    return ResultVO.success(fieldConfigService.getFieldConfigMap(spaceId, isEnabled));
  }
}
