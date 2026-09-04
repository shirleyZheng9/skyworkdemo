package com.iwhalecloud.bote.dto.knowledge.access.platform.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识中台返回结果
 *
 * @author lxs
 * @since 2025/7/12
 */
@Data
public class ResultResponse<T> {

  @Schema(description = "结果编码：0：成功，其他失败")
  private String code;

  @Schema(description = "结果描述")
  private String message;

  @Schema(description = "结果数据")
  private T data;

  @Schema(description = "分页总记录数")
  private Integer totalSize;

  @Schema(description = "分页总页数")
  private Integer totalPage;

  @Schema(description = "分页是否有下一页")
  private boolean nextPage;

  /**
   * 是否成功
   */
  public boolean isSuccess() {
    return "0".equals(this.code);
  }
}
