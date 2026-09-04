package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 解析 SQL 结果传输对象
 *
 * @author qian.sisheng
 * @since 2024/8/8
 */
@Getter
@Setter
@ToString
@Schema(description = "解析 SQL 结果")
public class ParseSqlResult {
  @Schema(description = "入参报文")
  private String requestObj;
  @Schema(description = "出参报文")
  private String responseObj;
  @Schema(description = "SQL 中用到的表名集合")
  private Set<String> lowTableNameSet;
}
