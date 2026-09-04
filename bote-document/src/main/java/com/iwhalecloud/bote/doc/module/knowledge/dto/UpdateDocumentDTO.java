package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档信息
 *
 * @author qian.sisheng
 * @since 2024-09-26
 */
@Getter
@Setter
@ToString
public class UpdateDocumentDTO {
  @Schema(description = "文档ID")
  private Long docId;
  @Schema(description = "状态")
  private String status;
  @Schema(description = "错误信息")
  private String errorMessage;
  @DiffField(name = "PARSE_STARTED_TIME")
  @Schema(description = "解析开始时间")
  private Date parseStartedTime;

}
