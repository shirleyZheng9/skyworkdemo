package com.iwhalecloud.bote.loop.evaluation.infra.pdf.page;

import com.iwhalecloud.bote.loop.evaluation.infra.pdf.OpenPdfContext;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.factory.OpenPdfElementFactory;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.ReportInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import lombok.Getter;
import lombok.Setter;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Getter
@Setter
public class HomePage extends AbstractPage {

  public static final String LOGO_PNG_PATH = "assets/images/logo.png";

  public HomePage(Document document, OpenPdfContext openPdfContext) {
    super(document, openPdfContext);
  }

  @Override
  public void render() {
    newPage();

    Paragraph spaceParagraph = new Paragraph();
    spaceParagraph.setSpacingBefore(60f);
    spaceParagraph.setSpacingAfter(5f);
    addElement(spaceParagraph);

    try (InputStream inputStream = new ClassPathResource(LOGO_PNG_PATH).getInputStream()) {
      byte[] imageByte = changeImageBackgroundColor(inputStream);
      Image image = Image.getInstance(imageByte);
      image.setAlignment(Element.ALIGN_CENTER);
      float maxWidth = PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin();
      image.scaleToFit(maxWidth, 100);
      addElement(image);
    }
    catch (Exception e) {
      throw new BssException("加载logo图片失败:" + e.getMessage(), e);
    }


    // 标题
    Paragraph title = new Paragraph("实验评测报告", openPdfContext.getFontTitle());
    title.setAlignment(Element.ALIGN_CENTER);
    title.setSpacingBefore(60f);
    title.setSpacingAfter(5f);
    addElement(title);

    // 细黑线
    addElement(OpenPdfElementFactory.createLineSeparator(1f));
    // 避免两条分割线重叠
    addElement(OpenPdfElementFactory.createSpaceParagraph(6f));
    // 粗黑线
    addElement(OpenPdfElementFactory.createLineSeparator(3f));

    // 报告信息表格
    ReportInfo reportInfo = openPdfContext.getOpenPdfData().getReportInfo();
    PdfPTable infoTable = createTable();
    infoTable.addCell(createCell("实验名称", openPdfContext.getFontHeading()));
    infoTable.addCell(createCell(reportInfo.getName(), openPdfContext.getFontNormal()));
    infoTable.addCell(createCell("报告时间", openPdfContext.getFontHeading()));
    infoTable.addCell(createCell(reportInfo.getDateTime(), openPdfContext.getFontNormal()));
    addElement(infoTable);

  }
  private byte[] changeImageBackgroundColor(InputStream inputStream) throws IOException {
    BufferedImage original = ImageIO.read(inputStream);
    if (original == null) {
      throw new BssException("无法解析图片输入流");
    }

    // 创建一个白色背景的图片
    BufferedImage newImage = new BufferedImage(
      original.getWidth(),
      original.getHeight(),
      BufferedImage.TYPE_INT_RGB
    );

    Graphics2D g2d = newImage.createGraphics();
    try {
      g2d.setColor(Color.WHITE);       // 背景色：白色
      g2d.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());
      g2d.drawImage(original, 0, 0, null);
      g2d.dispose();
    }
    finally {
      g2d.dispose();
    }

    // 转成 byte[] 给 OpenPDF 使用
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(newImage, "png", baos);
    return baos.toByteArray();
  }


  private PdfPTable createTable() {
    PdfPTable infoTable = new PdfPTable(2);
    infoTable.setWidthPercentage(50);
    infoTable.setHorizontalAlignment(Element.ALIGN_CENTER);
    infoTable.setWidths(new float[]{1f, 2f});
    infoTable.setSpacingBefore(20f); // 与粗线保持间距
    return infoTable;
  }

  public PdfPCell createCell(String content, Font font) {
    Paragraph paragraph = new Paragraph(content, font);
    PdfPCell cell = new PdfPCell(paragraph);
    setCellStyle(cell);
    return cell;
  }
}
