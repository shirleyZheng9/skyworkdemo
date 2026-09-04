package com.iwhalecloud.bote.dto.chat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图结果
 *
 * @author Admin
 */
@Getter
@Setter
@ToString
public class IntentResultDTO {
  /** 进入场景 */
  private SceneIntentResultDTO enterScene;
  /** 退出场景 */
  private SceneIntentResultDTO exitScene;

  public IntentResultDTO(SceneIntentResultDTO enterScene, SceneIntentResultDTO exitScene) {
    this.enterScene = enterScene;
    this.exitScene = exitScene;
  }
}
