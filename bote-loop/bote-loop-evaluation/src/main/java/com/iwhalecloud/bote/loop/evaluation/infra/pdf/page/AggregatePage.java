package com.iwhalecloud.bote.loop.evaluation.infra.pdf.page;

import com.iwhalecloud.bote.loop.evaluation.infra.pdf.OpenPdfContext;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.AggregateEvaluatorInfo;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.AggregateSummaryInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.util.Assert;

import java.util.List;

public class AggregatePage extends AbstractPage {

  public AggregatePage(Document document, OpenPdfContext openPdfContext) {
    super(document, openPdfContext);
  }

  public AggregatePage() {
  }

  @Override
  public void render() {
    Assert.notNull(openPdfContext, "openPdfContext must not be null");
    Assert.notNull(openPdfContext.getOpenPdfData(), "openPdfContext must not be null");
    newPage();

    Paragraph summaryTitle = new Paragraph("指标统计", openPdfContext.getFontTitle());
    summaryTitle.setSpacingBefore(5f);
    summaryTitle.setSpacingAfter(5f);
    addElement(summaryTitle);

    AggregateSummaryInfo aggregateSummaryInfo = openPdfContext.getOpenPdfData().getAggregateSummaryInfo();
    PdfPTable summaryTable = createSummaryTable();
    summaryTable.setSpacingBefore(5f);
    summaryTable.addCell(warpCell(createCell("评测集总数", openPdfContext.getFontHeading())));
    summaryTable.addCell(warpCell(createCell("整体通过得分", openPdfContext.getFontHeading())));
    summaryTable.addCell(warpCell(createCell("整体通过率", openPdfContext.getFontHeading())));
    if (aggregateSummaryInfo != null) {
      summaryTable.addCell(createCell(String.valueOf(aggregateSummaryInfo.getEvalSetSize()), openPdfContext.getFontNormal()));
      summaryTable.addCell(createCell(String.valueOf(aggregateSummaryInfo.getPassScore()), openPdfContext.getFontNormal()));
      summaryTable.addCell(createCell(String.format("%.2f", aggregateSummaryInfo.getPassRate()), openPdfContext.getFontNormal()));
    }
    addElement(summaryTable);


    Paragraph title = new Paragraph("评估器聚合得分", openPdfContext.getFontTitle());
    title.setSpacingBefore(5f);
    title.setSpacingAfter(5f);
    addElement(title);

    List<AggregateEvaluatorInfo> aggregateEvaluatorInfos = openPdfContext.getOpenPdfData().getAggregateEvaluatorInfos();
    PdfPTable table = createTable();
    table.setSpacingBefore(5f);
    table.setWidths(new int[] {
      3, 3, 2, 2, 2, 2, 3, 2
    });
    table.addCell(warpCell(createCell("评估器名称", openPdfContext.getFontHeading())));
    table.addCell(warpCell(createCell("评估器版本", openPdfContext.getFontHeading())));
    table.addCell(warpCell(createCell("平均值", openPdfContext.getFontHeading())));
    table.addCell(warpCell(createCell("最大值", openPdfContext.getFontHeading())));
    table.addCell(warpCell(createCell("最小值", openPdfContext.getFontHeading())));
    table.addCell(warpCell(createCell("总和", openPdfContext.getFontHeading())));
    table.addCell(warpCell(createCell("通过得分", openPdfContext.getFontHeading())));
    table.addCell(warpCell(createCell("通过率", openPdfContext.getFontHeading())));
    if (CollectionUtils.isEmpty(aggregateEvaluatorInfos)) {
      return;
    }
    for (AggregateEvaluatorInfo aggregateEvaluatorInfo : aggregateEvaluatorInfos) {
      table.addCell(createCell(aggregateEvaluatorInfo.getEvaluatorName(), openPdfContext.getFontNormal()));
      table.addCell(createCell(aggregateEvaluatorInfo.getEvaluatorVersion(), openPdfContext.getFontNormal()));
      table.addCell(createCell(String.valueOf(aggregateEvaluatorInfo.getAverageScore()), openPdfContext.getFontNormal()));
      table.addCell(createCell(String.valueOf(aggregateEvaluatorInfo.getMaxScore()), openPdfContext.getFontNormal()));
      table.addCell(createCell(String.valueOf(aggregateEvaluatorInfo.getMinScore()), openPdfContext.getFontNormal()));
      table.addCell(createCell(String.valueOf(aggregateEvaluatorInfo.getSumScore()), openPdfContext.getFontNormal()));
      table.addCell(createCell(String.valueOf(aggregateEvaluatorInfo.getPassScore()), openPdfContext.getFontNormal()));
      table.addCell(createCell(String.format("%.2f", aggregateEvaluatorInfo.getPassRate()), openPdfContext.getFontNormal()));
    }
    addElement(table);
  }

  private PdfPTable createSummaryTable() {
    PdfPTable table = new PdfPTable(3);
    table.setWidthPercentage(100);
    table.setHorizontalAlignment(Element.ALIGN_CENTER);
    return table;
  }

  private PdfPTable createTable() {
    PdfPTable table = new PdfPTable(8);
    table.setWidthPercentage(100);
    table.setHorizontalAlignment(Element.ALIGN_CENTER);
    return table;
  }

  public PdfPCell createCell(String content, Font font) {
    Paragraph paragraph = new Paragraph(content, font);
    PdfPCell cell = new PdfPCell(paragraph);
    setCellStyle(cell);
    return cell;
  }
}
