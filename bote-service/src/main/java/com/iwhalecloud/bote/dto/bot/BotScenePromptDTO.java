package com.iwhalecloud.bote.dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.bot.BotScenePromptEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 机器人场景提示词
 *
 * @author chen.linfa
 * @since 2024-08-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class BotScenePromptDTO extends BotScenePromptEntity {
}
