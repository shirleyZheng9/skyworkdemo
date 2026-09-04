package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.ImageWatermarkPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 图片水印插件
 * 在图片上添加水印信息，生成带有版权保护水印的新图片
 *
 * @author zhao.xu104
 * @since 2025-11-20
 */
@Component
public class ImageWatermarkPlugin extends AbstractFilePlugin<ImageWatermarkPluginParams> {
  /** 支持的水印位置 */
  private static final List<String> SUPPORTED_POSITIONS = List.of(
    "top-left", "top-right", "bottom-left", "bottom-right", "center", "repeat"
  );

  public ImageWatermarkPlugin(IFileStoreService fileStoreService) {
    super(ImageWatermarkPluginParams.class, fileStoreService);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_IMAGE_WATERMARK;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("fileId", "文件ID", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("fileUrl", "文件地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("watermarkText", "水印文本内容", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("position", "水印位置（top-left/top-right/bottom-left/bottom-right/center/repeat）", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fontSize", "字体大小（可选，默认24）", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("color", "字体颜色（可选，默认#808080）", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("opacity", "透明度（0.0-1.0，可选，默认0.5）", AttrDataType.NUMBER));
    children.add(ParameterSpec.newProperty("angle", "旋转角度（可选，默认0，单位：度）", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("spacing", "重复水印间距倍数（可选，默认1.5，表示水印间距为文本宽度的倍数，仅当position=\"repeat\"时有效）", AttrDataType.NUMBER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("fileId", "带水印的图片文件ID", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("fileName", "文件名", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fileSize", "文件大小（字节）", AttrDataType.INTEGER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(ImageWatermarkPluginParams params) {
    validateFileParams(params);
    Assert.notNull(params.getWatermarkText(), "水印文本内容不能为空");
    Assert.hasText(params.getWatermarkText(), "水印文本内容不能为空");
    String position = params.getPosition();
    if (StringUtils.isNotBlank(position)) {
      if (!SUPPORTED_POSITIONS.contains(position.toLowerCase(Locale.ROOT))) {
        throw new BssException("不支持的水印位置: " + position +
          "，支持的位置: " + String.join(", ", SUPPORTED_POSITIONS));
      }
    }
    if (params.getFontSize() != null && params.getFontSize() <= 0) {
      throw new BssException("字体大小必须大于0");
    }
    if (params.getOpacity() != null) {
      float opacity = params.getOpacity();
      if (opacity < 0.0f || opacity > 1.0f) {
        throw new BssException("透明度参数必须在0.0-1.0之间");
      }
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public Object doRun(ImageWatermarkPluginParams pluginParams) {
    try {
      byte[] originalImageBytes = downloadFile(pluginParams);
      BufferedImage image = readImage(originalImageBytes);
      // 添加水印
      BufferedImage watermarkedImage = addWatermark(image, pluginParams);
      // 生成新文件名
      String originalFileName = getOriginalFileName(pluginParams, "image.jpg");
      String baseName = FilenameUtils.getBaseName(originalFileName);
      if (StringUtils.isBlank(baseName)) {
        baseName = "watermarked_image";
      }
      String fileExtension = getFileExtension(originalFileName, "jpg");
      String newFileName = baseName + "_watermark." + fileExtension;
      // 转换为字节数组
      byte[] watermarkedBytes = imageToBytes(watermarkedImage, fileExtension);
      // 上传带水印的图片
      FileInfoVO watermarkedFileInfo = uploadFile(watermarkedBytes, newFileName, fileExtension);
      logger.info("图片水印添加成功, sourceFileId={}, watermarkText={}, newFileId={}",
        pluginParams.getFileId(), pluginParams.getWatermarkText(), watermarkedFileInfo.getFileId());
      Map<String, Object> result = new HashMap<>();
      result.put("fileId", watermarkedFileInfo.getFileId());
      result.put("fileName", newFileName);
      result.put("fileSize", watermarkedBytes.length);
      return result;
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      logger.error("图片水印添加失败: {}", e.getMessage(), e);
      throw new BssException("图片水印添加失败: " + e.getMessage(), e);
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
   * 添加水印
   */
  private BufferedImage addWatermark(BufferedImage originalImage, ImageWatermarkPluginParams params) {
    int width = originalImage.getWidth();
    int height = originalImage.getHeight();
    // 创建带透明通道的图片
    BufferedImage watermarkedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = watermarkedImage.createGraphics();
    // 设置高质量渲染
    setupGraphicsQuality(g);
    // 绘制原图
    g.drawImage(originalImage, 0, 0, null);
    // 获取水印参数
    WatermarkConfig config = extractWatermarkConfig(params);
    // 设置字体和颜色
    Font font = createWatermarkFont(config.fontSize);
    g.setFont(font);
    g.setColor(config.color);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, config.opacity));
    // 绘制水印
    drawWatermarkText(g, config, font, width, height);
    g.dispose();
    return watermarkedImage;
  }

  /**
   * 设置图形渲染质量
   */
  private void setupGraphicsQuality(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }

  /**
   * 提取水印配置参数
   */
  private WatermarkConfig extractWatermarkConfig(ImageWatermarkPluginParams params) {
    WatermarkConfig config = new WatermarkConfig();
    config.text = params.getWatermarkText();
    config.fontSize = params.getFontSize() != null ? params.getFontSize() : 24;
    config.opacity = params.getOpacity() != null ? params.getOpacity() : 0.5f;
    config.color = parseColor(params.getColor());
    config.position = StringUtils.isNotBlank(params.getPosition())
      ? params.getPosition().toLowerCase(Locale.ROOT)
      : "bottom-right";
    config.angle = params.getAngle() != null ? params.getAngle() : 0;
    config.spacing = params.getSpacing() != null ? params.getSpacing() : 1.5f;
    return config;
  }

  /**
   * 创建水印字体（支持中文）
   */
  private Font createWatermarkFont(int fontSize) {
    Font font;
    try (InputStream inputStream = new ClassPathResource("font/simhei.ttf").getInputStream()) {
      // 获取字体
      font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
      font = font.deriveFont(Font.PLAIN, fontSize);
    }
    catch (FontFormatException | IOException e) {
      throw new BssException("获取水印字体文件失败", e);
    }
    return font;
  }

  /**
   * 绘制水印文本
   */
  private void drawWatermarkText(Graphics2D g, WatermarkConfig config, Font font, int width, int height) {
    boolean shouldRepeat = "repeat".equals(config.position);
    if (shouldRepeat) {
      drawRepeatWatermark(g, config.text, font, width, height, config.angle, config.opacity, config.spacing);
    }
    else {
      drawSingleWatermark(g, config, width, height);
    }
  }

  /**
   * 绘制单个水印
   */
  private void drawSingleWatermark(Graphics2D g, WatermarkConfig config, int width, int height) {
    int[] positionXY = calculatePosition(g, config.text, width, height, config.position);
    int x = positionXY[0];
    int y = positionXY[1];
    // 应用旋转
    if (config.angle != 0) {
      AffineTransform transform = new AffineTransform();
      transform.rotate(Math.toRadians(config.angle), x, y);
      g.setTransform(transform);
    }
    // 绘制水印文本
    g.drawString(config.text, x, y);
  }


  /**
   * 计算水印位置
   */
  private int[] calculatePosition(Graphics2D g, String text, int imageWidth, int imageHeight, String position) {
    // 获取文本尺寸
    int textWidth = g.getFontMetrics().stringWidth(text);
    int textHeight = g.getFontMetrics().getHeight();
    int x;
    int padding = 20; // 边距
    int y = switch (position) {
      case "top-left" -> {
        x = padding;
        yield textHeight + padding;
      }
      case "top-right" -> {
        x = imageWidth - textWidth - padding;
        yield textHeight + padding;
      }
      case "bottom-left" -> {
        x = padding;
        yield imageHeight - padding;
      }
      case "bottom-right" -> {
        x = imageWidth - textWidth - padding;
        yield imageHeight - padding;
      }
      case "center" -> {
        x = (imageWidth - textWidth) / 2;
        yield (imageHeight + textHeight) / 2;
      }
      default -> {
        x = imageWidth - textWidth - padding;
        yield imageHeight - padding;
      }
    };
    return new int[]{x, y};
  }

  /**
   * 绘制重复水印（全屏水印）
   */
  private void drawRepeatWatermark(Graphics2D g, String text, Font font,
                                   int imageWidth, int imageHeight, int angle, float opacity, float spacing) {
    // 获取文本尺寸
    g.setFont(font);
    int textWidth = g.getFontMetrics().stringWidth(text);
    int textHeight = g.getFontMetrics().getHeight();
    // 计算间距（使用用户指定的间距倍数）
    int spacingX = (int) (textWidth * spacing);
    int spacingY = (int) (textHeight * spacing);
    // 如果设置了旋转角度，需要调整间距以适应旋转后的尺寸
    if (angle != 0) {
      double radians = Math.toRadians(angle);
      double cos = Math.abs(Math.cos(radians));
      double sin = Math.abs(Math.sin(radians));
      // 旋转后的文本宽度和高度
      int rotatedWidth = (int) (textWidth * cos + textHeight * sin);
      int rotatedHeight = (int) (textWidth * sin + textHeight * cos);
      spacingX = (int) (rotatedWidth * spacing);
      spacingY = (int) (rotatedHeight * spacing);
    }
    // 保存原始变换
    AffineTransform originalTransform = g.getTransform();
    Composite originalComposite = g.getComposite();
    // 在整个图片范围内重复绘制水印
    for (int x = 0; x < imageWidth + spacingX; x += spacingX) {
      for (int y = 0; y < imageHeight + spacingY; y += spacingY) {
        // 恢复原始变换和透明度
        g.setTransform(originalTransform);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        // 应用旋转（如果有）
        if (angle != 0) {
          AffineTransform transform = new AffineTransform();
          // 旋转中心点设为当前水印位置
          transform.rotate(Math.toRadians(angle), x, y);
          g.setTransform(transform);
        }
        // 绘制水印文本
        g.drawString(text, x, y);
      }
    }
    // 恢复原始状态
    g.setTransform(originalTransform);
    g.setComposite(originalComposite);
  }

  /**
   * 解析颜色
   */
  private Color parseColor(String colorStr) {
    if (StringUtils.isBlank(colorStr)) {
      return new Color(128, 128, 128); // 默认灰色 #808080
    }
    String color = colorStr.trim();
    if (color.startsWith("#")) {
      try {
        return Color.decode(color);
      }
      catch (NumberFormatException e) {
        logger.warn("无法解析颜色值: {}, 使用默认灰色", color);
        return new Color(128, 128, 128);
      }
    }
    // 尝试解析RGB格式
    if (color.startsWith("rgb(") || color.startsWith("RGB(")) {
      try {
        String rgb = color.substring(4, color.length() - 1);
        String[] parts = rgb.split(",");
        if (parts.length == 3) {
          int r = Integer.parseInt(parts[0].trim());
          int g = Integer.parseInt(parts[1].trim());
          int b = Integer.parseInt(parts[2].trim());
          return new Color(r, g, b);
        }
      }
      catch (Exception e) {
        logger.warn("无法解析RGB颜色值: {}, 使用默认灰色", color);
      }
    }
    // 尝试按颜色名称解析
    try {
      return (Color) Color.class.getField(color.toUpperCase()).get(null);
    }
    catch (Exception e) {
      logger.warn("无法解析颜色名称: {}, 使用默认灰色", color);
      return new Color(128, 128, 128);
    }
  }

  /**
   * 将图片转换为字节数组
   */
  private byte[] imageToBytes(BufferedImage image, String format) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    String imageFormat = format;
    if ("jpg".equals(format) || "jpeg".equals(format)) {
      imageFormat = "jpg";
    }
    boolean written = ImageIO.write(image, imageFormat, outputStream);
    if (!written) {
      throw new BssException("无法写入图片格式: " + format);
    }
    return outputStream.toByteArray();
  }

  /**
   * 水印配置内部类
   */
  private static final class WatermarkConfig {
    String text;
    int fontSize;
    float opacity;
    Color color;
    String position;
    int angle;
    float spacing;
  }
}
