package com.iwhalecloud.bote.dto.suggestion;

import com.iwhalecloud.bote.dto.base.BoteSuggestionResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 联想术语搜索结果DTO
 *
 * @author lizuyin
 * @since 2025-07-04
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "联想术语搜索结果")
public class SuggestionTermSearchDTO {

    @Schema(description = "match查询结果列表")
    private List<BoteSuggestionResponse> matchResults;

    @Schema(description = "建议查询结果列表")
    private List<BoteSuggestionResponse> suggestionResults;

    /**
     * 构造函数
     */
    public SuggestionTermSearchDTO() {
    }

    /**
     * 构造函数
     *
     * @param matchResults match查询结果列表
     * @param suggestionResults 建议查询结果列表
     */
    public SuggestionTermSearchDTO(List<BoteSuggestionResponse> matchResults, List<BoteSuggestionResponse> suggestionResults) {
        this.matchResults = matchResults;
        this.suggestionResults = suggestionResults;
    }
}
