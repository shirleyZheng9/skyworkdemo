package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * PlayWright 自动化步骤
 *
 * @author bianjp
 * @since 2026-01-14
 */
@Getter
@Setter
public class PlaywrightStep extends AbstractStep {
  /** 是否自定义会话 ID，默认关闭 */
  private Boolean useCustomChatId;
  /** 会话 ID(取值表达式), 仅在关闭自动管理会话时使用 */
  private String chatId;
  /** 是否自动释放沙箱，默认开启 */
  private Boolean autoRelease;
  /** 是否启用 VNC 调试，默认开启 */
  private Boolean vncEnabled;
  /** VNC 是否只允许查看，默认关闭 */
  private Boolean vncReadonly;
  /** 脚本内容 */
  private String scriptContent;
  /** 文件。支持常量值、引用表达式，常量值只能是单个下载地址、data URL、文件 ID, 引用表达式可以返回多个 */
  private String files;
  /** 入参 */
  private ParameterSpec parameters;
  /** 出参 */
  private ParameterSpec outData;

  public PlaywrightStep() {
    super(StepType.PLAYWRIGHT);
  }
}
