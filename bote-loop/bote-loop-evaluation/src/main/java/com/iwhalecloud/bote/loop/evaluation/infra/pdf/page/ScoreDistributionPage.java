package com.iwhalecloud.bote.loop.evaluation.infra.pdf.page;

import com.iwhalecloud.bote.loop.evaluation.infra.pdf.OpenPdfContext;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.handle.ScoreLabelGenerator;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.OpenPdfData;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.ScoreDistributionInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import org.apache.commons.collections4.MapUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Image;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreDistributionPage extends AbstractPage {

  public ScoreDistributionPage(Document document, OpenPdfContext openPdfContext) {
    super(document, openPdfContext);
  }

  public ScoreDistributionPage() {
  }

  @Override
  public void render() {
    // 大标题
    Paragraph title = new Paragraph("得分明细-数据项分布", openPdfContext.getFontTitle());
    title.setSpacingBefore(5f);
    title.setSpacingAfter(5f);
    addElement(title);

    OpenPdfData openPdfData = openPdfContext.getOpenPdfData();
    Map<String, List<ScoreDistributionInfo>> evaluatorScoreDistributionInfos = openPdfData.getEvaluatorScoreDistributionInfos();
    if (MapUtils.isEmpty(evaluatorScoreDistributionInfos)) {
      addEmptyDataParagraph();
      return;
    }

    StandardChartTheme theme = createTheme();
    // 遍历添加其他卡片
    for (Map.Entry<String, List<ScoreDistributionInfo>> entry : evaluatorScoreDistributionInfos.entrySet()) {
      String evaluatorKey = entry.getKey();
      List<ScoreDistributionInfo> scoreDistributionInfos = entry.getValue();
      PdfPTable outerTable = createOuterTable();
      PdfPTable cardTable = createCardTable();
      // 标题
      Phrase scoreDistributionTitle = new Phrase(evaluatorKey, openPdfContext.getFontHeading());
      // 百分比表格
      PdfPTable distributionTable = createDistributionTable();
      fillDistributionTable(distributionTable, scoreDistributionInfos);
      addCardCell(cardTable, new PdfPCell(scoreDistributionTitle));
      addCardCell(cardTable, new PdfPCell(distributionTable));
      // 百分比直方图
      JFreeChart chart = createChart(theme, scoreDistributionInfos);
      Image image = createImage(chart);
      addOuterCell(outerTable, new PdfPCell(cardTable));
      addOuterCell(outerTable, new PdfPCell(image));
      addElement(outerTable);
    }
  }

  private PdfPTable createOuterTable() {
    PdfPTable outerTable = new PdfPTable(1);
    outerTable.setWidthPercentage(100);
    return outerTable;
  }

  private void addOuterCell(PdfPTable outerTable, PdfPCell cell) {
    cell.setBorder(Rectangle.NO_BORDER);
    outerTable.addCell(cell);
  }

  private PdfPTable createCardTable() {
    PdfPTable cardTable = new PdfPTable(1);
    cardTable.setWidthPercentage(100);
    cardTable.setSpacingBefore(5f);
    cardTable.setSpacingAfter(5f);
    return cardTable;
  }

  private void addCardCell(PdfPTable cardTable, PdfPCell cell) {
    cell.setPadding(5f);
    cell.setBorder(Rectangle.NO_BORDER);
    cardTable.addCell(cell);
  }

  private PdfPTable createDistributionTable() {
    PdfPTable distributionTable = new PdfPTable(3);
    distributionTable.setWidthPercentage(100);
    distributionTable.setWidths(new int[]{3, 3, 4});
    return distributionTable;
  }

  private void fillDistributionTable(PdfPTable distributionTable, List<ScoreDistributionInfo> scoreDistributionInfos) {
    distributionTable.addCell(warpCell(createCell(new Phrase("数量", openPdfContext.getFontHeading()))));
    distributionTable.addCell(warpCell(createCell(new Phrase("分数", openPdfContext.getFontHeading()))));
    distributionTable.addCell(warpCell(createCell(new Phrase("百分比", openPdfContext.getFontHeading()))));
    for (ScoreDistributionInfo info : scoreDistributionInfos) {
      distributionTable.addCell(createCell(new Phrase(String.valueOf(info.getCount()), openPdfContext.getFontNormal())));
      distributionTable.addCell(createCell(new Phrase(info.getScore(), openPdfContext.getFontNormal())));
      distributionTable.addCell(createCell(new Phrase(String.format("%.2f%%", info.getPercentage()), openPdfContext.getFontNormal())));
    }
  }

  private PdfPCell createCell(Phrase phrase) {
    PdfPCell cell = new PdfPCell(phrase);
    setCellStyle(cell);
    cell.setVerticalAlignment(Element.ALIGN_TOP);
    return cell;
  }

  private Image createImage(JFreeChart chart) {
    // 1. 生成更专业的图表尺寸（宽高比 5:3，适配A4纸）
    int width = 500;  // 稍缩小宽度，避免撑满页面
    int height = 250; // 高度保持适中，保证可读性
    BufferedImage chartImage = chart.createBufferedImage(width, height);

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      ImageIO.write(chartImage, "PNG", outputStream);
      Image pdfImage = Image.getInstance(outputStream.toByteArray());
      // 2. 关键：在PDF中按比例缩放（不拉伸变形）
      pdfImage.scalePercent(100); // 按百分比缩放
      // 3. 专业排版：居中 + 合理上下边距
      pdfImage.setAlignment(Element.ALIGN_CENTER);
      pdfImage.setSpacingBefore(5f);  // 与上方表格拉开距离
      pdfImage.setSpacingAfter(5f);   // 与下方内容留足空白
      return pdfImage;
    }
    catch (IOException e) {
      throw new BssException("生成直方图失败" + e.getMessage(), e);
    }
  }

  private StandardChartTheme createTheme() {
    StandardChartTheme theme = new StandardChartTheme("CN");
    try (InputStream is = new ClassPathResource("font/simhei.ttf").getInputStream()) {
      Font font = Font.createFont(Font.TRUETYPE_FONT, is);
      Font fontTitle = font.deriveFont(Font.BOLD, 10f);
      Font larrgeFont = font.deriveFont(Font.PLAIN, 12f);
      Font regularFont = font.deriveFont(Font.PLAIN, 10f);
      theme.setExtraLargeFont(fontTitle); // 标题字体
      theme.setLargeFont(larrgeFont);    // 坐标轴标题字体
      theme.setRegularFont(regularFont);  // 标签字体
    }
    catch (Exception e) {
      throw new BssException("直方图字体加载失败" + e.getMessage(), e);
    }
    return theme;
  }

  private JFreeChart createChart(StandardChartTheme theme, List<ScoreDistributionInfo> scoreDistributionInfos) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    List<ScoreDistributionInfo> newList = new ArrayList<>();
    int size = scoreDistributionInfos.size();
    int maxCategory = 6;
    if (size > maxCategory) {
      List<ScoreDistributionInfo> otherList = scoreDistributionInfos.subList(0, size - maxCategory);
      ScoreDistributionInfo otherInfo = new ScoreDistributionInfo();
      otherInfo.setScore("其他");
      otherInfo.setCount(0L);
      otherInfo.setPercentage(0.0);
      for (ScoreDistributionInfo ele : otherList) {
        otherInfo.setCount(otherInfo.getCount() + ele.getCount());
        otherInfo.setPercentage(otherInfo.getPercentage() + ele.getPercentage());
      }
      newList.add(otherInfo);
      List<ScoreDistributionInfo> remainList = scoreDistributionInfos.subList(size - maxCategory, size);
      newList.addAll(remainList);
    }
    else {
      newList = scoreDistributionInfos;
    }
    for (ScoreDistributionInfo info : newList) {
      dataset.addValue(info.getPercentage(), "得分分布", info.getScore());
    }
    ScoreLabelGenerator scoreLabelGenerator = new ScoreLabelGenerator(newList);
    return createChart(dataset, theme, scoreLabelGenerator);
  }

  private JFreeChart createChart(DefaultCategoryDataset dataset, StandardChartTheme theme, ScoreLabelGenerator scoreLabelGenerator) {
    ChartFactory.setChartTheme(theme);
    // 直方图
    // 标题 评估器名称:评估器版本
    // X轴 得分
    // Y轴 百分比
    JFreeChart chart = ChartFactory.createBarChart(
      "",
      "得分",
      "百分比",
      dataset,
      PlotOrientation.VERTICAL,
      false,
      false,
      false
    );

    // 开启全局抗锯齿
    chart.setAntiAlias(true);
    // 优化文本渲染
    chart.getRenderingHints().put(
      RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    );
    chart.getRenderingHints().put(
      RenderingHints.KEY_FRACTIONALMETRICS,
      RenderingHints.VALUE_FRACTIONALMETRICS_ON
    );
    chart.getRenderingHints().put(
      RenderingHints.KEY_RENDERING,
      RenderingHints.VALUE_RENDER_QUALITY
    );

    // 绘图区背景白色
    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(Color.lightGray);
    // 去除绘图区边框
    plot.setOutlineVisible(false);

    TextTitle comment = new TextTitle("得分数量分布柱状图\n按照数量排序，只展示数量靠前的6类得分，剩余得分合并为其他得分\n未生成得分使用无得分表示");
    comment.setHorizontalAlignment(HorizontalAlignment.LEFT);
    comment.setVerticalAlignment(VerticalAlignment.BOTTOM);
    comment.setTextAlignment(HorizontalAlignment.LEFT);
    comment.setFont(theme.getLargeFont());
    comment.setPaint(Color.GRAY);
    comment.setPosition(RectangleEdge.BOTTOM);
    comment.setPadding(new RectangleInsets(0, 20, 5, 0));

    chart.addSubtitle(comment);

    // Y轴刻度水平虚线配置
    // 开启Y轴水平网格线（每个刻度对应一条）
    plot.setRangeGridlinesVisible(true);
    // 设置网格线为浅灰色（匹配参考图的淡虚线）
    plot.setRangeGridlinePaint(Color.WHITE);
    // 设置虚线样式：实线1px + 空白2px（可根据需要调整间隔）
    plot.setRangeGridlineStroke(new BasicStroke(
      1.5f,                  // 线条粗细（核心：加粗到1.5px）
      BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_BEVEL,
      0,
      new float[]{2, 1},           // 虚线间隔：实线2px + 空白1px（更密集）
      0
    ));

    // 隐藏垂直网格线
    plot.setDomainGridlinesVisible(false);

    // 设置柱子颜色
    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    // 设置所有柱子的填充颜色
    for (int i = 0; i < dataset.getRowCount(); i++) {
      renderer.setSeriesPaint(i, new Color(72, 144, 216));
    }

    // 0.1 表示柱子最大宽度为图表区域宽度的 10%
    renderer.setMaximumBarWidth(0.1);

    // 显示柱子数值
    // 开启标签显示（你的原有代码）
    renderer.setDefaultItemLabelsVisible(true);
    renderer.setDefaultItemLabelGenerator(scoreLabelGenerator);

    renderer.setDefaultItemLabelPaint(Color.BLACK); // 标签颜色
    // 设置标签位置：在柱子顶部居中显示（你的原有代码，保留）
    renderer.setDefaultPositiveItemLabelPosition(
      new ItemLabelPosition(
        ItemLabelAnchor.OUTSIDE12, // 标签锚点在柱子顶部外侧
        TextAnchor.BOTTOM_CENTER   // 文本底部居中对齐
      )
    );

    // Y轴配置
    ValueAxis rangeAxis = plot.getRangeAxis();
    // 强制设置新的上限，保证标签有足够显示空间
    rangeAxis.setUpperBound(109);
    // 固定Y轴下限为0
    rangeAxis.setLowerBound(0);

    // 你确认有效的 X 轴对齐
    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setCategoryMargin(0.2);
    domainAxis.setLowerMargin(0.05);
    domainAxis.setUpperMargin(0.1);
    plot.setInsets(RectangleInsets.ZERO_INSETS);

    return chart;
  }


}
