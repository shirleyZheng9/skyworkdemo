package com.iwhalecloud.bote.doc.common.utils.converter;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Element;

@Getter
@Setter
public class ListInfo {
  private Integer listID;
  private Integer level;
  private boolean isOrdered;
  private Element element;

  public ListInfo(Integer listID, Integer level, boolean isOrdered, Element element) {
    this.listID = listID;
    this.level = level;
    this.isOrdered = isOrdered;
    this.element = element;
  }

}
