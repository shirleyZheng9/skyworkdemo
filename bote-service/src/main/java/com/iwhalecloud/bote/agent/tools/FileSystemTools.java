package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.common.enums.SandboxMode;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileWriteResult;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 文件系统工具
 *
 * @author bianjp
 * @since 2026-02-03
 */
@SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
public final class FileSystemTools {
  private static final Logger logger = LoggerFactory.getLogger(FileSystemTools.class);
  private static final IFileStoreService fileStoreService = SpringUtil.getBean(IFileStoreService.class);

  /** 常见文本文件扩展名 */
  private static final Set<String> COMMON_TEXT_FILE_EXTENSIONS = Set.of(
    "txt", "md", "log",
    "json", "xml", "yml", "yaml", "properties", "conf", "ini", "env",
    "java", "js", "ts", "jsx", "tsx", "html", "css", "py", "sh", "sql",
    "csv", "tsv"
  );
  /** 常见二进制文件扩展名 */
  private static final Set<String> COMMON_BINARY_FILE_EXTENSIONS = Set.of(
    "png", "jpg", "jpeg", "gif", "bmp", "ico", "webp",
    "mp3", "wav", "flac",
    "mp4", "mov", "mkv", "avi",
    "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
    "zip", "rar", "7z", "tar", "gz", "xz", "iso",
    "bin",
    "exe"
  );

  private FileSystemTools() {
  }

  /**
   * 读取文件内容
   */
  @Tool(
    name = "read_file",
    description = """
      Read a file. Supports only text files.

      Usage:
      - By default, it reads up to 2000 lines starting from the beginning of the file
      - Any lines longer than 2000 characters will be truncated
      - Results are returned using cat -n format, with line numbers starting at 1
      """)
  public static String readFile(@ToolParam(description = "The absolute path of the file to read") String filePath,
                                @ToolParam(description = "The line number to start reading from. Only provide if the file is too large to read at once") @Nullable Integer offset,
                                @ToolParam(description = "The number of lines to read. Only provide if the file is too large to read at once") @Nullable Integer limit,
                                ToolContext toolContext) {
    try {
      Assert.hasLength(filePath, "Error: filePath is required");
      int startLine = offset != null ? Math.max(offset, 1) : 1;
      int maxLines = limit != null ? limit : 2000;

      // 使用 WebSocket 模式时在客户端执行
      if (toolContext.sandboxMode() == SandboxMode.CLIENT) {
        // 不支持二进制文件
        if (Boolean.TRUE.equals(isBinaryFileByExtension(filePath))) {
          throw new ToolExecutionException("Error: Only text files are supported");
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("filePath", filePath);
        params.put("offset", offset);
        params.put("limit", limit);
        return toolContext.webSocketChatContext().invokeTool("read_file", params);
      }

      // 不支持二进制文件
      if (isBinaryFile(filePath, () -> getInputStream(toolContext, filePath))) {
        throw new ToolExecutionException("Error: Only text files are supported");
      }

      try (InputStream inputStream = getInputStream(toolContext, filePath);
           BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        return readFileFromReader(filePath, startLine, maxLines, reader);
      }
    }
    catch (IllegalArgumentException e) {
      throw new ToolExecutionException(e.getMessage(), e);
    }
    catch (ToolExecutionException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ToolExecutionException("Error reading file: " + e.getMessage(), e);
    }
  }

  /**
   * 获取文件输入流
   */
  private static InputStream getInputStream(ToolContext toolContext, String filePath) throws IOException {
    // 沙箱模式
    if (toolContext.sandboxMode() == SandboxMode.REMOTE) {
      return toolContext.sandboxClient().readFileStream(filePath);
    }

    // 本地文件系统读取
    File file = new File(filePath);
    Assert.isTrue(file.exists(), () -> "Error: File does not exist: " + filePath);
    Assert.isTrue(!file.isDirectory(), () -> "Error: Path is a directory, not a file: " + filePath);
    return Files.newInputStream(file.toPath());
  }

  /**
   * 根据扩展名检查是否是二进制文件
   *
   * @return 是否是二进制文件。无法确定时返回 null
   */
  @Nullable
  @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
  private static Boolean isBinaryFileByExtension(String filePath) {
    String extension = StringUtils.lowerCase(FilenameUtils.getExtension(filePath));
    if (COMMON_TEXT_FILE_EXTENSIONS.contains(extension)) {
      return false;
    }
    if (COMMON_BINARY_FILE_EXTENSIONS.contains(extension)) {
      return true;
    }
    return null;
  }

  /**
   * 检查是否为二进制文件
   */
  private static boolean isBinaryFile(String filePath, Callable<InputStream> inputStreamSupplier) {
    // 先检查常见扩展名，开销更小
    Boolean isBinary = isBinaryFileByExtension(filePath);
    if (isBinary != null) {
      return isBinary;
    }

    // 检查前 8192 个字节是否包含 NULL 字节。文本文件不包含，二进制文件通常包含
    try (InputStream inputStream = inputStreamSupplier.call()) {
      byte[] buffer = new byte[8192];
      int bytesRead = inputStream.read(buffer);
      if (bytesRead > 0) {
        for (int i = 0; i < bytesRead; i++) {
          if (buffer[i] == 0) {
            return true;
          }
        }
      }
      return false;
    }
    catch (Exception e) {
      logger.error("Failed to detect file type: {}", filePath, e);
      throw new ToolExecutionException("Error: failed to detect file type: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 按行读取并分页（公共实现，供本地与沙箱复用）
   */
  private static String readFileFromReader(String filePath, int startLine, int maxLines, BufferedReader reader)
    throws IOException {
    List<String> lines = new ArrayList<>();
    int totalLines = 0;
    String line;
    while ((line = reader.readLine()) != null) {
      totalLines++;
      if (totalLines < startLine) {
        continue;
      }
      if (lines.size() >= maxLines) {
        continue;
      }
      if (line.length() > 2000) {
        line = line.substring(0, 2000) + "... (line truncated)";
      }
      lines.add(String.format("%6d\t%s", totalLines, line));
    }
    if (lines.isEmpty()) {
      if (totalLines == 0) {
        throw new ToolExecutionException("File is empty: " + filePath);
      }
      else {
        throw new ToolExecutionException(String.format("No lines to read. File has %d lines, but offset was %d", totalLines, startLine));
      }
    }
    StringBuilder result = new StringBuilder();
    result.append("File: ").append(filePath).append("\n")
      .append("Showing lines ").append(startLine).append("-").append(startLine + lines.size() - 1)
      .append(" of ").append(totalLines).append("\n\n");
    for (String l : lines) {
      result.append(l).append("\n");
    }
    return result.toString();
  }

  /**
   * 根据文件 ID 读取文件内容
   */
  @Tool(
    name = "read_file_by_file_id",
    description = """
      Read a file by fileId. Supports only text files.

      Usage:
      - By default, it reads up to 2000 lines starting from the beginning of the file
      - Any lines longer than 2000 characters will be truncated
      - Results are returned using cat -n format, with line numbers starting at 1
      """)
  public static String readFileByFileId(@ToolParam String fileId,
                                        @ToolParam(description = "The line number to start reading from. Only provide if the file is too large to read at once") @Nullable Integer offset,
                                        @ToolParam(description = "The number of lines to read. Only provide if the file is too large to read at once") @Nullable Integer limit) {
    Assert.notNull(fileId, "Error: fileId is required");
    Long id = parseFileId(fileId);
    FileInfoVO fileInfo = fileStoreService.getFileInfoById(id);
    Assert.notNull(fileInfo, "Error: File not exist");

    // 不支持二进制文件
    if (isBinaryFile(fileInfo.getFileName(), () -> fileStoreService.downloadFileStream(id))) {
      throw new ToolExecutionException("Error: Only text files are supported. file: " + fileInfo.getFileName());
    }

    int startLine = offset != null ? Math.max(offset, 1) : 1;
    int maxLines = limit != null ? limit : 2000;
    try (InputStream inputStream = fileStoreService.downloadFileStream(id);
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return readFileFromReader(fileId, startLine, maxLines, reader);
    }
    catch (ToolExecutionException e) {
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to read file by file_id: fileId={}", fileId, e);
      throw new ToolExecutionException("Error: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 写文件
   */
  @Tool(name = "write_file", description = "Create or overwrite a file. Must ensure parent directory exists before creating new files")
  public static String writeFile(@ToolParam(description = "The absolute path of the file to write") String filePath,
                                 @ToolParam(description = "The content to write to the file") String content,
                                 ToolContext toolContext) {
    try {
      Assert.hasLength(filePath, "Error: filePath is required");

      // 使用 WebSocket 模式时在客户端执行
      if (toolContext.sandboxMode() == SandboxMode.CLIENT) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("filePath", filePath);
        params.put("content", content);
        return toolContext.webSocketChatContext().invokeTool("write_file", params);
      }

      // 沙箱模式：只写入沙箱文件系统，不访问本地文件
      if (toolContext.sandboxMode() == SandboxMode.REMOTE) {
        SandboxFileWriteResult result = toolContext.sandboxClient().writeFile(filePath, content);
        if (!result.isSuccess()) {
          throw new ToolExecutionException("Error writing file: " + result.getErrorMessage());
        }
        return String.format("Successfully wrote file: %s (%d bytes)", filePath, content.length());
      }

      // 本地写入
      Path path = Paths.get(filePath);
      File file = path.toFile();
      boolean fileExists = file.exists();
      FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
      if (fileExists) {
        return String.format("Successfully overwrote file: %s (%d bytes)", filePath, content.length());
      }
      else {
        return String.format("Successfully created file: %s (%d bytes)", filePath, content.length());
      }
    }
    catch (IllegalArgumentException e) {
      throw new ToolExecutionException(e.getMessage(), e);
    }
    catch (Exception e) {
      throw new ToolExecutionException("Error writing file: " + e.getMessage(), e);
    }
  }

  /**
   * 修改文件内容
   */
  @Tool(name = "edit_file", description = "Perform exact find-and-replace on a file")
  public static String editFile(@ToolParam(description = "The absolute path of the file to modify") String filePath,
                                @ToolParam(description = "The exact text to replace") String oldString,
                                @ToolParam(description = "The text to replace it with (must be different from oldString)") String newString,
                                @ToolParam(description = "Replace all occurrences of oldString (default false)") @Nullable Boolean replaceAll,
                                ToolContext toolContext) { // @formatter:on
    try {
      Assert.hasLength(filePath, "Error: filePath is required");
      Assert.hasLength(oldString, "Error: oldString is required");
      Assert.hasLength(newString, "Error: newString is required");
      Assert.isTrue(!oldString.equals(newString), "Error: oldString and newString must be different");

      // 使用 WebSocket 模式时在客户端执行
      if (toolContext.sandboxMode() == SandboxMode.CLIENT) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("filePath", filePath);
        params.put("oldString", oldString);
        params.put("newString", newString);
        params.put("replaceAll", replaceAll);
        return toolContext.webSocketChatContext().invokeTool("edit_file", params);
      }

      String originalContent;
      try (InputStream inputStream = getInputStream(toolContext, filePath)) {
        originalContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      }
      Assert.hasLength(originalContent, "Error: file is empty");
      int occurrences = StringUtils.countMatches(originalContent, oldString);
      Assert.isTrue(occurrences > 0, () -> "Error: oldString not found in file: " + filePath);
      Assert.isTrue(Boolean.TRUE.equals(replaceAll) || occurrences == 1, () -> String.format("Error: oldString appears %d times in the file. Either provide a larger string with more surrounding context to make it unique or use replaceAll=true to change all instances.", occurrences));
      String newContent;
      if (Boolean.TRUE.equals(replaceAll)) {
        newContent = Strings.CS.replace(originalContent, oldString, newString);
      }
      else {
        newContent = Strings.CS.replaceOnce(originalContent, oldString, newString);
      }
      writeFile(filePath, newContent, toolContext);
      String snippet = generateEditSnippet(newContent, newString);
      return String.format("The file %s has been updated. Here's the result of running `cat -n` on a snippet of the edited file:\n%s", filePath, snippet);
    }
    catch (IllegalArgumentException e) {
      throw new ToolExecutionException(e.getMessage(), e);
    }
    catch (ToolExecutionException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ToolExecutionException("Error editing file: " + e.getMessage(), e);
    }
  }

  /**
   * 获取文件下载地址。沙箱模式下从沙箱内用二进制接口读取文件后上传，支持非文本文件。
   */
  @Tool(name = "get_download_url", description = "Get the relative download URL for a file by filePath or fileId. The returned URL must be used exactly as provided without any modification or domain prefixing")
  public static String getDownloadUrl(@ToolParam(description = "The absolute path of the file to upload; mutually exclusive with fileId") @Nullable String filePath,
                                      @ToolParam(description = "Existing fileId; mutually exclusive with filePath") @Nullable String fileId,
                                      ToolContext toolContext) {
    // TODO 校验文件路径，避免任意文件下载漏洞
    // TODO 上传时需要记录一些上下文信息(tenant_id, user_id, session_id)
    // TODO 需要考虑自动清理文件
    boolean hasPath = StringUtils.isNotEmpty(filePath);
    boolean hasId = StringUtils.isNotEmpty(fileId);
    if (hasPath && hasId) {
      throw new ToolExecutionException("Error: provide only one of filePath or fileId, not both");
    }
    if (!hasPath && !hasId) {
      throw new ToolExecutionException("Error: exactly one of filePath or fileId is required");
    }
    if (hasId) {
      Long id = parseFileId(fileId);
      FileInfoVO fileInfo = fileStoreService.getFileInfoById(id);
      Assert.notNull(fileInfo, "Error: File not exist");
      return "api/bote/file/file/id/" + fileInfo.getFileId();
    }
    FileInfoVO fileInfo = doUploadFile(filePath, toolContext);
    return "api/bote/file/file/id/" + fileInfo.getFileId();
  }

  /**
   * 上传文件到服务端存储并返回文件 ID（字符串，与 read_file_by_file_id 等工具的 fileId 参数一致）
   */
  @Tool(name = "upload_file", description = "Upload a file to file server. Returns the fileId")
  public static String uploadFile(@ToolParam(description = "The absolute path of the file to upload") String filePath,
                                  ToolContext toolContext) {
    Assert.hasLength(filePath, "Error: filePath is required");
    if (toolContext.sandboxMode() == SandboxMode.CLIENT) {
      Map<String, Object> params = Map.of("filePath", filePath);
      return toolContext.webSocketChatContext().invokeTool("upload_file", params);
    }
    FileInfoVO fileInfo = doUploadFile(filePath, toolContext);
    return "fileId: " + fileInfo.getFileId();
  }

  /**
   * 上传文件到文件服务器
   */
  private static FileInfoVO doUploadFile(String filePath, ToolContext toolContext) {
    if (toolContext.sandboxMode() == SandboxMode.REMOTE) {
      try (InputStream inputStream = toolContext.sandboxClient().readFileStream(filePath)) {
        UploadConfigVO uploadConfig = buildAgentSkillUploadConfig(FilenameUtils.getName(filePath), null);
        return fileStoreService.uploadFile(inputStream, uploadConfig);
      }
      catch (Exception e) {
        throw new ToolExecutionException("Error uploading file: " + e.getMessage(), e);
      }
    }

    File file = new File(filePath);
    if (!file.exists()) {
      throw new ToolExecutionException("Error: file not exist: " + filePath);
    }
    UploadConfigVO uploadConfig = buildAgentSkillUploadConfig(file.getName(), file.length());
    return fileStoreService.uploadFile(file, uploadConfig);
  }

  /**
   * 构造上传配置
   */
  private static UploadConfigVO buildAgentSkillUploadConfig(String originalFileName, @Nullable Long fileSize) {
    UploadConfigVO uploadConfig = new UploadConfigVO();
    uploadConfig.setSubFolder("agent-generated");
    uploadConfig.setOriginalFileName(originalFileName);
    uploadConfig.setFileSize(fileSize);
    uploadConfig.setFileType(FilenameUtils.getExtension(originalFileName));
    return uploadConfig;
  }

  /**
   * 下载文件到本地
   */
  @Tool(name = "download_file_by_file_id", description = "Download a file by fileId")
  public static String downloadFileByFileId(@ToolParam String fileId,
                                            @ToolParam(description = "The absolute path to save the file, including filename. Avoid Chinese, spaces, special chars. Use [a-z0-9_-]") String savePath,
                                            ToolContext toolContext) {
    Long id = parseFileId(fileId);
    Assert.hasLength(savePath, "Error: savePath is required");

    // 使用 WebSocket 模式时在客户端执行
    if (toolContext.sandboxMode() == SandboxMode.CLIENT) {
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("fileId", fileId);
      params.put("savePath", savePath);
      return toolContext.webSocketChatContext().invokeTool("download_file_by_file_id", params);
    }

    FileInfoVO fileInfo = fileStoreService.getFileInfoById(id);
    Assert.notNull(fileInfo, "Error: File not exist");

    try {
      if (toolContext.sandboxMode() == SandboxMode.REMOTE) {
        try (InputStream inputStream = fileStoreService.downloadFileStream(id)) {
          SandboxFileWriteResult result = toolContext.sandboxClient().writeFile(savePath, inputStream);
          if (!result.isSuccess()) {
            throw new ToolExecutionException("Error downloading file: " + result.getErrorMessage());
          }
        }
      }
      else {
        fileStoreService.downloadFile(id, savePath);
      }
    }
    catch (Exception e) {
      logger.error("Failed to download file: fileId={}, savePath={}", fileId, savePath, e);
      throw new ToolExecutionException("Error downloading file: " + e.getMessage(), e);
    }
    return "File downloaded to: " + savePath;
  }

  /**
   * 解析文件 ID
   *
   * <p>有些模型（比如 MiniMax-M2.5）不能正确处理长整数（可能是用 node.js 开发的），因此工具参数中将 fileId 定义为字符串类型以避免精度问题</p>
   */
  private static Long parseFileId(@Nullable String fileId) {
    Assert.hasLength(fileId, "Error: fileId is required");
    try {
      return Long.parseLong(fileId);
    }
    catch (RuntimeException e) {
      throw new ToolExecutionException("Error: fileId must be a integer", e);
    }
  }

  /**
   * 生成编辑区域的格式化片段
   */
  private static String generateEditSnippet(String fileContent, String newString) {
    String[] lines = fileContent.split("\n", -1);
    int startPos = fileContent.indexOf(newString);
    int startLine = StringUtils.countMatches(fileContent.substring(0, startPos), '\n');
    int endLine = startLine + StringUtils.countMatches(newString, '\n');
    // 前后显示最多 5 行上下文
    int contextLineCount = 5;
    startLine = Math.max(0, startLine - contextLineCount);
    endLine = Math.min(lines.length - 1, endLine + contextLineCount);
    StringBuilder snippet = new StringBuilder();
    for (int i = startLine; i <= endLine; i++) {
      snippet.append(String.format("%6d→%s", i + 1, lines[i]));
      if (i < endLine) {
        snippet.append("\n");
      }
    }
    return snippet.toString();
  }
}
