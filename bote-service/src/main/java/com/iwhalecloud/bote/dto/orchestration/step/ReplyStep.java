package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 回复步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class ReplyStep extends AbstractStep {
  /** 消息内容(模板内容，或脚本） */
  private String messageContent;
  /** 文件下载配置，可选 */
  private DownloadConfig download;
  /** 输出内容格式配置（默认 markdown），存量定义，后续移除 */
  private String contentType;
  /** 输出内容格式配置 */
  private ContentTypeConfig contentTypeConfig;
  /** 关联配置，可选 */
  private RelatedConfig related;

  public ReplyStep() {
    super(StepType.REPLY);
  }

  /**
   * 文件下载配置
   */
  @Getter
  @Setter
  @ToString
  public static class DownloadConfig {
    /** 是否启用下载功能 */
    private Boolean enabled;
    /** 文件类型 */
    private String fileType;
    /** 是否使用自定义内容 */
    private Boolean customEnabled;
    /** 自定义下载内容（模板字符串），可选，未配置时使用回复内容 */
    private String customContent;
  }

  /**
   * 关联配置
   */
  @Getter
  @Setter
  @ToString
  public static class RelatedConfig {
    /** 网页内容 */
    private String webPageContent;
    /** 附件内容 */
    private String fileContent;
  }

  /**
   * 内容格式配置
   */
  @Getter
  @Setter
  @ToString
  public static class ContentTypeConfig {
    /** 输出内容格式（默认 markdown） */
    private String contentType;
    /** 段落归属的文档 */
    private String group;
    /** 段落排序 */
    private String sortby;
  }
}
