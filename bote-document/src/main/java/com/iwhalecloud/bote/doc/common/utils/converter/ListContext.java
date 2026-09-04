package com.iwhalecloud.bote.doc.common.utils.converter;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListContext {
  // 当前打开的列表栈，每个元素是 [listID, listLevel, isOrdered, element]
  private List<ListInfo> openLists;
}
