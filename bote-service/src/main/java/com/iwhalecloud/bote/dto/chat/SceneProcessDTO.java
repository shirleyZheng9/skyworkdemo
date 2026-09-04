package com.iwhalecloud.bote.dto.chat;

import com.iwhalecloud.bote.entity.chat.SceneProcessEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话场景进度 DTO
 *
 * @author auto
 * @since 2024-12-17
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SceneProcessDTO extends SceneProcessEntity {
  /** 机器人名称 */
  private String botName;
}
