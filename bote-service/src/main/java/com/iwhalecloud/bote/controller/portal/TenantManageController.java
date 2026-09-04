package com.iwhalecloud.bote.controller.portal;

import com.github.pagehelper.PageInfo;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.ChineseTranslateUtil;
import com.iwhalecloud.bote.common.util.IconUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.portal.RemoveTenantUserForBeyondDTO;
import com.iwhalecloud.bote.dto.portal.SimpleTenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.portal.query.TenantQueryParams;
import com.iwhalecloud.bote.dto.workspace.SimpleWorkspaceDTO;
import com.iwhalecloud.bote.service.portal.ITenantManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租户管理 controller
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/tenant", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "门户：租户管理")
public class TenantManageController {

  private final ITenantManageService tenantManageService;

  @IgnoreSign
  @IgnoreSession
  @Operation(summary = "获取租户图标")
  @GetMapping(value = "getTenantIcon", produces = MediaType.ALL_VALUE)
  @RequestCacheable(sql = "SELECT updated_time FROM bt_tenant WHERE tenant_id = #{param1}", cacheOnNotFound = true)
  public void getTenantIcon(@RequestParam("tenantId") Long tenantId, HttpServletResponse response) throws IOException {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    String icon = tenantManageService.getTenantIcon(tenantId);
    if (StringUtils.isEmpty(icon)) {
      // 返回默认图标数据
      String path = "assets/avatar/img-tenant-avatar-default.png";
      ClassPathResource resource = new ClassPathResource(path);
      IconUtil.sendPathResourceIcon(response, resource);
    }
    else {
      IconUtil.sendBase64Icon(response, icon);
    }
  }

  @Operation(summary = "获取租户")
  @GetMapping("getTenant")
  public ResultVO<TenantDTO> getTenant(@RequestParam("tenantId") Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return ResultVO.success(tenantManageService.getTenant(tenantId));
  }

  @Operation(summary = "查询租户列表(分页)")
  @PostMapping("queryTenantPage")
  public ResultVO<PageInfo<TenantDTO>> queryTenants(@RequestBody TenantQueryParams queryParams) {
    return ResultVO.success(tenantManageService.queryTenantPage(queryParams));
  }

  @Operation(summary = "查询租户列表")
  @PostMapping("queryTenantList")
  public ResultVO<List<TenantDTO>> queryTenantList(@RequestBody TenantQueryParams queryParams) {
    return ResultVO.success(tenantManageService.queryTenantList(queryParams));
  }

  @Operation(summary = "保存租户")
  @PostMapping("saveTenant")
  public ResultVO<TenantDTO> saveTenant(@RequestBody TenantDTO tenant) {
    return tenantManageService.saveTenant(tenant);
  }

  @Operation(summary = "保存租户（用于外部平台，通过spaceCode获取spaceId）")
  @PostMapping("saveTenantForExternal")
  public ResultVO<TenantDTO> saveTenantForExternal(@RequestBody TenantDTO tenant) {
    return tenantManageService.saveTenantForExternal(tenant);
  }

  @Operation(summary = "保存租户设置")
  @PostMapping("saveTenantDetail")
  public ResultVO<TenantDTO> saveTenantDetail(@RequestBody @Valid TenantDTO tenant) {
    return tenantManageService.saveTenantDetail(tenant);
  }

  @Operation(summary = "删除租户")
  @GetMapping("deleteTenant")
  public ResultVO<Void> deleteTenant(@RequestParam("tenantId") Long tenantId) {
    return tenantManageService.deteleTenant(tenantId);
  }

  @Operation(summary = "查询租户成员列表（分页）")
  @PostMapping("queryTenantUserPage")
  public ResultVO<PageInfo<TenantUserDTO>> queryTenantUserPage(@RequestBody TenantQueryParams queryParams) {
    return ResultVO.success(tenantManageService.queryTenantUserPage(queryParams));
  }

  @Operation(summary = "查询企业授权信息")
  @GetMapping("qryAuthorizedWorkspaces")
  public ResultVO<List<SimpleWorkspaceDTO>> qryAuthorizedWorkspaces() {
    return ResultVO.success(tenantManageService.qryAuthorizedWorkspaces(SessionUtil.getLoginInfo().getUserId()));
  }

  @Operation(summary = "保存租户成员")
  @PostMapping("saveTenantUser")
  public ResultVO<List<TenantUserDTO>> saveTenantUser(@RequestBody List<TenantUserDTO> tenantUserList) {
    return tenantManageService.saveTenantUser(tenantUserList);
  }

  @Operation(summary = "移除租户成员")
  @GetMapping("removeTenantUser")
  public ResultVO<Void> removeTenantUser(@RequestParam("tenantId") Long tenantId, @RequestParam("userIds") List<Long> userIds) {
    Assert.notEmpty(userIds, "租户成员 ID 不能为空");
    return tenantManageService.deleteTenantUser(tenantId, userIds);
  }

  @Operation(summary = "保存租户成员（通过userCode，用于Beyond系统）")
  @PostMapping("saveTenantUserForBeyond")
  public ResultVO<List<TenantUserDTO>> saveTenantUserForBeyond(@RequestBody List<TenantUserDTO> tenantUserList) {
    return tenantManageService.saveTenantUserForBeyond(tenantUserList);
  }

  @Operation(summary = "移除租户成员（通过userCode，用于Beyond系统）")
  @PostMapping("removeTenantUserForBeyond")
  public ResultVO<Void> removeTenantUserForBeyond(@RequestBody RemoveTenantUserForBeyondDTO request) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    Assert.notEmpty(request.getUserCodes(), "用户编码列表不能为空");
    return tenantManageService.removeTenantUserForBeyond(request.getTenantId(), request.getUserCodes());
  }

  @Operation(summary = "翻译中文名称，用于编码自动生成")
  @GetMapping("translateToEnglish")
  public ResultVO<String> translateToEnglish(@Parameter(description = "中文") @RequestParam("chinese") String chinese,
    @Parameter(description = "规则") @RequestParam("rule") String rule) {
    Assert.hasText(chinese, "中文不能为空");
    String translatedText = ChineseTranslateUtil.translateToPinyin(chinese);
    // 翻译值替换成下划线小写
    if (StringUtils.isNotEmpty(translatedText)) {
      translatedText = translatedText.replace(" ", "_").toLowerCase();
    }
    // 转换命名风格
    Converter<@NonNull String, @NonNull String> converter = getCaseConverter(rule);
    if (converter != null) {
      translatedText = converter.convert(translatedText);
    }
    if (StringUtils.isEmpty(translatedText)) {
      return ResultVO.success();
    }
    // 平台限制编码只允许下划线特殊符号
    StringBuilder sb = new StringBuilder();
    char[] chars = translatedText.trim().toCharArray();
    for (char c : chars) {
      if (CharUtils.isAsciiAlpha(c) || CharUtils.isAsciiNumeric(c) || c == 95) {
        sb.append(c);
      }
    }
    return ResultVO.success(sb.toString());
  }

  @Operation(summary = "查询企业下的租户列表")
  @GetMapping("getSpaceTenantList")
  public ResultVO<List<SimpleTenantDTO>> getSpaceTenantList(@RequestParam("spaceId") Long spaceId) {
    return ResultVO.success(tenantManageService.querySpaceTenantList(spaceId));
  }

  /**
   * 获取命名风格转换器
   */
  private Converter<@NonNull String, @NonNull String> getCaseConverter(String rule) {
    if (StringUtils.isEmpty(rule)) {
      return null;
    }
    switch (rule) {
      case "UPPER_CAMEL":
        return CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL);
      case "UPPER_UNDERSCORE":
        return CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.UPPER_UNDERSCORE);
      case "LOWER_UNDERSCORE":
        return CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_UNDERSCORE);
      default:
        return null;
    }
  }
}
