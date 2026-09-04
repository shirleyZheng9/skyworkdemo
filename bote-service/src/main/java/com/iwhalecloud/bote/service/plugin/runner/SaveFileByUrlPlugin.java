package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.SaveFileByUrlParams;
import com.iwhalecloud.bote.util.FileUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * 根据地址保存图片
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Component
public class SaveFileByUrlPlugin extends AbstractPlugin<SaveFileByUrlParams> {
  private static final Logger logger = LoggerFactory.getLogger(SaveFileByUrlPlugin.class);
  /** 最大允许下载的文件大小: 50M */
  private static final int MAX_FILE_SIZE = 50 * 1024 * 1024;
  /** 文件大小超出限制的报错信息 */
  private static final String MAX_FILE_SIZE_ERROR = "文件大小不能超过 50M";
  /** 禁止下载的文件类型 */
  private static final Set<String> DISALLOWED_FILE_TYPES = Set.of(
    // 脚本类
    "jsp", "jspx", "php", "php5", "php7", "phtml", "asp", "aspx", "ashx", "cgi", "pl", "py", "rb", "sh", "bash", "bat",
    "cmd", "exe",
    // 环境变量/敏感文件
    ".env", ".bashrc", ".bash_profile", ".profile", ".zshrc", ".cshrc", ".tcshrc", ".login", ".logout",
    // 密钥/证书
    "id_rsa", "id_dsa", "id_ecdsa", "id_ed25519", "id_rsa.pub", "id_dsa.pub", "id_ecdsa.pub", "id_ed25519.pub", "pem",
    "crt", "key", "csr", "pfx", "p12",
    // 常见敏感文件
    "passwd", "shadow", "htpasswd", ".htaccess", ".htpasswd", "hosts", "sudoers",
    // 数据库文件
    "db", "sqlite", "sqlite3", "dbf", "mdb", "accdb",
    // 其他
    "log", "bak", "backup", "old", "swp", "swo", "tmp");

  private final IFileStoreService fileStoreService;

  public SaveFileByUrlPlugin(IFileStoreService fileStoreService) {
    super(SaveFileByUrlParams.class);
    this.fileStoreService = fileStoreService;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_SAVE_FILE_BY_URL;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(
      Collections.singletonList(ParameterSpec.newProperty("url", "文件url", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING),
      ParameterSpec.newProperty("fileId", "文件Id", AttrDataType.INTEGER),
      ParameterSpec.newProperty("fileName", "文件名", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(SaveFileByUrlParams params) {
    Assert.hasLength(params.getUrl(), "文件链接不能为空");
    Assert.isTrue(HttpUtil.isValid(params.getUrl()), "文件链接不合法");
  }

  @Override
  public Object doRun(SaveFileByUrlParams pluginParams) {
    URI url = URI.create(pluginParams.getUrl());

    try {
      return HttpUtil.getRestTemplate().execute(url, HttpMethod.GET, null, response -> {
        long contentLength = response.getHeaders().getContentLength();
        Assert.isTrue(contentLength <= MAX_FILE_SIZE, MAX_FILE_SIZE_ERROR);

        // 获取文件名称
        String fileName = FileUtil.getFileName(response, url);
        // 获取文件类型
        String fileType = FileUtil.getFileExtension(response.getHeaders().getContentType(), fileName);
        Assert.isTrue(fileType != null, "获取不到文件类型");
        Assert.isTrue(!DISALLOWED_FILE_TYPES.contains(fileType), "非法的文件类型: " + fileType);

        UploadConfigVO uploadConfig = new UploadConfigVO();
        uploadConfig.setOriginalFileName(fileName);
        uploadConfig.setFileType(fileType);
        uploadConfig.setFileSize(contentLength > 0 ? contentLength : null);

        try (InputStream inputStream = response.getBody()) {
          FileInfoVO fileInfo = fileStoreService.uploadFile(inputStream, uploadConfig);
          Map<String, Object> result = new HashMap<>();
          result.put("fileId", fileInfo.getFileId());
          result.put("fileName", fileInfo.getFileName());
          result.put("message", "文件上传成功");
          return result;
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
