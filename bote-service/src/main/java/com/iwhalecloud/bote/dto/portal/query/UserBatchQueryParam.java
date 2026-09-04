package com.iwhalecloud.bote.dto.portal.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 批量查询用户请求参数
 *
 * @author Aiqing
 * @since 2025/12/26
 */
@Getter
@Setter
@ToString
public class UserBatchQueryParam {

  @Schema(description = "用户ID集合")
  @NotEmpty(message = "用户ID不能为空")
  private List<Long> userIdList;
}
