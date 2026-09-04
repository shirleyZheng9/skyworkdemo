package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.ImageConvertPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 图片格式转换插件
 * 支持格式：PNG, JPEG, JPG, BMP, GIF
 *
 * @author zhao.xu104
 * @since 2025-11-20
 */
@Component
public class ImageConvertPlugin extends AbstractFilePlugin<ImageConvertPluginParams> {
  /** 支持的图片格式 */
  private static final List<String> SUPPORTED_FORMATS = List.of("png", "jpg", "jpeg", "bmp", "gif");

  public ImageConvertPlugin(IFileStoreService fileStoreService) {
    super(ImageConvertPluginParams.class, fileStoreService);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_IMAGE_CONVERT;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("fileId", "文件ID（与fileUrl二选一）", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("fileUrl", "文件地址（与fileId二选一）", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("targetFormat", "目标格式（png/jpg/jpeg/bmp/gif）", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("fileId", "转换后的文件ID", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("fileName", "转换后的文件名", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fileSize", "文件大小（字节）", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("format", "转换后的格式", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(ImageConvertPluginParams params) {
    validateFileParams(params);
    Assert.notNull(params.getTargetFormat(), "目标格式不能为空");
    String format = params.getTargetFormat().toLowerCase(Locale.ROOT);
    if (!SUPPORTED_FORMATS.contains(format)) {
      throw new BssException("不支持的目标格式: " + params.getTargetFormat() +
        "，支持的格式: " + String.join(", ", SUPPORTED_FORMATS));
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public Object doRun(ImageConvertPluginParams pluginParams) {
    try {
      // 下载源图片
      byte[] sourceImageBytes = downloadFile(pluginParams);
      // 读取图片
      BufferedImage image = readImage(sourceImageBytes);
      // 转换格式
      String targetFormat = pluginParams.getTargetFormat().toLowerCase(Locale.ROOT);
      byte[] convertedImageBytes = convertImage(image, targetFormat);
      // 生成新文件名
      String originalFileName = getOriginalFileName(pluginParams, "image.jpg");
      String baseName = FilenameUtils.getBaseName(originalFileName);
      if (StringUtils.isBlank(baseName)) {
        baseName = "converted_image";
      }
      String newFileName = baseName + "." + targetFormat;
      // 上传转换后的图片
      FileInfoVO convertedFileInfo = uploadFile(convertedImageBytes, newFileName, targetFormat);
      logger.info("图片格式转换成功, sourceFileId={}, targetFormat={}, newFileId={}",
        pluginParams.getFileId() != null ? pluginParams.getFileId() : pluginParams.getFileUrl(), 
        targetFormat, convertedFileInfo.getFileId());
      Map<String, Object> result = new HashMap<>();
      result.put("fileId", convertedFileInfo.getFileId());
      result.put("fileName", newFileName);
      result.put("fileSize", convertedImageBytes.length);
      result.put("format", targetFormat);
      return result;
    }
    catch (IOException e) {
      logger.error("图片格式转换失败: {}", e.getMessage(), e);
      throw new BssException("图片格式转换失败: " + e.getMessage(), e);
    }
  }

  /**
   * 读取图片
   */
  private BufferedImage readImage(byte[] imageBytes) throws IOException {
    try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
      BufferedImage image = ImageIO.read(inputStream);
      if (image == null) {
        throw new BssException("无法读取图片，可能不是有效的图片文件");
      }
      return image;
    }
  }

  /**
   * 转换图片格式
   *
   * @param image 源图片
   * @param targetFormat 目标格式
   * @return 转换后的图片字节数组
   */
  private byte[] convertImage(BufferedImage image, String targetFormat) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    boolean written = ImageIO.write(image, targetFormat, outputStream);
    if (!written) {
      throw new BssException("无法写入图片格式: " + targetFormat);
    }
    return outputStream.toByteArray();
  }
}
