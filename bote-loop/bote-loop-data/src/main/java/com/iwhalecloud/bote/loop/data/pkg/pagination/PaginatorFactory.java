package com.iwhalecloud.bote.loop.data.pkg.pagination;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.OrderByDTO;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.OrderBy;
import org.springframework.util.CollectionUtils;

import java.util.List;

public final class PaginatorFactory {
  private PaginatorFactory() {

  }

  public static Paginator newPaginator(OrderBy orderBy, Integer pageSize, Integer pageNumber) {
    if (orderBy != null && (pageSize == null || pageNumber == null)) {
      return null;
    }
    Paginator paginator = new Paginator();
    paginator.setPageSize(pageSize);
    paginator.setPageNum(pageNumber);
    if (orderBy != null) {
      Boolean isAsc = orderBy.getIsAsc();
      String field = orderBy.getField();
      paginator.setAsc(Boolean.TRUE.equals(isAsc));
      paginator.setIdColumn("id");
      paginator.setTimeColumn(field);
    }
    return paginator;
  }

  public static Paginator newPaginator(List<OrderByDTO> orderBys, Integer pageSize, Integer pageNumber) {
    if (CollectionUtils.isEmpty(orderBys) && (pageSize == null || pageNumber == null)) {
      return null;
    }
    Paginator paginator = new Paginator();
    paginator.setPageSize(pageSize);
    paginator.setPageNum(pageNumber);

    if (!CollectionUtils.isEmpty(orderBys)) {
      OrderByDTO orderBysFirst = orderBys.getFirst();
      Boolean isAsc = orderBysFirst.getIsAsc();
      String field = orderBysFirst.getField();
      paginator.setAsc(Boolean.TRUE.equals(isAsc));
      paginator.setIdColumn("id");
      paginator.setTimeColumn(field);
    }
    return paginator;
  }
}
