package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.ImageToBase64PluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreProcessor;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.util.FileStoreUtils;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 图片转base64插件
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class ImageToBase64Plugin extends AbstractPlugin<ImageToBase64PluginParams> {
  private static final Logger logger = LoggerFactory.getLogger(ImageToBase64Plugin.class);

  private final IFileStoreService fileStoreService;

  public ImageToBase64Plugin(IFileStoreService fileStoreService) {
    super(ImageToBase64PluginParams.class);
    this.fileStoreService = fileStoreService;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_IMAGE_TO_BASE_64;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> properties = new ArrayList<>();
    properties.add(ParameterSpec.newProperty("fileId", "文件ID", AttrDataType.INTEGER));
    properties.add(ParameterSpec.newProperty("fileUrl", "文件地址", AttrDataType.STRING));
    return ParameterSpec.newRoot(properties);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("text", "base64文本", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(ImageToBase64PluginParams params) {
    if (params.getFileId() == null && StringUtils.isEmpty(params.getFileUrl())) {
      throw new BssException("fileId和fileUrl不能同时为空");
    }
   }

  @Override
  public Object doRun(ImageToBase64PluginParams pluginParams) {
    Map<String, Object> params = new HashMap<>();
    String text = null;
    if (pluginParams.getFileId() != null) {
      text = fileStoreService.getImageData(pluginParams.getFileId());
    }
    if (StringUtils.isNotEmpty(pluginParams.getFileUrl())) {
      IFileStoreProcessor fileStoreProcessor = FileStoreUtils.getDefaultProcessor();
      try {
        byte[] bytes = fileStoreProcessor.download(pluginParams.getFileUrl());
        text = bytes == null || bytes.length == 0 ? null : "data:image;base64," + Base64.getEncoder().encodeToString(bytes);
      }
      catch (Exception e) {
        logger.error("根据文件路径获取文件异常, message={}", e.getMessage(), e);
        throw new BssException("根据文件路径获取文件异常: " + e.getMessage(), e);
      }
    }
    params.put("text", text);
    return params;
  }
}
