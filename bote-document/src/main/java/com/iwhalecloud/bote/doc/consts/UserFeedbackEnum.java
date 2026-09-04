package com.iwhalecloud.bote.doc.consts;

/**
 * 用户反馈枚举
 */
public enum UserFeedbackEnum {

  /**
   * 用户满意反馈
   */
  LIKE("LIKE", "点赞"),

  /**
   * 用户不满意反馈
   */
  DISLIKE("DISLIKE", "点踩");

  private final String code;
  private final String description;

  UserFeedbackEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }
}
