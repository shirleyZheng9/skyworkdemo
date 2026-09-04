package com.iwhalecloud.bote.service.search.internal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UnresponsiveEngine {
  private String engine;
  private String errorType;
  private boolean suspended;
}
