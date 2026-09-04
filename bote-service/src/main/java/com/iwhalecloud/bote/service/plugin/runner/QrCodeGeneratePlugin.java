package com.iwhalecloud.bote.service.plugin.runner;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.QrCodeGeneratePluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 二维码生成插件
 *
 * @author lizuyin
 * @since 2025-11-25
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class QrCodeGeneratePlugin extends AbstractPlugin<QrCodeGeneratePluginParams> {

  private static final Logger logger = LoggerFactory.getLogger(QrCodeGeneratePlugin.class);

  /**
   * 默认宽度
   */
  private static final int DEFAULT_WIDTH = 300;

  /**
   * 默认高度
   */
  private static final int DEFAULT_HEIGHT = 300;

  /**
   * 默认边距
   */
  private static final int DEFAULT_MARGIN = 4;

  /**
   * 默认Logo大小
   */
  private static final int DEFAULT_LOGO_SIZE = 60;

  /**
   * 默认前景色
   */
  private static final String DEFAULT_FOREGROUND_COLOR = "#000000";

  /**
   * 默认背景色
   */
  private static final String DEFAULT_BACKGROUND_COLOR = "#FFFFFF";

  /**
   * 默认格式
   */
  private static final String DEFAULT_FORMAT = "png";

  /**
   * 默认错误纠正级别
   */
  private static final String DEFAULT_ERROR_CORRECTION_LEVEL = "M";

  public QrCodeGeneratePlugin() {
    super(QrCodeGeneratePluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_QR_CODE_GENERATE;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("content", "二维码内容", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("width", "二维码宽度（像素），默认300", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("height", "二维码高度（像素），默认300", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("format", "图片格式，png, jpg, jpeg，默认png", AttrDataType.STRING));
    children.add(
      ParameterSpec.newProperty("errorCorrectionLevel", "错误纠正级别，L, M, Q, H，默认M", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("margin", "边距（像素），默认4", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("logoBase64", "Logo Base64编码（可选，data URI格式或纯Base64字符串）",
      AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("logoSize", "Logo大小（像素），默认60", AttrDataType.INTEGER));
    children.add(
      ParameterSpec.newProperty("foregroundColor", "前景色（十六进制，如#000000），默认#000000", AttrDataType.STRING));
    children.add(
      ParameterSpec.newProperty("backgroundColor", "背景色（十六进制，如#FFFFFF），默认#FFFFFF", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("base64", "二维码Base64编码（包含data URI前缀）", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(QrCodeGeneratePluginParams params) {
    validateContent(params);
    validateDimensions(params);
    validateFormat(params);
    validateErrorCorrectionLevel(params);
    validateMargin(params);
    validateLogo(params);
    validateColors(params);
  }

  /**
   * 校验内容参数
   */
  private void validateContent(QrCodeGeneratePluginParams params) {
    Assert.notNull(params.getContent(), "content不能为空");
    Assert.hasLength(params.getContent(), "content不能为空");
  }

  /**
   * 校验尺寸参数
   */
  private void validateDimensions(QrCodeGeneratePluginParams params) {
    if (params.getWidth() != null && params.getWidth() <= 0) {
      throw new BssException("width必须大于0");
    }
    if (params.getHeight() != null && params.getHeight() <= 0) {
      throw new BssException("height必须大于0");
    }
  }

  /**
   * 校验图片格式参数
   */
  private void validateFormat(QrCodeGeneratePluginParams params) {
    if (params.getFormat() != null) {
      String format = params.getFormat().toLowerCase();
      if (!"png".equals(format) && !"jpg".equals(format) && !"jpeg".equals(format)) {
        throw new BssException("format必须是png、jpg或jpeg");
      }
    }
  }

  /**
   * 校验错误纠正级别参数
   */
  private void validateErrorCorrectionLevel(QrCodeGeneratePluginParams params) {
    if (params.getErrorCorrectionLevel() != null) {
      String level = params.getErrorCorrectionLevel().toUpperCase();
      if (!"L".equals(level) && !"M".equals(level) && !"Q".equals(level) && !"H".equals(level)) {
        throw new BssException("errorCorrectionLevel必须是L、M、Q或H");
      }
    }
  }

  /**
   * 校验边距参数
   */
  private void validateMargin(QrCodeGeneratePluginParams params) {
    if (params.getMargin() != null && params.getMargin() < 0) {
      throw new BssException("margin必须大于等于0");
    }
  }

  /**
   * 校验Logo相关参数
   */
  private void validateLogo(QrCodeGeneratePluginParams params) {
    if (params.getLogoSize() != null && params.getLogoSize() <= 0) {
      throw new BssException("logoSize必须大于0");
    }
  }

  /**
   * 校验颜色参数
   */
  private void validateColors(QrCodeGeneratePluginParams params) {
    if (params.getForegroundColor() != null && !isValidColor(params.getForegroundColor())) {
      throw new BssException("foregroundColor格式不正确，应为十六进制颜色值，如#000000");
    }
    if (params.getBackgroundColor() != null && !isValidColor(params.getBackgroundColor())) {
      throw new BssException("backgroundColor格式不正确，应为十六进制颜色值，如#FFFFFF");
    }
  }

  @Override
  public Object doRun(QrCodeGeneratePluginParams pluginParams) {
    try {
      // 准备参数并设置默认值
      int width = getWidthOrDefault(pluginParams);
      int height = getHeightOrDefault(pluginParams);
      String format = getFormatOrDefault(pluginParams);
      String errorCorrectionLevel = getErrorCorrectionLevelOrDefault(pluginParams);
      int margin = getMarginOrDefault(pluginParams);
      int logoSize = getLogoSizeOrDefault(pluginParams);
      String foregroundColor = getForegroundColorOrDefault(pluginParams);
      String backgroundColor = getBackgroundColorOrDefault(pluginParams);

      // 生成二维码图片
      BufferedImage qrCodeImage = generateQrCode(pluginParams.getContent(), width, height, errorCorrectionLevel, margin,
        parseColor(foregroundColor), parseColor(backgroundColor));

      // 添加Logo（如果提供）
      processLogoIfNeeded(qrCodeImage, pluginParams.getLogoBase64(), logoSize);

      // 转换为字节数组并构建data URI
      String dataUri = buildDataUri(qrCodeImage, format);

      // 构建返回结果
      return buildResult(dataUri);
    }
    catch (Exception e) {
      logger.error("生成二维码失败: {}", e.getMessage(), e);
      throw new BssException("生成二维码失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取宽度参数或默认值
   */
  private int getWidthOrDefault(QrCodeGeneratePluginParams pluginParams) {
    return pluginParams.getWidth() != null ? pluginParams.getWidth() : DEFAULT_WIDTH;
  }

  /**
   * 获取高度参数或默认值
   */
  private int getHeightOrDefault(QrCodeGeneratePluginParams pluginParams) {
    return pluginParams.getHeight() != null ? pluginParams.getHeight() : DEFAULT_HEIGHT;
  }

  /**
   * 获取格式参数或默认值
   */
  private String getFormatOrDefault(QrCodeGeneratePluginParams pluginParams) {
    return StringUtils.isNotEmpty(pluginParams.getFormat()) ? pluginParams.getFormat().toLowerCase() : DEFAULT_FORMAT;
  }

  /**
   * 获取错误纠正级别参数或默认值
   */
  private String getErrorCorrectionLevelOrDefault(QrCodeGeneratePluginParams pluginParams) {
    return StringUtils.isNotEmpty(pluginParams.getErrorCorrectionLevel()) ? pluginParams.getErrorCorrectionLevel()
      .toUpperCase() : DEFAULT_ERROR_CORRECTION_LEVEL;
  }

  /**
   * 获取边距参数或默认值
   */
  private int getMarginOrDefault(QrCodeGeneratePluginParams pluginParams) {
    return pluginParams.getMargin() != null ? pluginParams.getMargin() : DEFAULT_MARGIN;
  }

  /**
   * 获取Logo大小参数或默认值
   */
  private int getLogoSizeOrDefault(QrCodeGeneratePluginParams pluginParams) {
    return pluginParams.getLogoSize() != null ? pluginParams.getLogoSize() : DEFAULT_LOGO_SIZE;
  }

  /**
   * 获取前景色参数或默认值
   */
  private String getForegroundColorOrDefault(QrCodeGeneratePluginParams pluginParams) {
    return StringUtils.isNotEmpty(pluginParams.getForegroundColor())
      ? pluginParams.getForegroundColor()
      : DEFAULT_FOREGROUND_COLOR;
  }

  /**
   * 获取背景色参数或默认值
   */
  private String getBackgroundColorOrDefault(QrCodeGeneratePluginParams pluginParams) {
    return StringUtils.isNotEmpty(pluginParams.getBackgroundColor())
      ? pluginParams.getBackgroundColor()
      : DEFAULT_BACKGROUND_COLOR;
  }

  /**
   * 处理Logo（如果提供）
   */
  private void processLogoIfNeeded(BufferedImage qrCodeImage, String logoBase64, int logoSize) {
    if (StringUtils.isEmpty(logoBase64)) {
      return;
    }

    try {
      byte[] logoBytes = parseBase64Image(logoBase64);
      if (logoBytes != null && logoBytes.length > 0) {
        BufferedImage logoImage = ImageIO.read(new java.io.ByteArrayInputStream(logoBytes));
        if (logoImage != null) {
          addLogoToQrCode(qrCodeImage, logoImage, logoSize);
        }
      }
    }
    catch (Exception e) {
      logger.warn("添加Logo失败，继续生成二维码: {}", e.getMessage());
    }
  }

  /**
   * 构建data URI
   */
  private String buildDataUri(BufferedImage qrCodeImage, String format) throws IOException {
    // 转换为字节数组
    byte[] imageBytes = imageToBytes(qrCodeImage, format);

    // 转换为Base64编码
    String base64String = Base64.getEncoder().encodeToString(imageBytes);

    // 构建data URI
    String mimeType = "jpg".equals(format) || "jpeg".equals(format) ? "image/jpeg" : "image/png";
    return "data:" + mimeType + ";base64," + base64String;
  }

  /**
   * 构建返回结果
   */
  private Map<String, Object> buildResult(String dataUri) {
    Map<String, Object> result = new HashMap<>();
    result.put("base64", dataUri);
    result.put("message", "二维码生成成功");
    return result;
  }

  /**
   * 生成二维码图片
   */
  private BufferedImage generateQrCode(String content, int width, int height, String errorCorrectionLevel, int margin,
    Color foregroundColor, Color backgroundColor) throws WriterException {
    Map<EncodeHintType, Object> hints = new HashMap<>();
    hints.put(EncodeHintType.ERROR_CORRECTION, parseErrorCorrectionLevel(errorCorrectionLevel));
    hints.put(EncodeHintType.MARGIN, margin);
    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = image.createGraphics();
    graphics.setColor(backgroundColor);
    graphics.fillRect(0, 0, width, height);
    graphics.setColor(foregroundColor);

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (bitMatrix.get(x, y)) {
          graphics.fillRect(x, y, 1, 1);
        }
      }
    }
    graphics.dispose();

    return image;
  }

  /**
   * 添加Logo到二维码中心
   */
  private void addLogoToQrCode(BufferedImage qrCodeImage, BufferedImage logoImage, int logoSize) {
    Graphics2D graphics = qrCodeImage.createGraphics();
    graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

    int qrWidth = qrCodeImage.getWidth();
    int qrHeight = qrCodeImage.getHeight();

    // 计算Logo位置（居中）
    int logoX = (qrWidth - logoSize) / 2;
    int logoY = (qrHeight - logoSize) / 2;

    // 缩放Logo
    Image scaledLogo = logoImage.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);
    BufferedImage bufferedLogo = new BufferedImage(logoSize, logoSize, BufferedImage.TYPE_INT_ARGB);
    Graphics2D logoGraphics = bufferedLogo.createGraphics();
    logoGraphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
    logoGraphics.drawImage(scaledLogo, 0, 0, null);
    logoGraphics.dispose();

    // 绘制白色背景
    graphics.setColor(Color.WHITE);
    graphics.setStroke(new BasicStroke(5));
    graphics.fillRoundRect(logoX - 5, logoY - 5, logoSize + 10, logoSize + 10, 10, 10);

    // 绘制Logo
    graphics.drawImage(bufferedLogo, logoX, logoY, null);
    graphics.dispose();
  }

  /**
   * 将图片转换为字节数组
   */
  private byte[] imageToBytes(BufferedImage image, String format) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    String imageFormat = "jpg".equals(format) || "jpeg".equals(format) ? "jpg" : "png";
    ImageIO.write(image, imageFormat, baos);
    return baos.toByteArray();
  }

  /**
   * 解析错误纠正级别
   */
  private ErrorCorrectionLevel parseErrorCorrectionLevel(String level) {
    switch (level) {
      case "L":
        return ErrorCorrectionLevel.L;
      case "M":
        return ErrorCorrectionLevel.M;
      case "Q":
        return ErrorCorrectionLevel.Q;
      case "H":
        return ErrorCorrectionLevel.H;
      default:
        return ErrorCorrectionLevel.M;
    }
  }

  /**
   * 解析颜色值
   */
  private Color parseColor(String colorStr) {
    if (colorStr.startsWith("#")) {
      colorStr = colorStr.substring(1);
    }
    if (colorStr.length() == 6) {
      int r = Integer.parseInt(colorStr.substring(0, 2), 16);
      int g = Integer.parseInt(colorStr.substring(2, 4), 16);
      int b = Integer.parseInt(colorStr.substring(4, 6), 16);
      return new Color(r, g, b);
    }
    throw new BssException("颜色格式不正确: " + colorStr);
  }

  /**
   * 验证颜色格式
   */
  private boolean isValidColor(String colorStr) {
    if (colorStr == null) {
      return false;
    }
    String hex = colorStr.startsWith("#") ? colorStr.substring(1) : colorStr;
    if (hex.length() != 6) {
      return false;
    }
    try {
      Integer.parseInt(hex, 16);
      return true;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * 解析Base64图片数据 支持data URI格式（data:image/png;base64,xxx）或纯Base64字符串
   */
  private byte[] parseBase64Image(String base64Str) {
    if (base64Str == null || base64Str.isEmpty()) {
      return null;
    }

    String base64Data = base64Str;
    // 如果是data URI格式，提取Base64部分
    if (base64Str.startsWith("data:")) {
      int commaIndex = base64Str.indexOf(',');
      if (commaIndex > 0) {
        base64Data = base64Str.substring(commaIndex + 1);
      }
    }

    try {
      return Base64.getDecoder().decode(base64Data);
    }
    catch (IllegalArgumentException e) {
      logger.warn("Base64解码失败: {}", e.getMessage());
      throw new BssException("Logo Base64编码格式不正确: " + e.getMessage(), e);
    }
  }
}

