package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.iwhalecloud.bote.loop.evaluation.domain.service.ExperimentOpenPdfService;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.pdf.ExptResultData;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.OpenPdfContext;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.handle.OpenPdfDataConvertor;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.OpenPdfData;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.page.AbstractPage;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.page.AggregatePage;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.page.BaseInfoPage;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.page.HomePage;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.page.ItemDetailPage;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.page.ItemPage;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.page.ScoreDistributionPage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import lombok.AllArgsConstructor;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class ExperimentOpenPdfServiceImpl implements ExperimentOpenPdfService {
  private final OpenPdfDataConvertor openPdfDataConvertor;

  @Override
  public void generatePdf(ExptResultData exptResultData, OutputStream outputStream) {
    OpenPdfData openPdfData = openPdfDataConvertor.convert(exptResultData);

    OpenPdfContext openPdfContext = new OpenPdfContext();
    try {
      openPdfContext.initFont();
    }
    catch (IOException e) {
      throw new BssException("初始化上下文字体失败:" + e.getMessage(), e);
    }
    openPdfContext.setOpenPdfData(openPdfData);

    try (Document document = new Document(PageSize.A4, 40, 40, 40, 40)) {
      PdfWriter.getInstance(document, outputStream);
      document.open();

      List<AbstractPage> pages = new ArrayList<>();
      HomePage homePage = new HomePage(document, openPdfContext);
      pages.add(homePage);
      BaseInfoPage baseInfoPage = new BaseInfoPage(document, openPdfContext);
      pages.add(baseInfoPage);
      AggregatePage aggregatePage = new AggregatePage(document, openPdfContext);
      pages.add(aggregatePage);
      ScoreDistributionPage scoreDistributionPage = new ScoreDistributionPage(document, openPdfContext);
      pages.add(scoreDistributionPage);
      ItemPage itemPage = new ItemPage(document, openPdfContext);
      pages.add(itemPage);
      ItemDetailPage itemDetailPage = new ItemDetailPage(document, openPdfContext);
      pages.add(itemDetailPage);
      for (AbstractPage page : pages) {
        page.render();
      }
    }
  }
}
