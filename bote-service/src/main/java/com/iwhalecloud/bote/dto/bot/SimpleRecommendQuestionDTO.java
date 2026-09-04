package com.iwhalecloud.bote.dto.bot;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * BOT的推荐位定义
 *
 * @author chen.linfa
 * @since 2025-04-17
 */
@Getter
@Setter
@ToString
public class SimpleRecommendQuestionDTO {
  private String themeOpt;

  /** 快捷入口 */
  private GroupSettingInfo quickLinkSettingInfo;

  /** 推荐问题 */
  private SettingInfo questionSettingInfo;
  /** 推荐智能体 */
  private SettingInfo scenesSettingInfo;
  /** 快捷指令 */
  private SettingInfo pointSettingInfo;
  /** 自定义推荐位 */
  private Map<String, Object> customSettingInfo;
  /** 主页设置内容 */
  private Map<String, Object> welcome;
  /** 侧边栏配置 */
  private Map<String, Object> sidebar;
  /** 开场白配置 */
  private Map<String, Object> dialogue;
  /** 版本数据 */
  private String version;

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SettingInfo {
    private Boolean display;
    private Integer gridColumns;
    private List<Map<String, Object>> list;
  }

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GroupSettingInfo {
    private Boolean display;
    private Integer gridColumns;
    private List<GroupInfo> list;
  }

  /**
   * 推荐信息
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GroupInfo {
    /** ID */
    private String id;
    /** 标题 */
    private String title;
    /** 子标题 */
    private String subTitle;
    /** 推荐信息 */
    private List<QuestionInfo> items;
  }

  /**
   * 推荐信息
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class QuestionInfo {
    /** ID */
    private String id;
    /** 分类（request：常见问题 point：指令  sceen：场景） */
    private String type;
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 图标 */
    private String icon;
    /** 开场白 */
    private String prologue;
    /** 指令页面信息 */
    private Object pageContentInfo;
  }
}
