package com.iwhalecloud.bote.common.diffc.persist.impl.bot;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.bot.BotUserExperienceDTO;
import com.iwhalecloud.bote.mapper.bot.BotUserExperienceManageMapper;
import org.springframework.stereotype.Component;

/**
 * 用户会话辅助信息
 *
 * @author qian.sisheng
 * @since 2024/7/29
 */
@Component
public final class BotUserExperienceDifferencePersistence extends BaseRootPersistence<BotUserExperienceDTO> {
 public BotUserExperienceDifferencePersistence(BotUserExperienceManageMapper botUserExperienceManageMapper) {
    setAddConsumer(botUserExperienceManageMapper::insertBotUserExperience);
    setModifyConsumer(botUserExperienceManageMapper::updateBotUserExperience);
 }
}
