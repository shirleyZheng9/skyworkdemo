package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.ImageCompressPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Component;

/**
 * 图片压缩插件
 * 对图片进行压缩并返回压缩后的base64数据
 *
 * @author zhao.xu104
 * @since 2025-11-20
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class ImageCompressPlugin extends AbstractFilePlugin<ImageCompressPluginParams> {
  public ImageCompressPlugin(IFileStoreService fileStoreService) {
    super(ImageCompressPluginParams.class, fileStoreService);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_IMAGE_COMPRESS;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("fileId", "文件ID", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("fileUrl", "文件地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("quality", "压缩质量（0.0-1.0，可选，默认0.8）", AttrDataType.NUMBER));
    children.add(ParameterSpec.newProperty("maxWidth", "最大宽度（可选，按比例缩放）", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("maxHeight", "最大高度（可选，按比例缩放）", AttrDataType.INTEGER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("base64", "压缩后的base64数据", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("originalSize", "原始文件大小（字节）", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("compressedSize", "压缩后文件大小（字节）", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("compressionRatio", "压缩率（百分比）", AttrDataType.NUMBER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(ImageCompressPluginParams params) {
    validateFileParams(params);
    // 验证质量参数
    if (params.getQuality() != null) {
      float quality = params.getQuality();
      if (quality < 0.0f || quality > 1.0f) {
        throw new BssException("压缩质量参数必须在0.0-1.0之间");
      }
    }
    // 验证尺寸参数
    if (params.getMaxWidth() != null && params.getMaxWidth() <= 0) {
      throw new BssException("最大宽度必须大于0");
    }
    if (params.getMaxHeight() != null && params.getMaxHeight() <= 0) {
      throw new BssException("最大高度必须大于0");
    }
  }

  @Override
  public Object doRun(ImageCompressPluginParams pluginParams) {
    try {
      byte[] originalImageBytes = downloadFile(pluginParams);
      int originalSize = originalImageBytes.length;
      String originalFormat = detectImageFormat(originalImageBytes);
      BufferedImage image = readImage(originalImageBytes);
      image = applyResizeIfNeeded(image, pluginParams);
      float quality = getQuality(pluginParams);
      byte[] compressedBytes = compressImage(image, quality, originalFormat);
      return buildResult(originalSize, compressedBytes, originalFormat);
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      logger.error("图片压缩失败: {}", e.getMessage(), e);
      throw new BssException("图片压缩失败: " + e.getMessage(), e);
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
   * 检测图片格式
   */
  private String detectImageFormat(byte[] imageBytes) {
    try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
      String format = ImageIO.getImageReaders(ImageIO.createImageInputStream(inputStream)).next().getFormatName();
      return format != null ? format.toLowerCase() : "jpeg";
    }
    catch (Exception e) {
      logger.warn("无法检测图片格式，默认使用jpeg: {}", e.getMessage());
      return "jpeg";
    }
  }

  /**
   * 如果需要，应用缩放
   */
  private BufferedImage applyResizeIfNeeded(BufferedImage image, ImageCompressPluginParams params) {
    if (params.getMaxWidth() != null || params.getMaxHeight() != null) {
      return resizeImage(image, params.getMaxWidth(), params.getMaxHeight());
    }
    return image;
  }

  /**
   * 获取压缩质量
   */
  private float getQuality(ImageCompressPluginParams params) {
    return params.getQuality() != null ? params.getQuality() : 0.8f;
  }

  /**
   * 构建返回结果
   */
  private Map<String, Object> buildResult(int originalSize, byte[] compressedBytes, String format) {
    String mimeType = "jpeg".equals(format) ? "image/jpeg" : "image/" + format;
    String base64 = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(compressedBytes);
    double compressionRatio = calculateCompressionRatio(originalSize, compressedBytes.length);
    logger.info("图片压缩完成, originalSize={}, compressedSize={}, format={}, compressionRatio={}%",
      originalSize, compressedBytes.length, format, String.format("%.2f", compressionRatio));
    Map<String, Object> result = new HashMap<>();
    result.put("base64", base64);
    result.put("originalSize", originalSize);
    result.put("compressedSize", compressedBytes.length);
    result.put("compressionRatio", Math.round(compressionRatio * 100.0) / 100.0);
    result.put("format", format);
    return result;
  }

  /**
   * 计算压缩率
   */
  private double calculateCompressionRatio(int originalSize, int compressedSize) {
    return originalSize > 0
      ? (1.0 - (double) compressedSize / originalSize) * 100.0
      : 0.0;
  }

  /**
   * 缩放图片
   *
   * @param originalImage 原始图片
   * @param maxWidth 最大宽度
   * @param maxHeight 最大高度
   * @return 缩放后的图片
   */
  private BufferedImage resizeImage(BufferedImage originalImage, Integer maxWidth, Integer maxHeight) {
    int originalWidth = originalImage.getWidth();
    int originalHeight = originalImage.getHeight();
    // 计算缩放比例
    double widthRatio = maxWidth != null ? (double) maxWidth / originalWidth : 1.0;
    double heightRatio = maxHeight != null ? (double) maxHeight / originalHeight : 1.0;
    double ratio = Math.min(widthRatio, heightRatio);
    // 如果不需要缩放，直接返回原图
    if (ratio >= 1.0) {
      return originalImage;
    }
    int newWidth = (int) (originalWidth * ratio);
    int newHeight = (int) (originalHeight * ratio);
    // 创建缩放后的图片
    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = resizedImage.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
    g.dispose();
    return resizedImage;
  }

  /**
   * 压缩图片
   * 先用JPEG格式压缩以获得更好的压缩效果，然后转换回原始格式
   *
   * @param image 图片
   * @param quality 压缩质量（0.0-1.0）
   * @param originalFormat 原始格式
   * @return 压缩后的字节数组
   */
  private byte[] compressImage(BufferedImage image, float quality, String originalFormat) throws IOException {
    // 对于JPEG格式，直接使用JPEG压缩
    if ("jpeg".equals(originalFormat) || "jpg".equals(originalFormat)) {
      return compressAsJpeg(image, quality);
    }
    // 对于其他格式，先用JPEG压缩，再转换回原始格式
    // 这样可以获得更好的压缩效果
    byte[] jpegBytes = compressAsJpeg(image, quality);
    return convertToOriginalFormat(jpegBytes, originalFormat);
  }

  /**
   * 将JPEG压缩后的图片转换回原始格式
   *
   * @param jpegBytes JPEG格式的字节数组
   * @param targetFormat 目标格式
   * @return 转换后的字节数组
   */
  private byte[] convertToOriginalFormat(byte[] jpegBytes, String targetFormat) throws IOException {
    // 读取JPEG图片
    BufferedImage image;
    try (InputStream inputStream = new ByteArrayInputStream(jpegBytes)) {
      image = ImageIO.read(inputStream);
      if (image == null) {
        throw new BssException("无法读取压缩后的JPEG图片");
      }
    }
    // 转换为目标格式
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    boolean written = ImageIO.write(image, targetFormat, outputStream);
    if (!written) {
      logger.warn("无法转换为格式{}，返回JPEG格式", targetFormat);
      return jpegBytes;
    }
    return outputStream.toByteArray();
  }

  /**
   * 使用JPEG格式压缩
   */
  private byte[] compressAsJpeg(BufferedImage image, float quality) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    // 转换为RGB格式（JPEG不支持透明通道）
    BufferedImage rgbImage = convertToRgb(image);
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
    if (!writers.hasNext()) {
      throw new BssException("系统不支持JPEG格式写入");
    }
    ImageWriter writer = writers.next();
    ImageWriteParam writeParam = writer.getDefaultWriteParam();
    // 设置压缩质量
    if (writeParam.canWriteCompressed()) {
      writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      writeParam.setCompressionQuality(quality);
    }
    try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
      writer.setOutput(imageOutputStream);
      writer.write(null, new IIOImage(rgbImage, null, null), writeParam);
    }
    finally {
      writer.dispose();
    }
    return outputStream.toByteArray();
  }

  /**
   * 转换为RGB格式
   */
  private BufferedImage convertToRgb(BufferedImage image) {
    if (image.getType() == BufferedImage.TYPE_INT_RGB) {
      return image;
    }
    BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g = rgbImage.createGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    return rgbImage;
  }
}
