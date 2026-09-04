package com.iwhalecloud.bote.service.reply.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.cache.TtsVoiceIdCache;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.reply.TtsRequest;
import com.iwhalecloud.bote.service.reply.IWordToAudioService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 语音合成服务实现
 *
 * @author qian.sisheng
 * @since 2025-11-21
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class WordToAudioServiceImpl implements IWordToAudioService {
  private static final Logger logger = LoggerFactory.getLogger(WordToAudioServiceImpl.class);
  /** 支持的音色名称列表。每个音色对应 src/main/resources/wav/ 目录下的一个音频文件 */
  public static final List<String> SUPPORTED_AUDIO_NAMES = List.of("man", "sweetGirl");
  /** 默认音色名称 */
  private static final String DEFAULT_AUDIO_NAME = "man";

  /** 默认语音播放速度 */
  private static final String DEFAULT_SPEED_FACTOR = "1.0";
  /** 音色文件的语音内容 */
  private static final String AUDIO_TEXT = "全天下所有好东西都该属于我，包括你在内";

  /** 音色文件缓存 */
  private final TtsVoiceIdCache ttsVoiceIdCache;
  /** 文件存储服务 */
  private final IFileStoreService fileStoreService;

  @Override
  public Long ttsAndUpload(TtsRequest request) {
    // 需要转为语音的文本
    String text = request.tts();
    Assert.hasLength(text, "文本不能为空");
    AtomicReference<Long> fileInfoHolder = new AtomicReference<>();
    generateAudioWithRetry(request.audio(), request.speedFactor(), text, (contentType, contentLength, inputStream) -> {
      UploadConfigVO uploadConfig = new UploadConfigVO();
      uploadConfig.setOriginalFileName("tts_" + System.currentTimeMillis() + ".wav");
      uploadConfig.setDescription("TTS 文件");
      uploadConfig.setFileSize(contentLength);
      uploadConfig.setFileType("wav");
      uploadConfig.setIsPicture(false);
      FileInfoVO fileInfo = fileStoreService.uploadFile(inputStream, uploadConfig);
      fileInfoHolder.set(fileInfo.getFileId());
    });
    return fileInfoHolder.get();
  }

  @Override
  public void tts(TtsRequest request, HttpServletResponse response) {
    // 需要转为语音的文本
    String text = request.tts();
    Assert.hasLength(text, "文本不能为空");
    generateAudioWithRetry(request.audio(), request.speedFactor(), text, (contentType, contentLength, inputStream) -> {
      if (contentType != null) {
        response.setContentType(contentType.toString());
      }
      if (contentLength != null) {
        response.setContentLengthLong(contentLength);
      }
      // 直接转发响应流，避免全量加载到内存中
      try {
        IOUtils.copy(inputStream, response.getOutputStream());
      }
      // 忽略客户端断开连接的异常
      catch (AsyncRequestNotUsableException e) {
        logger.warn("Client aborted while streaming audio");
      }
      catch (IOException e) {
        throw new BssException("生成语音失败: " + e.getMessage(), e);
      }
    });
  }

  /**
   * 生成音频，音色文件不存在时自动重试
   *
   * @param audioName 音色名称
   * @param text 文本
   * @param responseHandler 响应处理器
   */
  private void generateAudioWithRetry(@Nullable String audioName, @Nullable String speedFactor,  String text,
                                      TriConsumer<MediaType, Long, InputStream> responseHandler) {
    // 未指定音色，或音色不合法时使用默认音色
    String finalAudioName;
    if (audioName == null || !SUPPORTED_AUDIO_NAMES.contains(audioName)) {
      finalAudioName = DEFAULT_AUDIO_NAME;
    }
    else {
      finalAudioName = audioName;
    }
    if (speedFactor == null) {
      speedFactor = DEFAULT_SPEED_FACTOR;
    }
    else {
      try {
        double value = Double.parseDouble(speedFactor.trim());
        if (value < 0.0 || value > 2.0) {
          speedFactor = DEFAULT_SPEED_FACTOR;
        }
      }
      catch (NumberFormatException e) {
        logger.error("转换异常速率：{}", e.getMessage());
        speedFactor = DEFAULT_SPEED_FACTOR;
      }
    }
    String baseUrl = StringUtils.stripEnd(SystemParameter.WORD_TO_AUDIO_URL.getValueFromDb(), "/");
    Assert.hasLength(baseUrl, "未配置 TTS 服务地址");

    // 获取音色 ID, 不存在时自动上传
    Long voiceId = getVoiceId(baseUrl, finalAudioName, false);

    String url = baseUrl + "/tts";
    try {
      doGenerateAudio(url, text, voiceId, speedFactor, responseHandler);
    }
    catch (BssException e) {
      // 如果音色文件不存在，则重新上传，只重试一次。用于兼容 TTS 服务器清理了数据的场景
      if (!e.getMessage().contains("voice_id 不存在")) {
        throw e;
      }
      logger.debug("Cached voiceId is no long valid, will try reuploading: url={}, audioName={}, voiceId={}", baseUrl, finalAudioName, voiceId);
      Long newVoiceId = getVoiceId(baseUrl, finalAudioName, true);
      doGenerateAudio(url, text, newVoiceId, speedFactor, responseHandler);
    }
  }

  /**
   * 生成音频响应
   */
  private void doGenerateAudio(String url, String text, Long voiceId, String speedFactor,
                               TriConsumer<MediaType, Long, InputStream> responseHandler) {
    Map<String, Object> params = new HashMap<>();
    params.put("text", text);
    params.put("text_lang", "zh");
    params.put("voice_id", voiceId);
    params.put("prompt_lang", "zh");
    params.put("speed_factor", speedFactor);
    RequestCallback requestCallback = HttpUtil.getRestTemplate().httpEntityCallback(params);
    try {
      HttpUtil.getRestTemplate().execute(url, HttpMethod.POST, requestCallback, res -> {
        if (res.getStatusCode().value() != 200) {
          logger.error("Failed to generate audio: url={}, status={}, headers={}", url, res.getStatusCode().value(), res.getHeaders());
          throw new BssException("文字转语音失败: status=" + res.getStatusCode().value());
        }
        MediaType contentType = res.getHeaders().getContentType();
        long contentLength = res.getHeaders().getContentLength();
        try (InputStream inputStream = res.getBody()) {
          responseHandler.accept(contentType, contentLength > 0 ? contentLength : null, inputStream);
        }
        return null;
      });
    }
    catch (HttpStatusCodeException e) {
      String body = readResponseBody(e);
      String msg = extractErrorMsg(e, body);
      logger.error("Failed to generate audio: url={}, status={}, headers={}, body={}", url, e.getStatusCode().value(), e.getResponseHeaders(), body);
      throw new BssException("文字转语音失败: " + msg, e);
    }
  }

  /**
   * 获取音色 ID, 不存在时上传
   */
  private Long getVoiceId(String baseUrl, String audioName, boolean forceUpload) {
    // 强制上传，不检查缓存
    if (forceUpload) {
      // 先删除缓存，确保即使上传失败了也会再使用旧的缓存
      ttsVoiceIdCache.delete(baseUrl, audioName);
      Long voiceId = uploadAudio(baseUrl, audioName);
      ttsVoiceIdCache.put(baseUrl, audioName, voiceId);
      return voiceId;
    }
    Long voiceId = ttsVoiceIdCache.get(baseUrl, audioName);
    if (voiceId == null) {
      voiceId = uploadAudio(baseUrl, audioName);
      ttsVoiceIdCache.put(baseUrl, audioName, voiceId);
    }
    return voiceId;
  }

  /**
   * 上传音色文件
   */
  private Long uploadAudio(String baseUrl, String audioName) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add("file", new ClassPathResource("/wav/" + audioName + ".wav"));
    HttpEntity<?> requestEntity = new HttpEntity<>(params, headers);

    URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/upload_audio")
      .queryParam("prompt_text", AUDIO_TEXT)
      .queryParam("prompt_lang", "zh")
      .encode()
      .build()
      .toUri();
    Map<String, Object> response;
    try {
      response = HttpUtil.getRestTemplate().exchange(uri, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
      }).getBody();
    }
    catch (HttpStatusCodeException e) {
      String body = readResponseBody(e);
      String msg = extractErrorMsg(e, body);
      logger.error("Failed to upload audio: url={}, status={}, headers={}, body={}", baseUrl, e.getStatusCode().value(), e.getResponseHeaders(), body);
      throw new BssException("上传音频失败: " + msg, e);
    }
    catch (Exception e) {
      logger.error("Failed to upload audio: url={}", baseUrl, e);
      throw new BssException("上传音频失败: " + ExpUtil.getMsg(e), e);
    }
    Long voiceId = MapUtils.getLong(response, "voice_id");
    if (voiceId == null) {
      logger.error("Failed to upload audio, no voice_id returned: url={}, body={}", baseUrl, response);
      throw new BssException("上传音频失败: " + (response == null ? "响应为空" : JsonUtil.toJsonString(response)));
    }
    logger.debug("Uploaded audio: url={}, audioName={}, voiceId={}", baseUrl, audioName, voiceId);
    return voiceId;
  }

  /**
   * 读取响应体
   */
  private String readResponseBody(HttpStatusCodeException e) {
    try {
      return StringUtils.defaultString(e.getResponseBodyAsString());
    }
    catch (Exception e1) {
      // 忽略异常
      return "";
    }
  }

  /**
   * 从响应体提取错误信息
   */
  private String extractErrorMsg(HttpStatusCodeException e, String body) {
    try {
      if (StringUtils.isNotEmpty(body) && body.startsWith("{")) {
        JsonNode json = JsonUtil.readTree(body);
        JsonNode detail = json.get("detail");
        if (detail != null) {
          if (detail.isTextual()) {
            return detail.textValue();
          }
          if (detail.isArray()) {
            String msg = detail.path(0).path("msg").asText(null);
            if (StringUtils.isNotEmpty(msg)) {
              return msg;
            }
          }
        }
      }
    }
    catch (Exception e1) {
      // 忽略异常
    }
    return "status=" + e.getStatusCode().value() + ", body=" + body;
  }

  @Override
  public void clearCache() {
    ttsVoiceIdCache.clearCache();
  }

}
