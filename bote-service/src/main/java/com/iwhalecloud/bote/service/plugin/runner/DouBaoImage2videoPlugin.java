package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.PluginContextUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.dto.plugin.PluginExecutionContext;
import com.iwhalecloud.bote.dto.plugin.params.DouBaoImage2videoParams;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreProcessor;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.util.FileStoreUtils;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import static java.lang.Thread.sleep;

/**
 * 图生视频
 *
 * @author fan.cong
 * @since 2025-08-06
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class DouBaoImage2videoPlugin extends AbstractPlugin<DouBaoImage2videoParams> {
  // 定义图片类型静态变量jpeg、png、webp、bmp、tiff、gif
  private static final List<String> imageTypes = Arrays.asList("jpeg", "png", "webp", "bmp", "tiff", "gif");

  private final IFileStoreService fileStoreService;
  private final LargeModelManageMapper largeModelManageMapper;

  public DouBaoImage2videoPlugin(IFileStoreService fileStoreService, LargeModelManageMapper largeModelManageMapper) {
    super(DouBaoImage2videoParams.class);
    this.fileStoreService = fileStoreService;
    this.largeModelManageMapper = largeModelManageMapper;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("fileId", "图片", AttrDataType.INTEGER),
      ParameterSpec.newProperty("fileUrl", "图片url", AttrDataType.STRING),
      ParameterSpec.newProperty("prompt", "提示语", AttrDataType.STRING),
      ParameterSpec.newProperty("duration", "时长", AttrDataType.INTEGER),
      ParameterSpec.newProperty("resolution", "分辨率", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING),
      ParameterSpec.newProperty("videoUrl", "视频url", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(DouBaoImage2videoParams params) {
    if (params.getFileId() == null && StringUtils.isEmpty(params.getFileUrl())) {
      throw new BssException("图片id或url不能都为空");
    }
    Assert.notNull(params.getPrompt(), "提示语不能为空");
    Assert.notNull(params.getDuration(), "时长不能为空");
    Assert.notNull(params.getResolution(), "分辨率不能为空");
  }

  @Override
  public Object doRun(DouBaoImage2videoParams pluginParams) {
    Map<String, String> result = new HashMap<>();
    PluginExecutionContext pluginContext = PluginContextUtil.getContext();
    SimpleLargeModelDTO simpleLargeModelDTO = largeModelManageMapper.selectLargeModelById(pluginContext.getTenantId(), pluginContext.getModelId());

    if (simpleLargeModelDTO == null) {
      throw new BssException("模型不存在！");
    }
    Assert.notNull(simpleLargeModelDTO.getAccessKey(), "模型API key不能为空!");
    try {
      String base64Image = fetchImageBytes(pluginParams);
      String taskId = createGenerationTask(pluginParams, base64Image, simpleLargeModelDTO);
      String videoUrl = pollTaskStatus(taskId, simpleLargeModelDTO);
      result.put("videoUrl", videoUrl);
      result.put("message", "视频生成成功");
      return result;
    }
    catch (Exception e) {
      logger.error("生成视频异常:{}", e.getMessage(), e);
      throw new BssException("生成视频异常：" + e.getMessage(), e);
    }
  }

  private String createGenerationTask(DouBaoImage2videoParams pluginParams, String base64Image, SimpleLargeModelDTO model) {
    Map<String, Object> payload = getStringObjectMap(pluginParams, base64Image, model.getModelCode());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(model.getAccessKey());

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
    String url = model.getAccessUrl() + "/api/v3/contents/generations/tasks";
    ResponseEntity<String> resp = HttpUtil.getRestTemplate().postForEntity(url, entity, String.class);

    if (!resp.getStatusCode().is2xxSuccessful()) {
      throw new BssException("请求生成视频失败");
    }

    String body = resp.getBody();
    Assert.hasLength(body, "请求生成视频失败，响应为空");
    JsonNode root = JsonUtil.readTree(body);
    String taskId = root.path("id").asText(null);
    if (StringUtils.isEmpty(taskId)) {
      throw new BssException("未获取到任务ID");
    }
    logger.info("任务已创建，ID: {}", taskId);
    return taskId;
  }

  private String pollTaskStatus(String taskId, SimpleLargeModelDTO model) throws Exception {
    String statusUrl = model.getAccessUrl() + "/api/v3/contents/generations/tasks/" + taskId;
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(model.getAccessKey());
    int retry = 0;
    int maxRetry = 60;
    while (retry < maxRetry) {
      sleep(5000);
      ResponseEntity<String> statusResp = HttpUtil.getRestTemplate()
        .exchange(statusUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
      String body = statusResp.getBody();
      Assert.hasLength(body, "查询生成视频任务状态失败，响应为空");
      JsonNode statusNode = JsonUtil.readTree(body);
      String status = statusNode.path("status").asText();
      if ("succeeded".equals(status)) {
        return statusNode.path("content").path("video_url").asText(null);
      }
      if ("failed".equals(status)) {
        throw new BssException("生成失败: " + statusNode.path("error").path("message").asText(""));
      }
      if ("canceled".equals(status)) {
        throw new BssException("任务被取消");
      }
      logger.info("任务{},生成中，已等待 {} 秒...", taskId, (retry + 1) * 5);
      retry++;
    }
    throw new BssException("生成超时");
  }

  private static Map<String, Object> getStringObjectMap(DouBaoImage2videoParams pluginParams, String base64Image,
    String modelCode) {

    // 构造请求体
    String prompt = pluginParams.getPrompt();
    prompt += " --rs " + pluginParams.getResolution();
    prompt += " --dur " + pluginParams.getDuration();

    // 构造 content 列表
    List<Map<String, Object>> content = new ArrayList<>();

    // 添加文本部分
    Map<String, Object> textMap = new HashMap<>();
    textMap.put("type", "text");
    textMap.put("text", prompt);
    content.add(textMap);

    // 添加图片部分
    Map<String, Object> imageUrlMap = new HashMap<>();
    imageUrlMap.put("url", base64Image);

    Map<String, Object> imageMap = new HashMap<>();
    imageMap.put("type", "image_url");
    imageMap.put("image_url", imageUrlMap);
    content.add(imageMap);

    // 构造 payload
    Map<String, Object> payload = new HashMap<>();
    payload.put("model", modelCode);
    payload.put("content", content);
    return payload;
  }

  private String fetchImageBytes(DouBaoImage2videoParams pluginParams) {
    if (pluginParams.getFileId() != null) {
      return handleFileId(pluginParams.getFileId());
    }
    else {
      return handleFileUrl(pluginParams.getFileUrl());
    }

  }

  private String handleFileId(Long fileId) {
    try {
      FileInfoVO fileInfoVO = fileStoreService.getFileInfoById(fileId);
      if (fileInfoVO == null) {
        throw new BssException("文件不存在！");
      }
      String fileType = getFileType(fileInfoVO.getFileName());
      if (StringUtils.isEmpty(fileType) || !imageTypes.contains(fileType)) {
        throw new BssException("文件类型错误！");
      }
      if (fileInfoVO.getFileSize() >= 30 * 1024 * 1024) {
        throw new BssException("文件过大！");
      }
      String[] encodedResult = encodeImage(fileStoreService.downloadFile(fileId));
      String base64Image = encodedResult[0];
      return "data:image/" + fileType + ";base64," + base64Image;
    }
    catch (Exception e) {
      throw new BssException("获取图片异常, message={}", e.getMessage(), e);
    }
  }

  private String handleFileUrl(String fileUrl) {
    String fileType = getFileType(fileUrl);
    if (StringUtils.isEmpty(fileType) || !imageTypes.contains(fileType)) {
      throw new BssException("文件类型错误！");
    }
    IFileStoreProcessor fileStoreProcessor = FileStoreUtils.getDefaultProcessor();
    try {
      byte[] bytes = fileStoreProcessor.download(fileUrl);
      if (bytes == null || bytes.length == 0 || bytes.length > 30 * 1024 * 1024) {
        throw new BssException("文件过大！");
      }
      return "data:image/" + fileType + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }
    catch (Exception e) {
      logger.error("根据文件路径获取文件异常, message={}", e.getMessage(), e);
      throw new BssException("根据文件路径获取文件异常", e);
    }
  }

  // 编码图片为base64
  private String[] encodeImage(byte[] fileData) throws Exception {
    try {
      double imageSize = fileData.length / 1024.0;
      String encoded = Base64.getEncoder().encodeToString(fileData);
      double encodedSize = encoded.length() / 1024.0;
      String debugInfo = String.format("图片编码完成: 原始大小=%.2fKB, 编码后大小=%.2fKB", imageSize, encodedSize);
      return new String[] {encoded, debugInfo};
    }
    catch (Exception e) {
      throw new Exception("图片编码失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取文件类型
   */
  private String getFileType(String fileName) {
    // 先判空
    if (StringUtils.isEmpty(fileName)) {
      throw new BssException("获取到的文件名为空");
    }
    return fileName.substring(fileName.lastIndexOf(".") + 1);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DOUBAO_IMAGE_2_VIDEO;
  }
}
