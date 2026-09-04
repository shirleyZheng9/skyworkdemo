package com.iwhalecloud.bote.doc.consts;

/**
 * 触发类型枚举
 */
public enum TriggerTypeEnum {

  /**
   * 在线文档编辑保存触发
   */
  SAVE("SAVE", "保存触发"),

  /**
   * 文档覆盖上传完成触发
   */
  UPLOAD("UPLOAD", "上传触发"),

  /**
   * 上传触发名字发生了变化
   */
  UPLOAD_NAME("UPLOAD_NAME", "上传触发名字发生了变化"),

  /**
   * 用户手动点击重建触发
   */
  MANUAL("MANUAL", "手动触发"),

  /**
   * 批量导入任务完成触发
   */
  BATCH("BATCH", "批量触发"),

  /**
   * 系统定时任务或外部系统触发
   */
  SYSTEM("SYSTEM", "系统触发");

  private final String code;
  private final String description;

  TriggerTypeEnum(String code, String description) {
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
