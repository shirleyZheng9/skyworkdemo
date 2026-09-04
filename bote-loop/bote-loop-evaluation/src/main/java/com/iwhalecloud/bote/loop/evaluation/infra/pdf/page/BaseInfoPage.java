package com.iwhalecloud.bote.loop.evaluation.infra.pdf.page;

import com.iwhalecloud.bote.loop.evaluation.infra.pdf.OpenPdfContext;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.ExperimentBaseInfo;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.util.Assert;

import java.util.List;

public class BaseInfoPage extends AbstractPage {

  public BaseInfoPage(Document document, OpenPdfContext openPdfContext) {
    super(document, openPdfContext);
  }

  public BaseInfoPage() {
  }

  @Override
  public void render() {
    Paragraph title = new Paragraph("基础信息", openPdfContext.getFontTitle());
    title.setSpacingBefore(5f);
    title.setSpacingAfter(5f);
    title.setAlignment(Element.ALIGN_CENTER);
    addElement(title);

    ExperimentBaseInfo baseInfo = openPdfContext.getOpenPdfData().getBaseInfo();
    Assert.notNull(baseInfo, "评测实验基础信息不能为空");

    PdfPTable table = createTable();
    table.setSpacingBefore(5f);
    table.addCell(warpCell(createCell("评测集", openPdfContext.getFontHeading())));
    table.addCell(createCell(baseInfo.getEvalSet(), openPdfContext.getFontNormal()));
    table.addCell(warpCell(createCell("评测对象类型", openPdfContext.getFontHeading())));
    table.addCell(createCell(baseInfo.getEvalTargetType(), openPdfContext.getFontNormal()));
    table.addCell(warpCell(createCell("评测对象", openPdfContext.getFontHeading())));
    table.addCell(createCell(baseInfo.getEvalTarget(), openPdfContext.getFontNormal()));
    table.addCell(warpCell(createCell("评估器", openPdfContext.getFontHeading())));

    List<String> evaluatorNameVersions = baseInfo.getEvaluatorNameVersions();
    if (evaluatorNameVersions.isEmpty()) {
      table.addCell(createCell("-", openPdfContext.getFontNormal()));
    }
    else if (evaluatorNameVersions.size() == 1) {
      table.addCell(createCell(evaluatorNameVersions.getFirst(), openPdfContext.getFontNormal()));
    }
    else {
      PdfPTable evaluatorTable = new PdfPTable(2);
      int size = evaluatorNameVersions.size();
      for (String evaluatorNameVersion : evaluatorNameVersions) {
        PdfPCell cell = new PdfPCell(new Phrase(evaluatorNameVersion + evaluatorNameVersion, openPdfContext.getFontSmall()));
        setCellStyle(cell);
        cell.setPadding(2f);
        evaluatorTable.addCell(cell);
      }
      int emptySize = (2 - size % 2) % 2;
      for (int i = 0; i < emptySize; i++) {
        PdfPCell cell = new PdfPCell();
        setCellStyle(cell);
        cell.setPadding(2f);
        evaluatorTable.addCell(cell);
      }
      PdfPCell cell = new PdfPCell(evaluatorTable);
      cell.setBorder(Rectangle.NO_BORDER);
      table.addCell(cell);
    }

    table.addCell(warpCell(createCell("创建人", openPdfContext.getFontHeading())));
    table.addCell(createCell(baseInfo.getCreatedBy(), openPdfContext.getFontNormal()));
    table.addCell(warpCell(createCell("创建时间", openPdfContext.getFontHeading())));
    table.addCell(createCell(baseInfo.getCreatedAt(), openPdfContext.getFontNormal()));
    table.addCell(warpCell(createCell("结束时间", openPdfContext.getFontHeading())));
    table.addCell(createCell(baseInfo.getEndDate(), openPdfContext.getFontNormal()));
    table.addCell(warpCell(createCell("描述", openPdfContext.getFontHeading())));
    table.addCell(createCell(baseInfo.getDescription(), openPdfContext.getFontNormal()));

    addElement(table);
  }

  public PdfPTable createTable() {
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(70);
    table.setHorizontalAlignment(Element.ALIGN_CENTER);
    table.setWidths(new float[]{1f, 2.5f});
    return table;
  }

  public PdfPCell createCell(String content, Font font) {
    Paragraph paragraph = new Paragraph(content, font);
    PdfPCell cell = new PdfPCell(paragraph);
    setCellStyle(cell);
    return cell;
  }
}
