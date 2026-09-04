package com.iwhalecloud.bote.doc.module.knowledge.dto;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档切片数据
 *
 * @author qian.sisheng
 * @since 2025-07-03
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "参考文档")
public class ReferenceChunkResponseData {

  /** 文档名称 */
  private String docName;

  /** 标题 */
  @JsonAlias("heading_chain")
  private String heading;

  /** 内容 */
  private String content;

  public void convert() {
    if (this.heading == null) {
      return;
    }
    // 标题格式为 "文档名称#标题链"，有的文档没有标题（比如 excel, txt），则格式为 "文档名称"
    String[] headingParts = StringUtils.split(StringUtils.defaultString(this.heading), "#", 2);
    this.docName = headingParts.length > 0 ? headingParts[0] : null;
    this.heading = headingParts.length == 2 ? headingParts[1] : docName;
  }

}
