package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.session.RowBounds;

/**
 * 分页查询参数
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Getter
@Setter
@ToString
public abstract class PagingQueryParams {
  @Schema(description = "页数，默认 1")
  protected Integer pageNum;
  @Schema(description = "每页数量，默认 20")
  protected Integer pageSize;

  /**
   * 构造分页参数
   */
  public RowBounds buildRowBounds() {
    int num = pageNum == null ? 1 : pageNum;
    int size = pageSize == null ? 20 : pageSize;
    return new RowBounds((num - 1) * size, size);
  }
}
