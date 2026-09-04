package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * ElasticSearch检索响应
 *
 * @author lizuyin
 * @since 2025-06-10
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BoteEsResponse {

  @Schema(description = "文档数据")
  private BoteEsDocument source;

  @Schema(description = "文档内容")
  private List<String> highlight;

  @Schema(description = "相关性分数")
  private Double score;

  /**
   * 转换为文档对象
   */
  public static BoteEsDocument convert(Map<String, String> map) {
    BoteEsDocument document = new BoteEsDocument();

    document.setOwnerType(map.get("ownerType"));
    document.setOwnerId(map.get("ownerId"));
    document.setContent(map.get("content"));

    return document;
  }

}
