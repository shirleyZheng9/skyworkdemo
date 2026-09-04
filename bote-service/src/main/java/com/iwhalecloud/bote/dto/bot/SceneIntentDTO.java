package com.iwhalecloud.bote.dto.bot;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景意图信息
 *
 * <p>用作场景意图的缓存对象，只包含需要用到的属性</p>
 *
 * @author bianjp
 * @since 2025-04-08
 */
@Getter
@Setter
@ToString
public class SceneIntentDTO {
  /** 场景 ID */
  private Long sceneId;
  /** 场景名称 */
  private String sceneName;
  /** 场景描述（意图识别使用） */
  private String sceneDesc;

  /** 意图问句，内容可能过多，不适合放入缓存 */
  private List<String> questions;
}
