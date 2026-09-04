package com.iwhalecloud.bote.dto.chat;

import com.iwhalecloud.bote.dto.bot.RecommendedSceneDTO;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 推荐场景信息
 *
 * @author bianjp
 * @since 2025-03-10
 */
@Getter
@Setter
@ToString
public class SceneRecommendationDTO {
  /** 文本提示 */
  private String text;
  /** 场景列表 */
  private List<RecommendedSceneDTO> scenes;
}
