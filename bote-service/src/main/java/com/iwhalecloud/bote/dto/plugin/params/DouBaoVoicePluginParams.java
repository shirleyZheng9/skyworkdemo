package com.iwhalecloud.bote.dto.plugin.params;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 豆包语音合成插件参数
 *
 * @see <a href="https://www.volcengine.com/docs/6561/1257584">火山引擎大模型语音合成API</a>
 * @author qian.sisheng
 * @since 2025-10-29
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class DouBaoVoicePluginParams extends AbstractPluginParams {
  /** 应用相关配置 */
  private App app;
  /** 用户相关配置 */
  private User user;
  /** 音频相关配置 */
  private Audio audio;
  /** 请求相关配置 */
  private Request request;

  public DouBaoVoicePluginParams() {
    super(PluginConsts.PLUGIN_CODE_DOUBAO_VOICE);
  }

  @Getter
  @Setter
  @ToString
  @JsonInclude(Include.NON_NULL)
  public static class App {
    /** 应用标识 */
    private String appid;
    /** 应用令牌 */
    private String token;
    /** 集群标识 */
    private String cluster;
  }

  @Getter
  @Setter
  @ToString
  @JsonInclude(Include.NON_NULL)
  public static class User {
    /** 用户标识 */
    private String uid;
  }

  @Getter
  @Setter
  @ToString
  @JsonInclude(Include.NON_NULL)
  public static class Audio {
    /** 音色类型 */
    private String voiceType;
    /** 音色情感 */
    private String emotion;
    /** 是否启用音色情感 */
    private Boolean enableEmotion;
    /** 情绪值设置 */
    private Double emotionScale;
    /** 音频编码格式 wav/pcm/ogg_opus/mp3，默认为 pcm 注意：wav 不支持流式 */
    private String encoding;
    /** 语速 默认1.0 */
    private Double speedRatio;
    /** 音频采样率 */
    private Integer rate;
    /** 音频比特率 */
    private Integer bitrate;
    /** 明确语种，仅读指定语种的文本 */
    private String explicitLanguage;
    /** 参考语种，给模型提供参考的语种 */
    private String contextLanguage;
    /** 音量调节 [0.5,2]  默认为1，通常保留一位小数即可。0.5代表原音量0.5倍，2代表原音量2倍 */
    private Double loudnessRatio;
  }

  @Getter
  @Setter
  @ToString
  @JsonInclude(Include.NON_NULL)
  public static class Request {
    /** 请求标识  需要保证每次调用传入值唯一，建议使用 UUID */
    private String reqid;
    /** 合成语音的文本 */
    private String text;
    /** 模型版本 */
    private String model;
    /** 文本类型，使用 ssml 时需要指定，值为"ssml"*/
    private String textTpe;
    /** 句尾静音 设置该参数可在句尾增加静音时长，范围0~30000ms。启用该参数，必须在request下首先设置enable_trailing_silence_audio = true*/
    private Double silenceDuration;
    /** 时间戳相关，传入1时表示启用，将返回TN后文本的时间戳 */
    private String withTimestamp;
    /** 操作 query（非流式，http 只能 query） / submit（流式） */
    private String operation;
    /** 额外参数 */
    private String extraParam;
  }

  @Getter
  @Setter
  @ToString
  public static class CacheConfig {
    /** 缓存相关参数 和use_cache参数一起使用，需要开启缓存时传1 */
    private Integer textType;
    /** 和text_type参数一起使用，需要开启缓存时传true */
    private Boolean useCache;
  }
}
