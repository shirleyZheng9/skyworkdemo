package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.common.enums.BoteEsDocumentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** ElasticSearch文档实体类 */
@Getter
@Setter
@ToString(callSuper = true)
public class BoteEsDocument {

  @Schema(description = "文档ID")
  private String id;
  @Schema(description = "所有者类型")
  private String ownerType;
  /** 所有者ID String才能被ES默认识别为text */
  @Schema(description = "所有者ID")
  private String ownerId;
  @Schema(description = "文档内容")
  private String content;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "功能类别")
  private String type;

  @Schema(description = "ES建议字段，包含input和contexts结构")
  private BoteEsSuggest suggest;

  /**
   * 获取匹配的枚举名称（如果存在）。
   *
   * @return 匹配成功的枚举名称；否则返回 null
   */
  public String getMatchingType() {
    return BoteEsDocumentTypeEnum.getMatchingType(type);
  }
}
