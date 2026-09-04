package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.DocChainAdapter;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainDocumentHelper;
import com.iwhalecloud.bote.service.asr.offline.AsrRecognitionFactory;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * ocr 识别辅助工具
 *
 * @author chen.linfa
 * @since 2024-08-20
 */
public final class OcrUtil {
  private OcrUtil() {
  }

  private static final Logger logger = LoggerFactory.getLogger(OcrUtil.class);

  private static final IFileStoreService service = SpringUtil.getBean(IFileStoreService.class);

  private static final AsrRecognitionFactory videoRecognitionFactory = SpringUtil.getBean(AsrRecognitionFactory.class);

  public static Object identifyWords(File file) {
    try {
      String fileType;
      try (FileInputStream fis = new FileInputStream(file)) {
        fileType = FileTypeUtil.getType(fis, FilenameUtils.getExtension(file.getName()));
      }
      if (FileTypeUtil.isPicture(fileType)) {
        return doFromImage(null, file, null);
      }
      else if (FileTypeUtil.isVideo(fileType)) {
        return doFromVideo(null, file);
      }
      else {
        return doFromDoc(null, file);
      }
    }
    catch (Exception e) {
      throw new BssException("文件 OCR 识别出现异常：", e);
    }
  }

  /**
   * 从附件中提取文字
   *
   * @param fileId 文件 ID
   * @return 提取的文字
   */
  public static Object identifyWords(Long fileId) {
    FileInfoVO fileInfo = service.getFileInfoById(fileId);
    Assert.notNull(fileInfo, "查询不到有效的文件信息");
    String fileType = fileInfo.getFileType();
    if (StringUtils.isEmpty(fileType)) {
      // 兼容文件类型为空的情况
      fileType = StringUtils.substringAfterLast(fileInfo.getFileName(), ".");
    }
    if (FileTypeUtil.isPicture(fileType)) {
      return doFromImage(fileId, null, null);
    }
    else if (FileTypeUtil.isVideo(fileType)) {
      return doFromVideo(fileInfo, null);
    }
    else {
      return doFromDoc(fileInfo.getFileId(), null);
    }
  }

  /**
   * 从文档中提取文字
   *
   * @param fileId 文件 ID
   * @param file 附件，非必填
   * @return 提取的文字
   */
  private static Object doFromDoc(@Nullable Long fileId, @Nullable File file) {
    // 如果开启聚智OCR，则走聚智文件解析
    if (SystemParameter.JUZHI2_OCR_ENABLED.getBooleanValueFromEnv()) {
      return JuzhiOcrUtil.parseFile(fileId, file);
    }
    // 上传的文件，会挂载到 docchain 预置的主题（chat_doc_inner_topic），涉及文档访问权限问题。统一由博特助手预置的 docchain 账号对接
    return SpringUtil.getBean(DocChainDocumentHelper.class).uploadAndReadDocument(CommonConsts.COPILOT_TENANT_ID, fileId, file);
  }

  /**
   * 从图片中提取文字
   *
   * @param fileId 文件 ID
   * @param file 附件，非必填
   * @return 提取的文字
   */
  public static Object doFromImage(@Nullable Long fileId, @Nullable File file, @Nullable String base64) {
    Assert.isTrue(fileId != null || file != null || StringUtils.isNotEmpty(base64), "文件 ID 和文件不能同时同时为空");
    // 如果开启聚智OCR，则走聚智文件解析
    if (SystemParameter.JUZHI2_OCR_ENABLED.getBooleanValueFromEnv()) {
      return JuzhiOcrUtil.parseImage(fileId, file);
    }
    String base64Image;
    if (file != null) {
      try {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        base64Image = Base64.encodeBase64String(fileBytes);
      }
      catch (IOException e) {
        throw new BssException(e);
      }
    }
    else if (fileId != null) {
      byte[] bytes = service.downloadFile(fileId);
      Assert.notNull(bytes, "文件内容不能为空");
      base64Image = Base64.encodeBase64String(bytes);
    }
    else {
      base64Image = base64;
    }
    // 上传的文件，会挂载到 docchain 预置的主题（chat_doc_inner_topic），涉及文档访问权限问题。统一由博特助手预置的 docchain 账号对接
    return SpringUtil.getBean(DocChainAdapter.class).ocr(CommonConsts.COPILOT_TENANT_ID, base64Image);
  }

  /**
   * 从语音中提取文字
   *
   * @param fileInfo 文件信息
   * @param file 附件，非必填
   * @return 提取的文字
   */
  public static Object doFromVideo(@Nullable FileInfoVO fileInfo, @Nullable File file) {
    Assert.isTrue(fileInfo != null || file != null, "文件 ID 和文件不能同时同时为空");
    Path tmpDir = null;
    try {
      if (file != null) {
        return asr(file);
      }
      Long fileId = fileInfo.getFileId();
      tmpDir = Files.createTempDirectory("bote-ocr-");
      File tempFile = tmpDir.resolve(fileInfo.getFileName()).toFile();
      service.downloadFile(fileId, tempFile.getAbsolutePath());
      return asr(tempFile);
    }
    catch (Exception e) {
      logger.error("Failed to identify words from video.", e);
      throw BaseErrorConstant.OCR_IDENTIFY_WORDS_ERROR.toException(e, e.getMessage());
    }
    finally {
      if (tmpDir != null) {
        FileUtils.deleteQuietly(tmpDir.toFile());
      }
    }
  }

  /**
   * 语音识别
   *
   * @param file 语音文件
   * @return 识别结果
   */
  public static String asr(File file) {
    return videoRecognitionFactory.getVideoRecognitionAdapter().recognize(file);
  }

}
