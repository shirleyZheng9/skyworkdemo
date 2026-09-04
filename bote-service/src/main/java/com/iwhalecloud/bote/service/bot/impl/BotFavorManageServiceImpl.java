package com.iwhalecloud.bote.service.bot.impl;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.bot.BotFavorDTO;
import com.iwhalecloud.bote.mapper.bot.BotFavorManageMapper;
import com.iwhalecloud.bote.service.bot.IBotFavorManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 应用收藏服务
 *
 * @author auto
 * @since 2024-09-14
 */
@Service
@RequiredArgsConstructor
public class BotFavorManageServiceImpl implements IBotFavorManageService {

  private final BotFavorManageMapper botFavorManageMapper;

  @Override
  @Transactional
  public ResultVO<Void> addBotFavor(Long tenantId, Long botId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (botFavorManageMapper.getBotFavor(tenantId, botId, userId) != null) {
      return ResultVO.fail("应用已收藏，无需重复操作");
    }
    BotFavorDTO botFavor = new BotFavorDTO();
    botFavor.setFavorId(Sequences.BOT_FAVOR_ID.next());
    botFavor.setBotId(botId);
    botFavor.setUserId(userId);
    botFavor.setTenantId(tenantId);
    botFavor.setCreatorId(userId);
    botFavor.setStatusCd(BaseConsts.STATUS_CD_VALID);
    botFavorManageMapper.insertBotFavor(botFavor);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> cancelBotFavor(Long tenantId, Long botId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (botFavorManageMapper.getBotFavor(tenantId, botId, userId) == null) {
      return ResultVO.fail("未关注当前应用");
    }
    botFavorManageMapper.deleteBotFavor(tenantId, botId, userId);
    return ResultVO.success();
  }

  @Override
  public List<BotFavorDTO> queryBotFavorList(Long tenantId, Long userId) {
    return botFavorManageMapper.selectBotFavorByUserId(userId, tenantId);
  }
}
