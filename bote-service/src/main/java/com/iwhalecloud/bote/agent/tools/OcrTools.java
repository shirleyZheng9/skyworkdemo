package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.common.util.OcrUtil;
import org.springframework.util.Assert;

/**
 * OCR 识别工具。
 * <p>
 * 基于 {@link OcrUtil#identifyWords(Long)}，对已上传文件执行 OCR/ASR 文本提取，
 * 支持图片、视频和文档类文件。
 * </p>
 *
 * @author chen.linfa
 * @since 2026-04-01
 */
public final class OcrTools {

  private OcrTools() {
  }

  @Tool(
    name = "ocr_identify_words",
    description = "Extract text from an uploaded file via OCR/ASR. Use this for images, videos, or documents when you need textual content. Parameters: fileId (required)."
  )
  public static Object identifyWords(@ToolParam String fileId) {
    Assert.hasLength(fileId, "Error: fileId is required");
    long id;
    try {
      id = Long.parseLong(fileId);
    }
    catch (RuntimeException e) {
      throw new ToolExecutionException("Error: fileId must be a integer", e);
    }

    return OcrUtil.identifyWords(id);
  }

}
