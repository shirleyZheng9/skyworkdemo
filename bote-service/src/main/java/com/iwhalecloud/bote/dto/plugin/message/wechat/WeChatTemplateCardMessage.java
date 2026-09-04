package com.iwhalecloud.bote.dto.plugin.message.wechat;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模版卡片消息
 *
 * @author qian.sisheng
 * @since 2025-04-14
 */
@Getter
@Setter
@ToString
public class WeChatTemplateCardMessage extends AbstractWeChatMessage {
  /** 模版卡片 */
  private TemplateCard templateCard;

  public WeChatTemplateCardMessage() {
    super(PluginConsts.MESSAGE_TYPE_TEMPLATE_CARD);
  }

  @Getter
  @Setter
  @ToString
  public static class TemplateCard {
    /** 卡片类型 */
    private String cardType;
    /** 二级普通文本 */
    private String subTitleText;
    /** 卡片来源样式信息 */
    private Source source;
    /** 模版卡片的主要内容 */
    private MainTitle mainTitle;
    /** 关键数据样式 */
    private EmphasisContent emphasisContent;
    /** 引用文献样式 */
    private QuoteArea quoteArea;
    /** 二级标题+文本列表，该字段可为空数组 */
    private List<HorizontalContent> horizontalContentList;
    /** 跳转指引样式的列表，该字段可为空数组 */
    private List<Jump> jumpList;
    /** 卡片跳转类型 */
    private CardAction cardAction;
    /** 图片样式 */
    private CardImage cardImage;
    /** 左图右文样式 */
    private ImageTextArea imageTextArea;
    /** 卡片二级垂直内容，该字段可为空数组 */
    private List<VerticalContent> verticalContentList;
  }

  @Getter
  @Setter
  @ToString
  public static class Source {
    /** 来源图片的url */
    private String iconUrl;
    /** 来源图片的描述 */
    private String desc;
    /** 来源文字的颜色 */
    private String descColor;
  }

  @Getter
  @Setter
  @ToString
  public static class MainTitle {
    /** 级标题 */
    private String title;
    /** 标题辅助信息  */
    private String desc;
  }

  @Getter
  @Setter
  @ToString
  public static class EmphasisContent {
    /** 关键数据样式的数据内容 */
    private String title;
    /** 关键数据样式的数据描述内容 */
    private String desc;
    /** 关键数据样式 */
    private String content;
  }

  @Getter
  @Setter
  @ToString
  public static class QuoteArea {
    /** 引用文献样式区域点击事件，0或不填代表没有点击事件，1 代表跳转url，2 代表跳转小程序 */
    private String type;
    /** 点击跳转的小程序的appid，quote_area.type是2时必填 */
    private String appid;
    /** 点击跳转的小程序的pagepath，quote_area.type是2时选填 */
    private String pagepath;
    /** 引用文献样式的标题 */
    private String title;
    /** 点击跳转的url，quote_area.type是1时必填 */
    private String url;
    /** 引用文献样式的引用文案 */
    private String quoteText;
  }

  @Getter
  @Setter
  @ToString
  public static class HorizontalContent {
    /** 二级标题，建议不超过5个字 */
    private String keyName;
    /** 二级文本，如果horizontal_content_list.type是2，该字段代表文件名称（要包含文件类型）*/
    private String value;
    /** 模版卡片的二级标题信息内容支持的类型，1是url，2是文件附件，3 代表点击跳转成员详情 */
    private String type;
    /** 链接跳转的url，horizontal_content_list.type是1时必填 */
    private String url;
    /** 附件的media_id，horizontal_content_list.type是2时必填 */
    private String mediaId;
    /** 成员详情的userid，horizontal_content_list.type是3时必填 */
    private String userId;
  }

  @Getter
  @Setter
  @ToString
  public static class Jump {
    /** 跳转链接样式的文案内容 */
    private String title;
    /** 跳转链接类型，0或不填代表不是链接，1 代表跳转url，2 代表跳转小程序 */
    private String type;
    /** 跳转链接的url，jump_list.type是1时必填 */
    private String url;
    /** 跳转链接的小程序的pagepath，jump_list.type是2时选填 */
    private String pagepath;
  }

  @Getter
  @Setter
  @ToString
  public static class CardAction {
    /** 卡片跳转类型，1 代表跳转url，2 代表打开小程序。text_notice模版卡片中该字段取值范围为[1,2] */
    private String type;
    /** 跳转事件的url，card_action.type是1时必填 */
    private String url;
    /** 跳转事件的小程序的pagepath，card_action.type是2时选填 */
    private String pagepath;
    /** 跳转事件的小程序的appid，card_action.type是2时必填 */
    private String appid;
  }

  @Getter
  @Setter
  @ToString
  public static class CardImage {
    /** 图片的url */
    private String url;
    /** 图片的宽高比，宽高比要小于2.25，大于1.3，不填该参数默认1.3 */
    private String aspectRatio;
  }


  @Getter
  @Setter
  @ToString
  public static class ImageTextArea {
    /** 左图右文样式区域点击事件，0或不填代表没有点击事件，1 代表跳转url，2 代表跳转小程序 */
    private String type;
    /** 点击跳转的url，image_text_area.type是1时必填 */
      private String url;
    /** 左图右文样式的标题 */
    private String title;
    /** 左图右文样式的描述 */
    private String desc;
    /** 左图右文样式的图片url */
    private String imageUrl;
    /** 点击跳转的小程序的pagepath，image_text_area.type是2时选填  */
    private String pagepath;
    /** 点击跳转的小程序的appid，必须是与当前应用关联的小程序，image_text_area.type是2时必填 */
    private String appid;
  }

  @Getter
  @Setter
  @ToString
  public static class VerticalContent {
    /** 卡片二级标题 */
    private String title;
    /** 二级普通文本 */
    private String desc;
  }
}
