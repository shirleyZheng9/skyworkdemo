package com.iwhalecloud.bote.controller.agent;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.agent.AiModelDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.service.agent.IAiModelManageService;
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
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/generalAgent/model", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "BoteClaw 配置管理-模型")
public class AiModelGeneralAgentManageController {

  // @formatter:off
  private final IAiModelManageService modelManageService;
  private final IRefreshCacheService refreshCacheService;
  // @formatter:on

  @Operation(summary = "保存新增表记录启用模型")
  @PostMapping("saveBtAiModel")
  public ResultVO<AiModelDTO> saveAiModel(@RequestBody AiModelDTO model) {
    Assert.notNull(model.getSpaceId(), "空间 ID 不能为空");
    Assert.notNull(model.getModelId(), "模型 ID 不能为空");
    Assert.isTrue(StringUtils.isNotEmpty(model.getModelType()), "模型类型不能为空");
    if (model.getBotId() == null) {
      model.setBotId(BaseConsts.BOTE_AI_ID);
    }
    ResultVO<AiModelDTO> result = modelManageService.saveAiModel(model);
    if (result.isSuccess()) {
      Long userId = SessionUtil.getLoginInfo().getUserId();
      String key = model.getSpaceId() + CacheConsts.COLON + model.getBotId() + CacheConsts.COLON + userId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_GENERAL_AGENT, key);
    }
    return result;
  }

  @Operation(summary = "查询新增表记录启用模型列表")
  @PostMapping("queryBtAiModelList")
  public ResultVO<List<AiModelDTO>> queryAiModelList(@RequestBody AiQueryParams queryParams) {
    Assert.notNull(queryParams.getSpaceId(), "空间 ID 不能为空");
    if (queryParams.getBotId() == null) {
      queryParams.setBotId(BaseConsts.BOTE_AI_ID);
    }
    return ResultVO.success(modelManageService.queryAiModelList(queryParams));
  }


}
