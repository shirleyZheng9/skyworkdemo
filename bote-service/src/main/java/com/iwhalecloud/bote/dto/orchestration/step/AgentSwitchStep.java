package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 场景切换
 * @author zyt 11472359
 */
@Getter
@Setter
public class AgentSwitchStep extends AbstractStep {
  /** 切换模式 */
  private String switchMode;
  /** 智能应用ID 当前对话所在的智能应用ID */
  private Long sceneAppId;
  /** 场景ID - 自动切换 */
  private Long sceneId;
  /** 对话内容 - 自动切换 */
  private String userMessage;
  /** 参数 - 自动切换 */
  private ParameterSpec parameters;

  /** 提示语 - 用户选择切换 */
  private String userPrompt;
  /** 智能体来源 - 用户选择切换 */
  private String agentSource;
  /** 场景IDs - 用户选择切换 */
  private String sceneIds;
  /** 场景列表 - 用户选择切换 */
  private List<SceneInfo> scenes;
  /** 意图识别消息 - 用户选择切换 */
  private String intentRecognitionMessage;
  /** 机器人ID - 用户选择切换 */
  private Long botId;
  /** 引用变量入参 - 引用变量 - 用户选择切换 */
  private String referenceScenes;


  public AgentSwitchStep() {
    super(StepType.AGENT_SWITCH);
  }


  /**
   * 场景信息内部类
   */
  @Getter
  @Setter
  public static class SceneInfo {
    /** 场景名称 */
    private String sceneName;
    /** 场景ID */
    private String sceneId;
    /** 智能应用ID */
    private String sceneAppId;
  }
}

