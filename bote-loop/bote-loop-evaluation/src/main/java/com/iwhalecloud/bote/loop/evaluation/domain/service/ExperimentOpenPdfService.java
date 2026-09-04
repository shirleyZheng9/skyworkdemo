package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.pdf.ExptResultData;

import java.io.OutputStream;

public interface ExperimentOpenPdfService {

  /**
   * 生成实验结果报告
   * */
  void generatePdf(ExptResultData exptResultData, OutputStream outputStream);

}
