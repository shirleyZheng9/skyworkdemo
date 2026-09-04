package com.iwhalecloud.bote.dto.skill;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author qian.sisheng
 * @since 2024/8/8
 */
@Getter
@Setter
@ToString
public class ParseSqlNodeResult {
  /** 解析后的 SQL (去除标签) */
  private String resolvedSql;
  /** 标签中引用到的参数名称列表 */
  private List<String> paramNames;
}
