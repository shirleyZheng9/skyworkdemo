package com.iwhalecloud.bote.service.plugin.impl;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.service.plugin.IPluginRemoteService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件远程服务实现
 *
 * @author auto
 * @since 2025-04-01
 */
@Service
@RequiredArgsConstructor
public class PluginRemoteServiceImpl implements IPluginRemoteService {


  private static final String API_NL2SQL_BUILD_SCHEMA_VECTOR = "nl2sql/openApi/buildSchemaVector";
  private static final String API_NL2SQL_CREATE_SQL_BY_QUESTION = "nl2sql/openApi/createSqlByQuestion";
  private static final String API_MEDIA_PLUGIN_VIDEO_ANALYSIS = "media-plugin/openApi/analyzeVideo";
  private static final String API_MEDIA_PLUGIN_AUDIO_ANALYSIS = "media-plugin/openApi/analyzeAudio";
  private static final String API_MEDIA_PLUGIN_VIDEO_TO_AUDIO = "media-plugin/openApi/videoToAudio";
  private static final String API_MEDIA_PLUGIN_DOU_YIN_VIDEO_ANALYSIS = "media-plugin/openApi/analyzeDouYinVideo";
  private static final String API_MEDIA_PLUGIN_PLATFORM_VIDEO_DOWNLOAD = "media-plugin/openApi/downloadPlatformVideo";
  /** 博特网关地址 */
  private static final String  BOTE_API_URL = SystemParameter.BOTE_API_URL.getValueFromEnv();

  @Override
  public ResultVO<String> buildSchemaVector(Long tenantId, Long dataSourceInstId) {
    Map<String, Object> params = new HashMap<>();
    params.put("tenantId", tenantId);
    params.put("dataSourceInstId", dataSourceInstId);
    String url = buildUrl(API_NL2SQL_BUILD_SCHEMA_VECTOR);
    return HttpUtil.post(url, params, new ParameterizedTypeReference<ResultVO<String>>() {
    });
  }

  @Override
  public ResultVO<String> createSqlByQuestion(Long tenantId, Long dataSourceId, String question, String evnCode) {
    Map<String, Object> params = new HashMap<>();
    params.put("tenantId", tenantId);
    params.put("dataSourceId", dataSourceId);
    params.put("envCode", evnCode);
    params.put("question", question);
    String url = buildUrl(API_NL2SQL_CREATE_SQL_BY_QUESTION);
    return HttpUtil.post(url, params, new ParameterizedTypeReference<ResultVO<String>>() {
    });
  }

  @Override
  public ResultVO<Object> videoAnalysis(Long fileId, String analysisMode) {
    Map<String, Object> params = new HashMap<>();
    params.put("fileId", fileId);
    params.put("analysisMode", analysisMode);
    String url = buildUrl(API_MEDIA_PLUGIN_VIDEO_ANALYSIS);
    return HttpUtil.post(url, params, new ParameterizedTypeReference<ResultVO<Object>>() {
    });
  }

  @Override
  public ResultVO<Object> audioAnalysis(Long fileId) {
    Map<String, Object> params = new HashMap<>();
    params.put("fileId", fileId);
    String url = buildUrl(API_MEDIA_PLUGIN_AUDIO_ANALYSIS);
    return HttpUtil.post(url, params, new ParameterizedTypeReference<ResultVO<Object>>() {
    });
  }

  @Override
  public ResultVO<Object> videoToAudio(Long fileId, String audioFormat) {
    Map<String, Object> params = new HashMap<>();
    params.put("fileId", fileId);
    params.put("audioFormat", audioFormat);
    String url = buildUrl(API_MEDIA_PLUGIN_VIDEO_TO_AUDIO);
    return HttpUtil.post(url, params, new ParameterizedTypeReference<ResultVO<Object>>() {
    });
  }

  @Override
  public ResultVO<Object> douYinVideoAnalysis(String url, String analysisMode) {
    Map<String, Object> params = new HashMap<>();
    params.put("url", url);
    params.put("analysisMode", analysisMode);
    return HttpUtil.post(buildUrl(API_MEDIA_PLUGIN_DOU_YIN_VIDEO_ANALYSIS), params, new ParameterizedTypeReference<ResultVO<Object>>() {
    });
  }

  @Override
  public ResultVO<Object> platformVideoDownload(String url) {
    Map<String, Object> params = new HashMap<>();
    params.put("url", url);
    return HttpUtil.post(buildUrl(API_MEDIA_PLUGIN_PLATFORM_VIDEO_DOWNLOAD), params, new ParameterizedTypeReference<ResultVO<Object>>() {
    });
  }

  /**
   * 构建URL
   */
  private String buildUrl(String apiPath) {
    if (StringUtils.isEmpty(BOTE_API_URL)) {
      throw new RuntimeException("缺少BOTE_API_URL配置，请先配置BOTE_API_URL");
    }
    return BOTE_API_URL.endsWith("/") ? BOTE_API_URL + apiPath : BOTE_API_URL + "/" + apiPath;
  }

}
