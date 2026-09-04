package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.base.DcCfgDTO;
import com.iwhalecloud.bote.dto.base.DcCfgSafeDTO;
import com.iwhalecloud.bote.dto.base.DcCfgSaveRequest;
import com.iwhalecloud.bote.service.base.IDcCfgService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统参数管理
 *
 * @author auto
 * @since 2024-09-14
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "dccfg", name = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@RestController
@Tag(name = "基础：系统参数管理")
public class DcCfgController {
  private final IRefreshCacheService refreshCacheService;

  private final IDcCfgService dcCfgService;

  private final DcParamCache paramCache;

  /** 基础的平台配置参数列表，不需要鉴权就能查询 */
  private final List<String> PLATFORM_PARAM_CODES = List.of(
    "PLATFORM_NAME", "PLATFORM_SUB_NAME", "PLATFORM_DESC",
    "PLATFORM_BOT_MODEL_NAME",
    "HELPER_DOC_URL",
    "REGISTER_ENABLED",
    "SMS_CODE_ENABLED");

  @GetMapping("querySwitchList")
  @Operation(summary = "查询功能开关列表")
  public ResultVO<List<DcCfgDTO>> querySwitchList() {
    return ResultVO.success(dcCfgService.querySwitchList());
  }

  @GetMapping("queryParamList")
  @Operation(summary = "查询参数列表(包含功能开关)")
  public ResultVO<List<DcCfgDTO>> queryParamList(@RequestParam(value = "searchContent", required = false) String searchContent,
    @RequestParam(value = "type", required = false) String type) {
    return ResultVO.success(dcCfgService.queryParamList(searchContent, type));
  }

  @PostMapping("modParamData")
  @Operation(summary = "修改参数")
  public ResultVO<Void> modParamData(@RequestBody DcCfgSaveRequest dcCfgSaveRequest) {
    ResultVO<Void> result = dcCfgService.modParamData(dcCfgSaveRequest);
    if (result.isSuccess()) {
      // 刷新缓存
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_DC_PARAM, dcCfgSaveRequest.getParamCode());
    }
    return result;
  }

  @GetMapping("getParamValue")
  @Operation(summary = "获取参数值")
  public ResultVO<String> getParamValue(@RequestParam("paramCode") String paramCode) {
    return ResultVO.success(paramCache.getDcParamValByCode(paramCode));
  }

  @GetMapping("getParamValues")
  @Operation(summary = "批量获取参数值")
  public ResultVO<Map<String, String>> getParamValues(@RequestParam("paramCodes") List<String> paramCodes) {
    if (CollectionUtils.isEmpty(paramCodes)) {
      return ResultVO.success(Collections.emptyMap());
    }
    // 并行查询
    Map<String, String> map = paramCodes.stream()
      .filter(StringUtils::isNotEmpty)
      .distinct()
      .parallel()
      .map(p -> Pair.of(p, paramCache.getDcParamValByCode(p)))
      .filter(p -> p.getValue() != null)
      .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    return ResultVO.success(map);
  }

  @IgnoreSession
  @IgnoreSign
  @GetMapping("getPlatformParam")
  @Operation(summary = "获取平台参数：免鉴权使用")
  public ResultVO<Map<String, Object>> getPlatformParam() {
    Map<String, Object> map = new LinkedHashMap<>();
    for (String paramCode : PLATFORM_PARAM_CODES) {
      map.put(paramCode, paramCache.getDcParamValByCode(paramCode));
    }
    return ResultVO.success(map);
  }

  @IgnoreSession
  @IgnoreSign
  @GetMapping("getVerifyCodeEnable")
  @Operation(summary = "获取图形验证开关")
  public ResultVO<Boolean> getVerifyCodeEnable() {
    return ResultVO.success(SystemParameter.VERIFICATION_CODE_ENABLE.getBooleanValueFromDb());
  }

  @GetMapping("querySafeParamList")
  @Operation(summary = "查询安全策略参数列表")
  public ResultVO<List<DcCfgSafeDTO>> querySafeParamList() {
    return ResultVO.success(dcCfgService.getSafeParamList());
  }

  @PostMapping("modSafeParamData")
  @Operation(summary = "修改安全策略参数值")
  public ResultVO<Void> modSafeParamData(@RequestBody DcCfgSaveRequest dcCfgSaveRequest) {
    ResultVO<Void> result = dcCfgService.modSafeParamData(dcCfgSaveRequest);
    if (result.isSuccess()) {
      // 刷新缓存
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_DC_PARAM, dcCfgSaveRequest.getParamCode());
      // 同步刷新安全校验规则缓存
      if (BaseConsts.PASSWORD_STRATEGY_CFG_LIST.contains(dcCfgSaveRequest.getParamCode())) {
        refreshCacheService.refresh(CacheConsts.CACHE_NAME_DC_PARAM, SystemParameter.SECURITY_RULE_LOWER.getCode());
      }
    }
    return result;
  }

}
