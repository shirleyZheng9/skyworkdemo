package com.iwhalecloud.bote.loop.evaluation.infra.pdf.page;

import com.iwhalecloud.bote.loop.evaluation.infra.pdf.OpenPdfContext;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.factory.OpenPdfElementFactory;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;

import java.awt.Color;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractPage {
  protected Document document;
  protected OpenPdfContext openPdfContext;

  protected void newPage() {
    // 新建页面
    document.newPage();
    // 页眉
    Paragraph leftParagraph = new Paragraph("博特平台智能评测", openPdfContext.getFontNormal());
    Paragraph rightParagraph = new Paragraph("内部文件 注意保密", openPdfContext.getFontSmall());
    PdfPTable pageHeader = OpenPdfElementFactory.createPageHeader(leftParagraph, rightParagraph);
    addElement(pageHeader);
    // 分割线
    addElement(OpenPdfElementFactory.createLineSeparator());
  }

  protected void addEmptyDataParagraph() {
    Paragraph emptyDataParagraph = new Paragraph("暂无数据", openPdfContext.getFontNormal());
    emptyDataParagraph.setSpacingBefore(5f);
    emptyDataParagraph.setSpacingAfter(5f);
    addElement(emptyDataParagraph);
  }

  protected PdfPCell warpCell(PdfPCell cell) {
    cell.setBackgroundColor(new Color(240, 240, 240)); // 浅灰
    return cell;
  }

  protected void setCellStyle(PdfPCell cell) {
    cell.setBorder(Rectangle.BOX);
    cell.setBorderWidth(0.1f);
    cell.setBorderColor(Color.GRAY);
    cell.setPadding(5f);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
  }

  protected void addElement(Element element) {
    try {
      document.add(element);
    }
    catch (DocumentException e) {
      throw new BssException("PDF文档添加元素失败:" + e.getMessage(), e);
    }
  }

  public abstract void render();
}
