package com.iwhalecloud.bote.dto.chat;

import com.iwhalecloud.bote.doc.module.person.dto.homepage.SearchDocumentDTO;
import com.iwhalecloud.bote.dto.app.SimpleWebAppDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 通用搜索结果
 *
 * @author chen.linfa
 * @since 2025-10-14
 */
@Getter
@Setter
@ToString
public class SearchResultDTO {

  @Schema(description = "AI 助理")
  private List<SimpleBotDTO> bots;

  @Schema(description = "网页应用")
  private List<SimpleWebAppDTO> webs;

  @Schema(description = "文档")
  private List<SearchDocumentDTO> documents;
}
