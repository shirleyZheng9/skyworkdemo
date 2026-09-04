package com.iwhalecloud.bote.loop.data.pkg.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 分页器
 * 迁移对应关系: Go语言pagination.Paginator
 * - 功能: 支持分页查询，默认按 `order by id desc limit 10` 排序
 * - 字段定义: 分页配置和结果
 * <p>
 * Java实现说明:
 * - 对应Go的pagination.Paginator结构体
 * - 使用Java类定义，包含分页器字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go基本类型 -> Java基本类型
 * - Go函数选项模式 -> Java函数式接口
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Paginator {
  private String timeColumn;
  private String idColumn;
  private Boolean asc;
  private Integer limit;
  private Integer offset;
  private String rawCursor;
  private Exception error;
  private Cursor cursor;
  private PageResult result;
  // 分页兼容处理
  private Integer pageSize;
  private Integer pageNum;

  public RowBounds buildRowBounds() {
    int num = pageNum == null ? 1 : pageNum;
    int size = pageSize == null ? 20 : pageSize;
    return new RowBounds((num - 1) * size, size);
  }

  public String orderBy() {
    String suffix = " desc";
    if (Boolean.TRUE.equals(asc)) {
      suffix = " asc";
    }
    List<String> orderBys = Lists.newArrayList();
    if (StringUtils.isNotBlank(timeColumn)) {
      orderBys.add(timeColumn + suffix);
    }
    if (StringUtils.isNotBlank(idColumn)) {
      orderBys.add(idColumn + suffix);
    }
    return String.join(", ", orderBys);
  }
}
