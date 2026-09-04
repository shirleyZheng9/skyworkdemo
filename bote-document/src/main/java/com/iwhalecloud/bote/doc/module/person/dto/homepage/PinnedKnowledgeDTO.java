package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.common.utils.PinLinkedSortHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 置顶知识库
 *
 * @author yangran
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "置顶知识库")
public class PinnedKnowledgeDTO implements PinLinkedSortHelper.LinkedListNode<PinnedKnowledgeDTO> {
  @Schema(description = "置顶记录ID")
  private Long pinId;
  @Schema(description = "前一个置顶记录ID")
  private Long prevPinId;
  @Schema(description = "知识库名称")
  private String kbName;
  @Schema(description = "知识库id")
  private String kbId;
  @Schema(description = "知识库code")
  private String kbCode;
  @Schema(description = "图标")
  private String knowledgeIcon;
  @Schema(description = "图标颜色")
  private String color;
  @Schema(description = "描述")
  private String description;
  @Schema(description = "知识库类型")
  private String kbType;
  @Schema(description = "文档数量")
  private Integer documentCount;
  @Schema(description = "置顶时间")
  private Date pinTime;
  @Schema(description = "权限")
  private String permissions;

  @Schema(description = "租户ID")
  protected Long tenantId;

  @Schema(description = "企业空间ID")
  protected Long spaceId;
}
