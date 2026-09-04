package com.iwhalecloud.bote.doc.module.knowledge.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class AddDocumentResp {
  private List<DocumentDTO> documents;
  private List<String> fails;
  private List<String> success;
}
