package com.iwhalecloud.bote.dto.app.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网页应用访问记录查询参数
 *
 * @author tingyun.wang
 * @since 2025-09-23
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "网页应用访问记录查询参数")
public class WebAppRecordQueryParams extends PagingQueryParams {

  @Schema(description = "用户ID")
  private Long userId;

}
