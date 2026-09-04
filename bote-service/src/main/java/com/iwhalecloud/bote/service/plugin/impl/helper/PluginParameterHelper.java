package com.iwhalecloud.bote.service.plugin.impl.helper;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.io.FileInfoResource;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.PluginExecuteParams;
import com.iwhalecloud.bote.dto.plugin.response.PluginToolSpec;
import com.iwhalecloud.bote.llm.client.util.LlmTraceUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 执行工具入参格式转化辅助工具
 *
 * @author chen.linfa
 * @since 2026-01-06
 */
public final class PluginParameterHelper {
  private PluginParameterHelper() {
  }

  // @formatter:off
  /** 博特平台的大模型会话补全接口地址 */
  private static final String LLM_API_URL = StringUtils.stripEnd(BaseSystemParameter.BOTE_API_URL.getValueFromEnv(), "/") + "/bote/modelProxy/v1/chat/completions";
  /** 博特平台的文件上传接口地址 */
  private static final String UPLOAD_API_URL = StringUtils.stripEnd(BaseSystemParameter.BOTE_API_URL.getValueFromEnv(), "/") + "/bote/file/uploadSingleFile";
  /** 博特平台的文件下载接口地址 */
  private static final String DOWNLOAD_API_URL = StringUtils.stripEnd(BaseSystemParameter.BOTE_API_URL.getValueFromEnv(), "/") + "/bote/file/download";
  // @formatter:on

  /**
   * tool call 模式下，特殊参数格式内容，进行值处理
   */
  @SuppressWarnings("unchecked")
  public static void resolveParameterValue(PluginToolSpec tool, PluginExecuteParams params) {
    if (tool.getInput() == null) {
      return;
    }
    Map<String, Object> body = (Map<String, Object>) MapUtils.getMap(params.getParams(), "body");
    if (MapUtils.isEmpty(body)) {
      body = new HashMap<>();
      params.getParams().put("body", body);
    }
    for (ParameterSpec spec : tool.getInput().getBody()) {
      if ("llm_model".equals(spec.getFormat())) {
        // 使用博特开放的模型对话 API
        Long tenantId = params.getTenantId();
        Long modelId = params.getModelId();
        if (ModelConsts.DEFAULT_MODEL.equals(modelId)) {
          modelId = SpringUtil.getBean(TenantSettingInfoCache.class).getModelId(tenantId);
        }
        String traceId = LlmTraceUtil.getTraceId();
        Map<String, Object> llmModel = new HashMap<>();
        llmModel.put("url", traceId != null ? LLM_API_URL + "?traceId=" + traceId : LLM_API_URL);
        llmModel.put("model", tenantId + "/" + modelId);
        llmModel.put("apiKey", BaseSystemParameter.DOCCHAIN_LLM_API_KEY.getValueFromDb());
        body.put(spec.getName(), llmModel);
      }
      else if ("file_info".equals(spec.getFormat())) {
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("uploadUrl", UPLOAD_API_URL);
        fileInfo.put("downloadUrl", DOWNLOAD_API_URL);
        fileInfo.put("apiKey", BaseSystemParameter.DOCCHAIN_LLM_API_KEY.getValueFromDb());
        body.put(spec.getName(), fileInfo);
      }
      else if ("file".equals(spec.getType().getCode()) && body.get(spec.getName()) != null) {
        body.put(spec.getName(), resolveFileByFileId(body.get(spec.getName())));
      }
    }
  }

  /**
   * 工具调用模式下，特殊参数格式内容，进行值处理
   */
  public static Object resolveParameterValue(ParameterSpec spec, Long tenantId) {
    if ("llm_model".equals(spec.getFormat()) && StringUtils.isNotEmpty(spec.getValue())) {
      // 使用博特开放的模型对话 API
      String modelId = spec.getValue();
      if (ModelConsts.DEFAULT_MODEL.toString().equals(modelId)) {
        modelId = SpringUtil.getBean(TenantSettingInfoCache.class).getModelId(tenantId).toString();
      }
      String traceId = LlmTraceUtil.getTraceId();
      Map<String, Object> llmModel = new HashMap<>();
      llmModel.put("url", traceId != null ? LLM_API_URL + "?traceId=" + traceId : LLM_API_URL);
      llmModel.put("model", tenantId + "/" + modelId);
      llmModel.put("apiKey", BaseSystemParameter.DOCCHAIN_LLM_API_KEY.getValueFromDb());
      return llmModel;
    }
    else if ("file_info".equals(spec.getFormat())) {
      Map<String, Object> fileInfo = new HashMap<>();
      fileInfo.put("uploadUrl", UPLOAD_API_URL);
      fileInfo.put("downloadUrl", DOWNLOAD_API_URL);
      fileInfo.put("apiKey", BaseSystemParameter.DOCCHAIN_LLM_API_KEY.getValueFromDb());
      return fileInfo;
    }
    return spec.getValue();
  }

  @SuppressWarnings("unchecked")
  public static MultiValueMap<String, Object> buildMultipartBody(Object body) {
    Assert.isTrue(body instanceof Map, "表单数据必须是 Map");
    MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
    Map<String, Object> map = (Map<String, Object>) body;
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof List) {
        multipartBody.addAll(key, (List<Object>) value);
      }
      else {
        multipartBody.add(key, value);
      }
    }
    return multipartBody;
  }

  @SuppressWarnings("unchecked")
  public static void buildMultipartBody(PluginToolSpec tool, PluginExecuteParams params) {
    if (tool.getInput() == null || CollectionUtils.isEmpty(tool.getInput().getBody())) {
      return;
    }
    Map<String, Object> body = (Map<String, Object>) MapUtils.getMap(params.getParams(), "body");
    if (MapUtils.isEmpty(body)) {
      return;
    }
    MultiValueMap<String, Object> newBody = new LinkedMultiValueMap<>();
    for (ParameterSpec spec : tool.getInput().getBody()) {
      Object value = body.get(spec.getName());
      if (value == null) {
        continue;
      }
      handleParameterValue(spec, value, newBody);
    }
    params.getParams().put("body", newBody);
  }

  /**
   * 处理参数值
   */
  private static void handleParameterValue(ParameterSpec spec, Object value, MultiValueMap<String, Object> newBody) {
    if (AttrDataType.FILE == spec.getType() && value instanceof FileInfoResource) {
      convertFile(spec, (FileInfoResource) value, newBody);
    }
    else if (value instanceof Collection) {
      handleCollectionValue(spec, (Collection<?>) value, newBody);
    }
    else if (value instanceof Map) {
      newBody.add(spec.getName(), JsonUtil.toJsonString(value));
    }
    else {
      newBody.add(spec.getName(), String.valueOf(value));
    }
  }

  /**
   * 处理集合类型的参数值
   */
  private static void handleCollectionValue(ParameterSpec spec, Collection<?> collection, MultiValueMap<String, Object> newBody) {
    if (spec.getArrayElement() != null && spec.getArrayElement().getType() == AttrDataType.FILE) {
      for (Object item : collection) {
        if (item instanceof FileInfoResource) {
          convertFile(spec, (FileInfoResource) item, newBody);
        }
      }
      return;
    }
    // 将集合中的所有元素转换为字符串后添加
    for (Object item : collection) {
      newBody.add(spec.getName(), String.valueOf(item));
    }
  }

  private static void convertFile(ParameterSpec spec, FileInfoResource resource, MultiValueMap<String, Object> newBody) {
    try {
      // 将 FileInfoResource 转换为 FileSystemResource，用于 multipart/form-data 上传
      Path tmpDir = Files.createTempDirectory("plugin-upload-");
      String fileName = resource.getFilename();
      if (StringUtils.isBlank(fileName)) {
        fileName = "file";
      }
      File tempFile = tmpDir.resolve(fileName).toFile();
      // 将输入流内容写入临时文件
      try (InputStream inputStream = resource.getInputStream()) {
        Files.copy(inputStream, tempFile.toPath());
      }
      newBody.add(spec.getName(), new FileSystemResource(tempFile));
    }
    catch (Exception e) {
      throw new BssException("转换文件资源失败: " + ExpUtil.getMsg(e), e);
    }
  }

  @SuppressWarnings("unchecked")
  private static FileInfoResource resolveFileByFileId(Object value) {
    Long fileId = null;
    if (value instanceof Long) {
      fileId = (Long) value;
    }
    else if (value instanceof String) {
      fileId = Long.valueOf(value.toString());
    }
    else if (value instanceof Collection) {
      Collection<?> collection = (Collection<?>) value;
      // 获取第一个元素并递归处理
      fileId = Long.valueOf(collection.iterator().next().toString());
    }
    if (fileId != null) {
      FileInfoVO fileInfo = SpringUtil.getBean(IFileStoreService.class).getFileInfoById(fileId);
      if (fileInfo != null) {
        return new FileInfoResource(fileInfo);
      }
    }
    return null;
  }
}
