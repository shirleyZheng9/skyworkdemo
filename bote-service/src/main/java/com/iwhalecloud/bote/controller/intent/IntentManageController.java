package com.iwhalecloud.bote.controller.intent;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bote.service.intent.IIntentManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
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

/**
 * 意图管理 controller
 *
 * @author auto
 * @since 2025-02-17
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/intent", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "意图：意图管理")
public class IntentManageController {

  private final IIntentManageService intentManageService;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "查询策略")
  @GetMapping("findStrategy")
  public ResultVO<IntentStrategyDTO> findStrategy(@RequestParam(name = "tenantId") Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    IntentStrategyDTO strategy = intentManageService.findStrategy(tenantId);
    strategy.setPrompt(SystemParameter.GENERATE_PLAN_PROMPT.getValueFromDb());
    return ResultVO.success(strategy);
  }

  @Operation(summary = "保存策略")
  @PostMapping("saveStrategy")
  public ResultVO<IntentStrategyDTO> saveStrategy(@RequestBody IntentStrategyDTO strategy) {
    Assert.notNull(strategy.getTenantId(), "租户 ID 不能为空");
    ResultVO<IntentStrategyDTO> result = intentManageService.saveStrategy(strategy);
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_TENANT_SETTING, strategy.getTenantId().toString());
    }
    return result;
  }
}
