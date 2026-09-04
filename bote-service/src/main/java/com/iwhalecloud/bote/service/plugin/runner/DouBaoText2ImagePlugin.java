package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.PluginContextUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.dto.plugin.PluginExecutionContext;
import com.iwhalecloud.bote.dto.plugin.params.DouBaoText2ImageParams;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 文生图片
 *
 * @author fan.cong
 * @since 2025-08-06
 */
@Component
public class DouBaoText2ImagePlugin extends AbstractPlugin<DouBaoText2ImageParams> {
  private static final String OPENAI_URL = "/api/v3/images/generations";

  // 定义图片类型静态变量jpeg、png、webp、bmp、tiff、gif
  private static final List<String> imageTypes = Arrays.asList("jpeg", "png", "webp", "bmp", "tiff", "gif");

  private final LargeModelManageMapper largeModelManageMapper;

  private final IFileStoreService fileStoreService;

  public DouBaoText2ImagePlugin(IFileStoreService fileStoreService, LargeModelManageMapper largeModelManageMapper) {
    super(DouBaoText2ImageParams.class);
    this.fileStoreService = fileStoreService;
    this.largeModelManageMapper = largeModelManageMapper;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("prompt", "文本提示", AttrDataType.STRING),
      ParameterSpec.newProperty("responseFormat", "响应格式", AttrDataType.STRING),
      ParameterSpec.newProperty("size", "图片大小", AttrDataType.STRING),
      ParameterSpec.newProperty("fileIds", "参考图文件ID列表", AttrDataType.ARRAY),
      ParameterSpec.newProperty("maxImages", "最大图片张数", AttrDataType.INTEGER)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("mimeType", "图片格式", AttrDataType.STRING),
      ParameterSpec.newProperty("message ", "消息", AttrDataType.STRING),
      ParameterSpec.newProperty("imageBase64", "图片base64", AttrDataType.STRING),
      ParameterSpec.newProperty("imageUrl", "图片URL", AttrDataType.STRING),
      ParameterSpec.newProperty("responseFormat", "响应格式", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(DouBaoText2ImageParams params) {
    Assert.notNull(params.getPrompt(), "文本不能为空");
    Assert.notNull(params.getResponseFormat(), "响应格式不能为空");
    Assert.notNull(params.getSize(), "图片大小不能为空");
  }

  @Override
  public Object doRun(DouBaoText2ImageParams pluginParams) {
    Map<String, Object> result = new HashMap<>();
    PluginExecutionContext pluginContext = PluginContextUtil.getContext();
    SimpleLargeModelDTO simpleLargeModelDTO = largeModelManageMapper.selectLargeModelById(pluginContext.getTenantId(), pluginContext.getModelId());
    if (simpleLargeModelDTO == null) {
      throw new BssException("关联模型不存在！");
    }
    Assert.notNull(simpleLargeModelDTO.getAccessKey(), "模型API key不能为空!");
    // 构造请求体
    Map<String, Object> body = new HashMap<>();
    body.put("model", simpleLargeModelDTO.getModelCode());
    body.put("prompt", pluginParams.getPrompt());
    body.put("size", pluginParams.getSize());
    body.put("response_format", pluginParams.getResponseFormat());
    if (CollectionUtils.isNotEmpty(pluginParams.getFileIds())) {
      List<String> imgBase64List = new ArrayList<>();
      for (Long fileId : pluginParams.getFileIds()) {
        imgBase64List.add(handleFileId(fileId));
      }
      body.put("image", imgBase64List);
    }
    if (pluginParams.getMaxImages() != null && pluginParams.getMaxImages() > 0) {
      Map<String, Object> maxImages = new HashMap<>();
      maxImages.put("max_images", pluginParams.getMaxImages());
      body.put("sequential_image_generation_options", maxImages);
    }


    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(simpleLargeModelDTO.getAccessKey());

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<OpenAiImageResponse> response = HttpUtil.getRestTemplate()
        .exchange(simpleLargeModelDTO.getAccessUrl() + OPENAI_URL, HttpMethod.POST, entity, OpenAiImageResponse.class);

      OpenAiImageResponse respBody = response.getBody();
      if (respBody == null || respBody.getData() == null || respBody.getData().isEmpty()) {
        throw new BssException("API未返回图片数据！");
      }

      ImageData imageData = respBody.getData().getFirst();
      result.put("message", "图像生成成功！");
      result.put("mimeType", "jpeg");
      result.put("imageBase64", imageData.getB64_json());
      result.put("imageUrl", imageData.getUrl());
      result.put("responseFormat", pluginParams.getResponseFormat());
      return result;

    }
    catch (Exception e) {
      logger.error("生成图像时出错: {}", e.getMessage(), e);
      throw new BssException("生成图像时出错！" + e.getMessage(), e);
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
      if (fileInfoVO.getFileSize() >= 10 * 1024 * 1024) {
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

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DOUBAO_TEXT_2_IMAGE;
  }

  @Data
  public static class OpenAiImageResponse {
    private List<ImageData> data;
  }

  @Data
  public static class ImageData {
    private String url;

    private String b64_json;
  }
}
