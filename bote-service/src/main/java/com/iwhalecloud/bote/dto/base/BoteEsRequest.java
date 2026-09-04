package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.common.enums.BoteEsDocumentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ElasticSearch检索请求结果
 *
 * @author lizuyin
 * @since 2025-06-10
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "ElasticSearch检索请求参数")
public class BoteEsRequest {

  @Schema(description = "目标检索字段（范围）")
  private String field;

  @Schema(description = "目标检索词（关键字）", requiredMode = Schema.RequiredMode.REQUIRED)
  private String keyword;

  @Schema(description = "document归属者", requiredMode = Schema.RequiredMode.REQUIRED)
  private String ownerId;

  @Schema(description = "document归属者类型", requiredMode = Schema.RequiredMode.REQUIRED)
  private String ownerType;

  @Schema(description = "document归属租户", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long tenantId;

  @Schema(description = "document功能类别")
  private String type;

  @Schema(description = "匹配阈值，优先级：接口入参 > 缓存配置 > 默认值(0.7)")
  private Double score;

  @Schema(description = "匹配条数限制，优先级：接口入参 > 缓存配置 > 默认值(10)")
  private Integer limit;

  /**
   * 校验当前对象中的 type 是否合法。
   *
   * @return 返回当前对象，支持链式调用
   */
  public String getValidType() {
    return BoteEsDocumentTypeEnum.getMatchingType(type);
  }


}
