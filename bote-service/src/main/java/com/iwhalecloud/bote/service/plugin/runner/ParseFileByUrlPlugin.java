package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.OcrUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.ParseFileByUrlParams;
import com.iwhalecloud.bote.util.FileUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * 根据地址解析内容
 *
 * @author chen.linfa
 * @since 2025-11-14
 */
@Component
public class ParseFileByUrlPlugin extends AbstractPlugin<ParseFileByUrlParams> {
  private static final Logger logger = LoggerFactory.getLogger(ParseFileByUrlPlugin.class);

  public ParseFileByUrlPlugin() {
    super(ParseFileByUrlParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_PARSE_FILE_BY_URL;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("url", "文件url", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("content", "内容", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(ParseFileByUrlParams params) {
    Assert.hasLength(params.getUrl(), "文件链接不能为空");
    Assert.isTrue(HttpUtil.isValid(params.getUrl()), "文件链接不合法");
  }

  @Override
  public Object doRun(ParseFileByUrlParams pluginParams) {
    URI url = URI.create(pluginParams.getUrl());
    try {
      return HttpUtil.getRestTemplate().execute(url, HttpMethod.GET, null, response -> {
        // 获取文件名称
        String fileName = FileUtil.getFileName(response, url);
        // 获取文件类型
        String fileType = FileUtil.getFileExtension(response.getHeaders().getContentType(), fileName);
        Assert.isTrue(fileType != null, "获取不到文件类型");
        String fileTypes = BaseSystemParameter.ALLOW_DOCCHAIN_UPLOAD_FILE_TYPE.getValueFromDb();
        String[] allowFileTypes = fileTypes.split(",");
        // 检查文件类型是否在允许列表中
        Assert.isTrue(ArrayUtils.contains(allowFileTypes, StringUtils.lowerCase(fileType)), "非法的文件类型: " + fileType);
        Path tmpDir = null;
        try (InputStream inputStream = response.getBody()) {
          // 创建临时文件用于 OCR 识别
          tmpDir = Files.createTempDirectory("bote-parse-file-");
          String tempFileName = fileName != null ? fileName : "temp." + fileType;
          File tempFile = tmpDir.resolve(tempFileName).toFile();
          // 将输入流写入临时文件
          try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            IOUtils.copy(inputStream, fos);
          }
          // 调用 OCR 识别
          Map<String, Object> result = new HashMap<>();
          result.put("content", OcrUtil.identifyWords(tempFile));
          return result;
        }
        catch (IOException e) {
          logger.error("Failed to read file content: url={}", url, e);
          throw new BssException("读取文件内容失败: " + ExpUtil.getMsg(e), e);
        }
        finally {
          // 清理临时文件
          if (tmpDir != null) {
            FileUtils.deleteQuietly(tmpDir.toFile());
          }
        }
      });
    }
    catch (IllegalArgumentException | BssException e) {
      throw e;
    }
    catch (HttpStatusCodeException e) {
      int status = e.getStatusCode().value();
      String body = e.getResponseBodyAsString();
      logger.warn("Failed to download file by url: url={}, status={}, body={}", url, status, body);
      throw new BssException("文件下载失败，HTTP 状态码: " + status + "，响应内容: " + body, e);
    }
    catch (Exception e) {
      logger.warn("Failed to download file by url: url={}", url, e);
      throw new BssException("文件下载失败: " + ExpUtil.getMsg(e), e);
    }
  }
}
