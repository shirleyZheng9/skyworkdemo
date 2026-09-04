package com.iwhalecloud.bote.loop.evaluation.infra.pdf;

import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.OpenPdfData;
import lombok.Getter;
import lombok.Setter;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.BaseFont;

import java.awt.Color;
import java.io.IOException;

@Getter
@Setter
public class OpenPdfContext {
  private BaseFont baseFont;
  private OpenPdfData openPdfData;
  private Font fontHeading;
  private Font fontNormal;
  private Font fontSmall;
  private Font fontTitle;

  public void initFont() throws IOException {
    baseFont = BaseFont.createFont(
      "font/simhei.ttf",  // resources/font/simhei.ttf
      BaseFont.IDENTITY_H,
      BaseFont.EMBEDDED
    );
    fontHeading = new Font(baseFont, 12, Font.BOLD, Color.GRAY);
    fontNormal = new Font(baseFont, 12, Font.NORMAL, Color.GRAY);
    fontSmall = new Font(baseFont, 10, Font.NORMAL, Color.GRAY);
    fontTitle = new Font(baseFont, 16, Font.NORMAL);
  }
}
