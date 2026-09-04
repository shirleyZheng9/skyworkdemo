package com.iwhalecloud.bote.service.publish.platform.wework;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 企业微信消息响应抽象类
 * 替代Map<String, Object>，提供类型安全的消息响应
 *
 * @author system
 * @since 2025-01-09
 */
@Getter
@Setter
@ToString
public abstract class WeWorkMessageResponse {
  /** 消息类型 */
  protected String msgType;
  /** 接收者用户ID */
  protected String toUser;
  /** 应用ID */
  protected Long agentId;
  /** 是否安全发送 */
  protected Boolean safe = false;
  /** 是否启用ID转换 */
  protected Boolean enableIdTrans = false;
  /** 是否启用重复检查 */
  protected Boolean enableDuplicateCheck = false;
  /** 重复检查间隔（秒） */
  protected Integer duplicateCheckInterval = 1800;

  /**
   * 创建文本消息响应
   */
  public static TextResponse createTextResponse(String content) {
    return new TextResponse(content);
  }

  /**
   * 创建图片消息响应
   */
  public static ImageResponse createImageResponse(String mediaId) {
    return new ImageResponse(mediaId);
  }

  /**
   * 创建语音消息响应
   */
  public static VoiceResponse createVoiceResponse(String mediaId) {
    return new VoiceResponse(mediaId);
  }

  /**
   * 创建视频消息响应
   */
  public static VideoResponse createVideoResponse(String mediaId, String title, String description) {
    return new VideoResponse(mediaId, title, description);
  }

  /**
   * 创建文件消息响应
   */
  public static FileResponse createFileResponse(String mediaId) {
    return new FileResponse(mediaId);
  }

  /**
   * 文本消息响应
   */
  @Getter
  @Setter
  @ToString
  public static class TextResponse extends WeWorkMessageResponse {
    private TextContent text;

    public TextResponse(String content) {
      this.msgType = "text";
      this.text = new TextContent(content);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class TextContent {
      private String content;
    }
  }

  /**
   * 图片消息响应
   */
  @Getter
  @Setter
  @ToString
  public static class ImageResponse extends WeWorkMessageResponse {
    private ImageContent image;

    public ImageResponse(String mediaId) {
      this.msgType = "image";
      this.image = new ImageContent(mediaId);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class ImageContent {
      private String mediaId;
    }
  }

  /**
   * 语音消息响应
   */
  @Getter
  @Setter
  @ToString
  public static class VoiceResponse extends WeWorkMessageResponse {
    private VoiceContent voice;

    public VoiceResponse(String mediaId) {
      this.msgType = "voice";
      this.voice = new VoiceContent(mediaId);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class VoiceContent {
      private String mediaId;
    }
  }

  /**
   * 视频消息响应
   */
  @Getter
  @Setter
  @ToString
  public static class VideoResponse extends WeWorkMessageResponse {
    private VideoContent video;

    public VideoResponse(String mediaId, String title, String description) {
      this.msgType = "video";
      this.video = new VideoContent(mediaId, title, description);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class VideoContent {
      private String mediaId;
      private String title;
      private String description;
    }
  }

  /**
   * 文件消息响应
   */
  @Getter
  @Setter
  @ToString
  public static class FileResponse extends WeWorkMessageResponse {
    private FileContent file;

    public FileResponse(String mediaId) {
      this.msgType = "file";
      this.file = new FileContent(mediaId);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class FileContent {
      private String mediaId;
    }
  }
}
