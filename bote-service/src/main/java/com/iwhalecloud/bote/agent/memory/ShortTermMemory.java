package com.iwhalecloud.bote.agent.memory;

import com.iwhalecloud.bote.dto.agent.MessageMetadata;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 短期记忆（用作调用大模型时的历史消息）
 *
 * @author bianjp
 * @since 2026-03-05
 */
public interface ShortTermMemory {
  /**
   * 构造用户消息
   *
   * @param content 消息内容
   * @param fileIds 文件 ID 列表
   * @param params 参数（页面提交数据）
   * @return 用户消息
   */
  default UserMessage buildUserMessage(@Nullable String content, @Nullable List<Long> fileIds, @Nullable Map<String, Object> params) {
    return new UserMessage(buildUserPrompt(content, fileIds, params));
  }

  /**
   * 构造用户提示词
   *
   * @param content 消息内容
   * @param fileIds 文件 ID 列表
   * @param params 参数（页面提交数据）
   * @return 用户提示词
   */
  default String buildUserPrompt(@Nullable String content, @Nullable List<Long> fileIds, @Nullable Map<String, Object> params) {
    List<String> parts = new ArrayList<>(3);
    // 文本消息
    if (StringUtils.isNotBlank(content)) {
      parts.add(content.trim());
    }
    // 页面数据
    if (MapUtils.isNotEmpty(params)) {
      parts.add((StringUtils.isNotBlank(content) ? "提交的数据: " : "") + JsonUtil.toJsonString(params));
    }
    // 上传的文件
    if (CollectionUtils.isNotEmpty(fileIds)) {
      List<FileInfoVO> files = SpringUtil.getBean(IFileStoreService.class).getFileInfoByIds(fileIds);
      if (CollectionUtils.isNotEmpty(files)) {
        StringBuilder sb = new StringBuilder();
        sb.append("上传的文件:");
        for (FileInfoVO fileInfo : files) {
          sb.append("\n- file_id: ").append(fileInfo.getFileId()).append(", file_name: ").append(fileInfo.getFileName()).append(", file_size: ");
          if (fileInfo.getFileSize() != null && fileInfo.getFileSize() > 0) {
            sb.append(FileUtils.byteCountToDisplaySize(fileInfo.getFileSize()));
          }
          else {
            sb.append("unknown");
          }
        }
        parts.add(sb.toString());
      }
    }
    return String.join("\n\n", parts);
  }

  /**
   * 获取消息列表
   *
   * @return 消息列表
   */
  List<Message> getMessages();

  /**
   * 获取工具调用列表
   *
   * @return 工具调用列表
   */
  List<ToolCall> getToolCalls();

  /**
   * 添加消息
   *
   * @param message 消息
   * @param metadata 元数据
   */
  void addMessage(Message message, @Nullable MessageMetadata metadata);

  /**
   * 按需压缩消息列表
   */
  void compressIfNecessary();
}
