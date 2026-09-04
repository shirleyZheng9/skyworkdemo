package com.iwhalecloud.bote.controller.downloadReply;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.io.FileBackedOutputStream;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.chat.ReplyDownloadInfoDTO;
import com.iwhalecloud.bote.service.reply.IDownloadReplyService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = BaseConsts.API_PREFIX)
@RequiredArgsConstructor
@Tag(name = "对话：下载对话")
public class DownloadReplyController {
  private final IDownloadReplyService downloadReplyService;

  @Operation(summary = "根据消息 ID 下载回复")
  @GetMapping("download/replyFile")
  @SuppressFBWarnings("HTTP_RESPONSE_SPLITTING")
  public void downloadReplyFile(@RequestParam("tenantId") Long tenantId,
                                @RequestParam("msgId") String msgId,
                                HttpServletResponse response) throws IOException {
    ReplyDownloadInfoDTO downloadInfo = downloadReplyService.getDownloadInfo(tenantId, msgId);
    generateFile(response, tenantId, msgId, downloadInfo.getFileType(), downloadInfo.getContent());
  }

  @Operation(summary = "根据指定内容生成文件")
  @PostMapping("download/contentToFile")
  public void contentToFile(@RequestBody ContentToFileParams params, HttpServletResponse response) throws IOException {
    String content = params.getContent();
    Assert.hasLength(content, "文件内容不能为空");
    String fileType = StringUtils.defaultIfEmpty(params.getFileType(), "word");
    Long tenantId = params.getTenantId() != null ? params.getTenantId() : TenantIdUtil.getTenantIdOptional();
    generateFile(response, tenantId, Long.toString(System.currentTimeMillis()), fileType, content);
  }

  /**
   * 生成文件
   */
  private void generateFile(HttpServletResponse response, @Nullable Long tenantId, String basename, String fileType, String content) throws IOException {
    String filename;
    String contentType;
    // 先输出到临时文件，输出成功后再拷贝到响应流，以方便处理生成文件异常的情况
    FileBackedOutputStream outputStream = new FileBackedOutputStream();
    try (outputStream) {
      if ("word".equalsIgnoreCase(fileType)) {
        filename = basename + ".docx";
        contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        downloadReplyService.generateDocxByMarkdown(tenantId, content, outputStream);
      }
      else if ("excel".equalsIgnoreCase(fileType)) {
        filename = basename + ".xlsx";
        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        // content 可以是包含 list 属性的 JSON 字符串
        if (content.startsWith("{") && content.endsWith("}") && content.contains("\"list\"")) {
          downloadReplyService.generateExcelByJson(content, outputStream);
        }
        else {
          downloadReplyService.generateExcelByMarkdown(content, outputStream);
        }
      }
      else {
        throw new IllegalArgumentException("不支持的文件下载类型: " + fileType);
      }
    }

    response.setContentType(contentType);
    response.setContentLengthLong(outputStream.size());
    // filename 目前只有 ASCII 字符，不需要指定编码，如果包含 UTF-8 字符则需要指定编码
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename).build().toString());
    try (InputStream inputStream = outputStream.inputStream()) {
      IOUtils.copy(inputStream, response.getOutputStream());
    }
  }

  @Schema(description = "根据内容生成文件请求参数")
  @Getter
  @Setter
  @ToString
  public static class ContentToFileParams {
    @Schema(description = "租户 ID")
    public Long tenantId;
    @Schema(description = "文件类型", allowableValues = {"word", "excel"}, defaultValue = "word")
    private String fileType;
    @Schema(description = "文件内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
  }
}
