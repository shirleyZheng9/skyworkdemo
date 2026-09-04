package com.iwhalecloud.bote.loop.evaluation.infra.pdf.page;

import com.iwhalecloud.bote.loop.evaluation.infra.pdf.OpenPdfContext;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.EvaluatorRecordInfo;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.ItemDetailInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.draw.LineSeparator;

import java.awt.Color;
import java.util.List;
import java.util.Map;

public class ItemDetailPage extends AbstractPage {

  public ItemDetailPage(Document document, OpenPdfContext openPdfContext) {
    super(document, openPdfContext);
  }

  public ItemDetailPage() {
  }

  @Override
  public void render() {
    List<ItemDetailInfo> itemDetailInfos = openPdfContext.getOpenPdfData().getItemDetailInfos();
    if (CollectionUtils.isEmpty(itemDetailInfos)) {
      return;
    }

    List<ItemDetailInfo> passItemDetailInfos = itemDetailInfos.stream().filter(i -> {
      List<EvaluatorRecordInfo> evaluatorRecordInfos = i.getEvaluatorRecordInfos();
      if (CollectionUtils.isEmpty(evaluatorRecordInfos)) {
        return false;
      }
      return evaluatorRecordInfos.stream().allMatch(EvaluatorRecordInfo::isPassFlag);
    }).toList();

    List<ItemDetailInfo> notPassItemDetailInfos = itemDetailInfos.stream().filter(i -> {
      List<EvaluatorRecordInfo> evaluatorRecordInfos = i.getEvaluatorRecordInfos();
      if (CollectionUtils.isEmpty(evaluatorRecordInfos)) {
        return true;
      }
      return !evaluatorRecordInfos.stream().allMatch(EvaluatorRecordInfo::isPassFlag);
    }).toList();

    if (!notPassItemDetailInfos.isEmpty()) {
      newPage();

      Paragraph titlePass = new Paragraph("数据明细-不通过详情", openPdfContext.getFontTitle());
      titlePass.setSpacingBefore(10f);
      titlePass.setSpacingAfter(10f);
      addElement(titlePass);
    }

    for (int i = 0; i < notPassItemDetailInfos.size(); i++) {
      ItemDetailInfo itemDetailInfo = notPassItemDetailInfos.get(i);
      renderItemId(itemDetailInfo);
      renderEvalSet(itemDetailInfo);
      renderEvalTargetOutput(itemDetailInfo);
      renderEvaluatorRecords(itemDetailInfo);
      if (i != notPassItemDetailInfos.size() - 1) {
        addElement(Chunk.NEWLINE);
        addElement(new LineSeparator());
      }
    }

    if (!passItemDetailInfos.isEmpty()) {
      newPage();

      Paragraph titleNotPass = new Paragraph("数据明细-通过详情", openPdfContext.getFontTitle());
      titleNotPass.setSpacingBefore(10f);
      titleNotPass.setSpacingAfter(10f);
      addElement(titleNotPass);
    }

    for (int i = 0; i < passItemDetailInfos.size(); i++) {
      ItemDetailInfo itemDetailInfo = passItemDetailInfos.get(i);
      renderItemId(itemDetailInfo);
      renderEvalSet(itemDetailInfo);
      renderEvalTargetOutput(itemDetailInfo);
      renderEvaluatorRecords(itemDetailInfo);
      if (i != passItemDetailInfos.size() - 1) {
        addElement(Chunk.NEWLINE);
        addElement(new LineSeparator());
      }
    }


  }

  private void renderItemId(ItemDetailInfo itemDetailInfo) {
    Paragraph title = new Paragraph("数据项ID:" + itemDetailInfo.getItemId(), openPdfContext.getFontTitle());
    title.setSpacingBefore(10f);
    title.setSpacingAfter(10f);
    addElement(title);
  }

  private void renderEvalSet(ItemDetailInfo itemDetailInfo) {
    Paragraph title = new Paragraph("评测集数据", openPdfContext.getFontTitle());
    title.setSpacingBefore(10f);
    title.setSpacingAfter(10f);
    addElement(title);

    Map<String, String> evalSetMap = itemDetailInfo.getEvalSetMap();

    if (evalSetMap.isEmpty()) {
      Paragraph paragraph = new Paragraph("-", openPdfContext.getFontNormal());
      paragraph.setSpacingBefore(10f);
      paragraph.setSpacingAfter(10f);
      paragraph.setFirstLineIndent(2f);
      addElement(paragraph);
      return;
    }
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setSpacingBefore(10f);
    table.setWidths(new int[]{25, 75});
    for (Map.Entry<String, String> entry : evalSetMap.entrySet()) {
      String fieldName = entry.getKey();
      String fieldValue = entry.getValue();
      table.addCell(warpCell(createCell(new Phrase(fieldName, openPdfContext.getFontHeading()))));
      table.addCell(createCell(new Phrase(fieldValue, openPdfContext.getFontNormal())));
    }
    addElement(table);
  }

  private PdfPCell createCell(Phrase phrase) {
    PdfPCell cell = new PdfPCell(phrase);
    setCellStyle(cell);
    return cell;
  }

  private void renderEvalTargetOutput(ItemDetailInfo itemDetailInfo) {
    Paragraph title = new Paragraph("评测对象输出数据", openPdfContext.getFontTitle());
    title.setSpacingBefore(5f);
    title.setSpacingAfter(5f);
    addElement(title);

    Paragraph paragraph = new Paragraph(itemDetailInfo.getEvalTargetOutput(), openPdfContext.getFontNormal());
    paragraph.setSpacingBefore(5f);
    paragraph.setSpacingAfter(5f);
    addElement(paragraph);
  }

  private void renderEvaluatorRecords(ItemDetailInfo itemDetailInfo) {
    Paragraph title = new Paragraph("评估器得分", openPdfContext.getFontTitle());
    title.setSpacingBefore(10f);
    title.setSpacingAfter(10f);
    addElement(title);

    PdfPTable table = new PdfPTable(4);
    table.setWidthPercentage(100);
    table.setSpacingBefore(5f);
    table.setHeaderRows(1);
    table.setWidths(new int[]{25, 10, 50, 15});

    Font fontPass = new Font(openPdfContext.getBaseFont(), 12, Font.NORMAL, Color.GREEN);
    Font fontNotPass = new Font(openPdfContext.getBaseFont(), 12, Font.NORMAL, Color.RED);

    table.addCell(warpCell(createCell(new Phrase("评估器", openPdfContext.getFontHeading()))));
    table.addCell(warpCell(createCell(new Phrase("得分", openPdfContext.getFontHeading()))));
    table.addCell(warpCell(createCell(new Phrase("得分理由", openPdfContext.getFontHeading()))));
    table.addCell(warpCell(createCell(new Phrase("是否通过", openPdfContext.getFontHeading()))));
    List<EvaluatorRecordInfo> evaluatorRecords = itemDetailInfo.getEvaluatorRecordInfos();
    for (EvaluatorRecordInfo evaluatorRecord : evaluatorRecords) {
      String evaluator = evaluatorRecord.getEvaluator();
      Double score = evaluatorRecord.getScore();
      String reason = evaluatorRecord.getReason();
      boolean passFlag = evaluatorRecord.isPassFlag();
      table.addCell(createCell(new Phrase(evaluator, openPdfContext.getFontNormal())));
      table.addCell(createCell(new Phrase(score == null ? "-" : String.valueOf(score), openPdfContext.getFontNormal())));
      table.addCell(createCell(new Phrase(reason, openPdfContext.getFontNormal())));
      if (passFlag) {
        table.addCell(createCell(new Phrase("通过", fontPass)));
      }
      else {
        table.addCell(createCell(new Phrase("不通过", fontNotPass)));
      }
    }
    addElement(table);
  }
}
