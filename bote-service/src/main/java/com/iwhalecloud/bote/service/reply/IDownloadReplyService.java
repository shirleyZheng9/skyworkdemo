package com.iwhalecloud.bote.service.reply;

import com.iwhalecloud.bote.dto.chat.ReplyDownloadInfoDTO;
import java.io.IOException;
import java.io.OutputStream;
import org.springframework.lang.Nullable;

/**
 * 下载回复服务
 *
 * @author zyt
 * @since 2025-05-14
 */
public interface IDownloadReplyService {

  /**
   * 获取回复下载信息
   *
   * @param tenantId 租户 ID
   * @param msgId 消息 ID
   */
  ReplyDownloadInfoDTO getDownloadInfo(Long tenantId, String msgId);

  /**
   * 根据 Markdown 内容生成 Excel 文件
   *
   * <p>提取 Markdown 中的第一个表格，如果没有表格则生成空文件</p>
   *
   * @param markdownContent Markdown 文本
   * @param outputStream 输出流
   */
  void generateExcelByMarkdown(String markdownContent, OutputStream outputStream) throws IOException;

  /**
   * 根据 JSON 字符串生成 Excel 文件
   *
   * @param json JSON 字符串，需要包含一个 list 元素，表示列表数据
   * @param outputStream 输出流
   */
  void generateExcelByJson(String json, OutputStream outputStream) throws IOException;

  /**
   * 根据 Markdown 内容生成 Word 文件
   *
   * @param tenantId 租户 ID, 用于获取 DocChain 图片
   * @param markdownContent Markdown 文本
   * @param outputStream 输出流
   */
  void generateDocxByMarkdown(@Nullable Long tenantId, String markdownContent, OutputStream outputStream) throws IOException;

}
