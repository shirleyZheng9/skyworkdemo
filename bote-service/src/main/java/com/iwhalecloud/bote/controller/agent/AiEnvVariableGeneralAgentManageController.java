package com.iwhalecloud.bote.controller.agent;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.agent.AddAiEnvVariableDTO;
import com.iwhalecloud.bote.dto.agent.AiEnvVariableDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.service.agent.IAiEnvVariableManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * BoteClaw 配置管理 controller
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/generalAgent/envVariable", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "BoteClaw 配置管理-环境变量")
public class AiEnvVariableGeneralAgentManageController {

  // @formatter:off
  private final IAiEnvVariableManageService envVariableManageService;
  private final IRefreshCacheService refreshCacheService;
  // @formatter:on

  @Operation(summary = "保存用户级的环境变量")
  @PostMapping("saveBtAiEnvVariable")
  public ResultVO<List<AiEnvVariableDTO>> saveBtAiEnvVariable(@RequestBody AddAiEnvVariableDTO variable) {
    Assert.notNull(variable.getSpaceId(), "空间 ID 不能为空");
    if (variable.getBotId() == null) {
      variable.setBotId(BaseConsts.BOTE_AI_ID);
    }
    ResultVO<List<AiEnvVariableDTO>> result = envVariableManageService.saveAiEnvVariable(variable);
    if (result.isSuccess()) {
      Long userId = SessionUtil.getLoginInfo().getUserId();
      String key = variable.getSpaceId() + CacheConsts.COLON + variable.getBotId() + CacheConsts.COLON + userId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_GENERAL_AGENT, key);
    }
    return result;
  }

  @Operation(summary = "查询用户级的环境变量列表")
  @PostMapping("queryBtAiEnvVariableList")
  public ResultVO<List<AiEnvVariableDTO>> queryAiEnvVariableList(@RequestBody AiQueryParams queryParams) {
    Assert.notNull(queryParams.getSpaceId(), "空间 ID 不能为空");
    if (queryParams.getBotId() == null) {
      queryParams.setBotId(BaseConsts.BOTE_AI_ID);
    }
    return ResultVO.success(envVariableManageService.queryAiEnvVariableList(queryParams));
  }

}
