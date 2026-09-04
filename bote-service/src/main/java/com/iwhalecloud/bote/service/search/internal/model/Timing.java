package com.iwhalecloud.bote.service.search.internal.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Timing {
  private String engine;
  private double total;
  private double load;

  public Timing(String engine, double total, double load) {
    this.engine = engine;
    this.total = total;
    this.load = load;
  }
}
