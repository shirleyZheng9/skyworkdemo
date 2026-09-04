package com.iwhalecloud.bote.loop.evaluation.infra.pdf.factory;

import org.openpdf.text.Element;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.draw.LineSeparator;

import java.awt.Color;

public abstract class OpenPdfElementFactory {

  public static PdfPTable createPageHeader(Paragraph leftParagraph, Paragraph rightParagraph) {
    PdfPTable pageHeader = new PdfPTable(2);
    pageHeader.setWidthPercentage(100);

    PdfPCell leftCell = new PdfPCell(leftParagraph);
    leftCell.setBorder(Rectangle.NO_BORDER);
    leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    leftCell.setPaddingBottom(10f);
    leftCell.setPaddingLeft(10f);
    pageHeader.addCell(leftCell);

    PdfPCell rightCell = new PdfPCell(rightParagraph);
    rightCell.setBorder(Rectangle.NO_BORDER);
    rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    rightCell.setPaddingBottom(10f);
    rightCell.setPaddingRight(10f);
    pageHeader.addCell(rightCell);

    return pageHeader;
  }

  public static Paragraph createSpaceParagraph(float spacing) {
    Paragraph paragraph = new Paragraph();
    paragraph.setSpacingBefore(spacing);
    return paragraph;
  }

  /**
   * 默认分割线
   * */
  public static LineSeparator createLineSeparator() {
    return new LineSeparator();
  }


  /**
   * 颜色：黑色
   * 百分比：占页面40%
   * 宽度：指定分割线宽度
   * 对齐方式： 居中对齐
   * */
  public static LineSeparator createLineSeparator(float lineWidth) {
    LineSeparator lineSeparator = new LineSeparator();
    lineSeparator.setLineColor(Color.BLACK);
    lineSeparator.setLineWidth(lineWidth);
    lineSeparator.setPercentage(40);
    lineSeparator.setAlignment(Element.ALIGN_CENTER);
    return lineSeparator;
  }



}
