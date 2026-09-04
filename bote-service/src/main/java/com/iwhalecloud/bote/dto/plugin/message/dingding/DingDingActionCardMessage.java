package com.iwhalecloud.bote.dto.plugin.message.dingding;


import com.iwhalecloud.bote.common.consts.PluginConsts;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * actionCard消息
 *
 * @author qian.sisheng
 * @since 2025-04-16
 */
@Getter
@Setter
@ToString
public class DingDingActionCardMessage extends AbstractDingDingMessage {
  /** actionCard */
  private ActionCard actionCard;

  public DingDingActionCardMessage() {
    super(PluginConsts.MESSAGE_TYPE_ACTION_CARD);
  }

  @Getter
  @Setter
  @ToString
  public static class ActionCard {
    /** 0-按钮竖直排列，1-按钮横向排列 */
    private String btnOrientation;
    /** 按钮的信息 */
    private List<Btn> btns;
    /** 单个按钮的标题 */
    private String singleTitle;
    /** 消息内容 */
    private String text;
    /** 首屏会话透出的展示内容 */
    private String title;

    /** 富文本，仅发送工作通知可使用 */
    private List<BtnJson> btnJsonList;
    /** markdown，仅发送工作通知可使用 */
    private String markdown;
    /** 单个按钮的url，仅发送工作通知可使用 */
    private String singleUrl;
  }

  @Getter
  @Setter
  public static class Btn {
    /** 按钮的url */
    private String actionURL;
    /** 按钮的标题 */
    private String title;
  }

  @Getter
  @Setter
  public static class BtnJson {
    /** 按钮的url */
    private String actionURL;
    /** 按钮的标题 */
    private String title;
  }
}
