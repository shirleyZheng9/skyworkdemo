package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.FieldEncryptUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.SmtpEmailPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * SMTP邮件发送插件
 *
 * @author system
 * @since 2025-01-09
 */
@Component
public class SmtpEmailPlugin extends AbstractPlugin<SmtpEmailPluginParams> {
  private final IFileStoreService fileStoreService;

  public SmtpEmailPlugin(IFileStoreService fileStoreService) {
    super(SmtpEmailPluginParams.class);
    this.fileStoreService = fileStoreService;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_SMTP_EMAIL;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("host", "SMTP服务器地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("port", "SMTP端口", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("username", "发件人邮箱", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("password", "发件人密码", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("protocol", "协议（默认smtp）", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("encoding", "编码（默认UTF-8）", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("to", "收件人邮箱列表", AttrDataType.ARRAY));
    children.add(ParameterSpec.newProperty("subject", "邮件主题", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("content", "邮件内容（支持HTML）", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fileIds", "附件文件ID列表（可选），如果HTML内容中使用 cid:文件ID 引用，则作为内嵌图片", AttrDataType.ARRAY));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否发送成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "返回消息", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(SmtpEmailPluginParams params) {
    Assert.notNull(params.getHost(), "SMTP服务器地址不能为空");
    Assert.notNull(params.getPort(), "SMTP端口不能为空");
    Assert.notNull(params.getUsername(), "发件人邮箱不能为空");
    Assert.notNull(params.getPassword(), "发件人密码不能为空");
    Assert.notEmpty(params.getTo(), "收件人邮箱列表不能为空");
    Assert.notNull(params.getSubject(), "邮件主题不能为空");
    Assert.notNull(params.getContent(), "邮件内容不能为空");
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public Object doRun(SmtpEmailPluginParams pluginParams) {
    List<File> tempFileList = new ArrayList<>();
    try {
      JavaMailSender sender = getJavaMailSender(pluginParams);
      MimeMessage message = sender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
      // 设置发件人
      helper.setFrom(pluginParams.getUsername());
      // 设置收件人
      helper.setTo(pluginParams.getTo().toArray(new String[0]));
      // 设置主题
      helper.setSubject(pluginParams.getSubject());
      // 设置内容（支持HTML）
      helper.setText(pluginParams.getContent(), true);
      // 设置发送时间
      helper.setSentDate(new Date());
      // 处理文件：自动识别内嵌图片和附件
      if (pluginParams.getFileIds() != null && !pluginParams.getFileIds().isEmpty()) {
        processFiles(helper, pluginParams.getFileIds(), pluginParams.getContent(), tempFileList);
      }
      // 发送邮件
      sender.send(message);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "邮件发送成功");
      return result;
    }
    catch (Exception e) {
      logger.error("发送邮件失败: {}", e.getMessage(), e);
      Map<String, Object> result = new HashMap<>();
      result.put("success", false);
      result.put("message", "发送邮件失败: " + e.getMessage());
      return result;
    }
    finally {
      for (File tempFile : tempFileList) {
        if (tempFile != null && tempFile.exists()) {
          boolean deleted = tempFile.delete();
          if (!deleted) {
            logger.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
          }
        }
      }
    }
  }

  /**
   * 获取或创建JavaMailSender
   */
  private JavaMailSender getJavaMailSender(SmtpEmailPluginParams params) {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(params.getHost());
    mailSender.setPort(params.getPort());
    mailSender.setUsername(params.getUsername());
    // 支持密码密文传输，如果密码以 {EFP} 开头则自动解密
    String password = FieldEncryptUtil.decryptData(params.getPassword());
    mailSender.setPassword(password);
    // 设置协议，默认为smtp
    String protocol = StringUtils.isNotBlank(params.getProtocol()) ? params.getProtocol() : "smtp";
    mailSender.setProtocol(protocol);
    // 设置编码，默认为UTF-8
    String encoding = StringUtils.isNotBlank(params.getEncoding()) ? params.getEncoding() : "UTF-8";
    mailSender.setDefaultEncoding(encoding);
    // 设置邮件属性
    Properties properties = new Properties();
    properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
    // 超时设置
    properties.put("mail.smtp.connectiontimeout", "5000");
    properties.put("mail.smtp.timeout", "3000");
    properties.put("mail.smtp.writetimeout", "5000");
    mailSender.setJavaMailProperties(properties);
    return mailSender;
  }

  /**
   * 处理文件：自动识别内嵌图片和附件
   * 如果HTML内容中包含 cid:文件ID 的引用，则作为内嵌图片；否则作为附件
   */
  private void processFiles(MimeMessageHelper helper, List<Long> fileIds, String htmlContent, List<File> tempFileList) throws MessagingException {
    if (fileIds == null || fileIds.isEmpty()) {
      return;
    }

    // 检查HTML内容中是否包含 cid:文件ID 的引用
    String content = htmlContent != null ? htmlContent : "";

    for (Long fileId : fileIds) {
      if (fileId != null) {
        FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
        if (fileInfo == null) {
          throw new BssException("文件不存在,ID:" + fileId);
        }
        // 将InputStream转换为临时File
        File tempFile = null;
        try (InputStream inputStream = fileStoreService.downloadFileStream(fileInfo)) {
          // 创建临时文件
          String fileName = fileInfo.getFileName();
          // 创建临时文件，使用原始文件名
          tempFile = File.createTempFile("email_file_", fileName);
          // 将InputStream内容复制到临时文件
          Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

          // 检查HTML内容中是否包含 cid:文件ID 的引用
          String cidPattern = "cid:" + fileId;
          if (content.contains(cidPattern)) {
            // 作为内嵌图片添加
            helper.addInline(String.valueOf(fileId), tempFile);
            logger.debug("添加邮件内嵌图片: fileName={}, fileId={}", fileName, fileId);
          } else {
            // 作为附件添加
            helper.addAttachment(fileName, tempFile);
            logger.debug("添加邮件附件: fileName={}, fileId={}", fileName, fileId);
          }
          tempFileList.add(tempFile);
        }
        catch (IOException e) {
          throw new BssException("读取文件失败, fileId=" + fileId + ", error=" + e.getMessage(), e);
        }
      }
    }
  }
}
