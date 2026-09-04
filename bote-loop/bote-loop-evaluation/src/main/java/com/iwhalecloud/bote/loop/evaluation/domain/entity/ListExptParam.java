package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ListExptParam {
  private Integer pageNumber;
  private Integer pageSize;
  private Long spaceId;
  private ExptListFilter filter;
  private List<OrderBy> orders;
  private Session session;
  private Long catalogItemId;
}
