package com.iwhalecloud.bote.dto.plugin.message.dingding;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OA消息
 *
 * @author qian.sisheng
 * @since 2025-04-16
 */
@Getter
@Setter
@ToString
public class DingDingOAMessage extends AbstractDingDingMessage {
  /** oa消息 */
  private Oa oa;

  public DingDingOAMessage() {
   super(PluginConsts.MESSAGE_TYPE_OA);
  }

  @Getter
  @Setter
  @ToString
  public static class Oa {
    /** oa消息体 */
    private Body body;
    /** oa头部 */
    private Head head;
    /** 消息跳转链接 */
    private String messageUrl;
    /** pc端跳转链接 */
    private String pcMessageUrl;
    /** 状态栏 */
    private StatusBar statusBar;
  }

  @Getter
  @Setter
  @ToString
  public static class StatusBar {
    /** 状态栏文案 */
    private String statusValue;
    /** 状态栏背景色，默认为黑色，推荐0xFF加六位颜色值 */
    private String statusBg;
  }

  @Getter
  @Setter
  @ToString
  public static class Body {
    /** 自定义的作者名字 */
    private String author;
    /** 消息体的内容 */
    private String content;
    /** 文件数量 */
    private String fileCount;
    /** 消息体的表单 */
    private List<Form> form;
    /** 消息体中的图片，支持图片资源@mediaId。建议宽600像素 x 400像素，宽高比3 : 2。 */
    private String image;
    /** 单行富文本信息 */
    private Rich rich;
    /** 消息体的标题 */
    private String title;
  }

  @Getter
  @Setter
  @ToString
  public static class Form {
    /** 消息体的关键字 */
    private String key;
    /** 消息体的关键字对应的值 */
    private String value;
  }

  @Getter
  @Setter
  @ToString
  public static class Rich {
    /** 单行富文本信息的数目 */
    private String num;
    /** 单行富文本信息的单位 */
    private String unit;
  }

  @Getter
  @Setter
  @ToString
  public static class Head {
    /** 消息头部的背景颜色。长度限制为8个英文字符，其中前2为表示透明度，后6位表示颜色值。不要添加0x。 */
    private String bgcolor;
    /** 消息头部的标题 */
    private String text;
  }
}
