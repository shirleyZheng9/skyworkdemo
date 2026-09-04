package com.iwhalecloud.bote.controller.channel;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.channel.AiChannelDTO;
import com.iwhalecloud.bote.service.channel.IAiChannelManagerService;
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
 * 渠道管理 controller
 *
 * @author wangtingyun
 * @since 2026-03-09
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/aiChannel", name = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "渠道管理")
public class AiChannelManagerController {

  private final IAiChannelManagerService aiChannelManagerService;

  @Operation(summary = "查询渠道列表")
  @GetMapping("queryAiChannelList")
  public ResultVO<List<AiChannelDTO>> queryAiChannelList(@RequestParam("spaceId") Long spaceId, @RequestParam(value = "botId", required = false) Long botId) {
    Assert.notNull(spaceId, "企业空间 ID 不能为空");
    if (botId == null) {
      botId = BaseConsts.BOTE_AI_ID;
    }
    return ResultVO.success(aiChannelManagerService.queryAiChannelList(spaceId, botId));
  }

  @Operation(summary = "保存渠道")
  @PostMapping("saveAiChannel")
  public ResultVO<AiChannelDTO> saveAiChannel(@RequestBody AiChannelDTO dto) {
    Assert.notNull(dto.getSpaceId(), "企业空间 ID 不能为空");
    if (dto.getBotId() == null) {
      dto.setBotId(BaseConsts.BOTE_AI_ID);
    }
    return aiChannelManagerService.saveAiChannel(dto);
  }

  @Operation(summary = "查询已启用的渠道列表")
  @GetMapping("queryEnabledAiChannelList")
  public ResultVO<List<AiChannelDTO>> queryEnabledAiChannelList(@RequestParam("spaceId") Long spaceId, @RequestParam(value = "botId", required = false) Long botId) {
    Assert.notNull(spaceId, "企业空间 ID 不能为空");
    if (botId == null) {
      botId = BaseConsts.BOTE_AI_ID;
    }
    return ResultVO.success(aiChannelManagerService.queryEnabledAiChannelList(spaceId, botId));
  }

}
